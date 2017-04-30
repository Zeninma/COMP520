/*
 * All questionable places are marked with the comment
 * key word: Highlighted
 */
package miniJava.ContextualAnalyzer;

import java.util.HashMap;
import java.util.Stack;

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
import miniJava.AbstractSyntaxTrees.MemberDecl;
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
import miniJava.AbstractSyntaxTrees.Reference;
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
import miniJava.SyntacticAnalyzer.SourcePosition;

public class IdentificationVisitor implements Visitor<Object, Object> {
	IdStack id_stack = new IdStack(this);
	ClassDecl current_class_decl = null;
	boolean inStaticMethod = false;
	// declaration for array.length
	FieldDecl lenDecl = new FieldDecl(false, false, 
			new BaseType(TypeKind.INT, null), "length", null);
	ErrorReporter reporter;
	boolean funCall = false;
	
	public void identifyTree(AST ast, ErrorReporter new_reporter) {
		this.reporter = new_reporter;
		System.out.println("*********************** start Identification *************");
		ast.visit(this, null);
	}
	
	public boolean not_predefined(String name){
		if(name.equals("System")){
			return false;
		}
		else if(name.equals("_PrintStream")){
			return false;
		}
		else if(name.equals("String")){
			return false;
		}
		else{
			return true;
		}
	}
	
	public Object visitPackage(Package prog, Object arg) {
		// Store all ClassDecl into the class_table
		// In the meantime, for each class, create a 'class_members' in it
		// Put every field and method Decl into the 'class_members'
		// need to add predefined names, and base types
		this.id_stack.initialize(prog);
		
		this.id_stack.add_scope();
		ClassDeclList cl = prog.classDeclList;
		HashMap<String, Declaration> current_table = id_stack.peek();
		for (ClassDecl c : cl) {
			String name = c.name;
			if (current_table.containsKey(name)) {
				System.out.println("*** repeated class name: " + name);
				System.exit(4);
			} else {
				current_table.put(name, c);
				c.shallow_visit(this);
			}
		}
		for (ClassDecl c: cl){
			c.visit(this, null);
		}
		return null;
	}

	public void shallow_visit_class(ClassDecl c) {
		// shallowVisitClass will visit the given class,
		// put every field and method Decl into the 'class_members'
		//Remeber to first visit the field decl
		HashMap<String, Declaration> member_table = new HashMap<String, Declaration>();
		FieldDeclList fl = c.fieldDeclList;
		MethodDeclList ml = c.methodDeclList;
		for (FieldDecl field_decl : fl) {
			String name = field_decl.name;
			if (member_table.containsKey(name)) {
				System.out.println("*** repeated field or method name: " + name);
				System.exit(4);
			} else {
				member_table.put(name, field_decl);
			}
		}
		for (MethodDecl method_decl : ml) {
			String name = method_decl.name;
			if (member_table.containsKey(name)) {
				System.out.println("*** repeated field or method name: " + name);
				System.exit(4);
			} else {
				member_table.put(name, method_decl);
			}
		}
		c.class_members = member_table;
	}

	public Object visitClassDecl(ClassDecl c, Object arg) {
		this.current_class_decl = c;
		// push the class decl, and enter the 2nd level
		this.id_stack.add_scope();
		FieldDeclList fl = c.fieldDeclList;
		MethodDeclList ml = c.methodDeclList;
		//First visit the field decl
		for(FieldDecl field_decl: fl){
			field_decl.visit(this, null);
		}
		for(MethodDecl method_decl: ml){
			method_decl.visit(this, null);
		}
		// pop out the class table,
		// and goes back to the first level
		this.id_stack.pop_scope();
		return null;
	}

	public Object visitFieldDecl(FieldDecl fd, Object arg) {
		this.id_stack.add(fd.name, fd);
		fd.type.visit(this,null);
		return null;
	}

	public Object visitMethodDecl(MethodDecl md, Object arg) {
		// The table on the top of the current stack
		// Whenver enter a new scope, push the new table onto the stack
		//check return statement
		
		// check whether the return statement and the type of the method
		int lastStmtIdx = md.statementList.size() -1;
			if(md.type.typeKind == TypeKind.VOID){
				// void mehod
				if(lastStmtIdx >= 0 && ! (md.statementList.get(lastStmtIdx) instanceof ReturnStmt) ){
					md.statementList.add(new ReturnStmt(null, null));
				}
				if (lastStmtIdx >= 0 && (md.statementList.get(lastStmtIdx) instanceof ReturnStmt)){
					ReturnStmt rtStmt = (ReturnStmt) md.statementList.get(lastStmtIdx);
					if (rtStmt.returnExpr != null){
						this.report("void method cannot returtn value",rtStmt.posn);
						System.exit(4);
					}
				}
			}
			else{
				if (lastStmtIdx <0 ){
					System.out.println("*** Non void method has no return statement at the end");
					System.exit(4);
				}
				else if(! (md.statementList.get(lastStmtIdx) instanceof ReturnStmt)){
					System.out.println("*** Non void method has no return statement at the end");
					System.exit(4);
				}
				else{
					
				}
			}
		
		this.id_stack.add_scope();
		ParameterDeclList pl = md.parameterDeclList;
		StatementList sl = md.statementList;
		for(ParameterDecl parameter_decl: pl){
			parameter_decl.visit(this, null);
		}
		// Here actually starts another stmt block, need to add another scope
		this.id_stack.add_scope();
		if(md.isStatic){
			this.inStaticMethod = true;
		}
		for(Statement statement: sl){
			statement.visit(this, null);
		}
		this.inStaticMethod = false;
		this.id_stack.pop_scope();
		//At the end of the visit, pop out the current table
		this.id_stack.pop_scope();
		return null;
	}

	public Object visitParameterDecl(ParameterDecl pd, Object arg) {
		pd.type.visit(this, null);
		this.id_stack.add(pd.name, pd);
		pd.type.visit(this, null);
		return null;
	}

	public Object visitVarDecl(VarDecl decl, Object arg) {
		// put the VarDecl at the top table
		String var_name = decl.name;
		id_stack.add(var_name, decl);
		decl.type.visit(this, null);
		return null;
	}

	public Object visitBaseType(BaseType type, Object arg) {
		// Highlighted what to do with the Base Type
		return null;
	}

	public Object visitClassType(ClassType type, Object arg) {
		String class_name = type.className.spelling;
		//retrieve the class table
		HashMap<String, Declaration> class_table = this.id_stack.id_stack.get(1);
		HashMap<String, Declaration> predefined_table = this.id_stack.id_stack.get(0);
		Declaration classDecl = class_table.get(class_name);
		Declaration predefinedClassDecl = predefined_table.get(class_name);
		if(classDecl!= null){
			type.className.decl = classDecl;
		}
		else{
			if(predefinedClassDecl != null){
				type.className.decl = predefinedClassDecl;
			}
			else{
				System.out.println("*** No class found with the name: " + class_name);
				System.exit(4);
			}
		}
		return null;
	}

	public Object visitArrayType(ArrayType type, Object arg) {
		type.eltType.visit(this, null);
		return null;
	}

	public Object visitBlockStmt(BlockStmt stmt, Object arg) {
		// when entering a BlockStmt, need to put a new table onto the stack
		this.id_stack.add_scope();
		//for stmt list ...
		StatementList statement_list = stmt.sl;
		for (Statement statement: statement_list){
			statement.visit(this, null);
		}
		this.id_stack.pop_scope();
		return null;
	}

	public Object visitVardeclStmt(VarDeclStmt stmt, Object arg) {
		/*
		 * In vardeclStmt, need to prevent to using the variable
		 * being declared in he initializing expression
		 */
		String var_name = stmt.varDecl.name;
		Expression expr = stmt.initExp;
		// Highlighted
		this.id_stack.add(var_name, null); 
		expr.visit(this, null);
		// after finishing the expr, replace the null with the real decl
		this.id_stack.peek().put(var_name, stmt.varDecl);
		return null;
	}

	public Object visitAssignStmt(AssignStmt stmt, Object arg) {
		Reference ref = stmt.ref;
		Expression exp = stmt.val;
		ref.visit(this, null);
		exp.visit(this, null);
		return null;
	}

	public Object visitCallStmt(CallStmt stmt, Object arg) {
		this.funCall = true;
		stmt.methodRef.visit(this, null);
		this.funCall = false;
		if(!(stmt.methodRef.decl instanceof MethodDecl)){
			this.report(stmt.methodRef.decl.name+" is not a Method", stmt.posn);
			System.exit(4);
		}
		ExprList el = stmt.argList;
		for (Expression expr: el){
			expr.visit(this,null);
		}
		return null;
	}

	public Object visitReturnStmt(ReturnStmt stmt, Object arg) {
		Expression expr= stmt.returnExpr;
		if(expr != null){
			expr.visit(this, null);
		}
		return null;
	}

	public Object visitIfStmt(IfStmt stmt, Object arg) {
		Expression condition = stmt.cond;
		condition.visit(this,null);
		Statement then_stmt = stmt.thenStmt;
		if (then_stmt instanceof VarDeclStmt){
			this.report("VarDecl in a branch of a conditional statement",then_stmt.posn);
			System.exit(4);
		}
		then_stmt.visit(this,null);
		Statement else_stmt = stmt.elseStmt;
		if (else_stmt != null){
			if (else_stmt instanceof VarDeclStmt){
				this.report("VarDecl in a branch of a conditional statement",else_stmt.posn);
				System.exit(4);
			}
			else_stmt.visit(this,null);
		}
		return null;
	}

	public Object visitWhileStmt(WhileStmt stmt, Object arg) {
		Expression condition = stmt.cond;
		condition.visit(this, null);
		Statement body_stmt = stmt.body;
		if (body_stmt instanceof VarDeclStmt){
			this.report("VarDecl in a branch of a conditional statement",body_stmt.posn);
			System.exit(4);
		}
		body_stmt.visit(this, null);
		return null;
	}

	public Object visitUnaryExpr(UnaryExpr expr, Object arg) {
		expr.expr.visit(this, null);
		return null;
	}

	public Object visitBinaryExpr(BinaryExpr expr, Object arg) {
		expr.left.visit(this, null);
		expr.right.visit(this, null);
		return null;
	}

	public Object visitRefExpr(RefExpr expr, Object arg) {
		expr.ref.visit(this, null);
//		if(expr.ref.decl instanceof ClassDecl){
//			this.report(expr.ref.decl.name+ " Invalid reference", expr.ref.posn);
//		}
		return null;
	}

	public Object visitCallExpr(CallExpr expr, Object arg) {
		this.funCall = true;
		expr.functionRef.visit(this, null);
		this.funCall = false;
		Declaration funDecl = expr.functionRef.decl;
		if(!(funDecl instanceof MethodDecl)){
			this.report(funDecl.name+" is not a Method", expr.posn);
			System.exit(4);
		}
		ExprList el = expr.argList;
		for(Expression arg_exp: el){
			arg_exp.visit(this, null);
		}
		return null;
	}

	public Object visitLiteralExpr(LiteralExpr expr, Object arg) {
		expr.lit.visit(this, null);
		return null;
	}

	public Object visitNewObjectExpr(NewObjectExpr expr, Object arg) {
		expr.classtype.visit(this, null);
		return null;
	}

	public Object visitNewArrayExpr(NewArrayExpr expr, Object arg) {
		expr.eltType.visit(this, null);
		expr.sizeExpr.visit(this, null);
		return null;
	}

	public Object visitThisRef(ThisRef ref, Object arg) {
		ref.decl = this.current_class_decl;
		return null;
	}

	public Object visitIdRef(IdRef ref, Object arg) {
		Identifier id = ref.id;
		Declaration refDecl = this.id_stack.retrieve(id.spelling);
		if(this.inStaticMethod){
			if((refDecl instanceof FieldDecl) && !((FieldDecl)refDecl).isStatic ){
				this.report("cannot reference non-static symol: "+id.spelling+" in static context", refDecl.posn);
				System.exit(4);
			}
		}
		if(!this.funCall && (refDecl instanceof MethodDecl)){
			this.report("In valid function reference:"+id.spelling, refDecl.posn);
			System.exit(4);
		}
		ref.decl =refDecl;
		id.visit(this, null);
		return null;
	}

	public Object visitIxIdRef(IxIdRef ref, Object arg) {
		ref.id.visit(this, null);
		ref.decl = this.id_stack.retrieve(ref.id.spelling);
		if(this.inStaticMethod){
			if((ref.decl instanceof FieldDecl) && !((FieldDecl) ref.decl).isStatic ){
				this.report("cannot reference non-static symol: "+ref.id.spelling+" in static context", ref.decl.posn);
				System.exit(4);
			}
		}
		ref.indexExpr.visit(this, null);
		return null;
	}

	public Object visitQRef(QRef ref, Object arg) {
		// Highlighted
		String member_name = ref.id.spelling;
		ref.ref.visit(this, null);
		Declaration ref_decl = ref.ref.decl;
		//debug
		// All Declaration other than ClassDecl has field type
		// ClassDecl Foo.x, static method
		if (ref_decl instanceof ClassDecl){
			String class_name = ref_decl.name;
			HashMap<String, Declaration> member_table = ((ClassDecl) ref_decl).class_members;
			if(member_table.containsKey(member_name)){
				MemberDecl member_decl = (MemberDecl) member_table.get(member_name);
				//check private of the member_decl
				if(member_decl.isPrivate){
					if(this.current_class_decl.equals(ref_decl)){
						ref.decl = member_decl;
						if(this.inStaticMethod){
							if((ref.decl instanceof FieldDecl) && !((FieldDecl) ref.decl).isStatic ){
								this.report("cannot reference non-static symol: "+ref.id.spelling+" in static context", ref.decl.posn);
								System.exit(4);
							}
						}
					}
					else{
						System.out.println("*** Cannot access to the private member: "+ member_name + ", of Class: " + class_name);
						System.exit(4);
					}
				}
				else if(!member_decl.isStatic){
					if(ref.ref instanceof IdRef){
						if(((IdRef)ref.ref).id.spelling.equals(class_name)){
							System.out.println("*** Class: " + class_name + ",can only access to its static member, rather than: " + member_name);
							System.exit(4);
						}
					}
					ref.decl = member_decl;
					if(this.inStaticMethod){
						if((ref.decl instanceof FieldDecl) && !((FieldDecl) ref.decl).isStatic ){
							this.report("cannot reference non-static symol: "+ref.id.spelling+" in static context", ref.decl.posn);
							System.exit(4);
						}
					}
				}
				else{
					ref.decl = member_decl;
					if(this.inStaticMethod){
						if((ref.decl instanceof FieldDecl) && !((FieldDecl) ref.decl).isStatic ){
							this.report("cannot reference non-static symol: "+ref.id.spelling+" in static context", ref.decl.posn);
							System.exit(4);
						}
					}
				}
			}
			else{
				System.out.println("*** Class: " + class_name + ",does not contain member: " + member_name);
				System.exit(4);
			}
		}
		else if(ref_decl.type instanceof ArrayType && member_name.equals("length")){
			ref.decl = this.lenDecl;
		}
		// non_static, need to check the static
		//ClassType ie x.y.z in case y in x.y is a member of class x
		else if(ref_decl.type instanceof ClassType ){
			String class_name = ((ClassType) ref_decl.type).className.spelling;
			HashMap<String, Declaration> member_table = ((ClassDecl) this.id_stack.retrieveClass(class_name)).class_members;
			if(member_table.containsKey(member_name)){
				MemberDecl member_decl = (MemberDecl) member_table.get(member_name);
				if(member_decl.isPrivate){
					if(this.current_class_decl.name.equals(class_name)){
						ref.decl = member_decl;
					}
					else{
						System.out.println("*** Cannot access to the private member: "+ member_name + ", of Class: " + class_name);
						System.exit(4);
					}
				}
				//check static
				else{
					ref.decl = member_decl;
				}
				
			}
			else{
				System.out.println("*** Class: " + class_name + " does not contain member: " + member_name);
				System.exit(4);
			}
		}
		else{
			System.out.println("*** Class: " + ref_decl.type + " does not exists");
			System.exit(4);
		}
		return null;
	}

	public Object visitIxQRef(IxQRef ref, Object arg) {
		// Highlighted
		String member_name = ref.id.spelling;
		ref.ref.visit(this, null);
		Declaration ref_decl = ref.ref.decl;
		// All Declaration other than ClassDecl has field type
		if (ref_decl instanceof ClassDecl){
			String class_name = ref_decl.name;
			HashMap<String, Declaration> member_table = ((ClassDecl) ref_decl).class_members;
			if(member_table.containsKey(member_name)){
				FieldDecl fieldDecl = (FieldDecl) member_table.get(member_name);
				if (fieldDecl.type instanceof ArrayType){
					TypeDenoter elType = ((ArrayType) fieldDecl.type).eltType;
					if (elType instanceof BaseType && elType.typeKind == TypeKind.INT){
						ref.id.decl = fieldDecl;
						ref.decl = this.id_stack.retrieveClass("int");
					}
					else{
						this.report("Only support access to int array", ref.posn);
						System.exit(4);
					}
				}
				else{
					this.report("QRef is not a reference to an array", ref.posn);
					System.exit(4);
				}
			}
			else{
				System.out.println("*** Class: " + class_name + ",does not contain member: " + member_name);
				System.exit(4);
			}
		}
		else if(ref_decl.type instanceof ClassType ){
			String class_name = ((ClassType) ref_decl.type).className.spelling;
			HashMap<String, Declaration> member_table = ((ClassDecl)this.id_stack.retrieve(class_name)).class_members;
			if(member_table.containsKey(member_name)){
				FieldDecl fieldDecl = (FieldDecl) member_table.get(member_name);
				if (fieldDecl.type instanceof ArrayType){
					TypeDenoter elType = ((ArrayType) fieldDecl.type).eltType;
					if (elType instanceof BaseType && elType.typeKind == TypeKind.INT){
						ref.id.decl = fieldDecl;
						ref.decl = this.id_stack.retrieveClass("int");
					}
					else{
						this.report("Only support access to int array", ref.posn);
						System.exit(4);
					}
				}
				else{
					this.report("QRef is not a reference to an array", ref.posn);
					System.exit(4);
				}
			}
			else{
				System.out.println("*** Class: " + class_name + ",does not contain member: " + member_name);
				System.exit(4);
			}
		}
		else{
			System.out.println("*** Class: " + ref_decl.type + " does not exists");
			System.exit(4);
		}
		ref.ixExpr.visit(this, null);
		return null;
	}


	public Object visitIdentifier(Identifier id, Object arg) {
			id.decl = this.id_stack.retrieve(id.spelling);
		return id.decl;
	}

	public Object visitOperator(Operator op, Object arg) {
		return null;
	}

	public Object visitIntLiteral(IntLiteral num, Object arg) {
		return null;
	}

	public Object visitBooleanLiteral(BooleanLiteral bool, Object arg) {
		return null;
	}

	public Object visitNullLiteral(NullLiteral nllLit, Object arg) {
		return null;
	}
	
	public void report(String errorMessage, SourcePosition posn){
		this.reporter.reportError(errorMessage);
	}

}