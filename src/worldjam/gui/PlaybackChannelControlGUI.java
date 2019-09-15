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

import worldjam.audio.PlaybackChannel;
import worldjam.filters.NoiseGateFilter;
import worldjam.filters.pitchshift.PitchShift;
import worldjam.filters.pitchshift.WfsoPitchShift;
import worldjam.time.DelayManager;
import worldjam.time.DelayManager.DelayedChannel;
import worldjam.time.DelaySetting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

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

	public PlaybackChannelControlGUI(PlaybackChannel playbackChannel, String title, DelayManager dm) {


		setTitle(title);
		setSize(413, 300);
		setVisible(true);


		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		lineControls = new LineControls(playbackChannel.getLine());
		tabbedPane.addTab("Line", null, lineControls, null);
		tabbedPane.addTab("Transpose", null, createTransposeControls(playbackChannel), null);
		tabbedPane.addTab("Noise Gate", null, createNoiseGateControls(playbackChannel), null);
		tabbedPane.addTab("Delay", null, createDelayControls(playbackChannel,dm), null);
		tabbedPane.addTab("Info", null, createInfoPanel(playbackChannel), null);

		slb = new SoundLevelBar(playbackChannel, SoundLevelBar.VERTICAL);

		if(playbackChannel != null)
			getContentPane().add(slb, BorderLayout.WEST); 
	}


	private Component createInfoPanel(PlaybackChannel channel) {

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		String text = channel.getMixer().getMixerInfo().toString();

		textArea.setText(text);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		return textArea;
	}


	private JPanel createDelayControls(PlaybackChannel channel, DelayManager dm) {
		JPanel delayPanel = new JPanel();
		GridBagLayout gbl_delayPanel = new GridBagLayout();
		gbl_delayPanel.columnWidths = new int[]{64, 65, 61, 0, 0, 0};
		gbl_delayPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_delayPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_delayPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		delayPanel.setLayout(gbl_delayPanel);

		

		JSpinner spinner = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		spinner.setModel(new SpinnerNumberModel(1, 0, 12, 1));
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		delayPanel.add(spinner, gbc_spinner);

		int msPerMeasure = channel.getClock().getMsPerMeasure();

		JLabel lblMeasures = new JLabel("measures");
		GridBagConstraints gbc_lblMeasures = new GridBagConstraints();
		gbc_lblMeasures.insets = new Insets(0, 0, 5, 5);
		gbc_lblMeasures.gridx = 2;
		gbc_lblMeasures.gridy = 0;
		delayPanel.add(lblMeasures, gbc_lblMeasures);
		JLabel lblMsmeasure = new JLabel(String.format("( %d ms/measure )", msPerMeasure));
		GridBagConstraints gbc_lblMsmeasure = new GridBagConstraints();
		gbc_lblMsmeasure.anchor = GridBagConstraints.WEST;
		gbc_lblMsmeasure.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsmeasure.gridx = 3;
		gbc_lblMsmeasure.gridy = 0;
		delayPanel.add(lblMsmeasure, gbc_lblMsmeasure);

		/*JSpinner spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(channel.getAddDelayBeats(), 0, 48, 1));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 1;
		gbc_spinner_1.gridy = 1;
		delayPanel.add(spinner_1, gbc_spinner_1);*/


		/*JLabel lblBeats = new JLabel("beats");
		lblBeats.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblBeats = new GridBagConstraints();
		gbc_lblBeats.insets = new Insets(0, 0, 5, 5);
		gbc_lblBeats.gridx = 2;
		gbc_lblBeats.gridy = 1;
		delayPanel.add(lblBeats, gbc_lblBeats);*/
		
		
		

		JSpinner spinner_2 = new JSpinner();
		GridBagConstraints gbc_spinner_2 = new GridBagConstraints();
		spinner_2.setModel(new SpinnerNumberModel(dm.getChannel(channel.getSenderID()).getDelaySetting().getAdditionalDelayAudio(), 
				-1000, 1000, 10));
		gbc_spinner_2.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_2.gridx = 1;
		gbc_spinner_2.gridy = 2;
		delayPanel.add(spinner_2, gbc_spinner_2);
		
				JLabel lblMs = new JLabel("ms  (audio calibration)");
				lblMs.setHorizontalAlignment(SwingConstants.CENTER);
				GridBagConstraints gbc_lblMs = new GridBagConstraints();
				gbc_lblMs.gridwidth = 2;
				gbc_lblMs.insets = new Insets(0, 0, 5, 5);
				gbc_lblMs.gridx = 2;
				gbc_lblMs.gridy = 2;
				delayPanel.add(lblMs, gbc_lblMs);

				
				JCheckBox chckbxShowAdvancedSettings = new JCheckBox("Show advanced settings");
				GridBagConstraints gbc_chckbxShowAdvancedSettings = new GridBagConstraints();
				gbc_chckbxShowAdvancedSettings.gridwidth = 2;
				gbc_chckbxShowAdvancedSettings.insets = new Insets(0, 0, 5, 5);
				gbc_chckbxShowAdvancedSettings.gridx = 1;
				gbc_chckbxShowAdvancedSettings.gridy = 1;
				delayPanel.add(chckbxShowAdvancedSettings, gbc_chckbxShowAdvancedSettings);
				
				chckbxShowAdvancedSettings.addChangeListener(e->{
					lblMs.setVisible(chckbxShowAdvancedSettings.isSelected());
					spinner_2.setVisible(chckbxShowAdvancedSettings.isSelected());
				});
				
				spinner_2.setVisible(false);
				lblMs.setVisible(false);
		JLabel lblTotal = new JLabel("total:");
		GridBagConstraints gbc_lblTotal = new GridBagConstraints();
		gbc_lblTotal.anchor = GridBagConstraints.EAST;
		gbc_lblTotal.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotal.gridx = 0;
		gbc_lblTotal.gridy = 3;
		delayPanel.add(lblTotal, gbc_lblTotal);

		JLabel lblSum = new JLabel(Integer.toString(channel.getTotalDelayInMS()));
		GridBagConstraints gbc_lblSum = new GridBagConstraints();
		gbc_lblSum.anchor = GridBagConstraints.EAST;
		gbc_lblSum.insets = new Insets(0, 0, 5, 5);
		gbc_lblSum.gridx = 1;
		gbc_lblSum.gridy = 3;
		delayPanel.add(lblSum, gbc_lblSum);


		ChangeListener recalc = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				int val = ((int)spinner.getValue()*channel.getClock().beatsPerMeasure
						/*+(int)spinner_1.getValue()*/)*channel.getClock().msPerBeat
						+ (int)spinner_2.getValue();
				lblSum.setText(Integer.toString(val));
			}

		};
		spinner.addChangeListener(recalc);
		/*spinner_1.addChangeListener(recalc);*/
		spinner_2.addChangeListener(recalc);

		JLabel lblMs_1 = new JLabel("ms");
		GridBagConstraints gbc_lblMs_1 = new GridBagConstraints();
		gbc_lblMs_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMs_1.gridx = 2;
		gbc_lblMs_1.gridy = 3;
		delayPanel.add(lblMs_1, gbc_lblMs_1);
				
				JSeparator separator_1 = new JSeparator();
				GridBagConstraints gbc_separator_1 = new GridBagConstraints();
				gbc_separator_1.gridwidth = 4;
				gbc_separator_1.insets = new Insets(0, 0, 5, 5);
				gbc_separator_1.gridx = 0;
				gbc_separator_1.gridy = 3;
				delayPanel.add(separator_1, gbc_separator_1);
		
				JButton btnRevert = new JButton("Revert");
				GridBagConstraints gbc_btnRevert = new GridBagConstraints();
				gbc_btnRevert.gridwidth = 2;
				gbc_btnRevert.insets = new Insets(0, 0, 0, 5);
				gbc_btnRevert.gridx = 0;
				gbc_btnRevert.gridy = 5;
				delayPanel.add(btnRevert, gbc_btnRevert);
				btnRevert.setEnabled(false);
				
						JButton btnApply = new JButton("Apply");
						GridBagConstraints gbc_btnApply = new GridBagConstraints();
						gbc_btnApply.gridwidth = 2;
						gbc_btnApply.insets = new Insets(0, 0, 0, 5);
						gbc_btnApply.gridx = 2;
						gbc_btnApply.gridy = 5;
						delayPanel.add(btnApply, gbc_btnApply);
						
								btnApply.setEnabled(false);
								
										btnApply.addActionListener(
												e-> {
													/*channel.setReplayOffset((Integer)spinner.getModel().getValue(),
															0(Integer)spinner_1.getModel().getValue(), 
															(Integer)spinner_2.getModel().getValue());*/
													DelayedChannel dc = dm.getChannel(channel.getSenderID());
													dc.setDelay(new DelaySetting((Integer)spinner.getModel().getValue(), 
															dc.getDelaySetting().getAdditionalDelayGlobal(),
															(Integer)spinner_2.getModel().getValue(),
															dc.getDelaySetting().getAdditionalDelayVisual()));		
													btnApply.setEnabled(false);
													btnRevert.setEnabled(false);
												}
												);
				
						btnRevert.addActionListener(
								e -> {
									spinner.setValue(dm.getChannel(channel.getSenderID()).getDelaySetting().getMeasuresDelay());
									//spinner_1.setValue(channel.getAddDelayBeats());
									spinner_2.setValue(dm.getChannel(channel.getSenderID()).getDelaySetting().getAdditionalDelayAudio());
								});

		ChangeListener changeListener = e-> {
			boolean isSame = 
					(dm.getChannel(channel.getSenderID()).getDelaySetting().getMeasuresDelay() == (int)spinner.getValue())
					/*&& (channel.getAddDelayBeats() == (int)spinner_1.getValue())*/
					&&  (dm.getChannel(channel.getSenderID()).getDelaySetting().getAdditionalDelayAudio() == (int)spinner_2.getValue());
			btnApply.setEnabled(!isSame);
			btnRevert.setEnabled(!isSame);
		};
		spinner.addChangeListener(changeListener);
		/*spinner_1.addChangeListener(changeListener);*/
		spinner_2.addChangeListener(changeListener);


		return delayPanel;
	}

	private JPanel createNoiseGateControls(PlaybackChannel channel){
		NoiseGateFilter filter;
		if(channel.getFilter() == null || !(channel.getFilter() instanceof WfsoPitchShift))
			filter = new NoiseGateFilter(channel.getInputFormat());
		else
			filter = (NoiseGateFilter)channel.getFilter();

		JPanel filterControls = new JPanel();
		GridBagLayout gbl_filterControls = new GridBagLayout();
		gbl_filterControls.columnWidths = new int[]{0, 0, 105, 0, 0};
		gbl_filterControls.rowHeights = new int[]{0, 0, 0};
		gbl_filterControls.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_filterControls.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		filterControls.setLayout(gbl_filterControls);
		
		JCheckBox chckbxEnableNoiseGate = new JCheckBox("Enable Noise Gate");
		GridBagConstraints gbc_chckbxEnableNoiseGate = new GridBagConstraints();
		gbc_chckbxEnableNoiseGate.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxEnableNoiseGate.gridx = 1;
		gbc_chckbxEnableNoiseGate.gridy = 0;
		filterControls.add(chckbxEnableNoiseGate, gbc_chckbxEnableNoiseGate);
		
		chckbxEnableNoiseGate.addActionListener(
				e->{
					if(chckbxEnableNoiseGate.isSelected())
						channel.setFilter(filter);
					else
						channel.setFilter(null);
				});
		
		JLabel lblThreshold = new JLabel("Threshold");
		GridBagConstraints gbc_lblThreshold = new GridBagConstraints();
		gbc_lblThreshold.insets = new Insets(0, 0, 0, 5);
		gbc_lblThreshold.anchor = GridBagConstraints.EAST;
		gbc_lblThreshold.gridx = 1;
		gbc_lblThreshold.gridy = 1;
		filterControls.add(lblThreshold, gbc_lblThreshold);
		
		JSpinner spinnerThreshold = new JSpinner();
		GridBagConstraints gbc_jtfThreshold = new GridBagConstraints();
		gbc_jtfThreshold.insets = new Insets(0, 0, 0, 5);
		gbc_jtfThreshold.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfThreshold.gridx = 2;
		gbc_jtfThreshold.gridy = 1;
		filterControls.add(spinnerThreshold, gbc_jtfThreshold);
		spinnerThreshold.setValue((int)filter.getThresholdDB());
		spinnerThreshold.addChangeListener(
				e->{
					filter.setThresholdDB((Integer)spinnerThreshold.getValue());
				}
			);
		
		JLabel lblDb = new JLabel("dB");
		GridBagConstraints gbc_lblDb = new GridBagConstraints();
		gbc_lblDb.gridx = 3;
		gbc_lblDb.gridy = 1;
		filterControls.add(lblDb, gbc_lblDb);
		return filterControls;
	}
	
	private JPanel createTransposeControls(PlaybackChannel channel){


		WfsoPitchShift filter;
		if(channel.getFilter() == null || !(channel.getFilter() instanceof WfsoPitchShift))
			filter = new WfsoPitchShift(channel.getInputFormat(), 0);
		else
			filter = (WfsoPitchShift)channel.getFilter();

		JPanel filterControls = new JPanel();
		GridBagLayout gbl_filterControls = new GridBagLayout();
		gbl_filterControls.columnWidths = new int[]{0, 48, 60, 90, 0, 0, 0};
		gbl_filterControls.rowHeights = new int[]{26, 0, 0, 0, 0, 0, 0, 0,0};
		gbl_filterControls.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_filterControls.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		filterControls.setLayout(gbl_filterControls);


		JCheckBox chckbxPitchShift = new JCheckBox("Pitch Shift");
		GridBagConstraints gbc_chckbxPitchShift = new GridBagConstraints();
		gbc_chckbxPitchShift.anchor = GridBagConstraints.WEST;
		gbc_chckbxPitchShift.gridwidth = 2;
		gbc_chckbxPitchShift.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxPitchShift.gridx = 1;
		gbc_chckbxPitchShift.gridy = 0;
		filterControls.add(chckbxPitchShift, gbc_chckbxPitchShift);

		JSpinner spinner = new JSpinner();
		spinner.setMinimumSize(new Dimension(12, 30));


		spinner.setValue((int)filter.getShiftInCents());
		if(channel.getFilter() == filter){
			chckbxPitchShift.setSelected(true);
		}
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.anchor = GridBagConstraints.NORTH;
		gbc_spinner.gridx = 3;
		gbc_spinner.gridy = 0;
		filterControls.add(spinner, gbc_spinner);

		JLabel lblCents = new JLabel("cents*");
		GridBagConstraints gbc_lblCents = new GridBagConstraints();
		gbc_lblCents.insets = new Insets(0, 0, 5, 5);
		gbc_lblCents.gridx = 4;
		gbc_lblCents.gridy = 0;
		filterControls.add(lblCents, gbc_lblCents);

		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 6;
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 1;
		filterControls.add(separator, gbc_separator);

		JCheckBox chkbxAdvancedSettings = new JCheckBox("Show advanced settings");
		GridBagConstraints gbc_chkbxAdvancedSettings = new GridBagConstraints();
		gbc_chkbxAdvancedSettings.anchor = GridBagConstraints.WEST;
		gbc_chkbxAdvancedSettings.gridwidth = 4;
		gbc_chkbxAdvancedSettings.insets = new Insets(0, 0, 5, 5);
		gbc_chkbxAdvancedSettings.gridx = 1;
		gbc_chkbxAdvancedSettings.gridy = 2;
		filterControls.add(chkbxAdvancedSettings, gbc_chkbxAdvancedSettings);

		JLabel lblSegmentSize = new JLabel("segment size");
		GridBagConstraints gbc_lblSegmentSize = new GridBagConstraints();
		gbc_lblSegmentSize.anchor = GridBagConstraints.WEST;
		gbc_lblSegmentSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblSegmentSize.gridx = 2;
		gbc_lblSegmentSize.gridy = 3;
		filterControls.add(lblSegmentSize, gbc_lblSegmentSize);

		JSpinner spinner_1 = new JSpinner();
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_1.gridx = 3;
		gbc_spinner_1.gridy = 3;
		spinner_1.setValue((int)filter.getMsPerSegment());
		filterControls.add(spinner_1, gbc_spinner_1);

		JLabel lblMs_3 = new JLabel("ms");
		GridBagConstraints gbc_lblMs_3 = new GridBagConstraints();
		gbc_lblMs_3.anchor = GridBagConstraints.WEST;
		gbc_lblMs_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblMs_3.gridx = 4;
		gbc_lblMs_3.gridy = 3;
		filterControls.add(lblMs_3, gbc_lblMs_3);

		JLabel lblOverlap = new JLabel("overlap size");
		GridBagConstraints gbc_lblOverlap = new GridBagConstraints();
		gbc_lblOverlap.anchor = GridBagConstraints.WEST;
		gbc_lblOverlap.insets = new Insets(0, 0, 5, 5);
		gbc_lblOverlap.gridx = 2;
		gbc_lblOverlap.gridy = 4;
		filterControls.add(lblOverlap, gbc_lblOverlap);

		JSpinner spinner_2 = new JSpinner();
		GridBagConstraints gbc_spinner_2 = new GridBagConstraints();
		gbc_spinner_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_2.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_2.gridx = 3;
		gbc_spinner_2.gridy = 4;
		spinner_2.setValue((int)filter.getMsPerOverlap());
		filterControls.add(spinner_2, gbc_spinner_2);

		JLabel lblMs_2 = new JLabel("ms");
		GridBagConstraints gbc_lblMs_2 = new GridBagConstraints();
		gbc_lblMs_2.anchor = GridBagConstraints.WEST;
		gbc_lblMs_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblMs_2.gridx = 4;
		gbc_lblMs_2.gridy = 4;
		filterControls.add(lblMs_2, gbc_lblMs_2);

		JLabel lblSearch = new JLabel("search window");
		GridBagConstraints gbc_lblSearch = new GridBagConstraints();
		gbc_lblSearch.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearch.gridx = 2;
		gbc_lblSearch.gridy = 5;
		filterControls.add(lblSearch, gbc_lblSearch);

		JSpinner spinner_3 = new JSpinner();
		GridBagConstraints gbc_spinner_3 = new GridBagConstraints();
		gbc_spinner_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_3.insets = new Insets(0, 0, 5, 5);
		gbc_spinner_3.gridx = 3;
		gbc_spinner_3.gridy = 5;
		spinner_3.setValue((int)filter.getMsPerSearch());
		filterControls.add(spinner_3, gbc_spinner_3);

		JLabel lblMs_4 = new JLabel("ms");
		GridBagConstraints gbc_lblMs_4 = new GridBagConstraints();
		gbc_lblMs_4.anchor = GridBagConstraints.WEST;
		gbc_lblMs_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblMs_4.gridx = 4;
		gbc_lblMs_4.gridy = 5;
		filterControls.add(lblMs_4, gbc_lblMs_4);

		JLabel lblSemitone = new JLabel("*  1 semitone = 100 cents");
		GridBagConstraints gbc_lblSemitone = new GridBagConstraints();
		gbc_lblSemitone.gridwidth = 5;
		gbc_lblSemitone.gridx = 1;
		gbc_lblSemitone.gridy = 7;
		filterControls.add(lblSemitone, gbc_lblSemitone);



		ChangeListener changeListener = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				if(chckbxPitchShift.isSelected()){
					int cents = (Integer)spinner.getValue();
					filter.setShiftInCents(cents);
					filter.setMsPerSegment((Integer)spinner_1.getValue());
					filter.setMsPerOverlap((Integer)spinner_2.getValue());
					filter.setMsPerSearch((Integer)spinner_3.getValue());
					channel.setFilter(filter);
				}
				else {
					channel.setFilter(null);
				}
			}

		};
		spinner.addChangeListener(changeListener);
		spinner_1.addChangeListener(changeListener);
		spinner_2.addChangeListener(changeListener);
		spinner_3.addChangeListener(changeListener);
		chckbxPitchShift.addChangeListener(changeListener);

		Component[] advancedSettingsComponents = {spinner_1, spinner_2, spinner_3,
				lblMs_4, lblMs_3, lblMs_2, 
				lblSearch, lblOverlap, lblSegmentSize
		};
		for(Component c : advancedSettingsComponents){
			c.setVisible(false);
		}

		chkbxAdvancedSettings.addChangeListener(
				e ->{
					for(Component component : advancedSettingsComponents){
						component.setVisible(chkbxAdvancedSettings.isSelected());
					}
				}
				);

		return filterControls;
	}


	@Override
	public void dispose(){
		super.dispose();
		this.slb.stop();
	}


}
