package spyder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import common.DataEngine;
import common.OrderEngine;
import common.Strategy;

public class OPTStrategyWrap
{
// this is a wrap for OPTStrategy
	
	// use json to parse data
	// for each main symbol, create one OPTStrategy obj
	static public boolean parseWrap(String location, DataEngine dataEngine, OrderEngine orderEngine) {
		
		JSONParser parser = new JSONParser();	 
		try {
			Object obj = parser.parse(new FileReader(location));
			JSONArray jsonObject = (JSONArray) obj;

			Iterator<JSONObject> iterator = jsonObject.iterator();
			while (iterator.hasNext()) {
				try {
				JSONObject jObj = iterator.next(); 
				
				JSONObject monitoringObject = (JSONObject) jObj.get("monitoringObject"); 
				String monitoringSymbol = (String) monitoringObject.get("symbol"); 
				String monitoringMultiplier = (String) monitoringObject.get("multiplier");
				String monitoringType = (String) monitoringObject.get("type");
				String monitoringExpiry = (String) monitoringObject.get("expiry");
				String monitoringExchange = (String) monitoringObject.get("exchange");
				String currency = (String) monitoringObject.get("currency");
				
				boolean useRTH = (Boolean) monitoringObject.get("useRTH");
				String compareMode = (String) monitoringObject.get("compareMode");
				float priceThreshold = ((Double) monitoringObject.get("priceThreshold")).floatValue();
				
				
				JSONObject tradingObject = (JSONObject) jObj.get("tradingObject"); 
				String tradingSymbol = (String) tradingObject.get("symbol"); 
				String tradingMultiplier = (String) tradingObject.get("multiplier");
				String tradingType = (String) tradingObject.get("type");
				String tradingExpiry = (String) tradingObject.get("expiry");
				String tradingExchange = (String) tradingObject.get("exchange");
				double tradingStrike = (Double) tradingObject.get("strike");
				String tradingRight = (String) tradingObject.get("right");
				String tradingLocal = (String) tradingObject.get("localSymbol");
				int tradingAmount = ((Long) tradingObject.get("amount")).intValue();
				String tradingAction = (String) tradingObject.get("action");
				
				double miniTickThreshold = (Double) tradingObject.get("miniTickThreshold");
				double bigStep = (Double) tradingObject.get("bigStep");
				double smallStep = (Double) tradingObject.get("smallStep");
				
				String priceMode = (String) tradingObject.get("priceMode");
				
				// double miniTick = (Double) jObj.get("miniTick");  // throw exceptions since not exists. 
				
				// create an OPTStrategy for each trading pair. 
				OPTStrategy optStrategy = new OPTStrategy(); 
				optStrategy.setDataEngine(dataEngine); 
				optStrategy.setOrderEngine(orderEngine); 
				optStrategy.setTesting(false);
				optStrategy.setAction(tradingAction);
				optStrategy.setOrderAmount(tradingAmount);
				
				optStrategy.monitoringSecurity.setCurrency(currency);
				optStrategy.monitoringSecurity.setExchange(monitoringExchange);
				optStrategy.monitoringSecurity.setExpireDate(monitoringExpiry);
				optStrategy.monitoringSecurity.setMultiplier(monitoringMultiplier);			
				optStrategy.monitoringSecurity.setSecurityType(monitoringType);
				optStrategy.monitoringSecurity.setSymbol(monitoringSymbol);
				
				optStrategy.setUseRTH(useRTH);
				optStrategy.setTriggerMode(compareMode);  // >=
				optStrategy.setTriggerPrice(priceThreshold);
				optStrategy.setPriceMode(priceMode);  // MIDPOINT
				optStrategy.setMiniTickThreshold(miniTickThreshold);
				optStrategy.setBigStep(bigStep);
				optStrategy.setSmallStep(smallStep);				
				
				optStrategy.setSymbol(tradingSymbol);
				optStrategy.contract.m_symbol = tradingSymbol;
				optStrategy.contract.m_multiplier = tradingMultiplier;
				optStrategy.contract.m_secType = tradingType; 
				optStrategy.contract.m_expiry = tradingExpiry; 
				optStrategy.contract.m_exchange = tradingExchange; 
				optStrategy.contract.m_strike = tradingStrike;
				optStrategy.contract.m_right = tradingRight;
				optStrategy.contract.m_localSymbol = tradingLocal; 
				
				
				Strategy strategy = optStrategy; 
				
				strategy.monitor();    		
				
			}  catch (NullPointerException e) {
				e.printStackTrace();
			}
				
			}  // of loop
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}	
		
		return true; 
	}
	

}