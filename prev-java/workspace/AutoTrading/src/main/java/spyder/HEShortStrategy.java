package spyder;

import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import common.Bar;
import common.TradeAlarm;

public class HEShortStrategy extends HEStrategy{
	private transient final Logger log = LoggerFactory.getLogger(HEShortStrategy.class);
	
	public HEShortStrategy()
	{
	   super();
	}
	   
	public void checkStrategy()
    {
    	Date now = new Date(getDataEngine().getCurrentServerTime()*1000);
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
        
        log.info("{} checking HEShort Strategy, may calculate indicators..", contract.m_symbol);
        Collection<Bar> barCollection = barMap.values();
        log.info("bar size:{}", barCollection.size());
        
        Iterator<Bar> iterator = barCollection.iterator();       
        Bar bar = null;       
        setTodayVolume(0);
        while (iterator.hasNext()) {          
        	bar = (Bar) iterator.next();          
        	long barSec = bar.getDate();
        	if (isTesting()) {  // log
        		Date datetmp = new Date(barSec * 1000);  // mili-second
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        		String dateStr = sdf.format(datetmp);
        		log.info("Bar data: {}, {}, close:{}, volume:{}", new Object[]{ contract.m_symbol, dateStr, bar.getClose(), bar.getVolume() } );
        	}
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
	    if (this.getCurrentPrice() > this.getYesterdayClose()) {  // current price set by real time bar
	       	log.info("current:{} > yesterday close:{}, return", getCurrentPrice(), getYesterdayClose());
	       	return; 
	    }
	    if (this.getTodayVolume()*100 < this.getDUVolume()) {	// not vol check for test as it used now time. 
	       	log.info("volume so far:{} < DU volume:{}, return", getTodayVolume(), getDUVolume());
	       	// * 100, seems ib unit
	       	return; 
	    }  
	    if (( getYesterdayClose()-getTodayOpen())/getYesterdayClose()>0.05 ) {
	       	log.info("today open:{} gap up 5% of yesterday close:{}, return", getTodayOpen(), getYesterdayClose());
	       	return; // open 5% gap of yesterday's close.
	    }
        
        log.info(contract.m_symbol + " today's present volume so far {}, exceeds dry up volume {}", this.getTodayVolume(), this.getDUVolume());
        
        // check MACD Histogram (5,13,6), and Stochastics (14,1,3), so make sure it has at least 18 bars. it only checks at the time range. 
        // checking Stochastics. 
        int barSize = 16; 
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
        if (outNBElement.value != 1) {
        	log.error("please re-calculate Bar size to pass to ta-lab");
        	return; 
        }
        if (outSlowK[0] > 25 ) {
        	log.warn("{} slowK does not meet.{}", contract.m_symbol, outSlowK[0]);
        	return; 
        }
        log.info("{} passing Stoch check...{}", contract.m_symbol, outSlowK[0]);        
        
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
        if (outMACDHist[outNBElement.value-1] >=0 ) {  // the value comes out 0.21, but other caculate out like 0.17, 
        	log.warn("{} macdHist does not meet.{}", contract.m_symbol, outMACDHist[outNBElement.value-1]);
        	return; 
        }
        log.info("{} passing MACD check...{}", contract.m_symbol,outMACDHist[outNBElement.value-1]);
        
        log.info("Placing orders: {}", contract.m_symbol);
       	if (!isTesting()) {
       		double currentPrice = getCurrentPrice();
       		order.m_action = "SELL"; 
       		order.m_totalQuantity = getOrderAmount();
       		order.m_orderType = "LMT";
       		order.m_tif = "GTC";
       		order.m_lmtPrice = currentPrice;   
       		order.m_transmit = false; 
       		mainOrderId = orderEngine.placeOrder(this, contract, order);

       		// place an order of -2% stop loss
       		order.m_action = "BUY";
       		order.m_orderType = "STP";
       		order.m_tif = "DAY";
       		order.m_auxPrice = ((int)(currentPrice*1.02/0.01))*0.01;  // adjust to mini tick
       		order.m_transmit = false;  // wait for the final transmit
       		ocaGroup = String.format("%d", mainOrderId);
       		order.m_ocaGroup = ocaGroup; 
       		order.m_parentId = mainOrderId;
       		orderEngine.placeOrder(this, contract, order); 
       		
       		// place an limit order of 10% target
       		order.m_action = "BUY";
       		order.m_orderType = "LMT";
       		order.m_tif = "GTC";
       		order.m_lmtPrice = Math.round((currentPrice*0.9)/0.01)*0.01;  // adjust to min tick.
       		order.m_transmit = true;   // if one of children is transmitted, all transmitted? 
       		order.m_parentId = mainOrderId;
       		order.m_ocaGroup = ocaGroup; 
       		orderEngine.placeOrder(this, contract, order); 
       		
       		order.m_action = "SELL"; 
       		order.m_totalQuantity = getOrderAmount();
       		order.m_orderType = "LMT";
       		order.m_tif = "GTC";
       		order.m_lmtPrice = currentPrice;   
       		order.m_transmit = true; 
       		orderEngine.placeOrder(mainOrderId, this, contract, order);
       	}
       	placed = true;
       	HEStrategy.securityNoLeft--; 
    }
    
    private void exitStrategy() {
    	if (!this.placed) { return; }
    	
    	// 1, %2 stop, same day
    	// 2, volume not reached, same day
    	// 3, price is lower than previous close. 
    	
    	// others, 4, 5% trailing stop; 
    	// 5, out after 4/6/or 8 days
    	// 6, high vol reached in first 15-30 minutes. i.e. by end of day, vol bigger than peak one. 
    	// 7, 10% profits
    	Toolkit toolkit;
    	toolkit = Toolkit.getDefaultToolkit();
        
    	if ((this.getCurrentPrice()-this.getLmtPrice())/this.getLmtPrice()<-0.02) {  // exit rule 1
    		log.warn("Exit: same day price 2% stop. current price:{}", this.getCurrentPrice());
    		toolkit.beep();
    		return; 
    	}
    	
    	long nowSec = dataEngine.getCurrentServerTime();
    	Date now = new Date(nowSec*1000);
    	long fifteen55Sec = new Date(now.getYear(),now.getMonth(),now.getDate(), 15,55,00).getTime()/1000;
    	long sixteen01Sec = new Date(now.getYear(),now.getMonth(),now.getDate(), 16,01,00).getTime()/1000;
    	if ((nowSec>=fifteen55Sec)&&(nowSec<=sixteen01Sec)) {
    		if (this.getTodayVolume()*100<this.getFRVVolume()) { // exit rule 2
       			log.warn("Exit: today volume not reached FRV volume: {}", this.getTodayVolume()); 
    			toolkit.beep();
        		return;    			
    		}
    		if (this.getTodayClose()>this.getYesterdayClose()) {  // exit rule 3
    			log.warn("Exit: today close HIGHER than yesterday close: {}", this.getTodayClose()); 
    			toolkit.beep();
    	        TradeAlarm.playSound("siren.wav"); 
    	        
    			// place order to sell
           		order.m_action = "BUY";
           		order.m_totalQuantity = executedShares;
           		order.m_orderType = "MKT";   // market order
           		order.m_tif = "DAY";
           		order.m_transmit = false;  // transmit
           		order.m_ocaGroup = ocaGroup;   // same group with -2% stop loss
           		orderEngine.placeOrder(this, contract, order); 
        		return; 
    		}
    	}
	}
    	    
	public boolean work(String str) {
    	if ("S,".equals(str.substring(0, 2).toUpperCase())) {
    		str = str.substring(2);
    	} else {
    		return false; 
    	} 
    	return super.work(str);
	}
}
