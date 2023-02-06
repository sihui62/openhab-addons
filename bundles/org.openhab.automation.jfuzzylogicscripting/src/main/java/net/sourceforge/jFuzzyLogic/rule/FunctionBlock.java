package net.sourceforge.jFuzzyLogic.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.antlr.runtime.tree.Tree;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import net.sourceforge.jFuzzyLogic.defuzzifier.Defuzzifier;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfArea;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfGravity;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfGravityFunctions;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfGravitySingletons;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierLeftMostMax;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierMeanMax;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierRightMostMax;
import net.sourceforge.jFuzzyLogic.fcl.FclObject;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionCosine;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionDifferenceSigmoidal;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionFuncion;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionGaussian;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionGaussian2;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionGenBell;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionGenericSingleton;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionPieceWiseLinear;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionSigmoidal;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionSingleton;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTrapetzoidal;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTriangular;
import net.sourceforge.jFuzzyLogic.membership.Value;

/**
 * A complete inference system contains:
 * - input / output variables
 * - rule blocks
 *
 * Reference: See IEC 1131 - Part 7 - Fuzzy Control Programming
 *
 *
 * @author pcingola@users.sourceforge.net
 *
 */
@NonNullByDefault
public class FunctionBlock extends FclObject implements Iterable<RuleBlock>, Comparable<FunctionBlock> {

    private final HashMap<String, RuleBlock> ruleBlocks = new HashMap<>(); // Several RuleBlocks indexed by name
    private final HashMap<String, Variable> variables = new HashMap<>(); // Every variable is here (key: VariableName)
    private final String name; // Function block name - set when reading the tree

    @Override
    public int compareTo(FunctionBlock fb) {
        return name.compareTo(fb.getName());
    }

    /**
     * Create a defuzzifier based on defuziffier's name and a variable
     */
    private Defuzzifier createDefuzzifier(String methodType, Variable variable) {
        if (methodType.equalsIgnoreCase("COG")) {
            return new DefuzzifierCenterOfGravity(variable);
        } else if (methodType.equalsIgnoreCase("COGS")) {
            return new DefuzzifierCenterOfGravitySingletons(variable);
        } else if (methodType.equalsIgnoreCase("COGF")) {
            return new DefuzzifierCenterOfGravityFunctions(variable);
        } else if (methodType.equalsIgnoreCase("COA")) {
            return new DefuzzifierCenterOfArea(variable);
        } else if (methodType.equalsIgnoreCase("LM")) {
            return new DefuzzifierLeftMostMax(variable);
        } else if (methodType.equalsIgnoreCase("RM")) {
            return new DefuzzifierRightMostMax(variable);
        } else if (methodType.equalsIgnoreCase("MM")) {
            return new DefuzzifierMeanMax(variable);
        }
        throw new RuntimeException("Unknown/Unimplemented Rule defuzzification method '" + methodType + "'");
    }

    /**
     * Evaluate fuzzy rules in this function block
     */
    public void evaluate() {
        // reset(true); // Reset variables
        reset(); // Reset variables

        // First: Reset defuzzifiers, variables, etc.
        for (RuleBlock ruleBlock : this) {
            ruleBlock.reset();
        }

        // Second: Evaluate each RuleBlock
        for (RuleBlock ruleBlock : this) {
            ruleBlock.evaluate();
        }

        // Third: Defuzzify each consequent variable
        for (Variable var : variables()) {
            if (var.isOutput()) {
                var.defuzzify();
            }
        }
    }

    /**
     * Builds rule set based on FCL tree (parsed from an FCL file)
     *
     * @param tree : Tree to use
     * @return : RuleSet's name (or "" if no name)
     */
    public FunctionBlock(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Gpr.checkRootNode("FUNCTION_BLOCK", tree);
        ruleBlocks.clear();

        boolean firstChild = true;
        int ruleBlockCount = 1;
        String firstChildName = "";
        // Add every child
        for (int childNum = 0; childNum < tree.getChildCount(); childNum++) {
            Tree child = tree.getChild(childNum);
            Gpr.debug("\t\tChild: " + child.toStringTree());
            String leaveName = child.getText();

            if (firstChild) {
                firstChildName = leaveName;
            } else if (leaveName.equalsIgnoreCase("VAR_INPUT")) {
                fclTreeVariables(child);
            } else if (leaveName.equalsIgnoreCase("VAR_OUTPUT")) {
                fclTreeVariables(child);
            } else if (leaveName.equalsIgnoreCase("FUZZIFY")) {
                fclTreeFuzzify(child);
            } else if (leaveName.equalsIgnoreCase("DEFUZZIFY")) {
                fclTreeDefuzzify(child);
            } else if (leaveName.equalsIgnoreCase("RULEBLOCK")) {
                // Create and parse RuleBlock
                RuleBlock ruleBlock = new RuleBlock(this);
                String rbname = ruleBlock.fclTree(child);

                if (rbname.equals("")) {
                    rbname = "RuleBlock_" + ruleBlockCount; // Create name if none is given
                }
                ruleBlockCount++;

                // Add RuleBlock
                ruleBlocks.put(rbname, ruleBlock);
            } else {
                throw new RuntimeException("Unknown item '" + leaveName + "':\t" + child.toStringTree());
            }

            firstChild = false;
        }
        name = firstChildName;
    }

    /**
     * Parse a tree for "Defuzzify" item
     *
     * @param tree : Tree to parse
     * @return Variable (old or created)
     */
    private Variable fclTreeDefuzzify(Tree tree) {
        Gpr.checkRootNode("DEFUZZIFY", tree);
        Gpr.debug("Tree: " + tree.toStringTree());
        String defuzzificationMethodType = "COG";

        Tree child = tree.getChild(0);
        String varName = child.getText();

        // Get variable (or create a new one)
        Variable variable = getVariable(varName);
        if (variable == null) {
            variable = new Variable(varName);
            addVariable(variable);
            Gpr.debug("Variable '" + varName + "' does not exist => Creating it");
        }

        // ---
        // Explore each sibling in this level
        // ---
        for (int childNum = 1; childNum < tree.getChildCount(); childNum++) {
            child = tree.getChild(childNum);
            String leaveName = child.getText();
            Gpr.debug("\t\tChild: " + child.toStringTree());

            if (leaveName.equalsIgnoreCase("TERM")) {
                // Linguistic term
                LinguisticTerm linguisticTerm = fclTreeFuzzifyTerm(child, variable);
                variable.add(linguisticTerm);
            } else if (leaveName.equalsIgnoreCase("ACCU")) { // Accumulation method
                throw new RuntimeException("Accumulation method (ACCU) must be defined at RULE_BLOCK");
            } else if (leaveName.equalsIgnoreCase("METHOD")) { // Defuzzification method
                defuzzificationMethodType = child.getChild(0).getText();
            } else if (leaveName.equalsIgnoreCase("DEFAULT")) {
                // Default value
                String defaultValueStr = child.getChild(0).getText();
                if (defaultValueStr.equalsIgnoreCase("NC")) {
                    variable.setDefaultValue(Double.NaN); // Set it to "No Change"?
                } else { // Set it to "No Change"?
                    variable.setDefaultValue(Gpr.parseDouble(child.getChild(0))); // Set value
                }
            } else if (leaveName.equalsIgnoreCase("RANGE")) {
                // Range values (universe min / max)
                double universeMin = Gpr.parseDouble(child.getChild(0));
                double universeMax = Gpr.parseDouble(child.getChild(1));
                if (universeMax <= universeMin) {
                    throw new RuntimeException("Range's min is grater than range's max! RANGE := ( " + universeMin
                            + " .. " + universeMax + " );");
                }
                variable.setUniverseMax(universeMax);
                variable.setUniverseMin(universeMin);
            } else {
                throw new RuntimeException("Unknown/Unimplemented item '" + leaveName + "'");
            }
        }

        // Defuzzification method
        Defuzzifier defuzzifier = createDefuzzifier(defuzzificationMethodType, variable);
        variable.setDefuzzifier(defuzzifier);

        return variable;
    }

    /**
     * Parse a tree for "Fuzzify" item
     *
     * @param tree : Tree to parse
     * @return Variable (old or created)
     */
    private Variable fclTreeFuzzify(Tree tree) {
        Gpr.checkRootNode("FUZZIFY", tree);
        Gpr.debug("Tree: " + tree.toStringTree());
        Tree child = tree.getChild(0);
        String varName = child.getText();

        // Get variable (or create a new one)
        Variable variable = getVariable(varName);
        if (variable == null) {
            variable = new Variable(varName);
            addVariable(variable);
            Gpr.debug("Variable '" + varName + "' does not exist => Creating it");
        }

        // Explore each sibling in this level
        for (int childNum = 1; childNum < tree.getChildCount(); childNum++) {
            child = tree.getChild(childNum);
            Gpr.debug("\t\tChild: " + child.toStringTree());
            String leaveName = child.getText();

            if (leaveName.equalsIgnoreCase("TERM")) {
                LinguisticTerm linguisticTerm = fclTreeFuzzifyTerm(child, variable);
                variable.add(linguisticTerm);
            } else {
                throw new RuntimeException("Unknown/Unimplemented item '" + leaveName + "'");
            }
        }

        return variable;
    }

    /**
     * Parse a tree for "Term" item
     *
     * @param tree : Tree to parse
     * @return A new LinguisticTerm
     */
    private LinguisticTerm fclTreeFuzzifyTerm(Tree tree, Variable variable) {
        Gpr.checkRootNode("TERM", tree);
        Gpr.debug("Tree: " + tree.toStringTree());
        String termName = tree.getChild(0).getText();
        Tree child = tree.getChild(1);
        String leaveName = child.getText();
        Gpr.debug("\t\tTermname: " + termName + "\tLeavename: " + leaveName);

        MembershipFunction membershipFunction = null;
        if (leaveName.equalsIgnoreCase("POINT")) {
            membershipFunction = fclTreeFuzzifyTermPieceWiseLinear(tree);
        } else if (leaveName.equalsIgnoreCase("COSINE")) {
            membershipFunction = fclTreeFuzzifyTermCosine(child);
        } else if (leaveName.equalsIgnoreCase("DSIGM")) {
            membershipFunction = fclTreeFuzzifyTermDifferenceSigmoidal(child);
        } else if (leaveName.equalsIgnoreCase("GAUSS")) {
            membershipFunction = fclTreeFuzzifyTermGauss(child);
        } else if (leaveName.equalsIgnoreCase("GAUSS2")) {
            membershipFunction = fclTreeFuzzifyTermGauss2(child);
        } else if (leaveName.equalsIgnoreCase("TRIAN")) {
            membershipFunction = fclTreeFuzzifyTermTriangular(child);
        } else if (leaveName.equalsIgnoreCase("GBELL")) {
            membershipFunction = fclTreeFuzzifyTermGenBell(child);
        } else if (leaveName.equalsIgnoreCase("TRAPE")) {
            membershipFunction = fclTreeFuzzifyTermTrapetzoidal(child);
        } else if (leaveName.equalsIgnoreCase("SIGM")) {
            membershipFunction = fclTreeFuzzifyTermSigmoidal(child);
        } else if (leaveName.equalsIgnoreCase("SINGLETONS")) {
            membershipFunction = fclTreeFuzzifyTermSingletons(child);
        } else if (leaveName.equalsIgnoreCase("FUNCTION")) {
            membershipFunction = fclTreeFuzzifyTermFunction(child);
        } else if (leaveName.equalsIgnoreCase("-")) {
            membershipFunction = fclTreeFuzzifyTermSingleton(child);
        } else if (leaveName.equalsIgnoreCase("+")) {
            membershipFunction = fclTreeFuzzifyTermSingleton(child);
        } else {
            membershipFunction = fclTreeFuzzifyTermSingleton(child);
        }
        LinguisticTerm linguisticTerm = new LinguisticTerm(termName, membershipFunction);

        // Create linguistic term
        return linguisticTerm;
    }

    /**
     * Parse a tree for cosine membership function
     *
     * @param tree : tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermCosine(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Value net_max = new Value(tree.getChild(0), this);
        Value offset = new Value(tree.getChild(1), this);
        MembershipFunction membershipFunction = new MembershipFunctionCosine(net_max, offset);
        return membershipFunction;
    }

    /**
     * Parse a tree for difference sigmoidal membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermDifferenceSigmoidal(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Value a1 = new Value(tree.getChild(0), this);
        Value c1 = new Value(tree.getChild(1), this);
        Value a2 = new Value(tree.getChild(2), this);
        Value c2 = new Value(tree.getChild(3), this);
        MembershipFunction membershipFunction = new MembershipFunctionDifferenceSigmoidal(a1, c1, a2, c2);
        return membershipFunction;
    }

    /**
     * Parse a tree for trapetzoidal membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermFunction(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        return new MembershipFunctionFuncion(this, tree.getChild(0));
    }

    /**
     * Parse a tree for gaussian membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermGauss(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Tree child = tree.getChild(0);
        Value mean = new Value(child, this);
        Value stdev = new Value(tree.getChild(1), this);
        MembershipFunction membershipFunction = new MembershipFunctionGaussian(mean, stdev);
        return membershipFunction;
    }

    /**
     * Parse a tree for gaussian2 membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermGauss2(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Tree child = tree.getChild(0);
        Value mean = new Value(child, this);
        Value stdev = new Value(tree.getChild(1), this);
        Value mean2 = new Value(tree.getChild(2), this);
        Value stdev2 = new Value(tree.getChild(3), this);
        MembershipFunction membershipFunction = new MembershipFunctionGaussian2(mean, stdev, mean2, stdev2);
        return membershipFunction;
    }

    /**
     * Parse a tree for generilized bell membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermGenBell(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Tree child = tree.getChild(0);
        Value a = new Value(child, this);
        Value b = new Value(tree.getChild(1), this);
        Value mean = new Value(tree.getChild(2), this);
        MembershipFunction membershipFunction = new MembershipFunctionGenBell(a, b, mean);
        return membershipFunction;
    }

    /**
     * Parse a tree for piece-wice linear membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermPieceWiseLinear(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        int numberOfPoints = tree.getChildCount() - 1;
        Gpr.debug("\tNumber of points: " + numberOfPoints);

        Value x[] = new Value[numberOfPoints];
        Value y[] = new Value[numberOfPoints];
        for (int childNum = 1; childNum < tree.getChildCount(); childNum++) {
            Tree child = tree.getChild(childNum);
            Gpr.debug("\t\tChild: " + child.toStringTree());
            String leaveName = child.getText();

            // It's a set of points? => Defines a piece-wise linear membership function
            if (leaveName.equalsIgnoreCase("POINT")) {
                x[childNum - 1] = new Value(child.getChild(0), this); // Parse and add each point
                y[childNum - 1] = new Value(child.getChild(1), this);
                Gpr.debug("\t\tParsed point " + childNum + " x=" + x[childNum - 1] + ", y=" + y[childNum - 1]);
                if ((y[childNum - 1].getValue() < 0) || (y[childNum - 1].getValue() > 1)) {
                    throw new RuntimeException(
                            "\n\tError parsing line " + child.getLine() + " character " + child.getCharPositionInLine()
                                    + ": Membership function out of range (should be between 0 and 1). Value: '"
                                    + y[childNum - 1] + "'\n\tTree: " + child.toStringTree());
                }
            } else {
                throw new RuntimeException("Unknown (or unimplemented) option : " + leaveName);
            }
        }
        return new MembershipFunctionPieceWiseLinear(x, y);
    }

    /**
     * Parse a tree for sigmoidal membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermSigmoidal(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Value gain = new Value(tree.getChild(0), this);
        Value t0 = new Value(tree.getChild(1), this);
        MembershipFunction membershipFunction = new MembershipFunctionSigmoidal(gain, t0);
        return membershipFunction;
    }

    /**
     * Parse a tree for piece-wice linear membership function item
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermSingleton(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Value singleTonValueX = new Value(tree, this);
        MembershipFunction membershipFunction = new MembershipFunctionSingleton(singleTonValueX);
        return membershipFunction;
    }

    /**
     * Parse a tree for singletons membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermSingletons(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());

        // Count number of points
        int numPoints = 0;
        for (int childNum = 0; childNum < tree.getChildCount(); childNum++) {
            Tree child = tree.getChild(childNum);

            String leaveName = child.getText();
            if (leaveName.equalsIgnoreCase("(")) {
                numPoints++;
            }
            Gpr.debug("leaveName : " + leaveName + "\tnumPoints: " + numPoints);
        }

        // Parse multiple points (for piece-wise linear)
        return fclTreeFuzzifyTermSingletonsPoints(tree.getChild(0), numPoints);
    }

    /**
     * Parse a tree for singletons membership function series of points
     *
     * @param tree : Tree to parse
     * @param numberOfPoints : Number of points in this function
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermSingletonsPoints(Tree tree, int numberOfPoints) {
        Gpr.debug("Tree: " + tree.toStringTree());

        Value x[] = new Value[numberOfPoints];
        Value y[] = new Value[numberOfPoints];
        for (int childNum = 0; childNum < tree.getChildCount(); childNum++) {
            Tree child = tree.getChild(childNum);
            String leaveName = child.getText();
            Gpr.debug("Sub-Parsing: " + leaveName);

            // It's a set of points? => Defines a piece-wise linear membership function
            if (leaveName.equalsIgnoreCase("(")) {
                x[childNum] = new Value(child.getChild(0), this); // Parse and add each point
                y[childNum] = new Value(child.getChild(1), this);

                if ((y[childNum].getValue() < 0) || (y[childNum].getValue() > 1)) {
                    throw new RuntimeException(
                            "\n\tError parsing line " + child.getLine() + " character " + child.getCharPositionInLine()
                                    + ": Membership function out of range (should be between 0 and 1). Value: '"
                                    + y[childNum] + "'\n\tTree: " + child.toStringTree());
                }

                Gpr.debug("Parsed point " + childNum + " x=" + x[childNum] + ", y=" + y[childNum]);
            } else {
                throw new RuntimeException("Unknown (or unimplemented) option : " + leaveName);
            }
        }
        return new MembershipFunctionGenericSingleton(x, y);
    }

    /**
     * Parse a tree for trapetzoidal membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermTrapetzoidal(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Value min = new Value(tree.getChild(0), this);
        Value midLow = new Value(tree.getChild(1), this);
        Value midHigh = new Value(tree.getChild(2), this);
        Value max = new Value(tree.getChild(3), this);
        MembershipFunction membershipFunction = new MembershipFunctionTrapetzoidal(min, midLow, midHigh, max);
        return membershipFunction;
    }

    /**
     * Parse a tree for triangular membership function
     *
     * @param tree : Tree to parse
     * @return A new membership function
     */
    private MembershipFunction fclTreeFuzzifyTermTriangular(Tree tree) {
        Gpr.debug("Tree: " + tree.toStringTree());
        Value min = new Value(tree.getChild(0), this);
        Value mid = new Value(tree.getChild(1), this);
        Value max = new Value(tree.getChild(2), this);
        MembershipFunction membershipFunction = new MembershipFunctionTriangular(min, mid, max);
        return membershipFunction;
    }

    /**
     * Parse a tree for "Variable" item (either input or output variables)
     *
     * @param tree
     */
    private void fclTreeVariables(Tree tree) {
        Gpr.checkRootNode("VAR_OUTPUT", "VAR_INPUT", tree);
        Gpr.debug("Tree: " + tree.toStringTree());
        for (int childNum = 0; childNum < tree.getChildCount(); childNum++) {
            Tree child = tree.getChild(childNum);
            Gpr.debug("\tChild: " + child.toStringTree());
            String varName = child.getText();
            Variable variable = new Variable(varName);
            Gpr.debug("\tAdding variable: " + varName);

            // Set range?
            if (child.getChildCount() > 1) {
                Tree rangeTree = child.getChild(1);
                Gpr.debug("\tRangeTree: " + rangeTree.toStringTree());
                double min = Gpr.parseDouble(rangeTree.getChild(0));
                double max = Gpr.parseDouble(rangeTree.getChild(1));

                Gpr.debug("\tSetting universe to: [ " + min + " , " + max + " ]");
                variable.setUniverseMin(min);
                variable.setUniverseMax(max);
            }

            if (varibleExists(variable.getName())) {
                Gpr.debug("Warning: Variable '" + variable.getName() + "' duplicated");
                addVariable(variable); // OK? => Add variable
            }
        }
    }

    public String getName() {
        return name;
    }

    public HashMap<String, RuleBlock> getRuleBlocks() {
        return ruleBlocks;
    }

    public @Nullable Variable getVariable(String name) {
        return variables.get(name);
    }

    public HashMap<String, Variable> getVariables() {
        return variables;
    }

    @Override
    public Iterator<RuleBlock> iterator() {
        return ruleBlocks.values().iterator();
    }

    /**
     * Reset all variables
     */
    private void reset() {
        for (Variable var : variables()) {
            var.reset();
        }
    }

    private List<RuleBlock> ruleBlocksSorted() {
        ArrayList<RuleBlock> rbs = new ArrayList<RuleBlock>(ruleBlocks.values());
        Collections.sort(rbs);
        return rbs;
    }

    /**
     * Set a variable
     *
     * @param variableName : Variable's name
     * @param value : variable's value to be set
     * @return this
     */
    public void setVariableValue(String variableName, double value) {
        Variable var = getVariable(variableName);
        Objects.requireNonNull(var, "No such variable: '" + variableName + "'");
        var.setValue(value);
    }

    private void addVariable(Variable variable) {
        variables.put(variable.getName(), variable);
    }

    public void setVariables(HashMap<String, Variable> variables2) {
        variables.clear();
        variables.putAll(variables2);
    }

    @Override
    public String toStringFcl() {
        StringBuffer varsIn = new StringBuffer();
        StringBuffer varsOut = new StringBuffer();
        StringBuffer fuzzifiers = new StringBuffer();
        StringBuffer defuzzifiers = new StringBuffer();

        // ---
        // Show variables (sorted by name)
        // ---
        for (Variable var : variablesSorted()) {
            Defuzzifier defuzzifier = var.getDefuzzifier();
            if (defuzzifier == null) {
                // Add input variables
                varsIn.append("\t" + var.getName() + " : REAL;\n");

                // Add fuzzyfiers
                fuzzifiers.append("FUZZIFY " + var.getName() + "\n");
                for (LinguisticTerm linguisticTerm : var.linguisticTermsSorted()) {
                    fuzzifiers.append("\t" + linguisticTerm.toStringFcl() + "\n");
                }
                fuzzifiers.append("END_FUZZIFY\n\n");

            } else {
                // Add output variables
                varsOut.append("\t" + var.getName() + " : REAL;\n");

                // Add defuzzyfiers
                defuzzifiers.append("DEFUZZIFY " + var.getName() + "\n");
                for (LinguisticTerm linguisticTerm : var.linguisticTermsSorted()) {
                    defuzzifiers.append("\t" + linguisticTerm.toStringFcl() + "\n");
                }
                defuzzifiers.append("\t" + defuzzifier.toStringFcl() + "\n");
                defuzzifiers.append("\tDEFAULT := "
                        + (Double.isNaN(var.getDefaultValue()) ? "NC" : Double.toString(var.getDefaultValue()))
                        + ";\n");
                var.estimateUniverse();
                defuzzifiers.append("\tRANGE := (" + var.getUniverseMin() + " .. " + var.getUniverseMax() + ");\n");
                defuzzifiers.append("END_DEFUZZIFY\n\n");
            }
        }

        varsIn.insert(0, "VAR_INPUT\n");
        varsIn.append("END_VAR\n");
        varsOut.insert(0, "VAR_OUTPUT\n");
        varsOut.append("END_VAR\n");

        // ---
        // Iterate over each ruleSet and append it to output string
        // ---
        StringBuffer ruleBlocksStr = new StringBuffer();
        for (RuleBlock ruleBlock : ruleBlocksSorted()) {
            ruleBlocksStr.append(ruleBlock.toStringFcl());
        }

        // Build the whole thing
        return "FUNCTION_BLOCK " + name + "\n\n" //
                + varsIn + "\n" //
                + varsOut + "\n" //
                + fuzzifiers //
                + defuzzifiers //
                + ruleBlocksStr + "\n" //
                + "END_FUNCTION_BLOCK\n\n";
    }

    /** Get all variables */
    public Collection<Variable> variables() {
        return variables.values();
    }

    /** Get all variables (sorted by name) */
    private List<Variable> variablesSorted() {
        List<Variable> ll = new LinkedList<Variable>(variables.values());
        Collections.sort(ll);
        return ll;
    }

    /** Does this variable exist in this FunctionBlock? */
    public boolean varibleExists(String variableName) {
        return getVariable(variableName) != null;
    }
}
