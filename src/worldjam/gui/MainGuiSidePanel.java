package worldjam.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.OutputStream;
import java.util.Map;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import worldjam.audio.AudioFilter;
import worldjam.audio.AudioSample;
import worldjam.audio.PlaybackChannel;
import worldjam.exe.Client;
import worldjam.gui.MainGuiSidePanel.DummyPlaybackChannel;
import worldjam.time.ClockSetting;
import worldjam.time.DelaySetting;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;

public class MainGuiSidePanel extends JPanel{
	public class DummyPlaybackChannel implements PlaybackChannel {

		@Override
		public double getRMS(double window) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void sampleReceived(AudioSample sample) {
			// TODO Auto-generated method stub

		}

		@Override
		public void changeDelaySetting(DelaySetting newDelaySetting) {
			// TODO Auto-generated method stub

		}

		@Override
		public void close() {
			// TODO Auto-generated method stub

		}

		@Override
		public void changeClockSettingsNow(ClockSetting beatClock) {
			// TODO Auto-generated method stub

		}

		@Override
		public ClockSetting getClock() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Line getLine() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Mixer getMixer() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setFilter(AudioFilter filter) {
			// TODO Auto-generated method stub

		}

		@Override
		public AudioFormat getInputFormat() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AudioFormat getPlaybackFormat() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AudioFilter getFilter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getChannelName() {
			return "placeholder";
		}

		@Override
		public long getChannelID() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void startRecording(OutputStream output, long startTime) {
			// TODO Auto-generated method stub

		}

		@Override
		public void stopRecording(long timestamp) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setMuted(boolean muted) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isMuted() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean canBeMuted() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int getTotalDelayInMS() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public DelaySetting getDelaySetting() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void validateDelays() {
			// TODO Auto-generated method stub

		}

	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 709459704287380237L;
	private Client client;
	public MainGuiSidePanel(Client client) {
		this.client = client;
		this.setPreferredSize(new Dimension(320, 700));
		this.setLayout(new FlowLayout());
		if(client == null) { //dummy channels for design test
			this.addChannelPanel(new DummyPlaybackChannel());
			this.addChannelPanel(new DummyPlaybackChannel());
		} else {
			for(PlaybackChannel channel : client.getPlaybackManager().getChannels()) {
				addChannelPanel(channel);
			}
		}
		//thread that periodically refreshes the list of channels,
		// and their statuses
		Thread refresher = new Thread(()-> {
			while(true) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				refreshChannels();
			}
		});
		refresher.start();
	}
	private void refreshChannels() {
		boolean changed = false;
		//add panels for new channels
		for(PlaybackChannel channel : client.getPlaybackManager().getChannels()) {
			if(!channels2panels.containsKey(channel)) {
				addChannelPanel(channel);
				changed = true;
			}
		}
		//remove panels associated with dead channels
		for(JPanel panel : panels2channels.keySet().toArray(new JPanel[0])) {
			if(client.getPlaybackManager().getChannel(panels2channels.get(panel).getChannelID())==null) {
				remove(panel);
				channels2panels.remove(panels2channels.get(panel));
				panels2channels.remove(panel);
				changed = true;
			}
		}
		if(changed)
			this.revalidate();
		//update positions of the sliders and the status of the mute button
		for(JPanel panel : panels2channels.keySet()) {
			PlaybackChannel channel = panels2channels.get(panel);
			for(Component comp : panel.getComponents()) {
				if(comp.getName() == "muteButton") {
					((JButton)comp).setIcon(channel.isMuted() ? mutedIcon : unmutedIcon);
				} else if (comp.getName() == "gainSlider") {
					FloatControl control = ((FloatControl)channel.getLine().getControl(FloatControl.Type.MASTER_GAIN));
					((JSlider)comp).setValue((int)((control.getValue()-control.getMinimum())/control.getPrecision()));
				} else if (comp.getName() == "balanceSlider") {
					FloatControl control = ((FloatControl)channel.getLine().getControl(FloatControl.Type.BALANCE));
					((JSlider)comp).setValue((int)((control.getValue()-control.getMinimum())/control.getPrecision()));
				}
			}
		}
	}
	void addChannelPanel(PlaybackChannel channel) {
		JPanel subpanel = new JPanel();
		subpanel.setBackground(new Color(173, 216, 230));
		GridBagLayout gbl_subpanel = new GridBagLayout();
		gbl_subpanel.columnWidths = new int[]{20, 30, 125, 72, 0};
		gbl_subpanel.rowHeights = new int[]{29, 20, 0};
		gbl_subpanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_subpanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		subpanel.setLayout(gbl_subpanel);
		JLabel label = new JLabel(channel.getChannelName());
		label.setFont(new Font("Lucida Grande", Font.ITALIC, 15));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.SOUTHWEST;
		gbc_label.gridwidth = 2;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		subpanel.add(label, gbc_label);
		JButton muteButton = new JButton(channel.isMuted() ? mutedIcon : unmutedIcon);
		muteButton.setName("muteButton");
		muteButton.addActionListener(e->{
			channel.setMuted(!channel.isMuted());
			muteButton.setIcon(channel.isMuted()? mutedIcon : unmutedIcon);
		});
		GridBagConstraints gbc_muteButton = new GridBagConstraints();
		gbc_muteButton.anchor = GridBagConstraints.WEST;
		gbc_muteButton.insets = new Insets(0, 0, 0, 5);
		gbc_muteButton.gridx = 0;
		gbc_muteButton.gridy = 1;
		subpanel.add(muteButton, gbc_muteButton);
		JButton settingsButton = new JButton(settingsIcon);
		GridBagConstraints gbc_settingsButton = new GridBagConstraints();
		gbc_settingsButton.anchor = GridBagConstraints.WEST;
		gbc_settingsButton.insets = new Insets(0, 0, 0, 5);
		gbc_settingsButton.gridx = 1;
		gbc_settingsButton.gridy = 1;
		subpanel.add(settingsButton, gbc_settingsButton);
		settingsButton.addActionListener(e->{
			new PlaybackChannelControlGUI(channel, channel.getChannelName(), client.getDelayManager()).setVisible(true);
		});
		
		this.add(subpanel);
		
		
		FloatControl masterGain = (FloatControl)channel.getLine().getControl(FloatControl.Type.MASTER_GAIN);
		JSlider slider = createSliderFromControl(masterGain);
		slider.setPaintTrack(true);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		//slider.
		
		slider.setName("gainSlider");
		slider.setPreferredSize(new Dimension(130,slider.getPreferredSize().height));
		
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.insets = new Insets(0, 0, 0, 5);
		gbc_slider.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider.anchor = GridBagConstraints.NORTH;
		gbc_slider.gridx = 2;
		gbc_slider.gridy = 0;
		gbc_slider.gridheight = 2;
		subpanel.add(slider, gbc_slider);
		
		JSlider slider2 = createSliderFromControl((FloatControl)channel.getLine().getControl(FloatControl.Type.BALANCE));
		slider2.setName("balanceSlider");
		slider2.setPaintLabels(true);
		slider2.setPaintTicks(true);
		slider2.setPaintTrack(true);
		slider2.setPreferredSize(new Dimension(90,slider2.getPreferredSize().height));
		GridBagConstraints gbc_slider2 = new GridBagConstraints();
		gbc_slider2.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider2.anchor = GridBagConstraints.NORTH;
		gbc_slider2.gridx = 3;
		gbc_slider2.gridy = 0;
		gbc_slider2.gridheight = 2;
		subpanel.add(slider2, gbc_slider2);
		channels2panels.put(channel, subpanel);
		panels2channels.put( subpanel, channel);
	}
	Font SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
	Map<PlaybackChannel, JPanel> channels2panels = new HashMap();
	Map<JPanel,PlaybackChannel> panels2channels = new HashMap();

	private JSlider createSliderFromControl(FloatControl control) {
		int nPositions = (int)((control.getMaximum()-control.getMinimum())/control.getPrecision());
		int currentPosition = (int)((control.getValue()-control.getMinimum())/control.getPrecision());
		
		JSlider slider = new JSlider(0, nPositions, currentPosition);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(10);
		Dictionary labels = new Hashtable();
		//JLabel label = new JLabel();
		if(control.getType().equals(FloatControl.Type.MASTER_GAIN)) {
			for(int i = 0; i < 90 ; i++) {
				JLabel label = new JLabel(i != 0 ? String.format("%d ", -20*i) : "   0 dB");
				label.setFont(SMALL_FONT);
				int val = (int)((-control.getMinimum()-20*i)/control.getPrecision());
				labels.put(val, label);
			}
			slider.setMajorTickSpacing((int)(10/control.getPrecision()));
			slider.setMinorTickSpacing((int)(5/control.getPrecision()));
		} else if(control.getType().equals(FloatControl.Type.BALANCE)) {
			JLabel label = new JLabel("L");
			label.setFont(SMALL_FONT);
			labels.put(slider.getMinimum(), label);
			label = new JLabel("R");
			label.setFont(SMALL_FONT);
			labels.put(slider.getMaximum(), label);	
			slider.setMinorTickSpacing((int)(.2/control.getPrecision()));
			slider.setMajorTickSpacing((int)(1.0/control.getPrecision()));
		}
		slider.setLabelTable(labels);
		slider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent event){
				float value = control.getPrecision()*slider.getValue()+control.getMinimum();
				control.setValue(value);
			}
		});
		return slider;
	}

	private static ImageIcon mutedIcon = new ImageIcon(ClientListItem.class.getResource("/worldjam/gui/icons/mute.png"));
	private static ImageIcon unmutedIcon = new ImageIcon(ClientListItem.class.getResource("/worldjam/gui/icons/unmute.png"));
	private static ImageIcon settingsIcon = new ImageIcon(ClientListItem.class.getResource("/worldjam/gui/icons/settings.png"));
	public static void main(String arg[]) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new MainGuiSidePanel(null));
		frame.setSize(100, 300);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
