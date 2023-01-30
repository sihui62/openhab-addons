package net.sourceforge.jFuzzyLogic.fcl;

/**
 * The root of all FCL objects
 *
 * @author pcingola
 *
 */
public abstract class FclObject {

    @Override
    public String toString() {
        return toStringFcl();
    }

    public abstract String toStringFcl();
}
