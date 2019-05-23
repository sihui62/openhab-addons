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
package org.openhab.binding.glh.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ThermostatBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class GlhBindingConstants {

    public static final String BINDING_ID = "glh";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public static final ThingTypeUID THING_TYPE_FACADE = new ThingTypeUID(BINDING_ID, "facade");

    // Facade Channel ids
    public static final String SUN_AZIMUTH = "sunAzimuth";
    public static final String FACADE_BEARING = "bearing";
    public static final String FACADE_FACING = "facingSun";
    public static final String FACADE_SIDE = "side";

    // event channelIds
    public static final String EVENT_FACADE = "facadeEvent";
    public static final String EVENT_ENTER_FACADE = "SUN_ENTER";
    public static final String EVENT_LEAVE_FACADE = "SUN_LEAVE";
    public static final String EVENT_FRONT_FACADE = "SUN_FRONT";

    // Thermostat Channel ids
    public static final String CHANNEL_TEMPERATURE = "Temperature";
    public static final String CHANNEL_HUMIDITY = "Humidity";
    public static final String CHANNEL_HUMIDEX = "Humidex";

    public static final String CHANNEL_SETPOINT_MODE = "SetpointMode";
    public static final String CHANNEL_SETPOINT_TEMP = "Sp_Temperature";
    public static final String CHANNEL_THERM_RELAY = "ThermRelayCmd";
    public static final String CHANNEL_DURATION = "duration";

    public static final String SETPOINT_MODE_MANUAL = "manual";
    public static final String SETPOINT_MODE_HG = "hg";
    public static final String SETPOINT_MODE_OFF = "off";
    public static final String SETPOINT_MODE_MAX = "max";
}
