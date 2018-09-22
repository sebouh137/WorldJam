package worldjam.gui;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import worldjam.core.BeatClock;
import worldjam.exe.Client;
import worldjam.gui.conductor.BezierConductor;
import worldjam.util.DefaultObjects;

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
import javax.swing.JPanel;;

public class ClientSetupGUI extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2630697418874820157L;
	private JTextField txtUser;
	private JTextField txtIP;
	private JTextField txtSession;
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
	private BezierConductor previewConductor;
	private JLabel lblPort;
	private JTextField textFieldPort;
	
	public ClientSetupGUI() {
		this.setSize(666, 388);
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
		JPanel mainPanel = new JPanel();
		tabs.addTab("General", mainPanel);
		this.getContentPane().add(tabs);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{83, 45, 113, 64, 64, 96, 0};
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
		txtUser.setText("user1");
		GridBagConstraints gbc_txtUser = new GridBagConstraints();
		gbc_txtUser.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUser.gridwidth = 3;
		gbc_txtUser.insets = new Insets(0, 0, 5, 5);
		gbc_txtUser.gridx = 2;
		gbc_txtUser.gridy = 0;
		mainPanel.add(txtUser, gbc_txtUser);
		txtUser.setColumns(10);
		
		
		
		JLabel lblServerIpAddress = new JLabel("  Server IP Address");
		GridBagConstraints gbc_lblServerIpAddress = new GridBagConstraints();
		gbc_lblServerIpAddress.gridwidth = 2;
		gbc_lblServerIpAddress.anchor = GridBagConstraints.WEST;
		gbc_lblServerIpAddress.insets = new Insets(0, 0, 5, 5);
		gbc_lblServerIpAddress.gridx = 0;
		gbc_lblServerIpAddress.gridy = 1;
		mainPanel.add(lblServerIpAddress, gbc_lblServerIpAddress);
		
		txtIP = new JTextField();
		txtIP.setText("127.0.0.1");
		GridBagConstraints gbc_txtIP = new GridBagConstraints();
		gbc_txtIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtIP.insets = new Insets(0, 0, 5, 5);
		gbc_txtIP.gridx = 2;
		gbc_txtIP.gridy = 1;
		mainPanel.add(txtIP, gbc_txtIP);
		txtIP.setColumns(9);
		
		lblPort = new JLabel("Port");
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblPort.gridx = 3;
		gbc_lblPort.gridy = 1;
		mainPanel.add(lblPort, gbc_lblPort);
		
		textFieldPort = new JTextField();
		textFieldPort.setText(Integer.toString(DefaultObjects.defaultPort));
		GridBagConstraints gbc_textFieldPort = new GridBagConstraints();
		gbc_textFieldPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPort.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldPort.gridx = 4;
		gbc_textFieldPort.gridy = 1;
		mainPanel.add(textFieldPort, gbc_textFieldPort);
		//textFieldPort.setColumns(1);
		
		
		
		JLabel lblSessionName = new JLabel("  Session Name");
		GridBagConstraints gbc_lblSessionName = new GridBagConstraints();
		gbc_lblSessionName.gridwidth = 2;
		gbc_lblSessionName.anchor = GridBagConstraints.WEST;
		gbc_lblSessionName.insets = new Insets(0, 0, 5, 5);
		gbc_lblSessionName.gridx = 0;
		gbc_lblSessionName.gridy = 2;
		mainPanel.add(lblSessionName, gbc_lblSessionName);
		
		txtSession = new JTextField();
		txtSession.setText("awesome jam session");
		GridBagConstraints gbc_txtSession = new GridBagConstraints();
		gbc_txtSession.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSession.gridwidth = 3;
		gbc_txtSession.insets = new Insets(0, 0, 5, 5);
		gbc_txtSession.gridx = 2;
		gbc_txtSession.gridy = 2;
		mainPanel.add(txtSession, gbc_txtSession);
		txtSession.setColumns(10);
		
		rdbtnJoinExistingSession = new JRadioButton("Join Existing Session");
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
				
				if(rdbtnMsPerBeat.isSelected() && enable){
					txtBPM.setEnabled(false);
					spinner_msPerBeat.setEnabled(true);
				} else if (rdbtnBeatsPerMinute.isSelected() && enable){
					spinner_msPerBeat.setEnabled(false);
					txtBPM.setEnabled(true);
				}
				enable &= tabs.getSelectedComponent() == mainPanel;
				previewConductor.setVisible(enable);
			}
		};
		rdbtnJoinExistingSession.addChangeListener(cl);
		buttonGroup.add(rdbtnJoinExistingSession);
		tabs.addChangeListener(cl);
		GridBagConstraints gbc_rdbtnJoinExistingSession = new GridBagConstraints();
		gbc_rdbtnJoinExistingSession.gridwidth = 3;
		gbc_rdbtnJoinExistingSession.anchor = GridBagConstraints.WEST;
		gbc_rdbtnJoinExistingSession.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnJoinExistingSession.gridx = 0;
		gbc_rdbtnJoinExistingSession.gridy = 3;
		mainPanel.add(rdbtnJoinExistingSession, gbc_rdbtnJoinExistingSession);
		
		rdbtnStartNewSession = new JRadioButton("Start New Session");
		buttonGroup.add(rdbtnStartNewSession);
		
		rdbtnStartNewSession.setSelected(true);
		GridBagConstraints gbc_rdbtnStartNewSession = new GridBagConstraints();
		gbc_rdbtnStartNewSession.gridwidth = 3;
		gbc_rdbtnStartNewSession.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnStartNewSession.anchor = GridBagConstraints.WEST;
		gbc_rdbtnStartNewSession.gridx = 0;
		gbc_rdbtnStartNewSession.gridy = 4;
		mainPanel.add(rdbtnStartNewSession, gbc_rdbtnStartNewSession);
		rdbtnStartNewSession.addChangeListener(cl);
		
		
		
		lblTimeSignature = new JLabel("      Time Signature");
		lblTimeSignature.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblTimeSignature = new GridBagConstraints();
		gbc_lblTimeSignature.anchor = GridBagConstraints.WEST;
		gbc_lblTimeSignature.gridwidth = 2;
		gbc_lblTimeSignature.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeSignature.gridx = 1;
		gbc_lblTimeSignature.gridy = 5;
		mainPanel.add(lblTimeSignature, gbc_lblTimeSignature);
		
		 spinner = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 3;
		gbc_spinner.gridy = 5;
		spinner.setModel(new SpinnerNumberModel(4,1,64,1));
		mainPanel.add(spinner, gbc_spinner);
		ChangeListener changeTimeSignature = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				BeatClock clock = previewConductor.getClock().createWithDifferentBeatCount((int)spinner.getValue());
				previewConductor.setClock(clock);
			}
		};
		spinner.addChangeListener(changeTimeSignature);
		
		spinner_1 = new JSpinner();
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 4;
		gbc_spinner_1.gridy = 5;

		spinner_1.setModel(new SpinnerListModel(new Integer[]{1, 2, 4, 8, 16, 32, 64}));
		spinner_1.getModel().setValue(4);
		mainPanel.add(spinner_1, gbc_spinner_1);
		spinner_1.addChangeListener(changeTimeSignature);
		
		rdbtnBeatsPerMinute = new JRadioButton("Beats Per Minute");
		buttonGroup_1.add(rdbtnBeatsPerMinute);
		rdbtnBeatsPerMinute.setSelected(true);
		rdbtnBeatsPerMinute.addChangeListener(cl);
		
		GridBagConstraints gbc_rdbtnBeatsPerMinute = new GridBagConstraints();
		gbc_rdbtnBeatsPerMinute.gridwidth = 2;
		gbc_rdbtnBeatsPerMinute.anchor = GridBagConstraints.WEST;
		gbc_rdbtnBeatsPerMinute.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnBeatsPerMinute.gridx = 1;
		gbc_rdbtnBeatsPerMinute.gridy = 6;
		mainPanel.add(rdbtnBeatsPerMinute, gbc_rdbtnBeatsPerMinute);
		
		txtBPM = new JTextField();
		txtBPM.setText("120");
		GridBagConstraints gbc_txtBPM = new GridBagConstraints();
		gbc_txtBPM.gridwidth = 2;
		gbc_txtBPM.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtBPM.insets = new Insets(0, 0, 5, 5);
		gbc_txtBPM.gridx = 3;
		gbc_txtBPM.gridy = 6;
		mainPanel.add(txtBPM, gbc_txtBPM);
		txtBPM.setColumns(1);
		
		txtBPM.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				
				int val = (int)(60000./Double.parseDouble(txtBPM.getText()));
				val = val-(val%10);
				spinner_msPerBeat.setValue(val);
				txtBPM.setText(String.format("%.2f", 60000./val));
				previewConductor.setClock(previewConductor.getClock().createWithDifferentTempo(val));
			}
			
		});
		
		
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
		gbc_txt_msPerBeat.gridwidth = 2;
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
				previewConductor.setClock(previewConductor.getClock().createWithDifferentTempo(val));
			}
			
		});
		
		previewConductor = new BezierConductor(
				new BeatClock(
						(int)spinner_msPerBeat.getValue(),
						(int)spinner.getValue(),
						(int)spinner_1.getValue()));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 8;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 5;
		gbc_panel.gridy = 1;
		mainPanel.add(previewConductor, gbc_panel);
		
		JPanel tabAudioIO = new JPanel(); 
		tabs.addTab("Audio IO", tabAudioIO);
		GridBagLayout gbl_tabAudioIO = new GridBagLayout();
		gbl_tabAudioIO.columnWidths = new int[]{122, 44, 0};
		gbl_tabAudioIO.rowHeights = new int[]{27, 0, 0};
		gbl_tabAudioIO.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_tabAudioIO.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		tabAudioIO.setLayout(gbl_tabAudioIO);
		
		lblInput = new JLabel("Input");
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
		
		lblOutput = new JLabel("Output");
		GridBagConstraints gbc_lblOutput = new GridBagConstraints();
		gbc_lblOutput.anchor = GridBagConstraints.WEST;
		gbc_lblOutput.insets = new Insets(0, 0, 0, 5);
		gbc_lblOutput.gridx = 0;
		gbc_lblOutput.gridy = 1;
		tabAudioIO.add(lblOutput, gbc_lblOutput);
		
		comboBox_1 = new JComboBox();
		comboBox_1.setModel(getComboBoxModel(getMixers(SourceDataLine.class)));
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 1;
		tabAudioIO.add(comboBox_1, gbc_comboBox_1);
		
		
		btnStart = new JButton("Start");
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.gridwidth = 6;
		gbc_btnStart.gridx = 0;
		gbc_btnStart.gridy = 9;
		mainPanel.add(btnStart, gbc_btnStart);
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
			String serverIP = gui.txtIP.getText();
			String sessionName = gui.txtSession.getText();
			String displayName = gui.txtUser.getText();
			int num = (int)gui.spinner.getModel().getValue();
			int denom = (int)gui.spinner_1.getModel().getValue();
			int msPerBeat = (int) gui.spinner_msPerBeat.getValue();
			boolean join = gui.rdbtnJoinExistingSession.isSelected();
			Mixer inputMixer =  AudioSystem.getMixer(((MixerWrapper)(gui.comboBox.getModel().getSelectedItem())).info);
			Mixer outputMixer = AudioSystem.getMixer(((MixerWrapper)(gui.comboBox_1.getModel().getSelectedItem())).info);

			int port = Integer.parseInt(gui.textFieldPort.getText());
			gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			gui.dispose();
			Client client;
			try {
				client = new Client(serverIP, port, sessionName, displayName, inputMixer, outputMixer);
				if(join){
					client.joinSession();
				} else{
					BeatClock clock = new BeatClock(msPerBeat, num, denom);
					/*clock.beatsPerMeasure = num;
					clock.beatDenominator = denom;
					clock.msPerBeat = msPerBeat;*/
					client.startNewSession(clock);
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