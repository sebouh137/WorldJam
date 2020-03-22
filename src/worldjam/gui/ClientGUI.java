package worldjam.gui;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import worldjam.exe.Client;
import worldjam.gui.conductor.Conductor;
import worldjam.gui.extras.Tuner;
import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.time.MutableClock;
import worldjam.video.VideoFrame;
import worldjam.video.ViewPanel;
import worldjam.audio.*;

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

import com.github.sarxos.webcam.WebcamViewer;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
//import javax.swing.JList;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JButton;

public class ClientGUI extends JFrame implements PlaybackManager.ChannelChangeListener, ClockSubscriber {
	/**
	 * 
	 */

	ConductorAndWebcamViewer viewManager;
	private static final long serialVersionUID = -6893387160409587544L;
	private Client client;
	//private JMenu mnChannels;
	private Conductor conductor;
	//private ViewPanel webcamViewer;
	public ClientGUI(Client client) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				try {
					conductor.close();
					client.exit();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		setTitle("World Jam: " + client.getUserName());

		this.client = client;
		this.changeClockSettingsNow(client.getBeatClock());

		this.setSize(964, 646);



		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		//JMenu mnPlayback = new JMenu("Playback");
		//menuBar.add(mnPlayback);
		//mnChannels = new JMenu("Channels");
		//mnPlayback.add(mnChannels);		

		JMenu mnInput = new JMenu("Input");
		menuBar.add(mnInput);

		JMenuItem mntmInputMonitor = new JMenuItem("Input Monitor...");
		mnInput.add(mntmInputMonitor);

		JMenu mnOtherSettings = new JMenu("Timing");
		menuBar.add(mnOtherSettings);

		JMenuItem mntmTempo = new JMenuItem("Tempo ...");
		mnOtherSettings.add(mntmTempo);



		mntmTempo.addActionListener(e -> {
			MutableClock clockManager = new MutableClock(this.client.getBeatClock());
			ClockSubscriber globalChange = setting->{
				this.client.changeClockSettingsNow(setting);
				this.client.broadcastClockChange();
			};
			new BPMWindow(clockManager,globalChange).setVisible(true);
		});

		JMenuItem mntmDelays = new JMenuItem("Delays ...");
		mnOtherSettings.add(mntmDelays);

		JMenuItem mntmNewMenuItem = new JMenuItem("Calibration ...");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new TimeCalibrationDialog(client).setVisible(true);
			}
		});
		mnOtherSettings.add(mntmNewMenuItem);

		JMenu mnDebug = new JMenu("Debug");
		menuBar.add(mnDebug);

		JMenuItem mntmNetwork = new JMenuItem("Network...");
		mnDebug.add(mntmNetwork);
		mntmNetwork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new NetworkInfoWindow(client);
			}
		});



		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		JMenuItem mntmTuner = new JMenuItem("Tuner...");
		mnTools.add(mntmTuner);
		mntmTuner.addActionListener(e->{
			Tuner tuner = new Tuner();
			client.getInput().addSubscriber(tuner);
			JFrame frame = new JFrame();
			frame.setTitle("WorldJam: Tuner");
			frame.setSize(300,300);
			frame.add(tuner);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			frame.addWindowListener(new WindowAdapter() {


				@Override
				public void windowClosing(WindowEvent e) {
					client.getInput().removeSubscriber(tuner);
				}

				@Override
				public void windowClosed(WindowEvent e) {
					client.getInput().removeSubscriber(tuner);
				}

				@Override
				public void windowIconified(WindowEvent e) {
					client.getInput().removeSubscriber(tuner);
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
					client.getInput().addSubscriber(tuner);
				}


			});
			frame.setVisible(true);

		});
		JMenuItem mnRecording = new JMenuItem("Recording...");
		mnTools.add(mnRecording);
		mnRecording.addActionListener(e->{
			new RecordDialog(this.client).setVisible(true);
		});
		mntmDelays.addActionListener(e->{
			new DelaySettingsDialog(this.client.getDelayManager()).setVisible(true);
		});
		/*JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					client.exit();
					System.out.println("client.close();");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		menuBar.add(btnClose);*/
		if(client.getInput() == null){
			mntmInputMonitor.setEnabled(false);
		}
		mntmInputMonitor.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				new InputMonitor(client.getInput(), "Input").setVisible(true);;
			}

		});


		this.conductor = new Conductor(client.getBeatClock());
		//ViewPanel webcamViewer = new ViewPanel();
		//viewManager = new ConductorAndWebcamViewer(conductor, webcamViewer);
		viewManager = new ConductorAndWebcamViewer(conductor, this.client.getDelayManager());

		getContentPane().add(viewManager, BorderLayout.CENTER);

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

		//clientList = new JList<ClientListItem>();
		//JPopupMenu popupMenu = new JPopupMenu();
		//clientList.setCellRenderer(new ClientListItem.ClientListItemRenderer());
		//clientList.setComponentPopupMenu(popupMenu);
		//clientList.setFixedCellWidth(130);
		/*
		JCheckBoxMenuItem muteButton = new JCheckBoxMenuItem("Mute Channel");
		muteButton.setSelected(false);
		muteButton.addChangeListener(e->{
			ClientListItem selection = clientList.getSelectedValue();
			if(selection == null)
				return;
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

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		});
		clientListModel = new DefaultListModel<ClientListItem>();
		clientList.setModel(clientListModel);
		clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		clientList.setSelectedIndex(0);
		clientListModel.addElement(new ClientListItem(client.getUserName(), client.getDescriptor().clientID, true, true));

		clientList.validate();
		getContentPane().add(clientList, BorderLayout.EAST);*/

		//chat = new ChatPanel();
		//getContentPane().add(chat, BorderLayout.WEST);

		getContentPane().add(new MainGuiSidePanel(this.client), BorderLayout.EAST);

		client.getPlaybackManager().addChannelChangeListener(this);
		client.getPlaybackManager().updateChannels();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		channelsChanged();


	}
	//JList<ClientListItem> clientList;
	//DefaultListModel<ClientListItem> clientListModel;
	Font infoFont = new Font("Lucida Grande", Font.PLAIN, 16);
	private Component createTimeInfoPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		ClockSetting clock = client.getBeatClock();

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
		/*mnChannels.removeAll();
		boolean listChanged = false;
		for(Long id : client.getPlaybackManager().getIDs()){
			//Line line = client.getPlaybackManager().getLine(id);
			String channelName = client.getPlaybackManager().getChannelName(id);
			JMenuItem mnChannel = new JMenuItem(channelName);
			mnChannels.add(mnChannel);
			mnChannel.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					new PlaybackChannelControlGUI(client.getPlaybackManager().getChannel(id), "Settings for channel: " + channelName,
							client.getDelayManager());
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
				clientListModel.addElement(new ClientListItem(channelName, id, false, client.getPlaybackManager().getChannel(id).isMuted()));
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
		for(Object o : clientListModel.toArray()) {
			ClientListItem item = (ClientListItem)o;
			item.setMuted(client.getPlaybackManager().getChannel(item.getClientID()).isMuted());
		}
		//clientList.validate();
		//clientList.repaint();
		*/
	}
	public void changeClockSettingsNow(ClockSetting clock){
		if(this.conductor != null)
			this.conductor.changeClockSettingsNow(clock);
		if(this.viewManager != null)
			this.viewManager.changeClockSettingsNow(clock);
		if(lblTimeSig != null)
			lblTimeSig.setText(String.format("%d/%d", clock.beatsPerMeasure, clock.beatDenominator));
		if(lblBPM != null)
			lblBPM.setText(String.format("%.1f BPM", 60000./clock.msPerBeat));
		if(lblMS != null)
			lblMS.setText(String.format("(%d ms per beat)", clock.msPerBeat));
	}
	ChatPanel chat = null;
	void getChat(){

	}

	public void videoFrameReceived(VideoFrame frame) {
		viewManager.imageReceived(frame);
	}
}
