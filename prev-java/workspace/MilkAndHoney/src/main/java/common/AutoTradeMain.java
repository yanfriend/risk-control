package common;
 
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;


import old.monitor.PriceMonitorStrategy;

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
	
	// private PriceMonitorStrategy accountMonitor; 
	
	static Logger log = LoggerFactory.getLogger(AutoTradeMain.class);

	public static void main (String[] args)
    {   
	
    	// Resource resource = new ClassPathResource("HE" + File.separator + "beans.xml");
		Resource resource = new ClassPathResource("beans.xml");
    	XmlBeanFactory factory = new XmlBeanFactory(resource);
    	

    	AutoTradeMain autoTradeMain;
//    	autoTradeMain = (AutoTradeMain) factory.getBean("autoTradeMain");
//    	autoTradeMain.run(factory);
//		
//    	System.out.println("finish" );

    	int options = 3;
    	boolean[] chosen = new boolean[options];
    	for (int i=0; i<options; i++) { chosen[i]=false; }
    	int iChoice; 

    	while (true) {    		
            System.out.println("This is the main menu");
            System.out.println("1. real time trading, account monitor or download intraday data");
            System.out.println("2. test with downloaded data");
            Scanner reader = new Scanner(System.in);
            iChoice = reader.nextInt();
            
            if (chosen[iChoice]) {
            	log.info("{} has chosen and running, pls continue.", iChoice); continue; 
            }
            chosen[iChoice] = true; 
            
            //switch(iChoice) {
        	//case 1: 
            	autoTradeMain = (AutoTradeMain) factory.getBean("autoTradeMain");
            	autoTradeMain.run(factory, iChoice);
            	//break; 
        	//case 2: 
        		//System.out.println("this is case 2 running. "); 
        		//break;
            //}
    		
    	}
    }
    	
    void run(XmlBeanFactory factory, int option) {
    	switch(option) {
    	case 1:
//		if (accountMonitor != null) {
//			accountMonitor.monitor(); 
//		}
		
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
    	System.out.println("finish option 1" );
    	break;
    	
    	case 2: 
    	for(Iterator it = backtest_data.keySet().iterator(); it.hasNext();) {
    		String backtestString = (String) it.next();
    		String datafile = backtest_data.get(backtestString);
    		
    		BacktestProcess bt = (BacktestProcess) factory.getBean(backtestString);    
	    	System.out.println(bt + " = " + datafile);
 	    	bt.process(datafile);    		
    	}  // of while map backtest_data
    	System.out.println("finish option 2" );
    	break; 
    	}  // of switch
    }  // of run method
    
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

//	public PriceMonitorStrategy getAccountMonitor() {
//		return accountMonitor;
//	}
//
//	public void setAccountMonitor(PriceMonitorStrategy accountMonitor) {
//		this.accountMonitor = accountMonitor;
//	}

}
