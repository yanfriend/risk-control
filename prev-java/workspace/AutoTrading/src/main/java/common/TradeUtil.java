package common;

public class TradeUtil {
	public static String reverseAction(String action) {
		if ("BUY".equals(action)) {
			return "SELL";
		} else if ("SELL".equals(action)) {
			return "BUY";
		}
		return "";
	}
}
