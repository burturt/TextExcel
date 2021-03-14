/*
 * Creates instance of Spreadsheet, reads and runs commands, prints info to use
 *
 * @author Alec Machlis
 * @version March 11, 2021
 */
package textExcel;

import java.util.Scanner;

// Update this file with your own code.

public class TextExcel
{
	private static final String version = "2.0";
	public static void main(String[] args)
	{
	    System.out.println("Welcome to TextExcel v" + version + " by Alec Machlis!");
	    System.out.println("Commands:");
	    // Print command reference from spreadsheet class
		System.out.println(Spreadsheet.getCommandReference());
		System.out.println();
	    System.out.println("Creating an empty spreadsheet...");

		Spreadsheet spreadsheet = new Spreadsheet();
		System.out.println(spreadsheet);
		System.out.println();
		System.out.println("Done!");

		String command = "";
		Scanner input = new Scanner(System.in);
		while (!command.equalsIgnoreCase("quit")) {
			System.out.print("textExcel-" + version + "$ ");
			command = input.nextLine().trim();
			System.out.println(spreadsheet.processCommand(command));
		}
		System.out.println("Goodbye!");
	}

}
