package miniJava;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;

/**
 * Recognize whether input entered through the keyboard is a valid
 * arithmetic expression as defined by the simple CFG and scanner grammar above.  
 * 
 */
public class Compiler {

	public static void main(String[] args) {

		InputStream inputStream = null;
		try{
			inputStream = new FileInputStream(args[0]);
		}
		catch(FileNotFoundException e){
			System.out.println("Input file " + args[0] + "not found");
			System.exit(1);
		}
		
		ErrorReporter reporter = new ErrorReporter();
		Scanner scanner = new Scanner(inputStream, reporter);
		Parser parser = new Parser(scanner, reporter);

		System.out.println("Syntactic analysis ... ");
		parser.parse();
		System.out.print("Syntactic analysis complete:  ");
		if (reporter.hasErrors()) {
			System.out.println("INVALID miniJava program");
			System.exit(4);
		}
		else {
			System.out.println("valid miniJava program");
			System.exit(0);
		}
	}
}
