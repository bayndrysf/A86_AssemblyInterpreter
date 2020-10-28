/**
 * Class of the Flags objects that only have a value as data. There is a
 * constructor defined that takes a boolean value as a parameter and sets it to
 * the value of this object. This class has two methods that returns the object's
 * value and changes it.
 */
public class Flags {

	private boolean value = false;

	public Flags(boolean boolValue) {
		this.value = boolValue;
	}
	
	// gets the value
	public boolean get() {
		return this.value;
	}
	
	// changes the value of value
	public void set(boolean value) {
		this.value = value;
	}

}
