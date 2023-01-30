package net.sourceforge.jFuzzyLogic.ruleAccumulationMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rule accumulation mathod: Sum
 *
 * @author pcingola@users.sourceforge.net
 */
@NonNullByDefault
public class RuleAccumulationMethodBoundedSum extends RuleAccumulationMethod {

    public RuleAccumulationMethodBoundedSum() {
        super("bsum");
    }

    /**
     * @see net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod#aggregate(double, double)
     */
    @Override
    public double aggregate(double defuzzifierValue, double valueToAggregate) {
        return Math.min(1.0, defuzzifierValue + valueToAggregate);
    }

    /**
     * @see net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod#toStringFcl()
     */
    @Override
    public String toStringFcl() {
        return "ACCU : BSUM;";
    }
}
