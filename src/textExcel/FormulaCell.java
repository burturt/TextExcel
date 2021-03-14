package textExcel;

import java.util.ArrayList;

public class FormulaCell extends RealCell {

    private ArrayList<String> operators;

    public FormulaCell(String value) {
        super(value);
    }


    @Override
    public String abbreviatedCellText() {
        return String.format("%-10.10s", getDoubleValue() + "");
    }

    @Override
    public double getDoubleValue() {
        // Split by spaces and make all upper case
        String[] expressionPartsArray = fullCellText().toUpperCase().split(" ");
        ArrayList<String> expressionParts = new ArrayList<>();
        for (String part : expressionPartsArray) {
            expressionParts.add(part);
        }

        // Remove empty elements (caused by 2+ spaces in a row)
        for (int i = 0; i < expressionParts.size(); i++) {
            if (expressionParts.get(i).equals("")) {
                expressionParts.remove(i);
                i--;
            }
        }

        // Check and discard ( and ) elements at beginning and end
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
            try {
                Double.parseDouble(expressionParts.get(i - 1));

            } catch (Exception e) {
                throw new IllegalArgumentException("Expression must alternate between numbers/cells and operators");
            }
            if (!operators.contains(expressionParts.get(i))) {
                throw new IllegalArgumentException("Expression must alternate between numbers/cells and operators");
            }
        }
        try {
            Double.parseDouble(expressionParts.get(expressionParts.size()-1));
        } catch (Exception e) {
            throw new IllegalArgumentException("Expression must end in a number");
        }

        // Loop through elements of the array:
        // Get number, get operator and number after in next element, calculate and collapse
        while (1 < expressionParts.size()) {
            expressionParts.set(0, evaluateOperation(expressionParts.get(0), expressionParts.get(1), expressionParts.get(2)));
            expressionParts.remove(2);
            expressionParts.remove(1);
        }

        return Double.parseDouble(expressionParts.get(0));

    }

    // Executes operation given in string form the first number, operator, and second number
    public String evaluateOperation(String num1, String operator, String num2) {
        // Convert nums to doubles
        double num1d = Double.parseDouble(num1);
        double num2d = Double.parseDouble(num2);
        switch (operator){
            case "+":
                return num1d + num2d + "";
            case "-":
                return num1d - num2d + "";
            case "*":
                return num1d * num2d + "";
            case "/":
                return num1d / num2d + "";
        }
        throw new IllegalStateException("An unknown error occurred");
    }

    public double functionSum(ArrayList<String> function) {
        // If arrayList is not length 2 ("avg" and range), error out
        if (function.size() != 2) {
            throw new IllegalArgumentException("Sum function must only contain the word 'sum' and a range, like A1-C5");
        }
        String range = function.get(function.indexOf("SUM") + 1);
        String[] corners = range.split("-");
        if ( !(corners.length == 2) ) {
            throw new IllegalArgumentException("Invalid sum expression. Must only contain sum and a range of 2 cells separated by a - without spaces.");
        }

        return 0.0;

    }

    public double functionAvg(ArrayList<String> function) {
        return 0.0;
    }

    // Should be run on creation
    public void testValid() {
        // Initialize operators array
        operators = new ArrayList<>();
        operators.add("+");
        operators.add("-");
        operators.add("*");
        operators.add("/");

        // Test using operators
        getDoubleValue();

    }


}
