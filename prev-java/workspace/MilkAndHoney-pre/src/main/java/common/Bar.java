package common;

public class Bar {
	private double open; 
	private double high;
	private double low;
	private double close; 
	private int volume; 
	private long date; // second from 1970/1/1
	
	public Bar() {
		this.date = 0;
		this.open = 0;
		this.high = 0;
		this.low = 0; 
		this.close = 0; 
		this.volume = 0;		
	}
	
	public Bar(long date, double open, double high, double low, double close, int volume) 
	{
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low; 
		this.close = close; 
		this.volume = volume;		
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
	public void setDate(long date) {
		this.date = date;
	}
	public long getDate() {
		return date;
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
}
