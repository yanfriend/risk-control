package common;

public enum PriceTypeEnum {
	TRADES("TRADES"),
	BID("BID"),
	ASK("ASK"),
	MIDPOINT("MIDPOINT");
	
	String type; 
	PriceTypeEnum(String type) {
		this.type = type;
	}
	
	public String toString() {
		return type; 
	}

}
