package common;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
	private Map<String, String> backtest_data; 
	static Logger log = LoggerFactory.getLogger(AutoTradeMain.class);

	public static void main (String[] args)
    {   
		Resource resource = new ClassPathResource("beans.xml");
    	XmlBeanFactory factory = new XmlBeanFactory(resource);
    	
    	AutoTradeMain autoTradeMain = new AutoTradeMain();

    	int options = 3;
    	boolean[] chosen = new boolean[options];
    	for (int i=0; i<options; i++) { chosen[i]=false; }
    	int iChoice; 

    	//while (true) {    		
//            System.out.println("This is the main menu");
//            System.out.println("1. real time trading, account monitor or download intraday data");
//            System.out.println("2. test with downloaded data");
//            Scanner reader = new Scanner(System.in);
            iChoice = 1;//reader.nextInt();
//            
//            if (chosen[iChoice]) {
//            	//log.info("{} has chosen and running, pls continue.", iChoice); continue; 
//            }
//            chosen[iChoice] = true; 

            autoTradeMain.run(factory, iChoice);
    		
    	//}
    }
    	
    void run(XmlBeanFactory factory, int option) {
    	switch(option) {
    	case 1:
    		String location = "D:\\CommonData\\Stocks\\allstrategies.json";
    		try {
                FileReader reader = new FileReader(location);

                JSONParser jsonParser = new JSONParser();
                JSONArray units = (JSONArray) jsonParser.parse(reader); // get an array, not object in this case

				for(int i=0; i<units.size(); i++){
					JSONObject jobj = (JSONObject) units.get(i);
					boolean valid = (Boolean) jobj.get("valid");
					if (!valid) continue; 
					String strategyString = (String) jobj.get("strategy");
					Strategy strategy = (Strategy) factory.getBean(strategyString); 
					
					String str = jobj.toString();
        	 	    strategy.work(str);
        	    }
        	    reader.close();
        	} catch (IOException e) {
        		log.error("Exception in reading OPT stock scanning output file:" + e.getMessage());
        	} catch (Exception e) {
        		log.error("Exception in reading OPT stock scanning output file:" + e.getMessage());
        	}
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
    
	
	public Map<String, String> getBacktest_data() {
		return backtest_data;
	}
	public void setBacktest_data(Map<String, String> backtest_data) {
		this.backtest_data = backtest_data;
	}


}
