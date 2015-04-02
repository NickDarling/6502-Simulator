import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;

public class UserInterface extends JFrame {
    
	//initializing all variables for later use
	JButton stepFile;
	JTextField pcReg;
	JTextField xReg;
	JTextField yReg;
	JTextField accReg;
	JTextField statusReg;
	JTextField spReg;
	    
	JTextField nFlag;
	JTextField vFlag;
	JTextField gFlag;
	JTextField bFlag;
	JTextField dFlag;
	JTextField iFlag;
	JTextField zFlag;
	JTextField cFlag;
	
	JTextArea inputTextField;
	
	int intPC = 0;
	String HexStringPC = null;
	
	int intN = 0;
	int intV = 0;
	int intG = 0;
	int intB = 0;
	int intD = 0;
	int intI = 0;
	int intZ = 0;
	int intC = 0;
        
	int lineIndicatorOffset = 45;
    boolean pressed = false;
    boolean getFile = false;

    public void createUI() {  	
    	setTitle("6502-Simulator");
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);
  
    	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    	this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);       
        
        // step button
        JButton stepButton = new JButton("Step");     
        stepButton.setBackground(Color.white);
        stepButton.setBounds(585, 395, 100, 40);
        add(stepButton);

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	pressed = true;
            }
          };
         stepButton.addActionListener(actionListener);    
          
        // open file button
        JButton openButton = new JButton("Open");     
        openButton.setBackground(Color.white);
        openButton.setBounds(450, 395, 100, 40);
        add(openButton);

        ActionListener actionListener2 = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	getFile = true;
            }
          };
          openButton.addActionListener(actionListener2);             
        
        //*********************************************************
        //labels for registers
        //*********************************************************
        JLabel testlabel = new JLabel("Input Code:");
        add(testlabel);
        testlabel.setBounds(10, 15, 100, 25);
        
        JLabel registerLabel = new JLabel("Registers:");
        add(registerLabel);
        registerLabel.setBounds(450, 15, 80, 25);
        
        JLabel pcLabel = new JLabel("Program Counter");
        add(pcLabel);
        pcLabel.setBounds(450, 50, 150, 15);
        
        JLabel xLabel = new JLabel("Register x");
        add(xLabel);
        xLabel.setBounds(450, 110, 150, 15);
        
        JLabel yLabel = new JLabel("Register y");
        add(yLabel);
        yLabel.setBounds(450, 170, 150, 15);
        
        JLabel accLabel = new JLabel("Accumulator");
        add(accLabel);
        accLabel.setBounds(600, 50, 150, 15);
        
        JLabel statusLabel = new JLabel("Status Register");
        add(statusLabel);
        statusLabel.setBounds(600, 110, 150, 15);
        
        JLabel spLabel = new JLabel("Stack Pointer");
        add(spLabel);
        spLabel.setBounds(600, 170, 150, 15);
        
        JLabel flagLabel = new JLabel("Flags:");
        add(flagLabel);
        flagLabel.setBounds(450, 260, 150, 15);
        
        //*********************************************************
        //labels for flags
        //*********************************************************
        
        JLabel nLabel = new JLabel("N");
        add(nLabel);
        nLabel.setBounds(450, 295, 15, 15);
        
        JLabel vLabel = new JLabel("V");
        add(vLabel);
        vLabel.setBounds(480, 295, 15, 15);
        
        JLabel gLabel = new JLabel("G");
        add(gLabel);
        gLabel.setBounds(510, 295, 15, 15);
        
        JLabel bLabel = new JLabel("B");
        add(bLabel);
        bLabel.setBounds(540, 295, 15, 15);
        
        JLabel dLabel = new JLabel("D");
        add(dLabel);
        dLabel.setBounds(570, 295, 15, 15);
        
        JLabel iLabel = new JLabel("I");
        add(iLabel);
        iLabel.setBounds(600, 295, 15, 15);
        
        JLabel zLabel = new JLabel("Z");
        add(zLabel);
        zLabel.setBounds(630, 295, 15, 15);
        
        JLabel cLabel = new JLabel("C");
        add(cLabel);
        cLabel.setBounds(660, 295, 15, 15);
        
        
        //*********************************************************
        //text fields for registers
        //*********************************************************
        
        pcReg = new JTextField(50);
        add(pcReg);
        pcReg.setEditable(false);
        pcReg.setBackground(Color.white);
        pcReg.setBounds(450, 70, 80, 25);
        
        xReg = new JTextField(50);
        add(xReg);
        xReg.setEditable(false);
        xReg.setBackground(Color.white);
        xReg.setBounds(450, 130, 80, 25);
        
        yReg = new JTextField(50);
        add(yReg);
        yReg.setEditable(false);
        yReg.setBackground(Color.white);
        yReg.setBounds(450, 190, 80, 25);
        
        accReg = new JTextField(50);
        add(accReg);
        accReg.setEditable(false);
        accReg.setBackground(Color.white);
        accReg.setBounds(600, 70, 80, 25);
        
        statusReg = new JTextField(50);
        add(statusReg);
        statusReg.setEditable(false);
        statusReg.setBackground(Color.white);
        statusReg.setBounds(600, 130, 80, 25);
        
        spReg = new JTextField(50);
        add(spReg);
        spReg.setEditable(false);
        spReg.setBackground(Color.white);
        spReg.setBounds(600, 190, 80, 25);
        
        //*********************************************************
        //text fields for flags
        //*********************************************************
        nFlag = new JTextField(10);
        add(nFlag);
        nFlag.setEditable(false);
        nFlag.setBackground(Color.white);
        nFlag.setBounds(450, 310, 25, 25);
        
        vFlag = new JTextField(10);
        add(vFlag);
        vFlag.setEditable(false);
        vFlag.setBackground(Color.white);
        vFlag.setBounds(480, 310, 25, 25);
        
        gFlag = new JTextField(10);
        add(gFlag);
        gFlag.setEditable(false);
        gFlag.setBackground(Color.white);
        gFlag.setBounds(510, 310, 25, 25);
        
        bFlag = new JTextField(10);
        add(bFlag);
        bFlag.setEditable(false);
        bFlag.setBackground(Color.white);
        bFlag.setBounds(540, 310, 25, 25);
        
        dFlag = new JTextField(10);
        add(dFlag);
        dFlag.setEditable(false);
        dFlag.setBackground(Color.white);
        dFlag.setBounds(570, 310, 25, 25);
        
        iFlag = new JTextField(10);
        add(iFlag);
        iFlag.setEditable(false);
        iFlag.setBackground(Color.white);
        iFlag.setBounds(600, 310, 25, 25);
        
        zFlag = new JTextField(10);
        add(zFlag);
        zFlag.setEditable(false);
        zFlag.setBackground(Color.white);
        zFlag.setBounds(630, 310, 25, 25); 
        
        cFlag = new JTextField(10);
        add(cFlag);
        cFlag.setEditable(false);
        cFlag.setBackground(Color.white);
        cFlag.setBounds(660, 310, 25, 25); 
        
        
        //*********************************************************
        //text field for input text
        //*********************************************************
        inputTextField = new JTextArea(400, 450);
        inputTextField.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(inputTextField); 
        inputTextField.setEditable(false);
        inputTextField.setFont(new Font("Courier New", Font.PLAIN, 12));
        add(inputTextField);
        inputTextField.setBackground(Color.white);
        inputTextField.setBounds(10, 50, 400, 450);
        
    }
    
    //this method converts the file chosen by the user to a string
    //the string is returned to the main program to be interpreted
    public String fileUpload() {
	try {
        StringBuilder sb = new StringBuilder();
        String line = null;
                
	JFileChooser chooser = new JFileChooser();
	chooser.showOpenDialog(null);
	File f = chooser.getSelectedFile();
	String filename = f.getAbsolutePath();

	try {
		FileReader reader = new FileReader(filename);
		BufferedReader br = new BufferedReader(reader);
                    
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
	}catch (Exception x) {
		System.out.println("Can't load file");
	}
	inputTextField.setText(sb.toString());
	getFile = true;
	return sb.toString();  
	} catch (Exception e) {
		return null;
	}   
    }    
    
    //this method prints the values of registers to the text fields on the ui
    public void updateRegisters (short PC, byte AC, byte X, byte Y, byte SR, byte SP) {

    	// short --> int --> hex string
    	intPC = PC;  
    	HexStringPC = Integer.toHexString(intPC);
    	pcReg.setText("0x0" + HexStringPC);
    	    
    	//converts bytes to strings for printing
        accReg.setText(String.valueOf(AC));
        xReg.setText(String.valueOf(X));
        yReg.setText(String.valueOf(Y));
        statusReg.setText(String.valueOf(SR));
        spReg.setText(String.valueOf(SP));
      
        
    }
    
    //this method prints the values of flags to the text fields on the ui
    public void updateFlags (boolean N, boolean V, boolean G, boolean B, boolean D, boolean I, boolean Z, boolean C) {
        
    	//converts flags to 1 or 0    
        intN = (N) ? 1 : 0;
	intV = (V) ? 1 : 0; 
	intG = (G) ? 1 : 0; 
	intB = (B) ? 1 : 0;
	intD = (D) ? 1 : 0;
	intI = (I) ? 1 : 0; 
	intZ = (Z) ? 1 : 0; 
	intC = (C) ? 1 : 0;     
        
	//converts flag integers to strings for printing
        nFlag.setText(String.valueOf(intN));        
        vFlag.setText(String.valueOf(intV));        
        gFlag.setText(String.valueOf(intG));
        bFlag.setText(String.valueOf(intB));
        dFlag.setText(String.valueOf(intD));
        iFlag.setText(String.valueOf(intI));
        zFlag.setText(String.valueOf(intZ));
        cFlag.setText(String.valueOf(intC));
                
        
    }   
    
    public void updateText (String [] lines, int currAddr) {
    	String text = "\n";
    	
    	for (int i = 0; i < lines.length; i++) {
    		if (!lines[i].trim().isEmpty()) {
	        	String []split = (lines[i].substring(1)).split("\\s+");	
	    		if (split.length > 1 && Integer.parseInt(split[0], 16) == currAddr) {
	    			text += "  >>>   ";
	    			text += lines[i];
	    			for (int j = lines[i].length(); j < lineIndicatorOffset; j++)
	    				text += " ";
	    			text += "<<<";
	    		} else {
	    			text += "        ";
	    			text += lines[i];
	    		}
	    		text += "\n";
    		} else {
    			text += "\n";
    		}
    	}
    	inputTextField.setText(text);
    }
}
