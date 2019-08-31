package common;

import java.util.Date;

public class Quote {
	private Date date; // second from 1970/1/1
	private double open; 
	private double high;
	private double low;
	private double close; 
	private int volume; 
	private int OI; 
	
	public Quote() {
		this.setDate(new Date());
		this.open = 0;
		this.high = 0;
		this.low = 0; 
		this.close = 0; 
		this.volume = 0;		
		this.setOI(0);
	}
	
	public Quote(Date date, double open, double high, double low, double close, int volume, int OI) 
	{
		this.setDate(date);
		this.open = open;
		this.high = high;
		this.low = low; 
		this.close = close; 
		this.volume = volume;		
		this.setOI(OI);
	}
	
	public void setClose(double close) {
		this.close = close;
	}
	public double getClose() {
		return close;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}
	public int getVolume() {
		return volume;
	}
 
	public void setHigh(double high) {
		this.high = high;
	}

	public double getHigh() {
		return high;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getLow() {
		return low;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getOpen() {
		return open;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getOI() {
		return OI;
	}

	public void setOI(int oI) {
		OI = oI;
	}
}
