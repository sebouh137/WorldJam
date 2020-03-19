package worldjam.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
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

	ClientListItem(String username, long clientID, boolean isSelf,boolean isMuted){
		this.username = username;
		this.isSelf = isSelf;
		this.clientID = clientID;
		this.muted = isMuted;
	}

	public String toString() {

		return (this.isSelf ? "loopback" : username);
	}
	static class ClientListItemRenderer extends DefaultListCellRenderer{

		/**
		 * 
		 */
		private static final long serialVersionUID = 8705132098176075908L;
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {


			JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);
			if(((ClientListItem)value).muted) {
				renderer.setIcon(mutedIcon);
			} else {
				renderer.setIcon(unmutedIcon);
			}
			return renderer;

		}
		private static ImageIcon mutedIcon = new ImageIcon(ClientListItem.class.getResource("/worldjam/gui/icons/mute.png"));
		private static ImageIcon unmutedIcon = new ImageIcon(ClientListItem.class.getResource("/worldjam/gui/icons/unmute.png"));

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
