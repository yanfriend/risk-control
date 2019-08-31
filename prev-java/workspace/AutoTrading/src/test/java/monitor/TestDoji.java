package monitor;

import org.junit.After;
import org.junit.Before;

import common.Bar;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import java.util.Map.Entry;

public class TestDoji //extends TestCase
{

	Doji doji = new Doji();
	
    public TestDoji()
    {
    }

    @Before
    public void setUp() {
    }
 
    @After
    public void tearDown() {
        doji.barMap.clear();
    }
    
    @Test
    public void testApp()
    {
    	// set up for tests. 
    	for (int i=0; i<50; i++) {
	    	long barKey = i; 
	    	long datelong = i*1000;
	    	double open = 10;
	    	double close = 12;
	    	double high = 13;
	    	double low = 9;  // 10, 13, 12, 9
	    	int volume = 100;
	    	
	    	Bar bar = doji.barMap.get(barKey); 
	    	if (bar == null) {
	    		bar = new Bar(datelong, open, high, low, close, volume); 
	    	} else {
	    		bar.setDate(datelong);
	    		bar.setOpen(open);
	    		bar.setHigh(high);
	    		bar.setLow(low);
	    		bar.setClose(close);
	    		bar.setVolume(volume);
	    	}
	    	doji.barMap.put(barKey, bar); 
    	}
    	// change last value to test different results.
    	assertEquals("should exit(1) when last bar is not a doji", 1, doji.checkStrategy());        
    	
    	Entry<Long, Bar> last_entry = doji.barMap.lastEntry();
    	Bar bar = last_entry.getValue();
    	bar.setOpen(10);
    	bar.setHigh(13);
    	bar.setLow(9);
    	bar.setClose(10.1);
    	assertEquals("should exit(3) when last bar is a doji but the range is small", 3, doji.checkStrategy());        
    	
    	bar = last_entry.getValue();
    	bar.setOpen(10);
    	bar.setHigh(13);
    	bar.setLow(8);
    	bar.setClose(10.1);
    	assertEquals("should exit(0) when last bar is a doji but the range > 1.1*ATR", 0, doji.checkStrategy());        
    }
}
