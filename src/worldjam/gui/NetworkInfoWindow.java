package worldjam.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import worldjam.exe.Client;
import worldjam.net.NetworkUtils;

import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextPane;
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
			textArea.setText(client.getFormattedSessionStatusString());
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
			try {
				textArea.setText(NetworkUtils.getNetworkInterfaceInfo(!chckbxNewCheckBox.isSelected()));
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
		};
		a.actionPerformed(null);

		chckbxNewCheckBox.addActionListener(a);
		buttonRefresh.addActionListener(a);
		return panel;
	}

	private Component createDataRatePanel() {
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
			textArea.setText(String.format("Input: %.0f kBps\nOutput: %.0f kBps",
					client.sampleInputByteRate(1000)/1000.,
					client.sampleOutputByteRate(1000)/1000.));

		};
		a.actionPerformed(null);
		buttonRefresh.addActionListener(a);
		return panel;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2126560197829519642L;

}
