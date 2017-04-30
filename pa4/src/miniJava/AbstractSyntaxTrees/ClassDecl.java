/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.util.HashMap;

import miniJava.ContextualAnalyzer.IdentificationVisitor;
import  miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;

public class ClassDecl extends Declaration {

  public ClassDecl(String cn, FieldDeclList fdl, MethodDeclList mdl, SourcePosition posn) {
	  super(cn, null, posn);
	  fieldDeclList = fdl;
	  methodDeclList = mdl;
	  Identifier classId = new Identifier(new Token(TokenKind.ID, cn, posn));
	  super.type = new ClassType(classId, posn);
  }
  
  public <A,R> R visit(Visitor<A, R> v, A o) {
      return v.visitClassDecl(this, o);
  }
  
  public void shallow_visit(IdentificationVisitor v){
	  v.shallow_visit_class(this);
  }
  //add setter for class_members
  public FieldDeclList fieldDeclList;
  public MethodDeclList methodDeclList;
  // add memeber table for each class Declaration
  public HashMap<String, Declaration> class_members;
}
