package spyder;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.tictactec.ta.lib.Core;

import common.TradeAlarm;
import common.DataEngine;
import common.OrderEngine;
import common.PriceTypeEnum;
import common.Strategy;

public class StopOrderEquityStrategy implements Strategy{

	static private int securityNoLeft; 
	
	private String triggerMode;  // <= or >=
	private float triggerPrice;  // underlying stock price
//	private String priceMode;    // "MIDPOINT"
	
	private OrderEngine orderEngine; 
	private DataEngine dataEngine; 
	
	private double currentPrice; 
	private int orderAmount;   
	private String symbol;
	private String action; 
//	private String expireDate;
//	private String putCall;
//	private float strikingPrice;
	
	private Contract contract = new Contract(); 
	// private Contract contractStock = new Contract(); 
	private Order order = new Order();   // keep the main order info
	
//	private Security security = new Security();
	private boolean placed = false;
	
	private boolean testing;

	private transient final Logger log = LoggerFactory.getLogger(OPTStrategy.class);
    Core taLib = new Core();   // may change to spring DI. 

    public StopOrderEquityStrategy()
    {            
        contract.m_symbol = getSymbol();
        contract.m_exchange = "SMART";
    	contract.m_secType = "STK";
    	contract.m_currency = "USD";
    	order.m_tif = "GTC";
    }
    
    public void checkStrategy()
    {
    	if ((this.placed)||(securityNoLeft<=2)) { 
    		exitStrategy(); 
    		return; 
    	}
    	
    	if (currentPrice<=0) return;
    	
    	boolean triggered = false; 
    	if (triggerMode.equals("<=")) {
    		if (currentPrice<=triggerPrice) triggered=true;
    	}if (this.triggerMode.equals(">=")) {
    		if (currentPrice>=triggerPrice) triggered=true;
    	}
        
        if (!triggered) return; 
  
        log.info("Placing stop orders: {}", getSymbol());
        order.m_action = action; 
        order.m_totalQuantity = orderAmount;
        order.m_orderType = "MKT";
        order.m_tif = "GTC";
        order.m_transmit = false;   // stop order(market order) for testing. 

        TradeAlarm.playSound("siren.wav"); 
    	Toolkit toolkit = Toolkit.getDefaultToolkit();
    	toolkit.beep(); 
        
       	orderEngine.placeOrder(this, contract, order);
       	placed = true;
       	securityNoLeft--; 
    }
    
    public static int getSecurityNoLeft() {
		return securityNoLeft;
	}

	public void setSecurityNoLeft(int securityNoLeft) {
		this.securityNoLeft = securityNoLeft;
	}

	private void exitStrategy() {
    	if (!this.placed) { return; }
	}
    
	public void monitor() {
		
        int period = 30*1000;  // repeat every 30 seconds.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		if (currentPrice<=0) {
        			dataEngine.reqRealtimeBar(StopOrderEquityStrategy.this, PriceTypeEnum.TRADES.toString(), true);
        		}
        		checkStrategy(); 
        	}
        }, 0, period);
        try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.warn("{}",e);
		}
        checkStrategy(); // init check
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
 	public void setTriggerPrice(float triggerPrice) {
		this.triggerPrice = triggerPrice;
	}
	public float getTriggerPrice() {
		return triggerPrice;
	}
	public void setTriggerMode(String triggerMode) {
		this.triggerMode = triggerMode;
	}
	public String getTriggerMode() {
		return triggerMode;
	}
	public double getCurrentPrice() {
		return currentPrice;
	}	
	
	public void setOrderAmount(int orderAmount) {
		this.orderAmount = orderAmount;
	}

	public int getOrderAmount() {
		return orderAmount;
	}
	
	public Contract getContract() {
		return contract;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
		this.contract.m_symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setAction(String action) {
		this.action = action;
		order.m_action = action;
	}

	public String getAction() {
		return action;
	}
	
	public boolean parse(String str) {
	   	String[] strArr = str.split(",");
    	try {
    		String symbol = strArr[0].trim();
    		String compareMode = strArr[1].trim();
    		float stockPrice = Float.parseFloat(strArr[2].trim());
    		String buySell = strArr[3].trim();
     		int orderAmount = Integer.parseInt(strArr[4].trim());
     		
	    	this.setSymbol(symbol);
	    	this.setTriggerMode(compareMode);
	    	this.setTriggerPrice(stockPrice);
	    	this.setAction(buySell);
	    	this.setOrderAmount(orderAmount);
	    	
	    	return true; 
	    } catch (Exception e){
	    	log.error("error in opt stock file line: {}\n{}", str,e);
	    	return false; 
	    }
	}
	
	
	public void checkHistoricalData(String date, double open, double high,
			double low, double close, int volume) {
		// TODO Auto-generated method stub
		
	}

	public void setTargetReached(boolean targetReached) {
		// TODO Auto-generated method stub
		
	}

	public void checkExecution(Execution execution) {
		// TODO Auto-generated method stub
		
	}
			
	// 1, %2 stop, same day
	// 2, volume not reached, same day
	// 3, price is lower than previous close. 
	
	// others, 4, 5% trailing stop; 
	// 5, out after 4/6/or 8 days
	// 6, high vol reached in first 15-30 minutes. i.e. by end of day, vol bigger than peak one. 
	// 7, 10% profits

}
