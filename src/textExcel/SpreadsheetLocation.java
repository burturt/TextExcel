/*
 * Converts a cell identifier to row and column indexes
 *
 * @author Alec Machlis
 * @version April 22, 2021
 */

package textExcel;

public class SpreadsheetLocation implements Location {
    private int rowIndex;
    private int colIndex;

    // Returns row index
    @Override
    public int getRow()
    {
        return rowIndex;
    }

    // Returns column index
    @Override
    public int getCol()
    {
        return colIndex;
    }

    // Returns location as raw cell name, like "B6"
    public String toString() {
        char colChar = (char) (colIndex + 'A');
        int row = rowIndex + 1;
        return "" + colChar + row;
    }

    // Constructor: parses cellName (max one letter + number) and stores indexes
    public SpreadsheetLocation(String cellName)
    {
        // Trim to prevent spaces getting in the way
        cellName = cellName.trim();

        // Parse string: one letter (col) then rest of string is number (row)
        // Throw error when fails to parse
        try {
            char colChar = Character.toUpperCase(cellName.charAt(0));
            // Verify is a letter
            if (!(colChar >= (int) 'A' && colChar <= (int) 'Z')) {
                throw new IllegalArgumentException("Invalid cell row");
            }
            int row = Integer.parseInt(cellName.substring(1));
            rowIndex = row - 1;
            colIndex = colChar - 'A';
        } catch(Exception e) {
            throw new IllegalArgumentException("Invalid cell name");
        }
    }

    // Overloaded constructor: allow creation location directly by index instead
    public SpreadsheetLocation(int rowIndex, int colIndex) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }

    // Tests if 2 locations are equal. Good for ArrayList.contains()
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SpreadsheetLocation)) {
            return false;
        }
        SpreadsheetLocation loc = (SpreadsheetLocation) obj;
        return rowIndex == loc.rowIndex && colIndex == loc.colIndex;
    }

}
