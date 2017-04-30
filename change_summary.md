# Summary of Changes made to AST classes since PA2
* Terminal
	* Add NullLiteral
* Identifier
	* Add new field: Declaration decl.
* ClassDecl
	* Add new field class_member
	* New method shallow_visit
* ASTDisplay
	* Add new method, visitNullLiteral. 

## Notes:
Class A{
	void foo(A x){
		this = x;
	}
}
is illegal, hence for the visitAssignStmt, check stmt.ref instanceof ThisRef