package net.sourceforge.jFuzzyLogic.rule;

import java.util.Iterator;
import java.util.LinkedList;

import net.sourceforge.jFuzzyLogic.fcl.FclObject;
import net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod;
import net.sourceforge.jFuzzyLogic.ruleActivationMethod.RuleActivationMethod;

/**
 * Fuzzy rule
 *
 * Rule: If (x1 is termX1) AND (x2 is termX2) .... Then (y1 is termY1) AND (y2 is termY2) [weight: 1.0]
 * Notes:
 * - "If" clause is called "antecedent"
 * - "then" clause is called "consequent"
 * - There may be 1 or more antecedents connected using a 'RuleConnectionMethod' (e.g. AND, OR)
 * - As there are many ways to implement 'AND' and 'OR' connectors, you can customize them
 *
 * @author pcingola@users.sourceforge.net
 */
public class Rule extends FclObject {

    /** Rule antecedent ('if' part) */
    private RuleExpression antecedents = new RuleExpression();
    /** Degree of support */
    private double degreeOfSupport;
    /** Rule's weight */
    private double weight = 1.0;
    /** This rule belongs to ruleBlock */
    private final RuleBlock ruleBlock;
    /** Rule's name */
    private final String name;
    /** Rule consequent ('then' part) */
    private final LinkedList<RuleTerm> consequents = new LinkedList<>();

    /**
     * Default constructor
     * Default connection method: AND (minimum)
     */
    public Rule(String name, RuleBlock ruleBlock) {
        this.name = name;
        this.ruleBlock = ruleBlock;
    }

    /**
     * Add a condition "... AND ( variable is termName)" to this rule
     *
     * @param variable : Variable to evaluate
     * @param termName : RuleTerm for this condition
     * @return this Rule
     */
    public Rule addAntecedent(Variable variable, String termName, boolean negated) {
        if (variable.getMembershipFunction(termName) == null) {
            throw new RuntimeException(
                    "RuleTerm '" + termName + "' does not exists in variable '" + variable.getName() + "'");
        }
        RuleTerm fuzzyRuleTerm = new RuleTerm(variable, termName, negated);
        antecedents.add(fuzzyRuleTerm);
        return this;
    }

    /**
     * Add consequent "( variable is termName)" to this rule
     *
     * @param variable : Variable to evaluate
     * @param termName : RuleTerm for this condition
     * @return this Rule
     */
    public Rule addConsequent(Variable variable, String termName, boolean negated) {
        if (variable.getMembershipFunction(termName) == null) {
            throw new RuntimeException(
                    "RuleTerm '" + termName + "' does not exists in variable '" + variable.getName() + "'");
        }
        consequents.add(new RuleTerm(variable, termName, negated));
        return this;
    }

    /**
     * Evaluate this rule using 'RuleImplicationMethod'
     *
     * @param ruleActivationMethod : Rule implication method to use
     */
    public void evaluate() {
        RuleActivationMethod ruleActivationMethod = ruleBlock.getRuleActivationMethod();

        // ---
        // Evaluate antecedents
        // ---
        degreeOfSupport = antecedents.evaluate();

        // Apply weight
        degreeOfSupport *= weight;

        // ---
        // Imply rule consequents: Apply degreeOfSupport to consequent linguisticTerms
        // ---
        RuleAccumulationMethod ruleAccumulationMethod = ruleBlock.getRuleAccumulationMethod();
        Gpr.debug("degreeOfSupport:" + degreeOfSupport + "\truleAccumulationMethod:" + ruleAccumulationMethod
                + "\truleImplicationMethod:" + ruleActivationMethod);
        for (RuleTerm term : consequents) {
            Gpr.debug("\tfuzzyRuleTerm:" + term);
            ruleActivationMethod.imply(term, ruleAccumulationMethod, degreeOfSupport);
        }
    }

    public RuleExpression getAntecedents() {
        return antecedents;
    }

    public LinkedList<RuleTerm> getConsequents() {
        return consequents;
    }

    public double getDegreeOfSupport() {
        return degreeOfSupport;
    }

    public String getName() {
        return name;
    }

    public RuleBlock getRuleBlock() {
        return ruleBlock;
    }

    public double getWeight() {
        return weight;
    }

    public void setAntecedents(RuleExpression antecedents) {
        this.antecedents = antecedents;
    }

    public void setDegreeOfSupport(double degreeOfSupport) {
        this.degreeOfSupport = degreeOfSupport;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        String strAnt = "", strCon = "";

        // Show antecedents
        strAnt = antecedents.toString();

        // Show consequents
        Iterator<RuleTerm> itc = consequents.iterator();
        while (itc.hasNext()) {
            RuleTerm term = itc.next();
            if (strCon.length() > 0) {
                strCon += " , ";
            }
            strCon += term.toString();
        }

        return name + "\t(" + degreeOfSupport + ")\tif " + strAnt + " then " + strCon + " [weight: " + weight + "]";
    }

    @Override
    public String toStringFcl() {
        String strAnt = "", strCon = "";

        // Show antecedents
        strAnt = antecedents.toString();

        // Show consequents
        for (RuleTerm term : consequents) {
            if (strCon.length() > 0) {
                strCon += " , ";
            }
            strCon += term.toString();
        }

        return "IF " + strAnt + " THEN " + strCon + (weight != 1.0 ? " WITH " + weight : "") + ";";
    }
}
