package miniJava.ContextualAnalyzer;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassDeclList;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.ExprList;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxIdRef;
import miniJava.AbstractSyntaxTrees.IxQRef;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.NullLiteral;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;

public class TypeCheckVisitor implements Visitor<Object, TypeDenoter>{
	public ErrorReporter error_reporter;
	public BaseType intType = new BaseType(TypeKind.INT, null);
	public BaseType errorType = new BaseType(TypeKind.ERROR, null);
	public BaseType unsupportedType = new BaseType(TypeKind.UNSUPPORTED, null);
	public BaseType boolType = new BaseType(TypeKind.BOOLEAN, null);
	public boolean ismethod = false;
	
	public boolean isEqual(TypeDenoter type1, TypeDenoter type2) {
		if (type1.typeKind == TypeKind.CLASS
				&& type2.typeKind == TypeKind.CLASS) {
			return ((ClassType) type1).className.spelling
					.equals(((ClassType) type2).className.spelling);
		}
		else if ((type1.typeKind == TypeKind.CLASS && type2.typeKind == TypeKind.NULL)
				|| (type1.typeKind == TypeKind.NULL && type2.typeKind == TypeKind.CLASS)
				|| (type1.typeKind == TypeKind.NULL && type2.typeKind == TypeKind.NULL)
				|| (type1.typeKind == TypeKind.ERROR || type2.typeKind == TypeKind.ERROR)) {
			return true;
		} 
		else if(type1.typeKind == TypeKind.UNSUPPORTED || type2.typeKind == TypeKind.UNSUPPORTED){
			return false;
		}
		else{
			return type1.typeKind == type2.typeKind;
		}
	}
	
	public void check(AST ast, ErrorReporter reporter){
		this.error_reporter = reporter;
		System.out.println("***************************** Type Checking started");
		ast.visit(this, null);
	}
	
	@Override
	public TypeDenoter visitPackage(Package prog, Object arg) {
		ClassDeclList cl = prog.classDeclList;
		for (ClassDecl classDecl : cl) {
			classDecl.visit(this, null);
		}
		return null;

	}

	@Override
	public TypeDenoter visitClassDecl(ClassDecl cd, Object arg) {
		FieldDeclList fl = cd.fieldDeclList;
		for (FieldDecl field : fl){
			field.visit(this, null);
		}
		MethodDeclList ml = cd.methodDeclList;
		for (MethodDecl method : ml){
			method.visit(this, null);
		}
		return cd.type;
	}

	@Override
	public TypeDenoter visitFieldDecl(FieldDecl fd, Object arg) {
		fd.type.visit(this, null);
		return fd.type;
	}

	@Override
	public TypeDenoter visitMethodDecl(MethodDecl md, Object arg) {
		md.type.visit(this, null);
		ParameterDeclList pdl = md.parameterDeclList;
		for (ParameterDecl pd : pdl) {
			pd.visit(this, null);
		}
		StatementList sl = md.statementList;
		for (Statement statement : sl) {
			statement.visit(this, null);
		}
		return md.type;
	}

	@Override
	public TypeDenoter visitParameterDecl(ParameterDecl pd, Object arg) {
		pd.type.visit(this,null);
		return pd.type;
	}

	@Override
	public TypeDenoter visitVarDecl(VarDecl decl, Object arg) {
		decl.type.visit(this, null);
		return decl.type;
	}

	@Override
	public TypeDenoter visitBaseType(BaseType type, Object arg) {
		return type;
	}

	@Override
	public TypeDenoter visitClassType(ClassType type, Object arg) {
		return type;
	}

	@Override
	public TypeDenoter visitArrayType(ArrayType type, Object arg) {
		return type.eltType.visit(this, arg);
	}

	@Override
	public TypeDenoter visitBlockStmt(BlockStmt stmt, Object arg) {
		StatementList statementList = stmt.sl;
		for(Statement statement: statementList){
			statement.visit(this, null);
		}
		return null;
	}

	@Override
	public TypeDenoter visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		TypeDenoter varDeclType = stmt.varDecl.visit(this, arg);
		TypeDenoter exprType = stmt.initExp.visit(this, arg);
		if(!isEqual(varDeclType, exprType)){
			this.error_reporter.reportError("*** Unmatched type. Var has type: " + varDeclType.typeKind.toString()
			+ ", but the expression has the type: " + exprType.typeKind.toString());
		}
		return null;
	}

	@Override
	public TypeDenoter visitAssignStmt(AssignStmt stmt, Object arg) {
		TypeDenoter refType = stmt.ref.visit(this, null);
		TypeDenoter exprType = stmt.val.visit(this, null);
		if(!this.isEqual(refType, exprType)){
			this.error_reporter.reportError("*** Reference has the type: " + refType.typeKind.toString()
			+ ", but the Expression has type: " + exprType.typeKind.toString());
		}
		return null;
	}

	@Override
	public TypeDenoter visitCallStmt(CallStmt stmt, Object arg) {
		stmt.methodRef.visit(this, null);
		ParameterDeclList pdl;
		//stmt.methodRef must be an IdRef or a qualifiedRef
		if(stmt.methodRef instanceof IdRef){
			pdl = ((MethodDecl)((IdRef)stmt.methodRef).id.decl).parameterDeclList;
		}
		else{
			pdl = ((MethodDecl)((QRef)stmt.methodRef).decl).parameterDeclList;
		}
		ExprList al = stmt.argList;
		TypeDenoter eType;
		int i = 0;
		for (Expression e : al) {
			eType = e.visit(this, arg);
			if(!this.isEqual(eType, pdl.get(i).type)){
				this.error_reporter.reportError("*** Unmatched parameter, expected: "+
			pdl.get(i).type.typeKind.toString()
			+ ", but found" + eType.typeKind.toString());
				break;
			}
			i++;
		}
		return null;
	}

	@Override
	public TypeDenoter visitReturnStmt(ReturnStmt stmt, Object arg) {
		if (stmt.returnExpr != null){
		stmt.returnExpr.visit(this, null);
		}
		return null;
	}

	@Override
	public TypeDenoter visitIfStmt(IfStmt stmt, Object arg) {
		TypeDenoter conditionType = stmt.cond.visit(this, null);
		if(!this.isEqual(conditionType, new BaseType(TypeKind.BOOLEAN, null))){
			this.error_reporter.reportError("*** Condition is expected to have type: bool, but found type: "
		+ stmt.cond.visit(this, null).typeKind.toString());
		}
		else{
			stmt.thenStmt.visit(this, null);
			if(stmt.elseStmt != null){
				stmt.elseStmt.visit(this, null);
			}
		}
		return null;
	}

	@Override
	public TypeDenoter visitWhileStmt(WhileStmt stmt, Object arg) {
		TypeDenoter conditionType = stmt.cond.visit(this, null);
		if(!this.isEqual(conditionType, this.boolType)){
			this.error_reporter.reportError("*** Condition is expected to have type: bool, but found type: "
		+ stmt.cond.visit(this, null).typeKind.toString());
		}
		else{
			stmt.body.visit(this, null);
		}
		return null;
	}

	/*
	 * Start visit Expression,
	 * where all Expressions are expected to return TypeDenoter
	 */
	@Override
	public TypeDenoter visitUnaryExpr(UnaryExpr expr, Object arg) {
		TypeDenoter exprType = expr.expr.visit(this, null);
		if(this.isEqual(exprType, this.boolType)
				&& expr.operator.spelling.equals("!")){
			BaseType exprResultType = new BaseType(TypeKind.BOOLEAN, null);
			return exprResultType;
		}
		else if(this.isEqual(exprType, this.intType)
				&& expr.operator.spelling.equals("-")){
			BaseType exprResultType = new BaseType(TypeKind.INT, expr.posn);
			return exprResultType;
		}
		else if(expr.operator.spelling.equals("-")){
			this.error_reporter.reportError("*** Expected to see type int after -, but found "
					+ exprType.typeKind.toString());
			return this.errorType;
		}
		else {
			this.error_reporter.reportError("*** Expected to see type boolean after !, but found "
					+ exprType.typeKind.toString());
			return this.errorType;
		}
	}

	public TypeDenoter visitBinaryExpr(BinaryExpr expr, Object arg) {
		TypeDenoter leftType = (TypeDenoter) expr.left.visit(this, null);
		TypeDenoter rightType = (TypeDenoter) expr.right.visit(this, null);
		TypeDenoter expr_type;
		if (this.isEqual(leftType, this.intType)
				&& this.isEqual(leftType, rightType)
				&& (expr.operator.spelling.equals("+")
						|| expr.operator.spelling.equals("-")
						|| expr.operator.spelling.equals("*") || expr.operator.spelling
							.equals("/"))) {
			expr_type = new BaseType(TypeKind.INT, null);
			return expr_type;
		}
		else if (this.isEqual(leftType, this.intType)
				&& this.isEqual(leftType, rightType)
				&&(expr.operator.spelling.equals(">")
						|| expr.operator.spelling.equals("<")
						|| expr.operator.spelling.equals(">")
						|| expr.operator.spelling.equals("<=")
						|| expr.operator.spelling.equals(">=")
						|| expr.operator.spelling.equals("!=")
						)){
			return this.boolType;
		}
		else if (this.isEqual(leftType, this.boolType)
				&& this.isEqual(leftType, rightType)
				&& (expr.operator.spelling.equals("||") 
						|| expr.operator.spelling.equals("&&"))) {
			return this.boolType;
		}
		else if(this.isEqual(leftType, rightType)
				&& (expr.operator.spelling.equals("=="))
				){
			expr_type = new BaseType(TypeKind.BOOLEAN, null);
			return expr_type;
		}
		else{
			this.error_reporter.reportError("*** Unmatched type with operator "
			+ expr.operator.spelling
			+ ", where lefthand side is: " + leftType.typeKind.toString()
			+ ", and the righthand side is: " + rightType.typeKind.toString());
			expr_type = this.errorType;
			return expr_type;
		}
	}
	
	@Override
	public TypeDenoter visitRefExpr(RefExpr expr, Object arg) {
		TypeDenoter refType = (TypeDenoter) expr.ref.visit(this, null);
		return refType;
	}

	@Override
	public TypeDenoter visitCallExpr(CallExpr expr, Object arg) {
		this.ismethod = true;
		TypeDenoter resultType = (TypeDenoter) expr.functionRef.visit(this, null);
		this.ismethod = false;
		ExprList exprList = expr.argList;
		ParameterDeclList pdl;
		if(expr.functionRef instanceof IdRef){
			pdl = ((MethodDecl)((IdRef)expr.functionRef).id.decl).parameterDeclList;
		}
		else{
			pdl = ((MethodDecl)((QRef)expr.functionRef).decl).parameterDeclList;
		}
		int i = 0;
		boolean doesMatch = true;
		for(Expression e : exprList){
			TypeDenoter eType =(TypeDenoter) e.visit(this, null);
			TypeDenoter parameterType = (TypeDenoter) pdl.get(i).visit(this, null);
			if(!this.isEqual(eType, parameterType)){
				this.error_reporter.reportError("*** Parameters's type doesn not much, expected, but got");
				doesMatch = doesMatch && false;
			}
			else{
				doesMatch = doesMatch && true;
				i++;
			}
		}
		if(doesMatch){
			return resultType;
		}
		else{
			return this.errorType;
		}
	}

	@Override
	public TypeDenoter visitLiteralExpr(LiteralExpr expr, Object arg) {
		TypeDenoter litType = (TypeDenoter) expr.lit.visit(this, null);
		return litType;
	}

	@Override
	public TypeDenoter visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		return expr.classtype;
	}

	@Override
	public TypeDenoter visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		if(this.isEqual( expr.sizeExpr.visit(this, null), this.intType) ){;
			return new ArrayType(expr.eltType, null);
		}
		else{
			//highlighted fill the erro
			this.error_reporter.reportError("*** Expected IntType as index, but found");
			return this.errorType;
		}
	}
	
	/*
	 * Start visit Reference.
	 * All reference are expected to return typeDenoter
	 */
	@Override
	public TypeDenoter visitThisRef(ThisRef ref, Object arg) {
		return ref.decl.type;
	}

	@Override
	public TypeDenoter visitIdRef(IdRef ref, Object arg) {
			return ref.id.decl.type;
	}

	@Override
	public TypeDenoter visitIxIdRef(IxIdRef ref, Object arg) {
		// the Expr should return intType
		if (ref.id.decl.type instanceof ArrayType &&
				this.isEqual(this.intType, (TypeDenoter) ref.indexExpr.visit(this, null))){
			TypeDenoter type = ((ArrayType) ref.id.decl.type).eltType;
			return type;
		}
		else{
			// Highlighted, need to fill the message
			this.error_reporter.reportError("*** Expected ArrayType followed by int, but found "
					);
			return this.errorType;
		}
	}

	@Override
	public TypeDenoter visitQRef(QRef ref, Object arg) {
		return ref.decl.type;
	}

	@Override
	public TypeDenoter visitIxQRef(IxQRef ref, Object arg) {
		return new BaseType(TypeKind.INT, null);
	}
	
	
	/* 
	 * start visit terminal, all terminals, except operator, are expected to return
	 * a TypeDenoter
	 */
	@Override
	public TypeDenoter visitIdentifier(Identifier id, Object arg) {
		//ClassDecl has TypeDenoter already as type
		return id.decl.type;
	}

	@Override
	public TypeDenoter visitOperator(Operator op, Object arg) {
		// Operator has no type and thus return none
		return null;
	}

	@Override
	public TypeDenoter visitIntLiteral(IntLiteral num, Object arg) {
		BaseType intType = new BaseType(TypeKind.INT, null);
		return intType;
	}

	@Override
	public TypeDenoter visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		BaseType boolType = new BaseType(TypeKind.BOOLEAN, null);
		return boolType;
	}

	@Override
	public TypeDenoter visitNullLiteral(NullLiteral nllLit, Object arg) {
		BaseType nullType = new BaseType(TypeKind.NULL, null);
		return nullType;
	}

}
