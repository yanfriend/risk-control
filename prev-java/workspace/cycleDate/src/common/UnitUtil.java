package common;


public class UnitUtil {
	
	static StoreUnit[] storeUnits = { 
		new StoreUnit("coffee2","D:\\CommonData\\Stocks\\cycleData\\coffee2.csv","http://wikiposit.org/w.pl?action=dl&dltypes=comma%20separated&sp=daily&uid=FUTURE.KC2"),
		new StoreUnit("feeder cattle","","")
		}; 
	
	public static StoreUnit getStoreUnit(String sym) {
		for (int i=0; i<storeUnits.length; i++) {
			if (storeUnits[i].symbol.contains(sym)) return storeUnits[i];
		}
		return null;
	}

}
