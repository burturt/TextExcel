package textExcel;

public class ValueCell extends RealCell {
    public ValueCell(String value) {
        super(value);
    }
    public double getDoubleValue() {
        return Double.parseDouble(super.fullCellText());
    }



}
