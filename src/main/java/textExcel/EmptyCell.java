/*
 * Represents an empty cell. Holds no value and returns an empty string.
 *
 * @author Alec Machlis
 * @version April 22, 2021
 */
package textExcel;

public class EmptyCell implements Cell {

    // text for spreadsheet cell display, must be exactly length 10
    public String abbreviatedCellText() {
        return "          ";
    }
    // text for individual cell inspection, not truncated or padded
    public String fullCellText() {
        return "";
    }
}
