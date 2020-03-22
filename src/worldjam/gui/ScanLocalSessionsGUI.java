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
import worldjam.net.ScanForJamSessions;
import worldjam.net.SessionConnectionInfo;
import worldjam.net.WJConstants;
import worldjam.time.ClockSetting;
import worldjam.util.DefaultObjects;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class ScanLocalSessionsGUI extends JPanel{
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
	private JTextField textFieldScanRange;
	private JList list;
	//JTextPane textPane;
	public ScanLocalSessionsGUI() {
		setLayout(new BorderLayout(0, 0));
		
		//textPane = new JTextPane();
		//getContentPane().add(textPane, BorderLayout.CENTER);
		//textPane.setEditable(false);
		
		
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		
		JLabel lblNewLabel = new JLabel("Address range:");
		panel.add(lblNewLabel);
		
		textFieldScanRange = new JTextField();
		textFieldScanRange.setText("192.168.193.0/24");
		panel.add(textFieldScanRange);
		textFieldScanRange.setColumns(10);
		
		JButton btnScan = new JButton("Scan");
		panel.add(btnScan);
		btnScan.addActionListener(e->{
			new Thread(()->{update();}).start();
		});
		
		list = new JList();
		add(list, BorderLayout.CENTER);
		new Thread(()->{update();}).start();
		this.setSize(450, 300);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(e->{
			if(list.getSelectedValue() instanceof SessionConnectionInfo) {
				SessionConnectionInfo sci = (SessionConnectionInfo)list.getSelectedValue();
				StringBuilder sb = new StringBuilder();
				for(String s : sci.addresses) {
					sb.append(s+"/" + DefaultObjects.defaultPort + ",");
				}
				jtfSessionInfo.setText(sb.toString().substring(0, sb.toString().length()-1)); //remove trailing comma
			}
		});
		
		
		jtfSessionInfo = new JTextField();
		this.add(jtfSessionInfo, BorderLayout.SOUTH);
	}	
	JTextField jtfSessionInfo;
	private void update() {
		DefaultListModel<String> dummyListModel = new DefaultListModel<String>(); 
		list.setModel(dummyListModel);
		list.setEnabled(false);
		list.setCellRenderer(new DefaultListCellRenderer());
		dummyListModel.addElement("Scanning for jam sessions");
		try {
			ArrayList<SessionConnectionInfo> activeSessions = ScanForJamSessions.scanRange(textFieldScanRange.getText(),DefaultObjects.defaultPort, 1000);
			if(activeSessions.size() != 0) {
				DefaultListModel<Object> listModel = new DefaultListModel<Object>();
				for (SessionConnectionInfo session : activeSessions) {
					listModel.addElement(session);
				}
				list.setEnabled(true);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				list.setModel(listModel);
				list.setCellRenderer(new MyListCellRenderer());
				listModel.addElement(manualInput);
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
	Object manualInput = "Manually input session connection details";
	public String getSelection() {
		return jtfSessionInfo.getText().trim();
	}

}
