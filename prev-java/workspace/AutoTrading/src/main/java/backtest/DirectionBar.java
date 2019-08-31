package backtest;

import java.util.Date;

import common.Bar;

public class DirectionBar extends Bar{
	
	private int firstDown;
	private double range; 
	private long miliSeconds; 	
	
	public DirectionBar(Date date, double open, double high, double low, double close) {
		// getTime() Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this Date object.
		super(date.getTime()/1000, open, high, low, close, 0); 
	} 

	public DirectionBar() {
		super(); 
	}

	public int getFirstDown() {
		return firstDown;
	}
	public void setFirstDown(int firstDown) {
		this.firstDown = firstDown;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getRange() {
		return range;
	}

	public void setMiliSeconds(long miliSeconds) {
		this.miliSeconds = miliSeconds;
	}

	public long getMiliSeconds() {
		return miliSeconds;
	}
    
}
