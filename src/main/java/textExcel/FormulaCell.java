/*
 * Class that stores and evaluates a formula
 *
 * @author Alec Machlis
 * @version April 22, 2021
 */
package textExcel;

import java.util.ArrayList;

public class FormulaCell extends RealCell {

    private ArrayList<String> operators;
    private Spreadsheet parentSpreadsheet;
    // List of cells. Used to store list of already-called cells when checking for circular references
    private ArrayList<Cell> upstreamCells;

    // Stores formula, sets up operators ArrayList, and stores spreadsheet that cell is in so it can reference values
    public FormulaCell(String value, Spreadsheet parentSpreadsheet) {
        super(value);

        upstreamCells = new ArrayList<>();

        this.parentSpreadsheet = parentSpreadsheet;
    }

    // Returns calculated value or ERROR if error
    @Override
    public String abbreviatedCellText() {
        // Clear circular reference cell list check
        upstreamCells = new ArrayList<>();

        // Catch runtime errors
        try {
            return (getDoubleValue() + "          ").substring(0, 10);
        } catch (ArithmeticException | IllegalStateException e) { // Thrown if bad calculation or bad cell reference
            return "#ERROR    ";
        }

    }

    // Gets the double value of a formula by calculating value
    @Override
    public double getDoubleValue() {
        // Split by spaces and make all upper case
        String[] expressionPartsArray = fullCellText().toUpperCase().split(" ");
        ArrayList<String> expressionParts = new ArrayList<>();
        for (String part : expressionPartsArray) {
            expressionParts.add(part.toUpperCase());
        }

        // Remove empty elements (caused by 2+ spaces in a row)
        for (int i = 0; i < expressionParts.size(); i++) {
            if (expressionParts.get(i).equals("")) {
                expressionParts.remove(i);
                i--;
            }
        }

        // Check for and discard ( and ) elements at beginning and end
        if ( !(expressionParts.get(0).equals("(")) || !(expressionParts.get(expressionParts.size() - 1).equals(")")) ) {
            throw new IllegalArgumentException("Must start and end with parenthesis");
        }
        expressionParts.remove(0);
        expressionParts.remove(expressionParts.size() - 1);

        // Check if sum or avg function
        if (expressionParts.contains("SUM")) {
            return functionSum(expressionParts);
        } else if (expressionParts.contains("AVG")) {
            return functionAvg(expressionParts);
        }

        // Verify proper expression: alternating number, operator, number, etc
        for (int i = 1; i < expressionParts.size(); i += 2) {

            evaluateStringValue(expressionParts.get(i - 1));

            if (!operators.contains(expressionParts.get(i))) {
                throw new IllegalArgumentException("Expression must alternate between numbers/cells and operators");
            }
        }
        // last part must be number
        evaluateStringValue(expressionParts.get(expressionParts.size()-1));


        /*
         Loop through elements of the array:
         Get number, get operator and number after in next element, calculate and collapse
         Loop through once only doing * and / operators, second time do all operators remaining (order of operations)
        */

        int i = 0;
        while (i + 1 < expressionParts.size()) {
            if (expressionParts.get(i+1).equals("*") || expressionParts.get(i+1).equals("/")) {
                expressionParts.set(i, evaluateOperation(expressionParts.get(i), expressionParts.get(i + 1), expressionParts.get(i + 2)));
                expressionParts.remove(i + 2);
                expressionParts.remove(i + 1);
            } else {
                i += 2; // Jump over operator
            }
        }

        while (1 < expressionParts.size()) {
            expressionParts.set(0, evaluateOperation(expressionParts.get(0), expressionParts.get(1), expressionParts.get(2)));
            expressionParts.remove(2);
            expressionParts.remove(1);
        }

        return evaluateStringValue(expressionParts.get(0));

    }

    // Calculates and returns actual unformatted full value
    // Allows FormulaCell to send an #ERROR value instead of a double if necessary but not truncate double like abbreviatedCellText
    public String getFullStringValue() {
        try {
            return getDoubleValue() + "";
        } catch (ArithmeticException | IllegalStateException e) { // Thrown if bad calculation (divide by zero, etc) or bad cell reference
            return "#ERROR";
        }
    }

    // Calculates and returns actual unformatted full value after checking for recursion by passing in list of cells
    public String getValueCheckCircular(ArrayList<Cell> cells) {
        if (cells.contains(this)) {
            throw new IllegalStateException("Circular reference in formula");
        }
        cells.add(this);
        upstreamCells = cells;
        String fullValue = getFullStringValue();
        upstreamCells.remove(this); // Remove cell from list to prevent issues when cells aren't circular but parallel referenced
        return fullValue;
    }

    // Executes operation given in string form the first number, operator, and second number
    public String evaluateOperation(String num1, String operator, String num2) {
        // Convert nums to doubles
        double num1d = evaluateStringValue(num1);
        double num2d = evaluateStringValue(num2);

        switch (operator) {
            case "+":
                return num1d + num2d + "";
            case "-":
                return num1d - num2d + "";
            case "*":
                return num1d * num2d + "";
            case "/":
                // Check for division by zero
                if (num2d == 0) {
                    throw new ArithmeticException("Cannot divide by 0");
                }
                return num1d / num2d + "";
        }
        throw new AssertionError("An unknown error occurred");
    }

    // Converts either double or cell name from String into double
    private double evaluateStringValue(String strNum) {
        double num;
        try {
            num = parseCell(strNum);
        } catch (IllegalArgumentException e) {
            num = Double.parseDouble(strNum);
        }
        return num;
    }

    // Parses and returns value of cell given name. If not a cell, throws an IllegalArgumentException
    public double parseCell(String cellName) {
        Cell cell = parentSpreadsheet.getCell(new SpreadsheetLocation(cellName));
        if ( !(cell instanceof RealCell) ) {
            throw new IllegalStateException("Referenced cell does not have a valid value.");
        }
        // If formula cell, run with circular reference checks
        String value;
        if (cell instanceof FormulaCell) {
            value = ((FormulaCell) cell).getValueCheckCircular(upstreamCells);
        } else {
            value = ((RealCell) cell).getFullStringValue();
        }
        // If value is #ERROR, throw error
        if (value.equals("#ERROR")) {
            throw new IllegalStateException("Referenced cell has an error");
        }
        return Double.parseDouble(value);
    }

    // Given arraylist of the inputs of function, calculates and returns avg of cell range
    public double functionSum(ArrayList<String> function) {
        // If arrayList is not length 2 ("avg" and range), error out
        if (function.size() != 2) {
            throw new IllegalArgumentException("Sum function must only contain the word 'sum' and a range, like A1-C5");
        }
        String range = function.get(1);

        return getCellRangeSum(range);

    }

    // Given arraylist of the inputs of function, calculates and returns avg of cell range
    public double functionAvg(ArrayList<String> function) {
        // If arrayList is not length 2 ("avg" and range), error out
        if (function.size() != 2) {
            throw new IllegalArgumentException("Avg function must only contain the word 'avg' and a range, like A1-C5");
        }
        String range = function.get(1);

        return getCellRangeSum(range)/ getCellRangeCount(range);
    }

    // Sets up test info and tests for pattern of alternating cells and numbers.
    public void validate() {
        // Assign dummy spreadsheet if a spreadsheet doesn't already exist
        if (parentSpreadsheet == null) {
            parentSpreadsheet = new Spreadsheet();
            // Fill spreadsheet with temporary random real value to prevent premature test stopping
            for (int i = 0; i < parentSpreadsheet.getRows(); i++) {
                for (int j = 0; j < parentSpreadsheet.getCols(); j++) {
                    parentSpreadsheet.setCell(new ValueCell(Math.random() + 1 + ""), i, j);
                }
            }
        }
        // Fill operator array
        operators = new ArrayList<>();
        operators.add("+");
        operators.add("-");
        operators.add("*");
        operators.add("/");

        // Test with spreadsheet
        abbreviatedCellText();
    }

    // Get sum of values from cell range
    public double getCellRangeSum(String range) {

        String[] corners = range.split("-");
        if ( !(corners.length == 2) ) {
            throw new IllegalArgumentException("Invalid avg expression. Must only contain avg and a range of 2 cells separated by a - without spaces.");
        }

        SpreadsheetLocation corner1 = new SpreadsheetLocation(corners[0]);
        SpreadsheetLocation corner2 = new SpreadsheetLocation(corners[1]);
        double sum = 0.0;
        for (int i = corner1.getRow(); i <= corner2.getRow(); i++) {
            for (int j = corner1.getCol(); j <= corner2.getCol(); j++) {
                String cellName = (char) (j + 'A') + "" + (i + 1);
                sum += parseCell(cellName);
            }
        }
        return sum;
    }

    // Get count of cells in cell range - does not distinguish between different cell types including empty

    public double getCellRangeCount(String range) {
        String[] corners = range.split("-");
        if ( !(corners.length == 2) ) {
            throw new IllegalArgumentException("Invalid avg expression. Must only contain avg and a range of 2 cells separated by a - without spaces.");
        }

        SpreadsheetLocation corner1 = new SpreadsheetLocation(corners[0]);
        SpreadsheetLocation corner2 = new SpreadsheetLocation(corners[1]);
        // Multiply dimensions together to get count
        return ( corner2.getRow() - corner1.getRow() + 1 ) * ( corner2.getCol() - corner1.getCol() + 1 );
    }

}
