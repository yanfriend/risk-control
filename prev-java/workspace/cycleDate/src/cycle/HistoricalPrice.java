package cycle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * abstract class for check quote cycle and simulated trading methods. 
 * instantiate input method for real using. 
 */
public abstract class HistoricalPrice {
	static int INIT_YEAR = 1997;   // from 97 to 2011, 15 years data
	static int CYCLE = 1;
	double[][][] yearPrice = new double[15][12][32];  // first day from 1, not 0;  month is from 0-11
	
	public void checkCycle(String inFileName, String outFileName) {
    	BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(inFileName));
    	    scanDate(in); 			
    	    in.close();
    	} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		compressToTradingDay();  // push blank day to end of each month.
    	normalize();  // normalize data 
    	    
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
	
	public void simuTrade(String inFile, int startMonth, int startDay,
			int endMonth, int endDay) {
		startMonth--; 
		endMonth--; 
		
		assert((startMonth>=0) && (endMonth<12));
		assert((startDay>=1)&&(endDay<=31));
		assert(startMonth>=endMonth); 
		
    	BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(inFile));
    	    scanDate(in); 			
			in.close();
    	} catch (FileNotFoundException e1) {
			e1.printStackTrace();
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	compressToTradingDay();  // push blank day to end of each month.
    	
    	// output format: entry date, price, interim high, low, exit date, price, gross result, cumulative net result. 
    	double cumuPercentage = 0;
    	int winCount = 0, loseCount = 0; 
    	double maxUp=0, maxDown=0, totalUp=0, totalDown=0, maxupPer=0, maxdownPer=0; 
		for (int i=0; i<yearPrice.length; i++) {  // year. 
			String outstr = "";
			double startPrice=yearPrice[i][startMonth][startDay] , endPrice=yearPrice[i][endMonth][endDay]; 
			paraTrans pt = new paraTrans();

			if ((startPrice<=0)||(endPrice<=0)) {
				System.out.println("start/end price/month,day error");
				continue; 
			}
			outstr += String.format("%d/%d/%d,%f,", INIT_YEAR+i,startMonth+1, startDay, startPrice);

			if (startMonth == endMonth) {
				for (int day=startDay; day<=endDay; day++) {
					getHighLow(pt, i, startMonth, day);
				}
			} else {  // more than 1 month. 
				for (int day=startDay; day<32; day++) {   // the first month
					getHighLow(pt, i, startMonth, day);
				}
				for (int j=startMonth+1; j<endMonth; j++) {  // middle months 
					for (int k=1; k<=.32; k++) {
						getHighLow(pt,i, j, k);
					}	
				}
				for (int day=1; day<=endDay; day++) {   // the last month
					getHighLow(pt, i, endMonth, day);
				}
			} // else of startMonth == endMonth
			if ((pt.lowest<=0)||(pt.highest<=0)) {
				System.out.println("interim high/low error");
				continue; 
			}
			outstr += String.format("%f,%f,", pt.highest, pt.lowest); 
			outstr += String.format("%d/%d/%d,%f,%f%%", INIT_YEAR+i,endMonth+1, endDay, endPrice, (endPrice-startPrice)/startPrice*100);
			System.out.println(outstr);
			cumuPercentage += (endPrice-startPrice)/startPrice;
			if ((endPrice-startPrice)/startPrice>0) winCount++; else loseCount++; 
			if (pt.highest-startPrice>maxUp) {
				maxUp = pt.highest-startPrice;
				maxupPer = maxUp/startPrice; 
			}
			totalUp += pt.highest-startPrice; 
			if (startPrice-pt.lowest>maxDown) {
				maxDown = startPrice-pt.lowest;
				maxdownPer = maxDown/startPrice; 
			}
			totalDown += startPrice - pt.lowest;
		}
		System.out.println(String.format("win:%d, lose:%d, cumulative percentage:%f%%",winCount, loseCount, cumuPercentage*100));
		System.out.println(String.format("max up:%f, max up percentage:%f%%, max down:%f, max down percentage:%f%%",maxUp, maxupPer*100,
				maxDown, maxdownPer*100));
		System.out.println(String.format("average up:%f, average down:%f",totalUp/(winCount+loseCount), totalDown/(winCount+loseCount)));
	}

	protected abstract boolean scanDate(BufferedReader in);

	// -------------------- below are private ------------------------------
	private void compressToTradingDay() {
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
	}
	
	private void normalize() {
		// normalize all the prices. 
		for (int i=0; i<yearPrice.length; i++) {
			double initPrice = yearPrice[i][0][1];   // that year, Jan, 1st;
			if (initPrice <=0) continue; 
			for (int j=0; j<yearPrice[i].length; j++) 
				for (int k=1; k<yearPrice[i][j].length; k++) {
					yearPrice[i][j][k] /= initPrice; 
				}	
		}	
	}

	private void getHighLow(paraTrans pt, int i, int startMonth, int day) {
		if (yearPrice[i][startMonth][day]<=0) return; 
		if (pt.lowest>yearPrice[i][startMonth][day]) {pt.lowest=yearPrice[i][startMonth][day]; }
		if (pt.highest<yearPrice[i][startMonth][day]) {pt.highest=yearPrice[i][startMonth][day]; }
	}

	class paraTrans {
		public double lowest=99999, highest=-1; 	
	}

}

