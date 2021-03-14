package textExcel;

public class RealCell implements Cell {
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
    public double getDoubleValue() {
        return Double.parseDouble(value);
    }

    // Should be overridden - throws error if invalid
    public void testValid() {
        double tempDouble = getDoubleValue();
    }
}
