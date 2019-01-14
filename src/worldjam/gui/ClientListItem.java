package worldjam.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ClientListItem {
	static final int CONNECTED = 0;
	static final int OTHER = 1;
	private String username;
	private boolean isSelf;
	private int status;
	ClientListItem(String username, long clientID, boolean isSelf){
		this.username = username;
		this.isSelf = isSelf;
		this.clientID = clientID;
	}
	
	public String toString() {
		int[] codepoints = {0x1F507};
		String muteSymbol = " [M]";//new String(codepoints, 0, codepoints.length);//= "M";//"\uF09F\u9487";
		String unmuteSymbol = "";//"\uF09F\u9488";
		
		return username +  (isSelf ? " (me)": "") + (muted ? muteSymbol: unmuteSymbol);
	}
	static class ClientListItemRenderer extends Container implements ListCellRenderer<ClientListItem> {
		JLabel label = new JLabel();
		ClientListItemRenderer(){
			this.add(label);
			this.setPreferredSize(new Dimension(50, 15));
		}
		Color selectedColor = new Color(13,13, 255);
		Color unselectedColor = new Color(200,200, 200);
		@Override
		public Component getListCellRendererComponent(JList<? extends ClientListItem> list, ClientListItem value,
				int index, boolean isSelected, boolean cellHasFocus) {
			this.label.setText(value.username + (value.isSelf ? " (me)": ""));
			if(isSelected)
				label.setBackground(selectedColor);
			else
				label.setBackground(unselectedColor);
			return this;
		}
		
	}
	long clientID;
	private boolean muted;
	public Long getClientID() {
		return clientID;
	}
	public void setMuted(boolean muted) {
		this.muted = muted;
		
	}
	
}
