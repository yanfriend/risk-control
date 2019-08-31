import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ForexPrices extends HistoricalPrice{

	@Override
	protected boolean scanDate(BufferedReader in) {
		String str;
		// read data, at present stage, this is the only difference from CompactPriceCycle. 
		try {
		   str = in.readLine();  // skip the first line. 
	    while ((str = in.readLine()) != null) {
	    	if (str.trim().length()<=0) {
	    		continue;
	    	}
	    	String[] strArr = str.split(",");
	    	String dateString = strArr[0].trim();
	    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
	    	Date date = (Date)sdf.parse(dateString);  
	    	int year, month, day;
	    	year = date.getYear() + 1900;
	    	month = date.getMonth();
	    	day = date.getDate(); 
	    	if (year<INIT_YEAR)  continue;  // too old data. 
	    	if ((year-INIT_YEAR)>14) continue;  // only 1-14 in the array, not include 2012

    		double close = Double.parseDouble(strArr[1].trim());    // the only difference with futuresPrices.  
    		if (close <=0 ) {
	    		System.out.println("price problem, return null");
	    		return false; 
    		}
    		yearPrice[year-INIT_YEAR][month][day] = close; 
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
}
