import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RiskCalculator {

	private JFrame frmRiskCalculator;

	private JComboBox symbolCombo; 
	private JTextField textField_miniTick;
	private JTextField textField_tickValue;
	private JTextField textField_ATR;
	private JTextField textField_atRisk;
	private JTextField textField_atrValue;
	private JTextField textField_contracts;
	private JLabel lblContracts;
	private JLabel lblEntry;
	private JTextField textField_entry;
	private JLabel lblLongStop;
	private JTextField textField_longStop;
	private JLabel lblShortStop;
	private JTextField textField_shortStop;
	private JLabel lblMaximumHolding;
	
	List<RiskUnit> riskList = new ArrayList<RiskUnit>(); 
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
				
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RiskCalculator window = new RiskCalculator();
					window.frmRiskCalculator.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public RiskCalculator() {
		initialize();
	}


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		frmRiskCalculator = new JFrame();
		frmRiskCalculator.setTitle("Risk Management");
		frmRiskCalculator.setBounds(100, 100, 583, 396);
		frmRiskCalculator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRiskCalculator.getContentPane().setLayout(null);
		
		JButton btnNewButton = new JButton("Calculate");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				calButtonPressed();
			}


		});
		btnNewButton.setBounds(474, 7, 91, 23);
		frmRiskCalculator.getContentPane().add(btnNewButton);
		
		symbolCombo = new JComboBox();
		symbolCombo.setMaximumRowCount(30);
		symbolCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JComboBox cb = (JComboBox)arg0.getSource();
		        int index = (int)cb.getSelectedIndex();  
				populate(index); 
			}
		});
		
		symbolCombo.setToolTipText("Symbols");
		symbolCombo.setBounds(0, 0, 110, 20);
		frmRiskCalculator.getContentPane().add(symbolCombo);
		
		JLabel lblMinitick = new JLabel("MiniTick:");
		lblMinitick.setBounds(10, 33, 57, 14);
		frmRiskCalculator.getContentPane().add(lblMinitick);
		
		textField_miniTick = new JTextField();
		textField_miniTick.setColumns(10);
		textField_miniTick.setBounds(77, 31, 86, 20);
		frmRiskCalculator.getContentPane().add(textField_miniTick);
		
		JLabel lblTickvalue = new JLabel("TickValue:");
		lblTickvalue.setBounds(279, 36, 56, 14);
		frmRiskCalculator.getContentPane().add(lblTickvalue);
		
		textField_tickValue = new JTextField();
		textField_tickValue.setEditable(false);
		textField_tickValue.setColumns(10);
		textField_tickValue.setBounds(345, 30, 86, 20);
		frmRiskCalculator.getContentPane().add(textField_tickValue);
		
		JLabel lblTimesOf = new JLabel("N-ATR or N-stddev:");
		lblTimesOf.setBounds(10, 123, 190, 14);
		frmRiskCalculator.getContentPane().add(lblTimesOf);
		
		textField_ATR = new JTextField();
		textField_ATR.setColumns(10);
		textField_ATR.setBounds(151, 120, 86, 20);
		frmRiskCalculator.getContentPane().add(textField_ATR);
		
		JLabel lblAtRisk = new JLabel("At Risk:");
		lblAtRisk.setBounds(10, 148, 46, 14);
		frmRiskCalculator.getContentPane().add(lblAtRisk);
		
		textField_atRisk = new JTextField();
		textField_atRisk.setColumns(10);
		textField_atRisk.setBounds(151, 145, 86, 20);
		frmRiskCalculator.getContentPane().add(textField_atRisk);
		
		JLabel lblAtrValue = new JLabel("ATR Value:");
		lblAtrValue.setBounds(307, 123, 68, 14);
		frmRiskCalculator.getContentPane().add(lblAtrValue);
		
		textField_atrValue = new JTextField();
		textField_atrValue.setEditable(false);
		textField_atrValue.setColumns(10);
		textField_atrValue.setBounds(424, 120, 86, 20);
		frmRiskCalculator.getContentPane().add(textField_atrValue);
		
		textField_contracts = new JTextField();
		textField_contracts.setEditable(false);
		textField_contracts.setColumns(10);
		textField_contracts.setBounds(424, 148, 86, 20);
		frmRiskCalculator.getContentPane().add(textField_contracts);
		
		lblContracts = new JLabel("Contracts:(2*ATR)");
		lblContracts.setBounds(307, 148, 107, 14);
		frmRiskCalculator.getContentPane().add(lblContracts);
		
		lblEntry = new JLabel("Entry:");
		lblEntry.setBounds(10, 204, 35, 14);
		frmRiskCalculator.getContentPane().add(lblEntry);
		
		textField_entry = new JTextField();
		textField_entry.setColumns(10);
		textField_entry.setBounds(55, 201, 86, 20);
		frmRiskCalculator.getContentPane().add(textField_entry);
		
		lblLongStop = new JLabel("Long Stop:");
		lblLongStop.setBounds(186, 204, 78, 14);
		frmRiskCalculator.getContentPane().add(lblLongStop);
		
		textField_longStop = new JTextField();
		textField_longStop.setEditable(false);
		textField_longStop.setColumns(10);
		textField_longStop.setBounds(263, 201, 86, 20);
		frmRiskCalculator.getContentPane().add(textField_longStop);
		
		lblShortStop = new JLabel("Short Stop:");
		lblShortStop.setBounds(378, 204, 68, 14);
		frmRiskCalculator.getContentPane().add(lblShortStop);
		
		textField_shortStop = new JTextField();
		textField_shortStop.setEditable(false);
		textField_shortStop.setColumns(10);
		textField_shortStop.setBounds(445, 201, 86, 20);
		frmRiskCalculator.getContentPane().add(textField_shortStop);
		
		lblMaximumHolding = new JLabel("Maximum holding: 251 Bars");
		lblMaximumHolding.setBounds(10, 276, 245, 14);
		frmRiskCalculator.getContentPane().add(lblMaximumHolding);
		
		//Util.save(); // for test json. 
		pre_pop(); 		
	}
	
	private void pre_pop() {
		riskList = Util.load(); 
		
		for(int i=0; i<riskList.size(); i++) {
			symbolCombo.addItem(riskList.get(i).symbol + "," + riskList.get(i).tickValue);
	    }
		
		populate(0);
	}
	
	private void populate(int index) {
        DecimalFormat df = new DecimalFormat("#.######");  // 6 digits after . 
        // System.out.print(df.format(d));
        textField_miniTick.setText((df.format(riskList.get(index).miniTick))); 
        textField_tickValue.setText((riskList.get(index)).tickValue.toString()); 
	}
	
	private void calButtonPressed() {
		
		// String miniTickStr = textField_miniTick.getText().trim();
		
		try {
			double miniTick= Double.parseDouble(textField_miniTick.getText().trim()); 
			float ATRs= Float.parseFloat(textField_ATR.getText().trim()); 
			float tickValue= Float.parseFloat(textField_tickValue.getText().trim());  
			float atRisk=Float.parseFloat(textField_atRisk.getText().trim());  
			
			float atrValue = (float) (ATRs/miniTick*tickValue);
			float contract = (float) (atRisk/(2*atrValue)); 
			
			textField_atrValue.setText("" + atrValue);
			textField_contracts.setText("" + contract);
			
		    try {
		    	float entryvalue=Float.parseFloat(textField_entry.getText().trim()); 
		    	float longstop = entryvalue-2*ATRs; 
		    	float shortstop = entryvalue+2*ATRs;
		    	
		    	textField_longStop.setText(""+longstop);
		    	textField_shortStop.setText(""+shortstop);
		    } catch(NumberFormatException e) { 
		    }
		    
		} catch(NumberFormatException e) {
			JOptionPane.showMessageDialog(frmRiskCalculator, "Fill all ATR and money at risk please.");
		}		
				
	}
	
}



