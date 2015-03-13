import java.util.*;
import java.io.*;

public class CSC350P2 {

	public static void main (String [] args) {
		// initialize registers	
		// 16-bit program counter
		short PC = 0600;			// program counter (starts at 0600)
		
		// 8-bit registers
		byte AC = 0;				// accumulator
		byte X = 0;					// X register
		byte Y = 0;					// Y register
		byte SR = 0;				// status register
		byte SP = 0; 				// stack pointer
		
		// initialize flags
		boolean N = false;		// negative
		boolean V = false;		// overflow
		boolean G = false;		// ignored
		boolean B = false;		// break
		boolean D = false;		// decimal
		boolean I = false;		// interrupt
		boolean Z = true;		// zero
		boolean C = false;		// carry
		
		// wait for open button press?
		// open assembly code
		Scanner scanner = null;
		try {
			scanner = new Scanner (new File ("test.txt"));
		} catch (Exception e) {
			System.out.println("Error: Couldn't open file");
			System.exit(0);
		}
				
		// create interface
		UserInterface ui = new UserInterface();
		ui.createUI();
		
		// loop through file:
		while (scanner.hasNextLine()) {
			// 1. read line
			String input = scanner.nextLine();
			System.out.println(input);
			
			// split[0] = memory instruction for PC, split[1] = opcode, split [2] = opcode
			String[] split = (input.substring(1)).split("\\s+");

			PC = Short.parseShort(split[0]);		// update PC
			
			byte op1 = strToByte(split[1]);
			byte op2;
			
			// 2. read opcode and do instruction
			switch (op1) {
				case 0x69: 	
					// ADC - Add with Carry - Immediate
					op2 = strToByte(split[2]);
					
					V = updateVFlagAdd(AC, op2);	// check overflow
					C = updateCFlag(AC, op2);		// check carry
					
					AC += op2;		
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);
					break;	
				case 0x29:
					// AND - Logical And - immediate
					op2 = strToByte(split[2]);
					
					AC = (byte) (AC & op2);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);				
					break;
				case (byte) 0xA9: 	
					// LDA - Load Accumulator - Immediate
					op2 = strToByte(split[2]);
					AC = op2;	
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);			
					break;					
				default:
					System.out.println("Error: Couldn't find instruction for opcode: " + split[1]);
					break;
			}

			// tmp print registers
			System.out.println(String.format("%04d: AC: %d", PC, AC));
			System.out.println("flags: " + N + " " + V + " " + G + " " + B + " " + D + " " + I + " " + Z + " " + C);
					
			// 3. update UI
			ui.updateRegisters(PC, AC, X, Y, SR, SP);
			ui.updateFlags(N, V, G, B, D, I, Z, C);
			
			// 4. wait for button press?
		}
			
		scanner.close();
	}
	
	// take string in base 16 and return byte
	public static byte strToByte(String str) {
		return (byte)Integer.parseInt(str, 16);
	}
	
	// return true if accumulator = 0
	public static boolean updateZFlag (byte AC) {
		if (AC == 0)
			return true;
		return false;
	}
	
	// return true if accumulator is negative
	public static boolean updateNFlag (byte AC) {
		if (AC < 0)
			return true;
		return false;
	}	
	
	// return true if overflow in bit 7
	public static boolean updateCFlag (byte AC, byte op2) {
// ** to do			
		return false;
	}	
	
	// return true if overflow (i.e. sign bit is incorrect)
	// this will occur if adding 2 positive numbers that sum to > 127
	// or adding 2 negative numbers that sum to < -128
	public static boolean updateVFlagAdd (byte AC, byte op2) {
		int tmp = AC + op2;
		if (AC > 0 && op2 > 0 && tmp < 0)
			return true;
		if (AC < 0 && op2 < 0 && tmp > 0)
			return true;
		
		return false;		
	}		
}
