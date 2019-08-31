package rickeyCheung;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RCAdapter {
	class SmallBar {
		Date date;  // use Date for debug purpose
		double esClose, nqClose;
		public SmallBar() {
			date = new Date();
		}
	}
	private transient final Logger log = LoggerFactory.getLogger(RCAdapter.class);
	
	RCTrade rm = new RCTrade(); 
	static List testUnits = new ArrayList(); 
	
	private static void preTesting() {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		String ESdate = "D:\\CommonData\\Stocks\\testData\\ES_201103_FUT.csv"; 
		String NQDate = "D:\\CommonData\\Stocks\\testData\\NQ_201103_FUT.csv";		
		cal1.set(2010, 11, 30,0,0,0); //year is as expected, month is zero based, date is as expected
		cal2.set(2011, 2, 16,0,0,0); 
		Date startDate = cal1.getTime();
		Date endDate = cal2.getTime();
		TestUnit t1 = new TestUnit(ESdate, NQDate, startDate, endDate);
		testUnits.add(t1);
		
	}
	
	void process(String ESdata, String NQdata, Date startDate, Date endDate) {
		// get yesterday close of es and nq and set in rm.
		// if today is not short day (have 16:00 data), call rm.trading with each 5 min data.
		double ESyClose=0, NQyClose=0;
		double ESLast=0, NQLast=0; 
 
		// open two files. 
	    BufferedReader esIn = null, nqIn = null;
		try {
			esIn = new BufferedReader(new FileReader(ESdata));
			nqIn = new BufferedReader(new FileReader(NQdata));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return; 
		}
	    
		List fiveMinBars = new ArrayList<SmallBar>(); 
		List dayBars = new LinkedList<List<SmallBar>>();
 	
		readFile(esIn, nqIn, fiveMinBars, startDate, endDate);
		// group all the 5 min bars by each day
		categorize(dayBars, fiveMinBars);
		
		for (int i=0; i<dayBars.size()-1; i++) { 
			List<SmallBar> oneDayBar = (List<SmallBar>) dayBars.get(i);
			ESyClose = oneDayBar.get(oneDayBar.size()-1).esClose;
			NQyClose = oneDayBar.get(oneDayBar.size()-1).nqClose;
			
		    //log.info("day close:"+oneDayBar.get(oneDayBar.size()-1).date.toString()+","+ESyClose+","+NQyClose);
			RCTrade rt = new RCTrade(); 
			rt.preset(ESyClose, NQyClose);   // once per day
				
			oneDayBar = (List<SmallBar>) dayBars.get(i+1);  // next day.		
			if (oneDayBar.size()<81) {
				log.info("skip half trading day:{}", oneDayBar.get(0).date);
				continue;
			}
			for (int j=0; j<oneDayBar.size(); j++) {
				SmallBar fiveMinBar = oneDayBar.get(j);
				//log.info("passing in:"+fiveMinBar.date+","+fiveMinBar.esClose+","+fiveMinBar.nqClose);
				rt.trading(fiveMinBar.date.getTime()/1000, fiveMinBar.esClose, fiveMinBar.nqClose);   // once every 5 minutes
			}
		}
	}
	
	// categorize list of 5 min bar into groups of 5 min bars, each group is for one day
	private void categorize(List dayBars, List fiveMinBars) {
		SmallBar sm, lastSm = new SmallBar(); 
		List oneDayBars = new ArrayList<SmallBar>();
		boolean bfirst = true; 
		
		for (int i=0; i<fiveMinBars.size(); i++) {
			sm = (SmallBar) fiveMinBars.get(i); 
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
			if (!fmt.format(sm.date).equals(fmt.format(lastSm.date))) {
				if (bfirst)  {
					bfirst = false; 
				} else 
					dayBars.add(oneDayBars);
				oneDayBars = new ArrayList<SmallBar>();  // oneDayBars.clear();
				lastSm = sm; 
			}
			oneDayBars.add(sm);			
		}
		dayBars.add(oneDayBars);  // add the last one. 
	}

	private void readFile(BufferedReader esin, BufferedReader nqin, List fiveMinBars, Date startDate, Date endDate) {
		String esStr="", nqStr=""; 
	    try {
			while (esStr != null) {
				esStr = esin.readLine(); nqStr = nqin.readLine();
				
				if (esStr == null) break;   // end of file
				if (esStr.trim().length()<=0) {
					continue;
				}
				if (esStr.trim().charAt(0)=='#') {  // get rid of comment line beginning with #
					continue; 
				}   

    	    	SmallBar sb1 = readLine(esStr, startDate, endDate); 
    	    	SmallBar sb2 = readLine(nqStr, startDate, endDate);
    	    	if (sb1==null) continue; 
    	    	if (sb1.date.compareTo(sb2.date)!=0) {
    	    		log.error("Serious warning, data not synchronized, es: {}", sb1.date );
    	    		System.exit(-1);
    	    	}
    	    	sb1.nqClose = sb2.esClose; 
    	    	fiveMinBars.add(sb1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private SmallBar readLine(String str, Date startDate, Date endDate) {
    	Date date;
    	double open, high, low, close; 
    	try {
	    	String[] strArr = str.split(",");
	    	String dateString = strArr[0].trim();
	    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss");
	    	date = (Date)sdf.parse(dateString);  
    		open = Double.parseDouble(strArr[1].trim());
    		high = Double.parseDouble(strArr[2].trim());    	    		
    		low = Double.parseDouble(strArr[3].trim());
    		close = Double.parseDouble(strArr[4].trim());
    		if (date.before(startDate) ||date.after(endDate) ) return null; 
    		int hour = date.getHours();
    		int minute = date.getMinutes(); 
    		if ( (hour<9)||(hour>16)||((hour==9)&&(minute<30))||((hour==16)&&(minute>10)) ) return null; // get rid of bars before 9:30 and after 16:15 (16:10 for bar start time)
    		
    	} catch (Exception e){
    		log.error("error in backtest data file line: {}", str);
    		return null; 
    	}
    	SmallBar sb = new SmallBar(); 
    	sb.date = date; 
    	sb.esClose = close; 
    	return sb; 
	}

	public static void main(String[] args) {
		preTesting(); 
		
		int index = 0; 
		TestUnit tu = (TestUnit) testUnits.get(index);
		
		RCAdapter ra = new RCAdapter(); 
		ra.process(tu.ESdate, tu.NQDate, tu.startDate, tu.endDate); 
	}


}
