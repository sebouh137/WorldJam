package worldjam.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import worldjam.audio.PlaybackChannel;
import worldjam.exe.Client;
import worldjam.util.ConfigurationsXML;

public class TimeCalibrationDialog extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4829724163657688277L;
	private JTextField inputField;
	private JButton prevButton;
	private JButton nextButton;
	private JTextArea instructions;
	private Map<Long,Boolean> initialMuting = new HashMap();
	private JCheckBox checkbox;
	public TimeCalibrationDialog(Client client) {
		this.client = client;
		
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
				if(checkbox.isSelected())
					saveCurrentCalibrations();
				client.getGUI().channelsChanged();
			}

		});

		setTitle("Timing Calibration");
		setSize(400,339);
		getContentPane().setLayout(new BorderLayout());
		instructions = new JTextArea("Output timing calibration for:  " + 
				outputMixerName + 
				"\n\n" + outputCalibInstructions);
		instructions.setEditable(false);
		instructions.setLineWrap(true);
		instructions.setWrapStyleWord(true);
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(1,7));
		inputPanel.setMinimumSize(new Dimension(400,10));
		inputField = new JTextField("0");
		inputField.setHorizontalAlignment(SwingConstants.RIGHT);
		inputField.addActionListener(e-> {
			try {
				int i = Integer.parseInt(inputField.getText());
				if(mode == 1) {
					setInputCalibration(i);
				} else {
					setOutputCalibration(i);
				}
			} catch(NumberFormatException ex) {
				inputField.setText(Integer.toString(mode == 1 ? getInputCalibration() : getOutputCalibration()));
			}
		});

		String text[] = {"<<<","<<","<",">",">>",">>>"};
		int delta[] = {-100, -10,-1,1,10,100};
		for(int i = 0; i<6;i++) {
			JButton button = new JButton(text[i]);
			inputPanel.add(button);
			int adjustment = delta[i];
			button.setFont(new Font(Font.MONOSPACED,Font.BOLD,15));
			button.setBackground(Color.RED);
			button.getInsets().set(2, 2, 2, 2);
			button.setToolTipText(delta[i]>0 ? "Increases the offset by " + adjustment + " ms" : "Decreases the offset by " + -adjustment + " ms");
			button.addActionListener((e)->{
				adjustOffset(adjustment);
			});
			if(i == 2)
				inputPanel.add(inputField);
		}
		JPanel buttonPanel = new JPanel();
		prevButton = new JButton("Prev");
		prevButton.setEnabled(false);
		nextButton = new JButton("Next");

		setMode(0);
		prevButton.addActionListener((e->{
			setMode(0);
		}));
		nextButton.addActionListener(e->{
			if(mode==0) {
				setMode(1);
			} else {
				if(checkbox.isSelected()) {
					saveCurrentCalibrations();
				}
				dispose();
			}
		});
		checkbox = new JCheckBox("Save these settings for future sessions                     ");
		buttonPanel.add(checkbox);
		buttonPanel.add(prevButton);
		buttonPanel.add(nextButton);
		buttonPanel.setPreferredSize(new Dimension((int)checkbox.getPreferredSize().getWidth() + 5,
				(int)buttonPanel.getPreferredSize().getHeight()*2));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(instructions, BorderLayout.CENTER);
		mainPanel.add(inputPanel, BorderLayout.SOUTH);
		

		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		//getContentPane().add(tabbedPane, BorderLayout.CENTER);
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
			inputField.setText(Integer.toString(getInputCalibration()));
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
			inputField.setText(Integer.toString(getOutputCalibration()));
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
			inputField.setText(Integer.toString(newVal));
			setOutputCalibration(newVal);
		}
		if(mode == 1) {
			int newVal = getInputCalibration()+adjustment;
			inputField.setText(Integer.toString(newVal));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	public static void main(String arg[]) {
		TimeCalibrationDialog tcd = new TimeCalibrationDialog(null);
		tcd.setVisible(true);
		tcd.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}


}	


