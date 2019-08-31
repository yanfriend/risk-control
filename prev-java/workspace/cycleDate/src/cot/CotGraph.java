package cot;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;

 
public /*abstract*/ class CotGraph extends ApplicationFrame {
	
  static int count = 1;
  public static int window = 300;
  public static final int FIRST = 0;
  
  String title; 

  TimeSeriesCollection tsc = new TimeSeriesCollection();
		  
  TimeSeries ts1; // = new TimeSeries("OI", Day.class);

  // below 2 are for commodity usage
  TimeSeries ts2; // = new TimeSeries("Swap", Day.class);
  TimeSeries ts3; // = new TimeSeries("Prod", Day.class);
  TimeSeries ts5; // = new TimeSeries("MMoney", Day.class);
  
  TimeSeries ts4; // = new TimeSeries("Commercial", Day.class);  // common one for both
  TimeSeries ts6; // = new TimeSeries("Comm/OI", Day.class);  // to show commercial/OI
  
  // below 3 are for financial usage
//  TimeSeries dealerts = new TimeSeries("Dealer", Day.class);
//  TimeSeries assetts = new TimeSeries("AssetMgr", Day.class);
//  TimeSeries moneyts = new TimeSeries("LevMoney", Day.class);
  
  class DemoPanel extends JPanel implements ChangeListener {

    private ChartPanel chartPanel;
    private JFreeChart chart;
    private JSlider slider;
    private SlidingXYDataset dataset;

    public DemoPanel() {
      super(new BorderLayout());
      this.chart = createChart();
      this.chartPanel = new ChartPanel(this.chart);
      this.chartPanel.setPreferredSize(new java.awt.Dimension(1000, 600));
      this.chartPanel.setMouseZoomable(true, false);  // ?
      add(this.chartPanel);
      JPanel dashboard = new JPanel(new BorderLayout());
      // this.slider = new JSlider(0, count - window - 1, 0);
      this.slider = new JSlider(0, count - window/4 - 1, 0);
      slider.setPaintLabels(true);
      slider.setPaintTicks(true);
      slider.setMajorTickSpacing(window);
      this.slider.addChangeListener(this);
      dashboard.add(this.slider);
      add(dashboard, BorderLayout.SOUTH);
    }

    private JFreeChart createChart() {

      XYDataset dataset1 = createDataset("Random 1", 100.0, new Day(), count);
    	
      JFreeChart chart1 = ChartFactory.createTimeSeriesChart(title, "x axes", "y axes", dataset1, true, true, false);         
      XYPlot plot = chart1.getXYPlot();
      plot.setDomainCrosshairVisible(true);
      DateAxis xaxis = (DateAxis) plot.getDomainAxis();
      xaxis.setAutoRange(true);
      return chart1;
    }


    private XYDataset createDataset(String name, double base, RegularTimePeriod start, int count) {
      // TimeSeriesCollection tsc = getTSC(); 
      // getTSC();
      dataset = new SlidingXYDataset(tsc, FIRST, window);
      return dataset;
    }


    public void stateChanged(ChangeEvent event) {
      int value = this.slider.getValue();
      this.dataset.setFirstItemIndex(value);  
    }
  }

public CotGraph(CotDataSource cds, String commodity, CotUtil.Cot_Show cs) {
	super(commodity); 
	this.count = cds.count;
	this.window = cds.window;
	
	if (CotUtil.isFinancial(commodity)) {
		this.ts1 = cds.OIts;
		this.ts2 = cds.dealerts;
		this.ts3 = cds.assetts;
		this.ts4 = cds.commercials;
		this.ts5 = cds.moneyts; 
		this.ts6 = cds.commPer;
			if (cs==CotUtil.Cot_Show.Value) {
			    tsc.addSeries(ts1);
			    tsc.addSeries(ts2);
			    tsc.addSeries(ts3);
			    tsc.addSeries(ts4);
			    tsc.addSeries(ts5);
			} else 
				tsc.addSeries(ts6);   
	} else {
		this.ts1 = cds.OIts;
		this.ts2 = cds.swapts;
		this.ts3 = cds.prodts;
		this.ts4 = cds.commercials;
		this.ts5 = cds.mmoney; 
		this.ts6 = cds.commPer;
		if (cs==CotUtil.Cot_Show.Value) {
		    tsc.addSeries(ts1);
		    tsc.addSeries(ts3);
		    tsc.addSeries(ts2);
		    tsc.addSeries(ts4);
		    tsc.addSeries(ts5);   
		 } else 
			tsc.addSeries(ts6);    
	}
	this.title = commodity; 
	
	setContentPane(new DemoPanel());  // all data must be ready before calling new DemoPanel!  
} 


// below deprecated
public void commercialStoch() {
	// comm/OI, 26 week stochastic, return current value? 
	int countComm = ts4.getItemCount();
//	Double commTmp = (Double) commercials.getValue(countComm-1);
//	
	int countOI = ts1.getItemCount(); 
//	Double OItmp = (Double) OIts.getValue(countOI-1);
	// Double OItmp2 = (Double) OIts.getValue(countOI-2);
	
	if (countComm!=countOI) System.out.println("Warning: count commercial is not equal to count OI");
	
	List commPerList = new ArrayList(); 
	int lookbackPeriod = 26; 
	for (int i=lookbackPeriod; i>0; i--) {
		double commPer = (Double) ts4.getValue(countComm-i)/(Double) ts1.getValue(countOI-i);
		commPerList.add(commPer);
	}
	
	double max = -100, min = 100;
	for (int i=lookbackPeriod-1; i>=0; i--) {
		if (((Double) commPerList.get(i)).doubleValue() >max) max = (Double) commPerList.get(i);
		if (((Double) commPerList.get(i)).doubleValue() <min) min = (Double) commPerList.get(i);
	}
	System.out.println("Last 26 weeks, max percentage:"+ max + ", min:"+min);
	
	for (int i=2; i>=0; i--) {
		double per = (((Double) commPerList.get(i)).doubleValue()-min)/(max-min);
		System.out.println("stoch values:"+ per);
	}
	
//	System.out.println("commercial:"+ commTmp + ", oi:"+OItmp +" , pre OI:"+ OItmp2);
	
}

 
 
}