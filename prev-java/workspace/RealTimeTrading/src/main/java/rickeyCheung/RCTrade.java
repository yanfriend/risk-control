package rickeyCheung;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RCTrade {
	enum TimeRange {nil, StartTime, MidTime, EndTime};
	enum STATE {nil, ps_ns, ns_ps, ns_ns, ps_ps}
	
	STATE state = STATE.nil;
	double lastESClose, lastNQClose; 
	
	float stopLoss = 10; 
	double ESLevel;
	double NDQLevel;
	boolean priority = false; 
	boolean wait2a, wait2b, wait3a, wait3b, wait4a, wait5a, wait5b, wait6a, wait7a, wait7b, wait8a, wait8b, NTrade3; 
    int marketPosition;
	double entryPrice;
	double stopPrice;
	
	double NDQ; 
	
	private transient final Logger log = LoggerFactory.getLogger(RCTrade.class);
	
	public void preset(double ESyClose, double NQyClose) {
		lastESClose = ESyClose;
		lastNQClose = NQyClose;		
		// log.info("last close:"+ESyClose+ ", nq:"+ NQyClose);
	}

	void trading(long time, double ESLast, double NQLast) {
		// set buy/sell order, set stop order, adjust stop oder to breakeven, close eod at 16:00
		ESLevel = ESLast-lastESClose;
		NDQLevel = NQLast-lastNQClose;
		log.info( new Date(time*1000)+", ES="+ESLevel+", nq="+NDQLevel ); 
		
		if ( checkTimeRange(time)==TimeRange.StartTime ) {
			if ((ESLevel>=0)&&(ESLevel<10)&&(NDQLevel<0)&&(NDQLevel>-10)) state=STATE.ps_ns; 
			if ((ESLevel<0)&&(ESLevel>-10)&&(NDQLevel>=0)&&(NDQLevel<10)) state=STATE.ns_ps; 
			if ((ESLevel<0)&&(ESLevel>-10)&&(NDQLevel<0)&&(NDQLevel>-10)) state=STATE.ns_ns; 
			if ((ESLevel>=0)&&(ESLevel<10)&&(NDQLevel>=0)&&(NDQLevel<10)) state=STATE.ps_ps; 
			
			if (!priority) {
				if ((state==STATE.ns_ps)&&(Math.abs(NDQLevel)>=Math.abs(ESLevel))) {
					placeOrder("short 1a at {}", ESLast, -1);
					priority = true; 
				}
				if ((state==STATE.ns_ps)&&(Math.abs(ESLevel)>Math.abs(NDQLevel))) {
					placeOrder("short 1b at {}", ESLast, -1);
					priority = true; 
				}
				if ((state==STATE.ps_ns)&&(Math.abs(NDQLevel)>=Math.abs(ESLevel))) {
					NDQ = NDQLevel;
					wait2a = true; 
					priority = true; 
				}
				if ((state==STATE.ps_ns)&&(Math.abs(ESLevel)>Math.abs(NDQLevel))) {
					NDQ = NDQLevel;
					wait2b = true; 
					priority = true; 
				}
				if ((state==STATE.ns_ns)&&((ESLevel)>=(NDQLevel))) {
					NDQ = NDQLevel;
					wait3a = true; 
					priority = true; 
				}
				if ((state==STATE.ns_ns)&&((ESLevel)<(NDQLevel))) {
					wait3b = true; 
					priority = true; 
				}
				if ((state==STATE.ps_ps)&&((ESLevel)<=(NDQLevel))) {
					wait4a = true; 
					priority = true; 
					NDQ = NDQLevel;
				}				
				if ((state==STATE.ps_ps)&&((ESLevel)>(NDQLevel))) {
					placeOrder("short 4b at {}", ESLast, -1);
					priority = true; 
				}	// 8 situations above
				
				if ((ESLevel<0)&&(ESLevel>-10)&&(NDQLevel<=-10)&&(NDQLevel>=-25)) {
					NDQ = NDQLevel;
					wait5a = true;
					priority = true; 
				}
				if ((ESLevel>=0)&&(ESLevel<10)&&(NDQLevel>=10)&&(NDQLevel<=25)) {
					NDQ = NDQLevel;
					wait6a = true;
					priority = true; 
				}
				if ((ESLevel>=10)&&(NDQLevel>=10)) {
					NDQ = NDQLevel;
					wait7a = true;
					priority = true; 
				}
				if ((ESLevel<=-10)&&(NDQLevel<=-10)) {
					NDQ = NDQLevel;
					wait7b = true;
					priority = true; 
				}
				// above 4 situations
			}  // end of !priority 
		} else if (checkTimeRange(time)==TimeRange.MidTime)  { 
			if (wait2a&&(NDQLevel>=13)) { placeOrder("buy 2a at {}", ESLast,1); wait2a=false; priority=true; }
			if (wait2b&&(NDQLevel>=13)) { placeOrder("buy 2b at {}", ESLast,1); wait2b=false; priority=true; }			
			if (wait3a&&(NDQLevel<=-20)) { placeOrder("short 3a at {}", ESLast, -1); wait3a=false; priority=true; }
			if (wait3b&&(NDQLevel<=-8)) {placeOrder("short 3b at {}", ESLast,-1); wait3b=false; priority=true; }
			if (wait4a) { // check NDQ above
				if (NDQLevel-NDQ>=10) {
					placeOrder("buy 4a at {}", ESLast,1); 
					wait4a=false; 
					priority=true; 
				}
				if (NDQLevel<=-13) {  // does this condition correct? only use NDQLevel? 
					placeOrder("short 4a at {}", ESLast, -1); 
					wait4a=false; 
					priority=true; 
				}
			}   
			if (wait5a&&(NDQ-NDQLevel>=10)) { placeOrder("short 5a at {}", ESLast ,-1); wait5a=false; priority=true; }
			if (wait6a) { // check NDQ above
				if (NDQLevel-NDQ>=12) {
					placeOrder("buy 6a at {}", ESLast,1); 
					wait6a=false; 
					priority=true; 
				}
				if (NDQ-NDQLevel>=10) {
					placeOrder("short 6a at {}", ESLast,-1); 
					wait6a=false; 
					priority=true; 
				}
			}
			if ((wait7a)&&(NDQ-NDQLevel>=10)) { placeOrder("short 7a at {}", ESLast, -1); wait7a=false; priority=true; }
			if ((wait7b)&&(NDQLevel-NDQ>=10)) { placeOrder("long 7b at {}", ESLast, 1); wait7b=false; priority=true; }
		}
		
		// stop loss adjustment
		if ((marketPosition==1) && (ESLast-entryPrice>=4)) {
			stopPrice = entryPrice + 0.75;
		}
		if ((marketPosition==-1) && (entryPrice-ESLast>=4)) {
			stopPrice = entryPrice - 0.75;
		}
		
		// eod close position
		checkStop(ESLast); 
		
		if (checkTimeRange(time)==TimeRange.EndTime)  { 
			closeOrder("close position at: {}", ESLast);
		}
	}
	
	private void placeOrder(String disp, double price, int position) {
	// place order and log	
		log.info(disp, price);
		marketPosition = position; 
		entryPrice = price; 
		if (position == 1) {
			stopPrice = price - 10; 
		} else if (position == -1) stopPrice = price + 10; 
	}
	
	private void checkStop(double currentPrice) {
	// check if stop triggered. 
		if (marketPosition==1) {
			if (currentPrice <= stopPrice) closeOrder("stop out long at: {}", currentPrice); 
		} else if (marketPosition==-1) {
			if (currentPrice >= stopPrice) closeOrder("stop out short at: {}", currentPrice);
		}
	}
	
	private void closeOrder(String string, double currentPrice) {
		if (marketPosition != 0) log.info(string, currentPrice);
		marketPosition = 0; 
	}

	public void cleanup() {
	// set all variable back to default values. 

	}

	private TimeRange checkTimeRange(long time) {
		Date date = new Date(time*1000);
		if ((date.getHours()==10)&&(date.getMinutes()==5)) return TimeRange.StartTime;  // 10:10 is the start time. bar time is 10:05
		else if ((date.getHours()>=16)||(date.getHours()==15)&&(date.getMinutes()>=55)) return TimeRange.EndTime; // 15:55, close price is 16:00
		else if ((date.getHours()>10)||((date.getHours()==10)&&(date.getMinutes()>5))) return TimeRange.MidTime;
		return TimeRange.nil;
	}



	
	

}
