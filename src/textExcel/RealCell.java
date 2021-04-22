/*
 * Abstract class that evaluates to a numerical value
 *
 * @author Alec Machlis
 * @version April 22, 2021
 */
package textExcel;

public abstract class RealCell implements Cell, Comparable<RealCell> {
    private String value;

    public RealCell(String value) {
        this.value = value;
        // Verify is valid
        validate();
    }

    // Returns cell, length 10 exactly
    @Override
    public String abbreviatedCellText() {
        return (getDoubleValue() + "          ").substring(0, 10);
    }

    // Returns full unevaluated value. Can be overridden
    @Override
    public String fullCellText() {
        return value;
    }

    // returns double value of cell
    public double getDoubleValue() {
        return Double.parseDouble(fullCellText());
    }

    // Returns full actual unformatted value
    // May sometimes not be the same as fullCellText and is not guaranteed a number - formula cell may return "#ERROR"
    public String getFullStringValue() {
        return fullCellText();
    }

    // throws error if invalid, interrupting construction. Can be overridden.
    public void validate() {
        abbreviatedCellText();
    }

    // Compares 2 real cells in values
    @Override
    public int compareTo(RealCell o) {
        double value = getDoubleValue() - o.getDoubleValue();
        // Manually compare values as compareTo only deals with integers
        if (value > 0) {
            return 1;
        } else if (value < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
