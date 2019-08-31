import java.awt.EventQueue;
import java.util.Date;
import java.util.Scanner;

import cycle.ForexPrices;
import cycle.FuturesPrices;

import common.FuturesQuotes;
import cot.CotFrame;
import cot.CotGraph;
import download.Download;

public class MainFrame {
	
	public static void main(String[] args) {
		Scanner readerInt = new Scanner(System.in);
		int iChoice = -1;

		do {
		String inFile = "";
		String outFile = "";
//		if ((args.length>=1)&&(!args[0].isEmpty())) inFileName = args[0]; 
//	    if ((args.length>=2)&&(!args[1].isEmpty())) outFileName = args[1]; 

        System.out.println("This is the main menu");
        System.out.println("1. forex cycle");
        System.out.println("2. futures cycle(long date format, complete contract unit), clean up last month data");
        System.out.println();
        System.out.println("3. Simu trades between two dates on a specific underlying futures");
	    System.out.println("4. Simu trades between two dates on a specific forex");
        System.out.println();
	    System.out.println("5, futures weekday pattern");
        System.out.println();
	    System.out.println("6, financial/commodity commercial COT/OI percentages and values");
        System.out.println();
        System.out.println("8, download data");
        
        System.out.println("");
        //System.out.println("9, COT commercial stoch values");
        
        iChoice = readerInt.nextInt();
        if (iChoice==0) break; 
//        int iChoice = 6; 

        Scanner reader = new Scanner(System.in);
        String tmpin = "";
        if ((iChoice!=6)&&(iChoice!=7)) {
			inFile="D:\\CommonData\\Stocks\\cycleData\\CWheat2.csv";
		    System.out.println("please input the location of data file as D:\\\\CommonData\\\\Stocks\\\\cycleData\\\\CWheat2.csv:(.for keep..)");
		    tmpin = reader.next();
		    inFile = (".".equals(tmpin))?inFile:tmpin;  
        }
        
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
        	case 5: weekdayPattern(inFile, iChoice); break;
        	case 6: graphicCot(); break; 
        	//case 7: graphicCot(CotUtil.Cot_Show.Value); break; 
        	case 8: new Download().menu(); break; 
        	
        	//case 9: cotStoch(inFile, iChoice); break; 
        	
        }
        reader.close();
		} while (true);
		readerInt.close();
	}
	
	private static void graphicCot() {

		EventQueue.invokeLater(new Runnable() {
		public void run() {
			try {
				CotFrame frame = new CotFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	});

	}
	
	private static void cotStoch(String inFile, int choice) {  // not in use, deprecated
		String[] inFiles = new String[2];
		inFiles[0] = inFiles[1] = ""; 
		inFiles[0] = inFile;
		Scanner iSelect = new Scanner(System.in);

//		System.out.println("please input the location of this year data file as D:\\\\CommonData\\\\Stocks\\\\COT\\\\*.zip");
//	    String tmpin = iSelect.nextLine();
//		inFiles[1] = tmpin; 
		
	    System.out.println("please input line token:");    // token definition in CｏｔＧｒａｐｈ．ｊａｖａ．　
	    String commodity = iSelect.nextLine();
	    iSelect.close(); 
	    	
	    CotGraph cd1 = null, cd2 = null;
	    //if (choice==6) {
		    inFiles[0] = "D:\\CommonData\\Stocks\\COT\\com_disagg_txt_hist_2006_2011.zip";  //  for test for convenience input.
		    inFiles[1] = "D:\\CommonData\\Stocks\\COT\\com_disagg_txt_2012.zip";
		    // cd1 = new CommodityCotGraph("cot test", commodity, CotUtil.Cot_Show.Value);
	    //} else if (choice==7) {
//		    inFiles[0] = "D:\\CommonData\\Stocks\\COT\\com_fin_txt_2006_2011.zip";  //  for test for convenience input.
//		    inFiles[1] = "D:\\CommonData\\Stocks\\COT\\com_fin_txt_2012.zip";
//		    cd2 = new FinancialCotGraph("cot test", inFiles, commodity);
	    //}
		    
		cd1.commercialStoch(); 
		//cd2.commercialStoch();     
		
		
//		cd.pack();
//		RefineryUtilities.centerFrameOnScreen(cd);
//		cd.setVisible(true);
	}
	
	private static void weekdayPattern(String inFile, int choice) {
		int startYear = 1983, endYear = 2012, startMonth = 0, endMonth = 11, startDay = 1, endDay = 31; 
	    
		Scanner iSelect = new Scanner(System.in);
	    System.out.println("please input start year:");    
	    startYear = iSelect.nextInt();
	    System.out.println("please input end year:");
	    endYear = iSelect.nextInt();
	    iSelect.close(); 

	    Date startDate = new Date(startYear-1900, startMonth, startDay, 0,0,0); 
	    Date endDate = new Date(endYear-1900, endMonth, endDay);

	    FuturesQuotes fq = new FuturesQuotes(); 
	    fq.checkWeekdayPattern(inFile, startDate, endDate);
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

