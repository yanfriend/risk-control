import java.util.Scanner;

import spyder.RangeFadeStrategy;

import common.DataEngine;
import common.Strategy;


public class MainFrame {
	
	public static void main(String[] args) throws InterruptedException {
		DataEngine de = new DataEngine(3); 
		Thread.sleep(2000);  
		
		Strategy strategy = new RangeFadeStrategy(); 
		strategy.setDataEngine(de); 
		
		String str = ""; 
 	    if (strategy.parse(str)) {
	 		   strategy.monitor();    		
		    }
		System.out.println("finshed");
		
//		String inFileName = "";
//		if ((args.length>=1)&&(!args[0].isEmpty())) inFileName = args[0]; 
//	    String outFileName = "";
//	    if ((args.length>=2)&&(!args[1].isEmpty())) outFileName = args[1]; 

//        System.out.println("This is the main menu");
//        System.out.println("1. euro cycle(short date format)");
//        System.out.println("2. wheat cycle(long date format, complete contract unit)");
//        Scanner reader = new Scanner(System.in);
//        int iChoice = reader.nextInt();
		
//        int iChoice = 2; 
//        
//        switch(iChoice) {
//        	case 1: euroCycle(inFileName, outFileName); break; 
//        	case 2: wheatCycle(inFileName, outFileName); break; 
//        
//        }
		
	}
	
	static void wheatCycle(String inFileName, String outFileName) {
		if ("".equals(inFileName)) inFileName = "D:\\CommonData\\Stocks\\cycleData\\CWheat0.csv"; 
		if ("".equals(outFileName)) outFileName = "D:\\CommonData\\Stocks\\cycleData\\cycle_cwheat0.csv"; 
	    
		CompletePriceCycle pc = new CompletePriceCycle(); 
		pc.check(inFileName, outFileName); 
	}	

	static void euroCycle(String inFileName, String outFileName) {
		if ("".equals(inFileName)) inFileName = "D:\\CommonData\\Stocks\\cycleData\\euro_from99.csv"; 
		if ("".equals(outFileName)) outFileName = "D:\\CommonData\\Stocks\\cycleData\\cycle_euro.csv"; 
		
		CompactPriceCycle pc = new CompactPriceCycle(); 
		pc.check(inFileName, outFileName); 
	}

}

