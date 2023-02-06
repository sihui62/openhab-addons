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

import static org.openhab.core.types.TypeParser.parseState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.fuzzylogicscripting.internal.events.FuzzyLogicEventSubscriber;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.jFuzzyLogic.rule.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * The {@link FclCompilerService} compiles the fcl files
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = { FclCompilerService.class }, configurationPid = "automation.fuzzylogic")
public class FclCompilerService {
    private final Logger logger = LoggerFactory.getLogger(FclCompilerService.class);
    private final Set<ScriptItemLinker> scriptItems = new HashSet<>();
    private final ItemRegistry itemRegistry;
    private final FuzzyLogicEventSubscriber eventSubscriber;
    private final EventPublisher publisher;

    private class ScriptItemLinker implements PropertyChangeListener {
        private final Path scriptPath;
        private final Map<FunctionBlock, Set<String>> inputs = new HashMap<>();

        public ScriptItemLinker(Path scriptPath, InferenceSystem fis) {
            this.scriptPath = scriptPath;

            fis.forEach(fb -> {
                logger.info("Identified FunctionBlock {} in script {}", fb.getName(), scriptPath);
                inputs.put(fb, fb.getVariables().values().stream().filter(Variable::isInput).map(Variable::getName)
                        .collect(Collectors.toSet()));
            });

            inputs.values().stream().flatMap(Set::stream).collect(Collectors.toSet()).forEach(varName -> {
                logger.info("Monitoring item named {}", varName);
                Optional<Item> item = eventSubscriber.addPropertyChangeListener(varName, this);
                item.ifPresent(i -> setVariable(i.getName(), i.getState()));
            });
        }

        public void setVariable(String itemName, State itemState) {
            getDouble(itemState).ifPresentOrElse(itemValue -> {
                inputs.entrySet().stream().filter(e -> e.getValue().contains(itemName)).map(Entry::getKey)
                        .forEach(fb -> fb.setVariableValue(itemName, itemValue));
            }, () -> logger.info("Item '{}' current state is not adequate to trigger '{}'", itemName, scriptPath));
        }

        public void dispose() {
            eventSubscriber.removePropertyChangeListener(this);
            inputs.clear();
        }

        @Override
        public void propertyChange(@NonNullByDefault({}) PropertyChangeEvent event) {
            String itemName = event.getPropertyName();
            logger.debug("Property changed {} : {}", itemName, event.getNewValue());
            setVariable(itemName, (State) event.getNewValue());

            inputs.entrySet().stream().filter(e -> e.getValue().contains(itemName)).map(Entry::getKey).forEach(fb -> {
                fb.evaluate();
                fb.getVariables().values().stream().filter(Variable::isOutput).forEach(result -> {
                    String outItemName = result.getName();
                    publishResults(outItemName, result);
                    publishResults(outItemName + "_Deffuzified", result);
                });
                fb.getVariables().values().stream().filter(Variable::isInput).forEach(result -> {
                    String outItemName = result.getName();
                    publishResults(outItemName + "_Fuzified", result);
                });

            });
        }

        private void publishResults(String targetName, Variable result) {
            Item item = itemRegistry.get(targetName);
            if (item == null) {
                logger.warn("Unable to send result to {}, it does not exist in the registry", targetName);
            } else {
                getState(item, result).ifPresentOrElse(
                        state -> publisher.post(ItemEventFactory.createStateEvent(targetName, state)),
                        () -> logger.warn("Unable to cast {} to proper item state.", targetName));
            }

        }

        private Optional<Double> getDouble(State state) {
            if (state instanceof UnDefType) {
                return Optional.of(Double.NaN);
            } else if (state instanceof DecimalType decimal) {
                return Optional.of(decimal.doubleValue());
            } else if (state instanceof QuantityType<?> quantity) {
                return Optional.of(quantity.doubleValue());
            }
            return Optional.empty();
        }

        private Optional<State> getState(Item item, Variable variable) {
            List<Class<? extends State>> dataTypes = item.getAcceptedDataTypes();
            if (item instanceof NumberItem) {
                return Optional.ofNullable(parseState(dataTypes, Double.toString(variable.getValue())));
            } else if (item instanceof StringItem) {
                String term = variable.getLatestTerm();
                return Optional.ofNullable(term != null ? parseState(dataTypes, term) : null);
            }
            return Optional.empty();
        }
    }

    @Activate
    public FclCompilerService(@Reference ItemRegistry itemRegistry,
            @Reference FuzzyLogicEventSubscriber eventSubscriber, @Reference EventPublisher publisher,
            Map<String, Object> properties) {
        this.itemRegistry = itemRegistry;
        this.eventSubscriber = eventSubscriber;
        this.publisher = publisher;
    }

    public void loadScript(Path jclPath) {
        File jclFile = jclPath.toFile();
        String scriptName = jclFile.getName();
        if (jclFile.length() > 0) {
            scriptItems.add(new ScriptItemLinker(jclPath, new InferenceSystem(jclPath.toString())));
            logger.info("Script '{}' added.", scriptName);
        } else {
            logger.info("Script file `{}` is empty, ignored", scriptName);
        }
    }

    public void removeScript(Path jclPath) {
        scriptItems.stream().filter(e -> e.scriptPath.equals(jclPath)).forEach(e -> e.dispose());
        scriptItems.removeIf(e -> e.scriptPath.equals(jclPath));
    }
}
