package common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FuturesQuotes {

	List<Quote> quotes = new ArrayList<Quote>(); 
	
	public boolean loadDate(BufferedReader in, Date startDate, Date endDate) {
		quotes.clear();
		
		String str;
		try {
			for (int i=0; i<7; i++) {
				str = in.readLine();  // skip the first 7 lines. 				
			}
	    while ((str = in.readLine()) != null) {
	    	if (str.trim().length()<=0) {
	    		continue;
	    	}
	    	String[] strArr = str.split(",");
	    	String dateString = strArr[0].trim();
	    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
	    	Date date = (Date)sdf.parse(dateString);  
	    	
	    	if ((date.before(startDate))||(date.after(endDate))) continue; 
	    	
    		double open = Double.parseDouble(strArr[1].trim());     
    		double high = Double.parseDouble(strArr[2].trim()); 
    		double low = Double.parseDouble(strArr[3].trim()); 
    		double close = Double.parseDouble(strArr[4].trim()); 
    		int vol = Integer.parseInt(strArr[5].trim()); 
    		int oi = Integer.parseInt(strArr[6].trim()); 
    		
    		if ((close <=0)||(open<=0)||(high<=0)||(low<=0)||(vol<0)||(low>high)   // ignore oi so far
    				||(close>high)||(close<low)||(open<low)||(open>high) ) {
	    		System.out.println("data problem:"+str);
	    		continue; 
    		}
    		quotes.add(new Quote(date, open, high, low, close, vol, oi));
	    }
	} catch (IOException e) {
		System.out.println("Exception in reading cycle date file");
		e.printStackTrace();
	} catch (Exception e) {
		System.out.println("Exception in reading cycle date file");
		e.printStackTrace();
	}
		return true;
	}
	
	public void checkWeekdayPattern(String inFileName, Date startDate, Date endDate) {
		
		// load quotes
    	BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(inFileName));
    	    loadDate(in, startDate, endDate); 			
    	    in.close();
    	} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		weekdayStrategy(); 	
	}

	// implement strategy here. 
	private void weekdayStrategy() {
		if (quotes.size()<=0) return; 
		double cutlossAmount = 9999999;
		
		tradeLog[] weekdayLog = new tradeLog[6]; // 0 is not in use
		for (int i=1;i<6;i++) {
			weekdayLog[i] = new tradeLog(); 
		}
		char[] lastResult = new char[6]; 
		
		for (Quote aQuote:quotes)  {
			Date date = aQuote.getDate();
			int weekday = date.getDay(); 
//if(weekday!=2)  continue; // test tuesday only			
			// buy open and sell close
			double entry = aQuote.getOpen(), exit = aQuote.getClose(); 
			double result = exit-entry; 
			if ((aQuote.getOpen()-aQuote.getLow())>cutlossAmount) {
				result=-cutlossAmount; //trigger cut loss
				weekdayLog[weekday].setMaxintradayDrawdown( (float) -cutlossAmount);  // 9
			} else {
				weekdayLog[weekday].setMaxintradayDrawdown((float) result);
			}
			
			weekdayLog[weekday].finalResult += result;   // 1
			
			weekdayLog[weekday].allTrades++;   // 2
			if (result>=0) {
				weekdayLog[weekday].winningPoints += result; //3 
				weekdayLog[weekday].allWinningTrades++;  //4 
				if (lastResult[weekday]=='w') weekdayLog[weekday].consecutiveWin++;  // 5
				else weekdayLog[weekday].consecutiveWin = 1; 
				lastResult[weekday]='w';  // extra one. 
			} else {
				weekdayLog[weekday].losingPoints += result; //3 
				weekdayLog[weekday].allLosingTrades++;  //4 
				if (lastResult[weekday]=='l') weekdayLog[weekday].consecutiveLoss++;  // 5
				else weekdayLog[weekday].consecutiveLoss = 1; 
				lastResult[weekday]='l';  // extra one. 				
			}			
		}  // all quotes. 
		
		System.out.println(tradeLog.printTitle());
		for (int i=1;i<6;i++) {
			System.out.println(""+i+", "+weekdayLog[i]);
		}
	}


} // class

