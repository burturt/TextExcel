/*
 * Converts a cell identifier to row and column indexes
 *
 * @author Alec Machlis
 * @version March 11, 2021
 */

package textExcel;

//Update this file with your own code.

import java.util.Objects;

public class SpreadsheetLocation implements Location {
    private int rowIndex;
    private int colIndex;
    @Override
    public int getRow()
    {
        // TODO Auto-generated method stub
        return rowIndex;
    }

    @Override
    public int getCol()
    {
        // TODO Auto-generated method stub
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

    @Override
    public int hashCode() {
        return Objects.hash(rowIndex, colIndex);
    }
}
