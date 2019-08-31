import java.util.Scanner;

public class MainFrame {
	
	public static void main(String[] args) {
		String inFile = "";
		String outFile = "";
//		if ((args.length>=1)&&(!args[0].isEmpty())) inFileName = args[0]; 
//	    if ((args.length>=2)&&(!args[1].isEmpty())) outFileName = args[1]; 

        System.out.println("This is the main menu");
        System.out.println("1. forex cycle");
        System.out.println("2. futures cycle(long date format, complete contract unit), clean up last month data");
        System.out.println("3. Simu trades between two dates on a specific underlying futures");
	    System.out.println("4. Simu trades between two dates on a specific forex");
        Scanner reader = new Scanner(System.in);
        int iChoice = reader.nextInt();
//        int iChoice = 4; 

		inFile="D:\\CommonData\\Stocks\\cycleData\\CWheat2.csv";
	    System.out.println("please input the location of data file as D:\\\\CommonData\\\\Stocks\\\\cycleData\\\\CWheat2.csv:(.for keep..)");
	    String tmpin = reader.next();
	    inFile = (".".equals(tmpin))?inFile:tmpin;  
        
        switch(iChoice) {
        	case 1: 
        		outFile="D:\\CommonData\\Stocks\\cycleData\\cycle_CWheat2.csv";
        	    System.out.println("please input the location of data file as D:\\\\CommonData\\\\Stocks\\\\cycleData\\\\cycle_CWheat2.csv:(.for keep..)");
        	    tmpin = reader.next();
        	    outFile = (".".equals(tmpin))?outFile:tmpin;  
        		fxCycle(inFile, outFile); break; 
        	case 2: 
        		outFile="D:\\CommonData\\Stocks\\cycleData\\cycle_CWheat2.csv";
        	    System.out.println("please input the location of data file as D:\\\\CommonData\\\\Stocks\\\\cycleData\\\\cycle_CWheat2.csv:(.for keep..)");
        	    tmpin = reader.next();
        	    outFile = (".".equals(tmpin))?outFile:tmpin;  
        		futuresCycle(inFile, outFile); break; 
        	case 3: simuTradesImpl(iChoice, inFile); break;
        	case 4: simuTradesImpl(iChoice, inFile); break;
        }
		reader.close();
	}
	
	private static void simuTradesImpl(int choice, String inFile) {
		Scanner iSelect = new Scanner(System.in);
		int startMonth = 0, endMonth = 0, startDay = 0, endDay = 0; 
	    
	    System.out.println("please input start month(1-12):");   // dont forget to minus 1
	    startMonth = iSelect.nextInt();
	    System.out.println("please input start trading day number:");    
	    startDay = iSelect.nextInt();
	    
	    System.out.println("please input end month(1-12):");
	    endMonth = iSelect.nextInt();
	    System.out.println("please input end trading day number:");   // dont forget to minus 1
	    endDay = iSelect.nextInt();
	    
	    iSelect.close(); 
	    
	    if (choice ==3) 
	    	simuFuturesTrade(inFile,startMonth, startDay, endMonth, endDay);	    
	    else if(choice==4) {
	    	simuForexTrade(inFile,startMonth, startDay, endMonth, endDay);
	    } 

	}

	private static void simuFuturesTrade(String inFile, int startMonth, int startDay, int endMonth,
			int endDay) {
		// output format: entry date, price, interim high, low, exit date, price, gross result, cumulative net result. 
		FuturesPrices fp = new FuturesPrices(); 
		fp.simuTrade(inFile, startMonth, startDay, endMonth, endDay); 
	}

	private static void simuForexTrade(String inFile, int startMonth,
			int startDay, int endMonth, int endDay) {
		ForexPrices fp = new ForexPrices(); 
		fp.simuTrade(inFile, startMonth, startDay, endMonth, endDay); 
	}

	static void futuresCycle(String inFileName, String outFileName) {
		FuturesPrices fp = new FuturesPrices(); 
		fp.checkCycle(inFileName, outFileName); 
	}	

	static void fxCycle(String inFileName, String outFileName) {
		ForexPrices fp = new ForexPrices(); 
		fp.checkCycle(inFileName, outFileName); 
	}	
	
}

