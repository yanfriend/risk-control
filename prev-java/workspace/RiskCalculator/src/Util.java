import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Util {

	static List load() {
		List<RiskUnit> list = new ArrayList(); 
		
		JSONParser parser = new JSONParser();	 
		try {
			Object obj = parser.parse(new FileReader("D:\\CommonData\\Stocks\\config\\risk.json"));
			JSONArray jsonObject = (JSONArray) obj;

			Iterator<JSONObject> iterator = jsonObject.iterator();
			while (iterator.hasNext()) {
				try {
				JSONObject jObj = iterator.next(); 
				
				String symbol = (String) jObj.get("symbol"); 
				double miniTick = (Double) jObj.get("miniTick"); 
				long multiplier = (Long) jObj.get("multiplier");
				// System.out.println(symbol + " " + miniTick);
				
				RiskUnit ru = new RiskUnit(); 
				ru.symbol = symbol; 
				ru.miniTick = miniTick; 
				ru.tickValue = (float) (miniTick * multiplier); 
				
				list.add(ru);
				
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
	 
		return list; 
	}
	
	// save is for debug and test only
	static void save() {
		JSONObject obj1 = new JSONObject();
		obj1.put("symbol", "GF");
		obj1.put("multiplier", new Integer(50000));
		obj1.put("miniTick", new Double(.00025));
		obj1.put("tickValue", new Float(.00025 * 50000));
		
		JSONObject insideObj = new JSONObject();
		insideObj.put("symbol", "IBM");
		insideObj.put("multiplier", new Integer(100));
		
		JSONObject obj2 = new JSONObject();
		obj2.put("symbol", "name 2");
		obj2.put("multiplier", new Integer(200));
		obj2.put("insideObj", insideObj);  // object in object. 
		
		JSONArray list = new JSONArray();
		list.add(obj1); 
		list.add(obj2);
		
		try {
			 
			FileWriter file = new FileWriter("D:\\CommonData\\Stocks\\111.json");
			file.write(list.toJSONString());
			file.flush();
			file.close();
	 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
