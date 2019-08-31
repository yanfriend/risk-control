package futures;

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
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

import common.Bar;
import common.DataEngine;
import common.OrderEngine;
import common.PriceTypeEnum;
import common.Security;
import common.SimuDataEngine;
import common.Strategy;
import common.TradeUtil;

public class Top25OneStrategy implements Strategy  
{
	protected static int securityNoLeft; 
	
	public static int getSecurityNoLeft() {
		return securityNoLeft;
	}

	public void setSecurityNoLeft(int securityNoLeft) {
		this.securityNoLeft = securityNoLeft;
	}

	private int orderAmount; 
	int longSide = 0; // 1, long; 0, stop; -1, short
	private double miniTick;   // where this should be ? in order structure? 

	protected OrderEngine orderEngine; 
	protected DataEngine dataEngine; 
	
	protected Contract contract = new Contract(); 
	protected Order order = new Order();   // keep the main order info
	    
	double lastStopPrice;  // stop price, changing.  
	double normalizedSetupPrice;  // set up price
	protected int targetOrderId;
	private int stopOrderId; 
	
	private double currentPrice;   // not inuse

	protected TreeMap<Long, Bar> barMap = new TreeMap<Long, Bar>(); 
	
	protected boolean placed = false;
	private boolean targetReached; 
	
	private boolean testing;  // true means to in save data mode.

	private transient final Logger log = LoggerFactory.getLogger(Security.class);
    Core taLib = new Core();   // may change to spring DI. 

    public Top25OneStrategy()
    {
    	this.setMiniTick(1);
    	this.setSecurityType("FUT");
        contract.m_currency = "USD"; 
    }
    
    public void checkStrategy()
    {
    	if (this.placed) { 
    		log.info("has been placed, check strategy");
    		exitStrategy(); 
    		return; 
    	}    	
    	if (Top25OneStrategy.securityNoLeft<=0) {
    		log.info("max number reached, return");
    		return;
    	}
    	
    	// longSide = 1; // for testing, long only.
    	// boolean triggered = true; // true for test only... // false; 
         
    	int barSize = 52; 
        
    	log.info("{} checking futures strategy, may calculate indicators..", contract.m_symbol );
        Collection<Bar> barCollection = barMap.values();
        log.info("bar size:{}", barCollection.size());
        
        double[] inDouble = new double[barSize];
        Iterator<Bar> iterator = barCollection.iterator();       
        int i = 0;
        while (iterator.hasNext()) {   
        	Bar bar = (Bar) iterator.next();      
        	if (i++ < (barCollection.size()-barSize) ) {
        		continue; 
        	}
        	inDouble[i-1-(barCollection.size()-barSize)] = bar.getClose();
        } 
        
        MInteger outBegIdxMacd = new MInteger(); 
        MInteger outNBElementMacd = new MInteger(); 
        MInteger outBegIdxEma = new MInteger(); 
        MInteger outNBElementEma = new MInteger(); 
        double[] outMACD = new double[barSize];
        double[] outMACDSignal = new double[barSize];
        double[] outMACDHist = new double[barSize];
        double[] outEma = new double[barSize];
        
        taLib.macd(0, inDouble.length-1, inDouble, 12, 26, 9, outBegIdxMacd, outNBElementMacd, outMACD, outMACDSignal, outMACDHist);
  
    	taLib.ema(0, inDouble.length-1, inDouble, 20, outBegIdxEma, outNBElementEma, outEma);  // the first one is the oldest one. outNBElement-1 is the latest
    	log.info("last macd hist:{}, last ema:{}", outMACDHist[outNBElementMacd.value-1], outEma[outNBElementEma.value-1]);
    	if (longSide>0) {
    		setupLong(outMACDHist, outBegIdxMacd.value, outNBElementMacd.value, outEma, outBegIdxEma.value, outNBElementEma.value);
    	} else if (longSide<0) {
    		setupShort(outMACDHist, outBegIdxMacd.value, outNBElementMacd.value, outEma, outBegIdxEma.value, outNBElementEma.value);
    	}
    }
    
	private void setupLong(double[] outMACDHist,int outBegIdxMacd, int outNBElementMacd, double[] outEma, int outBegIdxEma, int outNBElementEma){
		int lastMacd = outNBElementMacd -1; 
		int lastEma = outNBElementEma -1; 
		double setupPrice = 0; 
		if (outMACDHist[lastMacd]>0) {  // can cause problems if big inter-day gap 
			for (int i=lastMacd-1; i>lastMacd-6; i--) {
				log.info("for long, macdHist = {}",outMACDHist[i]);
				if (outMACDHist[i]<0) {  // crossed above in last 5 bars, have buy signal
					setupPrice = outEma[lastEma];
					break; 
				}
			}
		} else {  // not work if macd is minus
			return; 
		}
		
		if (setupPrice <=0) return; 
		log.info("set up price(latest ems)={}",setupPrice);
		setupPrice += 10*miniTick; 
		
		normalizedSetupPrice = Math.round(setupPrice/miniTick)*miniTick; 
        log.info("Placing futures order: {}, {}", contract.m_symbol, normalizedSetupPrice);

        placeOrder(order, normalizedSetupPrice, 30);

       	placed = true;
       	Top25OneStrategy.securityNoLeft--; 
		
	}
    
	private void setupShort(double[] outMACDHist,int outBegIdxMacd, int outNBElementMacd, double[] outEma, int outBegIdxEma, int outNBElementEma){
		int lastMacd = outNBElementMacd -1; 
		int lastEma = outNBElementEma -1; 
		double setupPrice = 0; 
		if (outMACDHist[lastMacd]<0) {  // can cause problems if big inter-day gap 
			for (int i=lastMacd-1; i>lastMacd-6; i--) {
				log.info("for short, macdHist = {}",outMACDHist[i]);
				if (outMACDHist[i]>0) {  // crossed above in last 5 bars, have buy signal
					setupPrice = outEma[lastEma];
					break; 
				}
			}
		} else {  // not work if macd is plus
			return; 
		}
		
		if (setupPrice <=0) return; 
		log.info("set up price(latest ems)={}",setupPrice);
		setupPrice -= 10*miniTick; 
		
		normalizedSetupPrice = Math.round(setupPrice/miniTick)*miniTick; 
        log.info("Placing futures order: {}, {}", contract.m_symbol, normalizedSetupPrice);

        placeOrder(order, normalizedSetupPrice, 30);
        
       	placed = true;
       	Top25OneStrategy.securityNoLeft--; 
	}
	
	private void placeOrder(Order order, double normalizedSetupPrice, int distanceTicks) {
        order.m_totalQuantity = orderAmount;  // double the b/s unit
        order.m_orderType = "STPLMT";
        order.m_tif = "DAY";
        order.m_outsideRth = true;
        order.m_transmit = false;

        order.m_auxPrice = normalizedSetupPrice;
        double targetPrice, stopPrice; 
        if (longSide>0) {
        	order.m_lmtPrice = normalizedSetupPrice + 1*this.getMiniTick();
        	order.m_action = "BUY";
        	targetPrice = normalizedSetupPrice + distanceTicks*this.getMiniTick();
        	stopPrice = normalizedSetupPrice - distanceTicks*this.getMiniTick();
        } else if (longSide<0) {
        	order.m_lmtPrice = normalizedSetupPrice - 1*this.getMiniTick();
            order.m_action = "SELL";
        	targetPrice = normalizedSetupPrice - distanceTicks*this.getMiniTick();
        	stopPrice = normalizedSetupPrice + distanceTicks*this.getMiniTick();
        } else {
        	log.error("order buy/sell is nto set: {}", contract.m_symbol);
        	return; 
        }
       	int orderId1 = orderEngine.placeOrder(this, contract, order);    	
    	int orderId2 = orderEngine.placeOrder(this, contract, order);    
       	
    	String action = order.m_action;
    	action = TradeUtil.reverseAction(action);
    	
    	// 1st profit order
    	order.m_parentId = orderId1;
    	order.m_action = action; 
    	order.m_totalQuantity = orderAmount;
    	order.m_orderType = "LMT";
    	order.m_tif = "GTC";
    	order.m_lmtPrice = targetPrice;
    	order.m_ocaGroup = String.format("OCA%s", orderId1);
    	//order.m_transmit = true;
    	targetOrderId = orderEngine.placeOrder(this, contract, order);    	// keep target order id
       	
    	// 1st stop order
    	order.m_orderType = "STP";
    	order.m_auxPrice = stopPrice;
    	order.m_ocaGroup = String.format("OCA%s", orderId1);
    	order.m_transmit = true;
    	orderEngine.placeOrder(this, contract, order);    	// no need for this stop

    	// stop order for another group, trailing stop
       	order.m_parentId = orderId2;   // not in same group
    	order.m_orderType = "STP";
    	order.m_auxPrice = stopPrice;
    	lastStopPrice = stopPrice;  // keep global
    	order.m_ocaGroup = String.format("OCA%s", orderId2);
    	order.m_transmit = true;
    	stopOrderId = orderEngine.placeOrder(this, contract, order);    	// keep this stop id for trail
    	
    	// re-place and transmit order
    	action = TradeUtil.reverseAction(action);
    	order.m_action = action;
    	order.m_transmit = true;  // only change
    	order.m_ocaGroup = "";
        order.m_totalQuantity = orderAmount;  // double the b/s unit
        order.m_orderType = "STPLMT";
        order.m_tif = "DAY";
        order.m_auxPrice = normalizedSetupPrice;
        if ("BUY".equals(order.m_action)) {
        	order.m_lmtPrice = normalizedSetupPrice + 1*this.getMiniTick();
        } else if ("SELL".equals(order.m_action)) {
        	order.m_lmtPrice = normalizedSetupPrice - 1*this.getMiniTick();
        } else {
        	log.error("order buy/sell is nto set: {}", contract.m_symbol);
        	return; 
        }
       	orderEngine.placeOrder(orderId1, this, contract, order);    	// place and transmit. actually not needed.
       	orderEngine.placeOrder(orderId2, this, contract, order);    	// place and transmit. 
	}
	
    public int getLongSide() {
		return longSide;
	}

	public void setLongSide(int longSide) {
		this.longSide = longSide;
	}

	private void exitStrategy() {
    	if (!this.targetReached) {return;}
    	
    	// adjust stop price of stopOrderId
    	log.info("{} checking futures strategy, may calculate indicators..", contract.m_symbol );
        Collection<Bar> barCollection = barMap.values();
        log.info("bar size:{}", barCollection.size());
        
        int barSize = 52; 
        double[] inDouble = new double[barSize];
        Iterator<Bar> iterator = barCollection.iterator();       
        int i = 0;
        while (iterator.hasNext()) {   
        	Bar bar = (Bar) iterator.next();      
        	if (i++ < (barCollection.size()-barSize) ) {
        		continue; 
        	}
        	inDouble[i-1-(barCollection.size()-barSize)] = bar.getClose();
        } 
        
        MInteger outBegIdxEma = new MInteger(); 
        MInteger outNBElementEma = new MInteger(); 
        double[] outEma = new double[barSize];
    	taLib.ema(0, inDouble.length-1, inDouble, 20, outBegIdxEma, outNBElementEma, outEma);  // the first one is the oldest one. outNBElement-1 is the latest
    	
    	double lastEma = outEma[outNBElementEma.value-1];
    	if (longSide >0) {
    		lastEma = Math.round(lastEma/miniTick - 20)*miniTick; 
    		if ((lastStopPrice < normalizedSetupPrice)||(lastStopPrice < lastEma )) {
    			lastStopPrice = Math.max(normalizedSetupPrice, lastEma);   // set to break even at least. 
    			order.m_action = "SELL";
    		} else return; 
    	} else if (longSide<0) {
    		lastEma = Math.round(lastEma/miniTick + 20)*miniTick;
    		if ((lastStopPrice > normalizedSetupPrice)||(lastStopPrice > lastEma )) {    		
    			lastStopPrice = Math.min(normalizedSetupPrice, lastEma);   // set to break even at least. 
    			order.m_action = "BUY";
    		} else return; 
    	} else { // longSide == 0
    		return; 
    	} 
    	
    	order.m_auxPrice = lastStopPrice; 
    	order.m_totalQuantity = orderAmount;   
    	order.m_orderType = "STP";
    	order.m_tif = "GTC";
    	order.m_outsideRth = true;
    	order.m_transmit = true;
    	orderEngine.placeOrder(stopOrderId, this, contract, order);
	}
    
    
	public void monitor() {
        int period = 30*1000;  // repeat every 30 seconds.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		if (currentPrice<=0) {
        			dataEngine.reqRealtimeBar(Top25OneStrategy.this, PriceTypeEnum.TRADES.toString(), false);
        		}
        		String endDateTime;  //yyyymmdd HH:mm:ss ttt, where "ttt" is the optional time zone.
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
         	    endDateTime = sdf.format(new Date(dataEngine.getCurrentServerTime()*1000));
        	    log.info("requesting data till {}", endDateTime);
       	    	dataEngine.reqHistoricalData(Top25OneStrategy.this, endDateTime, "2 D", "5 mins", "TRADES", 0, 2);  // rth set to 0.    
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
    			new Object[]{ contract.m_symbol, dateStr, close, volume});
    	
    	long barKey = datelong;
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
    
	public void checkExecution(Execution execution) {
		int orderId = execution.m_orderId;
		if (targetOrderId == orderId) {
			setTargetReached(true);		
		}
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
	public double getMiniTick() {
		return miniTick;
	}
	public void setMiniTick(double miniTick) {
		this.miniTick = miniTick;
	}
	public void setSymbol(String symbol) {
		this.contract.m_symbol = symbol; 
	}
	public void setExpireDate(String expireDate) {
		this.contract.m_expiry = expireDate;
	}
	public void setExchange(String exchange) {
		this.contract.m_exchange = exchange; 
	}
	public void setOrderAmount(int orderAmount) {
		this.orderAmount = orderAmount; 
	}
	public int getOrderAmount() {
		return orderAmount;
	}
	public void setSecurityType(String type) {
		this.contract.m_secType = type; 
	}
	public Contract getContract() {
		return contract;
	}
	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public void setCurrentPrice(double open, double high, double low, double close) {
		this.currentPrice = close;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setTargetReached(boolean targetReached) {
		this.targetReached = targetReached;
	}

	public boolean isTargetReached() {
		return targetReached;
	}
	
	public boolean parse(String str) {
    	if ("R,".equals(str.substring(0, 2).toUpperCase())) {
    		return false; 
    	}
    	String[] strArr = str.split(",");
    	try {
    		String symbol = strArr[0].trim();
      		String expireDate = strArr[1].trim();
      		String exchange = strArr[2].trim();
      		int longSide = Integer.parseInt(strArr[3].trim());
      		int orderAmount = Integer.parseInt(strArr[4].trim());
      		double miniTick = Double.parseDouble(strArr[5].trim());
      		this.setSymbol(symbol);
      		this.setExpireDate(expireDate);
      		this.setExchange(exchange);
      		this.setLongSide(longSide);
      		this.setOrderAmount(orderAmount);
      		this.setMiniTick(miniTick);
      		return true;
	    } catch (Exception e){
	    	log.error("error in futures file line: {}\n{}", str,e);
	    	return false; 
	    }
	}


}