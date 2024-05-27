/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.meteoalerte.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An exception that occurred while communicating with Meteo Alerte API server or related processes.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MeteoAlerteException extends Exception {

    private static final long serialVersionUID = 7613161188837438233L;

    public MeteoAlerteException(String format, Object... args) {
        super(format.formatted(args));
    }

    public MeteoAlerteException(Exception e, String format, Object... args) {
        super(format.formatted(args), e);
    }

    public MeteoAlerteException(String message) {
        super(message);
    }
}
