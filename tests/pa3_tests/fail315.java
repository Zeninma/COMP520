/*** line 8: type "int" of variable "x" incompatible with type "boolean" initial value
 *** line 9: type "boolean" of variable "b" incompatible with type "int" initial value
 * COMP 520
 * Type checking (2 errors)
 */
class fail315 {
    public void foo() {
	int x = 3  > 4;
	boolean b = 2 + 3;
    }
}
