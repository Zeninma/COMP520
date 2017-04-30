package miniJava.SyntacticAnalyzer;
/**
 *   TokenKind is a simple enumeration of the different kinds of tokens
 *   where ASSIGN is for '=', GT for '>', LT for '<', GET for '>=', LET for '<='
 *   COMMENTSTART for '/*', COMMENTEND for '\*\/'
 *   
 */
// added null into TokenKind
public enum TokenKind {CLASS, VOID, PUBLIC, PRIVATE, STATIC, SEMICOL, NUM, BOOL, LEFTSBRACKET, RIGHTSBRACKET, INT,
	LEFTCBRACKET, RIGHTCBRACKET, LPAREN, RPAREN, THIS, RETURN, IF, ELSE, WHILE, TRUE, FALSE, COMMA, PERIOD, NEW, 
	LT, GT, EQUAL, LEQ, GEQ, NEQ, AND, OR, NOT, PLUS, MINUS, TIMES, DIVIDE, ASSIGN
	, ERROR, EOT, ID, COMMENTSTART, COMMENTEND, LCOMMENT, NEWLINE, CARRIAGERETURN, NULL}
