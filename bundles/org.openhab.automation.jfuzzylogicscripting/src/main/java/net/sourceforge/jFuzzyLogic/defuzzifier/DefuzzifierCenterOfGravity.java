package net.sourceforge.jFuzzyLogic.defuzzifier;

import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 * Center of gravity defuzzyfier
 *
 * @author pcingola@users.sourceforge.net
 */
public class DefuzzifierCenterOfGravity extends DefuzzifierContinuous {

    public DefuzzifierCenterOfGravity(Variable variable) {
        super(variable);
    }

    /** Defuzification function */
    @Override
    public double defuzzify() {
        double x = getMin(), sum = 0, weightedSum = 0;

        // Calculate integrals (approximated as sums)
        for (int i = 0; i < values.length; i++, x += getStepSize()) {
            sum += values[i];
            weightedSum += x * values[i];
        }

        // No sum? => this variable has no active antecedent
        if (sum <= 0) {
            return Double.NaN;
        }

        // Calculate center of gravity
        double cog = weightedSum / sum;
        return cog;
    }

    @Override
    public String toStringFcl() {
        return "METHOD : COG;";
    }
}
