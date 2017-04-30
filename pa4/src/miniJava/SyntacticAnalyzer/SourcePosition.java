package miniJava.SyntacticAnalyzer;

public class SourcePosition {
	// object holds the position of the token;
	public int line, column;
	// start from line 1, column 1;
	public SourcePosition() {
		line = 1;
		column = 1;
	}

	public SourcePosition(int new_line, int new_column) {
		this.line = new_line;
		this.column = new_column;
	}

	public SourcePosition(SourcePosition token_position) {
		this.line = token_position.line;
		this.column = token_position.column;

	}

	public String toString() {
		return "line: " + this.line + ",column: " + this.column + ";";
	}
}