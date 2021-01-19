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
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import worldjam.audio.AudioFilter;
import worldjam.audio.AudioSample;
import worldjam.audio.InputThread;
import worldjam.audio.PlaybackChannel;
import worldjam.exe.Client;
import worldjam.net.WJConstants;
import worldjam.time.ClockSetting;
import worldjam.time.DelaySetting;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;

public class SidePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 709459704287380237L;
	private Client client;
	public SidePanel(Client client) {
		this.client = client;
		this.setPreferredSize(new Dimension(310, 700));
		this.setLayout(new FlowLayout());
		this.setBackground(new Color(100,100,100));
		addMiscPanel(client);
		addInputPanel(client.getInput());
		for(PlaybackChannel channel : client.getPlaybackManager().getChannels()) {
			addChannelPanel(channel);
		}

		//thread that periodically refreshes the list of channels,
		// and their statuses
		Thread refresher = new Thread(()-> {
			while(true) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				refreshChannels();
			}
		}, "refresh channels");
		refresher.start();
	}
	private JButton convoModeButton;
	private void addMiscPanel(Client client) {
		JPanel subpanel = new JPanel();
		subpanel.setLayout(new FlowLayout());
		subpanel.setBackground(new Color(190, 204, 190));
		subpanel.setBorder(BorderFactory.createRaisedBevelBorder());
		subpanel.setPreferredSize(new Dimension(300, 70));
		convoModeButton = new JButton(convoModeOffIcon);
		subpanel.add(convoModeButton);
		convoModeButton.addActionListener(e->{
			boolean convoMode = !client.getConvoMode();
			client.setConvoMode(convoMode);
			client.broadcastConvoMode();
			convoModeButton.setToolTipText("Toggles convo mode, which decreases the latency"
					+ " to a smaller fixed value (" + WJConstants.CONVO_MODE_LATENCY + " ms).  "
							+ "  This is useful for allowing users to talk with one another before, after "
							+ "and between jamming.");
			convoModeButton.setIcon(convoMode ? convoModeOnIcon : convoModeOffIcon);
		});
		this.add(subpanel);
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
		if(changed) {
			this.revalidate();
			this.repaint();
		}
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
		for(Component comp : inputPanel.getComponents()) {
			if(comp.getName() == "muteButton") {
				((JButton)comp).setIcon(client.getInput().isMuted() ? mutedMicIcon : unmutedMicIcon);
			} else if (comp.getName() == "gainSlider") {
				FloatControl control = client.getInput().inputVolumeControl();
				((JSlider)comp).setValue((int)((control.getValue()-control.getMinimum())/control.getPrecision()));
			} 
		}
	}
	void addInputPanel(InputThread input){
		JPanel subpanel = new JPanel();
		//this only happens in some minimalist tests;
		if(input == null)
			return; 
		subpanel.setBackground(new Color(190, 190, 204));
		subpanel.setBorder(BorderFactory.createRaisedBevelBorder());
		GridBagLayout gbl_subpanel = new GridBagLayout();
		gbl_subpanel.columnWidths = new int[]{30, 30, 30, 125, 72, 0};
		gbl_subpanel.rowHeights = new int[]{29, 20, 0};
		gbl_subpanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_subpanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		subpanel.setLayout(gbl_subpanel);
		JLabel label = new JLabel("input");
		label.setFont(new Font("Lucida Grande", Font.ITALIC, 15));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.SOUTHWEST;
		gbc_label.gridwidth = 3;
		gbc_label.insets = new Insets(0, 3, 5, 2);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		subpanel.add(label, gbc_label);
		JButton muteButton = new JButton(input.isMuted() ? mutedMicIcon : unmutedMicIcon);
		muteButton.setName("muteButton");
		muteButton.addActionListener(e->{
			input.setMuted(!input.isMuted());
			muteButton.setIcon(input.isMuted()? mutedMicIcon : unmutedMicIcon);
		});
		GridBagConstraints gbc_muteButton = new GridBagConstraints();
		gbc_muteButton.anchor = GridBagConstraints.WEST;
		gbc_muteButton.insets = new Insets(0, 2, 0, 2);
		gbc_muteButton.gridx = 0;
		gbc_muteButton.gridy = 1;
		subpanel.add(muteButton, gbc_muteButton);
		JButton settingsButton = new JButton(settingsIcon);
		GridBagConstraints gbc_settingsButton = new GridBagConstraints();
		gbc_settingsButton.anchor = GridBagConstraints.WEST;
		gbc_settingsButton.insets = new Insets(0, 2, 0, 2);
		gbc_settingsButton.gridx = 1;
		gbc_settingsButton.gridy = 1;
		subpanel.add(settingsButton, gbc_settingsButton);
		settingsButton.addActionListener(e->{
			new InputMonitor(input, "Input").setVisible(true);
		});

		SoundLevelBar slb = new SoundLevelBar(input, SoundLevelBar.VERTICAL);
		slb.setPreferredSize(new Dimension(25,25));
		slb.setMinimumSize(new Dimension(25, 25));
		GridBagConstraints gbc_slb = new GridBagConstraints();
		gbc_slb.anchor = GridBagConstraints.WEST;
		gbc_slb.insets = new Insets(0, 2, 0, 2);
		gbc_slb.gridx = 2;
		gbc_slb.gridy = 1;
		subpanel.add(slb, gbc_slb);

		FloatControl inputVolume = input.inputVolumeControl();
		if(inputVolume != null) {
			JSlider slider = createSliderFromControl(inputVolume);
			slider.setPaintTrack(true);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			//slider.

			slider.setName("gainSlider");
			slider.setPreferredSize(new Dimension(130,slider.getPreferredSize().height));

			GridBagConstraints gbc_slider = new GridBagConstraints();
			gbc_slider.insets = new Insets(0, 0, 0, 10);
			gbc_slider.fill = GridBagConstraints.HORIZONTAL;
			gbc_slider.anchor = GridBagConstraints.NORTH;
			gbc_slider.gridx = 3;
			gbc_slider.gridy = 0;
			gbc_slider.gridheight = 2;
			gbc_slider.gridwidth = 2;
			subpanel.add(slider, gbc_slider);
		} else {
			JTextArea warning = new JTextArea("[Java cannot access audio input volume controls]");
			warning.setLineWrap(true);
			warning.setFont(SMALL_FONT);
			warning.setEditable(false);
			GridBagConstraints gbc_slider = new GridBagConstraints();
			gbc_slider.insets = new Insets(0, 0, 0, 0);
			gbc_slider.fill = GridBagConstraints.HORIZONTAL;
			gbc_slider.anchor = GridBagConstraints.NORTH;
			gbc_slider.gridx = 3;
			gbc_slider.gridy = 0;
			gbc_slider.gridheight = 2;
			subpanel.add(warning, gbc_slider);
		}

		subpanel.setPreferredSize(new Dimension(300, 65));
		this.add(subpanel);
		this.inputPanel = subpanel;
	}
	JPanel inputPanel;

	void addChannelPanel(PlaybackChannel channel) {
		JPanel subpanel = new JPanel();
		if(!channel.getChannelName().equals("loopback") && 
				!channel.getChannelName().equals("metronome") &&
				!channel.getChannelName().equals("tuning fork"))
			subpanel.setBackground(new Color(173, 216, 230));
		else 
			subpanel.setBackground(new Color(214, 214, 206));
		subpanel.setBorder(BorderFactory.createRaisedBevelBorder());
		GridBagLayout gbl_subpanel = new GridBagLayout();
		gbl_subpanel.columnWidths = new int[]{30, 30,30, 125, 72, 0};
		gbl_subpanel.rowHeights = new int[]{29, 20, 0};
		gbl_subpanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_subpanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		subpanel.setLayout(gbl_subpanel);
		JLabel label = new JLabel(channel.getChannelName());
		label.setFont(new Font("Lucida Grande", Font.ITALIC, 15));
		GridBagConstraints gbc_label = new GridBagConstraints();

		gbc_label.anchor = GridBagConstraints.SOUTHWEST;
		gbc_label.gridwidth = 3;
		gbc_label.insets = new Insets(0, 2, 5, 3);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		gbc_label.fill = GridBagConstraints.BOTH;
		subpanel.add(label, gbc_label);
		JButton muteButton = new JButton(channel.isMuted() ? mutedIcon : unmutedIcon);
		muteButton.setName("muteButton");
		muteButton.addActionListener(e->{
			channel.setMuted(!channel.isMuted());
			muteButton.setIcon(channel.isMuted()? mutedIcon : unmutedIcon);
		});
		GridBagConstraints gbc_muteButton = new GridBagConstraints();
		gbc_muteButton.anchor = GridBagConstraints.WEST;
		gbc_muteButton.insets = new Insets(0, 2, 0, 2);
		gbc_muteButton.gridx = 0;
		gbc_muteButton.gridy = 1;
		subpanel.add(muteButton, gbc_muteButton);
		Icon icon = settingsIcon;
		if(channel.getChannelName().equals("metronome"))
			icon = metronomeSettingsIcon;
		else if(channel.getChannelName().equals("loopback"))
			icon =loopbackSettingsIcon;
		else if(channel.getChannelName().equals("tuning fork"))
			icon = tuningforkSettingsIcon;
		JButton settingsButton = new JButton(icon);
		GridBagConstraints gbc_settingsButton = new GridBagConstraints();
		gbc_settingsButton.anchor = GridBagConstraints.WEST;
		gbc_settingsButton.insets = new Insets(0, 2, 0, 2);
		gbc_settingsButton.gridx = 1;
		gbc_settingsButton.gridy = 1;
		subpanel.add(settingsButton, gbc_settingsButton);
		settingsButton.addActionListener(e->{
			new PlaybackChannelControlGUI(channel, channel.getChannelName(), client.getDelayManager()).setVisible(true);
		});

		this.add(subpanel);

		SoundLevelBar slb = new SoundLevelBar(channel, SoundLevelBar.VERTICAL);
		slb.setPreferredSize(new Dimension(25,25));
		slb.setMinimumSize(new Dimension(25, 25));
		GridBagConstraints gbc_slb = new GridBagConstraints();
		gbc_slb.anchor = GridBagConstraints.WEST;
		gbc_slb.insets = new Insets(0, 2, 0, 2);
		gbc_slb.gridx = 2;
		gbc_slb.gridy = 1;
		subpanel.add(slb, gbc_slb);

		//master gain control
		FloatControl masterGain = (FloatControl)channel.getLine().getControl(FloatControl.Type.MASTER_GAIN);
		if(masterGain != null) {
			JSlider slider = createSliderFromControl(masterGain);
			slider.setPaintTrack(true);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			//slider.

			slider.setName("gainSlider");
			slider.setPreferredSize(new Dimension(130,slider.getPreferredSize().height));

			GridBagConstraints gbc_slider = new GridBagConstraints();
			gbc_slider.insets = new Insets(0, 0, 0, 0);
			gbc_slider.fill = GridBagConstraints.HORIZONTAL;
			gbc_slider.anchor = GridBagConstraints.NORTH;
			gbc_slider.gridx = 3;
			gbc_slider.gridy = 0;
			gbc_slider.gridheight = 2;
			subpanel.add(slider, gbc_slider);
		} else {
			JTextArea warning = new JTextArea("[Java cannot access channel gain controls]");
			warning.setLineWrap(true);
			warning.setFont(SMALL_FONT);
			warning.setEditable(false);
			GridBagConstraints gbc_slider = new GridBagConstraints();
			gbc_slider.insets = new Insets(0, 0, 0, 0);
			gbc_slider.fill = GridBagConstraints.HORIZONTAL;
			gbc_slider.anchor = GridBagConstraints.NORTH;
			gbc_slider.gridx = 3;
			gbc_slider.gridy = 0;
			gbc_slider.gridheight = 2;
			subpanel.add(warning, gbc_slider);
		}

		// balance control
		FloatControl balanceControl = (FloatControl)channel.getLine().getControl(FloatControl.Type.BALANCE);
		if(balanceControl != null) {
			JSlider slider2 = createSliderFromControl(balanceControl);
			slider2.setName("balanceSlider");
			slider2.setPaintLabels(true);
			slider2.setPaintTicks(true);
			slider2.setPaintTrack(true);
			slider2.setPreferredSize(new Dimension(90,slider2.getPreferredSize().height));
			GridBagConstraints gbc_slider2 = new GridBagConstraints();
			gbc_slider2.fill = GridBagConstraints.HORIZONTAL;
			gbc_slider2.anchor = GridBagConstraints.NORTH;
			gbc_slider2.gridx = 4;
			gbc_slider2.gridy = 0;
			gbc_slider2.gridheight = 2;
			subpanel.add(slider2, gbc_slider2);
		} else {
			JTextArea warning = new JTextArea("[Java cannot access channel balance controls]");
			warning.setLineWrap(true);
			warning.setFont(SMALL_FONT);
			warning.setEditable(false);
			GridBagConstraints gbc_slider = new GridBagConstraints();
			gbc_slider.insets = new Insets(0, 0, 0, 0);
			gbc_slider.fill = GridBagConstraints.HORIZONTAL;
			gbc_slider.anchor = GridBagConstraints.NORTH;
			gbc_slider.gridx = 4;
			gbc_slider.gridy = 0;
			gbc_slider.gridheight = 2;
			subpanel.add(warning, gbc_slider);
		}
		channels2panels.put(channel, subpanel);
		panels2channels.put( subpanel, channel);
		subpanel.setPreferredSize(new Dimension(300, 65));
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
		} else if(control.getType().equals(FloatControl.Type.VOLUME)) {
			JLabel label = new JLabel("0");
			label.setFont(SMALL_FONT);
			labels.put(slider.getMinimum(), label);
			label = new JLabel("100%");
			label.setFont(SMALL_FONT);
			labels.put(slider.getMaximum(), label);
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
		slider.setBackground(Color.LIGHT_GRAY);
		return slider;
	}

	private static ImageIcon mutedIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/mute.png"));
	private static ImageIcon unmutedIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/unmute.png"));
	private static ImageIcon settingsIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/settings.png"));
	private static ImageIcon metronomeSettingsIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/metronome_settings.png"));
	private static ImageIcon tuningforkSettingsIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/tuningfork_settings.png"));
	private static ImageIcon loopbackSettingsIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/loopback_settings.png"));
	private static ImageIcon convoModeOffIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/convoModeOff.png"));
	private static ImageIcon convoModeOnIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/convoModeOn.png"));



	private ImageIcon mutedMicIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/mutedmic.png"));;
	private ImageIcon unmutedMicIcon = new ImageIcon(SidePanel.class.getResource("/worldjam/gui/icons/mic.png"));;
	public static void main(String arg[]) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new SidePanel(null));
		frame.setSize(100, 300);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public void setConvoMode(boolean b) {
		convoModeButton.setIcon(b ? convoModeOnIcon : convoModeOffIcon);
	}
}
