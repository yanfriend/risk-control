package spyder;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.tictactec.ta.lib.Core;
import common.DataEngine;
import common.OrderEngine;
import common.PriceTypeEnum;
import common.Security;
import common.Strategy;

public class OPTStrategy implements Strategy
{
	static private int totalOrderNo = 0; 
	
	private String triggerMode;  // <= or >=
	private float triggerPrice;  // underlying stock price
	private String priceMode;    // "MIDPOINT"
	
	private OrderEngine orderEngine; 
	private DataEngine dataEngine; 
	
	private double currentPrice; 
	private int orderAmount;   
	private String symbol;
	private String action; 
	private String expireDate;
	private String putCall;
	private float strikingPrice;
	
	private Contract contract = new Contract(); 
	// private Contract contractStock = new Contract(); 
	private Order order = new Order();   // keep the main order info
	
	private Security security = new Security();
	private boolean placed = false;
	
	private boolean testing;

	private transient final Logger log = LoggerFactory.getLogger(OPTStrategy.class);
    Core taLib = new Core();   // may change to spring DI. 

    public OPTStrategy()
    {
        security.setSymbol(this.getSymbol());
        security.setSecurityType("STK");
        security.setCurrency("USD");
        security.setMultiplier("100");
               
        contract.m_symbol = getSymbol();
        contract.m_exchange = "SMART";
    	contract.m_multiplier = "100";
    	contract.m_secType = "OPT";
    	contract.m_currency = "USD";
    	order.m_tif = "GTC";
    }
    
    public void checkStrategy()
    {
    	if ((this.placed)||(OPTStrategy.totalOrderNo>=2)) { 
    		exitStrategy(); 
    		return; 
    	}    	
    	if (getCurrentPrice()<=0) return;
    	
    	double stockPrice = security.getCurrentPrice(); 
    	boolean triggered = false; 
    	if (this.triggerMode.equals("<=")) {
    		if (stockPrice<=this.triggerPrice) triggered=true;
    	}if (this.triggerMode.equals(">=")) {
    		if (stockPrice>=this.triggerPrice) triggered=true;
    	}
        
        if (!triggered) return; 
       
        
        double tmpPrice = getCurrentPrice();
        if ("BUY".equals(order.m_action)) {
        	tmpPrice += 0.005;  // for round
        }
        DecimalFormat df = new DecimalFormat("000.##"); 
 
        log.info("Placing option orders: {}", getSymbol());
        order.m_action = order.m_action; 
        order.m_totalQuantity = orderAmount;
        order.m_orderType = "LMT";
        order.m_tif = "GTC";
        order.m_lmtPrice = Double.parseDouble( df.format(tmpPrice) );

       	orderEngine.placeOrder(this, contract, order);
       	placed = true;
       	OPTStrategy.totalOrderNo++; 
    }
    
    private void exitStrategy() {
    	if (!this.placed) { return; }
	}
    
	public void monitor() {
		// option real time bar for this opt. 
//    	dataEngine.reqRealtimeBar(this, this.getPriceMode(), true);
    	
    	// the underlying stock real time bar.
    	security.setSymbol(this.getSymbol());
    	security.setSecurityType("STK");
    	security.setMultiplier("");
    	dataEngine.reqRealtimeBar(security, PriceTypeEnum.TRADES.toString(), true); 
    	
    	checkStrategy(); // check initially, otherwise, it waits period time.
        int period = 30*1000;  // repeat every 30 seconds.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		if (currentPrice<=0) {
        			dataEngine.reqRealtimeBar(OPTStrategy.this, OPTStrategy.this.getPriceMode(), true);
        		}
        		checkStrategy(); 
        	}
        }, 0, period);
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
	public void setPriceMode(String priceMode) {
		this.priceMode = priceMode;
	}
	public String getPriceMode() {
		return priceMode;
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
		this.security.setSymbol(symbol);
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

	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
		this.contract.m_expiry = expireDate; 
		this.security.setExpireDate(expireDate);
	}

	public String getExpireDate() {
		return expireDate;
	}

	public void setPutCall(String putCall) {
		this.putCall = putCall;
		this.contract.m_right = putCall; 
		this.security.setPutCall(putCall);
	}

	public String getPutCall() {
		return putCall;
	}

	public void setStrikingPrice(float strikingPrice) {
		this.strikingPrice = strikingPrice;
		this.contract.m_strike = strikingPrice; 
		this.security.setStrikingPrice(strikingPrice);
	}

	public float getStrikingPrice() {
		return strikingPrice;
	}
	
	public boolean parse(String str) {
	   	String[] strArr = str.split(",");
    	try {
    		String symbol = strArr[0].trim();
    		String compareMode = strArr[1].trim();
    		float stockPrice = Float.parseFloat(strArr[2].trim());
    		String buySell = strArr[3].trim();
    		String expireDate = strArr[4].trim();
    		float strikingPrice = Float.parseFloat(strArr[5].trim());
    		String putCall = strArr[6].trim();
    		String priceMode = strArr[7].trim();
     		int orderAmount = Integer.parseInt(strArr[8].trim());
     		
	    	this.setSymbol(symbol);
	    	this.setTriggerMode(compareMode);
	    	this.setExpireDate(expireDate);
	    	this.setTriggerPrice(stockPrice);
	    	this.setAction(buySell);
	    	this.setStrikingPrice(strikingPrice);
	    	this.setPutCall(putCall);
	    	this.setPriceMode(priceMode);
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

}