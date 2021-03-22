/*
 * Cell class that stores a percent and returns actual value, not percent value
 *
 * @author Alec Machlis
 * @version March 22, 2021
 */
package textExcel;

public class PercentCell extends ValueCell {

    // Store percent value
    public PercentCell(String percent) {
        super(percent);
        // Verify valid
    }

    // Returns actual decimal value
    @Override
    public double getDoubleValue() {
        return Double.parseDouble(super.fullCellText().substring(0, super.fullCellText().length() - 1)) / 100;
    }

    // Keeps precision by methods that need percent value
    public double getPercentValue() {
        return Double.parseDouble(super.fullCellText().substring(0, super.fullCellText().length() - 1));
    }

    // Returns value as a string in percent form, rounded down
    @Override
    public String abbreviatedCellText() {
        // Add spaces, get 9 characters, remove extra spaces, add percent sign, add spaces, trim to 10 characters
        // This ensures that the percent sign is where it needs to be
        return (((int) (getPercentValue()) + "          ").substring(0, 9).trim() + "%         ").substring(0,10);
    }

    // Returns full cell text of actual decimal value, no sign
    @Override
    public String fullCellText() {
        return getDoubleValue() + "";
    }

    // Tests that it can be printed and it ends in %
    @Override
    public void testValid() {
        if (!this.abbreviatedCellText().trim().endsWith("%")) {
            throw new IllegalArgumentException("Percents must end in a percent sign");
        }
    }

}
