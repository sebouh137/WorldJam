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
		
		String info = "";
		info += "username: " + client.getUserName();
		info += " (id = " + client.getDescriptor().clientID + ")\n";
		info +=  "server socket listening on port " +  client.getServerSocket().getLocalPort() ;
		if(!client.getServerSocket().getInetAddress().getHostAddress().equals("0.0.0.0")) {
			info +=  " (address = " +  client.getServerSocket().getInetAddress() +")\n";
		}
		else 
			info += " (listening on all local IP addresses)\n";
		textArea.setText(info);
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		};
		a.actionPerformed(null);
		
		chckbxNewCheckBox.addActionListener(a);
		buttonRefresh.addActionListener(a);
		return panel;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2126560197829519642L;
	
}
