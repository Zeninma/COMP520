/**
 * Parser
 *
 * Grammar:
 *   S ::= E '$'
 *   E ::= T (oper T)*     
 *   T ::= num | '(' E ')'
 */
package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.TokenKind;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;

public class Parser {

	private Scanner scanner;
	private ErrorReporter reporter;
	private Token token;
	private boolean trace = true;
	private boolean has_minus_unop = false;

	public Parser(Scanner scanner, ErrorReporter reporter) {
		this.scanner = scanner;
		this.reporter = reporter;
	}


	/**
	 * SyntaxError is used to unwind parse stack when parse fails
	 *
	 */
	class SyntaxError extends Error {
		private static final long serialVersionUID = 1L;	
	}

	/**
	 *  parse input, catch possible parse error
	 */
	public void parse() {
		token = scanner.scan();
		try {
			parseProgram();
		}
		catch (SyntaxError e) { }
	}

	//    Program ::= (ClassDeclaration)* eot$
	private void parseProgram() throws SyntaxError {
		while(token.kind == TokenKind.CLASS){
			parseClass();
		}
		accept(TokenKind.EOT);
	}
	
	//ClassDeclaration ::=class id { FieldMethodDeclaration* }
	private AST parseClass() throws SyntaxError{
		FieldDeclList fdl = new FieldDeclList();
		MethodDeclList mdl = new MethodDeclList();
		accept(TokenKind.CLASS);
		// get class name
		String cn = token.toString();
		accept(TokenKind.ID);
		accept(TokenKind.LEFTCBRACKET);
		while(token.kind != TokenKind.RIGHTCBRACKET){
			AST field_method_decl = parseFieldMethodDeclaration();
			if(field_method_decl instanceof FieldDecl){
				fdl.add((FieldDecl) field_method_decl);
			}
			else{
				mdl.add((MethodDecl) field_method_decl);
			}
			//need to check the exact type of the result
		}
		accept(TokenKind.RIGHTCBRACKET);
		ClassDecl class_decl = new ClassDecl(cn,fdl, mdl, null);
		return class_decl;
	}
	
	//FieldMethodDeclaration ::= Visibility Access (Type|void) id ( '('ParameterList?')') {statement*} )?
	private AST parseFieldMethodDeclaration() throws SyntaxError{
		boolean isPrivate;
		boolean isStatic;
		TypeDenoter t;
		String name;
		MemberDecl md;
		ParameterDeclList pl;
		StatementList sl;
		SourcePosition posn = null;
		
		if(token.kind == TokenKind.PUBLIC || token.kind == TokenKind.PRIVATE){
			acceptIt();
		}
		if(token.kind == TokenKind.STATIC){
			acceptIt();
		}
		//parse Type
		switch (token.kind){
		case INT:
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				accept(TokenKind.RIGHTSBRACKET);
			}
			break;
			
		case ID:
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				accept(TokenKind.RIGHTSBRACKET);
			}
			break;
			
		case BOOL:
			acceptIt();
			break;
			
		case VOID:
			acceptIt();
			accept(TokenKind.ID);
			accept(TokenKind.LPAREN);
			if(token.kind != TokenKind.RPAREN){
				parseParameterList();
			}
			accept(TokenKind.RPAREN);
			accept(TokenKind.LEFTCBRACKET);
			while(token.kind != TokenKind.RIGHTCBRACKET){
				parseStatement();
			}
			accept(TokenKind.RIGHTCBRACKET);
			return;
			
		default:
			parseError("Invalid Term - expecting Type or Void but found" + token.kind);
		}
		accept(TokenKind.ID);
		switch(token.kind){
		case LPAREN:
			acceptIt();
			if(token.kind != TokenKind.RPAREN){
				parseParameterList();
			}
			accept(TokenKind.RPAREN);
			accept(TokenKind.LEFTCBRACKET);
			while(token.kind != TokenKind.RIGHTCBRACKET){
				parseStatement();
			}
			accept(TokenKind.RIGHTCBRACKET);
			return;
		case SEMICOL:
			acceptIt();
			return;
		default:
			parseError("Invalid Term - expecting LPAREN or SEMICOL but found" + token.kind);
		}
	}
	
	
	//Statement ::= (...|...|...)
	private void parseStatement() throws SyntaxError{
		switch (token.kind){
		case LEFTCBRACKET:
			acceptIt();
			while(token.kind != TokenKind.RIGHTCBRACKET){
				parseStatement();
			}
			accept(TokenKind.RIGHTCBRACKET);
			return;
			
		case INT: case BOOL:
			parseType();
			accept(TokenKind.ID);
			accept(TokenKind.ASSIGN);
			parseExpression();
			accept(TokenKind.SEMICOL);
			return;
		/*gitttt*/
		/*gittt*/	
		//ID needs to be debuged
		case ID:
			acceptIt();
			switch(token.kind){
			case ID:
				acceptIt();
				accept(TokenKind.ASSIGN);
				parseExpression();
				accept(TokenKind.SEMICOL);
				return;
			case LEFTSBRACKET:
				acceptIt();
				switch(token.kind){
				//for expression
					case ID: case NOT: case MINUS: case LPAREN:
					case NUM: case TRUE: case FALSE: case NEW:
						parseExpression();
						accept(TokenKind.RIGHTSBRACKET);
						while(token.kind == TokenKind.PERIOD){
							acceptIt();
							accept(TokenKind.ID);
							if(token.kind == TokenKind.LEFTSBRACKET){
								acceptIt();
								parseExpression();
								accept(TokenKind.RIGHTSBRACKET);
							}
						}
						switch(token.kind){
						case ASSIGN:
							acceptIt();
							parseExpression();
							accept(TokenKind.SEMICOL);
							return;
						case LPAREN:
							acceptIt();
							if(token.kind !=TokenKind.RPAREN){
								parseArgumentList();
							}
							accept(TokenKind.RPAREN);
							accept(TokenKind.SEMICOL);
							return;
						default:
							parseError("Invalid Term - expecting LPAREN or ASSIGN but found " + token.kind);
							return;
						}
					// for Type
					case RIGHTSBRACKET:
						acceptIt();
						accept(TokenKind.ID);
						accept(TokenKind.ASSIGN);
						parseExpression();
						accept(TokenKind.SEMICOL);
						return;
					default:
						parseError("Invalid Term - expecting ID, NOT,MINUS, LPAREN, NUM, TRUE"
								+ "FALSE, NEW or RIGHTSBRACKET but found " + token.kind);
						return;
				}
			//Reference
			case PERIOD:
				while(token.kind == TokenKind.PERIOD){
					acceptIt();
					accept(TokenKind.ID);
					//last changed
					if(token.kind == TokenKind.LEFTSBRACKET){
						acceptIt();
						parseExpression();
						accept(TokenKind.RIGHTSBRACKET);
					}
					switch(token.kind){
					case ASSIGN:
						acceptIt();
						parseExpression();
						accept(TokenKind.SEMICOL);
						return;
					case LPAREN:
						acceptIt();
						if(token.kind !=TokenKind.RPAREN){
							parseArgumentList();
						}
						accept(TokenKind.RPAREN);
						accept(TokenKind.SEMICOL);
						return;
					default:
						parseError("Invalid Term - expecting LPAREN or ASSIGN but found " + token.kind);
						return;
					}
				}
				return;
			//Reference
			case ASSIGN:
				acceptIt();
				parseExpression();
				accept(TokenKind.SEMICOL);
				return;
				
			case LPAREN:
				acceptIt();
				if(token.kind !=TokenKind.RPAREN){
					parseArgumentList();
				}
				accept(TokenKind.RPAREN);
				accept(TokenKind.SEMICOL);
				return;
				
			default:
				parseError("Invalid Term - expecting ID, LEFTSBRACKET, PERIOD, ASSIG or LPAREN but found "+ token.kind);
			}

			
		case THIS:
			parseReference();
			switch(token.kind){
			case ASSIGN:
				acceptIt();
				parseExpression();
				accept(TokenKind.SEMICOL);
				return;
			
			case LPAREN:
				acceptIt();
				if(token.kind != TokenKind.RPAREN){
					parseArgumentList();
				}
				accept(TokenKind.RPAREN);
				accept(TokenKind.SEMICOL);
				return;
				
			default:
				parseError("Invalid Term - expecting Assign or LPAREN but found " + token.kind);
			}
		case RETURN:
			acceptIt();
			if(token.kind != TokenKind.SEMICOL){
				parseExpression();
			}
			accept(TokenKind.SEMICOL);
			return;
			
		case IF:
			acceptIt();
			accept(TokenKind.LPAREN);
			parseExpression();
			accept(TokenKind.RPAREN);
			parseStatement();
			if(token.kind == TokenKind.ELSE){
				acceptIt();
				parseStatement();
			}
			return;
			
		case WHILE:
			acceptIt();
			accept(TokenKind.LPAREN);
			parseExpression();
			accept(TokenKind.RPAREN);
			parseStatement();
			return;
			
		default:
			parseError("Invalid Term - expecting LEFTCBRACKET or ID, BOOL, INT, THIS, RETURN, IF OR WHILE but found " + token.kind);
		}
	}
	
	//ArgumentList ::= Expression (, Expression)*
	private void parseArgumentList() throws SyntaxError{
		parseExpression();
		while(token.kind == TokenKind.COMMA){
			acceptIt();
			parseExpression();
		}
	}
	
	//Expression = (...|...|...) (binop Expression)*
	private void parseExpression() throws SyntaxError{
		switch (token.kind){
		case ID:
			parseReference();
			if(token.kind == TokenKind.LPAREN){
				acceptIt();
				if(token.kind != TokenKind.RPAREN){
					parseArgumentList();
				}
				accept(TokenKind.RPAREN);
			}
			while(token.kind == TokenKind.GT || token.kind == TokenKind.LT || token.kind == TokenKind.EQUAL ||
					token.kind == TokenKind.LEQ|| token.kind == TokenKind.GEQ || token.kind == TokenKind.NEQ||
					token.kind == TokenKind.AND || token.kind == TokenKind.OR || token.kind == TokenKind.PLUS||
					token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE || token.kind == TokenKind.MINUS){
				acceptIt();
				parseExpression();
			}
			return;
		
		case THIS:
			parseReference();
			if(token.kind == TokenKind.LPAREN){
				acceptIt();
				if(token.kind != TokenKind.RPAREN){
					parseArgumentList();
				}
				accept(TokenKind.RPAREN);
			}
			while(token.kind == TokenKind.GT || token.kind == TokenKind.LT || token.kind == TokenKind.EQUAL ||
					token.kind == TokenKind.LEQ|| token.kind == TokenKind.GEQ || token.kind == TokenKind.NEQ||
					token.kind == TokenKind.AND || token.kind == TokenKind.OR || token.kind == TokenKind.PLUS||
					token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE || token.kind == TokenKind.MINUS){
				acceptIt();
				parseExpression();
			}
			return;
			
		case NOT: case MINUS:
			if(this.has_minus_unop && token.kind == TokenKind.MINUS){
				parseError("Invalid Term - expect NOT but found " + token.kind + "illegal consectutive MINUS (-)");
			}
			else if(token.kind == TokenKind.NOT && this.has_minus_unop){
				this.has_minus_unop = false;
			}
			else if (token.kind == TokenKind.MINUS){
				this.has_minus_unop = true;
			}
			else{
				this.has_minus_unop = false;
			}
			acceptIt();
			parseExpression();
			while(token.kind == TokenKind.GT || token.kind == TokenKind.LT || token.kind == TokenKind.EQUAL ||
					token.kind == TokenKind.LEQ|| token.kind == TokenKind.GEQ || token.kind == TokenKind.NEQ||
					token.kind == TokenKind.AND || token.kind == TokenKind.OR || token.kind == TokenKind.PLUS||
					token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE || token.kind == TokenKind.MINUS){
				acceptIt();
				parseExpression();
			}
			return;
			
		case LPAREN:
			acceptIt();
			parseExpression();
			accept(TokenKind.LPAREN);
			while(token.kind == TokenKind.GT || token.kind == TokenKind.LT || token.kind == TokenKind.EQUAL ||
					token.kind == TokenKind.LEQ|| token.kind == TokenKind.GEQ || token.kind == TokenKind.NEQ||
					token.kind == TokenKind.AND || token.kind == TokenKind.OR || token.kind == TokenKind.PLUS||
					token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE || token.kind == TokenKind.MINUS){
				acceptIt();
				parseExpression();
			}
			return;
			
		case NUM: case TRUE: case FALSE:
			acceptIt();
			while(token.kind == TokenKind.GT || token.kind == TokenKind.LT || token.kind == TokenKind.EQUAL ||
					token.kind == TokenKind.LEQ|| token.kind == TokenKind.GEQ || token.kind == TokenKind.NEQ||
					token.kind == TokenKind.AND || token.kind == TokenKind.OR || token.kind == TokenKind.PLUS||
					token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE || token.kind == TokenKind.MINUS){
				acceptIt();
				parseExpression();
			}
			return;
			
		case NEW:
			acceptIt();
			switch(token.kind){
				case ID:
					acceptIt();
					if(token.kind == TokenKind.LEFTSBRACKET){
						acceptIt();
						parseExpression();
						accept(TokenKind.RIGHTSBRACKET);
					}
					else if(token.kind == TokenKind.LPAREN){
						acceptIt();
						accept(TokenKind.RPAREN);
					}
					else{
						parseError("Invalid Term - expecting LEFTCBRACKET or LPAREN but found " + token.kind);
						break;
					}
					while(token.kind == TokenKind.GT || token.kind == TokenKind.LT || token.kind == TokenKind.EQUAL ||
							token.kind == TokenKind.LEQ|| token.kind == TokenKind.GEQ || token.kind == TokenKind.NEQ||
							token.kind == TokenKind.AND || token.kind == TokenKind.OR || token.kind == TokenKind.PLUS||
							token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE || token.kind == TokenKind.MINUS){
						acceptIt();
						parseExpression();
					}
					return;
				case INT:
					acceptIt();
					accept(TokenKind.LEFTSBRACKET);
					parseExpression();
					accept(TokenKind.RIGHTSBRACKET);
					while(token.kind == TokenKind.GT || token.kind == TokenKind.LT || token.kind == TokenKind.EQUAL ||
							token.kind == TokenKind.LEQ|| token.kind == TokenKind.GEQ || token.kind == TokenKind.NEQ||
							token.kind == TokenKind.AND || token.kind == TokenKind.OR || token.kind == TokenKind.PLUS||
							token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE || token.kind == TokenKind.MINUS){
						acceptIt();
						parseExpression();
					}
					return;
				default:
					parseError("Invalid Term - expecting INT or ID but found " + token.kind);
			}
			
		
		default:
			parseError("Invalid Term - expecting ID, unop, LPAREN, NUM, TRUE, FALSE "
					+ "or NEW but found " + token.kind);
		}
		//11 binop in total
//		while(token.kind == TokenKind.GT || token.kind == TokenKind.LT || token.kind == TokenKind.EQUAL ||
//				token.kind == TokenKind.LEQ|| token.kind == TokenKind.GEQ || token.kind == TokenKind.NEQ||
//				token.kind == TokenKind.AND || token.kind == TokenKind.OR || token.kind == TokenKind.PLUS||
//				token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE || token.kind == TokenKind.MINUS){
//			acceptIt();
//			parseExpression();
//		}
	}
	
	//ReferenceSkipId::= ([Expression])? ( . id ([Expression])?)*

	//Reference::= ( id ([Expression])? | this ) ( . id ([Expression])?)*
	private void parseReference() throws SyntaxError{
		switch(token.kind){
		case ID:
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				parseExpression();
				accept(TokenKind.RIGHTSBRACKET);
			}
			while(token.kind == TokenKind.PERIOD){
				acceptIt();
				accept(TokenKind.ID);
				if(token.kind == TokenKind.LEFTSBRACKET){
					acceptIt();
					parseExpression();
					accept(TokenKind.RIGHTSBRACKET);
				}
			}
			return;
			
		case THIS:
			acceptIt();
			while(token.kind == TokenKind.PERIOD){
				acceptIt();
				accept(TokenKind.ID);
				if(token.kind == TokenKind.LEFTSBRACKET){
					acceptIt();
					parseExpression();
					accept(TokenKind.RIGHTSBRACKET);
				}
			}
			return;
			
		default:
			parseError("Invalid Term - expecting ID or THIS but found " + token.kind);
		}
	}
	
	//ParameterList ::= Type id (, Type id)*
	private void parseParameterList() throws SyntaxError{
		parseType();
		accept(TokenKind.ID);
		while(token.kind == TokenKind.COMMA){
			acceptIt();
			parseType();
			accept(TokenKind.ID);
		}
	}
	
	//Type ::= int | boolean | id | (int | id) []
	private void parseType() throws SyntaxError{
		switch (token.kind){
		case INT:
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				accept(TokenKind.RIGHTSBRACKET);
			}
			return;
			
		case ID:
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				accept(TokenKind.RIGHTSBRACKET);
			}
			return;
			
		case BOOL:
			acceptIt();
			return;
			
		default:
			parseError("Invalid Term - expecting Type or Void but found" + token.kind);
		}
	}


	
	/**
	 * accept current token and advance to next token
	 */
	private void acceptIt() throws SyntaxError {
		accept(token.kind);
	}

	/**
	 * verify that current token in input matches expected token and advance to next token
	 * @param expectedToken
	 * @throws SyntaxError  if match fails
	 */
	private void accept(TokenKind expectedTokenKind) throws SyntaxError {
		if (token.kind == expectedTokenKind) {
			if (trace)
				pTrace();
			token = scanner.scan();
		}
		else
			parseError("expecting '" + expectedTokenKind +
					"' but found '" + token.kind + "'");
	}

	/**
	 * report parse error and unwind call stack to start of parse
	 * @param e  string with error detail
	 * @throws SyntaxError
	 */
	private void parseError(String e) throws SyntaxError {
		reporter.reportError("Parse error: " + e);
		throw new SyntaxError();
	}

	// show parse stack whenever terminal is  accepted
	private void pTrace() {
		StackTraceElement [] stl = Thread.currentThread().getStackTrace();
		for (int i = stl.length - 1; i > 0 ; i--) {
			if(stl[i].toString().contains("parse"))
				System.out.println(stl[i]);
		}
		System.out.println("accepting: " + token.kind + " (\"" + token.spelling + "\")");
		System.out.println();
	}

}

