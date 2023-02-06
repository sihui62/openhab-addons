package net.sourceforge.jFuzzyLogic.defuzzifier;

import net.sourceforge.jFuzzyLogic.fcl.FclObject;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * Generic defuzzifier
 *
 * @author pcingola@users.sourceforge.net
 */
public abstract class Defuzzifier extends FclObject {

    /** Discrete defuzzifier (e.g. for singletons) */
    private final boolean discrete;
    Variable variable;

    /** Constructor */
    public Defuzzifier(Variable variable, boolean discrete) {
        this.variable = variable;
        this.discrete = discrete;
    }

    /**
     * Deffuzification function
     * Note: Has to return Double.NaN if no rule inferred this variable
     */
    public abstract double defuzzify();

    /** Short name */
    public String getName() {
        String str = this.getClass().getName();
        String dfStr = "Defuzzifier";
        int ind = str.lastIndexOf('.');
        if (ind >= 0) {
            str = str.substring(ind + 1);
            if (str.startsWith(dfStr)) {
                str = str.substring(dfStr.length());
            }
        }
        return str;
    }

    protected abstract void init();

    public boolean isDiscrete() {
        return discrete;
    }

    /**
     * Do we need to re-initialize?
     * E.g. Defuzzified depends on an input variable that changed value
     */
    public abstract boolean needsInit();

    /**
     * Reset defuzzifier values, this method is invoked on every RuleSet.evaluate()
     */
    public void reset() {
        if (needsInit()) {
            init();
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
