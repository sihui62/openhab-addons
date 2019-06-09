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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;

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
    public static final ThingTypeUID THING_TYPE_VROLLO = new ThingTypeUID(BINDING_ID, "vrollo");
    public static final ThingTypeUID THING_TYPE_VGARAGE = new ThingTypeUID(BINDING_ID, "vgaragedoor");

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

    // Virtual Rollo Channels
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public static final String CHANNEL_ACTUATOR = "actuator";
    public static final String CHANNEL_OPENED = "opened";
    public static final String CHANNEL_CLOSED = "closed";
    public static final String CHANNEL_STATUS = "status";

    public static final State ROLLO_STATUS_CLOSED = new StringType("closed");
    public static final State ROLLO_STATUS_OPENED = new StringType("opened");
    public static final State ROLLO_STATUS_MOVEUP = new StringType("movingup");
    public static final State ROLLO_STATUS_MOVEDOWN = new StringType("movingdown");
    public static final State ROLLO_STATUS_STOPPED = new StringType("stopped");
}
