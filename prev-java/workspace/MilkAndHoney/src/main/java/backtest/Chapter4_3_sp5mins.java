package backtest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.Bar;

public class Chapter4_3_sp5mins implements BacktestProcess{

	private transient final Logger log = LoggerFactory.getLogger(Chapter4_3_sp5mins.class);
	
	public void process(String datafile) {
		// open file, read and process, save to output file. 
    	try {
    	    BufferedReader in = new BufferedReader(new FileReader(datafile));
    	    String str;
    		TreeMap<Long, Bar> fiveMinBars = new TreeMap<Long, Bar>(); 
    		TreeMap<Long, DirectionBar> directionDays = new TreeMap<Long, DirectionBar>(); 
    		
    		double previousRange = 0, range=0;
    		float rangePercentage = 0.5f;
    	    
    	    while ((str = in.readLine()) != null) {
    	    	if (str.trim().length()<=0) {
    	    		continue;
    	    	}
    	    	if (str.trim().charAt(0)=='#') {  // get rid of comment line beginning with #
    	    		continue; 
    	    	}        	    	
    	    	
    	    	Date date;
    	    	double open, high, low, close; 

    	    	try {
	    	    	String[] strArr = str.split(",");
	    	    	String dateString = strArr[0].trim();
	    	    	SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy HH:mm:ss");
	    	    	date = (Date)sdf.parse(dateString);  
    	    	
    	    		open = Double.parseDouble(strArr[1].trim());
    	    		high = Double.parseDouble(strArr[2].trim());    	    		
    	    		low = Double.parseDouble(strArr[3].trim());
    	    		close = Double.parseDouble(strArr[4].trim());
    	    	} catch (Exception e){
    	    		log.error("error in backtest data file line: {}", str);
    	    		previousRange = 0;
    	    		continue; 
    	    	}
    	    	
    	    	Bar barUnit = new DirectionBar(date, open, high, low, close); 
    	    	fiveMinBars.put(barUnit.getDate(), barUnit);
    	    	
    	    	if ((date.getHours()==15)&&(date.getMinutes()==55)&&(date.getSeconds()==0)) {
    	    		// last bar of the trading, call to handle a day bar. 
    	    		DirectionBar oneDayBar = compressBar(fiveMinBars, previousRange, rangePercentage);
    	    		if (oneDayBar == null) {
    	    			continue; 
    	    		}
    	    		directionDays.put(oneDayBar.getDate(), oneDayBar);
    	    		previousRange = oneDayBar.getHigh()-oneDayBar.getLow();
    	    		fiveMinBars.clear(); 
    	    	} 
    	    }
    	    in.close();
        	
        	tradeRecord(directionDays, "trade_"+datafile, rangePercentage);
        	
    	} catch (IOException e) {
    		log.error("Exception in reading OPT stock scanning output file:{}",e);
    	} catch (Exception e) {
    		log.error("Exception in reading OPT stock scanning output file:{}",e);
    	}	
	}

	private void tradeRecord(TreeMap<Long, DirectionBar> directionDays, String logfile, float rangePercentage) {
		// go through each day bars. recording buy/sell, prices. 
        Collection<DirectionBar> barCollection = directionDays.values();
        Iterator<DirectionBar> iterator = barCollection.iterator();       		
        DirectionBar bar = null;  
        Collection<TradeRecord> tRecords = new ArrayList<TradeRecord>();
        
        float totalProfitPercentage = 0, maxDrawdownPercentage = 0;
        int totalWinCount = 0, totalLoseCount = 0, totalTradeCount = 0; 
        
        while (iterator.hasNext()) {          
        	float tmpProfit = 0; 
        	float tmpProfitPercentage =0;
        	TradeRecord tr = new TradeRecord(); 
        	bar = (DirectionBar) iterator.next();          

        	if (bar.getFirstDown()==0) continue; 
        	else if (bar.getFirstDown()==1) {
        		tr.setOperation("Long");
        		tr.setEntryPrice(bar.getOpen()-rangePercentage*bar.getRange());
        		if (bar.getHigh()>(bar.getOpen()+rangePercentage*bar.getRange())) {
        			tr.setExitPrice(bar.getOpen()+rangePercentage*bar.getRange());
        		} else {
        	       	tr.setExitPrice(bar.getClose());
        		}
        	    tmpProfit = (float) (tr.getExitPrice()-tr.getEntryPrice()); 
        	    tmpProfitPercentage = (float) (tmpProfit*100/tr.getEntryPrice());
        	    
        	    maxDrawdownPercentage = (float) ((bar.getLow() - tr.getEntryPrice())/tr.getEntryPrice())*100;

        	} else if (bar.getFirstDown()==-1) {
        		tr.setOperation("Short");
        		tr.setEntryPrice(bar.getOpen()+rangePercentage*bar.getRange());
        		if (bar.getLow()<(bar.getOpen()-rangePercentage*bar.getRange())) {
        			tr.setExitPrice(bar.getOpen()-rangePercentage*bar.getRange());
        		} else {
        	       	tr.setExitPrice(bar.getClose());
        		} 
            	tmpProfit = -(float) (tr.getExitPrice()-tr.getEntryPrice()); 
            	tmpProfitPercentage = (float) (tmpProfit*100/tr.getEntryPrice());   	
            	
            	maxDrawdownPercentage = -(float) ((bar.getHigh() - tr.getEntryPrice())/tr.getEntryPrice())*100;
        	}
    	    tr.setEntryDate(bar.getMiliSeconds());   // exact entry time. 
        	
    	    // tr.setExitDate(bar.getDate());
        	Date tmpdate = new Date(bar.getDate()*1000);  
        	tmpdate.setHours(16); 
        	tmpdate.setMinutes(0);
        	tmpdate.setSeconds(0);
        	tr.setExitDate(tmpdate);
        	
        	tr.setProfit(tmpProfit);
        	tr.setProfitPercentage(tmpProfitPercentage);
        	tr.setMaxDrawdownPercentage(maxDrawdownPercentage);
        	
        	tRecords.add(tr);   // add a trade record to whole list. 
        	log.info(tr.toString());
        	
        	totalTradeCount ++; 
        	if (tmpProfit>0) totalWinCount++; else totalLoseCount++; 
        	totalProfitPercentage += tmpProfitPercentage; 
        }
        saveTradeData(this.getClass().getName(),tRecords);
        String summary = String.format("total trades:%d, win: %d, lose:%d, total profitPercentage: %f",totalTradeCount, totalWinCount, totalLoseCount, totalProfitPercentage);
        log.info(summary);
	}

	private DirectionBar compressBar(TreeMap<Long, Bar> barMap, double range, float rangePercentage) {
		DirectionBar oneDayBar = new DirectionBar();
		
        Collection<Bar> barCollection = barMap.values();
        Iterator<Bar> iterator = barCollection.iterator();       
        Bar bar = null;   
        double high = -1, low = 88888888;
        long highbar = 0, lowbar = 0; 
        
        while (iterator.hasNext()) {          
        	bar = (Bar) iterator.next();          
        	long barSec = bar.getDate();
       		Date datetmp = new Date(barSec * 1000);  // mili-second
       		if ((datetmp.getHours()==9)&&(datetmp.getMinutes()==30)) {
       			oneDayBar.setOpen(bar.getOpen());
       			oneDayBar.setDate(bar.getDate());
       			highbar = 0; lowbar = 0; 
       			high = -1; low = 88888888;
       		}
       		if (oneDayBar.getOpen()<=0) {
       			return null; 
       		}
       		if ((datetmp.getHours()==15)&&(datetmp.getMinutes()==55)) {
       			oneDayBar.setClose(bar.getClose());
       		}
       		if (bar.getLow()<low) {
       			low = bar.getLow();
       		}
       		if (bar.getHigh()>high) {
       			high = bar.getHigh();
       		}
       		if (bar.getLow()<(oneDayBar.getOpen()-rangePercentage*range)) {
       			lowbar = (lowbar>0)? lowbar : bar.getDate();  // only record the first appearance
       	        oneDayBar.setMiliSeconds(lowbar);
       		}
       		if (bar.getHigh()>(oneDayBar.getOpen()+rangePercentage*range)) {
       			highbar = (highbar>0)? highbar: bar.getDate();
       			oneDayBar.setMiliSeconds(highbar);
       		}
        }  // of while. 
        oneDayBar.setLow(low);
        oneDayBar.setHigh(high);
        if (range<=0) {
        	oneDayBar.setFirstDown(0);
        } else if ((lowbar<=0)&&(highbar<=0)) {
        	oneDayBar.setFirstDown(0); // not reached.
        } else if (lowbar<=0) {  // highbar reached
        	oneDayBar.setFirstDown(-1);
        } else if (highbar<=0) {  // lowbar reached
        	oneDayBar.setFirstDown(1);
        } else if (lowbar<highbar) {  // last two, all reached, compare. 
        	oneDayBar.setFirstDown(1);
        } else if (lowbar>highbar) {
        	oneDayBar.setFirstDown(-1);
        } else oneDayBar.setFirstDown(0);
        oneDayBar.setRange(range);
		return oneDayBar; 
	}

	private void saveTradeData(String strategy, Collection<TradeRecord> tmpBarColl) {
    	try {
		    BufferedWriter out = new BufferedWriter(new 
		    	FileWriter("D:\\CommonData\\Stocks\\testData\\"+ strategy +"_SYM_trades"+ ".csv"));
		    
		    Iterator<TradeRecord> barIter = tmpBarColl.iterator();
		    while (barIter.hasNext()) {
		    	String str="";
		    	TradeRecord tr = barIter.next();
		    	str=tr.toString(); 
		    	out.write(str +"\n");
		    }
		    out.close();
		    log.info("saved to trade file.");
    	} catch (IOException e2) {
    		log.error("Exception in saving trading records file:{}",e2);
    		return; 
    	}
    }	

}
