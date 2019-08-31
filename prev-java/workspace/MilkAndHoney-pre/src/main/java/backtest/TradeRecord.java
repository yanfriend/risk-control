package backtest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TradeRecord {

	private String operation;  // long or short
	Date entryDate, exitDate; 
	double entryPrice, exitPrice; 
	private int positions = 100;
	float positionValue; 
	float profit, profitPercentage; 
	int barCount; 
	float mae, mfe; 
	float profitPerBar;
	float maxDrawdownPercentage; 

	public String toString() {
		String conString=""; 
		SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss"); //  HH:mm:ss");  
		conString = String.format("%s,%s,%f,%s,%f,%f,%f, %f", 
				operation, sdf.format(entryDate),entryPrice,sdf.format(exitDate),exitPrice, profit*positions, profitPercentage, 
				maxDrawdownPercentage);
		return conString; 
	 }
	public Date getEntryDate() {
		return entryDate;
	}
	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}
	public void setEntryDate(long entryDate) {
		this.entryDate = new Date(entryDate*1000);
	}
	public Date getExitDate() {
		return exitDate;
	}
	public void setExitDate(Date exitDate) {
		this.exitDate = exitDate;
	}
	public void setExitDate(long exitDate) {
		this.exitDate = new Date(1000*exitDate);
	}
	public double getEntryPrice() {
		return entryPrice;
	}
	public void setEntryPrice(double entryPrice) {
		this.entryPrice = entryPrice;
	}
	public double getExitPrice() {
		return exitPrice;
	}
	public void setExitPrice(double exitPrice) {
		this.exitPrice = exitPrice;
	}
	public int getPositions() {
		return positions;
	}
	public void setPositions(int positions) {
		this.positions = positions;
	}
	public float getPositionValue() {
		return positionValue;
	}
	public void setPositionValue(float positionValue) {
		this.positionValue = positionValue;
	}
	public float getProfit() {
		return profit;
	}
	public void setProfit(float profit) {
		this.profit = profit;
	}
	public float getProfitPercentage() {
		return profitPercentage;
	}
	public void setProfitPercentage(float profitPercentage) {
		this.profitPercentage = profitPercentage;
	}
	public int getBarCount() {
		return barCount;
	}
	public void setBarCount(int barCount) {
		this.barCount = barCount;
	}
	public float getMae() {
		return mae;
	}
	public void setMae(float mae) {
		this.mae = mae;
	}
	public float getMfe() {
		return mfe;
	}
	public void setMfe(float mfe) {
		this.mfe = mfe;
	}
	public float getProfitPerBar() {
		return profitPerBar;
	}
	public void setProfitPerBar(float profitPerBar) {
		this.profitPerBar = profitPerBar;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getOperation() {
		return operation;
	} 
	public float getMaxDrawdownPercentage() {
		return maxDrawdownPercentage;
	}
	public void setMaxDrawdownPercentage(float maxDrawdown) {
		this.maxDrawdownPercentage = maxDrawdown;
	}
	
}
