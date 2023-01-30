package net.sourceforge.jFuzzyLogic.ruleActivationMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rule inference method : Minimum
 * Base abstract class
 *
 * @author pcingola@users.sourceforge.net
 */
@NonNullByDefault
public class RuleActivationMethodMin extends RuleActivationMethod {

    public RuleActivationMethodMin() {
        super("min");
    }

    @Override
    public double imply(double degreeOfSupport, double membership) {
        return Math.min(degreeOfSupport, membership);
    }

    /** Printable FCL version */
    @Override
    public String toStringFcl() {
        return "ACT : MIN;";
    }
}
