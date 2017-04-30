package miniJava.CodeGenerator;

public class RunTimeEntity {
	// For class decl, indicates the size of the class
	// how much space needs to be pushed onto the Heap
	int size = -1;
	// For method decl, indicates method's address to
	// CB
	int methodAddr = -1;
	// For non static field, indicates the distance to
	// the object's address
	int fieldDis = -1;
	// For static field, indicates the field's distance to
	// the SB
	int staticFieldDis = -1;
	// For local variable, indicates the variable's distace
	// to the LB
	int LBoffset = -1;
}
