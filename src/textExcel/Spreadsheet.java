/*
 * Holds data for and processes actions on a spreadsheet/grid
 *
 * @author Alec Machlis
 * @version March 11, 2021
 */

package textExcel;


import java.util.*;

public class Spreadsheet implements Grid
{
	private Cell[][] cells;
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
			{"*<value>", "<value>", "Plain numerical value", "Stores a number, can be a decimal. Can be used in computations"},
			{"*<perc>", "<perc>%", "Percent value", "Stores a percent of something. Stored as decimal, printed as percentage."},
			{"*<formula>","( <formula> )", "Mathematical formula", "A mathematical formula. Can reference other cells and use +, -, *, /, avg, sum. See 'help avg' or 'help sum'."},
			{"*avg", "avg <cell>-<cell>", "Calculate average in formula", "Given a range of cells, calculates the average value of all value, percent, and other formula cells."},
			{"*sum", "sum <cell>-<cell>", "Calculates sum in formula", "Given a range of cells, calculates the sum of all value, percentage, and other formula cells"}
	};


	public Spreadsheet() {
		// Create new cell array
		cells = new Cell[20][12];
		// Fill array with Empty Cells
		clearFullSpreadsheet();
		// Set up history data
		history = new ArrayList<>();
		historyLength = -1; // -1 signifies history disabled
	}

	// Assign all cells in array an EmptyCell
	public String clearFullSpreadsheet() {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = new EmptyCell();
			}
		}
		return toString();
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


		return processCellAssignment(command);

	}

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
		try {
			// Skip substring if command is not long enough. SpreadsheetLocation fails if invalid cell name
			SpreadsheetLocation cellLoc;
			if (command.length() <= 2) {
				cellLoc = new SpreadsheetLocation(command);
			} else {
				cellLoc = new SpreadsheetLocation(command.substring(0, 3).trim());
			}
			// Fails if cell location doesn't exist on spreadsheet
			Cell cell = getCell(cellLoc);

			// If cell is the only thing in the command, print cell text
			if (command.length() <= 3) {
				// Skip loop execution if equals is at index 2, so A4="hi" won't print out cell but assign properly
				if (command.length() == 2 || command.charAt(2) != '=')
					return cell.fullCellText();
			}

			// Split at = sign to try to get assignment value
			String[] splitCommand = command.split("=", 2);
			if (splitCommand.length != 2) {
				return "ERROR: Invalid command."; // No equals sign exists
			}
			String assignStatement = splitCommand[1].trim();
			// Check if string starts with straight or curly double quote and ends with straight or curly double quote
			if ( (assignStatement.startsWith("\"") || assignStatement.startsWith("“"))
					&& (assignStatement.endsWith("\"") || assignStatement.endsWith("”")) ) {
				// Create new cell with text, not including quotes
				Cell newCell = new TextCell(assignStatement.substring(1, assignStatement.length() - 1));
				cells[cellLoc.getRow()][cellLoc.getCol()] = newCell;
				return toString();
			}


		} catch (Exception e) {
			return "ERROR: Invalid command.";
		}
		// If reached here, no format matched, so must be invalid
		return "ERROR: Invalid command.";
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
	public String commandHistoryClear(String arugments) {
		// Get part after "clear"
		int clearCount = Integer.parseInt(arugments.substring(arugments.indexOf("clear") + 5).trim());
		int historySize = history.size();
		// Loop through clearCount elements at the end and remove them
		// Do not let loop below 0
		for (int i = historySize - 1; i >=  Math.max(historySize - clearCount,  0); i--) {
			history.remove(i);
		}
		return "";
	}

	// Process History Stop command
	public String commandHistoryStop(String arugments) {
		historyLength = -1;
		history.clear();
		return "";
	}

	@Override
	public int getRows()
	{
		return cells.length;
	}

	@Override
	public int getCols()
	{
		return cells[0].length;
	}

	@Override
	public Cell getCell(Location loc)
	{
		return cells[loc.getRow()][loc.getCol()];
	}

	@Override
	public String getGridText()
	{
		return toString();
	}

	// Returns command format in nice string format
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
				cells[clearLoc.getRow()][clearLoc.getCol()] = new EmptyCell();
				return toString();
			} catch (Exception e) {
				return "Invalid syntax for clear command. 'help clear' for more info.";
			}
		}
	}
	public String toString() {
		String formattedTable = "   |";
		// Make table header
		for (int i = 'A'; i <= 'L'; i++) {
			formattedTable += String.format("%-10.10s|", (char) i + "");
		}
		for (int i = 1; i <= cells.length; i++) {
			formattedTable += String.format("%n%-3s|", i + "");
			for (Cell cell : cells[i - 1]) {
				formattedTable += cell.abbreviatedCellText() + "|";
			}
		}

		return formattedTable + '\n';
	}

}
