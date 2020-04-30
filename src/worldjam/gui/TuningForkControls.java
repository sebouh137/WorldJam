package worldjam.gui;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import worldjam.audio.PlaybackChannel;
import worldjam.audio.TuningFork;

import javax.swing.JButton;
import javax.swing.JCheckBox;

public class TuningForkControls extends JPanel{
	private JSpinner spinner_freq;
	private JButton btnApply;
	private JSpinner spinner_pitch;
	private JSpinner spinner_cents;
	private JLabel lblNewLabel_2;
	private ChangeListener al;
	private JCheckBox chckbxPercussive;
	ChangeListener al2;
	public TuningForkControls() {

		al = e->{
			spinner_freq.removeChangeListener(al2);
			spinner_freq.setValue(((NamedPitch)spinner_pitch.getValue()).freq*
					Math.pow(2, ((double)spinner_cents.getValue())/1200.));
			spinner_freq.addChangeListener(al2);
			btnApply.setEnabled(true);
		};
		al2 = e->{
			updateSpinners();
			btnApply.setEnabled(true);
		};

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 121, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JLabel lblNewLabel = new JLabel("Frequency");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);

		spinner_freq = new JSpinner();
		spinner_freq.setModel(new SpinnerNumberModel((double)440, 20,20000,1));
		GridBagConstraints gbc_spinner_freq = new GridBagConstraints();
		gbc_spinner_freq.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_freq.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_freq.gridx = 2;
		gbc_spinner_freq.gridy = 1;
		add(spinner_freq, gbc_spinner_freq);

		JLabel lblNewLabel_1 = new JLabel("Pitch");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 2;
		add(lblNewLabel_1, gbc_lblNewLabel_1);

		spinner_pitch = new JSpinner();
		spinner_pitch.setModel(new SpinnerListModel(pitches()));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 2;
		gbc_spinner.gridy = 2;
		add(spinner_pitch, gbc_spinner);

		lblNewLabel_2 = new JLabel("Cents");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 3;
		add(lblNewLabel_2, gbc_lblNewLabel_2);

		spinner_cents = new JSpinner();
		spinner_cents.setMinimumSize(new Dimension(70, (int) spinner_cents.getPreferredSize().getHeight()));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.gridx = 2;
		gbc_spinner_1.gridy = 3;
		add(spinner_cents, gbc_spinner_1);
		spinner_cents.setModel(new SpinnerNumberModel((double)0,-50,50,1));
		spinner_cents.addChangeListener(al);

		chckbxPercussive = new JCheckBox("percussive mode");
		chckbxPercussive.addChangeListener(e->{
			btnApply.setEnabled(true);
		});
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.gridwidth = 2;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 1;
		gbc_chckbxNewCheckBox.gridy = 4;
		add(chckbxPercussive, gbc_chckbxNewCheckBox);




		btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.gridwidth = 2;
		gbc_btnApply.gridx = 1;
		gbc_btnApply.gridy = 5;
		add(btnApply, gbc_btnApply);



		spinner_pitch.addChangeListener(al);

		spinner_freq.addChangeListener(al2);
	}
	private void updateSpinners() {
		double freq = (double)spinner_freq.getValue();
		//find the nearest pitch
		NamedPitch closest = null;
		double df = 1000000;
		double cents = 0;
		for(NamedPitch pitch : (List<NamedPitch>)((SpinnerListModel)spinner_pitch.getModel()).getList()) {
			if(Math.abs(pitch.freq-freq)<df) {
				df = Math.abs(pitch.freq-freq);
				closest = pitch;
				cents = Math.log(freq/pitch.freq)/(Math.log(2)/1200);
			}
		}
		spinner_pitch.removeChangeListener(al);
		spinner_cents.removeChangeListener(al);
		spinner_pitch.setValue(closest);
		spinner_cents.setValue(cents);
		spinner_pitch.addChangeListener(al);
		spinner_cents.addChangeListener(al);
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
			double freq = 16.351597*Math.pow(2, i/12.);
			String name = pitchClassNames[i%12]+(i/12);
			pitches.add(new NamedPitch(name,freq));
		}
		return pitches;
	}
	public void setChannel(PlaybackChannel channel) {
		double freq = ((TuningFork)channel.getLoopBuilder()).getFrequency();
		spinner_freq.setValue(freq);
		updateSpinners();
		btnApply.setEnabled(false);
		TuningFork tf = ((TuningFork)channel.getLoopBuilder());
		this.chckbxPercussive.setSelected(tf.getPercussiveMode());
		btnApply.addActionListener(e->{
			tf.setFrequency((double)spinner_freq.getValue());
			tf.setPercussiveMode(this.chckbxPercussive.isSelected());
			btnApply.setEnabled(false);
			channel.rebuildLoop();
		});
		btnApply.setEnabled(false);
	}


}
