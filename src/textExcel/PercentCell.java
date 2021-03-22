/*
 * Cell class that stores a percent and returns actual value, not percent value
 *
 * @author Alec Machlis
 * @version March 16, 2021
 */
package textExcel;

public class PercentCell extends ValueCell {
    public PercentCell(String percent) {
        super(percent);
        // Verify valid
        abbreviatedCellText();
        if (!percent.endsWith("%")) {
            throw new IllegalArgumentException("Percents must end in a percent sign");
        }
    }

    @Override
    public double getDoubleValue() {
        return Double.parseDouble(super.fullCellText().substring(0, super.fullCellText().length() - 1)) / 100;
    }

    // Keeps precision by methods that need percent value
    public double getPercentValue() {
        return Double.parseDouble(super.fullCellText().substring(0, super.fullCellText().length() - 1));
    }

    @Override
    public String abbreviatedCellText() {
        // Add spaces, get 9 characters, remove extra spaces, add percent sign, add spaces, trim to 10 characters
        // This ensures that the percent sign is where it needs to be
        return (((int) (getPercentValue()) + "          ").substring(0, 9).trim() + "%         ").substring(0,10);
    }

    @Override
    public String fullCellText() {
        return getDoubleValue() + "";
    }

}
