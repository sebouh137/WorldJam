package worldjam.gui;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.sarxos.webcam.Webcam;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.exe.Client;
import worldjam.gui.conductor.Conductor;
import worldjam.time.ClockSetting;
import worldjam.util.ConfigurationsXML;
import worldjam.util.DefaultObjects;
import worldjam.video.WebcamThread;

import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JCheckBox;;

public class ClientSetupGUI_P2P_multiPeer extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2630697418874820157L;
	private static final String NO_WEBCAM = "[none]";
	private JTextField txtUser;
	private JTextField txtBPM;
	private JSpinner spinner_msPerBeat;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private JLabel lblTimeSignature;
	private JSpinner spinner;
	private JSpinner spinner_1;
	private JRadioButton rdbtnBeatsPerMinute;
	private JRadioButton rdbtnMsPerBeat;
	private JRadioButton rdbtnStartNewSession;
	private JRadioButton rdbtnJoinExistingSession;
	private JButton btnStart;
	private JLabel lblInput;
	private JComboBox<MixerWrapper> comboBox;
	private JLabel lblOutput;
	private JComboBox<MixerWrapper> comboBox_1;
	private Conductor previewConductor;
	private JTextField textFieldPort;
	private JLabel lblServerIpAddress;
	private JLabel lblLocalPort;
	private JTextField jtfLocalPort;
	private JLabel lblVideoInput;
	private JCheckBox chckbxUsedDefault;
	private JComboBox comboBoxWebcams;
	private JLabel lblVideoResolution;
	private JComboBox comboBoxResolutions;
	private JButton btnScan;


	public ClientSetupGUI_P2P_multiPeer() {
		this.setSize(742, 388);
		setTitle("WorldJam Client Setup");
		/*Image image;
		try {
			image = ImageIO.read(new File("img/icons/wj_logo.png"));
			this.setIconImage(image);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		JTabbedPane tabs = new JTabbedPane();
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(tabs, BorderLayout.CENTER);
		JPanel mainPanel = new JPanel();

		ChangeListener changeTimeSignature = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				ClockSetting clock = previewConductor.getClock().createWithDifferentBeatCount((int)spinner.getValue());
				previewConductor.changeClockSettingsNow(clock);
			}
		};
		tabs.addTab("General", mainPanel);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{83, 45, 113, 69, 62, 96, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		mainPanel.setLayout(gridBagLayout);

		JLabel lblDisplayName = new JLabel("  Display Name");
		GridBagConstraints gbc_lblDisplayName = new GridBagConstraints();
		gbc_lblDisplayName.gridwidth = 2;
		gbc_lblDisplayName.anchor = GridBagConstraints.WEST;
		gbc_lblDisplayName.insets = new Insets(0, 0, 5, 5);
		gbc_lblDisplayName.gridx = 0;
		gbc_lblDisplayName.gridy = 0;
		mainPanel.add(lblDisplayName, gbc_lblDisplayName);

		txtUser = new JTextField();
		txtUser.setText(ConfigurationsXML.getDefaultUserName());
		GridBagConstraints gbc_txtUser = new GridBagConstraints();
		gbc_txtUser.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUser.gridwidth = 2;
		gbc_txtUser.insets = new Insets(0, 0, 5, 5);
		gbc_txtUser.gridx = 2;
		gbc_txtUser.gridy = 0;
		mainPanel.add(txtUser, gbc_txtUser);
		txtUser.setColumns(10);

		ChangeListener cl = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				boolean enable = rdbtnStartNewSession.isSelected();
				lblTimeSignature.setEnabled(enable);
				rdbtnBeatsPerMinute.setEnabled(enable);
				rdbtnMsPerBeat.setEnabled(enable);
				spinner.setEnabled(enable);
				spinner_1.setEnabled(enable);
				txtBPM.setEnabled(enable);
				spinner_msPerBeat.setEnabled(enable);
				lblServerIpAddress.setEnabled(!enable);
				textFieldPort.setEnabled(!enable);

				if(rdbtnMsPerBeat.isSelected() && enable){
					txtBPM.setEnabled(false);
					spinner_msPerBeat.setEnabled(true);
				} else if (rdbtnBeatsPerMinute.isSelected() && enable){
					spinner_msPerBeat.setEnabled(false);
					txtBPM.setEnabled(true);
				}
				enable &= tabs.getSelectedComponent() == mainPanel;
				previewConductor.setVisible(enable);
				previewConductor.setMeasureNumberVisible(false);
			}
		};
		tabs.addChangeListener(cl);

		lblLocalPort = new JLabel("Local Port");
		GridBagConstraints gbc_lblLocalPort = new GridBagConstraints();
		gbc_lblLocalPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblLocalPort.gridx = 0;
		gbc_lblLocalPort.gridy = 1;
		mainPanel.add(lblLocalPort, gbc_lblLocalPort);

		jtfLocalPort = new JTextField(Integer.toString(DefaultObjects.defaultPort));
		GridBagConstraints gbc_jtfLocalPort = new GridBagConstraints();
		gbc_jtfLocalPort.insets = new Insets(0, 0, 5, 5);
		gbc_jtfLocalPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfLocalPort.gridx = 2;
		gbc_jtfLocalPort.gridy = 1;
		mainPanel.add(jtfLocalPort, gbc_jtfLocalPort);
		jtfLocalPort.setColumns(5);

		rdbtnJoinExistingSession = new JRadioButton("Join Existing Session");
		buttonGroup.add(rdbtnJoinExistingSession);
		GridBagConstraints gbc_rdbtnJoinExistingSession = new GridBagConstraints();
		gbc_rdbtnJoinExistingSession.gridwidth = 3;
		gbc_rdbtnJoinExistingSession.anchor = GridBagConstraints.WEST;
		gbc_rdbtnJoinExistingSession.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnJoinExistingSession.gridx = 0;
		gbc_rdbtnJoinExistingSession.gridy = 2;
		mainPanel.add(rdbtnJoinExistingSession, gbc_rdbtnJoinExistingSession);

		btnScan = new JButton("Scan...");
		btnScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame();
				frame.add(new ScanLocalSessionsGUI());
				frame.setVisible(true);
			}
		});
		GridBagConstraints gbc_btnScan = new GridBagConstraints();
		gbc_btnScan.insets = new Insets(0, 0, 5, 5);
		gbc_btnScan.gridx = 3;
		gbc_btnScan.gridy = 2;
		mainPanel.add(btnScan, gbc_btnScan);



		lblServerIpAddress = new JLabel("Input peer IP address/ports below (eg 192.12.13.14/2901, etc)");
		lblServerIpAddress.setEnabled(false);
		GridBagConstraints gbc_lblServerIpAddress = new GridBagConstraints();
		gbc_lblServerIpAddress.gridwidth = 4;
		gbc_lblServerIpAddress.anchor = GridBagConstraints.WEST;
		gbc_lblServerIpAddress.insets = new Insets(0, 0, 5, 5);
		gbc_lblServerIpAddress.gridx = 1;
		gbc_lblServerIpAddress.gridy = 3;
		mainPanel.add(lblServerIpAddress, gbc_lblServerIpAddress);

		textFieldPort = new JTextField();
		textFieldPort.setEnabled(false);
		textFieldPort.setText("127.0.0.1/2901");
		GridBagConstraints gbc_textFieldPort = new GridBagConstraints();
		gbc_textFieldPort.gridwidth = 4;
		gbc_textFieldPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPort.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldPort.gridx = 1;
		gbc_textFieldPort.gridy = 4;
		mainPanel.add(textFieldPort, gbc_textFieldPort);

		rdbtnStartNewSession = new JRadioButton("Start New Session");
		buttonGroup.add(rdbtnStartNewSession);

		rdbtnStartNewSession.setSelected(true);
		GridBagConstraints gbc_rdbtnStartNewSession = new GridBagConstraints();
		gbc_rdbtnStartNewSession.gridwidth = 3;
		gbc_rdbtnStartNewSession.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnStartNewSession.anchor = GridBagConstraints.WEST;
		gbc_rdbtnStartNewSession.gridx = 0;
		gbc_rdbtnStartNewSession.gridy = 5;
		mainPanel.add(rdbtnStartNewSession, gbc_rdbtnStartNewSession);
		rdbtnStartNewSession.addChangeListener(cl);



		lblTimeSignature = new JLabel("      Time Signature");
		lblTimeSignature.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblTimeSignature = new GridBagConstraints();
		gbc_lblTimeSignature.anchor = GridBagConstraints.WEST;
		gbc_lblTimeSignature.gridwidth = 2;
		gbc_lblTimeSignature.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeSignature.gridx = 1;
		gbc_lblTimeSignature.gridy = 6;
		mainPanel.add(lblTimeSignature, gbc_lblTimeSignature);

		spinner = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 3;
		gbc_spinner.gridy = 6;
		spinner.setModel(new SpinnerNumberModel(4,1,64,1));
		mainPanel.add(spinner, gbc_spinner);
		spinner.addChangeListener(changeTimeSignature);

		spinner_1 = new JSpinner();
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 4;
		gbc_spinner_1.gridy = 6;

		spinner_1.setModel(new SpinnerListModel(new Integer[]{1, 2, 4, 8, 16, 32, 64}));
		spinner_1.getModel().setValue(4);
		mainPanel.add(spinner_1, gbc_spinner_1);
		spinner_1.addChangeListener(changeTimeSignature);




		rdbtnMsPerBeat = new JRadioButton("ms Per Beat");
		buttonGroup_1.add(rdbtnMsPerBeat);
		GridBagConstraints gbc_rdbtnMsPerBeat = new GridBagConstraints();
		gbc_rdbtnMsPerBeat.gridwidth = 2;
		gbc_rdbtnMsPerBeat.anchor = GridBagConstraints.WEST;
		gbc_rdbtnMsPerBeat.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMsPerBeat.gridx = 1;
		gbc_rdbtnMsPerBeat.gridy = 7;
		mainPanel.add(rdbtnMsPerBeat, gbc_rdbtnMsPerBeat);
		rdbtnMsPerBeat.addChangeListener(cl);

		spinner_msPerBeat = new JSpinner();
		spinner_msPerBeat.setEnabled(false);
		spinner_msPerBeat.setModel(new SpinnerNumberModel(500, 10, 1000, 10));
		GridBagConstraints gbc_txt_msPerBeat = new GridBagConstraints();
		gbc_txt_msPerBeat.fill = GridBagConstraints.HORIZONTAL;
		gbc_txt_msPerBeat.insets = new Insets(0, 0, 5, 5);
		gbc_txt_msPerBeat.gridx = 3;
		gbc_txt_msPerBeat.gridy = 7;
		mainPanel.add(spinner_msPerBeat, gbc_txt_msPerBeat);

		spinner_msPerBeat.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				int val = (int)spinner_msPerBeat.getValue();
				val = val-(val%10);
				spinner_msPerBeat.setValue(val);
				txtBPM.setText(String.format("%.2f", 60000./val));
				previewConductor.changeClockSettingsNow(previewConductor.getClock().createWithDifferentTempo(val));
			}

		});
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 8;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 5;
		gbc_panel.gridy = 1;

		previewConductor = new Conductor(
				new ClockSetting(
						(int)spinner_msPerBeat.getValue(),
						(int)spinner.getValue(),
						(int)spinner_1.getValue()));
		mainPanel.add(previewConductor, gbc_panel);

		rdbtnBeatsPerMinute = new JRadioButton("Beats Per Minute");
		buttonGroup_1.add(rdbtnBeatsPerMinute);
		rdbtnBeatsPerMinute.setSelected(true);
		rdbtnBeatsPerMinute.addChangeListener(cl);

		GridBagConstraints gbc_rdbtnBeatsPerMinute = new GridBagConstraints();
		gbc_rdbtnBeatsPerMinute.gridwidth = 2;
		gbc_rdbtnBeatsPerMinute.anchor = GridBagConstraints.WEST;
		gbc_rdbtnBeatsPerMinute.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnBeatsPerMinute.gridx = 1;
		gbc_rdbtnBeatsPerMinute.gridy = 8;
		mainPanel.add(rdbtnBeatsPerMinute, gbc_rdbtnBeatsPerMinute);

		txtBPM = new JTextField();
		txtBPM.setHorizontalAlignment(SwingConstants.RIGHT);
		txtBPM.setText("120");
		GridBagConstraints gbc_txtBPM = new GridBagConstraints();
		gbc_txtBPM.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtBPM.insets = new Insets(0, 0, 5, 5);
		gbc_txtBPM.gridx = 3;
		gbc_txtBPM.gridy = 8;
		mainPanel.add(txtBPM, gbc_txtBPM);
		txtBPM.setColumns(1);

		txtBPM.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				int val = (int)(60000./Double.parseDouble(txtBPM.getText()));
				val = val-(val%10);
				spinner_msPerBeat.setValue(val);
				txtBPM.setText(String.format("%.2f", 60000./val));
				previewConductor.changeClockSettingsNow(previewConductor.getClock().createWithDifferentTempo(val));
			}

		});


		btnStart = new JButton("Start");
		getContentPane().add(btnStart, BorderLayout.SOUTH);

		JPanel tabAudioIO = new JPanel(); 
		tabs.addTab("Audio/Video IO", tabAudioIO);
		GridBagLayout gbl_tabAudioIO = new GridBagLayout();
		gbl_tabAudioIO.columnWidths = new int[]{122, 44, 0};
		gbl_tabAudioIO.rowHeights = new int[]{27, 0, 0, 0, 0, 0};
		gbl_tabAudioIO.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_tabAudioIO.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		tabAudioIO.setLayout(gbl_tabAudioIO);

		lblInput = new JLabel("Audio Input");
		GridBagConstraints gbc_lblInput = new GridBagConstraints();
		gbc_lblInput.anchor = GridBagConstraints.WEST;
		gbc_lblInput.insets = new Insets(0, 0, 5, 5);
		gbc_lblInput.gridx = 0;
		gbc_lblInput.gridy = 0;
		tabAudioIO.add(lblInput, gbc_lblInput);

		comboBox = new JComboBox();
		comboBox.setModel(getComboBoxModel(getMixers(TargetDataLine.class)));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		tabAudioIO.add(comboBox, gbc_comboBox);

		lblOutput = new JLabel("Audio Output");
		GridBagConstraints gbc_lblOutput = new GridBagConstraints();
		gbc_lblOutput.anchor = GridBagConstraints.WEST;
		gbc_lblOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutput.gridx = 0;
		gbc_lblOutput.gridy = 1;
		tabAudioIO.add(lblOutput, gbc_lblOutput);

		comboBox_1 = new JComboBox();
		comboBox_1.setModel(getComboBoxModel(getMixers(SourceDataLine.class)));
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 1;
		tabAudioIO.add(comboBox_1, gbc_comboBox_1);

		lblVideoInput = new JLabel("Video Input");
		GridBagConstraints gbc_lblVideoInput = new GridBagConstraints();
		gbc_lblVideoInput.anchor = GridBagConstraints.EAST;
		gbc_lblVideoInput.insets = new Insets(0, 0, 5, 5);
		gbc_lblVideoInput.gridx = 0;
		gbc_lblVideoInput.gridy = 3;
		tabAudioIO.add(lblVideoInput, gbc_lblVideoInput);

		List<Webcam> webcams = Webcam.getWebcams();
		List<String> webcamNames = new ArrayList();
		webcamNames.add(NO_WEBCAM);
		for(Webcam webcam : webcams){
			webcamNames.add(webcam.getName());
		}

		comboBoxWebcams = new JComboBox();
		comboBoxWebcams.setModel(new DefaultComboBoxModel(webcamNames.toArray()));
		GridBagConstraints gbc_comboBox_2 = new GridBagConstraints();
		gbc_comboBox_2.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_2.gridx = 1;
		gbc_comboBox_2.gridy = 3;
		tabAudioIO.add(comboBoxWebcams, gbc_comboBox_2);

		lblVideoResolution = new JLabel("Video Resolution");
		GridBagConstraints gbc_lblVideoResolution = new GridBagConstraints();
		gbc_lblVideoResolution.anchor = GridBagConstraints.EAST;
		gbc_lblVideoResolution.insets = new Insets(0, 0, 0, 5);
		gbc_lblVideoResolution.gridx = 0;
		gbc_lblVideoResolution.gridy = 4;
		tabAudioIO.add(lblVideoResolution, gbc_lblVideoResolution);

		comboBoxResolutions = new JComboBox();
		gbc_comboBox_2 = new GridBagConstraints();
		gbc_comboBox_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_2.gridx = 1;
		gbc_comboBox_2.gridy = 4;
		tabAudioIO.add(comboBoxResolutions, gbc_comboBox_2);

		comboBoxWebcams.addActionListener(e->{
			if(comboBoxWebcams.getSelectedItem() == NO_WEBCAM){
				comboBoxResolutions.setEnabled(false);
				lblVideoResolution.setEnabled(false);
			}
			else{
				lblVideoResolution.setEnabled(true);
				comboBoxResolutions.setEnabled(true);
				Vector<String> strings = new Vector();

				Webcam cam = Webcam.getWebcamByName((String)comboBoxWebcams.getSelectedItem());
				for(Dimension dim : cam.getViewSizes()){
					strings.add(dim.width + "x" + dim.height);
				}
				comboBoxResolutions.setModel(new DefaultComboBoxModel<String>(strings));
				comboBoxResolutions.setSelectedItem(cam.getViewSize().width + "x" + cam.getViewSize().height);

				System.out.println(strings);
			}
		});




	}

	class MixerWrapper {
		MixerWrapper(Mixer.Info info){
			this.info = info;
		}
		Mixer.Info info;
		public String toString(){
			if(info.getVersion() != null)
				return info.toString();
			else
				return info.getName() + ", version ?.?";
		}
	}

	private ComboBoxModel getComboBoxModel(Vector<Mixer.Info> mixers) {
		Vector<MixerWrapper> wrappers = new Vector();
		for(Mixer.Info mixer : mixers)
			wrappers.add(new MixerWrapper(mixer));

		DefaultComboBoxModel<Mixer.Info> cbml = new DefaultComboBoxModel(wrappers); 

		return cbml;
	}


	private Vector<Mixer.Info> getMixers(Class<? extends DataLine> class1) {
		Vector<Mixer.Info> availableMixers = new Vector();

		for(Mixer.Info info : AudioSystem.getMixerInfo()){
			if(AudioSystem.getMixer(info).isLineSupported(
					new DataLine.Info(class1, DefaultObjects.defaultFormat)))
				availableMixers.add(info);
		}
		return availableMixers;

	}


	public static void main(String arg[]){
		ClientSetupGUI_P2P_multiPeer gui = new ClientSetupGUI_P2P_multiPeer();
		gui.setVisible(true);

		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.btnStart.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				gui.previewConductor.close();

				String displayName = gui.txtUser.getText();
				int num = (int)gui.spinner.getModel().getValue();
				int denom = (int)gui.spinner_1.getModel().getValue();
				int msPerBeat = (int) gui.spinner_msPerBeat.getValue();
				boolean join = gui.rdbtnJoinExistingSession.isSelected();
				Mixer inputMixer =  AudioSystem.getMixer(((MixerWrapper)(gui.comboBox.getModel().getSelectedItem())).info);
				Mixer outputMixer = AudioSystem.getMixer(((MixerWrapper)(gui.comboBox_1.getModel().getSelectedItem())).info);

				int localPort = Integer.parseInt(gui.jtfLocalPort.getText());
				gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				gui.dispose();
				Client client;
				try {
					Webcam webcam = null;
					if (!gui.comboBoxWebcams.getSelectedItem().equals(NO_WEBCAM)){
						webcam = Webcam.getWebcamByName((String)gui.comboBoxWebcams.getSelectedItem());
						String s[] = ((String)gui.comboBoxResolutions.getSelectedItem()).split("x");

						Dimension viewSize = new Dimension(Integer.parseInt(s[0]),Integer.parseInt(s[1])); 
						webcam.setViewSize(viewSize);
					}
					if(webcam != null)
						webcam.open();
					WebcamThread webcamThread = webcam != null ? new WebcamThread(webcam) : null;
					if(join){
						InputThread input = new InputThread(inputMixer, DefaultObjects.defaultFormat, DefaultObjects.bc0);
						ClockSetting clock = DefaultObjects.bc0;
						PlaybackManager playback = new PlaybackManager(outputMixer, clock, DefaultObjects.defaultFormat);
						client = new Client(localPort, displayName, input, playback, clock,webcamThread);
						String[] entries = gui.textFieldPort.getText().trim().split(",[ \t]*");

						for(String entry : entries){
							client.joinSessionP2P(entry);
						}
					} else{
						ClockSetting clock = new ClockSetting(msPerBeat, num, denom);
						InputThread input = new InputThread(inputMixer, DefaultObjects.defaultFormat, clock);
						PlaybackManager playback = new PlaybackManager(outputMixer, clock, DefaultObjects.defaultFormat);
						System.out.println("user name is " + displayName);
						client = new Client(localPort, displayName, input, playback, clock, webcamThread);
						client.generateRandomSessionID();
						/*clock.beatsPerMeasure = num;
					clock.beatDenominator = denom;
					clock.msPerBeat = msPerBeat;*/
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
					System.exit(0);
				} 

			}
		});

	}
}
