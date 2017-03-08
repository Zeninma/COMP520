/**
 *  Scan the a single line of input
 *
 *  Grammar:
 *   num ::= digit digit*
 *   digit ::= '0' | ... | '9'
 *   oper ::= '+' | '*'
 *   
 *   whitespace is the space character
 */
package miniJava.SyntacticAnalyzer;

import java.io.*;
import miniJava.ErrorReporter;

public class Scanner{

	private InputStream inputStream;
	private ErrorReporter reporter;

	private char currentChar;
	private StringBuilder currentSpelling;
	
	// true when end of line is found
	private boolean eot = false; 

	public Scanner(InputStream inputStream, ErrorReporter reporter) {
		this.inputStream = inputStream;
		this.reporter = reporter;

		// initialize scanner state
		readChar();
	}

	/**
	 * skip whitespace and scan next token
	 */
	public Token scan() {

		// skip whitespace
		while (!eot && (currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r')){
			commenSkip();
		}
		while(currentChar == '/'&&!eot){
			skipIt();
			//single Line comment
			if(currentChar == '/'){
				skipIt();
				while((currentChar != '\n' && currentChar != '\r')&&!eot){
					commenSkip();
				}
				commenSkip();
			}
			//multi-line comment
			else if(currentChar == '*'){
				commenSkip();
				while(! eot ){
					if(currentChar == '*'){
						commenSkip();
						if(currentChar == '/'){
							commenSkip();
							break;
						}
					}
					else{
						commenSkip();
					}
				}
				if(eot){
					return new Token(TokenKind.ERROR, "Expecting end of comment, but not found");
				}
			}
			else{
				return new Token(TokenKind.DIVIDE, "/");
			}
			while (!eot && (currentChar == ' ' || currentChar == '\t' || currentChar == '\n' || currentChar == '\r')){
				commenSkip();
			}
		}
		// start of a token: collect spelling and identify token kind
		currentSpelling = new StringBuilder();
		TokenKind kind = scanToken();
		String spelling = currentSpelling.toString();
		return new Token(kind, spelling);
	}

	/**
	 * determine token kind
	 */
	public TokenKind scanToken() {
		
		if (eot)
			return(TokenKind.EOT); 

		// scan Token, key word has the first priority
		switch (currentChar) {
		case '\n':
			takeIt();
			return(TokenKind.NEWLINE);
		case '\r':
			takeIt();
			return(TokenKind.CARRIAGERETURN);
		case '{':
			takeIt();
			return (TokenKind.LEFTCBRACKET);
			
		case '}':
			takeIt();
			return(TokenKind.RIGHTCBRACKET);
			
		case '[':
			takeIt();
			return(TokenKind.LEFTSBRACKET);
			
		case ']':
			takeIt();
			return(TokenKind.RIGHTSBRACKET);
		
		case ';':
			takeIt();
			return(TokenKind.SEMICOL);
			
		case ',':
			takeIt();
			return(TokenKind.COMMA);
		
		case '(': 
			takeIt();
			return(TokenKind.LPAREN);

		case ')':
			takeIt();
			return(TokenKind.RPAREN);
		//Scan Token for operators,
		// 13 in total
		case '+':
			takeIt();
			return(TokenKind.PLUS);
		
		case '.':
			takeIt();
			return(TokenKind.PERIOD);
			
		case '*':
			takeIt();
			return(TokenKind.TIMES);
//			if(currentChar == '/'){
//				takeIt();
//				return(TokenKind.COMMENTEND);
//			}
//			else{
//				return(TokenKind.TIMES);
//			}

		
		case '/':
			takeIt();
			return(TokenKind.DIVIDE);
		
		case '>':
			takeIt();
			if(currentChar == '='){
				takeIt();
				return(TokenKind.GEQ);
			}
			else{
				return(TokenKind.GT);
			}
		
		case '<':
			takeIt();
			if(currentChar == '='){		
				takeIt();
				return(TokenKind.LEQ);
			}
			else{
				return(TokenKind.LT);
			}
			
		case '=':
			takeIt();
			if(currentChar == '='){
				takeIt();
				return(TokenKind.EQUAL);
			}
			else{
				return(TokenKind.ASSIGN);
			}
			
		case '!':
			takeIt();
			if(currentChar == '='){
				takeIt();
				return(TokenKind.NEQ);
			}
			else{
				return(TokenKind.NOT);
			}
		
		case '&':
			takeIt();
			if(currentChar == '&'){
				takeIt();
				return(TokenKind.AND);
			}
			else{
				scanError("Unrecognized token '" + '&' + currentChar + "' in input");
				return(TokenKind.ERROR);
			}
			
		case '|':
			takeIt();
			if(currentChar == '|'){
				takeIt();
				return(TokenKind.OR);
			}
			else{
				scanError("Unrecognized token '" + '|' + currentChar + "' in input");
				return(TokenKind.ERROR);
			}
			
		case '-':
			takeIt();
			return(TokenKind.MINUS);
		//scan token for ID and ALPH KEYWORD
		case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g':
		case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n':
		case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u':
		case 'v': case 'w': case 'x': case 'y': case 'z':
		case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G':
		case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N':
		case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U':
		case 'V': case 'W': case 'X': case 'Y': case 'Z':
			while (isAlph(currentChar) || isDigit(currentChar) || (currentChar == '_')){
				takeIt();
			}
			switch(currentSpelling.toString()){
				case "class": return(TokenKind.CLASS);	case "void": return(TokenKind.VOID);
				case "public": return(TokenKind.PUBLIC);	case "private": return(TokenKind.PRIVATE);
				case "static": return(TokenKind.STATIC);	case "int": return(TokenKind.INT);
				case "boolean":return(TokenKind.BOOL);	case "this": return(TokenKind.THIS);
				case "return": return(TokenKind.RETURN);	case "if": return(TokenKind.IF);
				case "else": return(TokenKind.ELSE);	case "while": return(TokenKind.WHILE);
				case "true": return(TokenKind.TRUE);	case "false": return(TokenKind.FALSE);
				case "new":return(TokenKind.NEW);
				
			}
			return(TokenKind.ID);
		
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			while (isDigit(currentChar))
				takeIt();
			return(TokenKind.NUM);

		default:
			scanError("Unrecognized character '" + currentChar + "' in input");
			return(TokenKind.ERROR);
		}
	}

	private void takeIt() {
		currentSpelling.append(currentChar);
		nextChar();
	}

	private void skipIt() {
		nextChar();
	}

	private boolean isDigit(char c) {
		return (c >= '0') && (c <= '9');
	}

	private boolean isAlph(char c){
		boolean is_cap = (c >= 'A') && (c <= 'Z');
		boolean is_small = (c>= 'a') && (c <= 'z');
		return (is_cap || is_small);
	}
	
	private void scanError(String m) {
		reporter.reportError("Scan Error:  " + m);
	}


	private final static char eolUnix = '\n';
	private final static char eolWindows = '\r';

	/**
	 * advance to next char in inputstream
	 * detect end of file or end of line as end of input
	 */
	private void nextChar() {
		if (!eot)
			readChar();
	}

	private void readChar() {
		try {
			int c = inputStream.read();
			currentChar = (char) c;
			if (c == -1) {
				eot = true;
			}
		} catch (IOException e) {
			scanError("I/O Exception!");
			eot = true;
		}
	}
	
	private void commenSkip(){
		try {
			int c = inputStream.read();
			currentChar = (char) c;
			if (c == -1) {
				eot = true;
			}
		} catch (IOException e) {
			scanError("I/O Exception!");
			eot = true;
		}
	}
	
//	private char nextNoUpdate(){
//			  inputStream.mark(5);
//			  char c;
//			  try {
//			   c = (char)inputStream.read();
//			   inputStream.reset();
//			   System.out.println("nextNoUpdate returns" + c);
//			   return c;
//			  } catch (IOException e) {
//				  System.out.println("nextNoUpdate returns" + '~');
//			   return '~';
//			  }
//	}
}
