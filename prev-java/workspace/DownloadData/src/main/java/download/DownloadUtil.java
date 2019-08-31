package download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DownloadUtil {
	
	/*
	 * the main function is not used any more, the function is incorporated into download timer function.
	 * but some sub-function in this Util is still used!
	 */
	public synchronized static void iBHourlyToSimuDaily() {
		System.out.println("sync ib hourly to simu daily");
		// for each file in D:\CommonData\Stocks\IBData\Hourly, find the response one in D:\CommonData\Stocks\IBData\Hourly\simuDaily
		// the last date from simu file, locate the one in hourly file, write the remaining one after that date. 
		// compare the date difference, and try to locate the preciously from the tail of the hourly file. 
		// suppose 100 chars per lines. 
		
		String folder = "D:\\CommonData\\Stocks\\IBData\\Hourly";
		File dir = new File(folder);
		for (File sourceFile : dir.listFiles()) {
		  if (sourceFile.getName().endsWith((".csv"))) {
			  RandomAccessFile srf;
			  try {
				srf = new RandomAccessFile(sourceFile, "r");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("error in reading source file:"+sourceFile.getName());
				continue; 
			}
			//String targetFile = folder + "\\simuDaily\\" + "S_" + sourceFile.getName();
			//String targetFile = folder + "\\simuDaily\\" + sourceFile.getName().replace("H_", "S_");  
			String targetFile = folder + "\\simuDaily\\" + sourceFile.getName().replace("H.csv", "S.csv");
			RandomAccessFile trf;
			try {
				trf = new RandomAccessFile(targetFile, "rw");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				File f=new File(targetFile);
        		try {
					f.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue; 
				}
				try {
					trf = new RandomAccessFile(targetFile, "rw");
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.err.println("error in reading target file:"+targetFile);
					continue; 
				}
			}
			iBHourlyToSimuDailyImpl(srf, trf);
			try {
				srf.close();
				trf.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(sourceFile.getName());
		  }  // of if csv. 
		}
	}
	
	private static void iBHourlyToSimuDailyImpl(RandomAccessFile srf, RandomAccessFile trf) {
		Date sLast = getTextLastDate(srf);
		Date tLast = getTextLastDate(trf);
		Date lastSimu = getTextLastSimuDate(trf);   // note: the first bar is neglected, if the simu file originally does not exit. 
		long diff = sLast.getTime() - tLast.getTime();
        long diffHours = diff / (60 * 60 * 1000);    
        
        final int lineLength = 100; 
        long scount, tcount;
	try {
		scount = srf.length();
	    tcount = trf.length();
	    
		if ((scount-lineLength*diffHours<=0)) srf.seek(0);
        else srf.seek(scount-lineLength*diffHours);   

	    String inputLine = srf.readLine();   // this might be a partial line. 

	    while(inputLine!=null){   
	 	  inputLine = srf.readLine();   // change this line to date, 
	 	  if ((inputLine == null)||(inputLine.equals(""))) continue; // finished
		  Date sourceDate = lineToDate(inputLine, "yyyy/MM/dd,HH:mm");
		  if (sourceDate == null) continue;  // paitial line, wrong date.  
			  
		  if (sourceDate.after(tLast)) {  // add it. 
			  // last simu date +1, merge to the string. 
			  
			  SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			  Calendar c = Calendar.getInstance();
			  c.setTime(lastSimu);
			  c.add(Calendar.DATE, 1);  
			  lastSimu = c.getTime();
			  String newsimu = sdf.format(lastSimu);
			  
			  String outLine = inputLine.substring(0, 17) + newsimu + "," + inputLine.substring(17);  // tricky, fixed length needed. 
			  
			  outLine += "\n";
			  trf.seek(tcount);   // trf.seek(tcount-1); 
			  trf.writeBytes(outLine);  // "/r/n" for windows, "/n" is current one
			  
			  tcount+=outLine.length();
		  }
	   }
	    
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	}
	
	public static Date getTextLastSimuDate(RandomAccessFile trf) {
		Date orgDate = new Date(0);

		// the data file exists, check size, get the last record date; 
		// skip the almost file size, get the file data, compare and decide if appended. 
		try {
		   long count = trf.length();  

		   final int lineLenth = 100; 
		   
		   trf.seek(count-lineLenth);  
		   String lastLine = trf.readLine(); 
		   while (trf.getFilePointer()<count-5) {  // not the last line yet
		      lastLine = trf.readLine();
		   }
		   // System.out.println(lastLine);  // change last line to date
		   Date lastSimuDate = lineToSimuDate(lastLine, "yyyy/MM/dd");
		   return lastSimuDate;
		   
		} catch (IOException e) {
			//e.printStackTrace();
			return orgDate;
		}
	}

	public synchronized static Date getTextLastDate(String txtFile) {
		Date orgDate = new Date(0);

		RandomAccessFile rf;
		try {
		   rf = new RandomAccessFile(txtFile, "r");  
		   orgDate = getTextLastDate(rf);
		   rf.close(); 
		} catch (IOException e) {
			// e.printStackTrace();
			return orgDate;
		}
		return orgDate; 
	}

	public static Date getTextLastDate(RandomAccessFile rf) {
		Date orgDate = new Date(0);

		// the data file exists, check size, get the last record date; 
		// skip the almost file size, get the file data, compare and decide if appended. 
		try {
		   long count = rf.length();  

		   final int lineLenth = 100; 
		   
		   rf.seek(count-lineLenth);  
		   String lastLine = rf.readLine(); 
		   while (rf.getFilePointer()<count-5) {  // not the last line yet
		      lastLine = rf.readLine();
		   }
		   // System.out.println(lastLine);  // change last line to date
		   Date lastRecordDate = lineToDate(lastLine, "yyyy/MM/dd,HH:mm");
		   
		   return lastRecordDate;

		} catch (IOException e) {
			// e.printStackTrace();
			return orgDate;
		}
	}
	
	public synchronized static void amiDBToAmiDaily() {
		System.out.println("sync ami db to ami daily");
	}
	
	private synchronized static Date lineToDate(String lastLine, String dateFormat) {
    	String[] strArr = lastLine.split(",");
    	String dateString = strArr[0].trim()+","+strArr[1].trim();
    	SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);   
    	try {
			Date date = (Date)sdf.parse(dateString);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}  
		return null;
	}
	

	private static Date lineToSimuDate(String lastLine, String dateFormat) {
    	String[] strArr = lastLine.split(",");
    	String dateString = strArr[2].trim();  // the third column
    	SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);   
    	try {
			Date date = (Date)sdf.parse(dateString);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}  
		return null;
	}

}
