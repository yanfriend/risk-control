package cot;

import java.util.Arrays;
import java.util.Vector;

class CotMatchUnit {
	public String symbol; 
	public String matchString; 
	public boolean financial; 
	
	public CotMatchUnit() {
		matchString = "";
		financial = false; 
	}
	public CotMatchUnit(String symbolin, String matchin, boolean finin) {
		symbol = symbolin;
		matchString = matchin;
		financial = finin; 
	}
}

public class CotUtil {
	/**
	 * if add a symbol, three places need changing:
	 * 1, add it to symbols
	 * 2, add its token to getToken()
	 * 3, if its financial, add it to isFinancial()
	 */
	
	public static enum Cot_Show {Percentage, Value};
	
	public static Vector<CotMatchUnit> container = new Vector<CotMatchUnit>(); 
	
	static String[] symbols;  
	
	static {
		// all main forex currency. 
		container.add(new CotMatchUnit("---currency:", "", true));
		container.add(new CotMatchUnit("usd ?", "\"U.S. DOLLAR INDEX - ICE FUTURES U.S.", true));
		container.add(new CotMatchUnit("eur", "\"EURO FX - ", true));
		container.add(new CotMatchUnit("jpy", "\"JAPANESE YEN - ", true));
		container.add(new CotMatchUnit("cad", "\"CANADIAN DOLLAR - ", true));
		container.add(new CotMatchUnit("chf", "\"SWISS FRANC - ", true));
		container.add(new CotMatchUnit("gbp", "\"BRITISH POUND STERLING - ", true));
		container.add(new CotMatchUnit("aud", "\"AUSTRALIAN DOLLAR - ", true));
		container.add(new CotMatchUnit("nzd", "\"NEW ZEALAND DOLLAR - ", true));		
			
		// indices
		container.add(new CotMatchUnit("---indices:", "", true));
		container.add(new CotMatchUnit("sp500 ?", "\"S&P 500 Consolidated - CHICAGO MERCANTILE EXCHANGE", true));
		container.add(new CotMatchUnit("djia ?", "\"DJIA Consolidated - CHICAGO BOARD OF TRADE", true));
		
		// bonds
		container.add(new CotMatchUnit("---interest rate:", "", true));
		container.add(new CotMatchUnit("10 year notes ?", "\"10-YEAR U.S. TREASURY NOTES - CHICAGO BOARD OF TRADE", true));
		container.add(new CotMatchUnit("5 year notes ?", "\"5-YEAR U.S. TREASURY NOTES - CHICAGO BOARD OF TRADE", true));
		container.add(new CotMatchUnit("2 year notes ?", "\"2-YEAR U.S. TREASURY NOTES - CHICAGO BOARD OF TRADE", true));
		container.add(new CotMatchUnit("30 year bonds ?", "\"LONG-TERM U.S. TREASURY BONDS - CHICAGO BOARD OF TRADE", true));

		// agriculture
		container.add(new CotMatchUnit("---agriculture:", "", true));
		container.add(new CotMatchUnit("wheat", "\"WHEAT - CHICAGO BOARD OF TRADE", false));
		container.add(new CotMatchUnit("corn", "\"CORN - CHICAGO BOARD OF TRADE", false));
		container.add(new CotMatchUnit("soybean oil", "\"SOYBEAN OIL - CHICAGO BOARD OF TRADE", false));
		container.add(new CotMatchUnit("soybean meal", "\"SOYBEAN MEAL - CHICAGO BOARD OF TRADE", false));
		container.add(new CotMatchUnit("soybean", "\"SOYBEANS - CHICAGO BOARD OF TRADE", false));
		
		// meat
		container.add(new CotMatchUnit("---meat:", "", false));
		container.add(new CotMatchUnit("lean hog", "\"LEAN HOGS - CHICAGO MERCANTILE EXCHANGE", false));
		container.add(new CotMatchUnit("feeder cattle", "\"FEEDER CATTLE - CHICAGO MERCANTILE EXCHANGE", false));
		container.add(new CotMatchUnit("live cattle", "\"LIVE CATTLE - CHICAGO MERCANTILE EXCHANGE", false));
		
		// energy
		container.add(new CotMatchUnit("---energy:", "", false));
		container.add(new CotMatchUnit("crude oil", "\"CRUDE OIL, LIGHT SWEET - NEW YORK MERCANTILE EXCHANGE", false));  // only for nyme, there are others. 
		container.add(new CotMatchUnit("natural gas", "\"NATURAL GAS - NEW YORK MERCANTILE EXCHANGE", false));  // not accurate for trading at all. 

		// metal
		container.add(new CotMatchUnit("---metal:", "", false));
		container.add(new CotMatchUnit("gold", "\"GOLD - COMMODITY EXCHANGE INC.", false));  // only for nyme, there are others. 
		container.add(new CotMatchUnit("silver", "\"SILVER - COMMODITY EXCHANGE INC.", false));  // not accurate for trading at all. 

		// softie
		container.add(new CotMatchUnit("---softie:", "", false));
		container.add(new CotMatchUnit("coffee", "\"COFFEE C - ICE FUTURES U.S.", false));  // only for nyme, there are others. 
		container.add(new CotMatchUnit("cotton", "\"COTTON NO. 2 - ICE FUTURES U.S.", false));  // not accurate for trading at all. 
		container.add(new CotMatchUnit("cocoa", "\"COCOA - ICE FUTURES U.S.", false));  // only for nyme, there are others. 
		container.add(new CotMatchUnit("sugar", "\"SUGAR NO. 11 - ICE FUTURES U.S.", false));  // not accurate for trading at all. 
		
		Vector<String> tmpvec=new Vector();
		for (int i=0; i<container.size(); i++) {
			tmpvec.add(container.get(i).symbol);
		}
		symbols = Arrays.copyOf(tmpvec.toArray(), tmpvec.toArray().length, String[].class);  
	}
	
	// static String[] symbols;  
	/* {"---currency:", "usd ?", "eur", "cad", "jpy", "chf", "gbp", "aud", "nzd",
		"---indices:", "sp500 ?", "djia ?", 
		"---interest rate:", "30 year bonds ?", "10 year notes ?", "5 year notes ?", "2 year notes ?",
		"---agriculture:", "wheat", "corn", "soybean", "soybean meal", "soybean oil", 
		"---meat:", "lean hog", "feeder cattle", "live cattle",
		"---energy:", "crude oil", "natural gas",
		"---metal:", "gold", "silver",
		"---softie:", "coffee", "cotton", "cocoa","sugar"
			}; 
			*/
	
	static String[] getFiles(String commodity) {
		String[] inFiles = new String[2];
		if (isFinancial(commodity)){
		    inFiles[0] = "D:\\CommonData\\Stocks\\COT\\com_fin_txt_2006_2015.zip";  //  for test for convenience input.
		    inFiles[1] = "D:\\CommonData\\Stocks\\COT\\com_fin_txt_2016.zip";
	    } else {
		    inFiles[0] = "D:\\CommonData\\Stocks\\COT\\com_disagg_txt_hist_2006_2015.zip";  //  for test for convenience input.
		    inFiles[1] = "D:\\CommonData\\Stocks\\COT\\com_disagg_txt_2016.zip";
	    }
		return inFiles;
	}
	
	static String getToken(String commodity) {
		for (int i=0; i<container.size(); i++) {
			if ((container.get(i).symbol).equals(commodity)) return container.get(i).matchString;
		}
		return null;
		
		/*
//		if (commodity==null) return null;

		// all main forex currency. 
		if (commodity.startsWith("usd")) return  "\"U.S. DOLLAR INDEX - ICE FUTURES U.S.";
		if (commodity.startsWith("euro")||commodity.startsWith("eur")) return "\"EURO FX - "; 
		if (commodity.startsWith("jpy")) return "\"JAPANESE YEN - "; 
		if (commodity.startsWith("cad")) return "\"CANADIAN DOLLAR - ";
		if (commodity.startsWith("chf")) return "\"SWISS FRANC - ";
		if (commodity.startsWith("gbp")) return "\"BRITISH POUND STERLING - ";
		if (commodity.startsWith("aud")) return "\"AUSTRALIAN DOLLAR - ";
		if (commodity.startsWith("nzd")) return "\"NEW ZEALAND DOLLAR - ";
		
		// bonds
		if (commodity.startsWith("10 year notes")) return "\"10-YEAR U.S. TREASURY NOTES - CHICAGO BOARD OF TRADE";
		if (commodity.startsWith("5 year notes")) return "\"5-YEAR U.S. TREASURY NOTES - CHICAGO BOARD OF TRADE";
		if (commodity.startsWith("2 year notes")) return "\"2-YEAR U.S. TREASURY NOTES - CHICAGO BOARD OF TRADE";
		if (commodity.startsWith("30 year bonds")) return "\"LONG-TERM U.S. TREASURY BONDS - CHICAGO BOARD OF TRADE";
		
		// indices
		if (commodity.startsWith("sp500")) return "\"S&P 500 Consolidated - CHICAGO MERCANTILE EXCHANGE";
		if (commodity.startsWith("djia")) return "\"DJIA Consolidated - CHICAGO BOARD OF TRADE";
		
		// agriculture
		if ((commodity.startsWith("cwheat"))||(commodity.startsWith("wheat"))) return  "\"WHEAT - CHICAGO BOARD OF TRADE";
		if (commodity.startsWith("corn")) return "\"CORN - CHICAGO BOARD OF TRADE"; 
		if (commodity.startsWith("soybean oil")) return "\"SOYBEAN OIL - CHICAGO BOARD OF TRADE";
		if (commodity.startsWith("soybean meal")) return "\"SOYBEAN MEAL - CHICAGO BOARD OF TRADE"; 
		if (commodity.startsWith("soybean")) return "\"SOYBEANS - CHICAGO BOARD OF TRADE";   // since I used begin with, Order is important!!

		// meat
		if (commodity.startsWith("lean hog")) return  "\"LEAN HOGS - CHICAGO MERCANTILE EXCHANGE";
		if (commodity.startsWith("feeder cattle")) return  "\"FEEDER CATTLE - CHICAGO MERCANTILE EXCHANGE";
		if (commodity.startsWith("live cattle")) return  "\"LIVE CATTLE - CHICAGO MERCANTILE EXCHANGE";
		
		// energy
		if (commodity.startsWith("crude oil")) return "\"CRUDE OIL, LIGHT SWEET - NEW YORK MERCANTILE EXCHANGE";   // only for nyme, there are others. 
		if (commodity.startsWith("natural gas")) return "\"NATURAL GAS - NEW YORK MERCANTILE EXCHANGE";   // not accurate for trading at all. 
			
		// metal
		if (commodity.startsWith("gold")) return "\"GOLD - COMMODITY EXCHANGE INC."; 
		if (commodity.startsWith("silver")) return "\"SILVER - COMMODITY EXCHANGE INC."; 
		

		// softie
		if (commodity.startsWith("coffee")) return "\"COFFEE C - ICE FUTURES U.S."; 
		if (commodity.startsWith("cotton")) return "\"COTTON NO. 2 - ICE FUTURES U.S."; 
		if (commodity.startsWith("cocoa")) return "\"COCOA - ICE FUTURES U.S."; 
		if (commodity.startsWith("sugar")) return "\"SUGAR NO. 11 - ICE FUTURES U.S."; 
		
	    System.out.println("token error, exit"); 
		System.exit(-1);
		return null;
		*/
	}
	
	static boolean isFinancial(String commodity) {
		for (int i=0; i<container.size(); i++) {
			if ((container.get(i).symbol).equals(commodity)) return container.get(i).financial;
		}
		return false;
		/*   if (	
				(commodity.startsWith("usd")) || 
				(commodity.startsWith("eur")) || 
				(commodity.startsWith("jpy")) || 
				(commodity.startsWith("cad")) ||
				(commodity.startsWith("chf")) ||
				(commodity.startsWith("gbp")) ||
				(commodity.startsWith("aud")) ||
				(commodity.startsWith("nzd")) ||
				
				(commodity.startsWith("10 year notes")) ||
				(commodity.startsWith("5 year notes")) ||
				(commodity.startsWith("2 year notes")) ||
				(commodity.startsWith("30 year bonds")) ||
				
				(commodity.startsWith("sp500")) ||
				(commodity.startsWith("djia"))
				) 
					return true; 
			else return false; 
			*/
	}
	
}
