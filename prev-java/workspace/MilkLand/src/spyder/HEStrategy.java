package spyder;

import java.awt.Toolkit;
import java.text.SimpleDateFormat;
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
import com.ib.client.Order;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import common.Bar;
import common.DataEngine;
import common.OrderEngine;
import common.PriceTypeEnum;
import common.SimuDataEngine;
import common.Strategy;
import common.TradeAlarm;

public class HEStrategy implements Strategy
{
	protected static int securityNoLeft; 
	
	public static int getSecurityNoLeft() {
		return securityNoLeft;
	}
	public void setSecurityNoLeft(int securityNoLeft) {
		this.securityNoLeft = securityNoLeft;  // use this to set static value correctly
	}
	
	private double yesterdayClose;  // for HE strategy, set from config file.   
	private long DUVolume; 
	private long FRVVolume;  
	
	private double todayOpen;
	private double todayClose;
	private long todayVolume; // today's volume, unit is hand, *100 for shares!!!
	private int orderAmount;   
	
	private double currentPrice; 
	private double lmtPrice; 

	OrderEngine orderEngine; 
	DataEngine dataEngine; 
	
	protected int mainOrderId; 
	protected String ocaGroup;  // with stop order and day close lower order. 
	protected int executedShares = 0; 
	
	Contract contract = new Contract(); 
	Order order = new Order();   // keep the main order info
	
	TreeMap<Long, Bar> barMap = new TreeMap<Long, Bar>(); 
	
	boolean placed = false;
	
	private boolean testing;  // testing true means save data mode

	private transient final Logger log = LoggerFactory.getLogger(Strategy.class);
    // private static final int CONSOLIDATE_30MINUTE_BAR = 30;
    Core taLib = new Core();   // may change to spring DI. 

    public HEStrategy()
    {
    	contract.m_secType = "STK";
    	contract.m_exchange = "SMART";
        contract.m_currency = "USD"; 
    }
    
    @SuppressWarnings("deprecation")
	public void checkStrategy()
    {
    	Date now = new Date(dataEngine.getCurrentServerTime()*1000);
    	Date nine30 = new Date(now.getYear(), now.getMonth(), now.getDate(), 9,30,0);
        Date eleven31 = new Date(now.getYear(), now.getMonth(), now.getDate(), 11,31,0);
        Date ten30 = new Date(now.getYear(), now.getMonth(), now.getDate(), 10,31,0);
        Date fifteen30= new Date(now.getYear(), now.getMonth(), now.getDate(), 15,30,0);
        
        Date beginToday = new Date(now.getYear(), now.getMonth(), now.getDate(), 0,0,0);
        Date endToday = new Date(now.getYear(), now.getMonth(), now.getDate(), 23,59,59);
        
        long eleven31Sec = eleven31.getTime()/1000; 
        long nine30Sec = nine30.getTime()/1000;
        long beginTodaySec = beginToday.getTime()/1000;
        long endTodaySec = endToday.getTime()/1000; 
        long fifteen30Sec = fifteen30.getTime()/1000; 
        
        log.info("{} checking HEstrategy, may calculate indicators..", contract.m_symbol);
        Collection<Bar> barCollection = barMap.values();
        log.info("bar size:{}", barCollection.size());
        
        Iterator<Bar> iterator = barCollection.iterator();       
        Bar bar = null;       
        setTodayVolume(0);
        while (iterator.hasNext()) {          
        	bar = (Bar) iterator.next();          
        	long barSec = bar.getDate();
        	//if (testing) {  // log
        		Date datetmp = new Date(barSec * 1000);  // mili-second
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        		String dateStr = sdf.format(datetmp);
        		log.info("Bar data: {}, {}, close:{}, volume:{}", new Object[]{ contract.m_symbol, dateStr, bar.getClose(), bar.getVolume() } );
        	//}
        	if ((barSec<endTodaySec)&&(barSec>beginTodaySec)) {
        		setTodayVolume(getTodayVolume() + bar.getVolume());
        	}
        	if (barSec == nine30Sec) {
        		setTodayOpen(bar.getOpen()); 
        	}
        	if (barSec == fifteen30Sec) {   // not really close until approaching 16:00
        		setTodayClose(bar.getClose());
        	}
        }  // finish checking, and calculate the specific period volume. 
        
        if (this.placed) { 
        	log.info("has been placed, check exitStrategy");
        	exitStrategy(); 
        	return; 
        }
        if(HEStrategy.securityNoLeft<=0) {
        	log.info("max number reached. return");
        	return; 
        }
	    if (now.before(nine30)) { 
	       	log.info("before 9:30, return");
	       	return; 
	    }  
	    if (now.after(eleven31)) {   // note, it's eleven according to the document.
	       	log.info("after 11:31, return");
	       	return; 
	    }
	    if (this.getCurrentPrice() < this.yesterdayClose) {  // current price set by real time bar
	       	log.info("current:{} < yesterday close:{}, return", getCurrentPrice(), yesterdayClose);
	       	return; 
	    }
	    if (this.getTodayVolume()*100 < this.DUVolume) {	// not vol check for test as it used now time. 
	       	log.info("volume so far:{} < DU volume:{}, return", getTodayVolume(), DUVolume);
	       	// * 100, seems ib unit
	       	return; 
	    }  
	    if (( todayOpen-yesterdayClose)/yesterdayClose>0.05 ) {
	       	log.info("today open:{} gap up 5% of yesterday close:{}, return", getTodayOpen(), getYesterdayClose());
	       	return; // open 5% gap of yesterday's close.
	    }
        
        log.info(contract.m_symbol + " today's present volume so far {}, exceeds dry up volume {}", this.getTodayVolume(), this.getDUVolume());
        
        // check MACD Histogram (5,13,6), and Stochastics (14,1,3), so make sure it has at least 18 bars. it only checks at the time range. 
        // checking Stochastics. 
        int barSize = 18; 
        if (barCollection.size() < barSize) {
        	log.error("{} has less than 16 bars, return. {}", contract.m_symbol, barCollection.size());
        	return; 
        }
        
        double[] inHigh = new double[barSize];
        double[] inLow = new double[barSize];
        double[] inClose = new double[barSize];
        iterator = barCollection.iterator();
        int i = 0;
        while (iterator.hasNext()) {   
        	bar = (Bar) iterator.next();      
        	if (i++ < (barCollection.size()-barSize) ) {
        		continue; 
        	}
        	inHigh[i-1-(barCollection.size()-barSize)] = bar.getHigh();
        	inLow[i-1-(barCollection.size()-barSize)] = bar.getLow();
        	inClose[i-1-(barCollection.size()-barSize)] = bar.getClose();
        } 
        
        MInteger outBegIdx = new MInteger(); 
        MInteger outNBElement = new MInteger(); 
        double[] outSlowK = new double[barSize];
        double[] outSlowD = new double[barSize];
        
        //taLib.stoch(startIdx, endIdx, inHigh, inLow, inClose, optInFastK_Period, optInSlowK_Period, optInSlowK_MAType, optInSlowD_Period, optInSlowD_MAType, outBegIdx, outNBElement, outSlowK, outSlowD)
        taLib.stoch(0, inClose.length-1, inHigh, inLow, inClose, 14, 1, MAType.Sma, 3, MAType.Sma, outBegIdx, outNBElement, outSlowK, outSlowD);
//        if (outNBElement.value != 1) {
//        	log.error("please re-calculate Bar size to pass to ta-lab");
//        	return; 
//        }
        if (outSlowK[outNBElement.value-1] <= 75 ) {
        	log.warn("{} slowK does not meet.{}", contract.m_symbol, outSlowK[outNBElement.value-1]);
        	return; 
        }
        if (outSlowK[outNBElement.value-2] < 50 ) {
        	log.warn("*****************{} previous bar slowK does not reach 50.{}", contract.m_symbol, outSlowK[outNBElement.value-2]);
        }
        log.info("{} passing Stoch check...{}", contract.m_symbol, outSlowK[outNBElement.value-1]);        
        
        // checking MACD
        barSize = 26;   // two days at least
        if (barCollection.size() < barSize) {
        	log.error("{} has less than 18 bars, return. {}", contract.m_symbol, barCollection.size());
        	return; 
        }
        
        double[] inDouble = new double[barSize];
        iterator = barCollection.iterator();
        i = 0;
        while (iterator.hasNext()) {   
        	bar = (Bar) iterator.next();      
        	if (i++ < (barCollection.size()-barSize) ) {
        		continue; 
        	}
        	inDouble[i-1-(barCollection.size()-barSize)] = bar.getClose();
        } 
        
        outBegIdx = new MInteger(); 
        outNBElement = new MInteger(); 
        double[] outMACD = new double[barSize];
        double[] outMACDSignal = new double[barSize];
        double[] outMACDHist = new double[barSize];
        
        taLib.macd(0, inDouble.length-1, inDouble, 5, 13, 6, outBegIdx, outNBElement, outMACD, outMACDSignal, outMACDHist);
        
//        if (outNBElement.value != 1) {
//        	log.error("please re-calculate Bar size to pass to ta-lab");
//        	return; 
//        }
        if (outMACDHist[outNBElement.value-1] <=0 ) {  // the value comes out 0.21, but other caculate out like 0.17, 
        	log.warn("{} macdHist does not meet.{}", contract.m_symbol, outMACDHist[outNBElement.value-1]);
        	return; 
        }
        log.info("{} passing MACD check...{}", contract.m_symbol,outMACDHist[outNBElement.value-1]);
        
        log.info("Placing orders: {}", contract.m_symbol);
       	//if (!testing) {
            // main order, set but not transmit yet
       		double currentPrice = getCurrentPrice();
       		order.m_action = "BUY"; 
       		order.m_totalQuantity = orderAmount;
       		order.m_orderType = "LMT";
       		order.m_tif = "GTC";
       		order.m_lmtPrice = currentPrice;   
       		order.m_transmit = false; 
       		mainOrderId = orderEngine.placeOrder(this, contract, order);
       		ocaGroup = String.format("%d", mainOrderId);  // used for place end of day order when close red
       		
       		// place an stop trail order of -5% stop loss
//       		order.m_action = "SELL";
//       		order.m_orderType = "TRAIL";
//       		order.m_tif = "GTC";
//       		order.m_percentOffset = 0.05;  // trailing stop %, not work that way, from IB
//       		order.m_transmit = true;  // transmit
//       		order.m_parentId = mainOrderId;
//       		order.m_ocaGroup = ocaGroup; 
//       		orderEngine.placeOrder(this, contract, order); 
       		
       		// place an limit order of 10% target
       		order.m_action = "SELL";
       		order.m_orderType = "LMT";
       		order.m_tif = "GTC";
       		order.m_lmtPrice = Math.round((currentPrice*1.1)/0.01)*0.01;  // adjust to min tick.
       		order.m_transmit = false;   // if one of child is transmitted, all transmitted? 
       		order.m_parentId = mainOrderId;
       		order.m_ocaGroup = ocaGroup; 
       		orderEngine.placeOrder(this, contract, order); 
       		
       		// real transmit the main order. 
       		order.m_action = "BUY"; 
       		order.m_totalQuantity = orderAmount;
       		order.m_orderType = "LMT";
       		order.m_tif = "GTC";
       		order.m_lmtPrice = currentPrice;   
       		order.m_transmit = true; 
       		orderEngine.placeOrder(mainOrderId, this, contract, order);
       		
       		// place an stop order of -2% stop loss
       		// this moves back not to really be placed
       		order.m_action = "SELL";
       		order.m_orderType = "STP";
       		order.m_tif = "DAY";
       		order.m_auxPrice = ((int)(currentPrice*0.98/0.01))*0.01;  // adjust to min tick.
       		order.m_transmit = false;  // wait for the final transmit. 
       		// order.m_parentId = mainOrderId;
       		order.m_ocaGroup = ocaGroup; 
       		orderEngine.placeOrder(this, contract, order); 
        //	}
       		
       	placed = true;
       	HEStrategy.securityNoLeft--; 
    }
    
    
    private void exitStrategy() {
    	if (!placed) { return; }
    	
    	// 1, %2 stop, same day
    	// 2, volume not reached, same day
    	// 3, price is lower than previous close. 
    	
    	// others, 4, 5% trailing stop; 
    	// 5, out after 4/6/or 8 days
    	// 6, high vol reached in first 15-30 minutes. i.e. by end of day, vol bigger than peak one. 
    	// 7, 10% profits
    	Toolkit toolkit;
    	toolkit = Toolkit.getDefaultToolkit();
        
    	// have set stop loss when placing order...
//    	if ((this.getCurrentPrice()-this.getLmtPrice())/this.getLmtPrice()<-0.02) {  // exit rule 1
//    		log.warn("Exit: same day price 2% stop. current price:{}", this.getCurrentPrice());
//    		toolkit.beep();
//    		return; 
//    	}
    	
    	long nowSec = dataEngine.getCurrentServerTime();
    	Date now = new Date(nowSec*1000);
    	long fifteen54Sec = new Date(now.getYear(),now.getMonth(),now.getDate(), 15,54,00).getTime()/1000;
    	long sixteen00Sec = new Date(now.getYear(),now.getMonth(),now.getDate(), 16,00,00).getTime()/1000;
    	if ((nowSec>=fifteen54Sec)&&(nowSec<sixteen00Sec)) {
    		if (this.getTodayVolume()*100<this.getFRVVolume()) { // exit rule 2
       			log.warn("Exit: today volume not reached FRV volume: {}", this.getTodayVolume()); 
    			toolkit.beep();
        		return;    			
    		}
    		if (this.getCurrentPrice()<this.getYesterdayClose()) {  // exit rule 3, current price is almost close price now.
    			log.warn("Exit: today close lower than yesterday close: {}", this.getTodayClose()); 
    			toolkit.beep();
    	        TradeAlarm.playSound("siren.wav"); 
    	        
    			// place order to sell
           		order.m_action = "SELL";
           		order.m_totalQuantity = executedShares;
           		order.m_orderType = "MKT";   // market order
           		order.m_tif = "DAY";
           		order.m_transmit = false;  // transmit, lesson learnt, not sell, just consider. 
           		order.m_ocaGroup = ocaGroup;   // same group with -2% stop loss
           		orderEngine.placeOrder(this, contract, order); 
           		placed = false;  // set the executed main order to false to avoid stop sell multiple times. 
        		return; 
    		}
    	}
	}
    
    
	public void monitor() {
    	// every 5 minutes, request 3 day, 30 minutes historical data, store them in a data structure?
    	Random generator = new Random();
        int delay = generator.nextInt( 5*1000 );   // delay randomly for 5 sec. unit milisecond
        int period = 5*60*1000;  // repeat every 5 min.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		if (currentPrice<=0) {
        			dataEngine.reqRealtimeBar(HEStrategy.this, PriceTypeEnum.TRADES.toString(), true);
        		}
        		String endDateTime;  //yyyymmdd HH:mm:ss ttt, where "ttt" is the optional time zone.
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
         	    endDateTime = sdf.format(new Date(dataEngine.getCurrentServerTime()*1000));
        	    log.info("requesting data till {}", endDateTime);
 
        	    dataEngine.reqHistoricalData(HEStrategy.this, endDateTime, "3 D", "30 mins", "TRADES", 1, 2);   
        	    // request 6 day 5 minutes data for testing. 
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

    public boolean parse(String str) {
    	if ("S,".equals(str.substring(0, 2).toUpperCase())) {
    		return false; 
    	}
    	String[] strArr = str.split(",");
    	String symbol = strArr[0].trim();
    	double yesterdayClose = 0; 
    	long DUVolume = 0; 
    	int orderAmount = 0; 
    	try {
    		yesterdayClose = Double.parseDouble(strArr[1].trim());
    		DUVolume = Long.parseLong(strArr[2].trim());
    		orderAmount = Integer.parseInt(strArr[3].trim());
    	} catch (Exception e){
    		log.error("error in stock file line: {}", str);
    		return false; 
    	}
		this.setSymbol(symbol);
		this.setOrderAmount(orderAmount);
		this.setYesterdayClose(yesterdayClose);
		this.setDUVolume(DUVolume);
		return true; 
	}
	
	public void setSymbol(String symbol) {
		this.contract.m_symbol = symbol; 
		
	}
	public void setOrderAmount(int orderAmount) {
		this.orderAmount = orderAmount;
	}
	public int getOrderAmount() {
		return this.orderAmount;
	}
	public void setYesterdayClose(double yesterdayClose) {
		this.yesterdayClose = yesterdayClose;
	}
	public double getYesterdayClose() {
		return yesterdayClose;
	}
	public void setDUVolume(long dUVolume) {
		DUVolume = dUVolume;
	}
	public long getDUVolume() {
		return DUVolume;
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
	public void setFRVVolume(long fRVVolume) {
		FRVVolume = fRVVolume;
	}
	public long getFRVVolume() {
		return FRVVolume;
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

	public void setTodayClose(double todayClose) {
		this.todayClose = todayClose;
	}

	public double getTodayClose() {
		return todayClose;
	}

	public void setTodayVolume(long todayVolume) {
		this.todayVolume = todayVolume;
	}

	public long getTodayVolume() {
		return todayVolume;
	}

	public void setLmtPrice(double lmtPrice) {
		this.lmtPrice = lmtPrice;
	}

	public double getLmtPrice() {
		return lmtPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}
	public Contract getContract() {
		return contract;
	}
	
	public void checkExecution(Execution execution) {
		if (execution.m_orderId == mainOrderId) { 
			executedShares = execution.m_cumQty; 
		}
	}
	
	public void setTargetReached(boolean targetReached) {
		// TODO Auto-generated method stub
	}

}