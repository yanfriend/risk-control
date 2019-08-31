package download;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import common.StoreUnit;
import common.UnitUtil;

public class Download {
	
	public Download () {
	}

	public void menu() {
		int iChoice = -1; 
		while (iChoice!=0) {
	        System.out.println("This is the download sub menu");
	        System.out.println("1. download");
	        System.out.println("0 to exit");
	        
	        Scanner reader = new Scanner(System.in);
	        iChoice = reader.nextInt();
	        
	        switch(iChoice) {
	        case 0: return; 
	        case 1: 
	        	System.out.println("input the download symbol");
	        	String sym = reader.next(); 
	        	downone(UnitUtil.getStoreUnit(sym)); 
	        	break;
	        }
	        
		}		
	}  // of menu function
	

	
	private void fullDownload(StoreUnit su) {
		try {
			URL aUrl;
			aUrl = new URL(su.url);
			ReadableByteChannel rbc;
			rbc = Channels.newChannel(aUrl.openStream());
			FileOutputStream fos;
			fos = new FileOutputStream(su.path);
			fos.getChannel().transferFrom(rbc, 0, 10240000);  // 10 M
			fos.close(); 
			rbc.close(); 		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		   
    }

	public void downone(StoreUnit su) {
		File f = new File(su.path);
		if(!f.exists()) {
			fullDownload(su);
			toAmibroker(su, -1);
			return;
		}
		// the data file exists, check size, get the last record date; 
		// skip the almost file size, get the file data, compare and decide if appended. 
		try {
		   RandomAccessFile rf = new RandomAccessFile(su.path, "rw");  
		   long count = rf.length();  

		   final int lineLenth = 100; 
		   
		   rf.seek(count-lineLenth);  
		   String lastLine = rf.readLine(); 
		   while (rf.getFilePointer()<count-5) {  // not the last line yet
		      lastLine = rf.readLine();
		   }
		   System.out.println(lastLine);  // change last line to date
		   Date lastRecordDate = lineToDate(lastLine);
		   
		   URL aUrl;
		   aUrl = new URL(su.url);
		   
		   BufferedReader in = new BufferedReader(new InputStreamReader(aUrl.openStream()));   
		   in.skip(count-lineLenth);  // skip local file size - line length. 

		   String inputLine = in.readLine();   // this might be a partial line. 
		   
		   int backday = 0; 
		   while(inputLine!=null){   
			  inputLine = in.readLine();   // change this line to date, 
			  if ((inputLine == null)||(inputLine.equals(""))) continue; // finished
			  
			  Date webDate = lineToDate(inputLine);
			  if (webDate.after(lastRecordDate)) {  // add it. 
				  rf.seek(count-1); 
				  inputLine += "\n";
				  count+=inputLine.length();
				  rf.writeBytes(inputLine);  // "/r/n" for windows, "/n" is current one
				  backday++;
			  }
		   }   
		   toAmibroker(su, backday);
		   in.close();   
		   rf.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Date lineToDate(String lastLine) {
    	String[] strArr = lastLine.split(",");
    	String dateString = strArr[0].trim();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
    	try {
			Date date = (Date)sdf.parse(dateString);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}  
		return null;
	}
	
//	private void batchToAmi(StoreUnit su) {
//		String cmdFile = "D:\\CommonData\\Stocks\\cycleData\\import.bat";   
//		
//		String cmd = String.format("D:\\CommonData\\Stocks\\cycleData\\batchToAmiJava.js %s %s 6 y", su.symbol, su.path);   // cannot run .js directly due to use activex object.
//		
//		File file = new File(cmdFile);
//		Writer writer = null;
//		try {		
//			writer = new BufferedWriter(new FileWriter(file));
//			writer.write(cmd);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		} finally {
//			try {
//				writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		Runtime run = Runtime.getRuntime();
//        try {
//			Process p = run.exec(cmdFile);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	private void toAmibroker(StoreUnit su, int backday) {
		String cmdFile = "D:\\CommonData\\Stocks\\cycleData\\import.bat";   
		
		// cannot run .js directly due to use activex object.
		String cmd = "";
		if (backday < 0) cmd = String.format("D:\\CommonData\\Stocks\\cycleData\\batchToAmiJava.js %s %s 6 y", su.symbol, su.path); 
		else cmd = String.format("D:\\CommonData\\Stocks\\cycleData\\batchToAmiJava.js %s %s %d d", su.symbol, su.path, Math.round(backday*1.4+0.5)+7);   // add 7 to avoid download data delay
		
		File file = new File(cmdFile);
		Writer writer = null;
		try {		
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(cmd);
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Runtime run = Runtime.getRuntime();
        try {
			Process p = run.exec(cmdFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
