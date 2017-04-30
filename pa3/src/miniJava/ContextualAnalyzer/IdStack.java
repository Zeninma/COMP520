package miniJava.ContextualAnalyzer;
import java.util.HashMap;
import java.util.Stack;

import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;

public class IdStack {
	Stack<HashMap<String, Declaration>> id_stack = new Stack<HashMap<String, Declaration>>();
	int current_level = 0;
	IdentificationVisitor idv;
	public IdStack(IdentificationVisitor new_idv){
		this.idv = new_idv;
	}
	
	public HashMap<String, Declaration> add_scope(){
		HashMap<String, Declaration> current_map = new HashMap<String, Declaration>();
		this.current_level++;
		this.id_stack.push(current_map);
		return current_map;
	}
	
	
	
	public void pop_scope(){
		this.current_level--;
		this.id_stack.pop();
	}
	
	public void initialize(){
		//Need to add:
		//			String
		//			System
		//			_PrintStream
		//			INT,
		//	        BOOLEAN
		if(this.current_level != 1){
			System.out.println("*** The IdStack needs exactly one scope");
			System.exit(4);
		}
		ClassDecl int_decl = new ClassDecl("int", null, null, null);
		this.add("int", int_decl);
		ClassDecl bool_decl = new ClassDecl("Boolean", null, null, null);
		this.add("Boolean", bool_decl);
		ClassDecl string_decl = new ClassDecl("String", new FieldDeclList(), new MethodDeclList(), null);
		string_decl.type = new BaseType(TypeKind.UNSUPPORTED, null);
		this.add("String", string_decl);
		
		//Start building decl for _PrintStream
		ParameterDeclList print_pl = new ParameterDeclList();
		print_pl.add(new ParameterDecl(new BaseType(TypeKind.INT, null),
				"n", null));
		MemberDecl print_method_memberDecl = new FieldDecl(false,
				false,new BaseType(TypeKind.VOID, null), "println", null);
		StatementList print_sl = new StatementList();
		MethodDecl print_methodDecl = new MethodDecl(print_method_memberDecl,
				print_pl, print_sl, null);
		MethodDeclList print_methodList = new MethodDeclList();
		print_methodList.add(print_methodDecl);
		ClassDecl print_decl = new ClassDecl("_PrintStream", new FieldDeclList(),
				print_methodList, null);
		// Add a memeber_table for printdecl
		HashMap<String, Declaration> print_memberTable = new HashMap<String, Declaration>();
		print_memberTable.put("println", print_decl);
		// Add _PrintStream to the id_stack
		this.add("_PrintStream", print_decl);
		//need to deal with predefined class
		
		FieldDeclList systemFdl = new FieldDeclList();
		systemFdl.add(new FieldDecl(false, true, print_decl.type, "out", null));
		ClassDecl systemDecl = new ClassDecl("System", systemFdl, new MethodDeclList(), null);
		this.add("System", systemDecl);
	}
	
	public void add(String name, Declaration decl){
		HashMap<String, Declaration> current_map = this.id_stack.peek();
		// need to check if there exists duplicate in the sope starting from
		// level 3
		int level = this.current_level;
		HashMap<String, Declaration> map;
		if(this.current_level > 3){
			while(level > 2){
				map = this.id_stack.get(level -1);
				if(map.containsKey(name)){
					System.out.println("*** Repeated name : " + name);
					System.exit(4);
				}
				level --;
			}
			current_map.put(name, decl);
		}
		// for the case 3 >= this.current_level >= 0
		else if(this.current_level >0){
			if(current_map.containsKey(name)){
				System.out.println("*** Repeated name : " + name);
				System.exit(4);
			}
			else{
				current_map.put(name, decl);
			}
		}
		else{
			System.out.println("*** Invalid scope number : " + this.current_level);
			System.exit(4);
		}
	}
	public HashMap<String, Declaration> peek(){
		return this.id_stack.peek();
	}
	
	
	public Declaration retrieve(String name){
		// if found the decl corresponding to the name return the decl
		// else exit;
		HashMap<String, Declaration> current_class_members = this.idv.current_class_decl.class_members;
		int level = this.current_level;
		HashMap<String, Declaration> map;
		while(level > 0){
			map = this.id_stack.get(level - 1);
			if(map.containsKey(name)){
				Declaration result_decl = map.get(name);
				if(result_decl != null){
					return result_decl;
				}
				else{
					System.out.println("*** Using variable being declared in the initializing expression");
					System.exit(4);
				}
			}
			level --;
		}
		if(current_class_members.containsKey(name)){
			return current_class_members.get(name);
		}
		System.out.println("*** Invalid Identifier : " + name + ",has not been declared");
		System.exit(4);
		return null;
	}
}
