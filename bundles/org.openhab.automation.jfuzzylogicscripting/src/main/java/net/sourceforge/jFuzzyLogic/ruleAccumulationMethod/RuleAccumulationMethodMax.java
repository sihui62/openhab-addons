package net.sourceforge.jFuzzyLogic.ruleAccumulationMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rule accumulation mathod: Max
 *
 * @author pcingola@users.sourceforge.net
 */
@NonNullByDefault
public class RuleAccumulationMethodMax extends RuleAccumulationMethod {

    public RuleAccumulationMethodMax() {
        super("max");
    }

    /**
     * @see net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod#aggregate(double, double)
     */
    @Override
    public double aggregate(double defuzzifierValue, double valueToAggregate) {
        return Math.max(defuzzifierValue, valueToAggregate);
    }

    /**
     * @see net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod#toStringFcl()
     */
    @Override
    public String toStringFcl() {
        return "ACCU : MAX;";
    }
}
