/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal.api.dto;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AirQualityCity} is responsible for storing the "city" node from the waqi.org JSON response
 *
 * @author Kuba Wolanin - Initial contribution
 */
@NonNullByDefault
public class AirQualityCity {
    private String name = "";
    private @Nullable String url;
    private double[] geo = {};

    public String getName() {
        return name;
    }

    public @Nullable String getUrl() {
        return url;
    }

    public String getGeo() {
        return Arrays.stream(geo).mapToObj(Double::toString).collect(Collectors.joining(", "));
    }
}
