/*
 * Class that stores raw text
 *
 * @author Alec Machlis
 * @version March 15, 2021
 */
package textExcel;

public class TextCell implements Cell {

    private String cellText;

    public TextCell(String cellText) {
        this.cellText = cellText;
    }

    @Override
    public String abbreviatedCellText() {
        return String.format("%-10.10s", cellText);
    }

    @Override
    public String fullCellText() {
        return '"' + cellText + '"';
    }
}
