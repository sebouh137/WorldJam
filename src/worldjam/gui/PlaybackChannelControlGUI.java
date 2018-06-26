package worldjam.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.EnumControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.tarsos.dsp.PitchShifter;
import worldjam.audio.PlaybackThread;
import worldjam.filters.pitchshift.PitchShift;
import worldjam.util.DefaultObjects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.JSeparator;

public class PlaybackChannelControlGUI extends JFrame {
	/*public static void main(String arg[]) throws LineUnavailableException{
		try {
			DefaultObjects.outputMixer.open();
			SourceDataLine defaultLine =(SourceDataLine) DefaultObjects.outputMixer.getLine(new SourceDataLine.Info(SourceDataLine.class, DefaultObjects.defaultFormat));
			defaultLine.open();
			defaultLine.start();
			new ChannelControlsGUI(defaultLine, "default channel for debugging GUI").setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);;
			
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
	
	LineControls lineControls;
	
	public PlaybackChannelControlGUI(PlaybackThread thread, String title) {
		lineControls = new LineControls(thread.getLine());
		JPanel filterControls = new JPanel();
		
		setTitle(title);
		setSize(600, 270);
		setVisible(true);

		getContentPane().add(lineControls,BorderLayout.CENTER);
		getContentPane().add(filterControls, BorderLayout.EAST);
		
		GridBagLayout gbl_filterControls = new GridBagLayout();
		gbl_filterControls.columnWidths = new int[]{0, 60, 49, 0};
		gbl_filterControls.rowHeights = new int[]{26, 0};
		gbl_filterControls.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_filterControls.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		filterControls.setLayout(gbl_filterControls);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(0, 0, 0, 5);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 0;
		filterControls.add(separator, gbc_separator);
		
		JCheckBox chckbxPitchShift = new JCheckBox("Pitch Shift");
		GridBagConstraints gbc_chckbxPitchShift = new GridBagConstraints();
		gbc_chckbxPitchShift.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxPitchShift.gridx = 1;
		gbc_chckbxPitchShift.gridy = 0;
		filterControls.add(chckbxPitchShift, gbc_chckbxPitchShift);
		
		JSpinner spinner = new JSpinner();
		spinner.setMinimumSize(new Dimension(12, 30));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.anchor = GridBagConstraints.NORTH;
		gbc_spinner.gridx = 2;
		gbc_spinner.gridy = 0;
		filterControls.add(spinner, gbc_spinner);
		
		ChangeListener pitchShiftListener = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				if(chckbxPitchShift.isSelected()){
					int value = (Integer)spinner.getValue();
					System.out.println("changing pitch shift to " + value);
					//thread.setFilter(filter);
					thread.setFilter(new PitchShift(thread.getInputFormat(), value));
				}
				else {
					System.out.println("deactivating pitch shift");
					thread.setFilter(null);
				}
			}
			
		};
		spinner.addChangeListener(pitchShiftListener);
		chckbxPitchShift.addChangeListener(pitchShiftListener);
		
		
	}
		
		
		

	
	


}
