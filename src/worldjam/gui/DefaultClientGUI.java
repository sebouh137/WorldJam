package worldjam.gui;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import worldjam.exe.DefaultClient;
import worldjam.audio.*;
import worldjam.core.BeatClock;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;

public class DefaultClientGUI extends JFrame implements PlaybackManager.ChannelChangeListener {
	private DefaultClient client;
	private JMenu mnChannels;
	public DefaultClientGUI(DefaultClient client) {
		setTitle("World Jam");
		this.client = client;
		this.setSize(400, 400);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnPlayback = new JMenu("Playback");
		menuBar.add(mnPlayback);
		mnChannels = new JMenu("Channel Controls");
		mnPlayback.add(mnChannels);
		
		JMenu mnInput = new JMenu("Input");
		menuBar.add(mnInput);
		
		JMenuItem mntmChannelSettings = new JMenuItem("Channel Settings");
		mnInput.add(mntmChannelSettings);
		mntmChannelSettings.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(client.getInput().getLine().getControls().length);
				new ChannelControlsGUI(client.getInput().getLine(),"Input settings");
				
			}
			
		});
		
		mntmChannelSettings = new JMenuItem("Mixer Settings");
		mnInput.add(mntmChannelSettings);
		mntmChannelSettings.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(client.getInput().getLine().getControls().length);
				new ChannelControlsGUI(client.getInput().getMixer(),"Input mixer settings");
				
			}
			
		});
		
		
		getContentPane().add(new Conductor(client.getClock()), BorderLayout.CENTER);
		
		JLabel lblInfo = new JLabel();
		lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfo.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		getContentPane().add(lblInfo, BorderLayout.SOUTH);
		BeatClock clock = client.getClock();
		lblInfo.setText(String.format("%d/%d   %.1f bpm  (%d ms per beat)", 
				clock.beatsPerMeasure,
				clock.beatDenominator,
				60000./clock.msPerBeat,
				clock.msPerBeat));
		
		client.getPlaybackManager().addChannelChangeListener(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public void channelsChanged(){
		for(Long id : client.getPlaybackManager().getIDs()){
			Line line = client.getPlaybackManager().getLine(id);
			String channelName = client.getPlaybackManager().getChannelName(id);
			JMenuItem mnChannel = new JMenuItem(channelName);
			mnChannels.add(mnChannel);
			mnChannel.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					new ChannelControlsGUI(line, "Settings for channel: " + channelName);
				}
				
			});
			
		}
	}
}
