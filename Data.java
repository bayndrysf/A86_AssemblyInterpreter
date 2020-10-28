
public class Data {

	private int offset = 0;
	private char charValue;
	private int intValue;
	private String type;
	private String name;
	private boolean isWord;

	public void setValue(int a) {
		if (type.equals("char") && a > 255) {
			System.out.println("Error - dataa1");
			System.exit(99);
		}
		if (isWord() && (a > 0xffff || a < 0)) {
			System.out.println("Error - data");
			System.exit(99);
		}

		this.setIntValue(a);
		this.charValue = (char) a;
	}

	public Data(String type, int line, char value, String typeOf, String name) {
		this.type = type;
		this.charValue = value;
		this.setIntValue(charValue);
		this.setName(name);
		if (typeOf.equalsIgnoreCase("db")) {
			this.setWord(false);
		}
		if (typeOf.equalsIgnoreCase("dw")) {
			this.setWord(true);
		}

	}

	public Data(String type, int line, int value, String typeOf, String name) {
		this.setName(name);
		this.type = type;
		this.setIntValue(value);
		if (typeOf.equalsIgnoreCase("db")) {
			this.setWord(false);
		} else if (typeOf.equalsIgnoreCase("dw")) {
			this.setWord(true);
		}
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public boolean isWord() {
		return isWord;
	}

	public void setWord(boolean isWord) {
		this.isWord = isWord;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
