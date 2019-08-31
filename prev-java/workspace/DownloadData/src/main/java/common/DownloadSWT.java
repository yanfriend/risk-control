package common;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import download.DownloadUtil;
import download.IBtoMt4Util;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import javax.swing.JSeparator;


public class DownloadSWT {

	DataEngine dataEngine = new DataEngine(5);  // 3 and 4 for trading, 5 for data
	//OrderEngine orderEngine = new OrderEngine(4);
	
	Timer hourlyTimer = new Timer();
	Timer dailyTimer = new Timer(); 
	
	private JFrame frmDownload;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		initObj(); 
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DownloadSWT window = new DownloadSWT();
					window.frmDownload.setVisible(true);
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
	public DownloadSWT() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDownload = new JFrame();
		frmDownload.setTitle("Download");
		frmDownload.setBounds(100, 100, 450, 366);
		frmDownload.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDownload.getContentPane().setLayout(null);
		
		final JCheckBox chckbxOtherStrategy = new JCheckBox("Other Strategy");
		chckbxOtherStrategy.setBounds(36, 309, 125, 23);
		frmDownload.getContentPane().add(chckbxOtherStrategy);
		
		JButton button = new JButton("Start");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bottonClicked(chckbxOtherStrategy, arg0); 
			}
		});
		button.setBounds(196, 309, 91, 23);
		frmDownload.getContentPane().add(button);
		
		final JCheckBox chckbxIBHourly = new JCheckBox("IB Hourly and Daily Continuous");
		chckbxIBHourly.setBounds(10, 25, 194, 23);
		frmDownload.getContentPane().add(chckbxIBHourly);
		
		JButton buttonDLStart = new JButton("Start Service");
		buttonDLStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bottonClicked(chckbxIBHourly, arg0); 
			}
		});
		buttonDLStart.setBounds(210, 25, 149, 23);
		frmDownload.getContentPane().add(buttonDLStart);
		
		JButton btnNewButton = new JButton("IB Hourly to simu Daily");
		btnNewButton.setEnabled(false);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DownloadUtil.iBHourlyToSimuDaily();
			}
		});
		btnNewButton.setBounds(96, 162, 192, 23);
		frmDownload.getContentPane().add(btnNewButton);
		
		JButton btnIbHourlyTo = new JButton("IB Hourly to MT4");
		btnIbHourlyTo.setEnabled(false);
		btnIbHourlyTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IBtoMt4Util.iBHourlyToMT4();
			}
		});
		btnIbHourlyTo.setBounds(138, 196, 149, 23);
		frmDownload.getContentPane().add(btnIbHourlyTo);
		
		JButton btnAmidbToAmidaily = new JButton("AmiDB to AmiDaily");
		btnAmidbToAmidaily.setEnabled(false);
		btnAmidbToAmidaily.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DownloadUtil.amiDBToAmiDaily();
			}
		});
		btnAmidbToAmidaily.setBounds(138, 230, 149, 23);
		frmDownload.getContentPane().add(btnAmidbToAmidaily);
		
		JButton btnIbDailyDownload = new JButton("IB Daily download");
		btnIbDailyDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startUpDownload(2);  
			}
		});
		btnIbDailyDownload.setBounds(210, 72, 149, 23);
		frmDownload.getContentPane().add(btnIbDailyDownload);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 117, 422, 2);
		frmDownload.getContentPane().add(separator);
	}

	protected void bottonClicked(JCheckBox chckbx, ActionEvent arg0) {
		if (chckbx.isSelected()) {
			if ( ((JButton)arg0.getSource()).getText().startsWith("Start")) { 
				if (startService(chckbx.getText()))   
					((JButton)arg0.getSource()).setText("Stop");
			}
			else if ( ((JButton)arg0.getSource()).getText().startsWith("Stop")) {
				if (stopService(chckbx.getText())) 
					((JButton)arg0.getSource()).setText("Start Service");
			}
		}
	}
	
	private boolean startService(String string) {
		// from the text, get to know which service to call. 
		boolean retVal = true; 
		if (!string.startsWith("IB Hourly")) return false; 

	     Calendar hourlyCld = Calendar.getInstance();  
         hourlyCld.set(Calendar.MINUTE, 0);  
	     hourlyCld.set(Calendar.SECOND, 15);  
	     Date hourlyDate = hourlyCld.getTime();  
		
	     Calendar dailyCld = Calendar.getInstance();  
	     dailyCld.set(Calendar.HOUR_OF_DAY, 18);  
	     dailyCld.set(Calendar.MINUTE, 30);   
	     dailyCld.set(Calendar.SECOND, 0);   // 18:30 
	     Date dailyDate = dailyCld.getTime();  
		
		// set up a timer to download at the beginning of each hour. 	     
	     int period = 60*60*1000;
        
        hourlyTimer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		Date now = new Date(); 
                int minutes = now.getMinutes(); 
                
                System.out.println("hourly timer downloading at "+now.getHours()+":"+minutes+":"+now.getSeconds());
        		startUpDownload(1);         	    
        	}
        }, hourlyDate, period);
        
        dailyTimer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		Date now = new Date(); 
                int minutes = now.getMinutes(); 
                Calendar tmpCal =  Calendar.getInstance();  
                if (tmpCal.get(Calendar.HOUR_OF_DAY)>=19) return; // don't download after 7pm. not triggered before 6:30pm.
                System.out.println("daily timer downloading at "+now.getHours()+":"+minutes+":"+now.getSeconds());
        		startUpDownload(2);         	    
        	}
        }, dailyDate, 24*period);
        
		return retVal;
	}
	
	private boolean startUpDownload(int downloadtype) {
		String location = "D:\\CommonData\\Stocks\\config\\symbols.txt";
		return DownloadDataWrap.parseWrap(dataEngine, location, downloadtype);
	}

	private boolean stopService(String text) {
		if (!text.equalsIgnoreCase("IB Hourly Continuous")) return false; 
	 // cancel the timer
		hourlyTimer.cancel();
		return true;
	}
}
