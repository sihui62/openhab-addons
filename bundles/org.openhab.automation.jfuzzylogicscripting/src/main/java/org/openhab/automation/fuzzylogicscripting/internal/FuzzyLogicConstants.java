/**
 * Copyright (c) 2021-2023 Contributors to the SmartHome/J project
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
package org.openhab.automation.fuzzylogicscripting.internal;

import java.nio.file.Path;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;

/**
 * The {@link FuzzyLogicConstants} class defines common constants, which are used across the Fuzzy Logic automation.
 *
 * @author Gaël L'hopitals - Initial contribution
 *
 */
@NonNullByDefault
public class FuzzyLogicConstants {
    public static final String SOFTWARE_NAME = "JFuzzyLogic";
    public static final String REVISION = "";
    public static final String BUILD = "2015-04-09";
    public static final String VERSION_MAJOR = "3.3";
    public static final String VERSION_SHORT = VERSION_MAJOR + REVISION;
    public static final String BY = "Pablo Cingolani";
    public static final String VERSION_NO_NAME = VERSION_SHORT + " (build " + BUILD + "), by " + BY;
    public static final String VERSION = SOFTWARE_NAME + " " + VERSION_NO_NAME;

    public static final String EMAIL = "pablo.e.cingolani@gmail.com";
    public static final String SCRIPT_TYPE = "fcl";

    public static final Path FCL_DIR = Path.of(OpenHAB.getConfigFolder(), "automation", SCRIPT_TYPE);

    public static final String FCL_FILE_TYPE = "." + SCRIPT_TYPE;
    public static final Predicate<Path> FCL_FILE_FILTER = p -> p.toString().endsWith(FCL_FILE_TYPE);
}
