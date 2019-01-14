package worldjam.gui;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import worldjam.exe.Client;
import worldjam.gui.conductor.BezierConductor;
import worldjam.audio.*;
import worldjam.core.BeatClock;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JLabel;
import java.awt.Font;

import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JList;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ClientGUI extends JFrame implements PlaybackManager.ChannelChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6893387160409587544L;
	private Client client;
	private JMenu mnChannels;
	private BezierConductor conductor;
	public ClientGUI(Client client) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				try {
					client.exit();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		setTitle("World Jam");
		
		this.client = client;
		this.setClock(client.getBeatClock());
		
		this.setSize(559, 400);
		
		/*try {
			Image image = ImageIO.read(new File("img/icons/wj_logo.png"));
			this.setIconImage(image);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
				
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnPlayback = new JMenu("Playback");
		menuBar.add(mnPlayback);
		mnChannels = new JMenu("Channels");
		mnPlayback.add(mnChannels);		
		
		JMenu mnInput = new JMenu("Input");
		menuBar.add(mnInput);
		
		JMenuItem mntmInputMonitor = new JMenuItem("Input Monitor...");
		mnInput.add(mntmInputMonitor);
		if(client.getInput() == null){
			mntmInputMonitor.setEnabled(false);
		}
		mntmInputMonitor.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				new InputMonitor(client.getInput(), "Input").setVisible(true);;
			}
			
		});
		
		
		this.conductor = new BezierConductor(client.getBeatClock());
		getContentPane().add(conductor, BorderLayout.CENTER);
		
		getContentPane().add(createTimeInfoPanel(), BorderLayout.SOUTH);;
		
		
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		
		JLabel lblSessionName = new JLabel(client.getSessionName());
		lblSessionName.setFont(infoFont);
		panel.add(lblSessionName, BorderLayout.WEST);
		/*JLabel lblUserName = new JLabel(client.getUserName());
		lblUserName.setFont(infoFont);
		panel.add(lblUserName, BorderLayout.EAST);*/
		
		clientList = new JList<ClientListItem>();
		JPopupMenu popupMenu = new JPopupMenu();
		clientList.setComponentPopupMenu(popupMenu);
		clientList.setFixedCellWidth(130);
		
		JCheckBoxMenuItem muteButton = new JCheckBoxMenuItem("Mute Channel");
		muteButton.setSelected(false);
		muteButton.addChangeListener(e->{
			ClientListItem selection = clientList.getSelectedValue();
			long channelID = selection.getClientID();
			PlaybackChannel channel = client.getPlaybackManager().getChannel(channelID);
			channel.setMuted(muteButton.isSelected());
			selection.setMuted(muteButton.isSelected());
			clientList.validate();
			clientList.repaint();
		});
		popupMenu.add(muteButton);
		popupMenu.addPopupMenuListener(new PopupMenuListener(){

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				long channelID = clientList.getSelectedValue().getClientID();
				PlaybackChannel channel = client.getPlaybackManager().getChannel(channelID);
				muteButton.setSelected(channel.isMuted());
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		clientListModel = new DefaultListModel<ClientListItem>();
		clientList.setModel(clientListModel);
		clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientList.setSelectedIndex(0);
		clientListModel.addElement(new ClientListItem(client.getUserName(), client.getDescriptor().clientID, true));
		
		clientList.validate();
		getContentPane().add(clientList, BorderLayout.EAST);
		
		
		
		client.getPlaybackManager().addChannelChangeListener(this);
		client.getPlaybackManager().updateChannels();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	JList<ClientListItem> clientList;
	DefaultListModel<ClientListItem> clientListModel;
	Font infoFont = new Font("Lucida Grande", Font.PLAIN, 16);
	private Component createTimeInfoPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		BeatClock clock = client.getBeatClock();
		
		lblTimeSig = new JLabel(String.format("%d/%d", clock.beatsPerMeasure, clock.beatDenominator));
		lblTimeSig.setFont(infoFont);
		panel.add(lblTimeSig, BorderLayout.WEST);
		
		lblBPM = new JLabel(String.format("%.1f BPM", 60000./clock.msPerBeat));
		lblBPM.setHorizontalAlignment(SwingConstants.CENTER);
		lblBPM.setFont(infoFont);
		panel.add(lblBPM, BorderLayout.CENTER);
		
		lblMS = new JLabel(String.format("(%d ms per beat)", clock.msPerBeat));
		lblMS.setFont(infoFont);
		panel.add(lblMS, BorderLayout.EAST);
		
		return panel;
	}
	
	JLabel lblTimeSig, lblBPM, lblMS;
	
	public void channelsChanged(){
		mnChannels.removeAll();
		boolean listChanged = false;
		for(Long id : client.getPlaybackManager().getIDs()){
			//Line line = client.getPlaybackManager().getLine(id);
			String channelName = client.getPlaybackManager().getChannelName(id);
			JMenuItem mnChannel = new JMenuItem(channelName);
			mnChannels.add(mnChannel);
			mnChannel.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					new PlaybackChannelControlGUI(client.getPlaybackManager().getChannel(id), "Settings for channel: " + channelName);
				}
				
			});
			boolean found = false;
			for(Object item : clientListModel.toArray()){
				if(((ClientListItem)item).clientID == id)
				{
					found = true;
					listChanged = true;
					break;
				}
			}
			if(!found)
				clientListModel.addElement(new ClientListItem(channelName, id, false));
		}
		//now remove any dead channels.
		for(Object item : clientListModel.toArray()){
			boolean found = false;
			for(Long id : client.getPlaybackManager().getIDs()){
				if(((ClientListItem)item).clientID == id)
				{
					found = true;
					listChanged = true;
					break;
				}
			}
			if(!found)
				clientListModel.removeElement(item);
				
		}
		if(listChanged){
			clientList.validate();
			clientList.repaint();
		}
	}
	public void setClock(BeatClock clock){
		if(this.conductor != null)
			this.conductor.setClock(clock);
		if(lblTimeSig != null)
			lblTimeSig.setText(String.format("%d/%d", clock.beatsPerMeasure, clock.beatDenominator));
		if(lblBPM != null)
			lblBPM.setText(String.format("%.1f BPM", 60000./clock.msPerBeat));
		if(lblMS != null)
			lblMS.setText(String.format("(%d ms per beat)", clock.msPerBeat));
	}
	
}
