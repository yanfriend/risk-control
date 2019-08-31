package common;
 
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import backtest.BacktestProcess;

public class AutoTradeMain 
{
	private Map<String, String> securityLocations; 
	private Map<String, String> backtest_data; 
	
	static Logger log = LoggerFactory.getLogger(AutoTradeMain.class);

	public static void main (String[] args)
    {   
    	// Resource resource = new ClassPathResource("HE" + File.separator + "beans.xml");
		Resource resource = new ClassPathResource("beans.xml");
    	XmlBeanFactory factory = new XmlBeanFactory(resource);

    	AutoTradeMain autoTradeMain = (AutoTradeMain) factory.getBean("autoTradeMain");
    	autoTradeMain.run(factory);
		
//      System.out.println("This is the main menu");
//      System.out.println("1. euro cycle(short date format)");
//      System.out.println("2. wheat cycle(long date format, complete contract unit)");
//      Scanner reader = new Scanner(System.in);
//      int iChoice = reader.nextInt();
//      System.out.println("your choice:" + iChoice);
    	System.out.println("finish" );
    }
    	
    void run(XmlBeanFactory factory) {
    	//if (securityLocations!=null) {
    	for(Iterator it = securityLocations.keySet().iterator(); it.hasNext();) {
    		String strategyString = (String) it.next();
    		String location = securityLocations.get(strategyString);
    		Strategy strategy = (Strategy) factory.getBean(strategyString);   //(Strategy) it.next();
    		System.out.println(strategy + " = " + location);

    		if (location.equals("")) {
    	 	    if (strategy.parse("")) {
    		 		   strategy.monitor();    		
    			    }
    		}
    		else {
    		try {
        	    BufferedReader in = new BufferedReader(new FileReader(location));
        	    String str;
        	    while ((str = in.readLine()) != null) {
        	    	if (str.trim().length()<=0) {
        	    		continue;
        	    	}
        	    	if (str.trim().charAt(0)=='#') {  // get rid of comment line beginning with #
        	    		continue; 
        	    	}        	    	
        	 	    if (strategy.parse(str)) {
        	 		   strategy.monitor();    		
        		    }
        	    }
        	    in.close();
        	} catch (IOException e) {
        		log.error("Exception in reading OPT stock scanning output file:{}",e);
        	} catch (Exception e) {
        		log.error("Exception in reading OPT stock scanning output file:{}",e);
        	}
    		}  // of else location not empty
    	}  // of while map securityLocations
    	// }  
    	
//    	for(Iterator it = backtest_data.keySet().iterator(); it.hasNext();) {
//    		String backtestString = (String) it.next();
//    		String datafile = backtest_data.get(backtestString);
//    		
//    		BacktestProcess bt = (BacktestProcess) factory.getBean(backtestString);    
//	    	System.out.println(bt + " = " + datafile);
// 	    	bt.process(datafile);    		
//    	}  // of while map backtest_data
    	
    }  // of run
    
	public void setSecurityLocations(Map<String, String> securityLocations) {
		this.securityLocations = securityLocations;
	}

	public Map<String, String> getSecurityLocations() {
		return securityLocations;
	}
	
	public Map<String, String> getBacktest_data() {
		return backtest_data;
	}

	public void setBacktest_data(Map<String, String> backtest_data) {
		this.backtest_data = backtest_data;
	}

}
