package net.sourceforge.jFuzzyLogic.rule;

import org.eclipse.jdt.annotation.NonNullByDefault;

import net.sourceforge.jFuzzyLogic.fcl.FclObject;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;

/**
 * A fuzzy logic term for a 'Rule'. E.g.: "speed IS high"
 *
 * @author pcingola@users.sourceforge.net
 */
@NonNullByDefault
public class RuleTerm extends FclObject {

    /** Is it negated? */
    private final boolean negated;
    /** RuleTerm's name */
    private final String termName;
    /** Varible */
    private final Variable variable;

    /**
     * Constructor
     *
     * @param variable
     * @param term
     * @param negated
     */
    public RuleTerm(Variable variable, String term, boolean negated) {
        this.variable = variable;
        this.termName = term;
        this.negated = negated;
    }

    public LinguisticTerm getLinguisticTerm() {
        return variable.getLinguisticTerm(termName);
    }

    public double getMembership() {
        double memb = variable.getMembership(termName);
        if (negated) {
            memb = 1.0 - memb;
        }
        return memb;
    }

    public MembershipFunction getMembershipFunction() {
        return variable.getMembershipFunction(termName);
    }

    public String getTermName() {
        return termName;
    }

    public Variable getVariable() {
        return variable;
    }

    public boolean isNegated() {
        return negated;
    }

    @Override
    public String toStringFcl() {
        String is = "IS";
        if (negated) {
            is = " IS NOT";
        }
        return variable.getName() + " " + is + " " + termName;
    }

}
