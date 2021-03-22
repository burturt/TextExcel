/*
 * Holds data for and processes actions on a spreadsheet/grid
 *
 * @author Alec Machlis
 * @version March 22, 2021
 */

package textExcel;

import java.util.ArrayList;

public class Spreadsheet implements Grid
{
	private Cell[][] sheet;
	private ArrayList<String> history;
	private int historyLength;

	// Command reference data. Array of string arrays: index 0 is command, index 1 is command syntax, index 2 is description, index 3 is long description
	// Use printf to print out nice table or loop through IDs to get all commands.
	private static final String[][] commands = {
			{"   ID    ", "   Command syntax   ", "                 Description                 ", "More info"},
			{"----------", "--------------------", "---------------------------------------------", "------"},
			{"<cell>", "<cell>", "Outputs the value of a cell", "Shows the full raw value of a given cell without formatting or running calculations.\n\t\tCell must be in range of spreadsheet: row 0-20 and column A-L."},
			{"print", "print", "Prints full spreadsheet", "Prints out the entire spreadsheet and its data without modifying it"},
			{"history", "history <args>", "Interact with history", "'history start <count>' enables history with <count> max remembered commands\n\t\t'history display' displays the current history buffer\n\t\t'history clear <count>' clears <count> commands from history starting with older commands\n\t\t'history stop' stops history logging and clears logs."},
			{"clear", "clear [cell]", "Clears cell data", "'clear' clears entire spreadsheet, 'clear [cell]' clears just that cell, like 'clear A1'"},
			{"help", "help [command ID]", "Print info on given or all commands.", "'help' shows all commands, 'help [command]' like 'help clear' or 'help \"str\"' shows help on specific command.\n\t\tUse <cell> or [cell] in place of a cell and <expr> or [expr] in place of an expression."},
			{"quit", "quit", "Exits the program and discards data."},
			{"=", "<cell> = <expr>", "Assigns a value to a cell", "Cell must be in range (see 'help <cell>'). See `help <expr>` for more info"},
			{"----------", "--------------------", "---------------------------------------------", "------"},
			{"<expr>", "     Expression     ", "Explanation of different types of expressions", "\"<text>\" is a text value - must be surrounded by quotes\n\t\t<value> is a numerical value - do not surround by anything.\n\t\t<perc>% is a percentage - do not surround by anything but add percent sign to end\n\t\t( <formula> ) is a mathematical expression - see 'help <formula>'"},
			{"<text>", "\"<text>\"", "Plain text cell - can store anything", "Cell that simply holds a text value. Cannot do computations. Indicate text by surrounding with quotes"},
			{"<value>", "<value>", "Plain numerical value", "Stores a number, can be a decimal. Can be used in computations"},
			{"<perc>", "<perc>%", "Percent value", "Stores a percent of something. Stored as decimal, printed as percentage."},
			{"<formula>","( <formula> )", "Mathematical formula", "A mathematical formula that is either arithmetic or a range function.\n\t\tArithmetic: Can use +, -, *, / combined with numbers and cell references. Evaluates left-to-right.\n\t\tRange function: Can be either 'sum' or 'svg' with nothing else in it. See 'help avg' or 'help sum' for info about the 2 functions.\n\t\tOperators and functions MUST be separated by spaces."},
			{"avg", "avg <cell>-<cell>", "Calculate average in formula", "Given a range of cells, calculates the average value of all value, percent, and other formula cells."},
			{"sum", "sum <cell>-<cell>", "Calculates sum in formula", "Given a range of cells, calculates the sum of all value, percentage, and other formula cells"}
	};


	public Spreadsheet() {
		// Create new cell array
		sheet = new Cell[20][12];
		// Fill array with Empty Cells
		clearFullSpreadsheet();
		// Set up history data
		history = new ArrayList<>();
		historyLength = -1; // -1 signifies history disabled
	}

	// Assign all cells in array an EmptyCell
	public String clearFullSpreadsheet() {
		for (int i = 0; i < sheet.length; i++) {
			for (int j = 0; j < sheet[i].length; j++) {
				sheet[i][j] = new EmptyCell();
			}
		}
		return toString();
	}

	// Set cell given x/y location index
	public void setCell(Cell cell, int row, int col) {
		sheet[row][col] = cell;
	}

	// Master method to process raw command input
	@Override
	public String processCommand(String command)
	{

		// Don't process history commands into history
		if (command.toLowerCase().startsWith("history")) {
			return commandHistory(command.trim());
		}
		addToHistory(command);

		command = command.trim();

		// return "" if no command;
		if (command.equals("")) {
			return "";
		}

		// Process non-assignment commands
		if (command.toLowerCase().startsWith("quit")) {
			return ""; // Main loop will terminate after this
		}
		if (command.toLowerCase().startsWith("help")) {
			return commandHelp(command);
		}
		if (command.toLowerCase().startsWith("clear")) {
			return commandClear(command);
		}
		if (command.toLowerCase().startsWith("print")) {
			return toString();
		}
		if (command.toLowerCase().startsWith("sort")) {
			if (command.toLowerCase().charAt(4) == 'a') {
				return commandSort(command, true);
			} else if (command.toLowerCase().charAt(4) == 'd') {
				return commandSort(command, false);
			} else {
				return "ERROR: invalid command";
			}
		}
		// Process cell assignment commands
		return processCellAssignment(command);

	}

	// Add command to history and remove if too long
	public void addToHistory(String command) {
		history.add(0, command);
		if (historyLength < history.size()) {
			history.remove(history.size() - 1);
		}
	}

	// Code dedicated to processing a cell assignment
	public String processCellAssignment(String command) {
		command = command.trim();

		// Try to resolve cell name - if fails, must be invalid
		Cell cell;
		SpreadsheetLocation cellLoc;
		try {
			// Skip substring if command is not long enough. SpreadsheetLocation fails if invalid cell name
			if (command.length() <= 2) {
				cellLoc = new SpreadsheetLocation(command);
			} else {
				cellLoc = new SpreadsheetLocation(command.substring(0, 3).trim());
			}
			// Fails if cell location doesn't exist on spreadsheet
			cell = getCell(cellLoc);

		} catch (Exception e) {
			return "ERROR: Invalid command or invalid cell name.";
		}

		// If cell is the only thing in the command, print cell text
		if (command.length() <= 3) {
			// Skip loop execution if equals is at index 2, so A4="hi" won't print out cell but assign properly
			if (command.length() == 2 || command.charAt(2) != '=') {
				return cell.fullCellText();
			}
		}

		// Split at = sign to try to get assignment value
		String[] splitCommand = command.split("=", 2);
		if (splitCommand.length != 2) {
			return "ERROR: Invalid command."; // No equals sign exists
		}
		String assignStatement = splitCommand[1].trim();
		Cell newCell;

		// Throw error if assignment expression is invalid
		try {

			// Check if string starts and ends with either type of double quote --> text
			if ((assignStatement.startsWith("\"") || assignStatement.startsWith("“"))
					&& (assignStatement.endsWith("\"") || assignStatement.endsWith("”"))) {
				// Create new cell with text, not including quotes
				newCell = new TextCell(assignStatement.substring(1, assignStatement.length() - 1));

			// Check if string starts and ends with () --> formula
			} else if (assignStatement.startsWith("(") && assignStatement.endsWith(")")) {
				// Create new cell with formula
				newCell = new FormulaCell(assignStatement, this);

			// Check if string ends in % --> percent
			} else if (assignStatement.endsWith("%")) {
				newCell = new PercentCell(assignStatement);

			// If reached here, assignment must be value or invalid
			} else {
				newCell = new ValueCell(assignStatement);
			}



		} catch (Exception e) {
			return "ERROR: assignment expression is invalid: " + e;
		}
		sheet[cellLoc.getRow()][cellLoc.getCol()] = newCell;
		return toString();
	}

	// Process base history command
	public String commandHistory(String command) {
		// Get part after "history"
		String arguments = command.substring(command.indexOf("history") + 7).trim();

		// Process start command
		if (arguments.startsWith("start")) {
			return commandHistoryStart(arguments);
		}

		// Process display command
		if (arguments.startsWith("display")) {
			return commandHistoryDisplay(arguments);
		}

		// Process clear command
		if (arguments.startsWith("clear")) {
			return commandHistoryClear(arguments);
		}

		// Process stop command
		if (arguments.startsWith("stop")) {
			return commandHistoryStop(arguments);
		}

		return "ERROR: Invalid history command. Run 'help history' for more info.";
	}

	// Process History Start command
	public String commandHistoryStart(String arguments) {
		// Verify history isn't already started
		if (historyLength != -1) {
			return "ERROR: History is already started. Stop with 'history stop' first";
		}
		// Get part after "start"
		try {
			int tempHistoryLength = Integer.parseInt(arguments.substring(arguments.indexOf("start") + 5).trim());

			// Verify valid history length and assign if valid
			if (tempHistoryLength >= 1) {
				this.historyLength = tempHistoryLength;
				return "";
			} else {
				return "ERROR: history start needs a valid history number";
			}
		} catch (Exception e) {
			// Catch if parseInt fails
			return "ERROR: history start needs a valid history number";
		}
	}

	// Process History Display command
	public String commandHistoryDisplay(String arguments) {
		String fullHistory = "";
		for (String loggedCommand : history) {
			fullHistory += loggedCommand + '\n';
		}
		return fullHistory;
	}

	// Process History Clear command
	public String commandHistoryClear(String arguments) {
		// Get part after "clear"
		int clearCount = Integer.parseInt(arguments.substring(arguments.indexOf("clear") + 5).trim());
		int historySize = history.size();
		// Loop through clearCount elements at the end and remove them
		// Do not let loop below 0
		for (int i = historySize - 1; i >=  Math.max(historySize - clearCount,  0); i--) {
			history.remove(i);
		}
		return "";
	}

	// Process History Stop command
	public String commandHistoryStop(String arguments) {
		historyLength = -1;
		history.clear();
		return "";
	}

	// Process sort commands, given if ascending
	public String commandSort(String command, boolean ascending) {

		String arguments = command.substring(5).trim();

		ArrayList<SpreadsheetLocation> cellsOrder = getCells(arguments.trim());
		ArrayList<Cell> cells = new ArrayList<>();
		for (SpreadsheetLocation loc : cellsOrder) {
			cells.add(getCell(loc));
		}
		// Selection sort - find minimum, remove, add to beginning, rinse and repeat ignoring the last first cell added
		for (int i = 0; i < cells.size(); i++) {
			Cell extreme = cells.get(i);
			for (int j = i; j < cells.size(); j++) {
				if (ascending && compareCells(extreme, cells.get(j)) > 0) {
					extreme = cells.get(j);
				} else if (!ascending && compareCells(extreme, cells.get(j)) < 0) {
					extreme = cells.get(j);
				}
			}
			// Remove that cell
			cells.remove(extreme);
			// Add cell to front
			cells.add(i, extreme);

		}
		// Assign cells back again
		setCells(arguments, cells);
		return toString();
	}

	// Compare 2 cells after verifying type
	public int compareCells(Cell cell1, Cell cell2) {

		// Empty Cells always come first
		if (cell1 instanceof EmptyCell) {
			return -1;
		} else if (cell2 instanceof EmptyCell) {
			return 1;
		}
		// If one but not both are TextCells, return the text cell
		if (cell1 instanceof TextCell && !(cell2 instanceof TextCell)) {
			return -1;
		} else if (cell2 instanceof TextCell && !(cell1 instanceof  TextCell)) {
			return 1;
		}
		// If both text cells, cast to TextCell and compare
		if (cell1 instanceof TextCell && cell2 instanceof TextCell) {
			return ((TextCell) cell1).compareTo((TextCell) cell2);
		}
		// If here, both must be RealCells, so sort them
		return ((RealCell) cell1).compareTo((RealCell) cell2);

	}


	// Get spreadsheet locations from cell range
	public ArrayList<SpreadsheetLocation> getCells(String cellRange) {
		ArrayList<SpreadsheetLocation> cells = new ArrayList<>();
		String[] corners = cellRange.split("-");
		if ( !(corners.length == 2) ) {
			throw new IllegalArgumentException("Invalid cell range");
		}

		SpreadsheetLocation corner1 = new SpreadsheetLocation(corners[0]);
		SpreadsheetLocation corner2 = new SpreadsheetLocation(corners[1]);
		for (int row = corner1.getRow(); row <= corner2.getRow(); row++) {
			for (int col = corner1.getCol(); col <= corner2.getCol(); col++) {
				SpreadsheetLocation loc = new SpreadsheetLocation(row, col);
				cells.add(loc);
			}
		}
		return cells;
	}

	// Set cells given arraylist and cellrange
	public void setCells(String cellRange, ArrayList<Cell> cells) {
		String[] corners = cellRange.split("-");
		if ( !(corners.length == 2) ) {
			throw new IllegalArgumentException("Invalid cell range");
		}

		SpreadsheetLocation corner1 = new SpreadsheetLocation(corners[0]);
		SpreadsheetLocation corner2 = new SpreadsheetLocation(corners[1]);
		int cellListIdx = 0;
		for (int row = corner1.getRow(); row <= corner2.getRow(); row++) {
			for (int col = corner1.getCol(); col <= corner2.getCol(); col++) {
				SpreadsheetLocation loc = new SpreadsheetLocation(row, col);
				setCell(cells.get(cellListIdx), loc.getRow(), loc.getCol());
				cellListIdx++;
			}
		}
	}

	// Get number of rows
	@Override
	public int getRows()
	{
		return sheet.length;
	}

	// Get number of columns
	@Override
	public int getCols()
	{
		return sheet[0].length;
	}

	// Return cell at location
	@Override
	public Cell getCell(Location loc)
	{
		return sheet[loc.getRow()][loc.getCol()];
	}

	// Calls toString() for compatibility
	@Override
	public String getGridText()
	{
		return toString();
	}

	// Returns command format in nice string format - NOT PART OF ASSIGNMENT, just something I did for fun
	public static String getCommandReference() {
		String reference = "";
		reference += "Arguments in [] are optional, arguments in <> are mandatory. Commands are case-insensitive.\n";
		reference += "If you want more info about a command, do 'help [ID]' like 'help <text>'\n";
		reference += "Commands prefixed by a * are a WIP";
		// Loop through array of String arrays of length 3: index 0 = command format, index 1 = description

		for (String[] command : commands) {
			reference += String.format("%n|%-10.10s| %-20.20s | %-45.45s |", command[0], command[1], command[2]);
		}
		return reference;
	}

	// Print full description of command given command name
	// If no valid command given, print full reference.
	public static String commandHelp(String command) {
		String[] helpArgument = command.toLowerCase().split(" ");
		if (helpArgument.length != 1) {
			command = helpArgument[1].toLowerCase();
			// Find command in commands array that matches command and if match, print and return
			for (String[] tempCommandInfo : commands) {
				if (command.startsWith(tempCommandInfo[0])) {
					return '\t' + tempCommandInfo[1] + "\n\t\t" + tempCommandInfo[2] + "\n\t\t" + tempCommandInfo[3];
				}
			}
			System.out.println("Invalid argument.");
		}
		return getCommandReference();

	}

	// Clear command handler
	public String commandClear(String command) {
		String[] helpArgument = command.split(" ",2);
		// If no arguments, clear entire spreadsheet. Else, clear only that cell
		if (helpArgument.length == 1) {
			return clearFullSpreadsheet();
		} else {
			try {
				SpreadsheetLocation clearLoc = new SpreadsheetLocation(helpArgument[1].trim());
				sheet[clearLoc.getRow()][clearLoc.getCol()] = new EmptyCell();
				return toString();
			} catch (Exception e) {
				return "Invalid syntax for clear command. 'help clear' for more info.";
			}
		}
	}

	// Returns nice view of spreadsheet values
	public String toString() {
		String formattedTable = "   |";
		// Make table header
		for (int i = 'A'; i <= 'L'; i++) {
			formattedTable +=  (char) i + "         |";
		}
		// Print rows
		for (int i = 1; i <= sheet.length; i++) {
			// Get # of spaces by String length of number
			String spaces = "";
			for (int s = 0; s < 3 - ("" + i).length(); s++ ) {
				spaces += " ";
			}
			formattedTable += "\n" + i + spaces + "|";
			// Print cells
			for (Cell cell : sheet[i - 1]) {
				formattedTable += cell.abbreviatedCellText() + "|";
			}
		}

		return formattedTable + '\n';
	}

}
