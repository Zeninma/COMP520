/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.Token;

public class Identifier extends Terminal {
	public Identifier(Token t) {
		super(t);
	}

	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitIdentifier(this, o);
	}
	//add new attribute decl and its setter.
	public void add_decl(Declaration new_decl){
		this.decl = new_decl;
	}
	
	public Declaration decl;
}
