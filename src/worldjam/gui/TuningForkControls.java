package worldjam.gui;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeListener;

import worldjam.audio.PlaybackChannel;
import worldjam.audio.TuningFork;

import javax.swing.JSpinner;
import javax.swing.JButton;

public class TuningForkControls extends JPanel{
	private JTextField textField;
	private JButton btnApply;
	private JSpinner spinner;
	private JSpinner spinner_1;
	public TuningForkControls() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 121, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JLabel lblNewLabel = new JLabel("Frequency");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 2;
		gbc_textField.gridy = 1;
		add(textField, gbc_textField);
		textField.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("Pitch");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 2;
		add(lblNewLabel_1, gbc_lblNewLabel_1);

		spinner = new JSpinner();
		spinner.setModel(new SpinnerListModel(pitches()));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 2;
		gbc_spinner.gridy = 2;
		add(spinner, gbc_spinner);

		spinner_1 = new JSpinner();
		spinner_1.setMinimumSize(new Dimension(70, (int) spinner_1.getPreferredSize().getHeight()));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.gridx = 3;
		gbc_spinner_1.gridy = 2;
		add(spinner_1, gbc_spinner_1);




		btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.gridwidth = 3;
		gbc_btnApply.insets = new Insets(0, 0, 0, 5);
		gbc_btnApply.gridx = 1;
		gbc_btnApply.gridy = 4;
		add(btnApply, gbc_btnApply);


		ChangeListener al = e->{
			textField.setText(String.format("%.2f", ((NamedPitch)spinner.getValue()).freq*
					Math.pow(2, ((Integer)spinner_1.getValue())/12000.)));
			btnApply.setEnabled(true);
		};
		spinner.addChangeListener(al);
		spinner_1.addChangeListener(al);

		textField.addActionListener(e->{
			updateSpinners();
			btnApply.setEnabled(true);
		});
	}
	private void updateSpinners() {
		double freq = Double.parseDouble(textField.getText());
		//find the nearest pitch
		NamedPitch closest = null;
		double df = 1000000;
		double cents = 0;
		for(NamedPitch pitch : (List<NamedPitch>)((SpinnerListModel)spinner.getModel()).getList()) {
			if(Math.abs(pitch.freq-freq)<df) {
				df = Math.abs(pitch.freq-freq);
				closest = pitch;
				cents = Math.log(freq/pitch.freq)/(Math.log(2)/12000);
			}
		}
		spinner.setValue(closest);
		spinner_1.setValue((int)cents);
	}
	private class NamedPitch {
		String name;
		double freq;
		public NamedPitch(String name, double freq) {
			super();
			this.name = name;
			this.freq = freq;
		}
		public String toString() {
			return name;
		}
	}

	private List<NamedPitch> pitches() {
		List<NamedPitch> pitches = new ArrayList();
		String pitchClassNames[] =  "C C#/Db D D#/Eb E F F#/Gb G G#/Ab A A#/Bb B".replaceAll("b", "\u266d").replaceAll("#", "\u266f").split(" ");
		for(int i = 12; i<72; i++) {
			double freq = 16.352*Math.pow(2, i/12.);
			String name = pitchClassNames[i%12]+(i/12);
			pitches.add(new NamedPitch(name,freq));
		}
		return pitches;
	}
	public void setChannel(PlaybackChannel channel) {
		double freq = ((TuningFork)channel.getLoopBuilder()).getFrequency();
		textField.setText(String.format("%.2f", freq));
		updateSpinners();
		btnApply.setEnabled(false);
		btnApply.addActionListener(e->{
			((TuningFork)channel.getLoopBuilder()).setFrequency(Double.parseDouble(textField.getText()));
			btnApply.setEnabled(false);
			channel.rebuildLoop();
		});
	}


}
