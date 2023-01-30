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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.fuzzylogicscripting.internal.FuzzyLogicConstants;
import org.openhab.automation.fuzzylogicscripting.internal.events.FuzzyLogicEventSubscriber;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.service.AbstractWatchService;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * The {@link CompilerService} compiles the fcl files
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = { CompilerService.class }, configurationPid = "automation.fuzzylogic")
public class CompilerService extends AbstractWatchService implements PropertyChangeListener {
    private final Logger logger = LoggerFactory.getLogger(CompilerService.class);
    private final Map<String, FIS> scripts = new HashMap<>();
    private final Map<String, Set<String>> items = new HashMap<>();
    private final ItemRegistry itemRegistry;
    private final FuzzyLogicEventSubscriber eventSubscriber;
    private final EventPublisher publisher;

    @Activate
    public CompilerService(@Reference ItemRegistry itemRegistry, @Reference FuzzyLogicEventSubscriber eventSubscriber,
            @Reference EventPublisher publisher, Map<String, Object> properties) {
        super(FCL_DIR.toString());
        this.itemRegistry = itemRegistry;
        this.eventSubscriber = eventSubscriber;
        this.publisher = publisher;

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
            List<Path> scriptFiles = scriptFileStream.filter(FuzzyLogicConstants.FCL_FILE_FILTER).toList();
            logger.debug("Scripts to load from '{}' to memory: {}", FCL_DIR, scriptFiles);
            scriptFiles.forEach(scriptFile -> processScript(scriptFile));
        } catch (IOException e) {
            logger.warn("Could not load scripts: {}", e.getMessage());
        }
    }

    private void processScript(Path jclPath) {
        try {
            File jclFile = jclPath.toFile();
            String scriptName = jclFile.getName();
            if (jclFile.length() > 0) {
                FileInputStream inputStream = new FileInputStream(jclFile);
                FIS fis = FIS.load(inputStream);
                boolean inputOk = true;
                boolean outputOk = true;
                Iterator<FunctionBlock> it = fis.iterator();
                while (it.hasNext()) {
                    FunctionBlock fb = it.next();
                    inputOk = inputOk && searchInputVariables(fb.variables(), scriptName, jclFile.getAbsolutePath());
                }

                it = fis.iterator();
                while (it.hasNext()) {
                    FunctionBlock fb = it.next();
                    outputOk = outputOk && searchOutputVariables(fb.variables(), scriptName);
                }

                if (inputOk && outputOk) {
                    scripts.put(jclFile.getAbsolutePath(), fis);
                    logger.info("Script '{}' added.", scriptName);
                }
            } else {
                logger.info("Script file `{}` is empty, ignored", scriptName);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean searchOutputVariables(Collection<Variable> variables, String scriptName) {
        for (Variable variable : variables) {
            if (variable.isOutput()) {
                String name = variable.getName();
                Item item = itemRegistry.get(name);
                if (item != null) {
                    // List<Class<? extends State>> dataTypes = item.getAcceptedDataTypes();
                    // if (validInputTypes(dataTypes)) {
                    // } else {
                    // logger.warn("File `{}`, output item `{}` is not a numeric value, script will be ignored",
                    // scriptName, name);
                    // return false;
                    // }
                    return true;
                } else {
                    logger.warn("File `{}`, output item `{}` not found, script will be ignored", scriptName, name);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean searchInputVariables(Collection<Variable> variables, String scriptName, String fullPath) {
        for (Variable variable : variables) {
            if (variable.isInput()) {
                String name = variable.getName();
                Item item = itemRegistry.get(name);
                if (item != null) {
                    List<Class<? extends State>> dataTypes = item.getAcceptedDataTypes();
                    if (validInputTypes(dataTypes)) {
                        double value = getDoubleState(item.getState());
                        variable.setValue(value);
                        Set<String> tracked = items.get(name);
                        if (tracked == null) {
                            tracked = new HashSet<>();
                            items.put(name, tracked);
                            eventSubscriber.addPropertyChangeListener(name, this);
                        }
                        logger.info("File `{}`, input item `{}` will monitored", scriptName, name);
                        tracked.add(fullPath);
                    } else {
                        logger.warn("File `{}`, input item `{}` is not a numeric value, script will be ignored",
                                scriptName, name);
                        return false;
                    }
                } else {
                    logger.warn("File `{}`, input item `{}` not found, script will be ignored", scriptName, name);
                    return false;
                }
            }
        }
        return true;
    }

    private double getDoubleState(State state) {
        if (state instanceof UnDefType) {
            return Double.NaN;
        } else if (state instanceof DecimalType decimal) {
            return decimal.doubleValue();
        } else if (state instanceof QuantityType<?> quantity) {
            return quantity.doubleValue();
        }
        return Double.NaN;
    }

    private boolean validInputTypes(List<Class<? extends State>> dataTypes) {
        return dataTypes.contains(UnDefType.class) && dataTypes.contains(DecimalType.class)
        // Il manque PercentType et probablement booleans
                && dataTypes.contains(QuantityType.class) && dataTypes.size() <= 3;
    }

    /*
     * following methods implement the watch service
     */
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
        String fileName = path.getFileName().toString();
        if (fileName.endsWith(FCL_FILE_TYPE)) {
            // try {
            if (kind == ENTRY_DELETE) {
            } else if (kind == ENTRY_MODIFY) {
                String absolutePath = path.toString();
                if (scripts.containsKey(absolutePath)) {
                    logger.info("Script '{}' modified, removing from cache", fileName);
                    scripts.remove(absolutePath);
                }
                processScript(path);
                // if (Files.exists(targetPath) && Files.readString(path).equals(Files.readString(targetPath))) {
                // file already exists and has same content, no need to rebuild
                // return;
                // }
            }
            // Files.createDirectories(targetPath.getParent());
            // Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
            // buildJavaRuleDependenciesJar();
            // } catch (IOException e) {
            // logger.warn("Failed to process event '{}' for '{}': {}", kind, path, e.getMessage());
            // }
        } else {
            logger.trace("Received '{}' for path '{}' - ignoring (wrong extension)", kind, path);
        }
    }

    @Override
    public void propertyChange(@NonNullByDefault({}) PropertyChangeEvent event) {
        logger.info("Property changed {} : {}", event.getPropertyName(), event.getNewValue());
        Set<String> associatedScripts = items.get(event.getPropertyName());
        if (associatedScripts != null) {
            associatedScripts.forEach(script -> {
                FIS fis = scripts.get(script);
                if (fis != null) {
                    Variable variable = fis.getVariable(event.getPropertyName());
                    if (variable != null) {
                        variable.setValue((Double) event.getNewValue());
                    }
                    fis.evaluate();
                    Iterator<FunctionBlock> it = fis.iterator();
                    while (it.hasNext()) {
                        FunctionBlock fb = it.next();
                        for (Variable vari : fb.variables()) {
                            if (vari.isOutput()) {
                                String name = vari.getName();
                                Item item = itemRegistry.get(name);
                                if (item == null) {
                                    logger.error("Unable to send output to {}, it does not exist in the registry",
                                            name);
                                } else {
                                    State state = null;
                                    if (item instanceof NumberItem) {
                                        state = TypeParser.parseState(item.getAcceptedDataTypes(),
                                                Double.toString(vari.getValue()));
                                    } else if (item instanceof StringItem) {
                                        String term = vari.getLatestTerm();
                                        if (term != null) {
                                            state = TypeParser.parseState(item.getAcceptedDataTypes(), term);
                                        }
                                    }
                                    if (state != null) {
                                        publisher.post(ItemEventFactory.createStateEvent(name, state));
                                    }
                                }

                            }
                        }
                    }
                }
            });
        }
    }

}
