/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.automation.fuzzylogicscripting.internal.events;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.events.GroupItemStateChangedEvent;
import org.openhab.core.items.events.ItemAddedEvent;
import org.openhab.core.items.events.ItemRemovedEvent;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.items.events.ItemUpdatedEvent;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FuzzyLogicEventSubscriber}
 *
 * @author Gaël L'hopital - Initial contribution
 */

@Component(service = { EventSubscriber.class,
        FuzzyLogicEventSubscriber.class }, configurationPid = "automation.fuzzylogicscripting.eventsubscriber")
@NonNullByDefault
public class FuzzyLogicEventSubscriber implements EventSubscriber {
    private final Logger logger = LoggerFactory.getLogger(FuzzyLogicEventSubscriber.class);
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final Set<String> itemRegistryEvents = Set.of(ItemUpdatedEvent.TYPE, ItemAddedEvent.TYPE,
            ItemRemovedEvent.TYPE);
    private final Set<String> itemStateEvents = Set.of(GroupItemStateChangedEvent.TYPE, ItemStateEvent.TYPE,
            ItemStateChangedEvent.TYPE);
    private final Set<String> subscribedEventTypes = Stream
            .concat(itemRegistryEvents.stream(), itemStateEvents.stream()).collect(Collectors.toSet());

    @Override
    public Set<String> getSubscribedEventTypes() {
        return subscribedEventTypes;
    }

    @Override
    public void receive(Event event) {
        logger.debug("Received event '{}' with topic '{}' and payload '{}'", event.getType(), event.getTopic(),
                event.getPayload());
        if (itemRegistryEvents.contains(event.getType())) {
            logger.debug("event processed as itemRegistryEvent: topic {} payload: {}", event.getTopic(),
                    event.getPayload());
            propertyChangeSupport.firePropertyChange("aaa", null, event);
        } else if (itemStateEvents.contains(event.getType())) {
            if (event instanceof ItemStateChangedEvent) {
                // ItemStateChangedEvent stateEvent = (ItemStateChangedEvent) event;
                // logger.debug("Event processed as itemStateEvent: topic {} payload: {}", event.getTopic(),
                // event.getPayload());
                // if (propertyChangeSupport.hasListeners(stateEvent.getItemName())) {
                // State newState = stateEvent.getItemState();
                // if (newState instanceof DecimalType) {
                // propertyChangeSupport.firePropertyChange(stateEvent.getItemName(), null,
                // ((DecimalType) newState).doubleValue());
                // }
                // }
            } else if (event instanceof ItemStateEvent) {
                ItemStateEvent stateEvent = (ItemStateEvent) event;
                logger.debug("Event processed as itemStateEvent: topic {} payload: {}", event.getTopic(),
                        event.getPayload());
                if (propertyChangeSupport.hasListeners(stateEvent.getItemName())) {
                    State newState = stateEvent.getItemState();
                    if (newState instanceof DecimalType) {
                        propertyChangeSupport.firePropertyChange(stateEvent.getItemName(), null,
                                ((DecimalType) newState).doubleValue());
                    }
                }
            }
        }
    }

    public void addPropertyChangeListener(String itemName, PropertyChangeListener pcl) {
        logger.debug("Adding listener for FuzzyLogicEventSubscriber");
        propertyChangeSupport.addPropertyChangeListener(itemName, pcl);
    }

    public void removePropertyChangeListener(String itemName, PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(itemName, pcl);
    }

}
