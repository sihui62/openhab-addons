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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.GroupItemStateChangedEvent;
import org.openhab.core.items.events.ItemAddedEvent;
import org.openhab.core.items.events.ItemRemovedEvent;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
    private static final Set<String> ITEM_REGISTRY_EVENTS = Set.of(ItemAddedEvent.TYPE,
            ItemRemovedEvent.TYPE /* ,ItemUpdatedEvent.TYPE, */);
    private static final Set<String> ITEM_STATE_EVENTS = Set.of(GroupItemStateChangedEvent.TYPE, ItemStateEvent.TYPE,
            ItemStateChangedEvent.TYPE);

    private final Logger logger = LoggerFactory.getLogger(FuzzyLogicEventSubscriber.class);
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final ItemRegistry itemRegistry;
    private final Map<String, Set<PropertyChangeListener>> pendingItems = new HashMap<>();

    @Activate
    public FuzzyLogicEventSubscriber(@Reference ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Stream.concat(ITEM_REGISTRY_EVENTS.stream(), ITEM_STATE_EVENTS.stream()).collect(Collectors.toSet());
    }

    @Override
    public void receive(Event event) {
        logger.debug("Received event '{}' with topic '{}' and payload '{}'", event.getType(), event.getTopic(),
                event.getPayload());
        if (ITEM_REGISTRY_EVENTS.contains(event.getType())) {
            if (event instanceof ItemAddedEvent) {
                ItemAddedEvent addedEvent = (ItemAddedEvent) event;
                String itemName = addedEvent.getItem().name;
                Set<PropertyChangeListener> pcls = pendingItems.remove(itemName);
                if (pcls != null) { // I was waiting this item
                    pcls.forEach(pcl -> propertyChangeSupport.addPropertyChangeListener(itemName, pcl));
                }
            } else if (event instanceof ItemRemovedEvent) {
                ItemRemovedEvent removedEvent = (ItemRemovedEvent) event;
                String itemName = removedEvent.getItem().name;
                List<PropertyChangeListener> pcls = Arrays
                        .asList(propertyChangeSupport.getPropertyChangeListeners(itemName));
                pendingItems.put(itemName, new HashSet<PropertyChangeListener>(pcls));
                pcls.forEach(pcl -> removePropertyChangeListener(itemName, pcl));
            }
        } else if (ITEM_STATE_EVENTS.contains(event.getType())) {
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
                    propertyChangeSupport.firePropertyChange(stateEvent.getItemName(), null, newState);
                }
            }
        }
    }

    public Optional<Item> addPropertyChangeListener(String itemName, PropertyChangeListener pcl) {
        logger.debug("Adding listener for FuzzyLogicEventSubscriber");
        Item item = itemRegistry.get(itemName);
        if (item != null) {
            propertyChangeSupport.addPropertyChangeListener(itemName, pcl);
        } else {
            Set<PropertyChangeListener> pcls = pendingItems.remove(itemName);
            if (pcls == null) {
                pcls = new HashSet<PropertyChangeListener>();
            }
            pcls.add(pcl);
            pendingItems.put(itemName, pcls);
            logger.info("Input item `{}` not found in registry", itemName);
        }
        return Optional.ofNullable(item);
    }

    public void removePropertyChangeListener(String itemName, PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(itemName, pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);

        pendingItems.forEach((key, pcls) -> pcls.remove(pcl));
        pendingItems.entrySet().removeIf(e -> e.getValue().isEmpty());
    }
}
