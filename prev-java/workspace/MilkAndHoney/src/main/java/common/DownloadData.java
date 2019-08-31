package common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
import common.SimuDataEngine;
import common.Strategy;

public class DownloadData implements Strategy
{

// todo: two things: 1, end condition, time exceeds;   2, volume, *100?  	
	
//	OrderEngine orderEngine; 
	DataEngine dataEngine; 
	
	Contract contract = new Contract(); 
	
	TreeMap<Long, Bar> barMap = new TreeMap<Long, Bar>(); 
	
	private transient final Logger log = LoggerFactory.getLogger(Strategy.class);
    // private static final int CONSOLIDATE_30MINUTE_BAR = 30;

    Calendar cal = Calendar.getInstance();
    Calendar startTime; 
    Calendar endTime;    
    
    String RTH = "";
    String whatToShow = "TRADES"; 
    
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
    
    @SuppressWarnings("deprecation")
	public void checkStrategy()
    {        
    }
    
	@SuppressWarnings("static-access")
	public void monitor() {
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
		}//sleep for 1000 ms

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
        		if (cal.getTimeInMillis()<endTime.getInstance().getTimeInMillis()) {
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
    
    public void checkHistoricalData(String date, double open, double high, double low, double close, int volume)
    {  // for ib, date time means the beginning of the bar, e.g. 9:30 means 9:30 to 9:45.. 
    	String dateStr=""; 
    	long datelong=0; 
    	try {
    		datelong = Long.parseLong(date);  // datelong is in second
    		Date datetmp = new Date(datelong * 1000);  // mili-second
    		//SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");   
    		dateStr = sdf.format(datetmp);
    	}
    	catch (Exception e) {
    		// when the last one is not a valid date
   			saveTestData(this, barMap); 
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

	private void saveTestData(Strategy strategy, TreeMap<Long, Bar> barMap) {
		Collection<Bar> tmpBarColl = barMap.values(); 
    	try {
		    BufferedWriter out = new BufferedWriter(new 
		    	FileWriter("D:\\CommonData\\Stocks\\testData\\"+ strategy.getContract().m_symbol +"_"+strategy.getContract().m_expiry+"_"+ strategy.getContract().m_secType +".csv"));
		    
		    Iterator<Bar> barIter = tmpBarColl.iterator();
		    while (barIter.hasNext()) {
		    	String str="";
		    	Bar bar = barIter.next();
	        	long barSec = bar.getDate();
	        	Date datetmp = new Date(barSec * 1000);  // mili-second
	        	// SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss");  // for js parse.
	        	SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy,HH:mm:ss");  // for amibroker import directly
	        	String dateStr = sdf.format(datetmp);
		    	str=String.format("%s,%f,%f,%f,%f,%d\n", dateStr, bar.getOpen(),bar.getHigh(),bar.getLow(),bar.getClose(),bar.getVolume());
		    	out.write(str);
		    }
		    out.close();
		    log.info("saved to test data file.");
    	} catch (IOException e2) {
    		log.error("Exception in saving testing stock data:{}",e2);
    		return; 
    	}
    }
    
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