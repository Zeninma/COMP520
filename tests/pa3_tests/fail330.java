/*** line 9: reference "this" is not a method
 * COMP 520
 * Identification
 */
class Fail330 {         
    // public static void main(String[] args) { }
	int a;

    public void p() {
        this();
        a();
    }
}

