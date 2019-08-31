package download;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadUtil {
	
	public synchronized static Date textLastDate(RandomAccessFile rf) {
//		File f = new File(su.path);
//		if(!f.exists()) {
//			fullDownload(su);
//			toAmibroker(su, -1);
//			return;
//		}
		
		// the data file exists, check size, get the last record date; 
		// skip the almost file size, get the file data, compare and decide if appended. 
		try {
//		   RandomAccessFile rf = new RandomAccessFile(txtFile, "rw");  
		   long count = rf.length();  

		   final int lineLenth = 100; 
		   
		   rf.seek(count-lineLenth);  
		   String lastLine = rf.readLine(); 
		   while (rf.getFilePointer()<count-5) {  // not the last line yet
		      lastLine = rf.readLine();
		   }
		   System.out.println(lastLine);  // change last line to date
		   Date lastRecordDate = lineToDate(lastLine, "yyyy/MM/dd,HH:mm");
		   return lastRecordDate;
		   
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
//		   rf.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
