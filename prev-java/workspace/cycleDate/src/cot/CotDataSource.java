package cot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import cot.CotGraph.DemoPanel;
 
@SuppressWarnings("serial")
public class CotDataSource {

	  static int count = 1;
	  public static int window = 300;
	  public static final int FIRST = 0;
	  
	  String title; 

	  TimeSeriesCollection tsc = new TimeSeriesCollection();
			  
	  TimeSeries OIts = new TimeSeries("OI", Day.class);

	  // below 2 are for commodity usage
	  TimeSeries prodts = new TimeSeries("Prod", Day.class);
	  TimeSeries swapts = new TimeSeries("Swap", Day.class);
	  TimeSeries mmoney = new TimeSeries("MMoney", Day.class);
	  
	  TimeSeries commercials = new TimeSeries("Commercial", Day.class);  // common one for both
	  TimeSeries commPer = new TimeSeries("Comm/OI", Day.class);  // to show commercial/OI
	  
	  // below 3 are for financial usage
	  TimeSeries dealerts = new TimeSeries("Dealer", Day.class);
	  TimeSeries assetts = new TimeSeries("AssetMgr", Day.class);
	  TimeSeries moneyts = new TimeSeries("LevMoney", Day.class);
	  
	  
public CotDataSource(String title, String commodity) {
    String[] inFiles = new String[2]; 
    inFiles = CotUtil.getFiles(commodity);
    fillData(inFiles, commodity);
	}


protected void fillData(String[] inFile, String commodity) {
	BufferedReader in;
	try {
		for (int i=0; i<2; i++) {
	    final ZipFile zipFile = new ZipFile(inFile[i]);
	    final Enumeration<? extends ZipEntry> entries = zipFile.entries();
	    while (entries.hasMoreElements()) {   // generally should has only 1. 
	        final ZipEntry zipEntry = entries.nextElement();
	        if (!zipEntry.isDirectory()) {
	        	InputStream input = zipFile.getInputStream(zipEntry);
	        	in = new BufferedReader(new InputStreamReader(input));  //, "UTF-8"));
	        	if (CotUtil.isFinancial(commodity)) 
	        		fillFinancialDataImpl(in, commodity,i); 			
	        	else
	        		fillCommodityDataImpl(in, commodity,i); 			
	    		in.close();
	        }
	    }
	    zipFile.close();
		}  // of for, just do twice
	} catch (FileNotFoundException e1) {
		e1.printStackTrace();
		System.out.println("input file error, return");
		return; 
	} catch (IOException e) {
		e.printStackTrace();
		System.out.println("input file error, return");
		return; 
	}
}

protected void fillFinancialDataImpl(BufferedReader in, String commodity, int fileOrder) {
	// if i is 0, it means file 0, file 1 will come. 
	 
	// if (OIts.getItemCount()>1) return;  // OIts.clear();   // clear timeseries. 
	  
	// read line one by one, has to define token string to find the lines. 
	String token = CotUtil.getToken(commodity);
	if ((token == null)||(token.equals(""))) { System.out.println("no line token specified, exit"); return; }
	
	String str;
	try {
		str = in.readLine();  // skip the first line. 
	    while ((str = in.readLine()) != null) {
	    	if (str.trim().length()<=0) {
	    		continue;
	    	}
	    	if (!str.contains(token)) { continue; }   // not the expected token line. 
	    	String[] strArr = str.split(",");  
	    	// get individual date below  // date, index2;
	    	
	    	String dateString = strArr[2].trim();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    	try {
	    		Date date = (Date)sdf.parse(dateString);

	    	Day tsDay = new Day(date);
	    	int oiValue = Integer.parseInt(strArr[7].trim());       // OI, index 7

	    	int dealerLong = Integer.parseInt(strArr[8].trim());       // dealer long, index 8
	    	int dealerShort = Integer.parseInt(strArr[9].trim());       // dealer short, index 9
	    	int assetLong = Integer.parseInt(strArr[11].trim());    // assetMgr long/short, index 11,12
	    	int assetShort = Integer.parseInt(strArr[12].trim()); 
	    	int moneyLong = Integer.parseInt(strArr[14].trim());    // Lev_Money_Positions long/short, index 14,15
	    	int moneyShort = Integer.parseInt(strArr[15].trim()); 
	    	
	    	if ((oiValue<=0)||(dealerLong <0 )||(dealerShort<0)||(assetLong<0)||(assetShort<0)||(moneyLong<0)||(moneyShort<0)) {
	    		String tmp = String.format("OI=%d, dealerLong=%d, dealerShort=%d, assetLong=%d, assetShort=%d, moneyLong=%d, moneyShort=%d. ", 
	    				oiValue, dealerLong, dealerShort, assetLong, assetShort, moneyLong, moneyShort); 
	    		System.out.println(tmp+"line data prob,continue:"+str);
	    		continue; 
	    	}
	    	
	    	OIts.add(tsDay, oiValue);  // the last statement to add all together. 
	    	dealerts.add(tsDay, dealerLong-dealerShort);
	    	assetts.add(tsDay, assetLong-assetShort);
	    	moneyts.add(tsDay, moneyLong-moneyShort );
	    	commercials.add(tsDay, dealerLong-dealerShort+assetLong-assetShort);
	    	commPer.add(tsDay, ((double)(dealerLong-dealerShort+assetLong-assetShort))/oiValue);  
	    	count++; 
	    	window = count-1;
	    	} catch(SeriesException e) {
	    		System.out.println("continued. timer series exception:"+str);
	    		continue; 	    		
	    	} catch (ParseException e) {
	    		System.out.println("continued. parse error:"+str);
	    		continue; 
	    	}  // for parse error.
	    }  // of while
	} catch (Exception e) {
		System.out.println("Exception in reading cot date file");
		e.printStackTrace();
		return; 
	}
//	if (fileOrder==1) {
//		//if (cs==CotUtil.Cot_Show.Value) {
//		    tsc.addSeries(OIts);
//		    tsc.addSeries(dealerts);
//		    tsc.addSeries(assetts);
//		    tsc.addSeries(commercials);
//		    tsc.addSeries(moneyts);
//		//} else 
//			tsc.addSeries(commPer);  // have to comment above to show this one, cos its values are small. 
//	 }
  }

protected void fillCommodityDataImpl(BufferedReader in, String commodity, int fileOrder) {
	 
	// if (OIts.getItemCount()>1) return;  // OIts.clear();   // clear timeseries. 
	  
	// read line one by one, has to define token string to find the lines. 
	String token = CotUtil.getToken(commodity);
	if ((token == null)||(token.equals(""))) { System.out.println("no line token specified, exit"); return; }
	
	String str;
	try {
		str = in.readLine();  // skip the first line. 
	    while ((str = in.readLine()) != null) {
	    	if (str.trim().length()<=0) {
	    		continue;
	    	}
	    	if (!str.contains(token)) continue;    // not the expected token line. 
	    	if (str.startsWith("\"CRUDE OIL,")) str=str.substring(19);  // special process for crude oil, due to it has one more comma.
	    	
	    	String[] strArr = str.split(",");  
	    	// get individual date below  // date, index2;
	    	
	    	String dateString = strArr[2].trim();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    	try {
	    		Date date = (Date)sdf.parse(dateString);

	    	Day tsDay = new Day(date);
	    	int oiValue = Integer.parseInt(strArr[7].trim());       // OI, index 7

	    	int prodLong = Integer.parseInt(strArr[8].trim());       // prod long, index 8
	    	int prodShort = Integer.parseInt(strArr[9].trim());       // prod short, index 9
	    	int swapLong = Integer.parseInt(strArr[10].trim());    // swap long/short, index 10,11
	    	int swapShort = Integer.parseInt(strArr[11].trim()); 
	    	int mmoneyLong = Integer.parseInt(strArr[13].trim());    // managed money long/short, index 13,14
	    	int mmoneyShort = Integer.parseInt(strArr[14].trim()); 
	    	
	    	if ((oiValue<=0)||(prodLong <0 )||(prodShort<0)||(swapLong<0)||(swapShort<0)||(mmoneyLong<0)||(mmoneyShort<0)) {
	    		String tmp = String.format("OI=%d, prodLong=%d, prodShort=%d, swapLong=%d, swapShort=%d, mmoneyLong=%d, mmoneyShort=%d. ", 
	    				oiValue, prodLong, prodShort, swapLong, swapShort, mmoneyLong, mmoneyShort); 
	    		System.out.println(tmp+"line data prob,continue:"+str);
	    		continue; 
	    	}
	    	
	    	OIts.add(tsDay, oiValue);  // the last statement to add all together. 
	    	swapts.add(tsDay, swapLong-swapShort);
	    	prodts.add(tsDay, prodLong-prodShort);
	    	commercials.add(tsDay, swapLong-swapShort+prodLong-prodShort );
	    	commPer.add(tsDay, ((double)(swapLong-swapShort+prodLong-prodShort))/oiValue);  
	    	mmoney.add(tsDay, mmoneyLong-mmoneyShort);  // can be seen as large speculator. 
	    	
	    	count++; // for drawing setting, dont touch the below two. 
	    	window = count-1;
	    	} catch(SeriesException e) {
	    		System.out.println("continued. timer series exception:"+str);
	    		continue; 	    		
	    	} catch (ParseException e) {
	    		System.out.println("continued. parse error:"+str);
	    		continue; 
	    	}  // for parse error.
	    }  // of while
	} catch (Exception e) {
		System.out.println("Exception in reading cot date file");
		e.printStackTrace();
		return; 
	}
//	if (fileOrder==1) {
//		if (cs==CotUtil.Cot_Show.Value) {
//		    tsc.addSeries(OIts);
//		    tsc.addSeries(prodts);
//		    tsc.addSeries(swapts);
//		    tsc.addSeries(commercials);
//		    tsc.addSeries(mmoney);   
//		 } else 
//			tsc.addSeries(commPer);  // have to comment above to show this one, cos its values are small. 
//			//tsc.removeSeries(commPer);
//	}
  }




}