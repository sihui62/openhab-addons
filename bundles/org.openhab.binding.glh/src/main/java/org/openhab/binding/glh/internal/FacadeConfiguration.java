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

/**
 * The {@link FacadeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FacadeConfiguration {
    private Integer orientation = 90;
    private Integer noffset = 90;
    private Integer poffset = 90;
    private Integer margin = 2;

    /**
     * Returns the orientation.
     */
    public Integer getOrientation() {
        return orientation;
    }

    /**
     * Returns the Negative Offset.
     */
    public Integer getNegativeOffset() {
        return noffset;
    }

    /**
     * Returns the Positive Offset.
     */
    public Integer getPositiveOffset() {
        return poffset;
    }

    /**
     * Returns the Margin.
     */
    public Integer getMargin() {
        return margin;
    }
}
