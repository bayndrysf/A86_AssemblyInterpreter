/**
 * Labels class is used to store labels with their line numbers and names. These
 * data cannot be changed after the construction.
 *
 */
public class Labels {
	private int line;

	private String labelName;

	public Labels(String name, int line) {
		this.line = line;
		this.labelName = name;
	}

	// get the line number
	public int getLine() {
		return this.line;
	}

	// label's name
	public String getName() {
		return this.labelName;
	}

}
