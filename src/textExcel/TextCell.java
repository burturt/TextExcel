/*
 * Class that stores raw text
 *
 * @author Alec Machlis
 * @version March 22, 2021
 */
package textExcel;

public class TextCell implements Cell, Comparable<TextCell> {

    private String cellText;

    // Simply assign cell text to value
    // Assumes input text has no ""s
    public TextCell(String cellText) {
        this.cellText = cellText;
    }

    // Returns max 10 characters from value
    @Override
    public String abbreviatedCellText() {
        return (cellText + "          ").substring(0,10);
    }

    // Returns full string with "" to denote that it is a string
    @Override
    public String fullCellText() {
        return '"' + cellText + '"';
    }

    // Return unformatted value
    public String getFullStringValue() {
        return cellText;
    }

    // Compares if a string is before (-1) or after (+1) the other in a list
    @Override
    public int compareTo(TextCell o) {
        return cellText.compareTo(o.getFullStringValue());
    }

}
