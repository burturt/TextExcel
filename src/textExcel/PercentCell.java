package textExcel;

public class PercentCell extends ValueCell {
    public PercentCell(String percent) {
        super(percent);
        // Verify valid
        getDoubleValue();
        if (!percent.endsWith("%")) {
            throw new IllegalArgumentException("Percents must end in a percent sign");
        }
    }

    @Override
    public double getDoubleValue() {
        return Double.parseDouble(super.fullCellText().substring(0, super.fullCellText().length() - 1));
    }

    @Override
    public String abbreviatedCellText() {
        return String.format("%-10.10s", String.format("%.9s%%", (int) getDoubleValue() + ""));
    }

    @Override
    public String fullCellText() {
        return getDoubleValue() / 100 + "";
    }

}