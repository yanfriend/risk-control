package common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import common.DataEngine;
import common.Strategy;

class SymUnit {
	String symbol, type, expiry, exchange; 
}

public class DownloadDataWrap
{
	@SuppressWarnings("static-access")
	static public boolean parseWrap(DataEngine dataEngine, String location, int downloadtype) {
		BufferedReader reader = null;
        try {
			reader = new BufferedReader(new FileReader(location));

        String text = null;

        // repeat until all lines is read
        while ((text = reader.readLine()) != null) {
        	if (text.trim()=="") continue; 
        	if (text.trim().startsWith("//")) continue; 
        	System.out.println(text); 
        // text = "6EU2-GLOBEX-FUT";
        text = text.replace("\"", "");   
        SymUnit symUnit = getSymUnit(text);
        if ((symUnit==null)||(symUnit.symbol.equals(""))) continue; /// change to continue; 
        
			DownloadData downloadStrategy = new DownloadData(); 
			downloadStrategy.downloadType = downloadtype;   // 1 for hourly download. 2 daily, 0 default 5 min
			downloadStrategy.setDataEngine(dataEngine); 
						
			downloadStrategy.contract.m_symbol = symUnit.symbol;  // "EUR";
			downloadStrategy.contract.m_secType = symUnit.type;  // "FUT"; 
			downloadStrategy.contract.m_expiry = symUnit.expiry; // "201209"; 
			downloadStrategy.contract.m_exchange = symUnit.exchange;  // "GLOBEX"; 
 
			Strategy strategy = downloadStrategy; 
				
			strategy.monitor();    		
			Thread.currentThread().sleep(1000*15); // sleep for 15 seconds.

		}  
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        catch (IOException e) {
        	e.printStackTrace();
        }
	    catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
	    }  
        try {
			Thread.currentThread().sleep(1000*15);  // wait for the last symbol to finish. 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  // 
        System.out.println("Finished this round of hourly update, waiting for the next trigger.");
		return true; 
	}


	private static SymUnit getSymUnit(String text) {
		// TODO "6EU2-GLOBEX-FUT"
		SymUnit su = new SymUnit();
		if (text.contains("GLOBEX")||text.contains("NYMEX")) {  // "6EU2-GLOBEX-FUT"  
			if (text.contains("GLOBEX")) su.exchange = "GLOBEX"; 
			if (text.contains("NYMEX")) su.exchange = "NYMEX"; 
			su.type = "FUT";
			
			if (text.startsWith("6E")) su.symbol = "EUR"; 
			else if (text.startsWith("6A")) su.symbol = "AUD"; 
			else if (text.startsWith("6B")) su.symbol = "GBP"; 
			else if (text.startsWith("6C")) su.symbol = "CAD"; 
			else if (text.startsWith("6S")) su.symbol = "CHF"; 
			else if (text.startsWith("6J")) su.symbol = "JPY"; 
			else if (text.startsWith("6N")) su.symbol = "NZD"; 
			else su.symbol = text.substring(0,2); // not include the last one. 
			
			int expire = 2010 + (text.charAt(3)-'0');       // valid for till 2020. 
			su.expiry = "" + expire + "" + charToMonth(text.charAt(2));
			return su;
		}
		else if (text.contains("ECBOT")) {  // "YM   SEP 12-ECBOT-FUT" 
			su.exchange = "ECBOT"; 
			su.type = "FUT";
			su.symbol = text.substring(0,2);
			// 5->11, SEP 12. 
			su.expiry = "" + (2000 + Integer.parseInt(text.substring(9,11))) + strToMonth(text.substring(5,8));  // valid till 2099
			return su;
		}
		
		return null; 
	}


	private static String strToMonth(String substring) {
		if (substring.equals("JAN")) return "01";
		else if (substring.equals("FEB")) return "02";
		else if (substring.equals("MAR")) return "03";
		else if (substring.equals("APR")) return "04";
		else if (substring.equals("MAY")) return "05";
		else if (substring.equals("JUN")) return "06";
		else if (substring.equals("JUL")) return "07";
		else if (substring.equals("AUG")) return "08";
		else if (substring.equals("SEP")) return "09";
		else if (substring.equals("OCT")) return "10";
		else if (substring.equals("NOV")) return "11";
		else if (substring.equals("DEC")) return "12";

		return null;
	}


	private static String charToMonth(char charAt) {
		switch (charAt) {
		case 'F': return "01";
		case 'G': return "02";
		case 'H': return "03";
		case 'J': return "04";
		case 'K': return "05";
		case 'M': return "06";
		case 'N': return "07";
		case 'Q': return "08";
		case 'U': return "09";
		case 'V': return "10";
		case 'X': return "11";
		case 'Z': return "12";
			/*
			Month Codes
			January	F
			February	G
			March	H
			April	J
			May	K
			June	M
			July	N
			August	Q
			September	U
			October	V
			November	X
			December	Z
			*/
		}
		return null;
	}
	

}