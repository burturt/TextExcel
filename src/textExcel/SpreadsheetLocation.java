/*
 * Converts a cell identifier to row and column indexes
 *
 * @author Alec Machlis
 * @version March 15, 2021
 */

package textExcel;

//Update this file with your own code.

public class SpreadsheetLocation implements Location {
    private int rowIndex;
    private int colIndex;
    @Override
    public int getRow()
    {
        return rowIndex;
    }

    @Override
    public int getCol()
    {
        return colIndex;
    }

    public String toString() {
        char colChar = (char) (colIndex + 'A');
        int row = rowIndex + 1;
        return "" + colChar + row;
    }
    
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
