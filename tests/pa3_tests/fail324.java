/*** line 8: expression in conditional statement does not yield a boolean result
 * COMP 520
 * Type checking
 */
class Fail324 {
    public static void main(String [] args) {
        int x = 3;
	if (x) 
	    x = x + 1;
    }
}
