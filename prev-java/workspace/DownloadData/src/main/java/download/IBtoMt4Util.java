package download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/*
 * this is not in use due to the binary file difference between java and c++ reading and writing. 
 */
public class IBtoMt4Util {
	
	static class HistoryHeader
	{
		int               version;            // 基础版本
		char[]             copyright = new char[64];      // 版权信息
		char[]              symbol = new char[12];         // 证券
		int               period = 60;             // 保证金期限
		int               digits = 6;             // digits after point.
		long            timesign;           // 基础报时的创建
		long            last_sync;          // 同步时间
		int[]               unused = new int[13];         // 将来应用
	
	 
		public static void writeOfflineHeader(RandomAccessFile trf, String symbol) throws IOException {
			int period = 60; 
			int digits = 6; 
			
			   //    version = 400;
			   //string c_copyright = "(C)opyright 2003, MetaQuotes Software Corp.";
			   //int    i_unused[13];
			   
			//   int F = fileOpenEx(offlineFileName(symbol, period), FILE_BIN | FILE_READ | FILE_WRITE);
			trf.seek(0);  //		   FileSeek(F, 0, SEEK_SET);
			trf.writeByte(0x90);trf.writeByte(0x01);trf.writeByte(0);trf.writeByte(0);  // 90010000, version info of first 4 bytes. 			
			// trf.writeLong(version);   //   FileWriteInteger(F, version, LONG_VALUE);
			int index = 0;
			String copyright =  "(C)opyright 2003, MetaQuotes Software Corp.";
			for (index=0; index<copyright.length(); index++) {
				trf.writeByte(copyright.charAt(index));   // FileWriteString(F, c_copyright, 64);
			}
			for (int i=index; i<64; i++) {
				trf.writeByte(0);
			}
			
			index = 0;
			for (index=0; index<Math.min(symbol.length(),12); index++) {
				trf.writeByte(symbol.charAt(index));   // FileWriteString(F, symbol, 12);
			}   
			for (int i=index; i<12; i++) {
				trf.writeByte(0);
			}
			trf.writeByte(period);
			trf.writeByte(digits); 
			// trf.writeInt(period);  //   FileWriteInteger(F, period, LONG_VALUE);  writeInt, the byte order is reversed with c++ one!!
			// trf.writeInt(digits);  //   FileWriteInteger(F, digits, LONG_VALUE);
			trf.writeInt(0);   //	   FileWriteInteger(F, 0, LONG_VALUE);       //timesign
			trf.writeInt(0);   //      FileWriteInteger(F, 0, LONG_VALUE);       //last_sync
			for ( int i = 0; i<13; i++) {
				trf.writeInt(0); 
			}  //	   FileWriteArray(F, i_unused, 0, 13);
			
		}
		
		
	};
	
	// byte aligned. 
	static class RateInfo
	{
		// 以秒计算当前时间 4个字节,基准时间为1970年1月1日0分0秒,占四个字节
		long            ctm;                
		double            open;
		double            low;
		double            high;  
		double            close;
		double            vol;
	};
	
	final static int OFFLINE_HEADER_SIZE = 148; // LONG_VALUE + 64 + 12 + 4 * LONG_VALUE + 13 * LONG_VALUE
	final static int OFFLINE_RECORD_SIZE = 44;  // 5 * DOUBLE_VALUE + LONG_VALUE

	
	
	public synchronized static void iBHourlyToMT4() {
		System.out.println("sync ib hourly to MT4");
		// for each file in D:\CommonData\Stocks\IBData\Hourly, find the response one in D:\CommonData\Stocks\IBData\Hourly\simuDaily
		// the last date from simu file, locate the one in hourly file, write the remaining one after that date. 
		// compare the date difference, and try to locate the preciously from the tail of the hourly file. 
		// suppose 100 chars per lines. 
		
		String sourceFolder = "D:\\CommonData\\Stocks\\IBData\\Hourly";
		String targetFolder = "D:\\Program Files (x86)\\MetaTrader - Pepperstone\\history\\Pepperstone-Demo-US";
		
		File dir = new File(sourceFolder);
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
			String targetFile = targetFolder + "\\" + sourceFile.getName();
			targetFile = targetFile.replace("_H", "60").replace("csv", "hst");
			
			RandomAccessFile trf = null;
			try {
				trf = new RandomAccessFile(targetFile, "rw");
					try {
						if (trf.length()<=0)
							HistoryHeader.writeOfflineHeader(trf, sourceFile.getName().replace(".csv", "").replace("_H", ""));
					} catch (IOException e) {
						continue;
					}
						
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				File f=new File(targetFile);
        		try {
					f.createNewFile();
					HistoryHeader.writeOfflineHeader(trf, sourceFile.getName().replace(".csv", "").replace("_H", ""));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue; 
				}
				try {
					trf = new RandomAccessFile(targetFile, "rw");
					// add header
					// HistoryHeader aheader = new IBtoMt4Util.HistoryHeader();			
					if (trf.length()<=0)
						HistoryHeader.writeOfflineHeader(trf, sourceFile.getName().replace(".csv", "").replace("_H", ""));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.err.println("error in reading target file:"+targetFile);
					continue; 
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue; 
				}
			}
			iBHourlyToMT4Impl(srf, trf);
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
	
	private static void iBHourlyToMT4Impl(RandomAccessFile srf, RandomAccessFile trf) {
		Date sLast = getTextLastDate(srf);
		Date tLast = getMT4LastDate(trf);

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
			  // add to hst file. 
			  
			  RateInfo outRecord = inLinetoRecord(inputLine); 
			  trf.seek(tcount);   // trf.seek(tcount-1); 
			  trf.writeLong(outRecord.ctm);  // "/r/n" for windows, "/n" is current one
			  trf.writeDouble(outRecord.open); 
			  trf.writeDouble(outRecord.high); 
			  trf.writeDouble(outRecord.low); 
			  trf.writeDouble(outRecord.close); 
			  tcount+=OFFLINE_RECORD_SIZE;
		  }
	   }
	    
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	}
	
	private static RateInfo inLinetoRecord(String inputLine) {
		RateInfo outRecord = new RateInfo(); 
		
    	String[] strArr = inputLine.split(",");
    	String dateString = strArr[0].trim()+","+strArr[1].trim();
    	String dateFormat = "yyyy/MM/dd,HH:mm";
    	SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);   
    	Date date; 
    	try {
			date = (Date)sdf.parse(dateString);
			outRecord.ctm = date.getTime();
			outRecord.open = Double.parseDouble(strArr[2]);
			outRecord.high = Double.parseDouble(strArr[3]);
			outRecord.low = Double.parseDouble(strArr[4]);
			outRecord.close = Double.parseDouble(strArr[5]);
			return outRecord; 
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Date getMT4LastDate(RandomAccessFile trf) {
		Date orgDate = new Date(1970,0,1,0,0,0);
		try {
			if (trf.length()<=OFFLINE_HEADER_SIZE) {
				return orgDate;
			}
			// get the last record and return its time. 
			trf.seek(trf.length()-OFFLINE_RECORD_SIZE); 
			long lasttime = trf.readLong();
			return new Date(lasttime);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return orgDate;
	}

	public synchronized static Date getTextLastDate(String txtFile) {
		File f = new File(txtFile);
		Date orgDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH,0);
		cal.set(Calendar.DAY_OF_MONTH,1);
		orgDate =  cal.getTime();

		if(!f.exists()) {
			return orgDate;
		}
		
		try {
		   RandomAccessFile rf = new RandomAccessFile(txtFile, "r");  
		   return getTextLastDate(rf);

		} catch (IOException e) {
			e.printStackTrace();
			return orgDate;
		}
	}

	private static Date getTextLastDate(RandomAccessFile rf) {
		Date orgDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH,0);
		cal.set(Calendar.DAY_OF_MONTH,1);
		orgDate =  cal.getTime();

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
		   
//		   URL aUrl;
//		   aUrl = new URL(su.url);
//		   
//		   BufferedReader in = new BufferedReader(new InputStreamReader(aUrl.openStream()));   
//		   in.skip(count-lineLenth);  // skip local file size - line length. 
//
//		   String inputLine = in.readLine();   // this might be a partial line. 
//		   
//		   int backday = 0; 
//		   while(inputLine!=null){   
//			  inputLine = in.readLine();   // change this line to date, 
//			  if ((inputLine == null)||(inputLine.equals(""))) continue; // finished
//			  
//			  Date webDate = lineToDate(inputLine, "dd-MMM-yyyy");
//			  if (webDate.after(lastRecordDate)) {  // add it. 
//				  rf.seek(count-1); 
//				  inputLine += "\n";
//				  count+=inputLine.length();
//				  rf.writeBytes(inputLine);  // "/r/n" for windows, "/n" is current one
//				  backday++;
//			  }
//		   }   
//		   toAmibroker(su, backday);
//		   in.close();   
		   
		   return lastRecordDate;
		   
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
			return orgDate;
		}
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

}
