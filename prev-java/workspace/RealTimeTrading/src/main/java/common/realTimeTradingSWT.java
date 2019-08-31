package common;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import spyder.OPTStrategy;
import spyder.OPTStrategyWrap;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class realTimeTradingSWT {

	DataEngine dataEngine = new DataEngine(3);  // Trading uses 3 and 4, data uses 5. 
	OrderEngine orderEngine = new OrderEngine(4); 
	
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		initObj(); 
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					realTimeTradingSWT window = new realTimeTradingSWT();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static void initObj() {
		// environment objects init	
	}

	/**
	 * Create the application.
	 */
	public realTimeTradingSWT() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		final JCheckBox chckbxSellOptions = new JCheckBox("Sell Options");
		chckbxSellOptions.setBounds(36, 21, 97, 23);
		frame.getContentPane().add(chckbxSellOptions);
		
		JButton btnSOStart = new JButton("Start");
		btnSOStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bottonClicked(chckbxSellOptions, arg0); 
			}
		});
		btnSOStart.setBounds(196, 21, 91, 23);
		frame.getContentPane().add(btnSOStart);
		
		final JCheckBox chckbxOtherStrategy = new JCheckBox("Other Strategy");
		chckbxOtherStrategy.setBounds(36, 143, 125, 23);
		frame.getContentPane().add(chckbxOtherStrategy);
		
		JButton button = new JButton("Start");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bottonClicked(chckbxOtherStrategy, arg0); 
			}
		});
		button.setBounds(196, 143, 91, 23);
		frame.getContentPane().add(button);
		
		final JCheckBox chckbxDownload = new JCheckBox("Download");
		chckbxDownload.setBounds(36, 73, 97, 23);
		frame.getContentPane().add(chckbxDownload);
		
		JButton buttonDLStart = new JButton("Start");
		buttonDLStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bottonClicked(chckbxDownload, arg0); 
			}
		});
		buttonDLStart.setBounds(196, 73, 91, 23);
		frame.getContentPane().add(buttonDLStart);
	}

	protected void bottonClicked(JCheckBox chckbx, ActionEvent arg0) {
		if (chckbx.isSelected()) {
			if ( ((JButton)arg0.getSource()).getText().equals("Start")) { 
				if (startService(chckbx.getText())) 
					((JButton)arg0.getSource()).setText("Stop");
			}
			else if ( ((JButton)arg0.getSource()).getText().equals("Stop")) {
				if (stopService(chckbx.getText())) 
					((JButton)arg0.getSource()).setText("Start");
			}
		}
	}
	
	private boolean startService(String text) {
		// from the text, get to know which service to call. 
		boolean retVal = false; 
		if (text.equalsIgnoreCase("Sell Options")) {
			retVal = startUpSellOptions(); 
		} else
		if (text.equalsIgnoreCase("Download")) {
			retVal = startUpDownload(); 
		}
		
		 
		return retVal;
	}
	
	private boolean startUpDownload() {
		String location = "D:\\CommonData\\Stocks\\config\\symbols.txt";
		return DownloadDataWrap.parseWrap(location, dataEngine);
	}

	private boolean startUpSellOptions() {
		String location = "D:\\CommonData\\Stocks\\config\\sellopt.json";
		return OPTStrategyWrap.parseWrap(location, dataEngine, orderEngine);
	}

	private boolean stopService(String text) {
	 
		return true;
	}
}
