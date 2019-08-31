package SlidingXYDataset;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import junit.framework.TestCase;

public class SlidingXYDatasetTests extends TestCase {

  public static final int WINDOW = 3;
  public static final int FIRST = 0;

  SlidingXYDataset dataset = createDataset();

 
  public void testGetSeriesCount() {
    assertEquals(3, dataset.getSeriesCount());
  }

  public void testGetXValue() {
    int series = 1;
    int item =0;
    assertEquals(1.0, dataset.getXValue(series, item), 0.0);
   
  }

  public void testGetYValue() {
    int series = 1;
    int item =0;
    assertEquals(6.0, dataset.getYValue(series, item), 0.0);
  }

  public void testGetItemCount() {
    
    //firstItemIndex=0
    assertEquals(0, dataset.getItemCount(0));
    assertEquals(3, dataset.getItemCount(1));
    assertEquals(1, dataset.getItemCount(2));
    
    dataset.setFirstItemIndex(1);
    assertEquals(0, dataset.getItemCount(0));
    assertEquals(3, dataset.getItemCount(1));
    assertEquals(2, dataset.getItemCount(2));
    
    dataset.setFirstItemIndex(4);
    assertEquals(2, dataset.getItemCount(0));
    assertEquals(0, dataset.getItemCount(1));
    assertEquals(0, dataset.getItemCount(2));
    
    
  }

  private SlidingXYDataset createDataset() {

    final XYSeries series1 = new XYSeries("0");
    series1.add(6.0, 7.0);
    series1.add(7.0, 5.0);
    series1.add(8.0, 7.0);
    series1.add(9.0, 5.0);
    series1.add(10.0, 7.0);
   

    final XYSeries series2 = new XYSeries("1");
    series2.add(1.0, 6.0);
    series2.add(2.0, 7.0);
    series2.add(3.0, 5.5);

    final XYSeries series3 = new XYSeries("2");

    series3.add(3.0, 4.0);
    series3.add(4.0, 3.0);
    

    final XYSeriesCollection dc = new XYSeriesCollection();
    dc.addSeries(series1);
    dc.addSeries(series2);
    dc.addSeries(series3);
    this.dataset = new SlidingXYDataset(dc, FIRST, WINDOW);
    return dataset; 
  }

}
