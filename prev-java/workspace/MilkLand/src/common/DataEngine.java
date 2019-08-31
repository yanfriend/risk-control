package common;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.*;

public class DataEngine implements EWrapper
{
	final static Logger log = LoggerFactory.getLogger(DataEngine.class);
	
	volatile private long currentServerTime;  // in seconds from 1970/01/01
	
    private Hashtable<Integer, Security> HistSecurityHash = new Hashtable<Integer, Security>(); 
    private Hashtable<Integer, Security> RealSecurityHash = new Hashtable<Integer, Security>(); 
    
    private Hashtable<Integer, Strategy> HistStrategyHash = new Hashtable<Integer, Strategy>(); 
    private Hashtable<Integer, Strategy> RealStrategyHash = new Hashtable<Integer, Strategy>(); 
    
    private static int nextSymbolID = 0;
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

	public DataEngine (int clientId)
	{
		client = new EClientSocket(this);
		client.eConnect (host, 7496, clientId);  // client.eConnect ("", 7496, 1);
		try {Thread.sleep (1000);} catch (Exception e) {};
		// set timer to request current server time. 
        Timer timer = new Timer();
        long period = 60*1000;  // 60 seconds
        int delay = 1000; 
        timer.scheduleAtFixedRate(new TimerTask() { 
        	public void run() {
        		client.reqCurrentTime(); 
        	}
        }, delay, period);  // request per 60 seconds. 
        // add per seconds
        Timer secTimer = new Timer(); 
        secTimer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		synchronized(this) {
        			currentServerTime = currentServerTime + 1;
        			// log.info("current timer(in timer): {}", currentServerTime);
        		}
        	}
        }, 0, 1000);
	}

	public DataEngine() {
		// TODO Auto-generated constructor stub
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
    	this.currentServerTime = time; 
		log.trace("current timer(in return): {}", currentServerTime);
    }

    public void execDetails (int orderId, Contract contract, Execution execution)
    {
    	String msg = EWrapperMsgGenerator.execDetails(orderId, contract, execution);
    	log.info(msg);
    }

    public void execDetailsEnd(int reqId) {
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

    public void accountDownloadEnd(String accountName) {
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
        System.err.println (str);
    }

    public void error (int id, int errorCode, String errorMsg)
    {
        System.err.println ("error (id, errorCode, errorMsg): id=" + id + ".  errorCode=" + errorCode + ".  errorMsg=" + errorMsg);
    }
    
    public void nextValidId (int orderId)
    {
        nextSymbolID = orderId;
    }

    public synchronized int getNextID ()
    {
        return (nextSymbolID++);
    }
    
    public void historicalData (int reqId, String date, double open,
            double high, double low, double close, int volume, int count,
            double WAP, boolean hasGaps)
    {   	
    	// do the process
    	Security security = HistSecurityHash.get(reqId);
        if (security != null) {
        	security.concatenateHistData(date, open, high, low, close, volume); 
        }
        
        Strategy strategy = HistStrategyHash.get(reqId);
        if (strategy != null) {
        	strategy.checkHistoricalData(date, open, high, low, close, volume); 
        }
    }
    
    public void realtimeBar(int reqId, long time, double open, double high,
            double low, double close, long volume, double wap, int count)
    {
        try {
        	Security security = RealSecurityHash.get(reqId);
            if (security != null) {
            	security.setCurrentPrice(close);
            	log.info("{}, {}, last price={}, volume={}", 
            			new Object[]{String.valueOf(reqId),security.getSymbol(),security.getCurrentPrice(),volume});            	
            }
        } catch (Exception e) {
            log.error("realtimebar process exception: {}",e);
        }
        
        try {
        	Strategy strategy = RealStrategyHash.get(reqId);
            if (strategy != null) {
            	strategy.setCurrentPrice(close);
            	log.info("{}, {}, last price={}, volume={}", 
            			new Object[]{reqId, strategy.getContract().m_symbol, close, volume});            	
            }
        } catch (Exception e) {
            log.error("strategy realtimebar process exception: {}",e);
        }
    }
   
    public synchronized void reqHistoricalData(Security security, String endDateTime, String durationStr, String barSizeSetting, 
    		String whatToShow, int useRTH, int formatDate) 
    {
        Contract contract = new Contract ();
        contract.m_currency = "USD"; 
        contract.m_symbol = security.getSymbol();
        contract.m_exchange = security.getExchange();
        contract.m_secType = security.getSecurityType();
        contract.m_expiry = security.getExpireDate();
    	contract.m_strike = security.getStrikingPrice();
    	contract.m_right = security.getPutCall();
    	contract.m_multiplier = security.getMultiplier();        
               
        // before inserting new request id into securityHash, remove the old one; otherwise Hashtable becomes too large.
        Collection<Security> securityColl = HistSecurityHash.values();
        securityColl.remove(security);
        int requestId = getNextID();   
        HistSecurityHash.put(requestId, security);
        log.info("Requesting historical data: " + requestId + ", " + security.getSymbol());
        client.reqHistoricalData(requestId, contract, endDateTime, durationStr, barSizeSetting, whatToShow, useRTH, formatDate);
        // end of real trading. 
        
    } 
    
    public synchronized void reqHistoricalData(Strategy strategy, String endDateTime, String durationStr, String barSizeSetting, 
    		String whatToShow, int useRTH, int formatDate) 
    {               
        // before inserting new request id into securityHash, remove the old one; otherwise Hashtable becomes too large.
        Collection<Strategy> strategyColl = HistStrategyHash.values();
        strategyColl.remove(strategy);
        int requestId = getNextID();   
        HistStrategyHash.put(requestId, strategy);
        log.info("Requesting historical data:{}, {}", requestId, strategy.getContract().m_symbol);
        client.reqHistoricalData(requestId, strategy.getContract(), endDateTime, durationStr, barSizeSetting, whatToShow, useRTH, formatDate);
        // end of real trading. 
    } 
    
    public synchronized void reqRealtimeBar(Strategy strategy, String priceType, boolean useRTH) 
    {
        int requestId = getNextID();   
        RealStrategyHash.put(requestId, strategy);
        client.reqRealTimeBars(requestId, strategy.getContract(), 5, priceType, useRTH);  // 5 sec, mandatory, /BID ASK MIDPOINT 
        log.info("Requesting realtime id:{}, symbol:{}", requestId, strategy.getContract().m_symbol); 
    } 
    
    public synchronized void reqRealtimeBar(Security security, String priceType, boolean useRTH) 
    {
        Contract contract = new Contract ();
        contract.m_currency = "USD"; 
        contract.m_symbol = security.getSymbol();
        contract.m_exchange = security.getExchange();
        contract.m_secType = security.getSecurityType();
        contract.m_expiry = security.getExpireDate();
    	contract.m_strike = security.getStrikingPrice();
    	contract.m_right = security.getPutCall();
    	contract.m_multiplier = security.getMultiplier();        
    	      
        int requestId = getNextID();   
        RealSecurityHash.put(requestId, security);
        client.reqRealTimeBars(requestId, contract, 5, priceType, useRTH);  // 5 sec, mandatory, /BID ASK MIDPOINT 
        log.info("Requesting realtime bar: " + requestId + ", " + security.getSymbol()); 
    } 
    
	public void setCurrentServerTime(long currentServerTime) {
		this.currentServerTime = currentServerTime;
	}

	public long getCurrentServerTime() {
		return currentServerTime;
	}
  
}
