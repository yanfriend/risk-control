package spyder;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import common.Bar;
import common.DataEngine;
import common.OrderEngine;
import common.PriceTypeEnum;
import common.Strategy;
import common.TradeAlarm;

public class RangeFadeStrategy implements Strategy
{
	private double todayOpen;
	private double currentPrice; 
	private double lastRange; 

	OrderEngine orderEngine; 
	DataEngine dataEngine; 
	
	Contract contract = new Contract(); 
	Order order = new Order();   // keep the main order info
	
	TreeMap<Long, Bar> barMap = new TreeMap<Long, Bar>(); 
	
	private transient final Logger log = LoggerFactory.getLogger(Strategy.class);

    public RangeFadeStrategy()
    {
    	contract.m_secType = "STK";
    	contract.m_exchange = "SMART";
        contract.m_currency = "USD"; 
    }
    
    @SuppressWarnings("deprecation")
	public void checkStrategy()
    {
        log.info("{} checking range fade strategy ..", contract.m_symbol);
        Collection<Bar> barCollection = barMap.values();
        log.info("bar size:{}", barCollection.size());
        
        if (barCollection.size()!=2) {
        	log.error("historical data size problem");
        	return; 
        }
        Iterator<Bar> iterator = barCollection.iterator();       
        Bar bar = null;       
       	bar = (Bar) iterator.next();          
        lastRange = bar.getHigh() - bar.getLow();
        bar = (Bar) iterator.next();
        todayOpen = bar.getOpen(); 
        
        log.info("last range:"+lastRange+", today open"+todayOpen);
    }
    
    
    private void checkSignal() {
    	if (lastRange <=0 )  {
    		log.error("last range not set, serious problem");
    		return;     		
    	}
    	if ( (currentPrice>=(todayOpen+lastRange/2)) || (currentPrice<=(todayOpen-lastRange/2)) ) {
            log.warn("get up, range fade trade pls!!");
    		TradeAlarm.playSound("D:\\WINDOWS\\Media\\Windows XP Critical Stop.wav"); 
    	} else 
    		log.info("last range:"+lastRange+", today open"+todayOpen);
    	        
	}
    
    
	public void monitor() {
    	// every 5 minutes, request 3 day, 30 minutes historical data, store them in a data structure?
        int period = 2*60*1000;  // repeat every 5 min.
        Timer timer = new Timer();
    	
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		
        		String endDateTime;  //yyyymmdd HH:mm:ss ttt, where "ttt" is the optional time zone.
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        		long serverTime = dataEngine.getCurrentServerTime();
        		serverTime *= 1000; 
        		
            	Date now = new Date(dataEngine.getCurrentServerTime()*1000);
            	Date nine30 = new Date(now.getYear(), now.getMonth(), now.getDate(), 9,30,0);
            	long nine30Mili = nine30.getTime();
        		
        		if (serverTime <= nine30Mili) return;  // do not request if time is earlier than 9:30 am. 
        		
         	    endDateTime = sdf.format(new Date(serverTime));
        	    log.info("requesting data till {}", endDateTime);
 
        	    if (lastRange>0) checkSignal();  // lastRange has been set. 
        	    else 
        	    	dataEngine.reqHistoricalData(RangeFadeStrategy.this, endDateTime, "2 D", "1 day", "TRADES", 1, 2);   
 
        	    if (currentPrice<=0) {
        	    	dataEngine.reqRealtimeBar(RangeFadeStrategy.this, PriceTypeEnum.TRADES.toString(), true);
        	    }
        	}
        }, 0, period);

    }
    
    public void checkHistoricalData(String date, double open, double high, double low, double close, int volume)
    {  // for ib, date time means the beginning of the bar, e.g. 9:30 means 9:30 to 9:45.. 
    	String dateStr=""; 
    	long datelong=0; 
    	try {
    		datelong = Long.parseLong(date);  // datelong is in second
    		Date datetmp = new Date(datelong * 1000);  // mili-second
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    		dateStr = sdf.format(datetmp);
    	}
    	catch (Exception e) {
    		// when the last one is not a valid date
    		checkStrategy();
    		return; // skip the last one
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

    public boolean parse(String str) {
//    	if ("S,".equals(str.substring(0, 2).toUpperCase())) {
//    		return false; 
//    	}
//    	String[] strArr = str.split(",");
    	String symbol = "SPY"; // strArr[0].trim();
//    	double yesterdayClose = 0; 
//    	long DUVolume = 0; 
//    	int orderAmount = 0; 
//    	try {
//    		yesterdayClose = Double.parseDouble(strArr[1].trim());
//    		DUVolume = Long.parseLong(strArr[2].trim());
//    		orderAmount = Integer.parseInt(strArr[3].trim());
//    	} catch (Exception e){
//    		log.error("error in stock file line: {}", str);
//    		return false; 
//    	}
		this.setSymbol(symbol);
//		this.setOrderAmount(orderAmount);
//		this.setYesterdayClose(yesterdayClose);
//		this.setDUVolume(DUVolume);
		return true; 
	}
	
	public void setSymbol(String symbol) {
		this.contract.m_symbol = symbol; 
		
	}
	public void setOrderEngine(OrderEngine orderEngine) {
		this.orderEngine = orderEngine;
	}
	public OrderEngine getOrderEngine() {
		return orderEngine;
	}
	public DataEngine getDataEngine() {
		return dataEngine;
	}
	public void setDataEngine(DataEngine dataEngine) {
		this.dataEngine = dataEngine;
	}
	public double getCurrentPrice() {
		return currentPrice;
	}
	public void setTodayOpen(double todayOpen) {
		this.todayOpen = todayOpen;
	}
	public double getTodayOpen() {
		return todayOpen;
	}
	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}
	public Contract getContract() {
		return contract;
	}
	public void setTargetReached(boolean targetReached) {
		// TODO Auto-generated method stub
	}

	@Override
	public void checkExecution(Execution execution) {
		// TODO Auto-generated method stub
		
	}

}