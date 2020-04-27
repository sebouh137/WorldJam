package worldjam.gui;

import javax.swing.JPanel;

import worldjam.audio.Metronome;
import worldjam.audio.PlaybackChannel;
import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import javax.swing.JRadioButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import java.awt.GridLayout;

public class MetronomeControls extends JPanel{
	PlaybackChannel channel;
	Metronome metronome;
	public MetronomeControls(PlaybackChannel channel) {
		this.channel = channel;
		this.metronome = (Metronome)channel.getLoopBuilder();
		setLayout(new BorderLayout(0, 0));
		
		JButton btnApply = new JButton("Apply");
		btnApply.setEnabled(false);
		add(btnApply, BorderLayout.SOUTH);
		
		JPanel topPanel = new JPanel();
		add(topPanel, BorderLayout.NORTH);
		
		
		JRadioButton allBeats = new JRadioButton("All beats");
		topPanel.add(allBeats);
		JRadioButton selectedBeats = new JRadioButton("Selected beats");
		topPanel.add(selectedBeats);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(allBeats);
		buttonGroup.add(selectedBeats);
		
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new GridLayout(3, 8, 0, 0));
		
		
		int maxBeats = 24;
		JCheckBox checkBoxes[] = new JCheckBox[maxBeats];
		for(int i = 0; i<maxBeats; i++) {
			checkBoxes[i] = new JCheckBox(Integer.toString(i+1));
			panel.add(checkBoxes[i]);
		}
		
		
		if(metronome.areAllBeatsPlayed()) {
			allBeats.setSelected(true);
			selectedBeats.setSelected(false);
			for(int i = 0; i< checkBoxes.length; i++) {
				JCheckBox cb = checkBoxes[i];
				cb.setEnabled(false);
				cb.setSelected(true);
				if(i >= channel.getClock().beatsPerMeasure) {
					cb.setVisible(false);
				}
			}
		} else {
			allBeats.setSelected(false);
			selectedBeats.setSelected(true);
			for(int i = 0; i< checkBoxes.length; i++) {
				JCheckBox cb = checkBoxes[i];
				cb.setEnabled(true);
				cb.setSelected(metronome.isBeatPlayed(i));
				if(i >= channel.getClock().beatsPerMeasure) {
					cb.setVisible(false);
				}
			}
		}
		
		ChangeListener cl = e->{
			if(allBeats.isSelected()) {
				metronome.playAllBeats();
				for(JCheckBox cb : checkBoxes ) {
					cb.setSelected(true);
					cb.setEnabled(false);
				}
			} else {
				for(int i = 0; i< checkBoxes.length; i++) {
					JCheckBox cb = checkBoxes[i];
					cb.setEnabled(true);
					if(i >= channel.getClock().beatsPerMeasure) {
						cb.setVisible(false);
					}
				}
			}
			btnApply.setEnabled(true);
		};
		
		allBeats.addChangeListener(cl);
		selectedBeats.addChangeListener(cl);
		for(JCheckBox box : checkBoxes) {
			box.addChangeListener(cl);
		}
		
		btnApply.addActionListener(e->{
			for(int i = 0; i< channel.getClock().beatsPerMeasure; i++) {
				metronome.playBeat(i, checkBoxes[i].isSelected());
			}
			
			channel.rebuildLoop();
			btnApply.setEnabled(false);
		});
		
	}
}
