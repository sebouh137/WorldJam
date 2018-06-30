package worldjam.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import worldjam.core.BeatClock;
import worldjam.exe.DefaultClient;
import worldjam.util.DefaultObjects;

import javax.swing.JSpinner;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;;

public class DefaultClientSetupGUI extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2630697418874820157L;
	private JTextField txtUser;
	private JTextField txtIP;
	private JTextField txtSession;
	private JTextField txtBPM;
	private JTextField txt_msPerBeat;
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
	private JComboBox comboBox;
	private JLabel lblOutput;
	private JComboBox comboBox_1;
	public DefaultClientSetupGUI() {
		this.setSize(769, 304);
		setTitle("Client Setup");
		/*Image image;
		try {
			image = ImageIO.read(new File("img/icons/wj_logo.png"));
			this.setIconImage(image);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{53, 76, 0, 56, 56, 91, 157, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblDisplayName = new JLabel("  Display Name");
		GridBagConstraints gbc_lblDisplayName = new GridBagConstraints();
		gbc_lblDisplayName.gridwidth = 2;
		gbc_lblDisplayName.anchor = GridBagConstraints.WEST;
		gbc_lblDisplayName.insets = new Insets(0, 0, 5, 5);
		gbc_lblDisplayName.gridx = 0;
		gbc_lblDisplayName.gridy = 0;
		getContentPane().add(lblDisplayName, gbc_lblDisplayName);
		
		txtUser = new JTextField();
		txtUser.setText("user1");
		GridBagConstraints gbc_txtUser = new GridBagConstraints();
		gbc_txtUser.gridwidth = 3;
		gbc_txtUser.insets = new Insets(0, 0, 5, 5);
		gbc_txtUser.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUser.gridx = 2;
		gbc_txtUser.gridy = 0;
		getContentPane().add(txtUser, gbc_txtUser);
		txtUser.setColumns(10);
		
		lblInput = new JLabel("Input");
		GridBagConstraints gbc_lblInput = new GridBagConstraints();
		gbc_lblInput.anchor = GridBagConstraints.EAST;
		gbc_lblInput.insets = new Insets(0, 0, 5, 5);
		gbc_lblInput.gridx = 5;
		gbc_lblInput.gridy = 0;
		getContentPane().add(lblInput, gbc_lblInput);
		
		comboBox = new JComboBox();
		comboBox.setModel(getComboBoxModel(getMixers(TargetDataLine.class)));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 6;
		gbc_comboBox.gridy = 0;
		getContentPane().add(comboBox, gbc_comboBox);
		
		JLabel lblServerIpAddress = new JLabel("  Server IP Address");
		GridBagConstraints gbc_lblServerIpAddress = new GridBagConstraints();
		gbc_lblServerIpAddress.gridwidth = 2;
		gbc_lblServerIpAddress.anchor = GridBagConstraints.WEST;
		gbc_lblServerIpAddress.insets = new Insets(0, 0, 5, 5);
		gbc_lblServerIpAddress.gridx = 0;
		gbc_lblServerIpAddress.gridy = 1;
		getContentPane().add(lblServerIpAddress, gbc_lblServerIpAddress);
		
		txtIP = new JTextField();
		txtIP.setText("127.0.0.1");
		GridBagConstraints gbc_txtIP = new GridBagConstraints();
		gbc_txtIP.gridwidth = 3;
		gbc_txtIP.insets = new Insets(0, 0, 5, 5);
		gbc_txtIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtIP.gridx = 2;
		gbc_txtIP.gridy = 1;
		getContentPane().add(txtIP, gbc_txtIP);
		txtIP.setColumns(10);
		
		lblOutput = new JLabel("Output");
		GridBagConstraints gbc_lblOutput = new GridBagConstraints();
		gbc_lblOutput.anchor = GridBagConstraints.EAST;
		gbc_lblOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutput.gridx = 5;
		gbc_lblOutput.gridy = 1;
		getContentPane().add(lblOutput, gbc_lblOutput);
		
		comboBox_1 = new JComboBox();
		comboBox_1.setModel(getComboBoxModel(getMixers(SourceDataLine.class)));
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 6;
		gbc_comboBox_1.gridy = 1;
		getContentPane().add(comboBox_1, gbc_comboBox_1);
		
		JLabel lblSessionName = new JLabel("  Session Name");
		GridBagConstraints gbc_lblSessionName = new GridBagConstraints();
		gbc_lblSessionName.gridwidth = 2;
		gbc_lblSessionName.anchor = GridBagConstraints.WEST;
		gbc_lblSessionName.insets = new Insets(0, 0, 5, 5);
		gbc_lblSessionName.gridx = 0;
		gbc_lblSessionName.gridy = 2;
		getContentPane().add(lblSessionName, gbc_lblSessionName);
		
		txtSession = new JTextField();
		txtSession.setText("awesome jam session");
		GridBagConstraints gbc_txtSession = new GridBagConstraints();
		gbc_txtSession.gridwidth = 3;
		gbc_txtSession.insets = new Insets(0, 0, 5, 5);
		gbc_txtSession.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSession.gridx = 2;
		gbc_txtSession.gridy = 2;
		getContentPane().add(txtSession, gbc_txtSession);
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
				txt_msPerBeat.setEnabled(enable);
				
				if(rdbtnMsPerBeat.isSelected() && enable){
					txtBPM.setEnabled(false);
					txt_msPerBeat.setEnabled(true);
				} else if (rdbtnBeatsPerMinute.isSelected() && enable){
					txt_msPerBeat.setEnabled(false);
					txtBPM.setEnabled(true);
				}
			}
		};
		rdbtnJoinExistingSession.addChangeListener(cl);
		buttonGroup.add(rdbtnJoinExistingSession);
		GridBagConstraints gbc_rdbtnJoinExistingSession = new GridBagConstraints();
		gbc_rdbtnJoinExistingSession.gridwidth = 3;
		gbc_rdbtnJoinExistingSession.anchor = GridBagConstraints.WEST;
		gbc_rdbtnJoinExistingSession.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnJoinExistingSession.gridx = 0;
		gbc_rdbtnJoinExistingSession.gridy = 3;
		getContentPane().add(rdbtnJoinExistingSession, gbc_rdbtnJoinExistingSession);
		
		rdbtnStartNewSession = new JRadioButton("Start New Session");
		buttonGroup.add(rdbtnStartNewSession);
		rdbtnStartNewSession.setSelected(true);
		GridBagConstraints gbc_rdbtnStartNewSession = new GridBagConstraints();
		gbc_rdbtnStartNewSession.gridwidth = 3;
		gbc_rdbtnStartNewSession.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnStartNewSession.anchor = GridBagConstraints.WEST;
		gbc_rdbtnStartNewSession.gridx = 0;
		gbc_rdbtnStartNewSession.gridy = 4;
		getContentPane().add(rdbtnStartNewSession, gbc_rdbtnStartNewSession);
		rdbtnStartNewSession.addChangeListener(cl);
		
		lblTimeSignature = new JLabel("    Time Signature");
		lblTimeSignature.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblTimeSignature = new GridBagConstraints();
		gbc_lblTimeSignature.gridwidth = 2;
		gbc_lblTimeSignature.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeSignature.gridx = 1;
		gbc_lblTimeSignature.gridy = 5;
		getContentPane().add(lblTimeSignature, gbc_lblTimeSignature);
		
		 spinner = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 3;
		gbc_spinner.gridy = 5;
		spinner.setModel(new SpinnerNumberModel(4,1,64,1));
		getContentPane().add(spinner, gbc_spinner);
		
		spinner_1 = new JSpinner();
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 4;
		gbc_spinner_1.gridy = 5;

		spinner_1.setModel(new SpinnerListModel(new Integer[]{1, 2, 4, 8, 16, 32, 64}));
		spinner_1.getModel().setValue(4);
		getContentPane().add(spinner_1, gbc_spinner_1);
		
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
		getContentPane().add(rdbtnBeatsPerMinute, gbc_rdbtnBeatsPerMinute);
		
		txtBPM = new JTextField();
		txtBPM.setText("120");
		GridBagConstraints gbc_txtBPM = new GridBagConstraints();
		gbc_txtBPM.gridwidth = 2;
		gbc_txtBPM.insets = new Insets(0, 0, 5, 5);
		gbc_txtBPM.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtBPM.gridx = 3;
		gbc_txtBPM.gridy = 6;
		getContentPane().add(txtBPM, gbc_txtBPM);
		txtBPM.setColumns(10);
		
		txtBPM.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				
				int val = (int)(60000./Double.parseDouble(txtBPM.getText()));
				val = val-(val%10);
				txt_msPerBeat.setText(Integer.toString(val));
				txtBPM.setText(String.format("%.2f", 60000./val));
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
		getContentPane().add(rdbtnMsPerBeat, gbc_rdbtnMsPerBeat);
		rdbtnMsPerBeat.addChangeListener(cl);
		
		txt_msPerBeat = new JTextField();
		txt_msPerBeat.setEnabled(false);
		txt_msPerBeat.setText("500");
		txt_msPerBeat.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				int val = Integer.parseInt(txt_msPerBeat.getText());
				val = val-(val%10);
				txt_msPerBeat.setText(Integer.toString(val));
				txtBPM.setText(String.format("%.2f", 60000./val));
			}
			
		});
		GridBagConstraints gbc_txt_msPerBeat = new GridBagConstraints();
		gbc_txt_msPerBeat.gridwidth = 2;
		gbc_txt_msPerBeat.insets = new Insets(0, 0, 5, 5);
		gbc_txt_msPerBeat.fill = GridBagConstraints.HORIZONTAL;
		gbc_txt_msPerBeat.gridx = 3;
		gbc_txt_msPerBeat.gridy = 7;
		getContentPane().add(txt_msPerBeat, gbc_txt_msPerBeat);
		txt_msPerBeat.setColumns(10);
		
		btnStart = new JButton("Start");
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.insets = new Insets(0, 0, 0, 5);
		gbc_btnStart.gridwidth = 7;
		gbc_btnStart.gridx = 0;
		gbc_btnStart.gridy = 9;
		getContentPane().add(btnStart, gbc_btnStart);
	}
	
	
	private ComboBoxModel getComboBoxModel(Object[] mixers) {
		DefaultComboBoxModel cbml = new DefaultComboBoxModel(mixers); 
		return cbml;
	}


	private Object[] getMixers(Class<? extends DataLine> class1) {
		ArrayList<Mixer.Info> availableMixers = new ArrayList();
		for(Mixer.Info info : AudioSystem.getMixerInfo()){
			if(AudioSystem.getMixer(info).isLineSupported(
					new DataLine.Info(class1, DefaultObjects.defaultFormat)))
				availableMixers.add(info);
		}
		System.out.println(availableMixers.size());
		return availableMixers.toArray();
	}


	public static void main(String arg[]){
		DefaultClientSetupGUI gui = new DefaultClientSetupGUI();
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
			int msPerBeat = Integer.parseInt(gui.txt_msPerBeat.getText());
			boolean join = gui.rdbtnJoinExistingSession.isSelected();
			Mixer inputMixer =  AudioSystem.getMixer((Mixer.Info)gui.comboBox.getModel().getSelectedItem());
			Mixer outputMixer = AudioSystem.getMixer((Mixer.Info)gui.comboBox_1.getModel().getSelectedItem());

			gui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			gui.dispose();
			DefaultClient client;
			try {
				client = new DefaultClient(serverIP, sessionName, displayName, inputMixer, outputMixer);
				if(join){
					client.joinSession();
				} else{
					BeatClock clock = new BeatClock();
					clock.beatsPerMeasure = num;
					clock.beatDenominator = denom;
					clock.msPerBeat = msPerBeat;
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
