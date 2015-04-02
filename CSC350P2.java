import java.util.*;
import java.io.*;

public class CSC350P2 {
	static final short STARTING_STACK_LOCATION = 0x01FF;	
	static final int TOTAL_MEMORY = 65536; 	// 4KB
	static final short STACK_SIZE = 256;	// 256 bytes
	
	public static void main (String [] args) {
		// initialize registers	
		// 16-bit program counter
		short PC = 0;	// program counter
		
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
		
		/*
		// wait for open button press?
		// open assembly code
		Scanner scanner = null;
		try {
			scanner = new Scanner (new File ("program3.asm"));
		} catch (Exception e) {
			System.out.println("Error: Couldn't open file");
			System.exit(0);
		}
		*/
		
		//calls fileUpload to receive uploaded file for reading        
		// create interface
		UserInterface ui = new UserInterface();
		ui.createUI();
		//ui.fileUpload();
        String fileToRead = "";
		
		// wait for open file press?	
		while (ui.getFile == false) {
			//System.out.println("waiting");
            try {
                Thread.sleep(200);                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            } 	
            
            if (ui.getFile)
            	fileToRead = ui.fileUpload();
            if (fileToRead == null)
            	ui.getFile = false;
		}        
		
		
		byte [] memory = new byte [TOTAL_MEMORY];	// 4 KB of memory

		String line;
		String[] split = null;
		
		/*
		if (!scanner.hasNextLine()) {
			System.out.println("Error: Can't read file");
			scanner.close();	
			System.exit(0);
		}
		*/
		
		boolean first = true;	// true when first instruction - used to get initial PC value
		boolean inData = false;
		int lastAddr = 0;		// addr of last instruction
		
		// else assume no data and just code
		// put program in memory
		//while (scanner.hasNextLine()) {
		String fileLines[] = fileToRead.split("\\r?\\n");
		for (int j = 0; j < fileLines.length; j++) {
			// 1. read line
			line = fileLines[j];
			//line = scanner.nextLine();
			//System.out.println(line);
			
			// 2. split up line
			// split[0] = address of instruction
			// split[1] = instruction opcode
			// (if instruction needs data) split [2] = data, split [3] = data
			if (!line.trim().isEmpty()) {
				split = (line.substring(1)).split("\\s+");		
				
				if (split[0].equals("code")) {
					inData = false;
					
				} else if (split[0].equals("data")) {
					inData = true;
				
				} else if (inData) {
					// put data in memory
					// split[0] = location in memory
					// split[1] = value
					memory[Integer.parseInt(split[0], 16)] = (byte)Integer.parseInt(split[1], 10);
				} else {
					// 3. save instruction opcode
					int incr = 0;
					memory[strToShort(split[0]) + incr++] = strToByte(split[1]);
					
					// used to get intial PC value
					if (first) {
						PC = strToShort(split[0]);
						first = false;
					}
					
					// 4. save data values (if there are any)
					// check if end of line or if token is > 2 characters (then it's a comment)
					int i = 2;
					while (split.length > i) {
						if (split[i].length() <= 2) {
							memory[strToShort(split[0]) + incr++] = strToByte(split[i++]);	// save data
						} else {
							break;
						}
					}
		
					lastAddr = strToShort(split[0]) + incr;
					
					// 5. check if exceeded memory limit
					if ((strToShort(split[0]) + incr) > TOTAL_MEMORY) {
						System.out.println("Error: Can't fit program in memory");
						System.exit(0);		
					}
				}
			}
		}
		//scanner.close();	

		
		// prints memory
		/*
		for (int i = 0; i < TOTAL_MEMORY; i++) {
			if (memory[i] != 0)
				System.out.printf("0x%02X: 0x%02X\n", i, memory[i]);
		}
		System.out.printf("0x%02X\n", lastAddr);
		System.out.printf("PC: 0x%02X\n", PC);
		System.exit(0);
		*/
		
		ui.pressed = false;
		
		// execute one instruction - end program once PC is finishes last line of code
		while (PC < lastAddr) {	
			// tmp print registers
			System.out.printf("0x%04X: AC=0x%02X, X=0x%02X\n", PC, AC, X);
			System.out.println("flags: " + N + " " + V + " " + G + " " + B + " " + D + " " + I + " " + Z + " " + C);
					
			// -1. update UI
			ui.updateRegisters(PC, AC, X, Y, SR, SP);
			ui.updateFlags(N, V, G, B, D, I, Z, C);
			ui.updateText(fileLines, PC);
			
			// 0. wait for button press?	
			while (ui.pressed == false) {
				//System.out.println("waiting");
	            try {
	                Thread.sleep(200);                 //1000 milliseconds is one second.
	            } catch(InterruptedException ex) {
	                Thread.currentThread().interrupt();
	            } 	
			}
			ui.pressed = false;				
			
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
					
					AC = memory[twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0xBD:
					// LDA - Load Accumulator - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					AC = memory[X + twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0xB9:
					// LDA - Load Accumulator - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					AC = memory[Y + twoBytesToShort(data2, data1)];
					
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
					
					X = memory[twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);	
					break;
					
				case (byte) 0xBE:
					// LDX - Load X Register - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					X = memory[Y + twoBytesToShort(data2, data1)];
					
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
					
					Y = memory[twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(Y);
					N = updateNFlag(Y);	
					break;
					
				case (byte) 0xBC:
					// LDY - Load Y Register - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					Y = memory[X + twoBytesToShort(data2, data1)];
					
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
					memory[twoBytesToShort(data2, data1)] = AC;
					break;					
					
				case (byte) 0x9D:
					// STA - Store Accumulator - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];		
					memory[X + twoBytesToShort(data2, data1)] = AC;			
					break;
					
				case (byte) 0x99:
					// STA - Store Accumulator - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					memory[Y + twoBytesToShort(data2, data1)] = AC;				
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
					memory[twoBytesToShort(data2, data1)] = X;
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
					memory[twoBytesToShort(data2, data1)] = Y;
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
					memory[STARTING_STACK_LOCATION - (SP++)] = AC;
				
					break;
					
				// -------------------- PHP - Push Processor Status --------------------	
				case (byte) 0x08:
					memory[STARTING_STACK_LOCATION - (SP++)] = flagsToByte(N, V, G, B, D, I, Z, C);			
					break;
			
				// -------------------- PLA - Pull Accumulator --------------------	
				case (byte) 0x68:
					AC = memory[STARTING_STACK_LOCATION - (SP--)];	
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;					
					
				// -------------------- PLP - Pull Processor Status --------------------	
				case (byte) 0x28:
					tmp = memory[STARTING_STACK_LOCATION - (SP--)];
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
					AC = (byte)(AC & memory[twoBytesToShort(data2, data1)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0x3D:
					// AND - Logical AND - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC & memory[X + twoBytesToShort(data2, data1)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0x39:
					// AND - Logical AND - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC & memory[Y + twoBytesToShort(data2, data1)]);
					
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
					AC = (byte)(AC ^ memory[twoBytesToShort(data2, data1)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0x5D:
					// EOR - Exclusive OR - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC ^ memory[X + twoBytesToShort(data2, data1)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0x59:
					// EOR - Exclusive OR - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC ^ memory[Y + twoBytesToShort(data2, data1)]);
					
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
					AC = (byte)(AC | memory[twoBytesToShort(data2, data1)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0x1D:
					// ORA - Inclusive OR - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC | memory[X + twoBytesToShort(data2, data1)]);
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0x19:
					// ORA - Inclusive OR - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					AC = (byte)(AC | memory[Y + twoBytesToShort(data2, data1)]);
					
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
					tmp = (byte)(AC & memory[twoBytesToShort(data2, data1)]);
					
					// update flags
					Z = updateZFlag(tmp);
					V = (((byte)tmp & 64) == 1)? true : false;	// bit 6
					N = (((byte)tmp & 128) == 1)? true : false;	// bit 7								
					break;
					
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Arithmetic operations 
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
					
				// -------------------- ADC - Add With Carry --------------------		
				case (byte) 0x69: 	
					// ADC - Add with Carry - Immediate
					data1 =  memory[PC++];
					
					V = updateVFlag(AC, 1, data1);	// check overflow
					C = updateCFlag(AC, 1, data1);	// check carry
					
					AC += data1;		
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0x65:
					// ADC - Add with Carry - Zero Page
					data1 = memory[PC++];
					
					V = updateVFlag(AC, 1, memory[data1]);	// check overflow
					C = updateCFlag(AC, 1, memory[data1]);	// check carry				
				
					AC += memory[data1];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
					
				case (byte) 0x75:
					// ADC - Add with Carry - Zero Page, X
					data1 = memory[PC++];
					
					V = updateVFlag(AC, 1, memory[data1 + X]);	// check overflow
					C = updateCFlag(AC, 1, memory[data1 + X]);	// check carry		
				
					AC += memory[data1 + X];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;				
					
				case (byte) 0x6D:
					// ADC - Add with Carry - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					System.out.println("data2: " + data2 + ", data1:" + data1);
					System.out.printf("data2: 0x%02X, data1: 0x%02X, result: 0x%04X \n", data2, data1, twoBytesToShort(data2, data1));
					System.out.println(memory[twoBytesToShort(data2, data1)]);
					
					V = updateVFlag(AC, 1, memory[twoBytesToShort(data2, data1)]);	// check overflow
					C = updateCFlag(AC, 1, memory[twoBytesToShort(data2, data1)]);	// check carry		
					
					AC += memory[twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0x7D:
					// ADC - Add with Carry - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					V = updateVFlag(AC, 1, memory[X + twoBytesToShort(data2, data1)]);	// check overflow
					C = updateCFlag(AC, 1, memory[X + twoBytesToShort(data2, data1)]);	// check carry							
					
					AC += memory[X + twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0x79:
					// ADC - Add with Carry - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
	
					V = updateVFlag(AC, 1, memory[Y + twoBytesToShort(data2, data1)]);	// check overflow
					C = updateCFlag(AC, 1, memory[Y + twoBytesToShort(data2, data1)]);	// check carry						
					
					AC += memory[Y + twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;	
					
				case (byte) 0x61:
					// ADC - Add with Carry - (Indirect,X) (16-bit address)
					data1 = memory[PC++];
	
					V = updateVFlag(AC, 1, memory[memory[X + data1]]);	// check overflow
					C = updateCFlag(AC, 1, memory[memory[X + data1]]);	// check carry					
				
					AC += memory[memory[X + data1]];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;			
					
				case (byte) 0x71:
					// ADC - Add with Carry - (Indirect), Y (16-bit address)
					data1 = memory[PC++];
	
					V = updateVFlag(AC, 1, memory[memory[data1] + Y]);	// check overflow
					C = updateCFlag(AC, 1, memory[memory[data1] + Y]);	// check carry						
				
					AC += memory[memory[data1] + Y];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;						
	
				// -------------------- SBC - Subtract with Carry --------------------		
				case (byte) 0xE9: 	
					// SBC - Subtract with Carry - Immediate
					data1 =  memory[PC++];
					
					V = updateVFlag(AC, 2, data1);	// check overflow
					C = updateCFlag(AC, 2, data1);	// check carry
					
					AC -= data1;		
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0xE5:
					// SBC - Subtract with Carry - Zero Page
					data1 = memory[PC++];
					
					V = updateVFlag(AC, 2, memory[data1]);	// check overflow
					C = updateCFlag(AC, 2, memory[data1]);	// check carry				
				
					AC -= memory[data1];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
					
				case (byte) 0xF5:
					// SBC - Subtract with Carry - Zero Page, X
					data1 = memory[PC++];
					
					V = updateVFlag(AC, 2, memory[data1 + X]);	// check overflow
					C = updateCFlag(AC, 2, memory[data1 + X]);	// check carry		
				
					AC -= memory[data1 + X];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;				
					
				case (byte) 0xED:
					// SBC - Subtract with Carry - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					V = updateVFlag(AC, 2, memory[twoBytesToShort(data2, data1)]);	// check overflow
					C = updateCFlag(AC, 2, memory[twoBytesToShort(data2, data1)]);	// check carry		
				
					AC -= memory[twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;
					
				case (byte) 0xFD:
					// SBC - Subtract with Carry - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					V = updateVFlag(AC, 2, memory[X + twoBytesToShort(data2, data1)]);	// check overflow
					C = updateCFlag(AC, 2, memory[X + twoBytesToShort(data2, data1)]);	// check carry							
					
					AC -= memory[X + twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;
					
				case (byte) 0xF9:
					// SBC - Subtract with Carry - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
	
					V = updateVFlag(AC, 2, memory[Y + twoBytesToShort(data2, data1)]);	// check overflow
					C = updateCFlag(AC, 2, memory[Y + twoBytesToShort(data2, data1)]);	// check carry						
					
					AC -= memory[Y + twoBytesToShort(data2, data1)];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;	
					
				case (byte) 0xE1:
					// SBC - Subtract with Carry - (Indirect,X) (16-bit address)
					data1 = memory[PC++];
	
					V = updateVFlag(AC, 2, memory[memory[X + data1]]);	// check overflow
					C = updateCFlag(AC, 2, memory[memory[X + data1]]);	// check carry					
				
					AC -= memory[memory[X + data1]];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;			
					
				case (byte) 0xF1:
					// SBC - Subtract with Carry - (Indirect), Y (16-bit address)
					data1 = memory[PC++];
	
					V = updateVFlag(AC, 2, memory[memory[data1] + Y]);	// check overflow
					C = updateCFlag(AC, 2, memory[memory[data1] + Y]);	// check carry						
				
					AC -= memory[memory[data1] + Y];
					
					// update flags
					Z = updateZFlag(AC);
					N = updateNFlag(AC);					
					break;						
			
				// -------------------- CMP - Compare --------------------		
				case (byte) 0xC9: 	
					// CMP - Compare - Immediate
					data1 = memory[PC++];
					
					// update flags
					C = AC >= data1 ? true : false;
					Z = AC == data1 ? true : false;							
					N = updateNFlag((byte)(AC - data1));	
					break;
					
				case (byte) 0xC5:
					// CMP - Compare - Zero Page
					data1 = memory[PC++];

					// update flags
					C = AC >= memory[data1] ? true : false;
					Z = AC == memory[data1] ? true : false;										
					N = updateNFlag((byte)(AC - memory[data1]));		
					break;	
					
				case (byte) 0xD5:
					// CMP - Compare - Zero Page, X
					data1 = memory[PC++];

					// update flags
					C = AC >= memory[data1 + X] ? true : false;
					Z = AC == memory[data1 + X] ? true : false;										
					N = updateNFlag((byte)(AC - memory[data1 + X]));	
					break;				
					
				case (byte) 0xCD:
					// CMP - Compare - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					// update flags
					C = AC >= memory[twoBytesToShort(data2, data1)] ? true : false;
					Z = AC == memory[twoBytesToShort(data2, data1)] ? true : false;										
					N = updateNFlag((byte)(AC - memory[twoBytesToShort(data2, data1)]));	
					break;	
					
				case (byte) 0xDD:
					// CMP - Compare - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					// update flags
					C = AC >= memory[X + twoBytesToShort(data2, data1)] ? true : false;
					Z = AC == memory[X + twoBytesToShort(data2, data1)] ? true : false;										
					N = updateNFlag((byte)(AC - memory[X + twoBytesToShort(data2, data1)]));
					break;
					
				case (byte) 0xD9:
					// CMP - Compare - Absolute,Y (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
	
					// update flags
					C = AC >= memory[Y + twoBytesToShort(data2, data1)] ? true : false;
					Z = AC == memory[Y + twoBytesToShort(data2, data1)] ? true : false;										
					N = updateNFlag((byte)(AC - memory[Y + twoBytesToShort(data2, data1)]));
					break;
					
				case (byte) 0xC1:
					// CMP - Compare - (Indirect,X) (16-bit address)
					data1 = memory[PC++];

					// update flags
					C = AC >= memory[memory[X + data1]] ? true : false;
					Z = AC == memory[memory[X + data1]] ? true : false;										
					N = updateNFlag((byte)(AC - memory[memory[X + data1]]));	
					break;
					
				case (byte) 0xD1:
					// CMP - Compare - (Indirect), Y (16-bit address)
					data1 = memory[PC++];

					// update flags
					C = AC >= memory[memory[data1] + Y] ? true : false;
					Z = AC == memory[memory[data1] + Y] ? true : false;										
					N = updateNFlag((byte)(AC - memory[memory[data1] + Y]));	
					break;	
					
				// -------------------- CPX - Compare X Register --------------------		
				case (byte) 0xE0: 	
					// CPX - Compare X Register - Immediate
					data1 = memory[PC++];
					
					// update flags
					C = X >= data1 ? true : false;
					Z = X == data1 ? true : false;										
					N = updateNFlag((byte)(X - data1));	
					break;
					
				case (byte) 0xE4:
					// CPX - Compare X Register - Zero Page
					data1 = memory[PC++];

					// update flags
					C = X >= memory[data1] ? true : false;
					Z = X == memory[data1] ? true : false;											
					N = updateNFlag((byte)(X - memory[data1]));		
					break;	
					
				case (byte) 0xEC:
					// CPX - Compare X Register - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					// update flags
					C = X >= memory[twoBytesToShort(data2, data1)] ? true : false;
					Z = X == memory[twoBytesToShort(data2, data1)] ? true : false;										
					N = updateNFlag((byte)(X - memory[twoBytesToShort(data2, data1)]));	
					break;		
					
				// -------------------- CPY - Compare Y Register --------------------		
				case (byte) 0xC0: 	
					// CPY - Compare Y Register - Immediate
					data1 = memory[PC++];
					
					// update flags
					C = Y >= data1 ? true : false;
					Z = Y == data1 ? true : false;									
					N = updateNFlag((byte)(Y - data1));	
					break;
					
				case (byte) 0xC4:
					// CPY - Compare Y Register - Zero Page
					data1 = memory[PC++];

					// update flags
					C = Y >= memory[data1] ? true : false;
					Z = Y == memory[data1] ? true : false;							
					N = updateNFlag((byte)(Y - memory[data1]));		
					break;	
					
				case (byte) 0xCC:
					// CPY - Compare Y Register - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					// update flags
					C = Y >= memory[twoBytesToShort(data2, data1)] ? true : false;
					Z = Y == memory[twoBytesToShort(data2, data1)] ? true : false;								
					N = updateNFlag((byte)(Y - memory[twoBytesToShort(data2, data1)]));	
					break;	
					
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Increments & Decrements 
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
					
				// -------------------- INC - Increment Memory --------------------		
				case (byte) 0xE6:
					// INC - Increment Memory - Zero Page
					data1 = memory[PC++];
					
					memory[data1]++;

					// update flags
					Z = updateZFlag(memory[data1]);
					N = updateNFlag(memory[data1]);	
					break;	
					
				case (byte) 0xF6:
					// INC - Increment Memory - Zero Page, X
					data1 = memory[PC++];
					memory[data1 + X]++;

					// update flags
					Z = updateZFlag(memory[data1 + X]);
					N = updateNFlag(memory[data1 + X]);	
					break;				
					
				case (byte) 0xEE:
					// INC - Increment Memory - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					memory[twoBytesToShort(data1, data2)]++;	

					// update flags
					Z = updateZFlag(memory[twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[twoBytesToShort(data2, data1)]);	
					break;
					
				case (byte) 0xFE:
					// INC - Increment Memory - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					memory[X + twoBytesToShort(data1, data2)]++;
					
					// update flags
					Z = updateZFlag(memory[X + twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[X + twoBytesToShort(data2, data1)]);					
					break;
					
				// -------------------- INX - Increment X Register --------------------	
				case (byte) 0xE8:
					X++;
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);	
					break;
	
				// -------------------- INY - Increment Y Register --------------------				
				case (byte) 0xC8:
					Y++;
					// update flags
					Z = updateZFlag(Y);
					N = updateNFlag(Y);	
					break;					
					
				// -------------------- DEC - Decrement Memory --------------------		
				case (byte) 0xC6:
					// DEC - Decrement Memory - Zero Page
					data1 = memory[PC++];
					
					memory[data1]--;

					// update flags
					Z = updateZFlag(memory[data1]);
					N = updateNFlag(memory[data1]);	
					break;	
					
				case (byte) 0xD6:
					// DEC - Decrement Memory - Zero Page, X
					data1 = memory[PC++];
					memory[data1 + X]--;

					// update flags
					Z = updateZFlag(memory[data1 + X]);
					N = updateNFlag(memory[data1 + X]);	
					break;				
					
				case (byte) 0xCE:
					// DEC - Decrement Memory - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					memory[twoBytesToShort(data1, data2)]--;	

					// update flags
					Z = updateZFlag(memory[twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[twoBytesToShort(data2, data1)]);	
					break;
					
				case (byte) 0xDE:
					// DEC - Decrement Memory - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					memory[X + twoBytesToShort(data1, data2)]--;
					
					// update flags
					Z = updateZFlag(memory[X + twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[X + twoBytesToShort(data2, data1)]);					
					break;	
				
				// -------------------- DEX - Decrement X Register --------------------	
				case (byte) 0xCA:
					X--;
					// update flags
					Z = updateZFlag(X);
					N = updateNFlag(X);	
					break;
	
				// -------------------- DEY - Decrement Y Register --------------------				
				case (byte) 0x88:
					Y--;
					// update flags
					Z = updateZFlag(Y);
					N = updateNFlag(Y);	
					break;					
				
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Shifts 
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *					
					
				// -------------------- ASL - Arithmetic Shift Left --------------------		
				case (byte) 0x0A:
					// ASL - Arithmetic Shift Left - Accumulator
					C = (((byte)AC & 128) == 1)? true : false;	// check for carry
					AC = (byte)(AC << 2);

					// update flags			
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
				
				case (byte) 0x06:
					// ASL - Arithmetic Shift Left - Zero Page
					data1 = memory[PC++];
					C = (((byte) memory[data1] & 128) == 1)? true : false;	// check for carry
				
					memory[data1] = (byte)(memory[data1] << 2);

					// update flags
					Z = updateZFlag(memory[data1]);
					N = updateNFlag(memory[data1]);	
					break;	
					
				case (byte) 0x16:
					// ASL - Arithmetic Shift Left - Zero Page, X
					data1 = memory[PC++];
					C = (((byte) memory[data1 + X] & 128) == 1)? true : false;	// check for carry
					
					memory[data1 + X] = (byte)(memory[data1 + X] << 2);

					// update flags
					Z = updateZFlag(memory[data1 + X]);
					N = updateNFlag(memory[data1 + X]);	
					break;				
					
				case (byte) 0x0E:
					// ASL - Arithmetic Shift Left - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					C = (((byte) memory[twoBytesToShort(data2, data1)] & 128) == 1)? true : false;	// check for carry
					
					memory[twoBytesToShort(data2, data1)] = (byte)(memory[twoBytesToShort(data2, data1)] << 2);	

					// update flags
					Z = updateZFlag(memory[twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[twoBytesToShort(data2, data1)]);	
					break;
					
				case (byte) 0x1E:
					// ASL - Arithmetic Shift Left - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					C = (((byte) memory[X + twoBytesToShort(data2, data1)] & 128) == 1)? true : false;	// check for carry
					
					memory[X + twoBytesToShort(data2, data1)] = (byte)(memory[X + twoBytesToShort(data2, data1)] << 2);
					
					// update flags
					Z = updateZFlag(memory[X + twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[X + twoBytesToShort(data2, data1)]);					
					break;						
	
				// -------------------- LSR - Logical Shift Right --------------------		
				case (byte) 0x4A:
					// LSR - Logical Shift Right - Accumulator
					data2 = (byte)(AC & 1);		// bit 0
					C = (data2 == 1)? true : false;	// carry is now bit 0
					
					AC = (byte)((AC >> 2) + (128 * data2));

					// update flags			
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
				
				case (byte) 0x46:
					// LSR - Logical Shift Right - Zero Page
					data1 = memory[PC++];
					data2 = (byte)(memory[data1] & 1);		// bit 0
					C = (data2 == 1)? true : false;	// check for carry
				
					memory[data1] = (byte)((memory[data1] >> 2) + (128 * data2));

					// update flags
					Z = updateZFlag(memory[data1]);
					N = updateNFlag(memory[data1]);	
					break;	
					
				case (byte) 0x56:
					// LSR - Logical Shift Right - Zero Page, X
					data1 = memory[PC++];
					data2 = (byte)(memory[data1 + X] & 1);		// bit 0
					C = (data2 == 1)? true : false;	// check for carry
				
					memory[data1] = (byte)((memory[data1] >> 2) + (128 * data2));

					// update flags
					Z = updateZFlag(memory[data1 + X]);
					N = updateNFlag(memory[data1 + X]);	
					break;				
					
				case (byte) 0x4E:
					// LSR - Logical Shift Right - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					tmp = (byte)(memory[twoBytesToShort(data2, data1)] & 1); 	// bit 0
					C = (tmp == 1)? true : false;	// check for carry
					
					memory[twoBytesToShort(data2, data1)] = (byte)((memory[twoBytesToShort(data2, data1)] >> 2) + (128 * tmp));

					// update flags
					Z = updateZFlag(memory[twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[twoBytesToShort(data2, data1)]);	
					break;
					
				case (byte) 0x5E:
					// LSR - Logical Shift Right - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					tmp = (byte)(memory[X + twoBytesToShort(data2, data1)] & 1); 	// bit 0
					C = (tmp == 1)? true : false;	// check for carry
					
					memory[X + twoBytesToShort(data1, data2)] = (byte)((memory[X + twoBytesToShort(data1, data2)] >> 2) + (128 * tmp));
					
					// update flags
					Z = updateZFlag(memory[X + twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[X + twoBytesToShort(data2, data1)]);					
					break;						
				
				// -------------------- ROL - Rotate Left --------------------		
				case (byte) 0x2A:
					// ROL - Rotate Left - Accumulator
					data2 = (byte)(AC & 128);	// old bit 7
				
					AC = (byte)(AC << 1);
					if (C) AC++;	// bit 0 is filled with current value of the carry flag
						
					// update flags	
					C = (data2 == 128)? true : false; // check for carry
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
				
				case (byte) 0x26:
					// ROL - Rotate Left - Zero Page
					data1 = memory[PC++];
					data2 = (byte) (AC & 128);	// old bit 7
					
					memory[data1] = (byte)(memory[data1] << 1);
					if (C) memory[data1]++;		// bit 0 is filled with current value of the carry flag

					// update flags
					C = (data2 == 128)? true : false; // check for carry
					Z = updateZFlag(memory[data1]);
					N = updateNFlag(memory[data1]);	
					break;	
					
				case (byte) 0x36:
					// ROL - Rotate Left - Zero Page, X
					data1 = memory[PC++];
					data2 = (byte)(memory[data1 + X] & 128);	// old bit 7
					
					memory[data1 + X] = (byte)(memory[data1 + X] << 1);
					if (C) memory[data1 + X]++;		// bit 0 is filled with current value of the carry flag
					
					// update flags
					C = (data2 == 128)? true : false; // check for carry
					Z = updateZFlag(memory[data1 + X]);
					N = updateNFlag(memory[data1 + X]);	
					break;				
					
				case (byte) 0x2E:
					// ROL - Rotate Left - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					tmp = (byte)(memory[twoBytesToShort(data2, data1)] & 128);	// old bit 7

					memory[twoBytesToShort(data2, data1)] = (byte)(memory[twoBytesToShort(data2, data1)] << 1);
					if (C) memory[twoBytesToShort(data1, data2)]++;		// bit 0 is filled with current value of the carry flag

					// update flags
					C = (tmp == 128)? true : false; // check for carry
					Z = updateZFlag(memory[twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[twoBytesToShort(data2, data1)]);	
					break;
					
				case (byte) 0x3E:
					// ROL - Rotate Left - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					tmp = (byte)(memory[X + twoBytesToShort(data2, data1)] & 128);	// old bit 7 
					
					memory[X + twoBytesToShort(data2, data1)] = (byte)(memory[X + twoBytesToShort(data2, data1)] << 1);
					if (C) memory[X + twoBytesToShort(data2, data1)]++;		// bit 0 is filled with current value of the carry flag
					
					// update flags
					C = (tmp == 128)? true : false; // check for carry
					Z = updateZFlag(memory[X + twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[X + twoBytesToShort(data2, data1)]);					
					break;					
	
				// -------------------- ROR - Rotate Right --------------------		
				case (byte) 0x6A:
					// ROR - Rotate Right - Accumulator
					data2 = (byte)(AC & 1);	// old bit 0
				
					AC = (byte)(AC >> 1);
					if (C) AC += 128;	// bit 7 is filled with current value of the carry flag
					
					// update flags	
					C = (data2 == 1)? true : false;	// check for carry
					Z = updateZFlag(AC);
					N = updateNFlag(AC);	
					break;	
				
				case (byte) 0x66:
					// ROR - Rotate Right - Zero Page
					data1 = memory[PC++];
					data2 = (byte)(AC & 1);	// old bit 0
					
					memory[data1] = (byte)(memory[data1] >> 1);
					if (C) memory[data1] += 128;		// bit 7 is filled with current value of the carry flag
					
					// update flags
					C = (data2 == 1)? true : false; // check for carry
					Z = updateZFlag(memory[data1]);
					N = updateNFlag(memory[data1]);	
					break;	
					
				case (byte) 0x76:
					// ROR - Rotate Right - Zero Page, X
					data1 = memory[PC++];
					data2 = (byte)(memory[data1 + X] & 1);	// old bit 0
					
					memory[data1 + X] = (byte)(memory[data1 + X] >> 1);
					if (C) memory[data1 + X] += 128;		// bit 7 is filled with current value of the carry flag
					
					// update flags
					C = (data2 == 1)? true : false; // check for carry
					Z = updateZFlag(memory[data1 + X]);
					N = updateNFlag(memory[data1 + X]);	
					break;				
					
				case (byte) 0x6E:
					// ROR - Rotate Right - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					tmp = (byte)(memory[twoBytesToShort(data2, data1)] & 1);	// old bit 0

					memory[twoBytesToShort(data2, data1)] = (byte)(memory[twoBytesToShort(data2, data1)] >> 1);
					if (C) memory[twoBytesToShort(data2, data1)] += 128;		// bit 7 is filled with current value of the carry flag

					// update flags
					C = (tmp == 1)? true : false; // check for carry
					Z = updateZFlag(memory[twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[twoBytesToShort(data2, data1)]);	
					break;
					
				case (byte) 0x7E:
					// ROR - Rotate Right - Absolute,X (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					tmp = (byte)(memory[X + twoBytesToShort(data2, data1)] & 1);	// old bit 0 
					
					memory[X + twoBytesToShort(data2, data1)] = (byte)(memory[X + twoBytesToShort(data2, data1)] >> 1);
					if (C) memory[X + twoBytesToShort(data2, data1)] += 128;		// bit 7 is filled with current value of the carry flag
					
					// update flags
					C = (tmp == 1)? true : false; // check for carry
					Z = updateZFlag(memory[X + twoBytesToShort(data2, data1)]);
					N = updateNFlag(memory[X + twoBytesToShort(data2, data1)]);					
					break;					
			
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Jumps & Calls 
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *					
				
				// -------------------- JMP - Jump --------------------	
				case (byte) 0x4C:
					// JMP - Jump - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					PC = (short)twoBytesToShort(data2, data1);
					break;
				case (byte) 0x6C:
					// JMP - Jump - (Indirect) (16-bit address)
					data1 = memory[PC++]; 
					data2 = memory[PC++]; 
					PC = (short)twoBytesToShort(memory[twoBytesToShort(data2, data1)], memory[twoBytesToShort(data2, data1) + 1]);				
					break;		
			
				// -------------------- JSR - Jump to Subroutine --------------------	
				case (byte) 0x20:
					// JMP - Jump - Absolute (16-bit address)
					data1 = memory[PC++];
					data2 = memory[PC++];
					
					PC--;
					memory[STARTING_STACK_LOCATION - (SP++ & 0xFF)] = (byte)(PC % 256);	// least significant byte
					memory[STARTING_STACK_LOCATION - (SP++ & 0xFF)] = (byte)(PC / 256);	// most significant byte
					
					PC = (short)twoBytesToShort(data1, data2);
					
					break;
				
				// -------------------- RTS - Return from Subroutine --------------------	
				case (byte) 0x60:
					data1 = memory[STARTING_STACK_LOCATION - (SP++ & 0xFF)];
					data2 = memory[STARTING_STACK_LOCATION - (SP++ & 0xFF)];
					PC = (short)twoBytesToShort(data2, data1);
					break;
					
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Branches
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *					
				
				// -------------------- BCC - Branch if carry flag clear --------------------
				case (byte) 0x90:
					data1 = memory[PC++];
					if (!C) PC += data1;
					break;

				// -------------------- BCS - Branch if carry flag set --------------------
				case (byte) 0xB0:
					data1 = memory[PC++];
					if (C) PC += data1;
					break;					

				// -------------------- BEQ - Branch if equal --------------------
				case (byte) 0xF0:
					data1 = memory[PC++];
					if (Z) PC += data1;
					break;	
					
				// -------------------- BMI - Branch if minus --------------------
				case (byte) 0x30:
					data1 = memory[PC++];
					if (N) PC += data1;
					break;
					
				// -------------------- BNE - Branch if not equal --------------------
				case (byte) 0xD0:
					data1 = memory[PC++];
					if (!Z) PC += data1;
					break;	
					
				// -------------------- BPL - Branch if positive --------------------
				case (byte) 0x10:
					data1 = memory[PC++];
					if (!N) PC += data1;
					break;	
				
				// -------------------- BVC - Branch if overflow clear --------------------
				case (byte) 0x50:
					data1 = memory[PC++];
					if (!V) PC += data1;
					break;			
					
				// -------------------- BVS - Branch if overflow set --------------------
				case (byte) 0x70:
					data1 = memory[PC++];
					if (V) PC += data1;
					break;			
					
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// Status Flag Changes
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *					
				
				// -------------------- CLC - Clear Carry Flag --------------------
				case (byte) 0x18:
					C = false;
					break;					
				
				// -------------------- CLD - Clear Decimal Flag --------------------
				case (byte) 0xD8:
					D = false;
					break;
					
				// -------------------- CLI - Clear Interrupt Flag --------------------
				case (byte) 0x58:
					I = false;
					break;
					
				// -------------------- CLV - Clear Overflow Flag --------------------
				case (byte) 0xB8:
					V = false;
					break;						
				
				// -------------------- SEC - Set Carry Flag --------------------
				case (byte) 0x38:
					C = true;
					break;	
					
				// -------------------- SED - Set Decimal Flag --------------------
				case (byte) 0xF8:
					D = true;
					break;
					
				// -------------------- SEI - Set Interrupt Flag --------------------
				case (byte) 0x78:
					I = true;
					break;
					
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
				// System Functions
				// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *					
				
				// -------------------- BRK - Force Interrupt --------------------	
				case (byte) 0x00:
					memory[STARTING_STACK_LOCATION - (SP++)] = flagsToByte(N, V, G, B, D, I, Z, C);		
					memory[STARTING_STACK_LOCATION - (SP++)] = (byte)((PC >> 8) & 0xFF);	
					memory[STARTING_STACK_LOCATION - (SP++)] = (byte)(PC & 0x00FF);			
					
					PC = (short)twoBytesToShort(memory[0xFFFF], memory[0xFFFE]);
					break;
				
				// -------------------- RTI - Return from Interrupt --------------------	
				case (byte) 0x40:
					// pull processor status
					tmp = memory[STARTING_STACK_LOCATION - (SP--)];
					C = (((byte)tmp & 1) == 1)? true : false;
					Z = (((byte)tmp & 2) == 1)? true : false;
					I = (((byte)tmp & 4) == 1)? true : false;
					D = (((byte)tmp & 8) == 1)? true : false;
					B = (((byte)tmp & 16) == 1)? true : false;
					G = (((byte)tmp & 32) == 1)? true : false;
					V = (((byte)tmp & 64) == 1)? true : false;
					N = (((byte)tmp & 128) == 1)? true : false;		
					
					data1 = memory[STARTING_STACK_LOCATION - (SP--)];
					data2 = memory[STARTING_STACK_LOCATION - (SP--)];				
					
					PC = (short)twoBytesToShort(data2, data1);
					break;	
					
					
				// -------------------- NOP - No Operation --------------------
				case (byte) 0xEA:
					break;					
					
				default:
					System.out.println("Error: Couldn't find instruction for opcode: " + opcode);
					break;
			}
		}
		// final update
		ui.updateRegisters(PC, AC, X, Y, SR, SP);
		ui.updateFlags(N, V, G, B, D, I, Z, C);
		ui.updateText(fileLines, PC);		
	}

	// take string in base 16 and return byte
	public static byte strToByte(String str) {
		return (byte)Integer.parseInt(str, 16);
	}
	
	// take string in base 16 and return short
	public static short strToShort(String str) {
		return (short)Integer.parseInt(str, 16);
	}	
	
	// take string in base 16 and return int
	public static int strToInt(String str) {
		return (int)Integer.parseInt(str, 16);
	}
	
	// take 2 bytes and create a short
	public static int twoBytesToShort(byte b1, byte b2) {
		short ub1 = (short)(b1 & 0xFF);
		short ub2 = (short)(b2 & 0xFF);
        return ((ub1*256 + ub2) & 0xFFFF);
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
	public static boolean updateCFlag (byte AC, int opcode, short input) {
		// opcode 1 = Add with Carry
		if (opcode == 1) {
			if (AC > 0 && input > 0) {
				// both positive
				if (AC + input > 127) 
					return true;
				return false;
			} else if (AC < 0 && input < 0) {
				// both negative
				if (AC + input < -128)
					return true;
				return false;
			} else {
				// one postive and one negative
				return false;
			}
		// opcode 2 = Subtract with carry
		} else if (opcode == 2) {
			if (AC > 0 && input > 0) {
				if (AC > input)
					return false;
				return true;
			} else if (AC > 0 && input < 0) {
				if (AC - input > 127)
					return true;
				return false;
			} else if (AC < 0 && input > 0) {
				if (AC - input < -128)
					return true;
				return false;
			} else if (AC < 0 && input < 0) {
				if (AC > input)
					return false;
				return true;
			} else {
				return false;
			}		
		} else {
			System.out.println("Error: Couldn't find opcode: " + opcode + " in updateCFlag");
		}			
		return false;	
	}	
	
	// return true if overflow (i.e. sign bit is incorrect)
	// this will occur if adding 2 positive numbers that sum to > 127
	// or adding 2 negative numbers that sum to < -128
	public static boolean updateVFlag (byte AC, int opcode, short input) {
		// opcode 1 = Add with Carry
		if (opcode == 1) {
			byte tmp = (byte) (AC + input);
			if (AC > 0 && input > 0 && tmp < 0)
				return true;
			if (AC < 0 && input < 0 && tmp > 0)
				return true;
			return false;
			
		// opcode 2 = Subtract with carry
		} else if (opcode == 2) {
			byte tmp = (byte) (AC - input);
			if (AC > 0 && input < 0 && tmp < 0)
				return true;
			if (AC < 0 && input > 0 && tmp > 0)
				return true;
			return false;
		} else {
			System.out.println("Error: Couldn't find opcode: " + opcode + " in updateVFlag");
		}		
		return false;		
	}		
}
