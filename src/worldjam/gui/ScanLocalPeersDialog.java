package worldjam.gui;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextPane;

import worldjam.exe.ClientDescriptor;
import worldjam.exe.SessionDescriptor;
import worldjam.gui.ScanLocalPeersDialog.MyListCellRenderer;
import worldjam.net.ScanForJamSessions;
import worldjam.net.SessionConnectionInfo;
import worldjam.time.ClockSetting;
import worldjam.util.DefaultObjects;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

public class ScanLocalPeersDialog extends JFrame{
	public class MyListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel a = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if(value instanceof SessionConnectionInfo) {
				SessionConnectionInfo val = (SessionConnectionInfo)value;
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for(int i = 0; i< val.descriptor.clients.length; i++) {
					ClientDescriptor c = val.descriptor.clients[i];
					sb.append(c.displayName); 
					if(i != val.descriptor.clients.length-1)
						sb.append(", ");
				}
				sb.append(");   ");
				ClockSetting clock = val.descriptor.clock;
				sb.append((int)clock.getBPM() + " BPM, " + clock.beatsPerMeasure + "/" +clock.beatDenominator); 
				
				a.setText(sb.toString());
			}
			return a;
		}

	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -1124388249742616941L;
	private JTextField textField;
	private JList list;
	//JTextPane textPane;
	public ScanLocalPeersDialog() {
		setTitle("World Jam:  Scan for active sessions");
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		//textPane = new JTextPane();
		//getContentPane().add(textPane, BorderLayout.CENTER);
		//textPane.setEditable(false);
		
		JButton btnScan = new JButton("Scan");
		getContentPane().add(btnScan, BorderLayout.SOUTH);
		btnScan.addActionListener(e->{
			new Thread(()->{update();}).start();
		});
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		
		JLabel lblNewLabel = new JLabel("Address range:");
		panel.add(lblNewLabel);
		
		textField = new JTextField();
		textField.setText("192.168.193.0/24");
		panel.add(textField);
		textField.setColumns(10);
		
		list = new JList();
		getContentPane().add(list, BorderLayout.CENTER);
		new Thread(()->{update();}).start();
		this.setSize(450, 300);
	}
	private void update() {
		DefaultListModel<String> dummyListModel = new DefaultListModel<String>(); 
		list.setModel(dummyListModel);
		list.setEnabled(false);
		list.setCellRenderer(new DefaultListCellRenderer());
		dummyListModel.addElement("Scanning for jam sessions");
		try {
			ArrayList<SessionConnectionInfo> activeSessions = ScanForJamSessions.scanRange(textField.getText(),DefaultObjects.defaultPort, 1000);
			if(activeSessions.size() != 0) {
				DefaultListModel<SessionConnectionInfo> listModel = new DefaultListModel<SessionConnectionInfo>();
				for (SessionConnectionInfo session : activeSessions) {
					listModel.addElement(session);
				}
				list.setEnabled(true);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				list.setModel(listModel);
				list.setCellRenderer(new MyListCellRenderer());
			} else {
				dummyListModel = new DefaultListModel<String>(); 
				list.setModel(dummyListModel);
				list.setEnabled(false);
				dummyListModel.addElement("No active sessions found");
				list.setCellRenderer(new DefaultListCellRenderer());
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		//textPane.setText(sb.toString());
	}

}
