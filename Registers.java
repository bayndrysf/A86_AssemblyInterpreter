/**
 * Registers class is used for all of the given registers for this assignment.
 * 
 * Registers' values can be stored in their value field and can be changed later
 * with corresponding methods. Also, the parent-child relationship is formed in
 * this class between 16-bit and 8-bit registers.
 * 
 * In addition to those, while setting their value, some controls are made and
 * error message is printed if there are something against the rules.
 * 
 */

public class Registers {

	private String regValue; // 0x... format
	private int regDecimalNumber;

	private Registers subRegistersUpper;
	private Registers subRegistersLower;
	private Registers parentRegister;

	private String name;
	private boolean update = false;

	// changes the value of the register
	public void setValue(int a) {
		String tempHex = Integer.toHexString(a);
		if (tempHex.length() > this.regValue.length() - 2) {
			tempHex = tempHex.substring(tempHex.length() - this.regValue.length() + 2);
		}

		String sp = "";
		for (int i = 0; i < this.regValue.length() - tempHex.length() - 2; i++)
			sp += "0";

		tempHex = "0x" + sp + tempHex;

		this.setRegValue(tempHex);
	}

	public void setValueMov(int a) {

		if (this.regValue.length() == 6 && (a < 0 || a > 0xffff)) {
			System.out.println(Integer.toHexString(a));
			System.out.println(this.getName());
			System.out.println("Error");
			System.exit(99);
		}
		if (this.regValue.length() == 4 && (a < 0 || a > 0xff)) {

			System.out.println("Error");
			System.exit(99);

		}
		this.regDecimalNumber = a;
		String t = Integer.toHexString(a);
		String sp = "";
		for (int i = 0; i < this.regValue.length() - t.length() - 2; i++)
			sp += "0";

		t = "0x" + sp + t;
		this.setRegValue(t);
	}

	/**
	 * Decreases the value of the register by 1. Prints error message if the new
	 * format is erroneous.
	 */
	public void decrement() {
		regDecimalNumber--;
		if (regDecimalNumber < 0) {
			System.out.println("Error");
			System.exit(99);
		}
		String newHex = Integer.toHexString(regDecimalNumber);
		String temp = "0x";
		for (int i = 0; i < this.regValue.length() - newHex.length() - 2; i++) {
			temp += "0";
		}
		temp += newHex;
		if (temp.length() != this.regValue.length()) {
			System.out.println("Error");
			System.exit(99);
		}
		this.setRegValue(temp);
	}

	// increases the register's value by 1
	public void increment() {
		regDecimalNumber++;
		if (regDecimalNumber > 0xffff && this.regValue.length() == 6
				|| regDecimalNumber > 0xff && this.regValue.length() == 4) {
			System.out.println("Error");
			System.exit(99);
		}
		String newHex = Integer.toHexString(regDecimalNumber);
		String temp = "0x";
		for (int i = 0; i < this.regValue.length() - newHex.length() - 2; i++) {
			temp += "0";
		}
		temp += newHex;
		if (temp.length() != this.regValue.length()) {
			System.out.println("Error");
			System.exit(99);
		}
		this.setRegValue(temp);
	}

	// gets the register's value in hexadecimal number
	public String getRegValue() {
		return regValue;
	}

	// gets the register's value in decimal number
	public int getDecimal() {
		return regDecimalNumber;
	}

	public void update() {
		update = true; // The true value of "update" prevents infinite loop.
		if (this.parentRegister != null) {
			String tempParent = parentRegister.subRegistersUpper.getRegValue()
					+ parentRegister.subRegistersLower.getRegValue().substring(2);
			parentRegister.setRegValue(tempParent);
		}
		if (this.subRegistersUpper != null && this.subRegistersLower != null) {
			String tempUpper = getRegValue().substring(0, 4);
			String tempLower = "0x" + getRegValue().substring(4);
			this.subRegistersUpper.setRegValue(tempUpper);
			this.subRegistersLower.setRegValue(tempLower);
		}

		update = false; // Make it possible to update regValue later on.
	}

	public void setRegValue(String temp) { // 0x--- format ;
		if (temp.length() != this.regValue.length()) {
			System.out.println("Error");
			System.exit(99);
		}
		this.regValue = temp;
		if (update == false) {
			update();
		} // prevents infinite loop
		this.regDecimalNumber = toDecimal(regValue);

	}

	// constructor
	public Registers(String regValue, String name) {
		this.regValue = regValue;
		this.setName(name);
		regDecimalNumber = toDecimal(regValue);
	}

	public void setParent(Registers r) {
		this.parentRegister = r;
	}

	public void setUpper(Registers r) {
		this.subRegistersUpper = r;
	}

	public void setLower(Registers r) {
		this.subRegistersLower = r;
	}

	private int toDecimal(String temp) {
		if (temp.length() < 3) {
			return 0;
		}
		int num = Integer.parseInt(temp.substring(2), 16);
		return num;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
