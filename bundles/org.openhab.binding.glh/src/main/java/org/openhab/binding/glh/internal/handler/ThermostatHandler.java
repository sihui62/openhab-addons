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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.glh.internal.ThermostatConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ThermostatHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ThermostatHandler.class);

    private double currentTemperature;
    private double currentHygro;
    private double targetTemperature;
    private double histeresis;

    private double humidex;
    private @NonNullByDefault({}) ThermostatConfiguration configuration;
    private String targetMode;
    private State heatingState = UnDefType.UNDEF;

    private @Nullable ZonedDateTime lastOn;

    public ThermostatHandler(Thing thing) {
        super(thing);
        targetMode = SETPOINT_MODE_OFF;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        configuration = this.getConfigAs(ThermostatConfiguration.class);
        histeresis = configuration.histeresis;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        switch (id) {
            case CHANNEL_TEMPERATURE:
                if (command instanceof QuantityType) {
                    @SuppressWarnings("unchecked")
                    QuantityType<Temperature> newTemp = (QuantityType<Temperature>) command;
                    this.currentTemperature = newTemp.doubleValue();
                } else if (command instanceof DecimalType) {
                    this.currentTemperature = ((DecimalType) command).doubleValue();
                }
                updateCalculations();
                break;
            case CHANNEL_HUMIDITY:
                if (command instanceof QuantityType) {
                    @SuppressWarnings("unchecked")
                    QuantityType<Dimensionless> newHumidity = (QuantityType<Dimensionless>) command;
                    this.currentHygro = newHumidity.doubleValue();
                } else if (command instanceof DecimalType) {
                    this.currentHygro = ((DecimalType) command).doubleValue();
                }
                updateCalculations();
                break;
            case CHANNEL_SETPOINT_MODE:
                this.targetMode = command.toString();
                switch (targetMode) {
                    case SETPOINT_MODE_HG:
                        targetTemperature = 10;
                        break;
                    case SETPOINT_MODE_OFF:
                    case SETPOINT_MODE_MAX:
                        targetTemperature = -1;
                        break;
                }
                updateCalculations();
                break;
            case CHANNEL_SETPOINT_TEMP: {
                BigDecimal spTemp = null;
                if (command instanceof QuantityType) {
                    @SuppressWarnings("unchecked")
                    QuantityType<Temperature> quantity = ((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS);
                    if (quantity != null) {
                        spTemp = quantity.toBigDecimal().setScale(1, RoundingMode.HALF_UP);
                    }
                } else if (command instanceof DecimalType) {
                    spTemp = new BigDecimal(command.toString()).setScale(1, RoundingMode.HALF_UP);
                }
                if (spTemp != null) {
                    this.targetMode = SETPOINT_MODE_MANUAL;
                    updateState(CHANNEL_SETPOINT_MODE, new StringType(this.targetMode));
                    this.targetTemperature = spTemp.doubleValue();
                    updateCalculations();
                }
                break;
            }
            default:
                logger.debug("The binding can not handle command: {} on channel: {}", command, channelUID);
        }
    }

    private void updateCalculations() {
        this.humidex = getHumidex(currentTemperature, currentHygro);
        updateState(CHANNEL_HUMIDEX, new DecimalType(humidex));
        updateState(CHANNEL_SETPOINT_TEMP, new QuantityType<Temperature>(targetTemperature, SIUnits.CELSIUS));
        switch (targetMode) {
            case SETPOINT_MODE_OFF:
                setHeatingState(OnOffType.OFF);
                break;
            case SETPOINT_MODE_MAX:
                setHeatingState(OnOffType.ON);
                break;
            default:
                double consideredTemperature = configuration.useHumidex ? humidex : currentTemperature;
                if (consideredTemperature > targetTemperature + histeresis) {
                    setHeatingState(OnOffType.OFF);
                } else if (consideredTemperature < targetTemperature - histeresis) {
                    setHeatingState(OnOffType.ON);
                }

        }
    }

    private void setHeatingState(OnOffType newState) {
        if (newState != heatingState) {
            if (newState == OnOffType.ON) {
                lastOn = ZonedDateTime.now();
            } else if (lastOn != null) {
                long sinceLastChange = Duration.between(lastOn, ZonedDateTime.now()).toMillis();
                updateState(CHANNEL_DURATION, new QuantityType<>(sinceLastChange / 1000, SmartHomeUnits.SECOND));
            }
            heatingState = newState;
            postCommand(CHANNEL_THERM_RELAY, newState);
            // updateState(CHANNEL_THERM_RELAY, heatingState);
        }
    }

    /**
     * Compute the Humidex index given temperature and hygrometry
     *
     *
     * @param temperature in (°C)
     * @param hygro       relative level (%)
     * @return Humidex index value
     */
    public static double getHumidex(double temperature, double hygro) {
        double result = 6.112 * Math.pow(10, 7.5 * temperature / (237.7 + temperature)) * hygro / 100;
        result = temperature + 0.555555556 * (result - 10);
        return result;
    }

}
