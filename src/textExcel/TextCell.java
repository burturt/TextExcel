/*
 * Class that stores raw text
 *
 * @author Alec Machlis
 * @version March 16, 2021
 */
package textExcel;

public class TextCell implements Cell, Comparable<TextCell> {

    private String cellText;

    public TextCell(String cellText) {
        this.cellText = cellText;
    }

    @Override
    public String abbreviatedCellText() {
        return (cellText + "          ").substring(0,10);
    }

    @Override
    public String fullCellText() {
        return '"' + cellText + '"';
    }

    // Return unformatted value
    public String getFullStringValue() {
        return cellText;
    }

    @Override
    public int compareTo(TextCell o) {
        return cellText.compareTo(o.getFullStringValue());
    }

}
