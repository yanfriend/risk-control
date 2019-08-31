package old.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;

import common.DataEngine;
import common.OrderEngine;
import common.PriceTypeEnum;
import common.Strategy;
import futures.Top25OneStrategy;

public class PriceMonitorStrategy implements Strategy
{
	OrderEngine orderEngine; 
	DataEngine dataEngine; 
	
	double currentPrice; 
	String symbol, expireDate, exchange;
	int orderAmount;
	double miniTick; 
	double costPrice; 
	double open, high, low, close; 
	int stop, target1, target2; 
			
	boolean hasContract; 
	
	enum STATE {Monitor, StopReached, Target1Placed, Target2Executed} 
	STATE state;
	
	
	Contract contract = new Contract(); 
	
	private transient final Logger log = LoggerFactory.getLogger(Strategy.class);
	private Object targetOrderId;

    public PriceMonitorStrategy()
    {
    }
    
    public void checkStrategy(String key, String value)
    {  // called by data engine, from account request, for available fund only
    	if ("AvailableFunds".equals(key)) {
    		double availFund = Double.parseDouble(value); 
    		log.info("-------------------account available fund:"+availFund);
    	} 
    }
    
	public void monitor() {
		dataEngine.reqAccountUpdates(this);
		
        int period = 30*1000;  // repeat every 30 seconds.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	public void run() {
        		if (!hasContract) return; 
        		if (currentPrice<=0) {
        			dataEngine.reqRealtimeBar(PriceMonitorStrategy.this, PriceTypeEnum.TRADES.toString(), false);  // this only set current price, not accurate. 
        		}
        		// check to see if target1, target2, or stop reached. 
        		checkPrice(); 
        	}
        }, 0, period);
    }
   
	public DataEngine getDataEngine() {
		return dataEngine;
	}
	public void setDataEngine(DataEngine dataEngine) {
		this.dataEngine = dataEngine;
	}

	public void checkContract(Contract contract, int position) { 
		// called by date engine from account request. 
   		log.info("-------------------symbol:"+ contract.m_symbol+", "+position);
	    if (hasContract) return;  // has this contract. 
	    
	    if ((this.symbol.equals(contract.m_symbol)) && (this.expireDate.equals(contract.m_expiry)) && (Math.abs(this.orderAmount) <= Math.abs(position)) && (position*orderAmount>0) ) {
	    // same symbol, same expire date, same long/short position, and account position >= monitor position. 	
	    	hasContract = true; 
	    	this.contract = contract; 
	    }
	}

	public boolean work(String str) {
		// set confSymbol, confPos
    	String[] strArr = str.split(",");
    	try {
    		String symbol = strArr[0].trim();
      		String expireDate = strArr[1].trim();
      		String exchange = strArr[2].trim();
      		int orderAmount = Integer.parseInt(strArr[3].trim());
      		double miniTick = Double.parseDouble(strArr[4].trim());
      		// stop distance, target1 distance, target2 distance. 
      		double costPrice = Double.parseDouble(strArr[5].trim());
      		int stop = Integer.parseInt(strArr[6].trim()); 
      		int target1 = Integer.parseInt(strArr[7].trim()); 
      		int target2 = Integer.parseInt(strArr[8].trim()); 
      		
      		this.symbol = symbol;
      		this.expireDate = expireDate;
      		this.exchange = exchange;
      		this.orderAmount = orderAmount;
      		this.miniTick = miniTick;
      		this.costPrice = costPrice;
      		this.stop = stop;
      		this.target1 = target1; 
      		this.target2 = target2; 
      		return true;
	    } catch (Exception e){
	    	log.error("error in futures file line: {}\n{}", str,e);
	    	return false; 
	    }
	}
		
	private void checkPrice() {
		// there is no cancel/modify fees for futures, but is for stocks and options. 
		
		// if long, stop reached, target 1 reached, target 2 reached, place order accordingly. 
		if (!hasContract) return; 
		if (currentPrice<=0) return; 
		
		int longSide = 0; 
		if (orderAmount>0) longSide = 1; else if (orderAmount<0) longSide = -1; else return; 
		
		switch (state) {
		case Monitor: 
			switch (longSide)  {
			case 1: 
				if (currentPrice>costPrice+target1*miniTick) { // reach target 1. 
					Order order = new Order(); 
			    	// order.m_parentId = orderId1;
			    	order.m_action = "SELL"; 
			    	order.m_totalQuantity = orderAmount;
			    	order.m_orderType = "STP";
			    	order.m_auxPrice = costPrice + 1;
			    	order.m_tif = "GTC";
			    	//order.m_lmtPrice = targetPrice;
			    	//order.m_ocaGroup = String.format("OCA%s", orderId1);
			    	order.m_transmit = false;  // to test
			    	targetOrderId = orderEngine.placeOrder(this, contract, order);    	// keep target order id
			       	state = STATE.Target1Placed;
				}
				break;
			case -1: 
				break;
			}
			
			break; // of monitor
		case Target1Placed: 
			break; 
		}
		
		
		
	}

	
	
	public void setCurrentPrice(double open, double high, double low, double close) {
		this.currentPrice = close;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close; 
	}
	
//  below is default ones. 
	public void checkHistoricalData(String date, double open, double high,
			double low, double close, int volume) {		
	}

	public void checkExecution(Execution execution) {
	}

	public Contract getContract() {
		// TODO Auto-generated method stub
		return null;
	}
}