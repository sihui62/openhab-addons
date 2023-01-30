package net.sourceforge.jFuzzyLogic.ruleAccumulationMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rule accumulation mathod: Sum
 *
 * @author pcingola@users.sourceforge.net
 */
@NonNullByDefault
public class RuleAccumulationMethodNormedSum extends RuleAccumulationMethod {

    public RuleAccumulationMethodNormedSum() {
        super("nsum");
    }

    /**
     * @see net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod#aggregate(double, double)
     */
    @Override
    public double aggregate(double defuzzifierValue, double valueToAggregate) {
        return (defuzzifierValue + valueToAggregate) / Math.max(1.0, defuzzifierValue + valueToAggregate);
    }

    /**
     * @see net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod#toStringFcl()
     */
    @Override
    public String toStringFcl() {
        return "ACCU : NSUM;";
    }
}
