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
package org.openhab.io.transport.webdav.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.transport.webdav.WebDAVManager;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

/**
 * This service provides functionality around ephemeris services and is the central service to be used directly by
 * others.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(name = "org.openhab.io.transport.webdav", property = {
        Constants.SERVICE_PID + "=org.openhab.io.transport.webdav" })
@NonNullByDefault
public class WebDAVManagerImpl implements WebDAVManager {

    private final Logger logger = LoggerFactory.getLogger(WebDAVManagerImpl.class);

    private final Map<String, Sardine> factories = new HashMap<>();

    @Activate
    public WebDAVManagerImpl() {
        logger.info("WebDAV Manager started");
    }

    @Override
    public void defineFactory(String domain, String username, String password) {
        factories.getOrDefault(domain, SardineFactory.begin(username, password));
    }

    @Override
    public List<DavResource> list(String path) throws MalformedURLException, IOException {
        URL url = new URL(path);
        String key = url.getProtocol() + url.getHost();
        if (factories.containsKey(key)) {
            Sardine factory = factories.get(key);
            return factory.list(path);
        } else {
            throw new IOException("Domain not found");
        }
    }
}
