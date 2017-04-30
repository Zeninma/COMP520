package miniJava;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.ContextualAnalyzer.IdentificationVisitor;
import miniJava.ContextualAnalyzer.TypeCheckVisitor;
import miniJava.CodeGenerator.*;

/**
 * Recognize whether input entered through the keyboard is a valid
 * arithmetic expression as defined by the simple CFG and scanner grammar above.  
 * 
 */
public class Compiler {
	public static void main(String[] args) {
		InputStream inputStream = null;
		String fileName = "test.java";
		try{
			inputStream = new FileInputStream(args[0]);
			fileName = args[0];
		}
		catch(FileNotFoundException e){
			System.out.println("Input file " + args[0] + "not found");
			System.exit(1);
		}
		
		ErrorReporter reporter = new ErrorReporter();
		Scanner scanner = new Scanner(inputStream, reporter);
		Parser parser = new Parser(scanner, reporter);
		AST ast = parser.parse();

		if (reporter.hasErrors()) {
			System.out.println("INVALID miniJava program. Has Error During Syntatical Analysis");
			System.exit(4);
		}

		IdentificationVisitor visitor = new IdentificationVisitor();
		visitor.identifyTree(ast);
		TypeCheckVisitor typeVisitor = new TypeCheckVisitor();
		typeVisitor.check(ast, reporter);
//		Generator generator = new Generator(reporter, fileName);
//		generator.startGenerate(ast);
		
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
