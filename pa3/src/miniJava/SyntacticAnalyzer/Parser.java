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
import miniJava.AbstractSyntaxTrees.Package;

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
	public Package parse() {
		token = scanner.scan();
		try {
			return parsePackage();
		}
		catch (SyntaxError e) { 
			System.exit(4);
			return null;
		}
	}

	//    Program ::= (ClassDeclaration)* eot$
	private Package parsePackage() throws SyntaxError {
		Package pack;
		ClassDeclList cl = new ClassDeclList();
		while(token.kind == TokenKind.CLASS){
			cl.add(parseClass());
		}
		accept(TokenKind.EOT);
		pack = new Package(cl,null);
		return pack;
	}
	
	//ClassDeclaration ::=class id { FieldMethodDeclaration* }
	private ClassDecl parseClass() throws SyntaxError{
		FieldDeclList fdl = new FieldDeclList();
		MethodDeclList mdl = new MethodDeclList();
		accept(TokenKind.CLASS);
		// get class name
		String cn = token.spelling;
		accept(TokenKind.ID);
		accept(TokenKind.LEFTCBRACKET);
		while(token.kind != TokenKind.RIGHTCBRACKET){
			MemberDecl field_method_decl = parseFieldMethodDeclaration();
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
	private MemberDecl parseFieldMethodDeclaration() throws SyntaxError{
		boolean isPrivate = false;
		boolean isStatic = false;
		TypeDenoter t = null;
		String name;
		MemberDecl md;
		ParameterDeclList pl = new ParameterDeclList();
		StatementList sl = new StatementList();
		SourcePosition posn = null;
		
		if(token.kind == TokenKind.PUBLIC ){
			acceptIt();
			isPrivate = false;
		}
		else if(token.kind == TokenKind.PRIVATE){
			acceptIt();
			isPrivate = true;
		}
		if(token.kind == TokenKind.STATIC){
			acceptIt();
			isStatic = true;
		}
		//parse Type
		switch (token.kind){
		case INT:
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				accept(TokenKind.RIGHTSBRACKET);
				TypeDenoter temp_t = new BaseType(TypeKind.INT, posn);
				t = new ArrayType(temp_t, null);
			}
			else{
				t = new BaseType(TypeKind.INT, posn);
			}
			break;
			
		case ID:
			Identifier tmp_id = new Identifier(token);
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				accept(TokenKind.RIGHTSBRACKET);
				t = new ClassType(tmp_id ,posn);
				t = new ArrayType(t, null); //previously forgot addinh Array type here, even I am considering for 'id[]'
			}
			else{
				t = new ClassType(tmp_id, posn);
			}
			break;
			
		case BOOL:
			t = new BaseType(TypeKind.BOOLEAN, posn);
			acceptIt();
			break;
			
		case VOID:
			// void indicates this is a method declaration
			t = new BaseType(TypeKind.VOID, posn);
			acceptIt();
			name = token.spelling;
			accept(TokenKind.ID);
			MemberDecl member_decl = new FieldDecl(isPrivate, isStatic, t, name, posn);
			pl = new ParameterDeclList();
			accept(TokenKind.LPAREN);
			if(token.kind != TokenKind.RPAREN){
				pl = parseParameterList();
			}
			accept(TokenKind.RPAREN);
			accept(TokenKind.LEFTCBRACKET);
			while(token.kind != TokenKind.RIGHTCBRACKET){
				sl.add(parseStatement());
			}
			accept(TokenKind.RIGHTCBRACKET);
			MethodDecl method_decl = new MethodDecl(member_decl, pl, sl, posn);
			return method_decl;
		default:
			parseError("Invalid Term - expecting Type or Void but found" + token.kind);
			break;
		}
		name = token.spelling;
		accept(TokenKind.ID);
		MemberDecl member_decl = new FieldDecl(isPrivate, isStatic, t, name, posn);
		switch(token.kind){
		//LPAREN indicates is a method_decl
		case LPAREN:
			acceptIt();
			if(token.kind != TokenKind.RPAREN){
				pl = parseParameterList();
			}
			accept(TokenKind.RPAREN);
			accept(TokenKind.LEFTCBRACKET);
			while(token.kind != TokenKind.RIGHTCBRACKET){
				sl.add(parseStatement());
			}
			accept(TokenKind.RIGHTCBRACKET);
			MethodDecl method_decl = new MethodDecl(member_decl, pl, sl, posn);
			return method_decl;
		// SEMICOL indicates this is a Field decl
		case SEMICOL:
			acceptIt();
			return member_decl;
		default:
			// may need to return ERROR
			parseError("Invalid Term - expecting LPAREN or SEMICOL but found" + token.kind);
			break;
		}
		System.exit(4);
		return null; // means error
	}
	
	
	//Statement ::= (...|...|...)
	private Statement parseStatement() throws SyntaxError{
		SourcePosition posn = null;
		Statement statement;
		TypeDenoter type;
		VarDecl vd;
		String name;
		Expression expr;
		Identifier cn;
		Reference ref;
		ExprList el;
		switch (token.kind){
		// BlockStmt
		case LEFTCBRACKET:
			acceptIt();
			StatementList sl = new StatementList();
			while(token.kind != TokenKind.RIGHTCBRACKET){
				sl.add(parseStatement());
			}
			accept(TokenKind.RIGHTCBRACKET);
			statement = new BlockStmt(sl, posn);
			return statement;
		//VarDeclStmt
		case INT: case BOOL:
			type = parseType();
			name = token.spelling;
			accept(TokenKind.ID);
			vd = new VarDecl(type, name, null);
			accept(TokenKind.ASSIGN);
			expr = parseExpression();
			accept(TokenKind.SEMICOL);
			statement = new VarDeclStmt(vd, expr, null);
			return statement;
		/*
		 * possible case: 
		 * Var DeclStmt: Type id = Expression;
		 * AssignStmt:   Reference = Expression;
		 * CallStmt:     Reference (ArgumentList?);
		 */
		case ID:
			Token reserved = token;
			acceptIt();
			switch(token.kind){
			// Var DeclStmt: Type id = Expression;
			case ID:
				cn = new Identifier(reserved);
				type = new ClassType(cn, null);
				name = token.spelling;
				acceptIt();
				accept(TokenKind.ASSIGN);
				expr = parseExpression();
				accept(TokenKind.SEMICOL);
				vd = new VarDecl(type, name, null);
				statement = new VarDeclStmt(vd, expr, null);
				return statement;
			/* Var DeclStmt: Type id = Expression;
			* AssignStmt:   Reference = Expression;
			* CallStmt:     Reference (ArgumentList?);
			*/
			case LEFTSBRACKET:
				acceptIt();
				switch(token.kind){
					// Reference
					//for expression
					case ID: case NOT: case MINUS: case LPAREN:
					case NUM: case TRUE: case FALSE: case NEW:
						cn = new Identifier(reserved);
						expr = parseExpression();
						accept(TokenKind.RIGHTSBRACKET);
						ref = new IxIdRef(cn, expr, null);
						while(token.kind == TokenKind.PERIOD){
							acceptIt();
							cn = new Identifier(token);
							accept(TokenKind.ID);
							if(token.kind == TokenKind.LEFTSBRACKET){
								acceptIt();
								expr = parseExpression();
								accept(TokenKind.RIGHTSBRACKET);
								ref = new IxQRef(ref ,cn , expr, null);
							}
							else{
								ref = new QRef(ref, cn, null);
							}
						}
						switch(token.kind){
						// AssignStmt
						case ASSIGN:
							acceptIt();
							expr = parseExpression();
							accept(TokenKind.SEMICOL);
							statement = new AssignStmt(ref, expr, null);
							return statement;
						// CallStmt
						case LPAREN:
							acceptIt();
							if(token.kind !=TokenKind.RPAREN){
								el = parseArgumentList();
							}
							else{
								el = new ExprList();
							}
							accept(TokenKind.RPAREN);
							accept(TokenKind.SEMICOL);
							statement = new CallStmt(ref, el, null);
							return statement;
						default:
							parseError("Invalid Term - expecting LPAREN or ASSIGN but found " + token.kind);
							System.exit(4);
							return null; // error
						}
					// for Type, VarDeclStmt: Type id = Expression;
					case RIGHTSBRACKET:
						cn = new Identifier(reserved);
						TypeDenoter array_type = new ClassType(cn, null);
						type = new ArrayType(array_type, null);
						acceptIt();
						name = token.spelling;
						vd = new VarDecl(type, name, null);
						accept(TokenKind.ID);
						accept(TokenKind.ASSIGN);
						expr = parseExpression();
						accept(TokenKind.SEMICOL);
						statement = new VarDeclStmt(vd, expr, null);
						return statement;
					default:
						parseError("Invalid Term - expecting ID, NOT,MINUS, LPAREN, NUM, TRUE"
								+ "FALSE, NEW or RIGHTSBRACKET but found " + token.kind);
						System.exit(0);
						return null;// error
				}
			//Reference
			case PERIOD:
				cn = new Identifier(reserved);
				ref = new IdRef(cn, null);
				while(token.kind == TokenKind.PERIOD){
					acceptIt();
					cn = new Identifier(token);
					accept(TokenKind.ID);
					//last changed
					if(token.kind == TokenKind.LEFTSBRACKET){
						acceptIt();
						expr = parseExpression();
						accept(TokenKind.RIGHTSBRACKET);
						ref = new IxQRef(ref, cn, expr, null);
					}
					else{
						ref = new QRef(ref, cn, null);
					}
				}
				switch(token.kind){
					//AssignStmt
					case ASSIGN:
						acceptIt();
						expr = parseExpression();
						accept(TokenKind.SEMICOL);
						statement = new AssignStmt(ref, expr, null);
						return statement;
					//CallStmt
					case LPAREN:
						acceptIt();
						if(token.kind !=TokenKind.RPAREN){
							el = parseArgumentList();
						}
						else{
							el = new ExprList();
						}
						accept(TokenKind.RPAREN);
						accept(TokenKind.SEMICOL);
						statement = new CallStmt(ref, el, null);
						return statement;
					default:
						parseError("Invalid Term - expecting LPAREN or ASSIGN but found " + token.kind);
						System.exit(4);
						return null; // ERROR
					}
			// AssignStmt
			case ASSIGN:
				cn = new Identifier(reserved);
				ref = new IdRef(cn, null);
				acceptIt();
				expr = parseExpression();
				accept(TokenKind.SEMICOL);
				statement = new AssignStmt(ref, expr, null);
				return statement;
				
			case LPAREN:
				cn = new Identifier(reserved);
				ref = new IdRef(cn, null);
				acceptIt();
				if(token.kind !=TokenKind.RPAREN){
					el = parseArgumentList();
				}
				else{
					el = new ExprList();
				}
				accept(TokenKind.RPAREN);
				accept(TokenKind.SEMICOL);
				statement = new CallStmt(ref, el, null);
				return statement;
				
			default:
				parseError("Invalid Term - expecting ID, LEFTSBRACKET, PERIOD, ASSIG or LPAREN but found "+ token.kind);
				System.exit(4);
				return null; //ERROR
			}

		//Reference
		case THIS:
			ref = parseReference();
			switch(token.kind){
			case ASSIGN:
				acceptIt();
				expr = parseExpression();
				accept(TokenKind.SEMICOL);
				statement =new AssignStmt(ref, expr, null);
				return statement;
			
			case LPAREN:
				acceptIt();
				if(token.kind != TokenKind.RPAREN){
					el = parseArgumentList();
				}
				else{
					el = new ExprList();
				}
				accept(TokenKind.RPAREN);
				accept(TokenKind.SEMICOL);
				statement = new CallStmt(ref, el, null);
				return statement;
				
			default:
				parseError("Invalid Term - expecting Assign or LPAREN but found " + token.kind);
				System.exit(4);
				return null; //Error
			}
		case RETURN:
			acceptIt();
			if(token.kind != TokenKind.SEMICOL){
				expr = parseExpression();
				statement = new ReturnStmt(expr, null);
			}
			else{
				statement = new ReturnStmt(null,null);
			}
			accept(TokenKind.SEMICOL);
			// Need To Check How to Deal With Return
			return statement;
		//IfStmt
		case IF:
			acceptIt();
			accept(TokenKind.LPAREN);
			expr = parseExpression();
			accept(TokenKind.RPAREN);
			Statement if_stmt = parseStatement();
			if(token.kind == TokenKind.ELSE){
				acceptIt();
				Statement else_stmt = parseStatement();
				statement = new IfStmt(expr, if_stmt, else_stmt, null);
				return statement;
			}
			else{
				statement = new IfStmt(expr, if_stmt, null);
				return statement;
			}
		//WhileStmt
		case WHILE:
			acceptIt();
			accept(TokenKind.LPAREN);
			expr = parseExpression();
			accept(TokenKind.RPAREN);
			Statement do_while_stmt = parseStatement();
			statement = new WhileStmt(expr, do_while_stmt, null);
			return statement;
			
		default:
			parseError("Invalid Term - expecting LEFTCBRACKET or ID, BOOL, INT, THIS, RETURN, IF OR WHILE but found " + token.kind);
			System.exit(4);
			return null; // Error
		}
	}
	
	//ArgumentList ::= Expression (, Expression)*
	private ExprList parseArgumentList() throws SyntaxError{
		ExprList el = new ExprList();
		Expression expr = parseExpression();
		el.add(expr);
		while(token.kind == TokenKind.COMMA){
			acceptIt();
			el.add(parseExpression());
		}
		return el;
	}
	
	
	// Deal with Precedence, building AST tree
	private Expression parseExpression() throws SyntaxError{
		Expression expr = parseDisjunction();
		return expr;
	}
	
	private Expression parseDisjunction() throws SyntaxError{
		Expression expr = parseConjunction();
		Operator op;
		while(token.kind == TokenKind.OR){
			op = new Operator(token);
			acceptIt();
			Expression expr_right = parseConjunction();
			expr = new BinaryExpr(op, expr, expr_right, null);
		}
		return expr;
	}
	
	private Expression parseConjunction() throws SyntaxError {
		Expression expr = parseEquality();
		Operator op;
		while (token.kind == TokenKind.AND){
			op = new Operator(token);
			acceptIt();
			Expression expr_right = parseEquality();
			expr = new BinaryExpr(op, expr, expr_right, null);
		}
		return expr;
	}
	
	private Expression parseEquality() throws SyntaxError {
		Expression expr = parseRelational();
		Operator op;
		while (token.kind == TokenKind.EQUAL || token.kind == TokenKind.NEQ){
			op = new Operator(token);
			acceptIt();
			Expression expr_right = parseRelational();
			expr = new BinaryExpr(op, expr, expr_right, null);
		}
		return expr;
	}
	
	private Expression parseRelational() throws SyntaxError{
		Expression expr = parseAdditive();
		Operator op;
		while (token.kind == TokenKind.GT || token.kind == TokenKind.LT
				|| token.kind == TokenKind.GEQ || token.kind == TokenKind.LEQ){
			op = new Operator(token);
			acceptIt();
			Expression expr_right = parseAdditive();
			expr = new BinaryExpr(op, expr, expr_right, null);
		}
		return expr;
	}
	
	private Expression parseAdditive() throws SyntaxError {
		Expression expr = parseMultiplicative();
		Operator op;
		while (token.kind == TokenKind.PLUS || token.kind == TokenKind.MINUS){
			op=new Operator(token);
			acceptIt();
			Expression expr_right = parseMultiplicative();
			expr = new BinaryExpr(op, expr, expr_right, null);
		}
		return expr;
	}
	
	private Expression parseMultiplicative() throws SyntaxError {
		Expression expr = parseUnary();
		Operator op;
		while (token.kind == TokenKind.TIMES || token.kind == TokenKind.DIVIDE){
			op = new Operator(token);
			acceptIt();
			Expression expr_right = parseUnary();
			expr = new BinaryExpr(op, expr, expr_right, null);
		}
		return expr;
	}
	
	private Expression parseUnary() throws SyntaxError {
		Expression expr;
		Operator op;
		if (token.kind == TokenKind.NOT || token.kind == TokenKind.MINUS){
			op = new Operator(token);
			acceptIt();
			Expression expr_right = parseUnary();
			expr = new UnaryExpr(op, expr_right, null);
		} else {
			expr = parseParen();
		}
		return expr;
	}
	
	private Expression parseParen() throws SyntaxError {
		Expression expr;
		if (token.kind == TokenKind.LPAREN){
			acceptIt();
			expr = parseExpression();
			accept(TokenKind.RPAREN);
		} else {
			expr = parseOperand();
		}
		return expr;
	}
	
	private Expression parseOperand() throws SyntaxError {
		Expression expr;
		Terminal term;
		Reference ref;
		Identifier cn;
		switch (token.kind) {
		case ID:
			/* Previously, redundant parse on the reference part,
			 * and made errors on parsing reference here.
			 * Now replace this redundant code with the well formed
			 * parseReference method
			*/
			ref = parseReference();
			if(token.kind == TokenKind.LPAREN){
				acceptIt();
				ExprList el = new ExprList();
				if (token.kind != TokenKind.RPAREN){
					el = parseArgumentList();
				}
				accept(TokenKind.RPAREN);
				expr = new CallExpr(ref, el, null);
			}
			else{
				expr = new RefExpr(ref, null);
			}
			return expr;
		
		case THIS:
			ref = new ThisRef(null);	
			acceptIt();
			if(token.kind == TokenKind.PERIOD){
				while(token.kind == TokenKind.PERIOD){
					acceptIt();
					cn = new Identifier(token);
					accept(TokenKind.ID);
					//last changed
					if(token.kind == TokenKind.LEFTSBRACKET){
						acceptIt();
						expr = parseExpression();
						accept(TokenKind.RIGHTSBRACKET);
						ref = new IxQRef(ref, cn, expr, null);
					}
					else{
						ref = new QRef(ref, cn, null);
					}
				}
			}
			//keep parsing after period
			if(token.kind == TokenKind.LPAREN){
				acceptIt();
				ExprList el = new ExprList();
				if(token.kind != TokenKind.RPAREN){
					el = parseArgumentList();
				}
				accept(TokenKind.RPAREN);
				expr = new CallExpr(ref, el, null);
			} else {
				expr = new RefExpr(ref, null);
			}
			return expr;

		case NUM:
			term = new IntLiteral(token);
			expr = new LiteralExpr(term,null);
			acceptIt();
			return expr;
			
		case TRUE: case FALSE:
			term = new BooleanLiteral(token);
			expr = new LiteralExpr(term,null);
			acceptIt();
			return expr;
			
		case NEW:
			Expression temp_expr;
			TypeDenoter type;
			acceptIt();
			if(token.kind == TokenKind.INT){
				type = new BaseType(TypeKind.INT, null);
				acceptIt();
				accept(TokenKind.LEFTSBRACKET);
				temp_expr = parseExpression();
				accept(TokenKind.RIGHTSBRACKET);
				expr = new NewArrayExpr(type, temp_expr, null);
			}
			else{
				cn = new Identifier(token);
				type = new ClassType(cn, null);
				accept(TokenKind.ID);
				if(token.kind == TokenKind.LPAREN){
					acceptIt();
					accept(TokenKind.RPAREN);
					expr = new NewObjectExpr((ClassType) type, null);
				}
				else {
					accept(TokenKind.LEFTSBRACKET);
					temp_expr = parseExpression();
					accept(TokenKind.RIGHTSBRACKET);
					expr = new NewArrayExpr(type, temp_expr, null);
				}
			}
			return expr;
			
		default:
			parseError("Invalid Term - Expecting an expression leaf but found " + token.kind);
			System.exit(4);
			return null; // ERROR
		}
	}
	
	//ReferenceSkipId::= ([Expression])? ( . id ([Expression])?)*

	//Reference::= ( id ([Expression])? | this ) ( . id ([Expression])?)*
	private Reference parseReference() throws SyntaxError{
		Reference ref;
		Identifier id;
		Expression expr;
		switch(token.kind){
		case ID:
			id = new Identifier(token); 
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				expr = parseExpression();
				accept(TokenKind.RIGHTSBRACKET);
				ref = new IxIdRef(id, expr, null);
			}
			else{
				ref = new IdRef(id, null);
			}
			while(token.kind == TokenKind.PERIOD){
				acceptIt();
				id = new Identifier(token);
				accept(TokenKind.ID);
				if(token.kind == TokenKind.LEFTSBRACKET){
					acceptIt();
					expr = parseExpression();
					accept(TokenKind.RIGHTSBRACKET);
					ref = new IxQRef(ref, id, expr, null);
				}
				else{
					ref = new QRef(ref, id, null);
				}
			}
			return ref;
			
		case THIS:
			ref = new ThisRef(null);
			acceptIt();
			while(token.kind == TokenKind.PERIOD){
				acceptIt();
				id = new Identifier(token);
				accept(TokenKind.ID);
				if(token.kind == TokenKind.LEFTSBRACKET){
					acceptIt();
					expr = parseExpression();
					accept(TokenKind.RIGHTSBRACKET);
					ref = new IxQRef(ref, id, expr, null);
				}
				else{
					ref = new QRef(ref, id, null);
				}
			}
			return ref;
			
		default:
			parseError("Invalid Term - expecting ID or THIS but found " + token.kind);
			System.exit(4);
			return null; //ERROR
		}
	}
	
	//ParameterList ::= Type id (, Type id)*
	private ParameterDeclList parseParameterList() throws SyntaxError{
		ParameterDeclList pl = new ParameterDeclList();
		TypeDenoter type = parseType();
		String name = token.spelling; // Previously mistaken on the order for reading the parameter name
		ParameterDecl p_dcl = new ParameterDecl(type, name, null);
		pl.add(p_dcl);
		accept(TokenKind.ID);
		while(token.kind == TokenKind.COMMA){
			acceptIt();
			type = parseType();
			name = token.spelling;// Previously mistaken on the order for reading the parameter name
			p_dcl = new ParameterDecl(type, name, null);
			pl.add(p_dcl);
			accept(TokenKind.ID);
		}
		return pl;
	}
	
	//Type ::= int | boolean | id | (int | id) []
	private TypeDenoter parseType() throws SyntaxError{
		TypeDenoter temp_type = null;
		TypeDenoter final_type = null;
		switch (token.kind){
		case INT:
			temp_type = new BaseType(TypeKind.INT, null);
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				accept(TokenKind.RIGHTSBRACKET);
				final_type = new ArrayType(temp_type, null);
				return final_type;
			}
			return temp_type;
			
		case ID:
			Identifier id = new Identifier(token);
			temp_type = new ClassType(id, null);
			acceptIt();
			if(token.kind == TokenKind.LEFTSBRACKET){
				acceptIt();
				accept(TokenKind.RIGHTSBRACKET);
				final_type = new ArrayType(temp_type, null);
				return final_type;//previously forgot to return the final type as above
			}
			return temp_type;
			
		case BOOL:
			acceptIt();
			temp_type = new BaseType(TypeKind.BOOLEAN, null);
			return temp_type;
			
		default:
			parseError("Invalid Term - expecting Type or Void but found" + token.kind);
			System.exit(4);
			return null; // error
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

