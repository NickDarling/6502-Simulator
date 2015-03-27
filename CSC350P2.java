import java.util.*;
import java.io.*;

public class CSC350P2 {

	static final short STARTING_MEMORY_LOCATION = 0x0600;
	static final short STARTING_STACK_LOCATION = 0x01FF;	
	static final int TOTAL_MEMORY = 65536; 	// 4KB
	static final short STACK_SIZE = 256;	// 256 bytes
	
	public static void main (String [] args) {
		// initialize registers	
		// 16-bit program counter
		short PC = STARTING_MEMORY_LOCATION;	// program counter
		
		// 8-bit registers
		byte AC = 0;		// accumulator
		byte X = 0;			// X register
		byte Y = 0;			// Y register
		byte SR = 0;		// status register
		byte SP = 0; 		// stack pointer - contains next free byte of stack space
		
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
			scanner = new Scanner (new File ("program2.asm"));
		} catch (Exception e) {
			System.out.println("Error: Couldn't open file");
			System.exit(0);
		}
		
		byte [] memory = new byte [TOTAL_MEMORY];	// 4 KB of memory

		int currLocation = STARTING_MEMORY_LOCATION;

		// put program in memory
		while (scanner.hasNextLine()) {
			// 1. read line
			String line = scanner.nextLine();
			//System.out.println(line);
			
			// 2. split up line
			// split[0] = address of instruction
			// split[1] = instruction opcode
			// (if instruction needs data) split [2] = data, split [3] = data
			String[] split = (line.substring(1)).split("\\s+");		
			
			// 3. save instruction opcode
			memory[currLocation++] = strToByte(split[1]);
			
			// 4. save data values (if there are any)
			// check if end of line or if token is > 2 characters (then it's a comment)
			int i = 2;
			while (split.length > i) {
				if (split[i].length() <= 2) {
					memory[currLocation++] = strToByte(split[i++]);	// save data
				} else {
					break;
				}
			}

			// 5. check if exceeded memory limit
			if (currLocation > TOTAL_MEMORY) {
				System.out.println("Error: Can't fit program in memory");
				System.exit(0);		
			}		
		}
		scanner.close();	
		
		// prints memory
		/*
		for (int i = STARTING_MEMORY_LOCATION; i < currLocation; i++) {
			System.out.printf("0x%02X: 0x%02X\n", i, memory[i]);
		}
		*/
		
		// create interface
		// UserInterface ui = new UserInterface();
		// ui.createUI();
		
		// execute one instruction - end program once PC is finishes last line of code
		while (PC < currLocation) {
			// 1. read opcode
			byte opcode = memory[PC++];		// contains instruction type	
			byte data1;		// first byte of data
			byte data2;		// second byte of data
			byte tmp;
			
			// 2. find instruction
			switch (opcode) {
			
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Load/store operations  
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
				// -------------------- LDA - Load Accumulator --------------------
				case (byte) 0xA9: 	
					// LDA - Load Accumulator - Immediate
					data1 = memory[PC++];
					AC = data1;	
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);			
					break;	
					
				case (byte) 0xA5:
					// LDA - Load Accumulator - Zero Page
					data1 = memory[PC++];
					
					AC = memory[data1];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
					
				case (byte) 0xB5:
					// LDA - Load Accumulator - Zero Page, X
					data1 = memory[PC++];
					
					AC = memory[data1 + X];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;				
					
				case (byte) 0xAD:
					// LDA - Load Accumulator - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					AC = memory[twoBytesToShort(data1, data2)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0xBD:
					// LDA - Load Accumulator - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					AC = memory[X + twoBytesToShort(data1, data2)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0xB9:
					// LDA - Load Accumulator - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					AC = memory[Y + twoBytesToShort(data1, data2)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;	
					
				case (byte) 0xA1:
					// LDA - Load Accumulator - (Indirect,X) (16-bit address)
					data1 = memory[PC++];
					
					AC = memory[memory[X + data1]];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;			
					
				case (byte) 0xB1:
					// LDA - Load Accumulator - (Indirect), Y (16-bit address)
					data1 = memory[PC++];
					
					AC = memory[memory[data1] + Y];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;					
					
				// -------------------- LDX - Load X Register  --------------------
				case (byte) 0xA2:
					// LDX - Load X Register - Immediate
					data1 = memory[PC++];
					X = data1;	
					
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);			
					break;	
					
				case (byte) 0xA6:
					// LDX - Load X Register - Zero Page
					data1 = memory[PC++];		
					X = memory[data1];
					
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);	
					break;	
					
				case (byte) 0xB6:
					// LDX - Load X Register - Zero Page, Y
					data1 = memory[PC++];
					X = memory[data1 + Y];
					
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);	
					break;	
					
				case (byte) 0xAE:
					// LDX - Load X Register - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					X = memory[twoBytesToShort(data1, data2)];
					
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);	
					break;
					
				case (byte) 0xBE:
					// LDX - Load X Register - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					X = memory[Y + twoBytesToShort(data1, data2)];
					
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);					
					break;	
				
				// -------------------- LDY - Load Y Register  --------------------	
				case (byte) 0xA0:
					// LDY - Load Y Register - Immediate
					data1 = memory[PC++];
					Y = data1;	
					
					// update flags
					Z = updateZFlag(Y);
					N = updateNFlag(Y);			
					break;	
					
				case (byte) 0xA4:
					// LDY - Load Y Register - Zero Page
					data1 = memory[PC++];		
					Y = memory[data1];
					
					// update flags
					Z = updateZFlag(Y);
					N = updateNFlag(Y);	
					break;	
					
				case (byte) 0xB4:
					// LDY - Load X Register - Zero Page, Y
					data1 = memory[PC++];
					Y = memory[data1 + X];
					
					// update flags
					Z = updateZFlag(Y);
					N = updateNFlag(Y);	
					break;	
					
				case (byte) 0xAC:
					// LDY - Load Y Register - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					Y = memory[twoBytesToShort(data1, data2)];
					
					// update flags
					Z = updateZFlag(Y);
					N = updateNFlag(Y);	
					break;
					
				case (byte) 0xBC:
					// LDY - Load Y Register - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					Y = memory[X + twoBytesToShort(data1, data2)];
					
					// update flags
					Z = updateZFlag(Y);
					N = updateNFlag(Y);					
					break;	
					
				// -------------------- STA - Store Accumulator  --------------------							
				case (byte) 0x85:
					// STA - Store Accumulator - Zero Page
					data1 = memory[PC++];
					memory[data1] = AC;
					break;	
					
				case (byte) 0x95:
					// STA - Store Accumulator - Zero Page, X
					data1 = memory[PC++];
					memory[data1 + X] = AC;
					break;				

				case (byte) 0x8D:
					// STA - Store Accumulator - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					memory[twoBytesToShort(data1, data2)] = AC;
					break;					
					
				case (byte) 0x9D:
					// STA - Store Accumulator - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];		
					memory[X + twoBytesToShort(data1, data2)] = AC;			
					break;
					
				case (byte) 0x99:
					// STA - Store Accumulator - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					memory[Y + twoBytesToShort(data1, data2)] = AC;				
					break;	
					
				case (byte) 0x81:
					// STA - Store Accumulator - (Indirect,X) (16-bit address)
					data1 = memory[PC++];
					memory[memory[X + data1]] = AC;				
					break;			
					
				case (byte) 0x91:
					// STA - Store Accumulator - (Indirect), Y (16-bit address)
					data1 = memory[PC++];
					memory[memory[data1] + Y] = AC;				
					break;								
			
				// -------------------- STX - Store X Register  --------------------							
				case (byte) 0x86:
					// STX - Store X Register - Zero Page
					data1 = memory[PC++];
					memory[data1] = X;
					break;	
					
				case (byte) 0x96:
					// STX - Store X Register - Zero Page, Y
					data1 = memory[PC++];
					memory[data1 + Y] = X;
					break;	
					
				case (byte) 0x8E:
					// STX - Store X Register - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					memory[twoBytesToShort(data1, data2)] = X;
					break;					
					
				// -------------------- STY - Store Y Register  --------------------							
				case (byte) 0x84:
					// STY - Store Y Register - Zero Page
					data1 = memory[PC++];
					memory[data1] = Y;
					break;	
					
				case (byte) 0x94:
					// STY - Store Y Register - Zero Page, X
					data1 = memory[PC++];
					memory[data1 + X] = Y;
					break;	
					
				case (byte) 0x8C:
					// STY - Store Y Register - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					memory[twoBytesToShort(data1, data2)] = Y;
					break;							
		
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Register Transfer Operations 
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *										
				// -------------------- TAX - Transfer Accumulator to X  --------------------							
				case (byte) 0xAA:
					X = AC;	
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);					
					break;	
					
				// -------------------- TAY - Transfer Accumulator to Y  --------------------							
				case (byte) 0xA8:
					Y = AC;	
					// update flags
					Z = updateZFlag(Y);
					N = updateNFlag(Y);					
					break;						
	
				// -------------------- TXA - Transfer X to Accumulator --------------------							
				case (byte) 0x8A:
					AC = X;	
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				// -------------------- TYA - Transfer Y to Accumulator --------------------							
				case (byte) 0x98:
					AC = Y;	
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;		
					
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Stack Operations 
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *						
				// -------------------- TSX - Transfer Stack Pointer to X --------------------	
				case (byte) 0xBA:
					X = SP;
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);	
					break;
				
				// -------------------- TXS - Transfer X to Stack Pointer --------------------	
				case (byte) 0x9A:
					SP = X;
					break;
					
				// -------------------- PHA - Push Accumulator --------------------	
				case (byte) 0x48:
					memory[STARTING_STACK_LOCATION - SP++] = AC;				
					break;
					
				// -------------------- PHP - Push Processor Status --------------------	
				case (byte) 0x08:
					memory[STARTING_STACK_LOCATION - SP++] = flagsToByte(N, V, G, B, D, I, Z, C);			
					break;
			
				// -------------------- PLA - Pull Accumulator --------------------	
				case (byte) 0x68:
					AC = memory[STARTING_STACK_LOCATION - SP--];	
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;					
					
				// -------------------- PLP - Pull Processor Status --------------------	
				case (byte) 0x28:
					tmp = memory[STARTING_STACK_LOCATION - SP--];
					C = (((byte)tmp & 1) == 1)? true : false;
					Z = (((byte)tmp & 2) == 1)? true : false;
					I = (((byte)tmp & 4) == 1)? true : false;
					D = (((byte)tmp & 8) == 1)? true : false;
					B = (((byte)tmp & 16) == 1)? true : false;
					G = (((byte)tmp & 32) == 1)? true : false;
					V = (((byte)tmp & 64) == 1)? true : false;
					N = (((byte)tmp & 128) == 1)? true : false;															
					break;					
					
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Logical operations 
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
				// -------------------- AND - Logical AND --------------------						
				case (byte) 0x29:
					// AND - Logical AND - immediate
					data1 =  memory[PC++];	
					AC = (byte) (AC & data1);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);				
					break;					
					
				case (byte) 0x25:
					// AND - Logical AND - Zero Page
					data1 = memory[PC++];
					AC = (byte) (AC & memory[data1]);	
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
					
				case (byte) 0x35:
					// AND - Logical AND - Zero Page, X
					data1 = memory[PC++];
					AC = (byte) (AC & memory[data1 + X]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;				
					
				case (byte) 0x2D:
					// AND - Logical AND - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];	
					AC = (byte)(AC & memory[twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0x3D:
					// AND - Logical AND - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC & memory[X + twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0x39:
					// AND - Logical AND - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC & memory[Y + twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;	
					
				case (byte) 0x21:
					// AND - Logical AND - (Indirect,X) (16-bit address)
					data1 = memory[PC++];			
					AC = (byte)(AC & memory[memory[X + data1]]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;			
					
				case (byte) 0x31:
					// AND - Logical AND - (Indirect), Y (16-bit address)
					data1 = memory[PC++];
					AC = (byte)(AC & memory[memory[data1] + Y]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;

				// -------------------- EOR - Exclusive OR --------------------						
				case (byte) 0x49:
					// EOR - Exclusive OR - Immediate
					data1 =  memory[PC++];				
					AC = (byte) (AC ^ data1);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);				
					break;					
					
				case (byte) 0x45:
					// EOR - Exclusive OR - Zero Page
					data1 = memory[PC++];
					AC = (byte) (AC ^ memory[data1]);	
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
					
				case (byte) 0x55:
					// EOR - Exclusive OR - Zero Page, X
					data1 = memory[PC++];
					AC = (byte) (AC ^ memory[data1 + X]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;				
					
				case (byte) 0x4D:
					// EOR - Exclusive OR - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];	
					AC = (byte)(AC ^ memory[twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0x5D:
					// EOR - Exclusive OR - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC ^ memory[X + twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0x59:
					// EOR - Exclusive OR - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC ^ memory[Y + twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;	
					
				case (byte) 0x41:
					// EOR - Exclusive OR - (Indirect,X) (16-bit address)
					data1 = memory[PC++];			
					AC = (byte)(AC ^ memory[memory[X + data1]]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;			
					
				case (byte) 0x51:
					// EOR - Exclusive OR - (Indirect), Y (16-bit address)
					data1 = memory[PC++];
					AC = (byte)(AC ^ memory[memory[data1] + Y]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;					
					
				// -------------------- ORA - Inclusive OR --------------------						
				case (byte) 0x09:
					// ORA - Inclusive OR - Immediate
					data1 =  memory[PC++];				
					AC = (byte) (AC | data1);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);				
					break;					
					
				case (byte) 0x05:
					// ORA - Inclusive OR - Zero Page
					data1 = memory[PC++];
					AC = (byte) (AC | memory[data1]);	
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
					
				case (byte) 0x15:
					// ORA - Inclusive OR - Zero Page, X
					data1 = memory[PC++];
					AC = (byte) (AC | memory[data1 + X]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;				
					
				case (byte) 0x0D:
					// ORA - Inclusive OR - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];	
					AC = (byte)(AC | memory[twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0x1D:
					// ORA - Inclusive OR - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC | memory[X + twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0x19:
					// ORA - Inclusive OR - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC | memory[Y + twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;	
					
				case (byte) 0x01:
					// ORA - Inclusive OR - (Indirect,X) (16-bit address)
					data1 = memory[PC++];			
					AC = (byte)(AC | memory[memory[X + data1]]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;			
					
				case (byte) 0x11:
					// ORA - Inclusive OR - (Indirect), Y (16-bit address)
					data1 = memory[PC++];
					AC = (byte)(AC | memory[memory[data1] + Y]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;	
					
				// -------------------- BIT - Bit Test --------------------								
				case (byte) 0x24:
					// BIT - Bit Test - Zero Page
					// ORA - Inclusive OR - Zero Page
					data1 = memory[PC++];
					tmp = (byte) (AC & memory[data1]);	
					
					// update flags
					Z = updateZFlag(tmp);
					V = (((byte)tmp & 64) == 1)? true : false;	// bit 6
					N = (((byte)tmp & 128) == 1)? true : false;	// bit 7	
					break;
				
				case (byte) 0x2C:
					// BIT - Bit Test - Absolute
					data1 = memory[PC++];
					data2 = memory[PC++];	
					tmp = (byte)(AC & memory[twoBytesToShort(data1, data2)]);
					
					// update flags
					Z = updateZFlag(tmp);
					V = (((byte)tmp & 64) == 1)? true : false;	// bit 6
					N = (((byte)tmp & 128) == 1)? true : false;	// bit 7								
					break;
					
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Arithmetic operations 
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *					
				case (byte) 0x69: 	
					// ADC - Add with Carry - Immediate
					data1 =  memory[PC++];
					
					V = updateVFlag(AC, opcode, data1);	// check overflow
					C = updateCFlag(AC, opcode, data1);	// check carry
					
					AC += data1;		
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
				default:
					System.out.println("Error: Couldn't find instruction for opcode: " + opcode);
					break;
			}

			// tmp print registers
			System.out.printf("0x%04X: AC=0x%02X\n", PC, AC);
			System.out.println("flags: " + N + " " + V + " " + G + " " + B + " " + D + " " + I + " " + Z + " " + C);
					
			// 3. update UI
			//ui.updateRegisters(PC, AC, X, Y, SR, SP);
			//ui.updateFlags(N, V, G, B, D, I, Z, C);
			
			// 4. wait for button press?			
		}
	}

	// take string in base 16 and return byte
	public static byte strToByte(String str) {
		return (byte)Integer.parseInt(str, 16);
	}
	
	// take string in base 16 and return int
	public static int strToInt(String str) {
		return (int)Integer.parseInt(str, 16);
	}
	
	// take 2 bytes and create a short
	public static short twoBytesToShort(byte b1, byte b2) {
        return (short) ((b1 << 8) | b2);
	}
	
	// takes all flags and returns a copy of the status flags as a byte
	public static byte flagsToByte(boolean N, boolean V, boolean G, boolean B, boolean D, boolean I, boolean Z, boolean C) {
		byte tmp = 0;
		if (C) tmp |= 1;
		if (Z) tmp |= 2;
		if (I) tmp |= 4;
		if (D) tmp |= 8;
		if (B) tmp |= 16;
		if (G) tmp |= 32;
		if (V) tmp |= 64;
		if (N) tmp |= 128;
		return tmp;
	}
	
/* * * * * * * * *
 * Flag methods  *
 * * * * * * * * */ 
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
	// this will occur if the addition of two numbers causes a carry of the most significant bit
	// or subtraction of two numbers requires a borrow into the most significant bit
	public static boolean updateCFlag (byte AC, byte opcode, byte input) {
// ** to do				
		return false;	
	}	
	
	// return true if overflow (i.e. sign bit is incorrect)
	// this will occur if adding 2 positive numbers that sum to > 127
	// or adding 2 negative numbers that sum to < -128
	public static boolean updateVFlag (byte AC, byte opcode, byte input) {
		if (opcode == 0x69) {
			byte tmp = (byte) (AC + input);
			if (AC > 0 && input > 0 && tmp < 0)
				return true;
			if (AC < 0 && input < 0 && tmp > 0)
				return true;
			return false;
		} else {
			System.out.println("Error: Couldn't find opcode: " + opcode + " in updateVFlag");
		}		
		return false;		
	}		
}
