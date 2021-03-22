/*
 * Abstract class that evaluates to a numerical value
 *
 * @author Alec Machlis
 * @version March 16, 2021
 */
package textExcel;

public abstract class RealCell implements Cell, Comparable<RealCell> {
    private String value;

    public RealCell(String value) {
        this.value = value;
        // Verify is valid
        testValid();
    }

    @Override
    public String abbreviatedCellText() {
        return (getDoubleValue() + "          ").substring(0, 10);
    }

    @Override
    public String fullCellText() {
        return value;
    }

    // Should be overridden - returns double value of cell
    public abstract double getDoubleValue();

    // Returns full actual unformatted value
    // May sometimes not be the same as fullCellText
    public String getFullStringValue() {
        return fullCellText();
    }

    // throws error if invalid, blocking construction. Can be overridden.
    public void testValid() {
        abbreviatedCellText();
    }

    @Override
    public int compareTo(RealCell o) {
        double value = getDoubleValue() - o.getDoubleValue();
        if (value > 0) {
            return 1;
        } else if (value < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
