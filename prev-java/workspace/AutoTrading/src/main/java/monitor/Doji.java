package monitor;

import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import common.Bar;
import common.DataEngine;
import common.Email;
import common.OrderEngine;
import common.PriceTypeEnum;
import common.SimuDataEngine;
import common.Strategy;
import common.TradeAlarm;

public class Doji implements Strategy
{
	// almost all of these can be in the upper class
	private double currentPrice;  

	OrderEngine orderEngine; 
	DataEngine dataEngine; 
	
	private boolean testing;  // testing true means save data mode
	private transient final Logger log = LoggerFactory.getLogger(Strategy.class);

	////////////////////////////////////////////////
	Contract contract = new Contract(); 
	
	Order order = new Order();   // keep the main order info
	TreeMap<Long, Bar> barMap = new TreeMap<Long, Bar>(); 
	
    Core taLib = new Core();   // may change to spring DI. 
    // end of in upper class. 
    
    final int RTH_ONLY = 1; 
    final int ALL_DATA = 0; 
    final int DATE_IN_SECOND = 2;
    
    final boolean REAL_TIME_BAR_useRTH = true; 
    final boolean REAL_TIME_BAR_ALLDATA = !REAL_TIME_BAR_useRTH;
    
    boolean all_time_trading = false; 

    public Doji()
    {

    }
    
    @SuppressWarnings("deprecation")
	public int checkStrategy()
    {
        Collection<Bar> barCollection = barMap.values();
        log.info("bar size:{}", barCollection.size());
        
        Iterator<Bar> iterator = barCollection.iterator();       
        Bar bar = null;    
        
        // prefer have all the value printed
        while (iterator.hasNext()) {          
        	bar = (Bar) iterator.next();          
        	long barSec = bar.getDate();
        	//if (testing) {  // log
        		Date datetmp = new Date(barSec * 1000);  // mili-second
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        		String dateStr = sdf.format(datetmp);
        		log.info("Bar data: {}, {}, close:{}, volume:{}", new Object[]{ contract.m_symbol, dateStr, bar.getClose(), bar.getVolume() } );
        	//}
        }  // finish checking, and calculate the specific period volume. 
        
        // Check if last bar is doji, if not return. 
        Entry<Long, Bar> last_entry = barMap.lastEntry();
        Bar last_bar = last_entry.getValue();
        double total_length = last_bar.getHigh() - last_bar.getLow(); 
        double body_length = Math.abs(last_bar.getClose() - last_bar.getOpen());
        if (body_length/total_length > 0.25) return 1;  // not a doji
        log.info( contract.m_symbol + " " + getReadableTime(null) +" doji found");
        // proceed only if body smaller than 1/4 of total length. 
        
        int barSize = barCollection.size(); 
        if (barSize < 30) {
        	log.error("{} has less than 30 bars, return. {}", contract.m_symbol, barSize);
        	return 2;  // too few data
        }
        
        double[] inHigh = new double[barSize];
        double[] inLow = new double[barSize];
        double[] inClose = new double[barSize];
        
        iterator = barCollection.iterator();
        int i = 0;
        while (iterator.hasNext()) {   
        	bar = (Bar) iterator.next();      

        	inHigh[i] = bar.getHigh();
        	inLow[i] = bar.getLow();
        	inClose[i] = bar.getClose();
        	i++;
        } 
        
        MInteger outBeginIndex = new MInteger(); 
        MInteger outLength = new MInteger(); 
        double[] outReal = new double[barSize];
        int cal_period = 33;
        
        taLib.atr(0, inClose.length-1, inHigh, inLow, inClose, cal_period, outBeginIndex, outLength, outReal); // 33 period atr
        
        int last_indicator_idx = barSize-1-cal_period;
        log.info("{} ATR last value: {}", contract.m_symbol, outReal[last_indicator_idx]); // the last atr value.   
        log.info("{} last bar range: {}", contract.m_symbol, total_length);

        // if current bar total length is smaller than 1.1 of 33 bar ATR, return. 
        if (total_length < outReal[last_indicator_idx]*1.1) return 3; // body is too small 
        
        // meet all condition, sending alert email
       	Email.sendEmail("", "Trading alert: Doji", contract.m_symbol + " is triggerng alert!!!");
       	return 0; // succeed
    }
     
	private String getReadableTime(Date date) {  // to replace several places
		if (date==null) date = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	    String reqTime = sdf.format(date.getTime());    
		return reqTime;
	}

	public void monitor() {
    	// every 5 minutes, request 3 day, 30 minutes historical data, store them in a data structure?
    	Random generator = new Random();
    	
        int delay = generator.nextInt(30);   // delay randomly for 5 sec. unit milisecond
        
        Calendar date = Calendar.getInstance();
        date.setTime(new Date());
        //date.set(Calendar.HOUR, 0);
        date.set(Calendar.MINUTE, 57);
        date.set(Calendar.SECOND, 29 + delay);  // maybe add 10  to 30 seconds delay, from 57:30
        date.set(Calendar.MILLISECOND, 0);
        
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	    String reqTime = sdf.format(date.getTime());
	    log.info("next requesting time: {}", reqTime);
        
        int period = 60*60*1000;  // repeat every hour.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		String endDateTime;  //yyyymmdd HH:mm:ss ttt, where "ttt" is the optional time zone
         	    endDateTime = sdf.format(new Date(dataEngine.getCurrentServerTime()*1000));
        	    log.info("requesting data till {}", endDateTime);
        	    
        	    dataEngine.reqHistoricalData(Doji.this, endDateTime, "1 W", "1 hour", "TRADES", get_trading_time(), DATE_IN_SECOND);   
        	    // hour must corresponds to week. 
        	    // https://www.interactivebrokers.com/en/software/api/apiguide/java/reqhistoricaldata.htm
        	}
        }, date.getTime(), period);
        
        show_current_price(); 
    }
	
	private int get_trading_time() {
		return (this.all_time_trading)? ALL_DATA : RTH_ONLY; 
	}
    
	// this can be in parent class too, show current price every minutes
	public void show_current_price() {
		if (currentPrice<=0) {
			dataEngine.reqRealtimeBar( Doji.this, PriceTypeEnum.TRADES.toString(), REAL_TIME_BAR_ALLDATA); // this will set current price. 
		}
		
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		String now;  //yyyymmdd HH:mm:ss ttt, where "ttt" is the optional time zone
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
         	    now = sdf.format(new Date(dataEngine.getCurrentServerTime()*1000));
        	    log.info(contract.m_symbol + " {} price: {}", now, currentPrice);
        	}
        }, 0, 60*1000);		
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
    		if (testing) { 
    			// dateEngine is not in testing mode, the one is 
    			// save data to the file to later use. 
    			((SimuDataEngine)dataEngine).saveTestData(this, barMap); 
    			return;  // only save data.
    		} 
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

    public boolean work(String str) {
    	log.debug("jim bai, in parse:" + str);
    	
    	JSONParser jsonParser = new JSONParser();
    	try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(str);
			JSONObject structure = (JSONObject) jsonObject.get("contract");

	    	contract.m_secType = ((String)structure.get("type"));
	    	contract.m_exchange = ((String)structure.get("exchange"));
	        contract.m_currency = ((String)structure.get("currency"));
	        contract.m_symbol = ((String)structure.get("symbol"));
	        contract.m_expiry = ((String)structure.get("expiry"));
		
	        try {
	        	this.all_time_trading = ((Boolean)jsonObject.get("all_time_trading"));
	        } catch (Exception e1) { 
	        }
	        
	        log.info("contract info:" + contract.toString());
	        
	        monitor();
			
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
		return true; 
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
	public boolean isTesting() {
		return testing;
	}
	public void setTesting(boolean testing) {
		this.testing = testing;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}
	public void setCurrentPrice(double open, double high, double low, double close) {
		this.currentPrice = close;
	}
	
	public Contract getContract() {
		return contract;
	}
	
	public void checkExecution(Execution execution) {
//		if (execution.m_orderId == mainOrderId) { 
//			executedShares = execution.m_cumQty; 
//		}
	}


}