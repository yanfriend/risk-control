package common;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.Execution;
import common.Bar;
import common.DataEngine;
import common.Strategy;
import download.DownloadUtil;

public class DownloadData implements Strategy
{

// todo: two things: 1, end condition, time exceeds;   2, volume, *100?  	
	
//	OrderEngine orderEngine; 
	DataEngine dataEngine; 
	
	Contract contract = new Contract(); 
	
	TreeMap<Long, Bar> barMap = new TreeMap<Long, Bar>(); 
	
	private transient final Logger log = LoggerFactory.getLogger(Strategy.class);

    Calendar cal = Calendar.getInstance();
    Calendar startTime; 
    Calendar endTime;    
    
    String RTH = "";
    String whatToShow = "TRADES"; 
    
    int downloadType = 0;

	private Date lastEndingDate=new Date(0);
	private Date lastSimuDate=new Date(0); 
    
    public DownloadData()
    {
    	contract.m_secType = "STK";
    	contract.m_exchange = "SMART";
        contract.m_currency = "USD"; 
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        
        startTime.set(1970, Calendar.JANUARY, 1);  
        endTime.set(2100, Calendar.JANUARY, 1);  
    }
    
    public void checkStrategy()
    {        
    }
    
	@SuppressWarnings("static-access")
	public void monitor() {
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
		}//sleep for 1000 ms
		
		if (downloadType == 0)  // 5 min, 1 year download, old input format.
			defaultDownload();    
		else if (downloadType == 1) { // new input format, hourly, 6 month. 
			// "5 D", "5 mins", whatToShow, (RTH.equals("!RTH"))?0:1, format
			ibDownload(6,25, "1 M", "1 hour", 0); 
		} else if (downloadType == 2) { // new input format, daily, 6 month. 
			ibDownload(6,180, "6 M", "1 day", 0);
		}
    }
    
	// rth(regular trading hour), 1 for use rth only, 0 for not use(returned all data). 
    private void ibDownload(int howmanyMonths, final int stepDays, final String downloadPeroid, final String barSize, final int rth) {
        Date now = new Date(); 
                
        cal = Calendar.getInstance();
        cal.setTime(now);     
       	endTime.setTime(now);
       	startTime.setTime(now);
       	
        startTime.add(Calendar.MONTH, -howmanyMonths);  // start time: 6 month ago->now, download hourly 

        String targetFile = getTargetFileString();
        
		RandomAccessFile rf;
		try {
		   rf = new RandomAccessFile(targetFile, "r");  
		   lastEndingDate = DownloadUtil.getTextLastDate(rf);
		   if (downloadType ==1) lastSimuDate = DownloadUtil.getTextLastSimuDate(rf);  // simuDate used for writing only. for hourly dl only
		   rf.close(); 
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println("not find target file:"+targetFile+", creating..");
		}      
        
        if (startTime.getTime().before(lastEndingDate)) {
        	startTime.setTime(lastEndingDate);
        }
        
        cal.setTime(startTime.getTime());
        cal.add(Calendar.DATE, stepDays);  // add 30 from start day. 
        
        
    	// every 15 seconds, request 5 day, 5 minutes historical data, store them in a data structure?
    	Random generator = new Random();
        int delay = generator.nextInt( 10*1000 );   // delay randomly for 5 sec. unit milisecond
        int period = 15*1000;  // repeat every 10sec.
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		String endTimeStr;  //yyyymmdd HH:mm:ss ttt, where "ttt" is the optional time zone.
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        		if (cal.getTimeInMillis()<endTime.getTimeInMillis()) {
        			endTimeStr = sdf.format(cal.getTime()); 
        		} else {
        			endTimeStr = sdf.format(endTime.getTime());
        			timer.cancel();
        		}
        		log.info("requesting data till {}", endTimeStr);
        	    
        	    dataEngine.reqHistoricalData(DownloadData.this, endTimeStr, downloadPeroid, barSize, whatToShow, rth, 2);
        	    // the last is returned data format, 2 is the standard one.  
        	    cal.add(Calendar.DATE, stepDays);   
        	    
        	}
        }, delay, period);
		
		
	}


	public void checkHistoricalData(String date, double open, double high, double low, double close, int volume)
    {  // for ib, date time means the beginning of the bar, e.g. 9:30 means 9:30 to 9:45.. 
    	String dateStr=""; 
    	long datelong=0; 
    	try {
    		if (downloadType == 2) {  // daily download just return different type! jsut 20121010. 
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    			Calendar caltmp = Calendar.getInstance();
    			caltmp.setTime(sdf.parse(date));  
    			datelong = caltmp.getTimeInMillis()/1000;  // change it to seconds finally 
    		} else {
	    		datelong = Long.parseLong(date);  // datelong is in second
    		}
	    	Date datetmp = new Date(datelong * 1000);  // mili-second
	    	//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");   
	    	dateStr = sdf.format(datetmp);
    	}
    	catch (Exception e) {
    		// when the last one is not a valid date
   			saveTestData(barMap); 
   			return;  // only save data.
    	}
    	log.trace("historical data: {}, {}, close:{}, volume:{}",
    			new Object[]{ contract.m_symbol,dateStr, close, volume});
    	
    	long barKey = datelong;  // (CONSOLIDATE_30MINUTE_BAR * 60);
    	Bar bar = barMap.get(barKey); 
    	if (bar == null) {
    		bar = new Bar(datelong, open, high, low, close, volume); 
    	} else {
    		bar.setDate(datelong);
    		bar.setOpen(open);
    		bar.setHigh(high);
    		bar.setLow(low);
    		bar.setClose(close);
    		bar.setVolume(volume);
    	}
    	barMap.put(barKey, bar); 
    }

	private void saveTestData(TreeMap<Long, Bar> barMap) {
		String targetLoc = "";
		targetLoc = getTargetFileString(); 

		Collection<Bar> tmpBarColl = barMap.values(); 
		int tmpcount = tmpBarColl.size();
		
		// important: do not write the last bar date cos its not complete!!!
		// but it can miss the last bar when it does have historical complete data, the next save would complete it luckily.
		tmpcount--;
		
		// for daily download, if time is Friday after 5pm, save the last bar. 
		if (downloadType == 2) {
			 Calendar calendar = Calendar.getInstance();  // same as NYC time zone. local one.
		     int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		     if (weekday == Calendar.FRIDAY)  { // friday
		    	 int hour = calendar.get(Calendar.HOUR_OF_DAY);
		    	 if (hour>=17) {   // after 5pm, all FX and commodities closed!
		    		 tmpcount++;		    		 
		    	 }
		     }  
		}
		
    	try {
    		// RandomAccessFile rf = new RandomAccessFile(targetLoc, "rw");  
		    //BufferedWriter out = new BufferedWriter(new FileWriter(targetLoc));
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetLoc, true)));  
		    Iterator<Bar> barIter = tmpBarColl.iterator();
		    //while (barIter.hasNext()) {
		    for (int i=0; i<tmpcount; i++) {	
		    	String str="";
		    	Bar bar = barIter.next();
	        	long barSec = bar.getDate();
	        	Date datetmp = new Date(barSec * 1000);  // mili-second
	        	if (!datetmp.after(lastEndingDate)) continue;   // from server, get old data. datetmp is before or equal file date. 
	        	
	        	// SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss");  // for js parse.
	        	SimpleDateFormat sdf = new SimpleDateFormat(getDateFormat());  
	        	String dateStr = sdf.format(datetmp);
		    	str=String.format("%s,%s%f,%f,%f,%f,%d\n", dateStr, getNextSimuDateStr(),  // getNextSimuDateStr() has ',', so that if it's "", the column just does not exist. 
		    			bar.getOpen(),bar.getHigh(),bar.getLow(),bar.getClose(),bar.getVolume());
		    	out.write(str);
		    	lastEndingDate = datetmp; 	    	
		    }
		    out.close();
		    log.info("saved to test data file.");
    	} catch (IOException e2) {
    		log.error("Exception in saving testing stock data:{}",e2);
    		return; 
    	}
    }
    
    private String getDateFormat() {
    	String dataformat = null;
		if (downloadType == 0) {
			dataformat = "MMM d yyyy,HH:mm:ss";  // for amibroker import directly
		} else if (downloadType == 1) {
			dataformat = "yyyy/MM/dd,HH:mm";
		} else if (downloadType == 2) {
			dataformat = "yyyy/MM/dd,HH:mm";
		}
		return dataformat;
	}
    
    private String getNextSimuDateStr() {
    	if (downloadType !=1 ) return ""; 
    	
    	// below is for hourly download. 
    	String newsimu = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Calendar c = Calendar.getInstance();
		c.setTime(lastSimuDate);
		c.add(Calendar.DATE, 1);  
		lastSimuDate = c.getTime();
		newsimu = sdf.format(lastSimuDate);
		return newsimu+","; 
	}

    private String getTargetFileString() {
    	String targetLoc = null;
    	String interval = "";
		if (downloadType == 0) {
			targetLoc = "D:\\CommonData\\Stocks\\testData\\";
			interval = "5Min";
		} else if (downloadType == 1) {
			targetLoc = "D:\\CommonData\\Stocks\\IBData\\Hourly\\";
			interval = "H";
		} else if (downloadType == 2) {
			targetLoc = "D:\\CommonData\\Stocks\\IBData\\Daily\\";
			interval = "D";
		}
		if(targetLoc==null) return null; 
//		return targetLoc + interval + "_" + contract.m_symbol +"_"+contract.m_expiry + ".csv";
		return targetLoc + contract.m_symbol + contract.m_expiry.substring(2) + interval + ".csv";  // expiry, 201209 -> 1209
	}

    
    //-----------------------------------------------------------------------------------------------------------------------------------
	@Deprecated
    public boolean parse(String str) {
    	String[] strArr = str.split(",");
    	String secType = strArr[0].trim();

    	String symbol = "";
    	String exchange = "";
    	String currency = "USD"; 
    	String expireDate = "";
    	
    	if (secType.equals("STK")) {
	    	try {
	    		symbol = strArr[1].trim();
	    		exchange = strArr[2].trim();
	    		currency = strArr[3].trim();
	        	contract.m_exchange = exchange;
	        	if (strArr.length == 7) {  // if the line have rth, starttime, endstime, three more fields. 
	        		RTH = strArr[4].trim();
		    		String beginstr = strArr[5].trim();
		    		String endstr = strArr[6].trim();
	    	    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss");
	    	    	try {
	    	    		startTime.setTime(sdf.parse(beginstr));  
	    	    	} catch (Exception e){
	    	    		log.error("start time error, line: {}", str);
	    	    	}
	    	    	try {
	    	    		endTime.setTime(sdf.parse(endstr)); 
	    	    	} catch (Exception e){
	    	    		log.error("end time error, line: {}", str);
	    	    	}
	        	}
	    	} catch (Exception e){
	    		log.error("error in DownloadData file line: {}", str);
	    		return false; 
	    	}
    	} else if (secType.equals("FUT")) {
    		try {
	    		symbol = strArr[1].trim();
	    		expireDate = strArr[2].trim();
	    		exchange = strArr[3].trim();
	    		currency = strArr[4].trim();
	    		RTH = strArr[5].trim();
	    		contract.m_exchange = exchange;
	    		contract.m_expiry = expireDate;  
	    		contract.m_includeExpired = true;
	    		// regular trading hour. start/end date. 
	    		String beginstr = strArr[6].trim();
	    		String endstr = strArr[7].trim();
	    		
    	    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss");
    	    	try {
    	    		startTime.setTime(sdf.parse(beginstr));  
    	    	} catch (Exception e){
    	    		log.error("start time error, line: {}", str);
    	    	}
    	    	try {
    	    		endTime.setTime(sdf.parse(endstr)); 
    	    	} catch (Exception e){
    	    		log.error("end time error, line: {}", str);
    	    	}
	    	} catch (Exception e){
	    		log.error("error in DownloadData file line: {}", str);
	    		return false; 
	    	}
    	} else if (secType.equals("CASH")) {
    		try {
	    		symbol = strArr[1].trim();
	    		expireDate = strArr[2].trim();
	    		exchange = strArr[3].trim();
	    		currency = strArr[4].trim();
	    		RTH = strArr[5].trim();
	    		whatToShow = strArr[6].trim();
	    		contract.m_exchange = exchange;
	    		contract.m_expiry = expireDate;  
	    		// contract.m_includeExpired = true;
	    		// regular trading hour. start/end date. 
	    		String beginstr = strArr[7].trim();
	    		String endstr = strArr[8].trim();
	    		
    	    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss");
    	    	try {
    	    		startTime.setTime(sdf.parse(beginstr));  
    	    	} catch (Exception e){
    	    		log.error("start time error, line: {}", str);
    	    	}
    	    	try {
    	    		endTime.setTime(sdf.parse(endstr)); 
    	    	} catch (Exception e){
    	    		log.error("end time error, line: {}", str);
    	    	}
	    	} catch (Exception e){
	    		log.error("error in DownloadData file line: {}", str);
	    		return false; 
	    	}
    	}  // end of CASH
		this.setSymbol(symbol);
      	contract.m_secType = secType;
        contract.m_currency = currency; 
		return true; 
	}
    
    @Deprecated
	private void defaultDownload() {  // used for 5 min. 
        Date now = new Date(); // new Date(dataEngine.getCurrentServerTime()*1000);  // delay 1000 milisec may not enough, so now is empty. use local directly
        cal = Calendar.getInstance();
        cal.setTime(now);    // = Calendar.getInstance();
        if (endTime.get(Calendar.YEAR)==2100) {  // end time is not set, set it to present. 
        	endTime.setTime(now);
        }
        if ( (cal.get(Calendar.YEAR)-startTime.get(Calendar.YEAR))>1 ) {  // start time is not set, set it to 1 year ago. also starttime cannnot be >1y earlier
        	startTime.setTime(now);
        	startTime.add(Calendar.YEAR, -1); 
        }
        cal.setTime(startTime.getTime());
        cal.add(Calendar.DATE, 5);  // add 5d from start day. 
        
    	// every 15 seconds, request 5 day, 5 minutes historical data, store them in a data structure?
    	Random generator = new Random();
        int delay = generator.nextInt( 5*1000 );   // delay randomly for 5 sec. unit milisecond
        int period = 15*1000;  // repeat every 10sec.
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
//        		if (currentPrice<=0) {
//        			dataEngine.reqRealtimeBar(DownloadData.this, PriceTypeEnum.TRADES.toString(), true);
//        		}
        		String endDateTime;  //yyyymmdd HH:mm:ss ttt, where "ttt" is the optional time zone.
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        		if (cal.getTimeInMillis()<endTime.getTimeInMillis()) {
        			// endDateTime = sdf.format(new Date(dataEngine.getCurrentServerTime()*1000));
        			endDateTime = sdf.format(cal.getTime()); 
        		} else {
        			endDateTime = sdf.format(endTime.getTime());
        			timer.cancel();
        		}
        			log.info("requesting data till {}", endDateTime);
        	    
        	    	dataEngine.reqHistoricalData(DownloadData.this, endDateTime, "5 D", "5 mins", whatToShow, (RTH.equals("!RTH"))?0:1, 2);
        	    	cal.add(Calendar.DATE, 7);  // request 5 trading day data, add 7 calendar day each time. 
        	       // rth, 1 for use rth only, 0 for not use. 
        	}
        }, delay, period);
		
	}
	
	public void setSymbol(String symbol) {
		this.contract.m_symbol = symbol; 
		
	}

	public DataEngine getDataEngine() {
		return dataEngine;
	}
	public void setDataEngine(DataEngine dataEngine) {
		this.dataEngine = dataEngine;
	}

	public Contract getContract() {
		return contract;
	}
	
	public void checkExecution(Execution execution) {
	}

	public void setCurrentPrice(double currentPrice, double high, double low, double close) {
		// TODO Auto-generated method stub
		
	}
	
}