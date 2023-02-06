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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.jFuzzyLogic.fcl.FclLexer;
import net.sourceforge.jFuzzyLogic.fcl.FclParser;
import net.sourceforge.jFuzzyLogic.rule.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * The {@link InferenceSystem} holds the logic held by a given script file
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class InferenceSystem implements Iterable<FunctionBlock> {
    private final static Logger logger = LoggerFactory.getLogger(InferenceSystem.class);

    private final HashMap<String, FunctionBlock> functionBlocks = new HashMap<>(); // Function blocks indexed by name

    public InferenceSystem(String fclFilePath) {
        File jclFile = new File(fclFilePath);
        try {
            FileInputStream inputStream = new FileInputStream(jclFile);
            // Parse file (lexer first, then parser)
            FclLexer lexer = new FclLexer(new ANTLRInputStream(inputStream));
            // Parse tree and create FIS
            createFromLexer(lexer);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load script file : '" + fclFilePath + "'", e);
        } catch (RecognitionException e) {
            throw new IllegalStateException("Error parsing script file : '" + fclFilePath + "'", e);
        }
    }

    /**
     * Loads a "Fuzzy inference system (FIS)" from an FCL definition string
     *
     * @param lexer : lexer to use
     */
    private void createFromLexer(FclLexer lexer) throws RecognitionException {
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FclParser parser = new FclParser(tokens);
        Tree parseTree = (Tree) parser.main().getTree();

        if (parseTree != null) {
            logger.debug("Tree: " + parseTree.toStringTree());
            // Add every FunctionBlock (there may be more than one in each FCL file)
            for (int childNum = 0; childNum < parseTree.getChildCount(); childNum++) {
                Tree child = parseTree.getChild(childNum);

                logger.debug("Child " + childNum + ":\t" + child + "\tTree:'" + child.toStringTree() + "'");

                // Create a new FunctionBlock based on tree
                FunctionBlock functionBlock = new FunctionBlock(child);

                logger.debug("FunctionBlock Name: '" + functionBlock.getName() + "'");
                functionBlocks.put(functionBlock.getName(), functionBlock);
            }
        } else {
            logger.warn("Can't read script file.");
        }
    }

    public @Nullable Variable getVariable(String varName) {
        Iterator<FunctionBlock> it = iterator();
        while (it.hasNext()) {
            FunctionBlock fb = it.next();
            Variable var = fb.getVariable(varName);
            if (var != null) {
                return var;
            }
        }
        return null;
    }

    public Optional<FunctionBlock> getFunctionBlock(String name) {
        return Optional.ofNullable(functionBlocks.get(name));
    }

    @Override
    public Iterator<FunctionBlock> iterator() {
        return functionBlocks.values().stream().sorted().iterator();
    }

    public Set<String> getVariablesKind(Predicate<Variable> p) {
        Set<String> result = new HashSet<>();
        forEach(fb -> {
            fb.getVariables().values().stream().filter(p).forEach(var -> result.add(var.getName()));
        });
        return result;
    }

    public Set<String> getInputVariables() {
        return getVariablesKind(Variable::isInput);
    }

    public Set<String> getOutputVariables() {
        return getVariablesKind(Variable::isOutput);
    }
}
