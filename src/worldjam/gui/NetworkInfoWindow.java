package worldjam.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import worldjam.exe.Client;
import worldjam.net.NetworkUtils;

import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JTextArea;

public class NetworkInfoWindow extends JFrame{

	private Client client;

	public NetworkInfoWindow(Client client) {
		this.client = client;
		setTitle("WorldJam: Network Info");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addTab("Network Interfaces", createNetworksPanel());
		tabbedPane.addTab("Session", createSessionPanel(client));
		tabbedPane.addTab("I/O Rates", createDataRatePanel());
		this.setSize(700, 500);
		this.setVisible(true);
	}

	private Component createSessionPanel(Client client2) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		JTextArea textArea = new JTextArea();
		textArea.setAutoscrolls(true);
		JScrollPane jsp = new JScrollPane(textArea);
		panel.add(jsp, BorderLayout.CENTER);

		String info = client.getFormattedSessionStatusString();

		textArea.setText(info);
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener((e)->{
			new Thread(()->{
				textArea.setText(client.getFormattedSessionStatusString());
			}).start();
		});
		panel.add(refresh,BorderLayout.SOUTH);
		return panel;
	}

	private Component createNetworksPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));

		JCheckBox chckbxNewCheckBox = new JCheckBox("Show All");
		chckbxNewCheckBox.setToolTipText("Display addresses that are loopback or link-local (which are by default hidden)");
		JPanel panel2 = new JPanel();
		panel2.add(chckbxNewCheckBox);
		JButton buttonRefresh = new JButton("Refresh");


		panel2.add(buttonRefresh);
		panel.add(panel2, BorderLayout.SOUTH);

		JTextArea textArea = new JTextArea();
		textArea.setAutoscrolls(true);
		JScrollPane jsp = new JScrollPane(textArea);
		panel.add(jsp, BorderLayout.CENTER);
		ActionListener a = (e)->{
			new Thread(()->{
				try {
					textArea.setText(NetworkUtils.getNetworkInterfaceInfo(!chckbxNewCheckBox.isSelected()));
				} catch (SocketException e1) {
					e1.printStackTrace();
				}
			}).start();
		};
		a.actionPerformed(null);

		chckbxNewCheckBox.addActionListener(a);
		buttonRefresh.addActionListener(a);
		return panel;
	}

	/*private Component createDataRatePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel2 = new JPanel();
		JButton buttonRefresh = new JButton("Refresh");


		panel2.add(buttonRefresh);
		panel.add(panel2, BorderLayout.SOUTH);

		JTextArea textArea = new JTextArea();
		textArea.setAutoscrolls(true);
		JScrollPane jsp = new JScrollPane(textArea);
		panel.add(jsp, BorderLayout.CENTER);
		ActionListener a = (e)->{
			new Thread(()->{
				textArea.setText(String.format("Input: %.0f kBps\nOutput: %.0f kBps",
						client.sampleInputByteRate(1000)/1000.,
						client.sampleOutputByteRate(1000)/1000.));
			}).start();

		};
		a.actionPerformed(null);
		buttonRefresh.addActionListener(a);
		return panel;
	}*/
	private Component createDataRatePanel() {
		XYSeriesCollection xyDataset = new XYSeriesCollection();
		final XYSeries seriesIn = new XYSeries( "input rate (kB/s)" );
		xyDataset.addSeries( seriesIn );
		final XYSeries seriesOut =  new XYSeries( "output rate (kB/s)" );
		xyDataset.addSeries( seriesOut );


		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"",  // title
				"x-axis",             // x-axis label
				"y-axis",   // y-axis label
				xyDataset,            // data
				true,               // create legend?
				false,               // generate tooltips?
				false               // generate URLs?
				);

		XYPlot plot = (XYPlot)chart.getPlot();
		ValueAxis yaxis = (ValueAxis)plot.getRangeAxis();
		yaxis.setLabel("data rate (kB/s)");
		yaxis.setRange(-10, 1000);
		
		DateAxis xaxis = (DateAxis)plot.getDomainAxis();
		xaxis.setLabel("time");
		//        axis.setDateFormatOverride( new SimpleDateFormat( "MMM-yyyy" ) );
		xaxis.setDateFormatOverride( new SimpleDateFormat( "hh:mm" ) );
		ChartPanel cp = new ChartPanel(chart);

		Thread thread1 = new Thread(()-> {
			while(!stopped) {
				double rate = client.sampleInputByteRate(5000)/1000;
				seriesIn.add(System.currentTimeMillis(), rate);
				if(seriesIn.getItemCount() >= 600)
					seriesIn.remove(0);
			}
		});

		Thread thread2 = new Thread(()-> {
			while(!stopped) {
				double rate = client.sampleOutputByteRate(5000)/1000;
				seriesOut.add(System.currentTimeMillis(), rate);
				if(seriesOut.getItemCount() >= 600)
					seriesOut.remove(0);
			}
		});
		thread1.start();
		thread2.start();
		this.addWindowStateListener(new WindowStateListener() {

			@Override
			public void windowStateChanged(WindowEvent e) {
				if(e.getNewState() == WindowEvent.WINDOW_CLOSED || e.getNewState() == WindowEvent.WINDOW_CLOSING)
					stopped = true;
			}
			
		});
		//cp.set
		return cp;
	}
	boolean stopped = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = -2126560197829519642L;

}
