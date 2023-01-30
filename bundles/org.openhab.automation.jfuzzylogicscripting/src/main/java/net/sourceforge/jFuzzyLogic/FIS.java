package net.sourceforge.jFuzzyLogic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.jFuzzyLogic.fcl.FclLexer;
import net.sourceforge.jFuzzyLogic.fcl.FclObject;
import net.sourceforge.jFuzzyLogic.fcl.FclParser;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * Fuzzy inference system (FIS)
 *
 * A fuzzy logic inference system based on FCL language
 * according to "INTERNATIONAL ELECTROTECHNICAL COMMISSION" IEC 1131 - Part 7
 *
 * @author pcingola@users.sourceforge.net
 */
public class FIS extends FclObject implements Iterable<FunctionBlock> {
    private final static Logger logger = LoggerFactory.getLogger(FIS.class);

    /** Several function blocks indexed by name */
    private HashMap<String, FunctionBlock> functionBlocks;
    private ArrayList<FunctionBlock> functionBlocksSorted;

    /**
     * Create a "Fuzzy inference system (FIS)" from an FCL definition string
     *
     * @param lexer : lexer to use
     * @return A new FIS (or null on error)
     */
    private static FIS createFromLexer(FclLexer lexer) throws RecognitionException {
        FIS fis = new FIS();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FclParser parser = new FclParser(tokens);

        // FclParser.fcl_return root = parser.fcl();
        FclParser.main_return root;
        root = parser.main();
        Tree parseTree = (Tree) root.getTree();

        // Error loading file?
        if (parseTree == null) {
            logger.warn("Can't create FIS");
            return null;
        }

        Gpr.debug("Tree: " + parseTree.toStringTree());

        // Add every FunctionBlock (there may be more than one in each FCL file)
        for (int childNum = 0; childNum < parseTree.getChildCount(); childNum++) {
            Tree child = parseTree.getChild(childNum);

            Gpr.debug("Child " + childNum + ":\t" + child + "\tTree:'" + child.toStringTree() + "'");

            // Create a new FunctionBlock
            FunctionBlock functionBlock = new FunctionBlock();

            // Generate fuzzyRuleSet based on tree
            String name = functionBlock.fclTree(child);
            Gpr.debug("FunctionBlock Name: '" + name + "'");
            fis.addFunctionBlock(name, functionBlock);
        }

        return fis;
    }

    /**
     * Load an FCL file and create a "Fuzzy inference system (FIS)"
     *
     * @param fileName : FCL file name
     * @return A new FIS or null on error
     */
    public static FIS load(InputStream inputStream) {
        try {
            // Parse file (lexer first, then parser)
            FclLexer lexer = new FclLexer(new ANTLRInputStream(inputStream));
            // Parse tree and create FIS
            return createFromLexer(lexer);
        } catch (IOException e) {
            logger.warn("Error reading inputStream '{}'", inputStream);
            return null;
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Default constructor
     */
    public FIS() {
        functionBlocks = new HashMap<String, FunctionBlock>();
    }

    /** Add a function block */
    private void addFunctionBlock(String name, FunctionBlock functionBlock) {
        functionBlocks.put(name, functionBlock);
        functionBlocksSorted = null;
    }

    /**
     * Evaluate fuzzy rules all function blocks (in alphabetical order: functionBlock.name)
     */
    public void evaluate() {
        if (functionBlocks.size() > 1) {
            // Create a sorted list
            ArrayList<FunctionBlock> fbs = new ArrayList<FunctionBlock>(functionBlocks.size());
            fbs.addAll(functionBlocks.values());
            Collections.sort(fbs);

            // Evaluate them all
            for (FunctionBlock fb : fbs) {
                fb.evaluate();
            }

        } else if (functionBlocks.size() == 1) {
            getFunctionBlock(null).evaluate();
        }
    }

    /**
     * Get a FunctionBlock
     *
     * @param name : FunctionBlock's name (can be null to retrieve first available one)
     * @return FunctionBlock (or null if not found)
     */
    private FunctionBlock getFunctionBlock(String name) {
        if (name == null) {
            if (functionBlocks.size() > 1) {
                throw new RuntimeException(
                        "Can't use default FunctionBlock when there are more than one function blocks!");
            }
            return functionBlocks.values().iterator().next();
        }
        return functionBlocks.get(name);
    }

    /**
     * Get a variable from first available function block
     *
     * @param name
     * @return
     */
    public Variable getVariable(String varName) {
        FunctionBlock defaultFunctionBlock = getFunctionBlock(null);
        if (defaultFunctionBlock == null) {
            throw new RuntimeException("Default function block not found!");
        }

        Variable var = defaultFunctionBlock.getVariable(varName);
        if (var == null) {
            throw new RuntimeException("Variable '" + varName + "' not found!");
        }

        return var;
    }

    @Override
    public Iterator<FunctionBlock> iterator() {
        if (functionBlocksSorted == null) {
            functionBlocksSorted = new ArrayList<FunctionBlock>(functionBlocks.size());
            functionBlocksSorted.addAll(functionBlocks.values());
            Collections.sort(functionBlocksSorted);
        }
        return functionBlocksSorted.iterator();
    }

    @Override
    public String toStringFcl() {
        StringBuffer out = new StringBuffer();

        // Sort function blocks by name
        ArrayList<String> al = new ArrayList<String>(functionBlocks.keySet());
        Collections.sort(al);

        // Iterate over each function block and append it to output string
        for (String name : al) {
            FunctionBlock functionBlock = getFunctionBlock(name);
            out.append(functionBlock.toStringFcl());
        }

        return out.toString();
    }

}
