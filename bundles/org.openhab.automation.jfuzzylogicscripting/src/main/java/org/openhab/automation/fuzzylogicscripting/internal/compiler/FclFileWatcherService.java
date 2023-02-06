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
package org.openhab.automation.fuzzylogicscripting.internal.compiler;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.openhab.automation.fuzzylogicscripting.internal.FuzzyLogicConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.fuzzylogicscripting.internal.FuzzyLogicConstants;
import org.openhab.core.service.AbstractWatchService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FclFileWatcherService} initializes and monitors the dedicated folder containing fcl scripts
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = { FclFileWatcherService.class }, immediate = true, configurationPid = "automation.fuzzylogic")
public class FclFileWatcherService extends AbstractWatchService {
    private final Logger logger = LoggerFactory.getLogger(FclFileWatcherService.class);
    private final FclCompilerService compilerService;

    @Activate
    public FclFileWatcherService(@Reference FclCompilerService fclCompilerService) {
        super(FCL_DIR.toString());
        compilerService = fclCompilerService;

        try {
            Files.createDirectories(FCL_DIR);
        } catch (IOException e) {
            logger.warn("Failed to create directory '{}': {}", FCL_DIR, e.getMessage());
            throw new IllegalStateException("Failed to initialize fcl scripts folder.");
        }

        if (!Files.isWritable(FCL_DIR) || !Files.isReadable(FCL_DIR)) {
            logger.warn("Directory '{}' must be available for read and write", FCL_DIR);
            throw new IllegalStateException("Failed to initialize fcl scripts folder.");
        }

        try (Stream<Path> scriptFileStream = Files.list(FCL_DIR)) {
            scriptFileStream.filter(FuzzyLogicConstants.FCL_FILE_FILTER).toList().forEach(scriptFile -> {
                logger.debug("Loading script: {}", FCL_DIR, scriptFile);
                compilerService.loadScript(scriptFile);
            });
        } catch (IOException e) {
            logger.warn("Could not load scripts: {}", e.getMessage());
        }
    }

    @Override
    public boolean watchSubDirectories() {
        return true;
    }

    @Override
    public WatchEvent.Kind<?> @Nullable [] getWatchEventKinds(@Nullable Path directory) {
        return new WatchEvent.Kind<?>[] { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };
    }

    @Override
    public void processWatchEvent(@Nullable WatchEvent<?> event, @Nullable Kind<?> kind, @Nullable Path path) {
        if (path == null || kind == null || (kind != ENTRY_CREATE && kind != ENTRY_MODIFY && kind != ENTRY_DELETE)) {
            logger.trace("Received '{}' for path '{}' - ignoring (null or wrong kind)", kind, path);
            return;
        }
        String absolutePath = path.getFileName().toString();
        if (absolutePath.endsWith(FCL_FILE_TYPE)) {
            if (ENTRY_DELETE.equals(kind)) {
                compilerService.removeScript(path);
            } else if (ENTRY_MODIFY.equals(kind)) {
                compilerService.removeScript(path);
                compilerService.loadScript(path);
            } else if (ENTRY_CREATE.equals(kind)) {
                compilerService.loadScript(path);
            }
        } else {
            logger.trace("Received '{}' for path '{}' - ignoring (wrong extension)", kind, path);
        }
    }
}
