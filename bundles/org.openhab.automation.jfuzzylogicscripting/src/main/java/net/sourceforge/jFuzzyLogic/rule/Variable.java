package net.sourceforge.jFuzzyLogic.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import net.sourceforge.jFuzzyLogic.Gpr;
import net.sourceforge.jFuzzyLogic.defuzzifier.Defuzzifier;
import net.sourceforge.jFuzzyLogic.fcl.FclObject;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;

/**
 * Fuzzy variable
 *
 * @author pcingola@users.sourceforge.net
 */
@NonNullByDefault
public class Variable extends FclObject implements Comparable<Variable>, Iterable<LinguisticTerm> {
    private static final double EPSILON = 1e-6;

    private final String name; // Variable name
    private final HashMap<String, LinguisticTerm> linguisticTerms = new HashMap<>(); // Terms for this variable
    private double defaultValue = Double.NaN; // Default value, when no change
    private double latestDefuzzifiedValue; // Latest defuzzified value
    private double universeMax = Double.NaN; // Universe max (range max)
    private double universeMin = Double.NaN; // Universe minimum (range minimum)
    private double value = Double.NaN; // Variable's value
    private @Nullable String latestTerm;
    private @Nullable Defuzzifier defuzzifier; // Defuzzifier class
    private @Nullable HashMap<Variable, Double> variableValues = null; // Terms may use variables, we need to keep track
                                                                       // of values to know when universe needs to be
                                                                       // updated

    /**
     * Default constructor
     *
     * @param name : Variable's name
     */
    public Variable(String name) {
        this.name = name;

        reset(); // Reset values
    }

    /**
     * Adds a termName to this variable
     *
     * @param linguisticTerm : Linguistic term to add
     * @return this variable
     */
    public Variable add(LinguisticTerm linguisticTerm) {
        linguisticTerms.put(linguisticTerm.getTermName(), linguisticTerm);
        variableValues = null; // Reset cache
        return this;
    }

    @Override
    public int compareTo(Variable anotherVariable) {
        return name.compareTo(anotherVariable.getName());
    }

    /**
     * Defuzzify this (output) variable
     * Set defuzzufied values to 'value' and 'latestDefuzzifiedValue'
     */
    public double defuzzify() {
        Defuzzifier localDefuzzifier = Objects.requireNonNull(defuzzifier);
        double ldv = localDefuzzifier.defuzzify();

        // Only assign valid defuzzifier's result
        if (!Double.isNaN(ldv)) {
            value = latestDefuzzifiedValue = ldv;
        }

        return latestDefuzzifiedValue;
    }

    /** Estimate universe */
    public void estimateUniverse() {
        // Are universeMin and universeMax already set? => nothing to do
        if ((!Double.isNaN(universeMin)) && (!Double.isNaN(universeMax))) {
            return;
        }

        // Calculate max / min on every membership function
        double umin = Double.POSITIVE_INFINITY;
        double umax = Double.NEGATIVE_INFINITY;

        if (linguisticTerms.size() > 0) {
            for (LinguisticTerm lt : this) {
                MembershipFunction membershipFunction = lt.getMembershipFunction();
                membershipFunction.estimateUniverseForce();

                umin = Math.min(membershipFunction.getUniverseMin(), umin);
                umax = Math.max(membershipFunction.getUniverseMax(), umax);
            }
        } else {
            if (!Double.isNaN(defaultValue)) {
                umin = umax = defaultValue;
            } else {
                umin = umax = 0;
            }
        }

        // Set parameters
        universeMin = umin;
        universeMax = umax;
    }

    /**
     * Find all variables used in linguistic terms
     */
    private Set<Variable> findVariables() {
        Set<Variable> vars = new HashSet<Variable>();

        for (LinguisticTerm lt : this) {
            vars.addAll(lt.getMembershipFunction().findVariables());
        }

        return vars;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public @Nullable Defuzzifier getDefuzzifier() {
        return defuzzifier;
    }

    public double getLatestDefuzzifiedValue() {
        return latestDefuzzifiedValue;
    }

    /** Get 'termName' linguistic term */
    public LinguisticTerm getLinguisticTerm(String termName) {
        LinguisticTerm lt = linguisticTerms.get(termName);
        Objects.requireNonNull(lt, "No such linguistic term: '" + termName + "'");
        return lt;
    }

    public HashMap<String, LinguisticTerm> getLinguisticTerms() {
        return linguisticTerms;
    }

    /** Evaluate 'termName' membershipfunction at 'value' */
    public double getMembership(String termName) {
        return getMembershipFunction(termName).membership(value);
    }

    /** Get 'termName' membershipfunction */
    public MembershipFunction getMembershipFunction(String termName) {
        LinguisticTerm lt = linguisticTerms.get(termName);
        Objects.requireNonNull(lt, "No such linguistic term: '" + termName + "'");
        latestTerm = termName;
        return lt.getMembershipFunction();
    }

    public String getName() {
        return name;
    }

    public double getUniverseMax() {
        return universeMax;
    }

    public double getUniverseMin() {
        return universeMin;
    }

    public double getValue() {
        return value;
    }

    /** Return 'true' if this is an output variable */
    public boolean isInput() {
        return (defuzzifier == null); // Only output variables have defuzzyfiers
    }

    /** Return 'true' if this is an output variable */
    public boolean isOutput() {
        return (defuzzifier != null); // Only output variables have defuzzyfiers
    }

    @Override
    public Iterator<LinguisticTerm> iterator() {
        return linguisticTerms.values().iterator();
    }

    /** Get a 'linguisticTerms sorted by name */
    public List<LinguisticTerm> linguisticTermsSorted() {
        ArrayList<LinguisticTerm> al = new ArrayList<LinguisticTerm>(linguisticTerms.values());
        Collections.sort(al);
        return al;
    }

    /**
     * Do we need to estimate universe?
     * (e.g. when a variable referred by a linguistic term changed)
     */
    private boolean needEstimateUniverse() {
        // ---
        // Find all dependent variables
        // ---
        if (variableValues == null) {
            Set<Variable> vars = findVariables();

            // Add all variables
            variableValues = new HashMap<Variable, Double>();
            for (Variable var : vars) {
                variableValues.put(var, var.getValue());
            }
        }

        // ---
        // Any dependent variables changed?
        // ---
        LinkedList<Variable> change = null;
        for (Variable var : variableValues.keySet()) {
            double value = variableValues.get(var);
            if (Math.abs(value - var.getValue()) > EPSILON) {
                if (change == null) {
                    change = new LinkedList<Variable>();
                }
                change.add(var);
            }
        }

        // ---
        // Do we need to update any value?
        // ---
        if (change != null) {
            for (Variable var : change) {
                variableValues.put(var, var.getValue());
            }
        }

        return change != null;
    }

    /** Reset defuzzifier (if any) */
    public void reset() {
        if (needEstimateUniverse()) {
            universeMin = universeMax = Double.NaN; // Force
            estimateUniverse();
        }

        // Only reset variables that have a defuzzifier
        if (defuzzifier != null) {
            defuzzifier.reset();
        }

        // Set default value for output variables (if any default value was defined)
        if (!Double.isNaN(defaultValue)) {
            latestDefuzzifiedValue = value = defaultValue;
        }
    }

    public void setDefaultValue(double defualtValue) {
        defaultValue = defualtValue;
    }

    public void setDefuzzifier(Defuzzifier deffuzifier) {
        defuzzifier = deffuzifier;
    }

    public void setUniverseMax(double universeMax) {
        this.universeMax = universeMax;
    }

    public void setUniverseMin(double universeMin) {
        this.universeMin = universeMin;
    }

    public void setValue(double value) {
        if ((value < universeMin) || (value > universeMax)) {
            Gpr.warn("Value out of range?. Variable: '" + name + "', Universe: [" + universeMin + ", " + universeMax
                    + "], Value: " + value);
        }
        this.value = value;
    }

    public @Nullable String getLatestTerm() {
        return latestTerm;
    }

    @Override
    public String toString() {
        String str = name + " : \n";

        // Show defuzzifier for "output" variables, value for "input" variables
        if (defuzzifier != null) {
            str += "\tDefuzzifier : " + defuzzifier.toString() + "\n\tLatest defuzzified value: "
                    + latestDefuzzifiedValue + "\n";
        } else {
            str += "\tValue: " + value + "\n";
        }

        if (!Double.isNaN(defaultValue)) {
            str += "\tDefault value: " + defaultValue + "\n";
        }

        // Show each 'termName' and it's membership function
        for (LinguisticTerm linguisticTerm : this) {
            str += "\t" + linguisticTerm.toString(value) + "\n";
        }

        return str;
    }

    @Override
    public String toStringFcl() {
        return name;
    }
}
