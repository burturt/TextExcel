/*
 * Basic RealCell subclass that simply stores a single value
 *
 * @author Alec Machlis
 * @version March 22, 2021
 */
package textExcel;

public class ValueCell extends RealCell {
    public ValueCell(String value) {
        super(value);
    }
    public double getDoubleValue() {
        return Double.parseDouble(super.fullCellText());
    }

}
