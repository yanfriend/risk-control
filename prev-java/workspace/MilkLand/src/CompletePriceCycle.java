import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CompletePriceCycle {
	final static int INIT_YEAR = 1997;   // from 97 to 2011, 15 years data
	final static int CYCLE = 1;
	
	public void check(String inFileName, String outFileName) {
		double[][][] yearPrice = new double[15][12][32];  // first day from 1, not 0

    	BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(inFileName));
    	    scanDate(in, yearPrice); 			
    	} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
    	    
    	double[][][] cyclePrice = new double[CYCLE][12][32];  // first day from 1, not 0
    	for (int j=0; j<12; j++) {
    		for (int i=0; i<CYCLE; i++) {
	    		int year = i; 
	    		int[] realCount = new int[32];
	    		while (year<yearPrice.length) {
	    			for (int k=0; k<32; k++) {
	    				if (yearPrice[year][j][k]>0) {
	    					cyclePrice[i][j][k] += yearPrice[year][j][k];
	    					realCount[k]++; 
	    				}
	    			}  // finish for a month
	    			year += CYCLE; 
	    		}  // finish for a month in all years in one cycle. 
	    		// now calculate the average for that month
	    		for (int k=0; k<32; k++) {
	    			if (realCount[k]>=6) // require at least appear 6 times to a valid mean. 
	    				cyclePrice[i][j][k] /= realCount[k];
	    			else cyclePrice[i][j][k] = 0;  // discard the too few data points. 
	    		}
    		}  // for year cycle
    	}
    	    
	    try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outFileName));
			
			for (int i=0; i<cyclePrice.length; i++) {
			  for (int j=0; j<cyclePrice[i].length; j++){ 
				for (int k=0; k<cyclePrice[i][j].length; k++) {
					if (cyclePrice[i][j][k] > 0) {
						Date prtDate = new Date(INIT_YEAR+i,j,k);
						SimpleDateFormat sdf = new SimpleDateFormat("d-MMM-yy");
				    	String prtString;
				    	prtString = String.format("%s,%f", sdf.format(prtDate),cyclePrice[i][j][k]);   
				    	out.write(prtString+"\n");				    	
					}
					
				}
			  }
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finished");
	}
	
	@SuppressWarnings("deprecation")
	boolean scanDate(BufferedReader in, double[][][] yearPrice) {
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

    		double close = Double.parseDouble(strArr[4].trim());    // 4 is close price.  
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
	
	// move 0 price to back positions. 
	for (int i=0; i<yearPrice.length; i++) 
		for (int j=0; j<yearPrice[i].length; j++) {
			int daylength = yearPrice[i][j].length; 
			int zp; 
			int k = 1; 

			while ((k<daylength)&&(yearPrice[i][j][k]>0)) k++; 
		    zp = k+1; 
		    while (zp<daylength) { 
		    	if (yearPrice[i][j][zp]>0) {
		    		yearPrice[i][j][k++] = yearPrice[i][j][zp];
		    		yearPrice[i][j][zp] = 0; 
		    	}
		    	zp++;  
		    }
		    while (k<daylength) {
		    	yearPrice[i][j][k++] = 0; 
		    }
			
		}  // two loops
	
	// normalize all the prices. 
	for (int i=0; i<yearPrice.length; i++) {
		double initPrice = yearPrice[i][0][1];   // that year, Jan, 1st;
		if (initPrice <=0) continue; 
		for (int j=0; j<yearPrice[i].length; j++) 
			for (int k=1; k<yearPrice[i][j].length; k++) {
				yearPrice[i][j][k] /= initPrice; 
			}	
	}
		return true;
	}

}
