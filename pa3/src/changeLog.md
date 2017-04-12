# Summary of Changes made to AST classes since PA2
* Terminal
	* Add NullLiteral
* Identifier
	* Add new field: Declaration decl.
* ClassDecl
	* Add new field class_member
	* New method shallow_visit
	* Add new field type to denote the corresponding ClassType
* Reference
	* For all subclasses of Reference, added a new field Declaration decl.

## Notes:
Class A{
	void foo(A x){
		this = x;
	}
}
is illegal, hence for the visitAssignStmt, check stmt.ref instanceof ThisRef
## Notes:
Need to fix the VisitCallExpr in the typeVisit