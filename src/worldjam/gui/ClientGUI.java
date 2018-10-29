package worldjam.gui;

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
import javax.swing.JPanel;

public class ClientGUI extends JFrame implements PlaybackManager.ChannelChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6893387160409587544L;
	private Client client;
	private JMenu mnChannels;
	private BezierConductor conductor;
	public ClientGUI(Client client) {
		setTitle("World Jam");
		
		this.client = client;
		this.setClock(client.getBeatClock());
		
		this.setSize(400, 400);
		
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
		JLabel lblUserName = new JLabel(client.getUserName());
		lblUserName.setFont(infoFont);
		panel.add(lblUserName, BorderLayout.EAST);
		
		
		
		
		client.getPlaybackManager().addChannelChangeListener(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	Font infoFont = new Font("Lucida Grande", Font.PLAIN, 16);
	private Component createTimeInfoPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		BeatClock clock = client.getBeatClock();
		
		JLabel lblTimeSig = new JLabel(String.format("%d/%d", clock.beatsPerMeasure, clock.beatDenominator));
		lblTimeSig.setFont(infoFont);
		panel.add(lblTimeSig, BorderLayout.WEST);
		
		JLabel lblBPM = new JLabel(String.format("%.1f BPM", 60000./clock.msPerBeat));
		lblBPM.setHorizontalAlignment(SwingConstants.CENTER);
		lblBPM.setFont(infoFont);
		panel.add(lblBPM, BorderLayout.CENTER);
		
		JLabel lblMS = new JLabel(String.format("(%d ms per beat)", clock.msPerBeat));
		lblMS.setFont(infoFont);
		panel.add(lblMS, BorderLayout.EAST);
		
		return panel;
	}
	public void channelsChanged(){
		mnChannels.removeAll();
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
			
		}
	}
	public void setClock(BeatClock clock){
		if(this.conductor != null)
			this.conductor.setClock(clock);
	}
}
