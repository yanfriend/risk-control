package common;

import com.ib.client.Contract;
import com.ib.client.Execution;

public interface Strategy {
	public boolean parse(String str);
	public void monitor(); 
	public void checkHistoricalData(String date, double open, double high, double low, double close, int volume); 
	public void checkExecution(Execution execution);
	
	
	public Contract getContract();
	public void setCurrentPrice(double currentPrice);
	// public void setTargetReached(boolean targetReached);
}
