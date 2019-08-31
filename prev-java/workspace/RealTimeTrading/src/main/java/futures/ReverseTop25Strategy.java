package futures;

import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Execution;
import com.ib.client.Order;
import com.tictactec.ta.lib.MInteger;

import common.Bar;
import common.PriceTypeEnum;
import common.SimuDataEngine;
import common.TradeUtil;

public class ReverseTop25Strategy extends Top25OneStrategy  
{
	private int stopOrderId=-1;
	private int mainOrderId=-1;
	private int currentPosition = 0; 
	
	int consecutiveLoss = 0; 
	
	private transient final Logger log = LoggerFactory.getLogger(ReverseTop25Strategy.class);
	
    public ReverseTop25Strategy()
    {
    	super(); 
    	// this.setMiniTick(1);
    	// this.setSecurityType("FUT");
    	// getContract().m_currency = "USD"; 
    }
    
    public void checkStrategy()
    {
    	if (this.placed) { 
    		log.info("{}, has been placed, check exit strategy", contract.m_symbol);
    		// exitStrategy(); 
    		return; 
    	}    	
    	if (currentPosition>0) {
    		log.info("{} holding position:{}, check exit strategy", contract.m_symbol, currentPosition);
    		exitStrategy();
    		return; 
    	}
    	if (ReverseTop25Strategy.securityNoLeft<=0) {
    		log.info("{}, max amount reached, return", contract.m_symbol);
    		return;
    	}
    	
    	if ((consecutiveLoss >= 2) && (securityNoLeft>=2)) {   // This is like pair trading.
    		// if consecutive loss two times, continue only if it is holding another security.  
    		log.info("{}, two consecutive losses, don't open a new one unless the other one is holding, return", contract.m_symbol);
        	Toolkit toolkit;
        	toolkit = Toolkit.getDefaultToolkit();
    		toolkit.beep(); toolkit.beep(); toolkit.beep();
    		return; 
    	}
    	
    	// for testing only
    	/*
    	if (getCurrentPrice()<=0) return; 
        placeOrder(order, getCurrentPrice(), 30);
       	placed = true;
       	if(true) return; 
        // end of for testing.  
       	*/
    	
    	int barSize = 52; 
        
    	log.info("{} checking futures strategy, may calculate indicators..", getContract().m_symbol );
        Collection<Bar> barCollection = barMap.values();
        log.info("bar size:{}", barCollection.size());
        
        float[] inFloat = new float[barSize];
        Iterator<Bar> iterator = barCollection.iterator();       
        int i = 0;
        while (iterator.hasNext()) {   
        	Bar bar = (Bar) iterator.next();      
        	if (i++ < (barCollection.size()-barSize) ) {
        		continue; 
        	}
        	inFloat[i-1-(barCollection.size()-barSize)] = (float) bar.getClose();
        } 
        
        MInteger outBegIdxMacd = new MInteger(); 
        MInteger outNBElementMacd = new MInteger(); 
        MInteger outBegIdxEma = new MInteger(); 
        MInteger outNBElementEma = new MInteger(); 
        double[] outMACD = new double[barSize];
        double[] outMACDSignal = new double[barSize];
        double[] outMACDHist = new double[barSize];
        double[] outEma = new double[barSize];
        
        taLib.macd(0, inFloat.length-1, inFloat, 12, 26, 9, outBegIdxMacd, outNBElementMacd, outMACD, outMACDSignal, outMACDHist);
  
    	taLib.ema(0, inFloat.length-1, inFloat, 20, outBegIdxEma, outNBElementEma, outEma);  // the first one is the oldest one. outNBElement-1 is the latest
    	log.info("last macd hist:{}, last ema:{}", outMACDHist[outNBElementMacd.value-1], outEma[outNBElementEma.value-1]);
    	
    	// Important here, change long and short side to reverse the original strategy
    	// for example, if short, then becomes long here, in set up long, change buy to sell.
    	
    	longSide = -longSide; 
    	// End of changes. 
    	
    	if (longSide>0) {
    		setupLong(outMACDHist, outBegIdxMacd.value, outNBElementMacd.value, outEma, outBegIdxEma.value, outNBElementEma.value);
    	} else if (longSide<0) {
    		setupShort(outMACDHist, outBegIdxMacd.value, outNBElementMacd.value, outEma, outBegIdxEma.value, outNBElementEma.value);
    	}
    	longSide = -longSide;  // change back for next check.
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
		setupPrice += 10*getMiniTick(); 
		
		normalizedSetupPrice = Math.round(setupPrice/getMiniTick())*getMiniTick(); 
        log.info("Placing futures order: {}, {}", getContract().m_symbol, normalizedSetupPrice);

        placeOrder(order, normalizedSetupPrice, 30);
       	placed = true;
	
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
		setupPrice -= 10*getMiniTick(); 
		
		normalizedSetupPrice = Math.round(setupPrice/getMiniTick())*getMiniTick(); 
        log.info("Placing futures order: {}, {}", getContract().m_symbol, normalizedSetupPrice);

        placeOrder(order, normalizedSetupPrice, 30);
       	placed = true;
	}
	
	private void placeOrder(Order order, double normalizedSetupPrice, int distanceTicks) {
        order.m_totalQuantity = getOrderAmount();  
        order.m_orderType = "LMT"; //"STPLMT";
        order.m_tif = "DAY";
        order.m_outsideRth = true;
        order.m_transmit = true;
        order.m_ocaGroup = null; 

        order.m_auxPrice = normalizedSetupPrice;
        double targetPrice, stopPrice; 
        
        // change here, if longSide > 0 , the input means short, so sell.  
        // if (longSide>0) {
        if (longSide < 0) {
        	order.m_lmtPrice = normalizedSetupPrice; // + 1*this.getMiniTick();
        	order.m_action = "BUY";
        	targetPrice = normalizedSetupPrice + distanceTicks*this.getMiniTick();
        	stopPrice = normalizedSetupPrice - distanceTicks*this.getMiniTick();
        } else if (longSide > 0) {
        	order.m_lmtPrice = normalizedSetupPrice; //  - 1*this.getMiniTick();
            order.m_action = "SELL";
        	targetPrice = normalizedSetupPrice - distanceTicks*this.getMiniTick();
        	stopPrice = normalizedSetupPrice + distanceTicks*this.getMiniTick();
        } else {
        	log.error("order buy/sell is nto set: {}", contract.m_symbol);
        	return; 
        }
       	mainOrderId = orderEngine.placeOrder(this, contract, order);    	
    	
       	// cancel main order after 25 minutes, and set placed to false. 
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
        	public void run() {
        		orderEngine.cancleOrder(mainOrderId);
        		placed = false;
        		timer.cancel();
        	}
        }, 25*60*1000);  // 25 minutes, cancel main order if not executed.
       	
	}
	
	private void exitStrategy() {
		// fixed target and stop
	}
    
    
	public void monitor() {
        int period = 30*1000;  // repeat every 30 seconds.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		if (getCurrentPrice()<=0) {
        			dataEngine.reqRealtimeBar(ReverseTop25Strategy.this, PriceTypeEnum.TRADES.toString(), false);
        		}
        		long datelong = dataEngine.getCurrentServerTime(); // in second; 
        		Date datetmp = new Date(datelong * 1000);  // mili-second
        		Date threeAm = new Date(datetmp.getYear(), datetmp.getMonth(), datetmp.getDate(),3,0,0);  // this is regular time for both in US and europe
        		Date fourPm = new Date(datetmp.getYear(), datetmp.getMonth(), datetmp.getDate(),16,0,0);  // neither summer time. 
        		
        		if (datetmp.before(threeAm)) {
        			log.warn("before 3am, return");
            		return; 
            	}
        		if (datetmp.after(fourPm)) {
        			log.warn("after 4pm, return");
            		return; 
            	}
            	
        		String endDateTime;  //yyyymmdd HH:mm:ss ttt, where "ttt" is the optional time zone.
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
         	    endDateTime = sdf.format(new Date(dataEngine.getCurrentServerTime()*1000));
        	    log.info("requesting data of {} till {}", contract.m_symbol, endDateTime);
       	    	dataEngine.reqHistoricalData(ReverseTop25Strategy.this, endDateTime, "2 D", "5 mins", "TRADES", 0, 2);  // rth set to 0.    
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
    		if (isTesting()) { 
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
		if (targetOrderId == orderId) {   // take profit
			currentPosition -= execution.m_cumQty;   		
			order.m_ocaGroup = null; 
			securityNoLeft++;   
			consecutiveLoss = 0;   // no consecutive loss
			targetOrderId = -1;
			stopOrderId = -1;
			mainOrderId = -1;
		} else if (stopOrderId == orderId) {  // stop loss
			// stopped out; should wait 5 minutes to re-enter. i.e. to reset securityNoLeft etc. 
			currentPosition -= execution.m_cumQty;
			order.m_ocaGroup = null; 
			placed = true; // has to use it to disable re-entry in 5 minutes. 
	        final Timer timer = new Timer();
	        timer.schedule(new TimerTask() {
	        	public void run() {
	    			placed = false; 
	    			log.info("set placed to false in timer");
	    			securityNoLeft++;   
	    			consecutiveLoss++;   // consecutive loss add 1 more time
	    			targetOrderId = -1;
	    			stopOrderId = -1;
	    			mainOrderId = -1;
	    			timer.cancel();
	        	}
	        }, 5*60*1000);  // reset after 5 mins.
			
		} else if (mainOrderId == orderId) {
			log.info("");
			String action; 
			double stopPrice;
			double targetPrice;
			int distance = 30; 
			if ("BOT".equals(execution.m_side)) {
				action = "SELL";
				stopPrice = execution.m_price - distance * getMiniTick();
				targetPrice = execution.m_price + distance * getMiniTick();
			} else {
				action = "BUY";
				stopPrice = execution.m_price + distance * getMiniTick();
				targetPrice = execution.m_price - distance * getMiniTick();
			}
			
	    	// 1st stop order, transmit still false; 
	    	order.m_action = action;
	    	order.m_ocaGroup = String.format("OCA%s", mainOrderId);
	    	order.m_totalQuantity = execution.m_shares;
	    	order.m_tif = "GTC";
	    	order.m_orderType = "STP";
	    	order.m_auxPrice = stopPrice;
	    	order.m_transmit = true;   // the idea is if one of child orders is transmitted, all would be so. 
	    	stopOrderId = orderEngine.placeOrder(this, contract, order);    	
	    	
	    	// 1st profit order
	    	order.m_ocaGroup = String.format("OCA%s", mainOrderId);
	    	order.m_orderType = "LMT";
	    	order.m_lmtPrice = targetPrice;
	    	targetOrderId = orderEngine.placeOrder(this, contract, order);    	// keep target order id

	    	// reduce 1 to init order finished. 
			currentPosition = execution.m_cumQty;   
			placed = false; 
			order.m_ocaGroup = null; 
			securityNoLeft--;   // suppose full execution.
		}
	}
	
	public boolean parse(String str) {
    	if ("R,".equals(str.substring(0, 2).toUpperCase())) {
    		str = str.substring(2);
    	} else {
    		return false; 
    	} 
    	return super.parse(str);
	}

}