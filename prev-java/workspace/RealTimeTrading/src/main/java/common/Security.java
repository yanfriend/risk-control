package common;
/*
 * this class includes common contract and order info 
 * new changes: only have contract info and to accept realtime data and historical data. 
 */
public class Security {
	// contract info
	private String symbol;
	private String securityType;
	private String expireDate;
	private String exchange; 
	private String putCall;
	private float strikingPrice;
	private String multiplier; 
	private double currentPrice; 
	private String currency; 
	
//	// order info
//	private int parentId; 
//	private int orderId; 
//	private int stopOrderId;
//	private String action;  // buy sell
//	private String orderType; 
//	private double lmtPrice; 
//	private double stpPrice; 
//	private int orderAmount;   
//	private String orderTif="DAY"; 
//	private boolean outsideRth;
//	private boolean transmit;
//
//	private double todayOpen;
//	private double todayClose;
//	private long todayVolume; // today's volume, unit is hand, *100 for shares!!!
//	
//	private double bidPrice;  // from tickPrice(), market data
//	private double askPrice;
	
	// execution info 
	// ...

	public void concatenateHistData(String date, double open,double high,double low,double close,int volume) {
	}
    public void monitor() {
	}
 
    
    public Security() {
    	//orderAmount = 100;
    	//lmtPrice = 1.0;
    	//action = "BUY";
    	this.setExchange("SMART"); 

    }
    
    public Security(Security security) {
    	// contract info
    	symbol = security.getSymbol();
    	securityType = security.getSecurityType();
    	expireDate = security.getExpireDate();
    	exchange = security.getExchange(); 
        putCall = security.getPutCall();
    	strikingPrice = security.getStrikingPrice();
    	multiplier = security.getMultiplier(); 
    	
    	// order info
//    	action = security.getAction();  // buy sell
//    	parentId = security.getParentId(); 
//    	orderId = security.getOrderId(); 
//    	orderType = security.getOrderType(); 
//    	currentPrice = security.getCurrentPrice(); 
//    	lmtPrice = security.getLmtPrice(); 
//    	stpPrice = security.getStpPrice(); 
//    	orderAmount = security.getOrderAmount();   
//    	orderTif = security.getOrderTif(); 
//    	stopOrderId = security.getStopOrderId();
//    	outsideRth = security.isOutsideRth();
//    	transmit = security.isTransmit();
//
//    	todayOpen = security.getTodayOpen();
//    	todayClose = security.getTodayClose();
//    	todayVolume = security.getTodayVolume(); // today's volume, unit is hand, *100 for shares!!!
//    	
//    	bidPrice = security.getBidPrice();  // from tickPrice(), market data
//    	askPrice = security.getAskPrice();
    	
    	// execution info 
    	// ...
    	
    	
    }
    
    
	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}
	public String getSecurityType() {
		return securityType;
	}
	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}
	public String getExpireDate() {
		return expireDate;
	}
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}
	public String getExchange() {
		return exchange;
	}
	public void setPutCall(String putCall) {
		this.putCall = putCall;
	}
	public String getPutCall() {
		return putCall;
	}
	public void setStrikingPrice(float strikingPrice) {
		this.strikingPrice = strikingPrice;
	}
	public float getStrikingPrice() {
		return strikingPrice;
	}
	public void setMultiplier(String multiplier) {
		this.multiplier = multiplier;
	}
	public String getMultiplier() {
		return multiplier;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	
/*
	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getOrderType() {
		return orderType;
	}


	public void setLmtPrice(double lmtPrice) {
		this.lmtPrice = lmtPrice;
	}

	public double getLmtPrice() {
		return lmtPrice;
	}

	public void setOrderAmount(int orderAmount) {
		this.orderAmount = orderAmount;
	}

	public int getOrderAmount() {
		return orderAmount;
	}

	public void setTodayVolume(long todayVolume) {
		this.todayVolume = todayVolume;
	}

	public long getTodayVolume() {
		return todayVolume;
	}

	public void setBidPrice(double bidPrice) {
		this.bidPrice = bidPrice;
	}

	public double getBidPrice() {
		return bidPrice;
	}

	public void setAskPrice(double askPrice) {
		this.askPrice = askPrice;
	}

	public double getAskPrice() {
		return askPrice;
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

	public String getOrderTif() {
		return orderTif;
	}
	public void setOrderTif(String orderTif) {
		this.orderTif = orderTif;
	}
	public void setStopOrderId(int stopOrderId) {
		this.stopOrderId = stopOrderId;
	}
	public int getStopOrderId() {
		return stopOrderId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public int getParentId() {
		return parentId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setStpPrice(double stpPrice) {
		this.stpPrice = stpPrice;
	}
	public double getStpPrice() {
		return stpPrice;
	}

	public void setOutsideRth(boolean outsideRth) {
		this.outsideRth = outsideRth;
	}
	public boolean isOutsideRth() {
		return outsideRth;
	}
	public void setTransmit(boolean transmit) {
		this.transmit = transmit;
	}
	public boolean isTransmit() {
		return transmit;
	}

	*/
}
