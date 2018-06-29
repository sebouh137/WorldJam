package worldjam.gui;

import javax.sound.sampled.Line;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import worldjam.exe.DefaultClient;
import worldjam.audio.*;
import worldjam.core.BeatClock;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JLabel;
import java.awt.Font;

import javax.swing.SwingConstants;

public class DefaultClientGUI extends JFrame implements PlaybackManager.ChannelChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6893387160409587544L;
	private DefaultClient client;
	private JMenu mnChannels;
	private Conductor conductor;
	public DefaultClientGUI(DefaultClient client) {
		setTitle("World Jam");
		
		this.client = client;
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
		
		this.conductor = new DefaultConductor(client.getClock());
		getContentPane().add(conductor, BorderLayout.CENTER);
		
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
		this.conductor.setClock(clock);
	}
}
