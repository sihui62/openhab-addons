package net.sourceforge.jFuzzyLogic;

import org.antlr.runtime.tree.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General pupose rutines
 *
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 */
public class Gpr {
    private final static Logger logger = LoggerFactory.getLogger(Gpr.class);

    /** Only print this number of warnings */
    private static int MAX_NUMBER_OF_WARNINGS = 5;

    /** Print warning only N times */
    private static int warnCount = 0;

    /**
     * Sanity check for a root node
     *
     * @param name1
     * @param name2
     * @param tree
     */
    public static void checkRootNode(String name1, String name2, Tree tree) {
        if (!tree.getText().equalsIgnoreCase(name1) && !tree.getText().equalsIgnoreCase(name2)) { //
            throw new RuntimeException("\n\tThis tree does NOT have a '" + name1 + "' or '" + name2
                    + "' as root node! (this should never happen)\n\tLine: " + tree.getLine() + "\n");
        }
    }

    /**
     * Sanity check for a root node
     *
     * @param name
     * @param tree
     */
    public static void checkRootNode(String name, Tree tree) {
        if (!tree.getText().equalsIgnoreCase(name)) { //
            throw new RuntimeException("\n\tThis tree does NOT have a '" + name
                    + "' as root node! (this should never happen)\n\tLine: " + tree.getLine() + "\n");
        }
    }

    /**
     * Prits a debug message (prints class name, method and line number)
     *
     * @param obj : Object to print
     */
    public static void debug(Object obj) {
        debug(obj, 1, true);
    }

    /**
     * Prits a debug message (prints class name, method and line number)
     *
     * @param obj : Object to print
     * @param offset : Offset N lines from stacktrace
     */
    private static void debug(Object obj, int offset) {
        debug(obj, offset, true);
    }

    /**
     * Prits a debug message (prints class name, method and line number)
     *
     * @param obj : Object to print
     * @param offset : Offset N lines from stacktrace
     * @param newLine : Print a newline char at the end ('\n')
     */
    private static void debug(Object obj, int offset, boolean newLine) {
        StackTraceElement ste = new Exception().getStackTrace()[1 + offset];
        String steStr = ste.getClassName();
        int ind = steStr.lastIndexOf('.');
        steStr = steStr.substring(ind + 1);
        steStr += "." + ste.getMethodName() + "(" + ste.getLineNumber() + "): " + (obj == null ? null : obj.toString());
        logger.debug(steStr);
        if (newLine) {
            logger.debug("\n");
        }
    }

    /** Parse a double number */
    public static double parseDouble(Tree tree) {
        double sign = +1.0;
        double number = 0;

        if (tree.getText().equals("-")) {
            // Negative sign
            sign = -1.0;
            number = Gpr.parseDoubleSafe(tree.getChild(0).getText());
        } else if (tree.getText().equals("+")) {
            // Positive sign
            sign = +1.0;
            number = Gpr.parseDoubleSafe(tree.getChild(0).getText());
        } else {
            number = Gpr.parseDoubleSafe(tree.getText());
        }

        return sign * number;
    }

    /**
     * Equivalent to Double.parseDouble, except it returns 0 on invalid value (NumberFormatException)
     *
     * @param s
     * @return double
     */
    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Print a warning message (only a few of them)
     *
     * @param warning
     */
    public static void warn(String warning) {
        if (warnCount < MAX_NUMBER_OF_WARNINGS) {
            warnCount++;
            Gpr.debug(warning, 2);
        }
    }

}
