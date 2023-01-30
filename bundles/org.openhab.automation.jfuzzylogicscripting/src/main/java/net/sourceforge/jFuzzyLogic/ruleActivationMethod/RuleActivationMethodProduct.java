package net.sourceforge.jFuzzyLogic.ruleActivationMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rule inference method: Product
 * Base abstract class
 *
 * @author pcingola@users.sourceforge.net
 */
@NonNullByDefault
public class RuleActivationMethodProduct extends RuleActivationMethod {

    public RuleActivationMethodProduct() {
        super("product");
    }

    @Override
    public double imply(double degreeOfSupport, double membership) {
        return degreeOfSupport * membership;
    }

    /** Printable FCL version */
    @Override
    public String toStringFcl() {
        return "ACT : PROD;";
    }
}
