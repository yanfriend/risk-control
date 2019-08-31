package common;
/*
 * simulate dataEngine from test data.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.*;

public class SimuDataEngine extends DataEngine implements EWrapper
{
	private static Logger log = LoggerFactory.getLogger(SimuDataEngine.class);
	
	volatile private long currentServerTime;  // in seconds from 1970/01/01
	
    private Hashtable<Integer, Security> HistSecurityHash = new Hashtable<Integer, Security>(); 
    private Hashtable<Integer, Security> RealSecurityHash = new Hashtable<Integer, Security>(); 
    
    private Hashtable<Integer, Strategy> HistStrategyHash = new Hashtable<Integer, Strategy>(); 
    private Hashtable<Integer, Strategy> RealStrategyHash = new Hashtable<Integer, Strategy>(); 
    
    private static int nextSymbolID = 0;
    private EClientSocket client = null;
	
	private String host = "";

	public SimuDataEngine (int clientId)
	{
		super();
//		// set timer to request current server time. 
        Timer timer = new Timer();
        long period = 60*1000;  // 60 seconds
        int delay = 1000; 
		currentServerTime = new Date(2010-1900,8,21,11,1,31).getTime()/1000;  // sep 21.  for HE
        timer.scheduleAtFixedRate(new TimerTask() { 
        	public void run() {
       			// currentServerTime = new Date(2010-1900,8,24,5,31,0).getTime()/1000;  // sep 24.  for YM future in early morning 
       			return; 
        	}
        }, delay, period);  // request per 60 seconds. 
        // add per seconds
        Timer secTimer = new Timer(); 
        secTimer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		synchronized(this) {
        			currentServerTime = currentServerTime + 1;
        			// log.info("current timer(in timer): {}", currentServerTime);
        		}
        	}
        }, 0, 1000);
	}

    public void currentTime (long time)
    {
    	this.currentServerTime = time; 
		log.trace("current timer(in return): {}", currentServerTime);
    }

    public void execDetails (int orderId, Contract contract, Execution execution)
    {
    	String msg = EWrapperMsgGenerator.execDetails(orderId, contract, execution);
    	log.info(msg);
    }

    public void error (Exception e)
    {
        e.printStackTrace ();
    }

    public void error (String str)
    {
        System.err.println (str);
    }

    public void error (int id, int errorCode, String errorMsg)
    {
        System.err.println ("error (id, errorCode, errorMsg): id=" + id + ".  errorCode=" + errorCode + ".  errorMsg=" + errorMsg);
    }
    
    public void nextValidId (int orderId)
    {
        nextSymbolID = orderId;
    }

    public synchronized int getNextID ()
    {
        return (nextSymbolID++);
    }
    
    public void historicalData (int reqId, String date, double open,
            double high, double low, double close, int volume, int count,
            double WAP, boolean hasGaps)
    {   	
    	// do the process
    	Security security = HistSecurityHash.get(reqId);
        if (security != null) {
        	security.concatenateHistData(date, open, high, low, close, volume); 
        }
        
        Strategy strategy = HistStrategyHash.get(reqId);
        if (strategy != null) {
        	strategy.checkHistoricalData(date, open, high, low, close, volume); 
        }
    }
    
    public void realtimeBar(int reqId, long time, double open, double high,
            double low, double close, long volume, double wap, int count)
    {
        try {
        	Security security = RealSecurityHash.get(reqId);
            if (security != null) {
            	security.setCurrentPrice(close);
            	log.info("{}, {}, last price={}, volume={}", 
            			new Object[]{String.valueOf(reqId),security.getSymbol(),security.getCurrentPrice(),volume});            	
            }
        } catch (Exception e) {
            log.error("realtimebar process exception: {}",e);
        }
        
        try {
        	Strategy strategy = RealStrategyHash.get(reqId);
            if (strategy != null) {
            	strategy.setCurrentPrice(close);
            	log.info("{}, {}, last price={}, volume={}", 
            			new Object[]{reqId, strategy.getContract().m_symbol, close, volume});            	
            }
        } catch (Exception e) {
            log.error("strategy realtimebar process exception: {}",e);
        }
    }
   
    public synchronized void reqHistoricalData(Security security, String endDateTime, String durationStr, String barSizeSetting, 
    		String whatToShow, int useRTH, int formatDate) 
    {
        Contract contract = new Contract ();
        contract.m_currency = "USD"; 
        contract.m_symbol = security.getSymbol();
        contract.m_exchange = security.getExchange();
        contract.m_secType = security.getSecurityType();
        contract.m_expiry = security.getExpireDate();
    	contract.m_strike = security.getStrikingPrice();
    	contract.m_right = security.getPutCall();
    	contract.m_multiplier = security.getMultiplier();        
               
        // before inserting new request id into securityHash, remove the old one; otherwise Hashtable becomes too large.
        Collection<Security> securityColl = HistSecurityHash.values();
        securityColl.remove(security);
        int requestId = getNextID();   
        HistSecurityHash.put(requestId, security);
        log.info("Requesting historical data: " + requestId + ", " + security.getSymbol());
        client.reqHistoricalData(requestId, contract, endDateTime, durationStr, barSizeSetting, whatToShow, useRTH, formatDate);
        // end of real trading. 
        
        // the below is for testing. 
      	// runTestData(security);
    } 
    
    public synchronized void reqHistoricalData(Strategy strategy, String endDateTime, String durationStr, String barSizeSetting, 
    		String whatToShow, int useRTH, int formatDate) 
    {               
        // before inserting new request id into securityHash, remove the old one; otherwise Hashtable becomes too large.
        Collection<Strategy> strategyColl = HistStrategyHash.values();
        strategyColl.remove(strategy);
        int requestId = getNextID();   
        HistStrategyHash.put(requestId, strategy);
        log.info("Requesting historical data:{}, {}", requestId, strategy.getContract().m_symbol);
        // client.reqHistoricalData(requestId, strategy.getContract(), endDateTime, durationStr, barSizeSetting, whatToShow, useRTH, formatDate);

        runTestData(strategy);
    } 
    
    public synchronized void reqRealtimeBar(Strategy strategy, String priceType, boolean useRTH) 
    {
        int requestId = getNextID();   
        RealStrategyHash.put(requestId, strategy);
       	realtimeBar(requestId, new Date().getTime()/1000, 500, 500,
                    500, 123.45, 12345, 500, 5);
//        client.reqRealTimeBars(requestId, strategy.getContract(), 5, priceType, useRTH);  // 5 sec, mandatory, /BID ASK MIDPOINT 
//        log.info("Requesting realtime id:{}, symbol:{}", requestId, strategy.getContract().m_symbol); 
    } 
    
    public synchronized void reqRealtimeBar(Security security, String priceType, boolean useRTH) 
    {
        Contract contract = new Contract ();
        contract.m_currency = "USD"; 
        contract.m_symbol = security.getSymbol();
        contract.m_exchange = security.getExchange();
        contract.m_secType = security.getSecurityType();
        contract.m_expiry = security.getExpireDate();
    	contract.m_strike = security.getStrikingPrice();
    	contract.m_right = security.getPutCall();
    	contract.m_multiplier = security.getMultiplier();        
    	      
        int requestId = getNextID();   
        RealSecurityHash.put(requestId, security);
       	realtimeBar(requestId, new Date().getTime()/1000, 500, 500,
                    500, 500, 12345, 500, 5);
        // return; 
//        client.reqRealTimeBars(requestId, contract, 5, priceType, useRTH);  // 5 sec, mandatory, /BID ASK MIDPOINT 
        log.info("Requesting realtime bar: " + requestId + ", " + security.getSymbol()); 
    } 
    
    private void runTestData(Strategy strategy) { // read data from local file as data source
        List<Bar> barList = new ArrayList<Bar>();
    	try {
    		// log.info("sending out testing hist data:{}",getContract().m_symbol +"_"+ getContract().m_secType +".csv");
		    BufferedReader in = new BufferedReader(
		    	new FileReader("D:\\CommonData\\Stocks\\testData\\"+ strategy.getContract().m_symbol +"_"+ strategy.getContract().m_secType +".csv"));
		    String str;
		    long targetTime; 
		    targetTime = new Date(2010-1900,8,21,11,01,31).getTime()/1000;  // sep 21.
		    targetTime = new Date(2010-1900,8,24,5,31,0).getTime()/1000;  // sep 21.
		    while ((str = in.readLine()) != null) {
		    	if (str.charAt(0)=='#') {
		    		continue; 
		    	}
		        Bar bar = process(str);
		        if (bar == null) continue;
		        if (bar.getDate()>targetTime) {
		        	break;
		        }
		        barList.add(bar);
		    }
		    in.close();
    	} catch (IOException e) {
    		log.error("Exception in reading testing stock data");
    		return; 
    	}

	    for (int i=0; i<barList.size(); i++) {
	    	Bar bar = barList.get(i);
		    long dateSec = bar.getDate(); 
		    String dateStr = String.valueOf(dateSec);
		    strategy.checkHistoricalData(dateStr, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume() );
	    }    	
	    strategy.checkHistoricalData("Finished", 0, 0, 0, 0, 0 );
    }
    
    private Bar process(String str)
    {
    	String[] strArr = str.split(",");
    	double open, high, low, close; 
    	int volume = Integer.MAX_VALUE;
    	long date;
    	
    	//String symbol = strArr[0].trim();
    	try {
    		date = Long.parseLong(strArr[0].trim());
    		open = Double.parseDouble(strArr[1].trim());
    		high = Double.parseDouble(strArr[2].trim());
    		low = Double.parseDouble(strArr[3].trim());
    		close = Double.parseDouble(strArr[4].trim());
    		volume = Integer.parseInt(strArr[5].trim());
    	} catch (Exception e){
    		log.error("error in stock file line: {}", str);
    		return null; 
    	}
    	if (date>this.currentServerTime) {
    		return null;    // not return if testing && time is passed. 
    	}
    	Bar bar = new Bar();
    	bar.setDate(date);
    	bar.setOpen(open);
    	bar.setClose(close);
    	bar.setHigh(high);
    	bar.setLow(low);
    	bar.setVolume(volume);
    	return bar;
    }

	public void saveTestData(Strategy strategy, TreeMap<Long, Bar> barMap) {
		Collection<Bar> tmpBarColl = barMap.values(); 
    	try {
		    BufferedWriter out = new BufferedWriter(new 
		    	FileWriter("D:\\CommonData\\Stocks\\testData\\"+ strategy.getContract().m_symbol +"_"+ strategy.getContract().m_secType +".csv"));
		    
		    Iterator<Bar> barIter = tmpBarColl.iterator();
		    while (barIter.hasNext()) {
		    	String str="";
		    	Bar bar = barIter.next();
	        	long barSec = bar.getDate();
	        	Date datetmp = new Date(barSec * 1000);  // mili-second
	        	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
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
	
	public void setCurrentServerTime(long currentServerTime) {
		this.currentServerTime = currentServerTime;
	}

	public long getCurrentServerTime() {
		return currentServerTime;
	}
     
}
