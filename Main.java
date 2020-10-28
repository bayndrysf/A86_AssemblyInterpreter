
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Scanner;

/**
 * An interpreter for an assembly language of a hypothetical 8086-like CPU
 * called HYP86 with the given features.
 * 
 *@author Can Atakan Ugur, 2017400057
 *@author Yusuf Bayindir , 2017400042 
 */

/**
 * This is where objects are instantiated, the given ".asm" file is read, and
 * according to the instructions, executed. Also, Main class is the class that
 * contains main method in it.
 *
 */
public class Main {

	private static int instructionPointer = 1;

	/**
	 * Objects for the given registers and flags in the description. Will be
	 * instantiated under the instantiation() method.
	 */
	private static Registers AX, BX, CX, DX, DI, SP, SI, BP, AH, AL, BH, BL, CH, CL, DH, DL;
	private static Flags ZF, CF, AF, SF, OF;

	/**
	 * Given instructions are added to this list to decide a given word is a member
	 * of it later on.
	 */
	private static List<String> instructionList;

	/**
	 * Instructions starting with "j"
	 */
	private static List<String> j_instructionList = new ArrayList<String>();

	/**
	 * Lines to be executed are added to this list.
	 */
	private static List<ArrayList<String>> executionList = new ArrayList<ArrayList<String>>();

	/**
	 * Register objects are stored in this list.
	 */
	private static List<Registers> registerList = new ArrayList<Registers>();

	/**
	 * These check lists are used in order to decide whether the given word is a
	 * register, or 16-bit register or an 8-bit register, or not.
	 */
	private static List<String> registersCheckList = new ArrayList<String>();
	private static List<String> registers16CheckList = new ArrayList<String>();
	private static List<String> registers8CheckList = new ArrayList<String>();

	/**
	 * Labels and Data objects are stored within these lists as they are recognized.
	 */
	private static ArrayList<Labels> labels = new ArrayList<Labels>();
	private static ArrayList<Data> variables = new ArrayList<Data>();

	/**
	 * Variables are added to this list as they are recognized to decide whether a
	 * word is a variable or not later on.
	 */
	public static List<String> vairablesCheckList = new ArrayList<String>();

	private static char[] memory = new char[2 << 15];

	private static int numberOfInstructions = 0;

	/**
	 * Lines for the code segment and data segment are stored in different lists.
	 */
	private static ArrayList<ArrayList<String>> codeLines = new ArrayList<>();
	private static ArrayList<ArrayList<String>> dataLines = new ArrayList<>();


	/**
	 * The main method that reads the ".asm" file, adds lines to be executed into
	 * the list and sends those to the execution() in a for loop until the list is
	 * empty. Also, instantiations and adding registers to the list are made here.
	 * 
	 * Takes args as a parameter for the ".asm" file, and throws
	 * FileNotFoundException in case given file is not found while using the file
	 * object.
	 * 
	 * First, it handles instantiation of registers and adds those into the
	 * registerList by using instantiation and addAllregisterListtoList methods.
	 * 
	 * Then it creates a file object using the argument that the program takes when
	 * it's launched. Then reads the file object and checks the lines. If it
	 * encounters any trivial syntax errors, outputs "Error", otherwise adds the
	 * lines to be executed to the list. These syntax rules are recognized with
	 * patterns.
	 * 
	 * toLowerCaseExecution method is used to simplify things at the end.
	 * 
	 * Finally, there is a for loop to finish all the lines to be executed and it
	 * goes until the iteration of list is finished.
	 * 
	 * @param args file path
	 * @throws FileNotFoundException thrown when the given file is not found and
	 *                               used for reading
	 */
	public static void main(String[] args) throws FileNotFoundException {

		String[] instructions = { "MOV", "ADD", "SUB", "MUL", "DIV", "XOR", "OR", "AND", "NOT", "RCL", "RCR", "SHL",
				"SHR", "PUSH", "POP", "NOP", "CMP", "JMP", "JZ", "JNZ", "JE", "JNE", "JA", "JAE", "JB", "JBE", "JNAE",
				"JNB", "JNBE", "JNC", "JC", "INT", "INC", "DEC", "CODE" };

		instructionList = Arrays.asList(instructions);

		// register instantiations
		instantiation();
		// add all registerList ;
		addAllregisterListtoList();

		// File Handling

		File f = new File(args[0]);
		Scanner read = new Scanner(f);

		// for int 20h
		int lineForInt20h = 1;// for int 20h

		/*
		 * Instructions Processing-until "int 20h"
		 */
		while (read.hasNextLine()) {

			String tempNewLine = read.nextLine();

			// In case of any trivial syntax error
			if (tempNewLine.matches("(.*)\\[\\s*\\S+\\s+\\S+\\s*\\](.*),")) {
				System.out.println("Error");
				System.exit(99);
			} else if (tempNewLine.matches("(.*),(.*)\\[\\s*\\S+\\s+\\S+\\s*\\](.*)")) {
				System.out.println("Error");
				System.exit(99);
			}

			else if ((tempNewLine.matches("(.*),\\s*[a-zA-Z0-9_$]+\\s+[a-zA-Z0-9_$]+\\s*")
					|| tempNewLine.matches("\\s*[a-zA-Z0-9_$]+\\s+[a-zA-Z0-9_$]+\\s+[a-zA-Z0-9_$]+\\s*,(.*)")
					|| tempNewLine.matches("(.*)\\[\\s*\\S+\\s+\\S+(.*)"))
					&& !tempNewLine.matches("(.*)[bw]\\s+\\S+\\s*,(.*) | (.*),\\s*[bw]\\s+\\S+(.*) ")
					&& !tempNewLine.matches("(.*)[bw]\\s*\\[(.*)\\](.*)")
					&& !tempNewLine.matches("(.*)\\boffset\\b\\s+\\S+\\s*,(.*)")
					&& !tempNewLine.matches("(.*),\\s*\\boffset\\b\\s+\\S+(.*)")) {
				System.out.println("Error");
				System.exit(99);
			}

			StringTokenizer st1 = new StringTokenizer(tempNewLine);

			// In case of single or double quotes in tempLeftOperands
			if (tempNewLine.matches("(.*)[\"\'](.*)[\"\'](.*)|(.*)[\"\'](.*)[\"\'](.*)")) {
				if (!tempNewLine.matches("(.*),(.*)\'[ -~]\'(.*)") && !tempNewLine.matches("(.*)\"[ -~]\"(.*),(.*)")) {
					System.out.println("Error");
					System.exit(99);
				}
			}

			ArrayList<String> tempArrayList = new ArrayList<>();

			while (st1.hasMoreTokens()) {
				tempArrayList.add(st1.nextToken());
			}

			// Resolves given space character.(' ')
			int temp = 0;
			if (tempNewLine.contains("\' \'") || tempNewLine.contains("\" \"")) {
				if (tempNewLine.matches("(.*),(.*)[\'\"]\\s[\'\"](.*)"))
					temp = 2; // right operand
				if (tempNewLine.matches("(.*)[\'\"]\\s[\'\"](.*),(.*)"))
					temp = 1; // left operand
			}

			if (temp != 0) {
				tempArrayList.set(tempArrayList.indexOf("\'"), "\' \'");
				tempArrayList.remove(tempArrayList.size() - 1);
			}

			codeLines.add(tempArrayList);

			// int 20h
			if (tempArrayList.size() == 2 && tempArrayList.get(0).equalsIgnoreCase("int")
					&& tempArrayList.get(1).equalsIgnoreCase("20h")) {
				break;
			}
			lineForInt20h++;
		}
		// Instruction processing

		int tempLine = lineForInt20h; // tempLine = int 20h line

		// Get the data variables.
		lineForInt20h++;
		while (read.hasNextLine()) {
			StringTokenizer st1 = new StringTokenizer(read.nextLine());

			ArrayList<String> temp = new ArrayList<>();
			while (st1.hasMoreTokens()) {
				temp.add(st1.nextToken());
			}
			dataLines.add(temp);
			lineForInt20h++;
		} // data info

		// Read and store the data.
		for (int i = 0; i < dataLines.size(); i++) {
			syntaxError(dataLines.get(i), tempLine + 1, false, true);
			tempLine++;
		}

		// Read and process the instructions
		for (int i = 0; i < codeLines.size(); i++) {
			syntaxError(codeLines.get(i), i + 1, true, false);
			tempLine++;
		}

		// All into lower case,except characters.
		toLowerCaseExecution();

		variableToMemory();

		// Executes the lines.
		for (int i = 0; i < executionList.size();) {
			execution(executionList.get(i), i + 1);
			i = instructionPointer;
			instructionPointer++;
		}

	}

	/**
	 * Executes the given line of the executionList by checking its tokens, and
	 * calling the related methods according to those.
	 * 
	 * First checks syntax errors such as missing or unnecessary operands according
	 * to the mnemonic. After it decides it's in the right format, the necessary
	 * method is called afterwards.
	 * 
	 * @param executionListLine the string in the whole current line to be executed
	 * @param line              number of the line
	 */
	public static void execution(ArrayList<String> executionListLine, int line) {

		if (executionListLine.size() == 0)
			return;

		String mnemonics = "";
		mnemonics = executionListLine.get(0);
		mnemonics = mnemonics.toLowerCase();

		if (executionListLine.size() == 1 && !mnemonics.equalsIgnoreCase("nop")) {
			return; // label
		}

		if (mnemonics.equals("div")) {
			if (executionListLine.size() != 2) {
				System.out.println("Error");
				System.exit(99);
			}

			String sourceOperand = executionListLine.get(1);
			div(sourceOperand);

		}

		else if (mnemonics.equals("mul")) {
			if (executionListLine.size() != 2) {
				System.out.println("Error");
				System.exit(99);
			}
			String sourceOperand = executionListLine.get(1);
			mul(sourceOperand);
		}

		else if (mnemonics.equals("push")) {
			if (executionListLine.size() != 2) {
				System.out.println("Error");
				System.exit(99);
			}
			String source = executionListLine.get(1);
			push(source);
		}

		else if (mnemonics.equals("pop")) {
			if (executionListLine.size() != 2) {
				System.out.println("Error");
				System.exit(99);
			}
			String dest = executionListLine.get(1);
			pop(dest);
		}

		else if (mnemonics.equals("int")) {

			Scanner scanner = new Scanner(System.in);
			if (executionListLine.size() != 2) {
				System.out.println("Error");
				System.exit(99);
			}
			String subFunction = Integer.toHexString(AH.getDecimal());
			int subFunctionInt = Integer.parseInt(subFunction, 16);

			String destination = executionListLine.get(1);

			if (destination.equalsIgnoreCase("20h")) {
				System.exit(99);
			}

			else if (destination.equalsIgnoreCase("21h")) {
				// Read from user

				if (subFunctionInt == 1) {
					String tempLine = scanner.next();
					char character = tempLine.charAt(0);
					AL.setValue(character);

				}

				// write to the console
				if (subFunctionInt == 2) {
					char sth = (char) DL.getDecimal();
					AL.setValue(sth);
					System.out.print(sth);
				}

			} else {
				System.out.println("Error");
				System.exit(99);

			}

		} // int 20h, int 21h

		else if (mnemonics.equalsIgnoreCase("and")) {
			and(executionListLine, line);
		}

		else if (mnemonics.equalsIgnoreCase("or")) {
			or(executionListLine, line);
		}

		else if (mnemonics.equalsIgnoreCase("xor")) {
			xor(executionListLine, line);
		}

		else if (mnemonics.equalsIgnoreCase("not")) {
			not(executionListLine, line);
		}

		else if (mnemonics.equals("rcl")) {
			rcl(executionListLine, line);
		}

		else if (mnemonics.equals("rcr")) {
			rcr(executionListLine, line);
		}

		else if (mnemonics.equals("shr")) {
			shr(executionListLine, line);
		}

		else if (mnemonics.equals("shl")) {
			shl(executionListLine, line);
		}

		else if (mnemonics.equals("add") || mnemonics.equals("sub")) {
			addsub(executionListLine, line);
		}

		else if (mnemonics.equals("mov")) {
			mov(executionListLine, line);
		}

		else if (mnemonics.equalsIgnoreCase("inc") || mnemonics.equalsIgnoreCase("dec")) {
			incdec(executionListLine, line);
		}

		else if (mnemonics.equalsIgnoreCase("nop")) {
			return;
		}

		else if (mnemonics.equalsIgnoreCase("cmp")) {
			cmp(executionListLine, line);
		}

		else if (j_instructionList.contains(mnemonics)) {

			if (executionListLine.size() != 2) {
				System.out.println("Error");
				System.exit(99);
			}
			String label = executionListLine.get(1);

			int tempIndex = 0;
			for (int i = 0; i < labels.size(); i++) {
				if (labels.get(i).getName().equals(label)) {
					tempIndex = i;
					break;
				}
			}

			int getLine = labels.get(tempIndex).getLine();

			if (mnemonics.equals("jmp")) {
				instructionPointer = getLine;
			}
			else if (mnemonics.equals("jnz") || mnemonics.equals("jne")) {
				if (!ZF.get()) {
					instructionPointer = getLine;
				}

			}

			else if (mnemonics.equals("je") || mnemonics.equals("jz")) {
				if (ZF.get()) {
					instructionPointer = getLine;
				}

			}

			else if (mnemonics.equals("jnbe") || mnemonics.equals("ja")) {
				if (!ZF.get() && !CF.get()) {
					instructionPointer = getLine;
				}

			}

			else if (mnemonics.equals("jnc") || mnemonics.equals("jae") || mnemonics.equals("jnb")) {
				if (!CF.get()) {
					instructionPointer = getLine;
				}

			}

			else if (mnemonics.equals("jc") || mnemonics.equals("jb") || mnemonics.equals("jnae")) {
				if (CF.get()) {
					instructionPointer = getLine;
				}

			}
			else if (mnemonics.equals("jbe")) {
				if (ZF.get() || CF.get()) {
					instructionPointer = getLine;
				}

			}

		}

	} // end of the execution(List,int) 

	/**
	 * Checks the syntax errors in a detailed way, and prints "Error" if finds any.
	 * 
	 * @param codeLines all lines
	 * @param line      number of the line
	 * @param code      boolean value for indicating that the line under the
	 *                  examination is under the code segment if it's true
	 * @param data      boolean value for indicating that the line under the
	 *                  examination is under the data segment if it's true
	 */
	public static void syntaxError(ArrayList<String> codeLines, int line, boolean code, boolean data) {

		ArrayList<String> tempExecutionListElement = new ArrayList<String>();

		// Blank lines passed
		if (codeLines.size() == 0 && !data) {
			executionList.add(line - 1, tempExecutionListElement);
			return;
		}

		/*
		 * Read,check, and add the instructions to executionList.
		 * [mnemonics-label][leftOperand][rightOperand]
		 */

		// Read, check, and process the instructions
		if (code) {

			String firstToken = codeLines.get(0);

			boolean isLabel = isLabel(codeLines);
			if (isLabel && codeLines.size() == 1) {
				firstToken = firstToken.substring(0, firstToken.length() - 1);
			}

			tempExecutionListElement.add(firstToken);

		   String allTheOperandss = "";

			if (isLabel) {
				allTheOperandss += firstToken;
			}

			else {
				for (int i = 1; i < codeLines.size(); i++) {
					allTheOperandss += codeLines.get(i);
				}
			}

			String secondToken;
			if (!instructionList.stream().anyMatch(firstToken::equalsIgnoreCase)) {
				if (!isLabel) { // labels adding
					System.out.println("Error");
					System.exit(99);
				} //

				Labels temp = new Labels(firstToken, line); // labels with :
				labels.add(temp);
			} else {
				if (!firstToken.equalsIgnoreCase("code"))
					numberOfInstructions++;
			}

			// code segment test
			if (firstToken.equalsIgnoreCase("code")) {
				secondToken = (codeLines.size() == 2) ? codeLines.get(1) : "";
				if (!secondToken.equalsIgnoreCase("segment")) {
					System.out.println("Error");
					System.exit(99);
				}
			}

			// Double commas test
			if (allTheOperandss.matches("(.*),,(.*)")) {
				System.out.println("Error");
				System.exit(99);
			}

			// adding tempLeftOperands to executionList
			String leftOperand = "";
			String rightOperand = "";
			if (!isLabel) {
				int comma = allTheOperandss.indexOf(",");
				if (comma != -1) {
					leftOperand = allTheOperandss.substring(0, comma);
					rightOperand = allTheOperandss.substring(comma + 1);

					tempExecutionListElement.add(leftOperand);
					tempExecutionListElement.add(rightOperand);
				}
				if (comma == -1) {
					tempExecutionListElement.add(allTheOperandss);
				}
			}

			executionList.add(line - 1, tempExecutionListElement);

		}

		/*
		 * Read,check, and store the data.
		 */

		if (data) {

			if (codeLines.size() == 0) {
				return;
			}

			// code ends
			if (codeLines.size() == 2) {
				if (!codeLines.get(0).equalsIgnoreCase("code") || !codeLines.get(1).equalsIgnoreCase("ends")) {
					System.out.println("Error");
					System.exit(99);
				} else {
					return;
				}
			}

			if (codeLines.size() != 3) {
				System.out.println("Error");
				System.exit(99);
			}

			String identifier = codeLines.get(0);

			boolean valid = true;
			if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
				valid = false;
			}
			for (int i1 = 1; i1 < identifier.length(); i1++) {
				if (!Character.isJavaIdentifierPart(identifier.charAt(i1)))
					valid = false;
			}

			if (!valid) {
				System.out.println("Error");
				System.exit(99);
			}
			// isValid variable name ;

			// data type
			String type = codeLines.get(1);
			if (!type.equalsIgnoreCase("db") && !type.equalsIgnoreCase("dw")) {
				System.out.println("Error");
				System.exit(99);
			}

			boolean validSyntax = false;
			boolean isCharacter = false;
			String dataValue = codeLines.get(2);
			Data newMember = null;

			int validNumber = -1;
			char validCharacter = ' ';

			// Character check
			if (dataValue.length() == 3 && (dataValue.startsWith("'") && dataValue.endsWith("'"))
					|| (dataValue.startsWith("\"") && dataValue.endsWith("\""))) {
				validSyntax = true;
				validCharacter = dataValue.charAt(1);
				validNumber = validCharacter;
				isCharacter = true;
			}
			// Character check

			else { // Number check

				String typeOfValue = whichTypeOfNumber(dataValue);

				// Hex number
				if (typeOfValue.contains("error")) {
					System.out.println("Error");
					System.exit(99);
				}

				if (typeOfValue.contains("hexnumber")) {
					if (dataValue.endsWith("h") || dataValue.endsWith("H"))
						dataValue = dataValue.substring(0, dataValue.length() - 1);

					validSyntax = true;
					validNumber = Integer.parseInt(dataValue, 16);

				}

				if (typeOfValue.contains("decnumber")) {
					if (dataValue.endsWith("d") || dataValue.endsWith("D")) // not ending with [dD]
						dataValue = dataValue.substring(0, dataValue.length() - 1);

					validSyntax = true;
					validNumber = Integer.parseInt(dataValue);
				}

				if (type.equalsIgnoreCase("db") && (validNumber < 0 || validNumber > 255)) {
					validSyntax = false;
				}
				if (type.equalsIgnoreCase("dw") && (validNumber < 0 || validNumber > 65535)) {
					validSyntax = false;
				}

			} // Number check

			if (!validSyntax) {
				System.out.println("Error");
				System.exit(99);
			}

			// Creates data variables
			if (isCharacter) {
				newMember = new Data("char", line, validCharacter, type, identifier);
			}

			else {
				newMember = new Data("number", line, validNumber, type, identifier);
			}

			vairablesCheckList.add(identifier.toLowerCase());
			variables.add(newMember);

		}

	}

	/**
	 * Implements the MOV instruction if there are no errors.
	 * 
	 * Firstly, checks the format. If it's malformed, prints an error message.
	 * Secondly, calls whichType method in order to understand which operands are
	 * given to the method in executionListLine parameter. If these operands are
	 * erronous, then whichType method will add an "error" word within the type of
	 * those. After checking their types, too, the process continues.
	 * 
	 * Value in the second parameter is obtained by using appropriateOperand method.
	 * 
	 * At the end, value of the first operand is updated.
	 * 
	 * @param executionListLine the string in the whole current line to be executed
	 * @param line              number of the line
	 */
	public static void mov(ArrayList<String> executionListLine, int line) {

		String destination = "";

		String leftOperand;
		String rightOperand;

		boolean isDestWord = false;
		boolean containsByteFirst = false;

		if (executionListLine.size() != 3) {
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);
		rightOperand = executionListLine.get(2);

		// types are obtained by using whichType method.
		String typeOfLeftOperand = whichType(leftOperand, line);
		String typeOfRightOperand = whichType(rightOperand, line);

		if (!typeOfLeftOperand.contains("memory")
				&& (!typeOfLeftOperand.contains("var") && !typeOfLeftOperand.contains("reg"))) {
			System.out.println("Error");
			System.exit(99);
		}

		if (typeOfLeftOperand.contains("error") || typeOfRightOperand.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		// [bw]
		boolean hasPrefix = false;

		if (typeOfLeftOperand.contains("byte") || typeOfLeftOperand.contains("word")) {
			hasPrefix = true;
			if (typeOfLeftOperand.contains("word"))
				isDestWord = true;
			else if (typeOfLeftOperand.contains("byte")) {
				isDestWord = false;
				containsByteFirst = true;
			}
			typeOfLeftOperand = typeOfLeftOperand.substring(4);
			leftOperand = leftOperand.substring(1);
		}

		int leftOperandInteger = -1;
		boolean isDestMemory = false;
		boolean isDestReg = false;
		boolean isDestVar = false;

		/*
		 * Checks the type of the operand, and keeps going on accordingly. After the
		 * each word in the type, the type variable is updated by taking is substring in
		 * order to exclude the premier word.
		 */
		if (typeOfLeftOperand.contains("memory")) {
			isDestMemory = true;
			typeOfLeftOperand = typeOfLeftOperand.substring(0, typeOfLeftOperand.length() - 6);
			leftOperand = leftOperand.substring(1, leftOperand.length() - 1);
		}

		if (typeOfLeftOperand.equals("hexnumber") || typeOfLeftOperand.equals("decnumber")) {
			leftOperandInteger = returnTheNumber(leftOperand, typeOfLeftOperand);
		}
		boolean isDestWord2 = false;
		if (typeOfLeftOperand.equals("var")) {
			leftOperandInteger = variables.get(vairablesCheckList.indexOf(leftOperand)).getIntValue();
			isDestWord2 = variables.get(vairablesCheckList.indexOf(leftOperand)).isWord() ? true : false;
			if (!isDestMemory)
				isDestVar = true;
			destination = variables.get(vairablesCheckList.indexOf(leftOperand)).getName();
		}

		if (typeOfLeftOperand.equals("reg")) {
			if (returnTheReg(leftOperand) != null)
				leftOperandInteger = returnTheReg(leftOperand).getDecimal();
			if (registers16CheckList.contains(returnTheReg(leftOperand).getName())) {
				if (!isDestWord)
					isDestWord2 = true;

				if (!isDestMemory) {
					isDestReg = true;
					destination = returnTheReg(leftOperand).getName();
				}
			}
			if (registers8CheckList.contains(returnTheReg(leftOperand).getName())) {
				isDestWord2 = false;
				if (!isDestMemory)
					isDestReg = true;
				destination = returnTheReg(leftOperand).getName();
			}
		}

		if (typeOfLeftOperand.equals("char")) {
			leftOperandInteger = (char) leftOperand.charAt(0);
		}

		// value is obtained
		String tempHex = appropriateOperand(rightOperand, typeOfRightOperand, 1);

		int rightOperandInteger = Integer.parseInt(tempHex, 16);

		boolean isSourceWord = false;
		if (typeOfRightOperand.equals("var")) {
			isSourceWord = variables.get(vairablesCheckList.indexOf(rightOperand)).isWord() ? true : false;

		}

		// erroneous forms
		if (isSourceWord && !isDestWord && !isDestWord2) {
			System.out.println("Error");
			System.exit(99);
		}

		if (isSourceWord && !isDestWord2 && hasPrefix) {

			System.out.println("Error");
			System.exit(99);
		}
		if (!isDestWord2 && isSourceWord) {
			System.out.println("Error");
			System.exit(99);
		}

		String number = "";

		if (isDestMemory) {

			if (containsByteFirst && rightOperandInteger > 0xff) {
				System.out.println("Error");
				System.exit(99);
			}

			if (isDestWord) {

				number = Integer.toHexString(rightOperandInteger);

				// adds necessary number of zeros in front
				String s = "";
				for (int i = 0; i < 4 - number.length(); i++)
					s += "0";

				number = s + number;

				String upper = number.substring(0, 2);
				// upper = "0x" + upper ;
				String bottom = number.substring(2);
				// bottom = "0x" + bottom ;

				int u = Integer.parseInt(upper, 16);
				int b = Integer.parseInt(bottom, 16);

				setMemory(leftOperandInteger, b);
				setMemory(leftOperandInteger + 1, u);

			} else {
				number = Integer.toHexString(rightOperandInteger);

				// adds necessary number of zeros in front
				String s = "";
				for (int i = 0; i < 2 - number.length(); i++)
					s += "0";

				number = s + number;

				setMemory(leftOperandInteger, Integer.parseInt(number, 16));

			}

		} // memory dest

		else {

			if (isDestReg) {
				returnTheReg(destination).setValueMov(rightOperandInteger);
			}
			if (isDestVar) {

				variables.get(vairablesCheckList.indexOf(destination)).setValue(rightOperandInteger);
			}
		}

	} // void mov(List,int)

	/**
	 * Implements INC and DEC instructions if there are no errors.
	 * 
	 * First checks the format, then checks the types of operands and finally if
	 * they are also of right form, continues accordingly and increases if the
	 * instruction is INC and decreases if it is DEC.
	 * 
	 * @param executionListLine the string in the whole current line to be executed
	 * @param line              number of the line
	 */
	public static void incdec(ArrayList<String> executionListLine, int line) {
		String mnemonics = "";
		mnemonics = executionListLine.get(0);
		mnemonics = mnemonics.toLowerCase();

		if (executionListLine.size() != 2) {
			System.out.println("Error");
			System.exit(99);
		}
		String tempInctempLeftOperand = executionListLine.get(1);
		tempInctempLeftOperand = tempInctempLeftOperand.toLowerCase();

		String typeOfInctempLeftOperand = whichType(tempInctempLeftOperand, line);

		if (!typeOfInctempLeftOperand.contains("memory") && !typeOfInctempLeftOperand.contains("reg")) {
			System.out.println("Error");
			System.exit(99);
		}

		boolean hasOffsetFirst = false;
		if (tempInctempLeftOperand.contains("offset")) {
			tempInctempLeftOperand = tempInctempLeftOperand.substring(6);
			hasOffsetFirst = true;
		}

		boolean isMemory = hasOffsetFirst;
		boolean asWord = false;
		if (typeOfInctempLeftOperand.contains("word")) {
			asWord = true;
			tempInctempLeftOperand = tempInctempLeftOperand.substring(1);
			typeOfInctempLeftOperand = typeOfInctempLeftOperand.substring(4);
		}
		if (typeOfInctempLeftOperand.contains("byte")) {
			tempInctempLeftOperand = tempInctempLeftOperand.substring(1);
			typeOfInctempLeftOperand = typeOfInctempLeftOperand.substring(4);
		}

		if (typeOfInctempLeftOperand.contains("memory")) {
			if (!hasOffsetFirst)
				tempInctempLeftOperand = tempInctempLeftOperand.substring(1, tempInctempLeftOperand.length() - 1);

			isMemory = true;
			typeOfInctempLeftOperand = typeOfInctempLeftOperand.substring(0, typeOfInctempLeftOperand.length() - 6);
		}

		typeOfInctempLeftOperand = whichType(tempInctempLeftOperand, line);

		int before = -1;
		int MemoryIndex = -1;
		if (typeOfInctempLeftOperand.equals("hexnumber")) {
			if (tempInctempLeftOperand.contains("h"))
				tempInctempLeftOperand = tempInctempLeftOperand.substring(0, tempInctempLeftOperand.length() - 1);
			MemoryIndex = Integer.parseInt(tempInctempLeftOperand, 16);
		}

		if (typeOfInctempLeftOperand.equals("decnumber")) {
			if (tempInctempLeftOperand.contains("d"))
				tempInctempLeftOperand = tempInctempLeftOperand.substring(0, tempInctempLeftOperand.length() - 1);
			MemoryIndex = Integer.parseInt(tempInctempLeftOperand);
		}

		if (typeOfInctempLeftOperand.equals("var")) {
			int tempIndexOfVariable = vairablesCheckList.indexOf(tempInctempLeftOperand);
			MemoryIndex = variables.get(tempIndexOfVariable).getIntValue();
			if (variables.get(tempIndexOfVariable).isWord())
				asWord = true;
			else
				asWord = false;
		}

		if (typeOfInctempLeftOperand.equals("reg")) {
			MemoryIndex = returnTheReg(tempInctempLeftOperand).getDecimal();
			if (registers16CheckList.contains(returnTheReg(tempInctempLeftOperand).getName()))
				asWord = true;
			else
				asWord = false;

		}

		String beforeBinary = "";
		if (isMemory) {
			before = memory[MemoryIndex];
			if (mnemonics.equals("inc")) {
				memory[MemoryIndex]++;
			}
			if (mnemonics.equals("dec")) {
				memory[MemoryIndex]--;
			}
		} else {
			before = MemoryIndex;
			if (mnemonics.equals("inc")) {
				returnTheReg(tempInctempLeftOperand).increment();
			}
			if (mnemonics.equals("dec")) {
				returnTheReg(tempInctempLeftOperand).decrement();
			}
		}

		beforeBinary = Integer.toBinaryString(before);
		setFlags(beforeBinary, "1", before, 1, mnemonics, asWord);

	}

	/**
	 * Implements ADD and SUB instructions if there are no errors.
	 * 
	 * First checks the format, then checks the types of operands and finally if
	 * they are also of right form, continues accordingly and takes the sum of two
	 * operands if the instruction is ADD and subtracts if it is SUB.
	 * 
	 * @param executionListLine the string in the whole current line to be executed
	 * @param line              number of the line
	 */
	public static void addsub(ArrayList<String> executionListLine, int line) {

		String mnemonics = "";
		mnemonics = executionListLine.get(0);

		if (executionListLine.size() != 3) {
			System.out.println("Error");
			System.exit(99);
		}

		String tempLeftOperand = executionListLine.get(1);

		String tempRightOperand = executionListLine.get(2);

		String typeOfLeftOperand = whichType(tempLeftOperand, line);
		String typeOfRightOperand = whichType(tempRightOperand, line);

		if (typeOfLeftOperand.contains("error") || typeOfRightOperand.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		String hexLeft = appropriateOperand(tempLeftOperand, typeOfLeftOperand, line);
		String hexRight = appropriateOperand(tempRightOperand, typeOfRightOperand, line);

		int numLeft = Integer.parseInt(hexLeft, 16);
		int numRight = Integer.parseInt(hexRight, 16);

		boolean asWord = false;
		boolean asByte = false;
		boolean isMemory = false;
		boolean isVar = false;
		boolean isReg = false;

		boolean hasOffset = false;
		int tempIndex = 0;
		if (typeOfLeftOperand.matches("(.*)memory")) {
			isMemory = true;
		}
		if (typeOfLeftOperand.matches("byte(.*)"))
			asByte = true;
		if (typeOfRightOperand.matches("word(.*)"))
			asWord = true;

		if (tempLeftOperand.contains("offset")) {
			hasOffset = true;
			tempLeftOperand = tempLeftOperand.substring(6);
		}

		if (asByte || asWord) {
			tempLeftOperand = tempLeftOperand.substring(1);
		}

		if (isMemory && !hasOffset)
			tempLeftOperand = tempLeftOperand.substring(1, tempLeftOperand.length() - 1);

		if (vairablesCheckList.contains(tempLeftOperand)) {
			tempIndex = vairablesCheckList.indexOf(tempLeftOperand);
			isVar = true;
		}
		if (registers16CheckList.contains(tempLeftOperand)) {
			asWord = true;
			isReg = true;
		}
		if (registers8CheckList.contains(tempLeftOperand)) {
			asByte = true;
			isReg = true;
		}

		if (isMemory) {
			if (asWord) {
				String t = "";
				for (int i = 0; i < 4 - hexLeft.length(); i++)
					t += "0";

				hexLeft = t + hexLeft;
			}
			if (asByte) {
				tempIndex = numLeft;

				numLeft = accessMemory(numLeft);

				hexLeft = Integer.toHexString(numLeft);
				String t = "";
				for (int i = 0; i < 2 - hexLeft.length(); i++)
					t += "0";

				hexLeft = t + hexLeft;
			}

			if (hexRight.length() > hexLeft.length()) {
				System.out.println("Error");
				System.exit(99);
			}
		}

		int addsub = (mnemonics.equals("sub")) ? numLeft - numRight : numLeft + numRight;

		if (isVar && !isMemory)
			variables.get(vairablesCheckList.indexOf(tempLeftOperand)).setValue(addsub);

		else if (isReg && !isMemory)
			returnTheReg(tempLeftOperand).setValue(addsub);

		else {
			if (asWord) {
				String t = Integer.toHexString(addsub);

				if (t.length() > 4) {
					System.out.println("Error");
					System.exit(99);
				}
				String u = t.substring(0, 2);
				String b = t.substring(2);
				if (tempIndex >= 0xffff) {
					System.out.println("Error");
					System.exit(99);
				}

				memory[tempIndex] = (char) Integer.parseInt(b, 16);
				memory[tempIndex + 1] = (char) Integer.parseInt(u, 16);

			}
			if (asByte) {
				String t = Integer.toHexString(addsub);
				if (t.length() > 2) {
					System.out.println("Error");
					System.exit(99);
				}

				memory[tempIndex] = (char) Integer.parseInt(t, 16);
			}

		}

		hexLeft = Integer.toBinaryString(Integer.parseInt(hexLeft, 16));
		hexRight = Integer.toBinaryString(Integer.parseInt(hexRight, 16));

		setFlags(hexLeft, hexRight, numLeft, numRight, mnemonics, asWord);

	}

	/**
	 * Implements CMP instruction if there are no errors.
	 * 
	 * After checking the format and everything, takes the values of the operands.
	 * Since flags must be updated after this method, then sends these values into
	 * setFlags method as parameters with also the parameter "cmp" in order to
	 * declare that this is a CMP operation.
	 * 
	 * setFlags method completes the process with the given parameters.
	 * 
	 * @param executionListLine the string in the whole current line to be executed
	 * @param line              number of the line
	 */
	public static void cmp(ArrayList<String> executionListLine, int line) {
		String leftOperand = "";
		String rightOperand = "";
		if (executionListLine.size() != 3) {
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);
		rightOperand = executionListLine.get(2);

		String typeOfLeftOperand1 = whichType(leftOperand, line);
		String typeOfRightOperand1 = whichType(rightOperand, line);

		if (typeOfLeftOperand1.contains("error") || typeOfRightOperand1.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		String hexLeft = appropriateOperand(leftOperand, typeOfLeftOperand1, line);
		String hexRight = appropriateOperand(rightOperand, typeOfRightOperand1, line);

		int left = Integer.parseInt(hexLeft, 16);
		int right = Integer.parseInt(hexRight, 16);

		hexLeft = Integer.toBinaryString(Integer.parseInt(hexLeft, 16));
		hexRight = Integer.toBinaryString(Integer.parseInt(hexRight, 16));

		setFlags(hexLeft, hexRight, left, right, "cmp", true);

	}

	public static void pop(String destination) {

		String typeOfdestination = whichType(destination, 1);
		if (!typeOfdestination.equals("reg")) {
			System.out.println("Error");
			System.exit(99);
		}

		if (!registers16CheckList.contains(destination)) {
			System.out.println("Error");
			System.exit(99);
		}

		SP.setValue(SP.getDecimal() + 2);
		String upper = Integer.toHexString(accessMemory(SP.getDecimal() + 1));
		String bottom = Integer.toHexString(accessMemory(SP.getDecimal()));
		upper = signExtended(upper, 2);
		bottom = signExtended(bottom, 2);

		returnTheReg(destination).setRegValue("0x" + upper + bottom);

	}

	public static void push(String source) {
		if (source.startsWith("offset")) {
			source = source.substring(6);
		}

		String typeOfSource = whichType(source, 1);
		String hexNumber = appropriateOperand(source, typeOfSource, 1);

		if (!typeOfSource.equals("reg")) {
			System.out.println("Error");
			System.exit(99);
		}

		if (!registers16CheckList.contains(source)) {
			System.out.println("Error");
			System.exit(99);
		}
		String s = "";
		for (int i = 0; i < 4 - hexNumber.length(); i++) {
			s += 0;
		}
		hexNumber = s + hexNumber;

		String upper = hexNumber.substring(0, 2);
		String bottom = hexNumber.substring(2);

		setMemory(SP.getDecimal() + 1, Integer.parseInt(upper, 16));
		setMemory(SP.getDecimal(), Integer.parseInt(bottom, 16));
		SP.setValue(SP.getDecimal() - 2);

	}

	public static void div(String operand) {

		String dividendHex = "";
		String divisorHex = "";
		int divisor = 0;
		int dividendNumber = 0;
		int remainder = -1;
		int quotient = -1;

		if (registers16CheckList.contains(operand)) {
			String dxHex = DX.getRegValue().substring(2);
			String axHex = AX.getRegValue().substring(2);

			dividendHex = dxHex + axHex;

			dividendNumber = Integer.parseInt(dividendHex, 16);

			divisorHex = returnTheReg(operand).getRegValue().substring(2);
			divisor = Integer.parseInt(divisorHex, 16);

			quotient = dividendNumber / divisor;
			remainder = dividendNumber % divisor;

			String tempBinaryStringForAX = Integer.toBinaryString(quotient);
			if (tempBinaryStringForAX.length() > 16) {
				System.out.println("Error");
				System.exit(99);
			}

			AX.setValue(quotient);
			DX.setValue(remainder);
		} else if (registers8CheckList.contains(operand)) {
			String axHex = AX.getRegValue().substring(2);

			dividendHex = axHex;

			dividendNumber = Integer.parseInt(dividendHex, 16);

			divisorHex = returnTheReg(operand).getRegValue().substring(2);
			divisor = Integer.parseInt(divisorHex, 16);

			quotient = dividendNumber / divisor;
			remainder = dividendNumber % divisor;

			String tempBinaryStringForAL = Integer.toBinaryString(quotient);
			if (tempBinaryStringForAL.length() > 8) {
				System.out.println("Error");
				System.exit(99);
			}

			AL.setValue(quotient);
			AH.setValue(remainder);

		}

	}

	public static void mul(String operand) {

		if (registers16CheckList.contains(operand)) {

			int sourceNumber = returnTheReg(operand).getDecimal();
			int destNumber = AX.getDecimal();

			destNumber *= sourceNumber;

			String tempBinaryString = Integer.toBinaryString(destNumber);

			String hexDX = "";
			String hexAX = "";

			if (tempBinaryString.length() <= 16) {
				CF.set(false);
				OF.set(false);
				hexAX = Integer.toHexString(Integer.parseInt(tempBinaryString, 2));

				String s = "";
				for (int i = 0; i < 4 - hexAX.length(); i++)
					s += "0";
				hexAX = s + hexAX;

				AX.setRegValue("0x" + hexAX);
				DX.setRegValue("0x0000");
			} else {
				int length = tempBinaryString.length();
				CF.set(true);
				OF.set(true);
				hexDX = Integer.toHexString(Integer.parseInt(tempBinaryString.substring(0, length - 16), 2));
				hexAX = Integer.toHexString(Integer.parseInt(tempBinaryString.substring(length - 16), 2));

				String s = "";
				for (int i = 0; i < 4 - hexDX.length(); i++)
					s += "0";
				hexDX = "0x" + s + hexDX;
				s = "";

				for (int i = 0; i < 4 - hexAX.length(); i++)
					s += "0";

				hexAX = "0x" + s + hexAX;

				AX.setRegValue(hexAX);
				DX.setRegValue(hexDX);
			}

		}

		else if (registers8CheckList.contains(operand)) {
			int sourceNumber = returnTheReg(operand).getDecimal();
			int destNumber = AL.getDecimal();

			destNumber *= sourceNumber;

			String tempBinaryString = Integer.toBinaryString(destNumber);
			String hexAH = "";
			String hexAL = "";

			if (tempBinaryString.length() <= 8) {
				CF.set(false);
				OF.set(false);
				hexAL = Integer.toHexString(Integer.parseInt(tempBinaryString, 2));
				AL.setRegValue("0X" + hexAL);
				AH.setRegValue("0x00");
			} else {
				CF.set(true);
				OF.set(true);
				hexAH = Integer.toHexString(Integer.parseInt(tempBinaryString.substring(8), 2));
				hexAL = Integer.toHexString(Integer.parseInt(tempBinaryString.substring(0, 8), 2));
				hexAH = "0x" + hexAH;
				hexAL = "0x" + hexAL;

				String s = "";
				for (int i = 0; i < 2 - hexAH.length(); i++)
					s += "0";
				hexAH = s + hexAH;

				AH.setRegValue(hexAH);
				AL.setRegValue(hexAL);

			}

		}

	}

	/*
	 * These three methods below are used for applying binary logic operations to
	 * two binary strings. Results are returned as an integer by converting the
	 * final binary string.
	 * 
	 * notBinary method is though, used for applying binary "not" operation to the
	 * given hexadecimal string, it also returns the final result as an integer.
	 */

	/**
	 * Takes two binary strings and applies "and" operation to those, returns the
	 * result as an integer by converting the final binary string.
	 * 
	 * @param binaryFirst  first operand
	 * @param binarySecond second operand
	 * @return converts the final binary string to integer and returns that value.
	 */
	private static int andBinaries(String binaryFirst, String binarySecond) {
		if (binarySecond.length() < binaryFirst.length()) {
			binarySecond = signExtended(binarySecond, binaryFirst.length());
		}

		else if (binaryFirst.length() < binarySecond.length()) {
			binaryFirst = signExtended(binaryFirst, binarySecond.length());
		}

		String newBinaryFirst = "";
		for (int i = 0; i < binaryFirst.length(); i++) {
			if (binaryFirst.charAt(i) == '1' && binarySecond.charAt(i) == '1') {
				newBinaryFirst += '1';

			}

			else {
				newBinaryFirst += '0';
			}
		}

		int newDecimalFirst = Integer.parseUnsignedInt(newBinaryFirst, 2);
		return newDecimalFirst;
	}

	private static int orBinaries(String binaryFirst, String binarySecond) {

		if (binarySecond.length() < binaryFirst.length()) {
			binarySecond = signExtended(binarySecond, binaryFirst.length());
		}

		else if (binaryFirst.length() < binarySecond.length()) {
			binaryFirst = signExtended(binaryFirst, binarySecond.length());
		}

		String newBinaryFirst = "";
		for (int i = 0; i < binaryFirst.length(); i++) {
			if (binaryFirst.charAt(i) == '0' && binarySecond.charAt(i) == '0') {
				newBinaryFirst += '0';

			}

			else {
				newBinaryFirst += '1';
			}
		}

		int newDecimalFirst = Integer.parseUnsignedInt(newBinaryFirst, 2);
		return newDecimalFirst;
	}

	private static int xorBinaries(String binaryFirst, String binarySecond) {
		if (binarySecond.length() < binaryFirst.length()) {
			binarySecond = signExtended(binarySecond, binaryFirst.length());
		}

		else if (binaryFirst.length() < binarySecond.length()) {
			binaryFirst = signExtended(binaryFirst, binarySecond.length());
		}

		int length = binaryFirst.length();
		String newBinaryFirst = "";
		for (int i = 0; i < length; i++) {
			if (binaryFirst.charAt(i) == '1' && binarySecond.charAt(i) == '1') {
				newBinaryFirst += '0';

			}

			else if (binaryFirst.charAt(i) == '0' && binarySecond.charAt(i) == '0') {
				newBinaryFirst += '0';
			}

			else {
				newBinaryFirst += '1';
			}
		}

		int newDecimalFirst = Integer.parseUnsignedInt(newBinaryFirst, 2);
		return newDecimalFirst;
	}

	/**
	 * Takes a hexadecimal string, converts it to a binary string. Applies "not"
	 * operation to that string and converts the result to integer. Returns that
	 * final integer value.
	 * 
	 * @param hex the hexadecimal number to be applied the not operation on.
	 * @return converts final binary string to integer and returns that value.
	 */
	private static int notBinary(String hex) {

		int decimal = Integer.parseUnsignedInt(hex, 16);
		String binary = Integer.toBinaryString(decimal);

		String newBinary = "";
		for (int i = 0; i < binary.length(); i++) {
			if (binary.charAt(i) == '1') {
				newBinary += '0';
			}

			else {
				newBinary += '1';
			}
		}

		int hexDigits = hex.length();

		int totalBits = hexDigits * 4;

		int binaryLength = binary.length();

		for (int i = 0; i < totalBits - binaryLength; i++) {
			newBinary = '1' + newBinary;
		}

		int newDecimal = Integer.parseUnsignedInt(newBinary, 2);
		return newDecimal;
	}

	/*
	 * execution method calls these four methods below, and they call the four
	 * methods above accordingly. Then sends the result to mov method as a
	 * parameter.
	 */

	/**
	 * Implements AND instruction if there are no errors.
	 * 
	 * After checking the format and everything, takes the values of the operands.
	 * Converts these values to binary strings and sends them to andBinaries method
	 * as parameters. Takes the final result as an integer back, and changes the
	 * first operand's value with that final result by calling the method mov with
	 * that value as a parameter at the end.
	 * 
	 * 
	 * @param executionListLine the string in the whole current line to be executed
	 * @param line              number of the line
	 */

	private static void and(ArrayList<String> executionListLine, int line) {

		ArrayList<String> tempExecutionList = new ArrayList<>();

		String leftOperand = "";
		String rightOperand = "";
		if (executionListLine.size() != 3) { // {cmp,ltempLeftOperand,rtempLeftOperand}
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);
		rightOperand = executionListLine.get(2);

		String typeOfLeftOperand1 = whichType(leftOperand, line);
		String typeOfRightOperand1 = whichType(rightOperand, line);

		if (typeOfLeftOperand1.contains("error") || typeOfRightOperand1.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		String hexLeft = appropriateOperand(leftOperand, typeOfLeftOperand1, line);
		String hexRight = appropriateOperand(rightOperand, typeOfRightOperand1, line);

		int left = Integer.parseInt(hexLeft, 16);
		int right = Integer.parseInt(hexRight, 16);

		String binaryLeft = Integer.toBinaryString(left);
		String binaryRight = Integer.toBinaryString(right);

		tempExecutionList.add("mov");
		tempExecutionList.add(leftOperand);
		tempExecutionList.add(andBinaries(binaryLeft, binaryRight) + "");
		mov(tempExecutionList, line);

	}

	private static void or(ArrayList<String> executionListLine, int line) {
		ArrayList<String> tempExecutionList = new ArrayList<>();

		String leftOperand = "";
		String rightOperand = "";
		if (executionListLine.size() != 3) {
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);
		rightOperand = executionListLine.get(2);

		String typeOfLeftOperand1 = whichType(leftOperand, line);
		String typeOfRightOperand1 = whichType(rightOperand, line);

		if (typeOfLeftOperand1.contains("error") || typeOfRightOperand1.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		String hexLeft = appropriateOperand(leftOperand, typeOfLeftOperand1, line);
		String hexRight = appropriateOperand(rightOperand, typeOfRightOperand1, line);

		int left = Integer.parseInt(hexLeft, 16);
		int right = Integer.parseInt(hexRight, 16);

		String binaryLeft = Integer.toBinaryString(left);
		String binaryRight = Integer.toBinaryString(right);

		tempExecutionList.add("mov");
		tempExecutionList.add(leftOperand);
		tempExecutionList.add(orBinaries(binaryLeft, binaryRight) + "");
		mov(tempExecutionList, line);

	}

	private static void xor(ArrayList<String> executionListLine, int line) {
		ArrayList<String> tempExecutionList = new ArrayList<>();

		String leftOperand = "";
		String rightOperand = "";
		if (executionListLine.size() != 3) {
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);
		rightOperand = executionListLine.get(2);

		String typeOfLeftOperand1 = whichType(leftOperand, line);
		String typeOfRightOperand1 = whichType(rightOperand, line);

		if (typeOfLeftOperand1.contains("error") || typeOfRightOperand1.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		String hexLeft = appropriateOperand(leftOperand, typeOfLeftOperand1, line);
		String hexRight = appropriateOperand(rightOperand, typeOfRightOperand1, line);

		int left = Integer.parseInt(hexLeft, 16);
		int right = Integer.parseInt(hexRight, 16);

		String binaryLeft = Integer.toBinaryString(left);
		binaryLeft = signExtended(binaryLeft, 16);
		String binaryRight = Integer.toBinaryString(right);
		binaryRight = signExtended(binaryRight, 16);
		int value = xorBinaries(binaryLeft, binaryRight);
		String result = Integer.toBinaryString(value);
		result = signExtended(result, 16);
		if (value == 0)
			ZF.set(true);
		else
			ZF.set(false);
		OF.set(false);
		CF.set(false);
		if (registers16CheckList.contains(leftOperand)) {
			if (result.charAt(0) == '0')
				SF.set(false);
			else
				SF.set(true);
		} else {
			if (result.charAt(8) == '0')
				SF.set(false);
			else
				SF.set(true);

		}

		tempExecutionList.add("mov");
		tempExecutionList.add(leftOperand);
		tempExecutionList.add(value + "");
		mov(tempExecutionList, line);

	}

	private static void not(ArrayList<String> executionListLine, int line) {
		ArrayList<String> tempExecutionList = new ArrayList<>();

		String leftOperand = "";
		if (executionListLine.size() != 2) {
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);

		String typeOfLeftOperand1 = whichType(leftOperand, line);

		if (typeOfLeftOperand1.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		String hexLeft = appropriateOperand(leftOperand, typeOfLeftOperand1, line);

		tempExecutionList.add("mov");
		tempExecutionList.add(leftOperand);
		tempExecutionList.add(notBinary(hexLeft) + "");
		mov(tempExecutionList, line);
	}

	/**
	 * Implements RCL instruction if there are no errors.
	 * 
	 * After checking the format and everything, takes the values of the operands.
	 * Takes the hexdecimal string of the first operand and sends it with number of
	 * rotation to rclBinary method as parameters. Takes the final result as an
	 * integer back, and changes the first operand's value with that final result by
	 * calling the method mov with that value as a parameter at the end.
	 * 
	 * @param executionListLine the string in the whole current line to be executed
	 * @param line              number of the line
	 */
	private static void rcl(ArrayList<String> executionListLine, int line) {

		ArrayList<String> tempExecutionList = new ArrayList<>();

		String leftOperand = "";
		String rightOperand = "";
		if (executionListLine.size() != 3) {
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);
		rightOperand = executionListLine.get(2);

		String typeOfLeftOperand1 = whichType(leftOperand, line);
		String typeOfRightOperand1 = whichType(rightOperand, line);

		if (typeOfLeftOperand1.contains("error") || typeOfRightOperand1.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		String hexLeft = appropriateOperand(leftOperand, typeOfLeftOperand1, line);
		String hexRight = appropriateOperand(rightOperand, typeOfRightOperand1, line);

		int right = Integer.parseInt(hexRight, 16);

		tempExecutionList.add("mov");
		tempExecutionList.add(leftOperand);
		tempExecutionList.add(rclBinary(hexLeft, right) + "");

		mov(tempExecutionList, line);

	}

	/**
	 * Takes a hexadecimal number, converts it to binary and rotates it in the left
	 * direction with the Carry Flag, converts the result into integer and returns
	 * it.
	 * 
	 * @param hex    hexadecimal number to be rotated
	 * @param rotate number of rotations
	 * @return converts the resulting string to integer and returns it.
	 */
	private static int rclBinary(String hex, int rotate) {

		int decimal = Integer.parseUnsignedInt(hex, 16);
		String binary = Integer.toBinaryString(decimal);

		int hexDigits = hex.length();

		int totalBits = hexDigits * 4;

		int binaryInitialLength = binary.length();

		for (int i = 0; i < totalBits - binaryInitialLength; i++) {
			binary = '0' + binary;
		}

		int length = binary.length();
		rotate = rotate % length;

		for (int i = 0; i < rotate; i++) {

			String character = binary.substring(0, 1);
			String tempResult = "";
			tempResult = binary.substring(1);

			if (CF.get()) {
				tempResult += "1";
			} else {
				tempResult += "0";
			}
			;// character;
			if (character.equals("1"))
				CF.set(true);
			else
				CF.set(false);

			binary = tempResult;

		}

		return Integer.parseUnsignedInt(binary, 2);
	}

	// sends binary value of the first operand and rotation number to rcrBinary
	// method
	private static void rcr(ArrayList<String> executionListLine, int line) {

		ArrayList<String> tempExecutionList = new ArrayList<>();

		String leftOperand = "";
		String rightOperand = "";
		if (executionListLine.size() != 3) {
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);
		rightOperand = executionListLine.get(2);

		String typeOfLeftOperand1 = whichType(leftOperand, line);
		String typeOfRightOperand1 = whichType(rightOperand, line);

		if (typeOfLeftOperand1.contains("error") || typeOfRightOperand1.contains("error")) {

			System.exit(99);
		}

		String hexLeft = appropriateOperand(leftOperand, typeOfLeftOperand1, line);
		String hexRight = appropriateOperand(rightOperand, typeOfRightOperand1, line);

		int right = Integer.parseInt(hexRight, 16);

		tempExecutionList.add("mov");
		tempExecutionList.add(leftOperand);
		tempExecutionList.add(rcrBinary(hexLeft, right) + "");

		mov(tempExecutionList, line);

	}

	// converts the resulting binary string to int and returns it
	private static int rcrBinary(String hex, int rotate) {

		int decimal = Integer.parseUnsignedInt(hex, 16);
		String binary = Integer.toBinaryString(decimal);

		int hexDigits = hex.length();

		int totalBits = hexDigits * 4;

		int binaryInitialLength = binary.length();

		for (int i = 0; i < totalBits - binaryInitialLength; i++) {
			binary = '0' + binary;
		}

		int length = binary.length();
		rotate = rotate % length;

		for (int i = 0; i < rotate; i++) {

			String character = binary.substring(length - 1);

			String tempResult = "";
			tempResult = binary.substring(0, length - 1);

			if (CF.get()) {
				tempResult = "1" + tempResult;
			} else {
				tempResult = "0" + tempResult;
			}
			// character;
			if (character.equals("1"))
				CF.set(true);
			else
				CF.set(false);

			binary = tempResult;
		}

		return Integer.parseUnsignedInt(binary, 2);

	}

	// implements SHL instruction, and updates the necessary flags
	private static void shl(ArrayList<String> executionListLine, int line) {

		ArrayList<String> tempExecutionList = new ArrayList<>();

		String leftOperand = "";
		String rightOperand = "";
		if (executionListLine.size() != 3) {
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);
		rightOperand = executionListLine.get(2);

		String typeOfLeftOperand1 = whichType(leftOperand, line);
		String typeOfRightOperand1 = whichType(rightOperand, line);

		if (typeOfLeftOperand1.contains("error") || typeOfRightOperand1.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		String hexLeft = appropriateOperand(leftOperand, typeOfLeftOperand1, line);
		String hexRight = appropriateOperand(rightOperand, typeOfRightOperand1, line);

		int left = Integer.parseInt(hexLeft, 16);
		int right = Integer.parseInt(hexRight, 16);
		String tempBinaryString = Integer.toBinaryString(left);

		int size = (returnTheReg(leftOperand).getRegValue().length() - 2) * 4;

		String s = "";
		for (int i = 0; i < size - tempBinaryString.length(); i++) {
			s += "0";
		}
		tempBinaryString = s + tempBinaryString;

		String zeros = "";
		for (int a = 0; a < right; a++)
			zeros += "0";

		char carryFlag = tempBinaryString.charAt(right - 1);
		tempBinaryString = tempBinaryString.substring(right);

		tempBinaryString = tempBinaryString + zeros;

		if (carryFlag == '0')
			CF.set(false);
		else {
			CF.set(true);
		}

		int value = Integer.parseInt(tempBinaryString, 2);

		tempExecutionList.add("mov");

		tempExecutionList.add(leftOperand);
		tempExecutionList.add("" + value);

		mov(tempExecutionList, line);
	}

	private static void shr(ArrayList<String> executionListLine, int line) {
		ArrayList<String> tempExecutionList = new ArrayList<>();

		String leftOperand = "";
		String rightOperand = "";
		if (executionListLine.size() != 3) {
			System.out.println("Error");
			System.exit(99);
		}

		leftOperand = executionListLine.get(1);
		rightOperand = executionListLine.get(2);

		String typeOfLeftOperand1 = whichType(leftOperand, line);
		String typeOfRightOperand1 = whichType(rightOperand, line);

		if (typeOfLeftOperand1.contains("error") || typeOfRightOperand1.contains("error")) {
			System.out.println("Error");
			System.exit(99);
		}

		String hexLeft = appropriateOperand(leftOperand, typeOfLeftOperand1, line);
		String hexRight = appropriateOperand(rightOperand, typeOfRightOperand1, line);

		int left = Integer.parseInt(hexLeft, 16);
		int right = Integer.parseInt(hexRight, 16);

		String tempBinaryString = Integer.toBinaryString(left);

		int size = (returnTheReg(leftOperand).getRegValue().length() - 2) * 4;

		String s = "";
		for (int i = 0; i < size - tempBinaryString.length(); i++) {
			s += "0";
		}
		tempBinaryString = s + tempBinaryString;

		String zeros = "";
		for (int a = 0; a < right; a++)
			zeros += "0";

		char carryFlag = tempBinaryString.charAt(size - right);
		tempBinaryString = tempBinaryString.substring(0, size - right);

		tempBinaryString = zeros + tempBinaryString;

		if (carryFlag == '0')
			CF.set(false);
		else {
			CF.set(true);
		}

		int value = Integer.parseInt(tempBinaryString, 2);

		tempExecutionList.add("mov");
//		System.out.println("left operand " + leftOperand + "  value === " + value);
		tempExecutionList.add(leftOperand);
		tempExecutionList.add("" + value);

		mov(tempExecutionList, line);
	}

	/**
	 * Takes the type of the number and returns it as an integer.
	 * 
	 * @param temp value is within this string
	 * @param type number's type
	 * @return the value of the given number type as an integer
	 */

	public static int returnTheNumber(String temp, String type) {

		if (type.equals("hexnumber")) {
			if (temp.endsWith("h")) {
				temp = temp.substring(0, temp.length() - 1);
				return Integer.parseInt(temp, 16);
			}
			if (temp.startsWith("0")) {
				return Integer.parseInt(temp, 16);
			}

		}

		if (type.equals("decnumber")) {
			if (temp.endsWith("d"))
				temp = temp.substring(0, temp.length() - 1);

			return Integer.parseInt(temp);
		}

		return -1;

	}

	/**
	 * Increases a binary number's bits by simply extending its sign bits as
	 * demanded.
	 * 
	 * @param temp binary number to put into the sign extended form
	 * @param x    amount of bits demanded
	 * @return the sign extended binary string is returned
	 */
	public static String signExtended(String temp, int x) {
		String rt = "";
		for (int i = 0; i < x - temp.length(); i++)
			rt += "0";

		rt += temp;
		return rt;
	}

	/**
	 * Updates the values in the flags depending on the instruction, and operands.
	 * Also, some operations take place under this method so that it can decide
	 * whether the flag will be updated to 0 or 1 for example.
	 * 
	 * @param leftB     binary form of the first operand
	 * @param rightB    binary form of the second operand
	 * @param left      first operand
	 * @param right     second operand
	 * @param mnemonics instruction
	 * @param isWord    a boolean value on whether it's a word or not
	 */
	public static void setFlags(String leftB, String rightB, int left, int right, String mnemonics, boolean isWord) {

		int tempSize = 0;
		if (isWord)
			tempSize = 16;
		else
			tempSize = 8;
		leftB = signExtended(leftB, tempSize);
		rightB = signExtended(rightB, tempSize);

		if (mnemonics.equals("add") || mnemonics.equals("inc")) {

			int sum = left + right;
			String tempSumBinary = Integer.toBinaryString(sum);
			tempSumBinary = signExtended(tempSumBinary, tempSize);

			if (tempSumBinary.length() > tempSize) {
				if (!mnemonics.equals("inc"))
					CF.set(true);
			} else {
				CF.set(false);
			}

			if (leftB.charAt(0) == rightB.charAt(0) && leftB.charAt(0) != tempSumBinary.charAt(0))
				OF.set(true);
			else
				OF.set(false);

			if (sum == 0)
				ZF.set(true);
			else
				ZF.set(false);

			if (tempSumBinary.charAt(0) == 1)
				SF.set(true);
			else
				SF.set(false);

			String hexL = Integer.toHexString(left);
			String hexR = Integer.toHexString(right);
			int leftFirstNibble = Integer.parseInt(("" + hexL.charAt(hexL.length() - 1)), 16);
			int rightFirstNibble = Integer.parseInt(("" + hexR.charAt(hexR.length() - 1)), 16);

			if (leftFirstNibble < rightFirstNibble || leftFirstNibble + rightFirstNibble > 15) {
				AF.set(true);
			} else {
				AF.set(false);
			}

		} // add,inc

		if (mnemonics.equals("cmp") || mnemonics.equals("dec") || mnemonics.equals("sub")) {
			if (left < right) { // for subtraction
				if (!mnemonics.equals("dec"))
					CF.set(true);
			}

			else if (left > right) {
				if (!mnemonics.equals("dec"))
					CF.set(false);
			}
			if (left - right == 0) {
				ZF.set(true);
			} else {
				ZF.set(false);
			}

			int tempResult = left - right;
			String tempBinaryResult = Integer.toBinaryString(tempResult);
			tempBinaryResult = signExtended(tempBinaryResult, tempSize);

			if (leftB.charAt(0) == rightB.charAt(0) && leftB.charAt(0) != tempBinaryResult.charAt(0))
				OF.set(true);
			else
				OF.set(false);

			if (tempBinaryResult.charAt(0) == 1)
				SF.set(true);
			else
				SF.set(false);

			String hexL = Integer.toHexString(left);
			String hexR = Integer.toHexString(right);

			int leftFirstNibble = Integer.parseInt(("" + hexL.charAt(hexL.length() - 1)), 16);
			int rightFirstNibble = Integer.parseInt(("" + hexR.charAt(hexR.length() - 1)), 16);

			if (leftFirstNibble < rightFirstNibble || leftFirstNibble + rightFirstNibble > 15) {
				AF.set(true);
			} else {
				AF.set(false);
			}

		} // cmp,dec,sub

	} // void setFlags(String,String)

	/**
	 * Returns the corresponding Registers object to the given string. Returns null,
	 * if there is none.
	 * 
	 * @param reg name of the register as a string
	 * @return register object of that corresponding string is returned, null is
	 *         returned if there is none.
	 */
	public static Registers returnTheReg(String reg) {
		if (reg.equals("ax")) {
			return AX;
		}
		if (reg.equals("bx")) {
			return BX;
		}
		if (reg.equals("cx")) {
			return CX;
		}
		if (reg.equals("dx")) {
			return DX;
		}
		if (reg.equals("ah")) {
			return AH;
		}
		if (reg.equals("bh")) {
			return BH;
		}
		if (reg.equals("ch")) {
			return CH;
		}
		if (reg.equals("dh")) {
			return DH;
		}
		if (reg.equals("al")) {
			return AL;
		}
		if (reg.equals("bl")) {
			return BL;
		}
		if (reg.equals("cl")) {
			return CL;
		}
		if (reg.equals("dl")) {
			return DL;
		}
		if (reg.equals("bp")) {
			return BP;
		}
		if (reg.equals("sp")) {
			return SP;
		}
		if (reg.equals("di")) {
			return DI;
		}
		if (reg.equals("si")) {
			return SI;
		}

		return null;

	}

	/**
	 * After the memory error check, given value is put to the given index in the
	 * memory array.
	 * 
	 * @param index to put the value at
	 * @param value to be put in the memory
	 */
	public static void setMemory(int index, int value) {
		if (index < 6 * numberOfInstructions && value < 0 || value > 255) {
			System.out.println("Error");
			System.exit(99);
		}
		memory[index] = (char) value;
	}

	// to access a given index in the memory
	public static int accessMemory(int index) {
		// System.out.println(index);
		if (index < numberOfInstructions * 6 || index > 0xffff) {
			System.out.println("Error");
			System.exit(99);
		}

		return memory[index];
	}

	/**
	 * When given a well formed operand, examines it for further mistakes, and
	 * returns it's hex value at the end.
	 * 
	 * @param operand       to be checked and wanted to get its hex value
	 * @param typeOfOperand the type of the operand obtained beforehand
	 * @param line          number of the line
	 * @return the number as hexadecimal string
	 */
	public static String appropriateOperand(String operand, String typeOfOperand, int line) {
		boolean isMemory = false;
		boolean asWord = false;
		boolean asByte = false;

		if (operand.contains("offset")) {
			operand = operand.substring(6);
			typeOfOperand = typeOfOperand.substring(6);
			if (typeOfOperand.equals("var")) {
				int tempIndexOfVariable = vairablesCheckList.indexOf(operand);
				int returnTheOffset = variables.get(tempIndexOfVariable).getOffset();
				String hex = Integer.toHexString(returnTheOffset);
				return hex;
			} else {
				System.out.println("Error");
				System.exit(99);
			}
		}

		if (typeOfOperand.contains("word")) {
			asWord = true;
			operand = operand.substring(1);
			typeOfOperand = typeOfOperand.substring(4);
		}
		if (typeOfOperand.contains("byte")) {
			asByte = true;
			operand = operand.substring(1);
			typeOfOperand = typeOfOperand.substring(4);
		}

		if (typeOfOperand.contains("memory")) {
			operand = operand.substring(1, operand.length() - 1);
			isMemory = true;
			typeOfOperand = typeOfOperand.substring(0, typeOfOperand.length() - 6);
		}

		typeOfOperand = whichType(operand, line);

		int MemoryIndex = -1;

		if (typeOfOperand.equals("hexnumber")) {
			if (operand.contains("h"))
				operand = operand.substring(0, operand.length() - 1);

			MemoryIndex = Integer.parseInt(operand, 16);
		}

		if (typeOfOperand.equals("decnumber")) {
			if (operand.endsWith("d"))
				operand = operand.substring(0, operand.length() - 1);

			MemoryIndex = Integer.parseInt(operand);
		}

		if (typeOfOperand.equals("var")) {

			int tempIndexOfVariable = vairablesCheckList.indexOf(operand);

			MemoryIndex = variables.get(tempIndexOfVariable).getIntValue();
			if (variables.get(tempIndexOfVariable).isWord())
				asWord = true;
			else
				asWord = false;
		}

		if (typeOfOperand.equals("reg")) {
			MemoryIndex = returnTheReg(operand).getDecimal();
		}

		if (typeOfOperand.equals("char")) {
			operand = operand.substring(1, operand.length() - 1);
			MemoryIndex = operand.charAt(0);
		}

		String tempNumber = "";
		if (isMemory) {
			if (asWord) {
				String tempUp = Integer.toHexString(accessMemory(MemoryIndex + 1));
				String tempBottom = Integer.toHexString(accessMemory(MemoryIndex));

				String s = "";
				for (int i = 0; i < 2 - tempUp.length(); i++)
					s += "0";
				tempUp = s + tempUp;
				s = "";
				for (int i = 0; i < 2 - tempBottom.length(); i++)
					s += "0";
				tempBottom = s + tempBottom;

				return tempUp + tempBottom;
			} else if (asByte) {
				tempNumber = "" + Integer.toHexString(accessMemory(MemoryIndex));
				if (tempNumber.length() == 2)
					return tempNumber;
				if (tempNumber.length() == 1)
					return "0" + tempNumber;
			} else {
				tempNumber = "" + Integer.toHexString(accessMemory(MemoryIndex));
				if (tempNumber.length() == 2)
					return tempNumber;
				if (tempNumber.length() == 1)
					return "0" + tempNumber;
			}
		}

		else {

			String temp = Integer.toHexString(MemoryIndex);

			String s = "";
			if (asWord) {
				for (int i = 0; i < 4 - temp.length(); i++)
					s += "0";
				return s + temp;
			}

			else {
				for (int i = 0; i < 2 - temp.length(); i++)
					s += "0";
				return s + temp;
			}

		} //

		return "";

	}

	/**
	 * Instantiations of all registers and flags take place under this method and
	 * given lists are filled.
	 */
	public static void instantiation() {
		// general registerList
		// registerList AX = new registerList("0x0000") ;
		AX = new Registers("0x0000", "ax");
		AH = new Registers("0x00", "ah");
		AL = new Registers("0x00", "al");
		BX = new Registers("0x0000", "bx");
		BH = new Registers("0x00", "bh");
		BL = new Registers("0x00", "bl");
		CX = new Registers("0x0000", "cx");
		CH = new Registers("0x00", "ch");
		CL = new Registers("0x00", "cl");
		DX = new Registers("0x0000", "dx");
		DH = new Registers("0x00", "dh");
		DL = new Registers("0x00", "dl");

		AX.setUpper(AH);
		AX.setLower(AL);
		AH.setParent(AX);
		AL.setParent(AX);
		BX.setUpper(BH);
		BX.setLower(BL);
		BH.setParent(BX);
		BL.setParent(BX);
		CX.setUpper(CH);
		CX.setLower(CL);
		CH.setParent(CX);
		CL.setParent(CX);
		DX.setUpper(DH);
		DX.setLower(DL);
		DH.setParent(DX);
		DL.setParent(DX);

		DI = new Registers("0x0000", "di"); //
		SI = new Registers("0x0000", "si"); // source
		BP = new Registers("0x0000", "bp"); // base pointer register
		SP = new Registers("0xFFFE", "sp"); // stack pointer register

		ZF = new Flags(false);
		CF = new Flags(false);
		AF = new Flags(false);
		SF = new Flags(false);
		OF = new Flags(false);

		registersCheckList.add("ax");
		registersCheckList.add("ah");
		registersCheckList.add("bx");
		registersCheckList.add("al");
		registersCheckList.add("bh");
		registersCheckList.add("bl");
		registersCheckList.add("cx");
		registersCheckList.add("dh");
		registersCheckList.add("dx");
		registersCheckList.add("dl");
		registersCheckList.add("ch");
		registersCheckList.add("cl");
		registersCheckList.add("si");
		registersCheckList.add("bp");
		registersCheckList.add("di");
		registersCheckList.add("sp");

		registers16CheckList.add("ax");
		registers16CheckList.add("dx");
		registers16CheckList.add("si");
		registers16CheckList.add("bp");
		registers16CheckList.add("bx");
		registers16CheckList.add("cx");
		registers16CheckList.add("di");
		registers16CheckList.add("sp");

		registers8CheckList.add("bh");
		registers8CheckList.add("bl");
		registers8CheckList.add("dl");
		registers8CheckList.add("dh");
		registers8CheckList.add("ch");
		registers8CheckList.add("cl");
		registers8CheckList.add("al");
		registers8CheckList.add("ah");

		j_instructionList.add("jz");
		j_instructionList.add("jbe");
		j_instructionList.add("jnz");
		j_instructionList.add("jb");
		j_instructionList.add("je");
		j_instructionList.add("jae");
		j_instructionList.add("jne");
		j_instructionList.add("ja");
		j_instructionList.add("jnae");
		j_instructionList.add("jnc");
		j_instructionList.add("jnb");
		j_instructionList.add("jnbe");
		j_instructionList.add("jc");
		j_instructionList.add("jmp");
	}

	/**
	 * All registers instantiated before are added to the registerList list.
	 */
	public static void addAllregisterListtoList() {
		registerList.add(AX);
		registerList.add(AH);
		registerList.add(AL);
		registerList.add(BX);
		registerList.add(BH);
		registerList.add(BL);
		registerList.add(CX);
		registerList.add(CH);
		registerList.add(CL);
		registerList.add(DX);
		registerList.add(DH);
		registerList.add(DL);

		registerList.add(DI);
		registerList.add(SI);
		registerList.add(SP);
		registerList.add(BP);

	}

	/** Checks the line not determined as a valid instruction line. 
	 *  If the line is not specified as a label,too, then the interpreter
	 *  terminates the program due to syntax error.
	 *  @param codeLines  code line to be checked
	 *  @return true if the given line is a valid label line, false otherwise.
	 */
	public static boolean isLabel(ArrayList<String> codeLines) {
		boolean valid = false;

		boolean substring = false;
		if (codeLines.size() == 1 && codeLines.get(0).endsWith(":")) {
			substring = true;
			valid = true;
		}
		if (codeLines.size() == 2 && codeLines.get(1).equals(":")) {
			valid = true;
		}
		String str = codeLines.get(0);
		if (substring) {
			str = str.substring(0, str.length() - 1);
		}

		if (valid) {

			if (!Character.isJavaIdentifierStart(str.charAt(0))) {
				valid = false;
			}
			for (int i1 = 1; i1 < str.length(); i1++) {
				if (!Character.isJavaIdentifierPart(str.charAt(i1)))
					valid = false;
			}
		}

		return valid;

	}

	/**
	 * Checks the type of the given number. Returns "hexnumber" if the given string
	 * is a hexadecimal number, "decnumber" if it is decimal, "error" if it is not
	 * in the right form.
	 * 
	 * @param operand number to be checked
	 * @return the type of the given string
	 */
	public static String whichTypeOfNumber(String operand) {

		boolean validSyntax = false;
		boolean isHexNumber = false;
		boolean isDecNumber = false;
		operand = operand.toLowerCase();

		if (operand.endsWith("h") || operand.startsWith("0")) {
			isHexNumber = true;
		}
		if (operand.endsWith("h")) {
			operand = operand.substring(0, operand.length() - 1);
		}

		if (operand.endsWith("d") && !operand.startsWith("0")) {
			operand = operand.substring(0, operand.length() - 1);
			isDecNumber = true;
		} else {
			isDecNumber = true;
		}

		// Dec Number

		try {
			if (isHexNumber) {
				validSyntax = true;
			} else if (isDecNumber) {
				validSyntax = true;
			}
		}

		catch (NumberFormatException e) {
			validSyntax = false;
		}

		if (validSyntax && isHexNumber)
			return "hexnumber";

		if (validSyntax && isDecNumber)
			return "decnumber";

		return "error";

	}

	/**
	 * Creates a string and concatenates it with some words about the given
	 * operand's type. Finally, returns that string.
	 * 
	 * Adds "offset" to the type of that string if it is an offset, adds "var" for
	 * variable, "byte", "word" for bytes and words accordingly, "reg" for
	 * registers, "memory" for memory, and "error" for anything erroneous.
	 * 
	 * At the end, after concatenating all these types, the final string is ready
	 * and is returned.
	 * 
	 * @param operand operand to its type to be decided
	 * @param line    number of the line
	 * @return returns the final string after all necessary concatenations
	 */
	public static String whichType(String operand, int line) {

		String tempType = "";
		boolean isByteDeclared = false;
		if (operand.matches("offset(.*)")) {
			operand = operand.substring(6);
			tempType += "offset";

			if (vairablesCheckList.contains(operand))
				tempType += "var";

			else {
				tempType = "error";
			}

		}

		else if (operand.matches("[bw]\\[(.*)\\]")) {

			isByteDeclared = (operand.charAt(0) == 'b') ? true : false;
			if (isByteDeclared) {
				tempType += "byte";
			} else {
				tempType += "word";
			}

			operand = operand.substring(2, operand.length() - 1);

			if (registersCheckList.contains(operand)) {
				tempType += "reg";
			}

			else if (!whichTypeOfNumber(operand).equals("error")) {
				tempType += whichTypeOfNumber(operand);
			} else {
				tempType = "error";
			}

			tempType += "memory";

		}

		else if (registersCheckList.contains(operand)) {
			tempType += "reg";
		}

		else if (vairablesCheckList.contains(operand)) {
			tempType += "var";
		}

		else if (operand.matches("[bw]\\S+")) {

			if (operand.startsWith("b"))
				tempType += "byte";
			if (operand.startsWith("w"))
				tempType += "word";

			operand = operand.substring(1);
			if (registersCheckList.contains(operand)) {
				tempType += "reg";
			} else if (vairablesCheckList.contains(operand)) {
				tempType += "var";
			} else if (operand.matches("[\'\"](.*)[\'\"]")) {
				tempType += "char";
			} else {
				tempType = whichTypeOfNumber(operand);
			}
		}

		else if (operand.matches("\\[(.*)\\]")) {

			String tempMemAdress = operand.substring(1, operand.length() - 1);

			if (registersCheckList.contains(tempMemAdress)) {
				tempType += "reg";
			} else if (!whichTypeOfNumber(tempMemAdress).equals("error")) {
				tempType += whichTypeOfNumber(tempMemAdress);
			} else {
				tempType = "error";
			}
			tempType += "memory";

		}

		else if (operand.matches("[\'\"](.*)[\'\"]")) {

			if (operand.length() > 3)
				tempType += "error";
			tempType += "char";
		}

		else {
			tempType += whichTypeOfNumber(operand);
		}

		return tempType;

	}

	// changes all execution list to lower case except characters
	public static void toLowerCaseExecution() {
		for (int i = 0; i < executionList.size(); i++) {
			for (int j = 0; j < executionList.get(i).size(); j++) {
				if (!executionList.get(i).get(j).matches("(.*)[\'\"][ -~][\'\"](.*)")) {
					executionList.get(i).set(j, executionList.get(i).get(j).toLowerCase());
				}
			}
		}
	}
	
	/**
	 * A void method that simulates storing a variable in the memory that has no
	 * parameters.
	 */
	public static void variableToMemory() {

		int tempIndex = 6 * numberOfInstructions;
		String temp = "";

		for (int i = 0; i < variables.size(); i++) {
			temp = Integer.toHexString(variables.get(i).getIntValue());
			// dw data variables
			if (variables.get(i).isWord()) {
				if (temp.length() > 4) {
					System.out.println("Error");
					System.exit(99);
				}

				// add necessary number of zeros in front
				String s = "";
				for (int j = 0; j < 4 - temp.length(); j++) {
					s += "0";
				}
				temp = s + temp;

				String u = temp.substring(0, 2);
				String b = temp.substring(2);

				// sends hexadecimal numbers to the setMemory() method and puts them in two
				// different indices
				setMemory(tempIndex, Integer.parseInt(b, 16));
				setMemory(tempIndex + 1, Integer.parseInt(u, 16));
				variables.get(i).setOffset(tempIndex);

				tempIndex = tempIndex + 2;
			}

			// db data variables
			else {
				// erroneous syntax
				if (temp.length() > 2) {
					System.out.println("Error");
					System.exit(99);
				}

				// add necessary number of zeros in front
				String s = "";
				for (int j = 0; j < 2 - temp.length(); j++) {
					s += "0";
				}
				temp = s + temp;

				setMemory(tempIndex, Integer.parseInt(temp, 16));
				variables.get(i).setOffset(tempIndex);
				tempIndex++;
			}
		}
	}


}
