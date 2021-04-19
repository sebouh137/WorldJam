package worldjam.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import worldjam.audio.PlaybackChannel;
import worldjam.exe.Client;
import worldjam.util.ConfigurationsXML;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class TimeCalibrationDialog extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4829724163657688277L;
	private JSpinner spinner;
	private JButton prevButton;
	private JButton nextButton;
	private JTextArea instructions;
	private Map<Long,Boolean> initialMuting = new HashMap();
	private JCheckBox chckbxSaveTheseSettings;
	public TimeCalibrationDialog(Client client) {
		this.client = client;
		
		//only do this if this is during certain types of tests
		if(client.getInput() == null || client.getPlaybackManager() == null)
			return;
		
		inputMixerName = ConfigurationsXML.getActualMixerName(client.getInput().getMixer().getMixerInfo().getName(),true);
		outputMixerName = ConfigurationsXML.getActualMixerName(client.getPlaybackManager().getMixer().getMixerInfo().getName(),false);
		// keep track of which channels are initially muted, and then bring it back to 
		// the original setting after the calibration is done.
		for(PlaybackChannel channel : client.getPlaybackManager().getChannels()) {
			initialMuting.put(channel.getChannelID(), channel.isMuted());
			channel.setMuted(true);
		}
		client.getGUI().channelsChanged();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				for (Map.Entry<Long,Boolean> entry : initialMuting.entrySet()) {
					long id = entry.getKey();
					boolean muted = entry.getValue();
					client.getPlaybackManager().getChannel(id).setMuted(muted);
				}
				client.getGUI().channelsChanged();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				for (Map.Entry<Long,Boolean> entry : initialMuting.entrySet()) {
					long id = entry.getKey();
					boolean muted = entry.getValue();
					client.getPlaybackManager().getChannel(id).setMuted(muted);
				}
				if(chckbxSaveTheseSettings.isSelected())
					saveCurrentCalibrations();
				client.getGUI().channelsChanged();
			}

		});

		setTitle("Timing Calibration");
		setSize(405,394);
		getContentPane().setLayout(new BorderLayout());
		instructions = new JTextArea("Output timing calibration for:  " + 
				outputMixerName + 
				"\n\n" + outputCalibInstructions);
		instructions.setEditable(false);
		instructions.setLineWrap(true);
		instructions.setWrapStyleWord(true);







		JPanel buttonPanel = new JPanel();

		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		gbl_buttonPanel.columnWidths = new int[]{177, 50, 50, 50, 0};
		gbl_buttonPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_buttonPanel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_buttonPanel.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		buttonPanel.setLayout(gbl_buttonPanel);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(instructions, BorderLayout.CENTER);


		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		prevButton = new JButton("Prev");
		prevButton.setEnabled(false);
		prevButton.addActionListener((e->{
			setMode(0);
		}));
		JLabel lblOffsetms = new JLabel("Offset (ms):");
		GridBagConstraints gbc_lblOffsetms = new GridBagConstraints();
		gbc_lblOffsetms.anchor = GridBagConstraints.EAST;
		gbc_lblOffsetms.insets = new Insets(0, 0, 5, 5);
		gbc_lblOffsetms.gridx = 0;
		gbc_lblOffsetms.gridy = 0;
		buttonPanel.add(lblOffsetms, gbc_lblOffsetms);

		spinner = new JSpinner();
		spinner.setValue(getOutputCalibration());
		spinner.addChangeListener(e-> {

			int i = (int)spinner.getValue();
			if(mode == 1) {
				setInputCalibration(i);
			} else {
				setOutputCalibration(i);
			}

		});

		GridBagConstraints gbc_input = new GridBagConstraints();
		gbc_input.gridwidth = 2;
		gbc_input.fill = GridBagConstraints.HORIZONTAL;
		gbc_input.insets = new Insets(0, 0, 5, 5);
		gbc_input.gridx = 1;
		gbc_input.gridy = 0;
		buttonPanel.add(spinner, gbc_input);
		//getContentPane().add(tabbedPane, BorderLayout.CENTER);


		SpinnerNumberModel nsm = (SpinnerNumberModel) spinner.getModel();
		JLabel label_1 = new JLabel("Adjustment increment (ms):");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.anchor = GridBagConstraints.EAST;
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 0;
		gbc_label_1.gridy = 1;
		buttonPanel.add(label_1, gbc_label_1);
		chckbxSaveTheseSettings = new JCheckBox("Save these settings for future sessions");
		GridBagConstraints gbc_chckbxSaveTheseSettings = new GridBagConstraints();
		gbc_chckbxSaveTheseSettings.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxSaveTheseSettings.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxSaveTheseSettings.gridwidth = 4;
		gbc_chckbxSaveTheseSettings.gridx = 0;
		gbc_chckbxSaveTheseSettings.gridy = 2;
		buttonPanel.add(chckbxSaveTheseSettings, gbc_chckbxSaveTheseSettings);
		buttonPanel.setPreferredSize(new Dimension((int)chckbxSaveTheseSettings.getPreferredSize().getWidth() + 5,
				(int)buttonPanel.getPreferredSize().getHeight()*2));
		GridBagConstraints gbc_prevButton = new GridBagConstraints();
		gbc_prevButton.anchor = GridBagConstraints.WEST;
		gbc_prevButton.insets = new Insets(0, 0, 0, 5);
		gbc_prevButton.gridx = 0;
		gbc_prevButton.gridy = 3;
		buttonPanel.add(prevButton, gbc_prevButton);
		nextButton = new JButton("Next");
		nextButton.addActionListener(e->{
			if(mode==0) {
				setMode(1);
			} else {
				if(chckbxSaveTheseSettings.isSelected()) {
					saveCurrentCalibrations();
				}
				dispose();
			}
		});
		GridBagConstraints gbc_nextButton = new GridBagConstraints();
		gbc_nextButton.anchor = GridBagConstraints.WEST;
		gbc_nextButton.gridx = 3;
		gbc_nextButton.gridy = 3;
		buttonPanel.add(nextButton, gbc_nextButton);
		nsm.setStepSize(100);
		JRadioButton jrb100 = new JRadioButton("100");
		jrb100.setSelected(true);
		JRadioButton jrb10 = new JRadioButton("10");
		JRadioButton jrb1 = new JRadioButton("1");

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 1;
			gbc.gridy = 1;
			buttonPanel.add(jrb100, gbc);
		}

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 2;
			gbc.gridy = 1;
			buttonPanel.add(jrb10, gbc);
		}
		
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 3;
			gbc.gridy = 1;
			buttonPanel.add(jrb1, gbc);
		} 
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(jrb100); bg.add(jrb10); bg.add(jrb1);

		ActionListener al = e->{
			if(jrb100.isSelected()) {
				nsm.setStepSize(100);
			} else if(jrb10.isSelected()) {
				nsm.setStepSize(10);
			} if(jrb1.isSelected()) {
				nsm.setStepSize(1);
			}
		};
		jrb100.addActionListener(al);
		jrb10.addActionListener(al);
		jrb1.addActionListener(al);

		setMode(0);
	}

	private void saveCurrentCalibrations() {
		ConfigurationsXML.saveCalibrationConstants(outputMixerName, false, getOutputCalibration());
		ConfigurationsXML.saveCalibrationConstants(inputMixerName, true, getInputCalibration());

	}

	private Client client;
	private String inputMixerName;
	private String outputMixerName;
	private void setMode(int i) {
		if (i == 1) { 
			prevButton.setEnabled(true);
			mode = 1;
			nextButton.setText("Done");
			instructions.setText("Input timing calibration for:  " + 
					inputMixerName + 
					"\n\n" + inputCalibInstructions);
			spinner.setValue(getInputCalibration());
			if(client != null) {
				client.getPlaybackManager().getChannelByName("metronome").setMuted(false);
				client.getPlaybackManager().getChannelByName("loopback").setMuted(false);
				client.getGUI().channelsChanged();
			}
		} else if (i == 0) {
			prevButton.setEnabled(false);
			mode = 0;
			nextButton.setText("Next");
			instructions.setText("Output timing calibration for:  " + 
					outputMixerName + 
					"\n\n" + outputCalibInstructions);
			spinner.setValue(getOutputCalibration());
			if(client != null) {
				client.getPlaybackManager().getChannelByName("metronome").setMuted(false);
				client.getPlaybackManager().getChannelByName("loopback").setMuted(true);
				client.getGUI().channelsChanged();
			}
		}
	}
	int mode = 0;

	private synchronized void adjustOffset(int adjustment) {
		if(mode == 0) {
			int newVal = getOutputCalibration()+adjustment;
			spinner.setValue(newVal);
			setOutputCalibration(newVal);
		}
		if(mode == 1) {
			int newVal = getInputCalibration()+adjustment;
			spinner.setValue(newVal);
			setInputCalibration(newVal);
		}
	}



	private void setInputCalibration(int i) {
		if(client != null)
			client.getInput().setTimeCalibration(i);
	}

	private int getInputCalibration() {
		if(client != null)
			return client.getInput().getTimeCalibration();
		return 0;
	}

	private void setOutputCalibration(int i) {
		if(client != null) {
			client.getPlaybackManager().setTimeCalibration(i);
		}

	}

	private int getOutputCalibration() {
		if (client != null) {
			return client.getPlaybackManager().getTimeCalibration();
		}
		return 0;
	}
	static String outputCalibInstructions;
	static String inputCalibInstructions;

	static {
		try {
			URL url = TimeCalibrationDialog.class.getResource("/worldjam/gui/calib_instructions/output.txt");
			Scanner scanner;

			scanner = new Scanner(url.openStream());
			outputCalibInstructions = "";
			while(scanner.hasNextLine())
				outputCalibInstructions += scanner.nextLine()+"\n";
			scanner.close();
			URL url2 = TimeCalibrationDialog.class.getResource("/worldjam/gui/calib_instructions/input.txt");
			Scanner scanner2 =  new Scanner(url2.openStream());
			inputCalibInstructions = "";
			while(scanner2.hasNextLine())
				inputCalibInstructions += scanner2.nextLine()+"\n";
			scanner2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	public static void main(String arg[]) {
		TimeCalibrationDialog tcd = new TimeCalibrationDialog(null);
		tcd.setVisible(true);
		tcd.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}


}	


