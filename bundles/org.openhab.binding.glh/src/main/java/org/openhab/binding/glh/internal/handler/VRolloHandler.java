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

import static org.openhab.binding.glh.internal.GlhBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.glh.internal.VRolloConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VRolloHandler} is responsible for updating calculated facade
 * illumination data.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VRolloHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VRolloHandler.class);
    private int upUpdateDelay;
    private int downUpdateDelay;
    private int currentPosition;
    private @NonNullByDefault({}) OnOffType ongoingAction;
    private @Nullable ScheduledFuture<?> positionUpdater;

    public VRolloHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        VRolloConfiguration config = getConfigAs(VRolloConfiguration.class);
        upUpdateDelay = config.timeUp / 10;
        downUpdateDelay = config.timeDown / 10;
        updateStatus(ThingStatus.ONLINE);
        setCurrentPosition(0);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        switch (id) {
            case CHANNEL_ROLLERSHUTTER:
                if (command instanceof UpDownType) {
                    // Pour openhab 100= 100% fermé
                    movetoTarget((UpDownType) command == UpDownType.UP ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    if ((StopMoveType) command == StopMoveType.STOP && ongoingAction != null) {
                        terminateMove();
                    }
                } else if (command instanceof OnOffType) {
                    movetoTarget((OnOffType) command == OnOffType.ON ? 0 : 100);
                } else if (command instanceof QuantityType) {
                    movetoTarget(((QuantityType<?>) command).intValue());
                } else if (command instanceof DecimalType) {
                    movetoTarget(((DecimalType) command).intValue());
                }
                break;
        }
    }

    private void movetoTarget(int i) {
        if (currentPosition != i) {
            int toMove = currentPosition - i;
            OnOffType expectedAction = toMove > 0 ? OnOffType.ON : OnOffType.OFF;
            updateState(CHANNEL_STATUS, toMove > 0 ? ROLLO_STATUS_MOVEUP : ROLLO_STATUS_MOVEDOWN);
            if (ongoingAction == null) {
                ongoingAction = expectedAction;
                int delay = toMove > 0 ? upUpdateDelay : downUpdateDelay;

                updateActuator();
                positionUpdater = scheduler.scheduleWithFixedDelay(() -> {
                    int newPosition = currentPosition + (ongoingAction == OnOffType.ON ? -10 : 10);
                    if ((toMove < 0 && newPosition > i) || (toMove >= 0 && newPosition < i)) {
                        terminateMove();
                        setCurrentPosition(i);
                    } else {
                        setCurrentPosition(newPosition);
                    }
                }, delay, delay, TimeUnit.SECONDS);
            } else {
                if (ongoingAction != expectedAction) {
                    terminateMove();
                    movetoTarget(i);
                }
            }
        }

    }

    private void updateActuator() {
        postCommand(CHANNEL_ACTUATOR, ongoingAction);
    }

    private void setCurrentPosition(int i) {
        currentPosition = i;
        if (currentPosition < 0) {
            currentPosition = 0;
        }
        if (currentPosition > 100) {
            currentPosition = 100;
        }
        QuantityType<Dimensionless> state = new QuantityType<>(currentPosition, SmartHomeUnits.PERCENT);
        updateState(CHANNEL_ROLLERSHUTTER, state);
        if (i == 100) {
            updateState(CHANNEL_STATUS, ROLLO_STATUS_CLOSED);
        } else if (i == 0) {
            updateState(CHANNEL_STATUS, ROLLO_STATUS_OPENED);
        }
    }

    private void terminateMove() {
        updateActuator();
        ongoingAction = null;
        cancelPositionUpdater();
        updateState(CHANNEL_STATUS, ROLLO_STATUS_STOPPED);
    }

    private void cancelPositionUpdater() {
        if (positionUpdater != null) {
            positionUpdater.cancel(true);
            positionUpdater = null;
        }
    }

}
