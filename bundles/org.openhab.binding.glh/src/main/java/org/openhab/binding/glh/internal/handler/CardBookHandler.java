/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.glh.internal.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.glh.internal.CardBookConfiguration;
import org.openhab.binding.glh.internal.dto.Contact;
import org.openhab.binding.glh.internal.dto.Email;
import org.openhab.binding.glh.internal.dto.PhoneNumber;
import org.openhab.io.transport.webdav.WebDAVManager;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sardine.DavResource;

import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.vcard.VCard;
import net.sourceforge.cardme.vcard.exceptions.VCardParseException;
import net.sourceforge.cardme.vcard.types.BDayType;
import net.sourceforge.cardme.vcard.types.EmailType;
import net.sourceforge.cardme.vcard.types.PhotoType;
import net.sourceforge.cardme.vcard.types.TelType;
import net.sourceforge.cardme.vcard.types.params.EmailParamType;
import net.sourceforge.cardme.vcard.types.params.TelParamType;

/**
 * The {@link CardBookHandler} is for getting informations from a vCard file
 * and making them available to OH2
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CardBookHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(CardBookHandler.class);

    private @NonNullByDefault({}) CardBookConfiguration config;
    private final VCardEngine vCardEngine = new VCardEngine();

    private @NonNullByDefault({}) WebDAVManager webDAVManager;

    public CardBookHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        config = getConfigAs(CardBookConfiguration.class);
        try {
            webDAVManager.defineFactory(config.domain, config.username, config.password);
            loadContacts();
            updateStatus(ThingStatus.ONLINE);
        } catch (VCardParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    private List<Contact> loadContacts() throws VCardParseException, IOException {
        logger.info("Starting CardBook listing...");
        List<Contact> contacts = new ArrayList<Contact>();
        List<DavResource> resources = webDAVManager
                .list("https://lhopital.org/nextcloud/remote.php/dav/addressbooks/users/gael/contacts/");
        File cardBook = new File(config.rootDirectory);

        for (File file : cardBook.listFiles((d, name) -> name.endsWith(".vcf"))) {
            VCard vcard = vCardEngine.parse(file);
            Contact contact = new Contact(file.getName().replace(".vcf", ""), vcard.getN().getGivenName(),
                    vcard.getN().getFamilyName());
            if (vcard.hasBDay()) {
                BDayType birthday = vcard.getBDay();
                contact.setBirthday(birthday.getBirthday());
            }
            if (vcard.hasTels()) {
                for (TelType telType : vcard.getTels()) {
                    PhoneNumber telephone = new PhoneNumber();
                    StringBuilder sb = new StringBuilder();
                    if (telType.getParams() != null) {
                        for (TelParamType telParamType : telType.getParams()) {
                            if (sb.length() > 0) {
                                sb.append(", ");
                            }
                            sb.append(telParamType.getDescription());
                        }
                    }
                    telephone.setType(sb.toString());
                    telephone.setNumber(telType.getTelephone());
                    contact.getPhoneNumbers().add(telephone);
                }
            }
            if (vcard.hasEmails()) {
                for (EmailType emailType : vcard.getEmails()) {
                    Email email = new Email();
                    StringBuilder sb = new StringBuilder();
                    if (emailType.getParams() != null) {
                        for (EmailParamType emailParamType : emailType.getParams()) {
                            if (sb.length() > 0) {
                                sb.append(", ");
                            }
                            sb.append(emailParamType.getDescription());
                        }
                    }
                    email.setType(sb.toString());
                    email.setEmail(emailType.getEmail());
                    contact.getEmails().add(email);
                    if (vcard.hasCategories()) {
                        contact.getCategories().addAll(vcard.getCategories().getCategories());
                    }
                }
            }
            if (vcard.hasPhotos()) {
                PhotoType photo = vcard.getPhotos().get(0);
                contact.setPhoto(new RawType(photo.getPhoto(), "application/octet-stream"));
            }
            logger.debug("found contact: {} : telephones: {}", contact.getFullName());
            contacts.add(contact);
        }

        String match = config.matchCategory.trim();

        Predicate<Contact> isQualified = c -> c.hasFullName()
                && (c.hasBirthday() || c.hasEmails() || c.hasPhoneNumbers())
                && (c.ofCategory(match) || match.isEmpty());

        contacts = contacts.stream().filter(isQualified).collect(Collectors.toList());

        return contacts;
    }

    @Reference
    protected void setWebDAVManager(WebDAVManager webDAVManager) {
        this.webDAVManager = webDAVManager;
    }

    protected void unsetWebDAVManager(WebDAVManager webDAVManager) {
        this.webDAVManager = null;
    }

}
