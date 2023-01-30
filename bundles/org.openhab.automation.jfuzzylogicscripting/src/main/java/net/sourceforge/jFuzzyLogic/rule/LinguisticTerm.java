package net.sourceforge.jFuzzyLogic.rule;

import org.eclipse.jdt.annotation.NonNullByDefault;

import net.sourceforge.jFuzzyLogic.fcl.FclObject;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;

/**
 * A linguistic term is an asociation between a termName and a membership function
 *
 * @author pcingola@users.sourceforge.net
 */
@NonNullByDefault
public class LinguisticTerm extends FclObject implements Comparable<LinguisticTerm> {

    /** Membership function */
    private final MembershipFunction membershipFunction;

    /** Terms's name */
    private final String termName;

    public LinguisticTerm(String termName, MembershipFunction membershipFunction) {
        this.termName = termName;
        this.membershipFunction = membershipFunction;
    }

    @Override
    public int compareTo(LinguisticTerm lt) {
        return termName.compareTo(lt.getTermName());
    }

    public MembershipFunction getMembershipFunction() {
        return membershipFunction;
    }

    public String getTermName() {
        return termName;
    }

    @Override
    public String toString() {
        return "\tTerm: " + termName + "\t" + membershipFunction.toString();
    }

    public String toString(double value) {
        return "Term: " + termName + "\t" + membershipFunction.membership(value) + "\t" + membershipFunction.toString();
    }

    @Override
    public String toStringFcl() {
        return "TERM " + termName + " := " + membershipFunction.toStringFcl() + ";";
    }

}
