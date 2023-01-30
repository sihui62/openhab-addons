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
package org.openhab.automation.fuzzylogicscripting.internal;

import java.util.List;
import java.util.stream.Stream;

import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.fuzzylogicscripting.internal.compiler.CompilerService;
import org.openhab.automation.fuzzylogicscripting.internal.script.JFuzzyLogicScriptEngineFactory;
import org.openhab.core.automation.module.script.AbstractScriptEngineFactory;
import org.openhab.core.automation.module.script.ScriptEngineFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This is an implementation of a {@link ScriptEngineFactory} for jFuzzyLogic.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = ScriptEngineFactory.class)
@NonNullByDefault
public class FuzzyLogicFactory extends AbstractScriptEngineFactory {
    private final JFuzzyLogicScriptEngineFactory factory;
    private final List<String> scriptTypes;

    @Activate
    public FuzzyLogicFactory(@Reference CompilerService compilerService) {
        this.factory = new JFuzzyLogicScriptEngineFactory(compilerService);
        this.scriptTypes = Stream.of(factory.getExtensions(), factory.getMimeTypes()).flatMap(List::stream).toList();
    }

    @Override
    public List<String> getScriptTypes() {
        return scriptTypes;
    }

    @Override
    public @Nullable ScriptEngine createScriptEngine(String scriptType) {
        return scriptTypes.contains(scriptType) ? null /* factory.getScriptEngine() */ : null;
    }
}
