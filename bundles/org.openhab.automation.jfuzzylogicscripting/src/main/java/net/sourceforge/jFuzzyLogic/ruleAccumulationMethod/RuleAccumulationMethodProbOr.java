package net.sourceforge.jFuzzyLogic.ruleAccumulationMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rule accumulation mathod: Probabilistic or
 *
 * @author pcingola@users.sourceforge.net
 */
@NonNullByDefault
public class RuleAccumulationMethodProbOr extends RuleAccumulationMethod {

    public RuleAccumulationMethodProbOr() {
        super("probOr");
    }

    /**
     * @see net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod#aggregate(double, double)
     */
    @Override
    public double aggregate(double defuzzifierValue, double valueToAggregate) {
        return defuzzifierValue + valueToAggregate - defuzzifierValue * valueToAggregate;
    }

    /**
     * @see net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethod#toStringFcl()
     */
    @Override
    public String toStringFcl() {
        return "ACCU : PROBOR;";
    }

}
