package cot;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.JScrollPane;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.jfree.ui.RefineryUtilities;

public class CotFrame extends JFrame {

	private JPanel contentPane;
	
	CotGraph[] cds = new CotGraph[3];
	int count = 0; 
	

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					CotFrame frame = new CotFrame();
//					frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the frame.
	 */
	public CotFrame() {
		setTitle("COT Commercial");
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 262, 370);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 242, 343);
		contentPane.add(scrollPane);
		
		final JList list = new JList();
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if( (arg0.getSource() == list) && !arg0.getValueIsAdjusting() )	{
					
					String commodity = (String)list.getSelectedValue();
					
					implGraphicCot(commodity); 
					//System.out.println(stringValue);
				}
			}
		});
		scrollPane.setViewportView(list); // here
		list.setModel(new AbstractListModel() {
			String[] values=CotUtil.symbols; 
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		list.setVisibleRowCount(88);
		list.setValueIsAdjusting(true);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	private void implGraphicCot(String commodity) {
		final int winCount = 2; 
		CotGraph cd1 = cds[count%winCount];
	    if (cd1!=null) {
	    	cds[count%winCount].dispose(); 
	    	cds[count%winCount] = null;
	    }
	    
	    if (commodity.startsWith("-")) return;   // delimit symbol
	    if ("exit".equals(commodity)) return;	
	    CotDataSource cotDataSource = new CotDataSource("cot commercial", commodity);
	    // above is preparation. 
	    
	    cd1 = new CotGraph(cotDataSource, commodity, CotUtil.Cot_Show.Value); 
	    cds[count%winCount] = cd1;
		cd1.pack();
		RefineryUtilities.positionFrameOnScreen(cd1, 0.8, 0.8); // near bottom right. 
		// RefineryUtilities.centerFrameOnScreen(cd1);
		count++;
		
		// another window
		CotGraph cd2 = cds[count%winCount];
	    if (cd2!=null) {
	    	cds[count%winCount].dispose(); 
	    	cds[count%winCount] = null;
	    }
	    
	    cd2 = new CotGraph(cotDataSource, commodity, CotUtil.Cot_Show.Percentage); 
	    cds[count%winCount] = cd2;
		cd2.pack();
		//put the lefttop. 
		count++;
		
		cd1.setVisible(true);
		cd2.setVisible(true);
	}
}
