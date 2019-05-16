package worldjam.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;

import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JFormattedTextField;

public class ChatPanel extends JPanel {
	JTextPane convoDisplay;
	StringBuilder convo = new StringBuilder();
	String username = "user1";
	/**
	 * Create the panel.
	 */
	public ChatPanel() {
		setLayout(new BorderLayout(0, 0));
		
		convoDisplay = new JTextPane();
		convoDisplay.setContentType("text/html");
		addText("System", "Chat enabled");
		add(convoDisplay, BorderLayout.CENTER);
		
		jtaEntry = new JFormattedTextField();
		add(jtaEntry, BorderLayout.SOUTH);
	}
	
	JFormattedTextField jtaEntry;
	void addText(String username, String message){
	
		//synchronized(convoDisplay){
			String t = convo.toString();
			convo.append("\n<br><b>" + username + "</b>:" + message);
			convoDisplay.setText("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\">\n" 
					+ convo.toString() +
					"</p>\n  </body>\n</html>\n\n<b>");
		//}
	}
	public static void main(String[] args){
		JFrame frame = new JFrame();
		frame.getContentPane().add(new ChatPanel());
		frame.setSize(500,500);
		
		frame.setVisible(true);
	}
}
