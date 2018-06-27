package worldjam.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import worldjam.audio.PlaybackThread;
import worldjam.filters.pitchshift.PitchShift;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JButton;

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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -776783323280321553L;
	LineControls lineControls;
	private SoundLevelBar slb;
	
	public PlaybackChannelControlGUI(PlaybackThread thread, String title) {
		
		
		setTitle(title);
		setSize(413, 270);
		setVisible(true);
		
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		lineControls = new LineControls(thread.getLine());
		tabbedPane.addTab("Line", null, lineControls, null);
		tabbedPane.addTab("Filters", null, createFilterControls(thread), null);
		tabbedPane.addTab("Delay", null, createDelayControls(thread), null);
		tabbedPane.addTab("Info", null, createInfoPanel(thread), null);
		
		slb = new SoundLevelBar(thread, SoundLevelBar.VERTICAL);
		
		if(thread != null)
			getContentPane().add(slb, BorderLayout.WEST); 
	}
		
		
	private Component createInfoPanel(PlaybackThread thread) {

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		String text = thread.getMixer().getMixerInfo().toString();
		
		textArea.setText(text);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
	
		return textArea;
	}


	private JPanel createDelayControls(PlaybackThread thread) {
		JPanel delayPanel = new JPanel();
		GridBagLayout gbl_delayPanel = new GridBagLayout();
		gbl_delayPanel.columnWidths = new int[]{49, 72, 0, 0, 0};
		gbl_delayPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_delayPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_delayPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		delayPanel.setLayout(gbl_delayPanel);
		
		JSpinner spinner = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		spinner.setModel(new SpinnerNumberModel(1, 0, 12, 1));
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		delayPanel.add(spinner, gbc_spinner);
		
		JLabel lblMeasures = new JLabel("measures");
		GridBagConstraints gbc_lblMeasures = new GridBagConstraints();
		gbc_lblMeasures.insets = new Insets(0, 0, 5, 5);
		gbc_lblMeasures.anchor = GridBagConstraints.EAST;
		gbc_lblMeasures.gridx = 2;
		gbc_lblMeasures.gridy = 0;
		delayPanel.add(lblMeasures, gbc_lblMeasures);
		
		JLabel label_1 = new JLabel("+");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.anchor = GridBagConstraints.EAST;
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 0;
		gbc_label_1.gridy = 1;
		delayPanel.add(label_1, gbc_label_1);
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(thread.getAddDelayBeats(), 0, 100, 1));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 1;
		gbc_spinner_1.gridy = 1;
		delayPanel.add(spinner_1, gbc_spinner_1);
		
		JLabel lblBeats = new JLabel("beats");
		lblBeats.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblBeats = new GridBagConstraints();
		gbc_lblBeats.anchor = GridBagConstraints.WEST;
		gbc_lblBeats.insets = new Insets(0, 0, 5, 5);
		gbc_lblBeats.gridx = 2;
		gbc_lblBeats.gridy = 1;
		delayPanel.add(lblBeats, gbc_lblBeats);
		
		JLabel label = new JLabel("+");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 2;
		delayPanel.add(label, gbc_label);
		
		JSpinner spinner_2 = new JSpinner();
		GridBagConstraints gbc_spinner_2 = new GridBagConstraints();
		gbc_spinner_2.fill = GridBagConstraints.HORIZONTAL;
		spinner_2.setModel(new SpinnerNumberModel(thread.getAddDelayMS(), -1000, 1000, 10));
		gbc_spinner_2.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_2.gridx = 1;
		gbc_spinner_2.gridy = 2;
		delayPanel.add(spinner_2, gbc_spinner_2);
		
		JLabel lblMs = new JLabel("ms");
		GridBagConstraints gbc_lblMs = new GridBagConstraints();
		gbc_lblMs.anchor = GridBagConstraints.WEST;
		gbc_lblMs.insets = new Insets(0, 0, 5, 5);
		gbc_lblMs.gridx = 2;
		gbc_lblMs.gridy = 2;
		delayPanel.add(lblMs, gbc_lblMs);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 3;
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 3;
		delayPanel.add(separator, gbc_separator);
		
		JLabel lblTotal = new JLabel("total:");
		GridBagConstraints gbc_lblTotal = new GridBagConstraints();
		gbc_lblTotal.anchor = GridBagConstraints.EAST;
		gbc_lblTotal.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotal.gridx = 0;
		gbc_lblTotal.gridy = 4;
		delayPanel.add(lblTotal, gbc_lblTotal);
		
		JLabel lblSum = new JLabel(Integer.toString(thread.getDelayInMS()));
		GridBagConstraints gbc_lblSum = new GridBagConstraints();
		gbc_lblSum.anchor = GridBagConstraints.EAST;
		gbc_lblSum.insets = new Insets(0, 0, 5, 5);
		gbc_lblSum.gridx = 1;
		gbc_lblSum.gridy = 4;
		delayPanel.add(lblSum, gbc_lblSum);
		
		JLabel lblMs_1 = new JLabel("ms");
		GridBagConstraints gbc_lblMs_1 = new GridBagConstraints();
		gbc_lblMs_1.anchor = GridBagConstraints.WEST;
		gbc_lblMs_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMs_1.gridx = 2;
		gbc_lblMs_1.gridy = 4;
		delayPanel.add(lblMs_1, gbc_lblMs_1);
		

		ChangeListener recalc = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				int val = ((int)spinner.getValue()*thread.getClock().beatsPerMeasure
						+(int)spinner_1.getValue())*thread.getClock().msPerBeat
						+ (int)spinner_2.getValue();
				lblSum.setText(Integer.toString(val));
			}
			
		};
		spinner.addChangeListener(recalc);
		spinner_1.addChangeListener(recalc);
		spinner_2.addChangeListener(recalc);
		
		JButton btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.insets = new Insets(0, 0, 0, 5);
		gbc_btnApply.gridx = 1;
		gbc_btnApply.gridy = 6;
		delayPanel.add(btnApply, gbc_btnApply);
		btnApply.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				thread.setReplayOffset((Integer)spinner.getModel().getValue(),
						(Integer)spinner_1.getModel().getValue(), 
						(Integer)spinner_2.getModel().getValue());
			}
			
		});
		return delayPanel;
	}


	private JPanel createFilterControls(PlaybackThread thread){
		JPanel filterControls = new JPanel();
		GridBagLayout gbl_filterControls = new GridBagLayout();
		gbl_filterControls.columnWidths = new int[]{0, 60, 49, 0};
		gbl_filterControls.rowHeights = new int[]{26, 0};
		gbl_filterControls.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_filterControls.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		filterControls.setLayout(gbl_filterControls);
		
		
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
					thread.setFilter(new PitchShift(thread.getFormat(), value));
				}
				else {
					System.out.println("deactivating pitch shift");
					thread.setFilter(null);
				}
			}
			
		};
		spinner.addChangeListener(pitchShiftListener);
		chckbxPitchShift.addChangeListener(pitchShiftListener);
		return filterControls;
	}

	
	@Override
	public void dispose(){
		super.dispose();
		this.slb.stop();
	}


}
