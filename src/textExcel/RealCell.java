/*
 * Abstract class that evaluates to a numerical value
 *
 * @author Alec Machlis
 * @version March 15, 2021
 */
package textExcel;

public abstract class RealCell implements Cell {
    private String value;

    public RealCell(String value) {
        this.value = value;
        // Verify is valid
        testValid();
    }

    @Override
    public String abbreviatedCellText() {
        return String.format("%-10.10s", getDoubleValue() + "");
    }

    @Override
    public String fullCellText() {
        return value;
    }

    // Should be overridden - returns double value of cell
    public abstract double getDoubleValue();

    // Returns full actual unformatted value
    public String getFullStringValue() {
        return fullCellText();
    }

    // throws error if invalid
    public void testValid() {
        abbreviatedCellText();
    }
}
