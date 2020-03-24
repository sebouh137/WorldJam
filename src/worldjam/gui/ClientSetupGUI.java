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
import worldjam.util.Configurations;
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

public class ClientSetupGUI extends JFrame{
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
	private JSpinner spinnerNumerator;
	private JSpinner spinnerDenominator;
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
	//private JTextField textFieldPort;
	private JLabel lblLocalPort;
	private JTextField jtfLocalPort;
	private JLabel lblVideoInput;
	private JComboBox comboBoxWebcams;
	private JLabel lblVideoResolution;
	private JComboBox comboBoxResolutions;
	protected ScanLocalSessionsGUI scanPanel;

	JPanel newSessionPanel;
	ClientSetupGUI() {
		this.setSize(544, 388);
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
		mainPanel.setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		mainPanel.add(topPanel, BorderLayout.NORTH);

		tabs.addTab("General", mainPanel);
		

		JLabel lblDisplayName = new JLabel("  Display Name");

		topPanel.add(lblDisplayName);
		topPanel.setPreferredSize(new Dimension(370,70));

		txtUser = new JTextField();
		txtUser.setText(Configurations.getDefaultUsername());
		topPanel.add(txtUser);
		txtUser.setColumns(10);
		lblLocalPort = new JLabel("Local Port");
		topPanel.add(lblLocalPort);

		jtfLocalPort = new JTextField(Integer.toString(DefaultObjects.defaultPort));
		topPanel.add(jtfLocalPort);
		jtfLocalPort.setColumns(5);

		rdbtnStartNewSession = new JRadioButton("Start New Session");
		buttonGroup.add(rdbtnStartNewSession);

		rdbtnStartNewSession.setSelected(true);
		GridBagConstraints gbc_rdbtnStartNewSession = new GridBagConstraints();
		topPanel.add(rdbtnStartNewSession, gbc_rdbtnStartNewSession);

		rdbtnJoinExistingSession = new JRadioButton("Join Existing Session");
		buttonGroup.add(rdbtnJoinExistingSession);
		topPanel.add(rdbtnJoinExistingSession);

		scanPanel = new ScanLocalSessionsGUI();

		
		ChangeListener cl = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				boolean enable = rdbtnStartNewSession.isSelected();
				lblTimeSignature.setEnabled(enable);
				rdbtnBeatsPerMinute.setEnabled(enable);
				rdbtnMsPerBeat.setEnabled(enable);
				spinnerNumerator.setEnabled(enable);
				spinnerDenominator.setEnabled(enable);
				txtBPM.setEnabled(enable);
				spinner_msPerBeat.setEnabled(enable);

				
				enable &= tabs.getSelectedComponent() == newSessionPanel;
				previewConductor.setMeasureNumberVisible(false);
				if(rdbtnStartNewSession.isSelected()) {
					mainPanel.remove(scanPanel);
					mainPanel.add(newSessionPanel,BorderLayout.CENTER);
					mainPanel.revalidate();
					revalidate();
					repaint();

				} else {
					mainPanel.remove(newSessionPanel);
					mainPanel.add(scanPanel,BorderLayout.CENTER);
					mainPanel.revalidate();
					revalidate();
					repaint();
				}
			}
		};
		rdbtnStartNewSession.addChangeListener(cl);
		tabs.addChangeListener(cl);

		newSessionPanel = createNewSessionPanel();

		mainPanel.add(newSessionPanel,BorderLayout.CENTER);

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

	private JPanel createNewSessionPanel() {
		newSessionPanel = new JPanel();
		JPanel subPanel = new JPanel();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{148, 65};

		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		subPanel.setLayout(gridBagLayout);
		
		newSessionPanel.setLayout(new BorderLayout());
		newSessionPanel.add(subPanel, BorderLayout.CENTER);

		lblTimeSignature = new JLabel("Time Signature");
		lblTimeSignature.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblTimeSignature = new GridBagConstraints();
		gbc_lblTimeSignature.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTimeSignature.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeSignature.gridx = 0;
		gbc_lblTimeSignature.gridy = 1;
		subPanel.add(lblTimeSignature, gbc_lblTimeSignature);

		ChangeListener changeTimeSignature = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				ClockSetting clock = previewConductor.getClock().createWithDifferentBeatCount((int)spinnerNumerator.getValue());
				previewConductor.changeClockSettingsNow(clock);
			}
		};
		
		spinnerNumerator = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.EAST;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 1;
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		spinnerNumerator.setModel(new SpinnerListModel(new Integer[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24}));
		spinnerNumerator.getModel().setValue(4);
		subPanel.add(spinnerNumerator, gbc_spinner);
		spinnerNumerator.addChangeListener(changeTimeSignature);

		spinnerDenominator = new JSpinner();
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.anchor = GridBagConstraints.EAST;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 1;
		gbc_spinner_1.gridy = 2;
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;

		spinnerDenominator.setModel(new SpinnerListModel(new Integer[]{1, 2, 4, 8, 16}));
		spinnerDenominator.getModel().setValue(4);
		subPanel.add(spinnerDenominator, gbc_spinner_1);
		spinnerDenominator.addChangeListener(changeTimeSignature);

		
		ChangeListener cl = e->{
			if(rdbtnMsPerBeat.isSelected()){
				txtBPM.setEnabled(false);
				spinner_msPerBeat.setEnabled(true);
			} else if (rdbtnBeatsPerMinute.isSelected()){
				spinner_msPerBeat.setEnabled(false);
				txtBPM.setEnabled(true);
			}
		};
		
		rdbtnBeatsPerMinute = new JRadioButton("Beats Per Minute");
		buttonGroup_1.add(rdbtnBeatsPerMinute);
		rdbtnBeatsPerMinute.setSelected(true);
		rdbtnBeatsPerMinute.addChangeListener(cl);




		rdbtnMsPerBeat = new JRadioButton("ms Per Beat");
		buttonGroup_1.add(rdbtnMsPerBeat);
		GridBagConstraints gbc_rdbtnMsPerBeat = new GridBagConstraints();
		gbc_rdbtnMsPerBeat.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnMsPerBeat.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnMsPerBeat.gridx = 0;
		gbc_rdbtnMsPerBeat.gridy = 4;
		subPanel.add(rdbtnMsPerBeat, gbc_rdbtnMsPerBeat);
		rdbtnMsPerBeat.addChangeListener(cl);


		spinner_msPerBeat = new JSpinner();
		spinner_msPerBeat.setEnabled(false);
		spinner_msPerBeat.setModel(new SpinnerNumberModel(500, 10, 1000, 10));
		previewConductor = new Conductor(
				new ClockSetting(
						(int)spinner_msPerBeat.getValue(),
						(int)spinnerNumerator.getValue(),
						(int)spinnerDenominator.getValue()));
		previewConductor.setPreferredSize(new Dimension(280,280));
		GridBagConstraints gbc_txt_msPerBeat = new GridBagConstraints();
		gbc_txt_msPerBeat.fill = GridBagConstraints.HORIZONTAL;
		gbc_txt_msPerBeat.insets = new Insets(0, 0, 5, 5);
		gbc_txt_msPerBeat.gridx = 1;
		gbc_txt_msPerBeat.gridy = 4;
		subPanel.add(spinner_msPerBeat, gbc_txt_msPerBeat);

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

		GridBagConstraints gbc_rdbtnBeatsPerMinute = new GridBagConstraints();
		gbc_rdbtnBeatsPerMinute.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnBeatsPerMinute.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnBeatsPerMinute.gridx = 0;
		gbc_rdbtnBeatsPerMinute.gridy = 5;
		subPanel.add(rdbtnBeatsPerMinute, gbc_rdbtnBeatsPerMinute);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 7;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 3;
		gbc_panel.gridy = 1;
		newSessionPanel.add(previewConductor, BorderLayout.EAST);	
		txtBPM = new JTextField();
		txtBPM.setHorizontalAlignment(SwingConstants.RIGHT);
		txtBPM.setText("120");
		GridBagConstraints gbc_txtBPM = new GridBagConstraints();
		gbc_txtBPM.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtBPM.insets = new Insets(0, 0, 5, 5);
		gbc_txtBPM.gridx = 1;
		gbc_txtBPM.gridy = 5;
		subPanel.add(txtBPM, gbc_txtBPM);
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
		return newSessionPanel;
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
		ClientSetupGUI gui = new ClientSetupGUI();
		gui.setVisible(true);

		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.btnStart.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				gui.previewConductor.close();

				String displayName = gui.txtUser.getText();
				int num = (int)gui.spinnerNumerator.getModel().getValue();
				int denom = (int)gui.spinnerDenominator.getModel().getValue();
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
						String[] entries = gui.scanPanel.getSelection().split(",[ \t]*");

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
