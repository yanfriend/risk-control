package common;

import java.util.Hashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.*;

public class OrderEngine implements EWrapper
{
	private static Logger log = LoggerFactory.getLogger(OrderEngine.class);
	
    private Hashtable<Integer, Security> securityHash = new Hashtable<Integer, Security>();  
    private Hashtable<Integer, Strategy> strategyHash = new Hashtable<Integer, Strategy>();  
    
    private int nextSymbolID = 0;
    private EClientSocket client = null;
    
	private String host = "";
//	private int clientId; 
//    
//	public int getClientId() {
//		return clientId;
//	}
//
//	public void setClientId(int clientId) {
//		this.clientId = clientId;
//	}
//	
    public OrderEngine(int clientId)
    {
        client = new EClientSocket (this);
        client.eConnect (host, 7496, clientId);
        try {Thread.sleep (1000);} catch (Exception e) {};
    }
    

    /*
    public synchronized void placeOrder(Security security) {
    	placeOrder(0,security);
    }
    public synchronized void placeOrder(int id, Security security) {
        Order order = new Order();         
        order.m_action = security.getAction();
        order.m_lmtPrice = security.getLmtPrice(); 
        order.m_auxPrice = security.getStpPrice();
        order.m_totalQuantity = security.getOrderAmount();
        order.m_orderType = security.getOrderType();
        order.m_tif = security.getOrderTif();
        order.m_parentId = security.getParentId();
        order.m_outsideRth = security.isOutsideRth();
        order.m_transmit = security.isTransmit();
 
        Contract contract = new Contract(); 
        contract.m_symbol = security.getSymbol();
        contract.m_currency = "USD"; 
        contract.m_symbol = security.getSymbol();
        contract.m_exchange = security.getExchange();
        contract.m_secType = security.getSecurityType();
        contract.m_expiry = security.getExpireDate();
    	contract.m_strike = security.getStrikingPrice();
    	contract.m_right = security.getPutCall();
    	contract.m_multiplier = security.getMultiplier();        
    	
        // place order
    	int orderId;
    	if (id <= 0) {
    		orderId = getNextID();
    	} else {
    		orderId = id; 
    	}
         
        client.placeOrder( orderId, contract, order );
        security.setOrderId(orderId);
        securityHash.put(orderId, security);
        log.info("{} {} {} @ {}", new Object[]{security.getAction(), security.getOrderAmount(),
        		security.getSymbol(), security.getLmtPrice()});
    }
    */
    
    public synchronized int placeOrder(Strategy strategy, Contract contract, Order order) {
    	return placeOrder(0, strategy, contract, order);
    }
    
    public synchronized int placeOrder(int id, Strategy strategy, Contract contract, Order order) {   	
        // place order
    	int orderId;
    	if (id <= 0) {
    		orderId = getNextID();
    	} else {
    		orderId = id; 
    	}
        
        client.placeOrder( orderId, contract, order );
        strategyHash.put(orderId, strategy);
        log.info("{} {} {} @ {}/{}, order type:{}, transmited:{}", new Object[]{order.m_action, order.m_totalQuantity, 
        		contract.m_symbol, order.m_lmtPrice, order.m_auxPrice, order.m_orderType, order.m_transmit} );
        return orderId; 
    }
      
    public void execDetails (int orderId, Contract contract, Execution execution)
    {
    	String msg = EWrapperMsgGenerator.execDetails(orderId, contract, execution);
    	log.info(msg);
    	 
    	Strategy strategy = strategyHash.get(execution.m_orderId); 
    	if (strategy == null) {
    		log.error("error: executing order id is not logged: {},{}", execution.m_orderId, contract.m_symbol);
    		return; 
    	}
    	strategy.checkExecution(execution); 
    }

	public void cancleOrder(int orderId) {
		client.cancelOrder(orderId);
	}
    
    public void bondContractDetails (int reqId, ContractDetails contractDetails)
    {
    }

    public void contractDetails (int reqId, ContractDetails contractDetails)
    {
    }

    public void contractDetailsEnd (int reqId)
    {
    }

    public void fundamentalData (int reqId, String data)
    {
    }

    public void deltaNeutralValidation(int reqId, UnderComp underComp) {
    }

    public void bondContractDetails (ContractDetails contractDetails)
    {
    }

    public void contractDetails (ContractDetails contractDetails)
    {
    }

    public void currentTime (long time)
    {
    }

    public void execDetailsEnd(int reqId) {
    }

    public void historicalData (int reqId, String date, double open,
            double high, double low, double close, int volume, int count,
            double WAP, boolean hasGaps)
    {
    }

    public void managedAccounts (String accountsList)
    {
    }

    public void openOrder (int orderId, Contract contract, Order order,
            OrderState orderState)
    {
    }

    public void orderStatus (int orderId, String status, int filled,
            int remaining, double avgFillPrice, int permId, int parentId,
            double lastFillPrice, int clientId, String whyHeld)
    {
    }

    public void receiveFA (int faDataType, String xml)
    {
    }

    public void scannerData (int reqId, int rank,
            ContractDetails contractDetails, String distance, String benchmark,
            String projection, String legsStr)
    {
    }

    public void scannerDataEnd (int reqId)
    {
    }

    public void scannerParameters (String xml)
    {
    }

    public void tickEFP (int symbolId, int tickType, double basisPoints,
            String formattedBasisPoints, double impliedFuture, int holdDays,
            String futureExpiry, double dividendImpact, double dividendsToExpiry)
    {
    }

    public void tickGeneric (int symbolId, int tickType, double value)
    {
    }

    public void tickOptionComputation (int symbolId, int field,
            double impliedVol, double delta, double modelPrice,
            double pvDividend)
    {
    }

    public void tickPrice (int symbolId, int field, double price,
            int canAutoExecute)
    {
    }

    public void tickSize (int symbolId, int field, int size)
    {
    }

    public void tickString (int symbolId, int tickType, String value)
    {
    }

    public void updateAccountTime (String timeStamp)
    {
    }

    public void accountDownloadEnd(String accountName) 
    {
    }

    public void updateAccountValue (String key, String value, String currency,
            String accountName)
    {
    }

    public void updateMktDepth (int symbolId, int position, int operation,
            int side, double price, int size)
    {
    }

    public void updateMktDepthL2 (int symbolId, int position,
            String marketMaker, int operation, int side, double price, int size)
    {
    }

    public void updateNewsBulletin (int msgId, int msgType, String message,
            String origExchange)
    {
    }

    public void updatePortfolio (Contract contract, int position,
            double marketPrice, double marketValue, double averageCost,
            double unrealizedPNL, double realizedPNL, String accountName)
    {
    }

    public void connectionClosed ()
    {
    }

    public void tickSnapshotEnd(int reqId) {}
    public void openOrderEnd(){}

    public void error (Exception e)
    {
        e.printStackTrace ();
    }

    public void error (String str)
    {
        log.error(str);
    }

    public void error (int id, int errorCode, String errorMsg)
    {
        log.error("error (id, errorCode, errorMsg): id=" + id + ".  errorCode=" + errorCode + ".  errorMsg=" + errorMsg);
    }
    
    public void nextValidId (int orderId)
    {
        nextSymbolID = orderId;
    }

    public synchronized int getNextID ()
    {
        return (nextSymbolID++);
    }

    public void realtimeBar (int reqId, long time, double open, double high,
            double low, double close, long volume, double wap, int count)
    {
    }


}
