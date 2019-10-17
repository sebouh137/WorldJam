package worldjam.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.event.ChangeListener;

import worldjam.time.DelayManager;
import worldjam.time.DelaySetting;

import javax.swing.event.ChangeEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.Font;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class DelaySettingsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JButton revertButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DelayManager dm = new DelayManager();
			dm.addChannel(2414L, "user1");
			dm.addChannel(2413L, "user2");
			dm.getChannel(2414L).addListener(setting->{
				System.out.println("user1 setting is now\n" + setting.toString());
			});
			dm.getChannel(2413L).addListener(setting->{
				System.out.println("user2 setting is now\n" + setting.toString());
			});
			
			DelaySettingsDialog dialog = new DelaySettingsDialog(dm);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DelaySettingsDialog(DelayManager dm) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("WorldJam Delay Settings");
		setBounds(100, 100, 525, 306);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		/*GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{110, 107, 1, 79, 94, 119, 0};
		gbl_contentPanel.rowHeights = new int[]{16, 16, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);*/
		int nEntries = dm.getChannelCount();
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{94, 107, 1, 69, 82, 97};
		gbl_contentPanel.rowHeights = new int[3+nEntries];
		for(int i = 0; i<2+nEntries; i++){
			gbl_contentPanel.rowHeights[i] = 18;
		}
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_contentPanel.rowWeights = new double[3+nEntries];
		gbl_contentPanel.rowWeights[2+nEntries] = Double.MIN_VALUE;
		contentPanel.setLayout(gbl_contentPanel);
		{
			//dummy label to make the "hide advanced options" not mess up the alignment of objects in the gui.  
			JLabel label = new JLabel("");
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.anchor = GridBagConstraints.NORTH;
			gbc_label.fill = GridBagConstraints.HORIZONTAL;
			label.setFont(new Font("Lucida Grande", Font.BOLD, 13));
			gbc_label.gridwidth = 2;
			gbc_label.insets = new Insets(0, 0, 5, 5);
			gbc_label.gridx = 0;
			gbc_label.gridy = 0;
			contentPanel.add(label, gbc_label);
		}
		{
			JSeparator separator = new JSeparator();
			GridBagConstraints gbc_separator = new GridBagConstraints();
			gbc_separator.anchor = GridBagConstraints.NORTH;
			gbc_separator.fill = GridBagConstraints.HORIZONTAL;
			gbc_separator.insets = new Insets(0, 0, 5, 5);
			gbc_separator.gridheight = 2;
			gbc_separator.gridx = 2;
			gbc_separator.gridy = 0;
			contentPanel.add(separator, gbc_separator);
		}
		{
			JLabel lblDelayCalibrationadvanced = new JLabel("Delay Calibration (advanced)");
			lblDelayCalibrationadvanced.setFont(new Font("Lucida Grande", Font.BOLD, 13));
			lblDelayCalibrationadvanced.setHorizontalAlignment(SwingConstants.CENTER);
			GridBagConstraints gbc_lblDelayCalibrationadvanced = new GridBagConstraints();
			gbc_lblDelayCalibrationadvanced.anchor = GridBagConstraints.NORTH;
			gbc_lblDelayCalibrationadvanced.insets = new Insets(0, 0, 5, 0);
			gbc_lblDelayCalibrationadvanced.gridwidth = 3;
			gbc_lblDelayCalibrationadvanced.gridx = 3;
			gbc_lblDelayCalibrationadvanced.gridy = 0;
			contentPanel.add(lblDelayCalibrationadvanced, gbc_lblDelayCalibrationadvanced);
			advancedComponents.add(lblDelayCalibrationadvanced);
		}
		{
			JLabel lblChannelName = new JLabel("Channel");
			lblChannelName.setHorizontalAlignment(SwingConstants.CENTER);
			lblChannelName.setFont(new Font("Lucida Grande", Font.BOLD, 13));
			GridBagConstraints gbc_lblChannelName = new GridBagConstraints();
			gbc_lblChannelName.anchor = GridBagConstraints.NORTH;
			gbc_lblChannelName.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblChannelName.insets = new Insets(0, 0, 5, 5);
			gbc_lblChannelName.gridx = 0;
			gbc_lblChannelName.gridy = 1;
			contentPanel.add(lblChannelName, gbc_lblChannelName);
		}
		{
			JLabel lblDelaymeasures = new JLabel("Delay (measures)");
			lblDelaymeasures.setFont(new Font("Lucida Grande", Font.BOLD, 13));
			GridBagConstraints gbc_lblDelaymeasures = new GridBagConstraints();
			gbc_lblDelaymeasures.anchor = GridBagConstraints.NORTHEAST;
			gbc_lblDelaymeasures.insets = new Insets(0, 0, 5, 5);
			gbc_lblDelaymeasures.gridx = 1;
			gbc_lblDelaymeasures.gridy = 1;
			contentPanel.add(lblDelaymeasures, gbc_lblDelaymeasures);
		}
		{
			JLabel lblGeneral = new JLabel("General");
			lblGeneral.setFont(new Font("Lucida Grande", Font.BOLD, 13));
			lblGeneral.setHorizontalAlignment(SwingConstants.CENTER);
			GridBagConstraints gbc_lblGeneral = new GridBagConstraints();
			gbc_lblGeneral.anchor = GridBagConstraints.NORTH;
			gbc_lblGeneral.insets = new Insets(0, 0, 5, 5);
			gbc_lblGeneral.gridx = 3;
			gbc_lblGeneral.gridy = 1;
			contentPanel.add(lblGeneral, gbc_lblGeneral);
			advancedComponents.add(lblGeneral);
		}
		{
			JLabel lblAudio = new JLabel("Audio");
			lblAudio.setFont(new Font("Lucida Grande", Font.BOLD, 13));
			lblAudio.setHorizontalAlignment(SwingConstants.CENTER);
			GridBagConstraints gbc_lblAudio = new GridBagConstraints();
			gbc_lblAudio.anchor = GridBagConstraints.NORTH;
			gbc_lblAudio.insets = new Insets(0, 0, 5, 5);
			gbc_lblAudio.gridx = 4;
			gbc_lblAudio.gridy = 1;
			contentPanel.add(lblAudio, gbc_lblAudio);
			advancedComponents.add(lblAudio);
		}
		{
			JLabel lblVideo = new JLabel("Video");
			lblVideo.setFont(new Font("Lucida Grande", Font.BOLD, 13));
			lblVideo.setHorizontalAlignment(SwingConstants.CENTER);
			GridBagConstraints gbc_lblVideo = new GridBagConstraints();
			gbc_lblVideo.insets = new Insets(0, 0, 5, 0);
			gbc_lblVideo.anchor = GridBagConstraints.NORTH;
			gbc_lblVideo.gridx = 5;
			gbc_lblVideo.gridy = 1;
			contentPanel.add(lblVideo, gbc_lblVideo);
			advancedComponents.add(lblVideo);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JCheckBox chckbxHideAdvancedOptions = new JCheckBox("Show Advanced Options");
				chckbxHideAdvancedOptions.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						
						for (JComponent comp : advancedComponents){
							comp.setVisible(chckbxHideAdvancedOptions.isSelected());
						}
					}
				});
				buttonPane.add(chckbxHideAdvancedOptions);
			}
			{
				okButton = new JButton("Apply");
				okButton.setEnabled(false);
				buttonPane.add(okButton);
				okButton.addActionListener(e -> {
					for (DelayManager.DelayedChannel chan : dm.getChannels()){
						DelaySetting ds = new DelaySetting(
								(Integer)measuresSpinners.get(chan).getValue(), 
								(Integer)generalCalibSpinners.get(chan).getValue(),
								(Integer)audioCalibSpinners.get(chan).getValue(), 
								(Integer)videoCalibSpinners.get(chan).getValue()
								);
						chan.setDelay(ds);
					}
					okButton.setEnabled(false);
					revertButton.setEnabled(false);
				});
				//getRootPane().setDefaultButton(okButton);
			}
			{
				revertButton = new JButton("Revert");
				//cancelButton.setActionCommand("Cancel");
				buttonPane.add(revertButton);
				revertButton.addActionListener(e->{
					okButton.setEnabled(false);
					revertButton.setEnabled(false);
					for(DelayManager.DelayedChannel chan : dm.getChannels()){
						measuresSpinners.get(chan).setValue(chan.getDelaySetting().getMeasuresDelay());
						generalCalibSpinners.get(chan).setValue(chan.getDelaySetting().getAdditionalDelayGlobal());
						audioCalibSpinners.get(chan).setValue(chan.getDelaySetting().getAdditionalDelayAudio());
						videoCalibSpinners.get(chan).setValue(chan.getDelaySetting().getAdditionalDelayVisual());
					}
					
				}
				);
				revertButton.setEnabled(false);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				//cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(e->{
					this.dispose();
				}
				);
			}
		}
		
		addEntries(dm);
		for (JComponent comp : advancedComponents){
			comp.setVisible(false);
		}
	}
	
	Map<DelayManager.DelayedChannel,JSpinner> measuresSpinners = new HashMap();
	Map<DelayManager.DelayedChannel,JSpinner> generalCalibSpinners = new HashMap();
	Map<DelayManager.DelayedChannel,JSpinner> audioCalibSpinners = new HashMap();
	Map<DelayManager.DelayedChannel,JSpinner> videoCalibSpinners = new HashMap();
	ArrayList<JComponent> advancedComponents = new ArrayList();
	private JButton okButton;
	
	void addEntries(DelayManager dm){
		
		ArrayList<JSpinner> allSpinners = new ArrayList();
		int row = 2;
		for(DelayManager.DelayedChannel channel : dm.getChannels()){
			String name = channel.getName();
			JLabel lblChannelName = new JLabel(name);
			GridBagConstraints gbc_lblChannelName = new GridBagConstraints();
			gbc_lblChannelName.anchor = GridBagConstraints.NORTH;
			gbc_lblChannelName.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblChannelName.insets = new Insets(0, 0, 0, 5);
			gbc_lblChannelName.gridx = 0;
			gbc_lblChannelName.gridy = row;
			contentPanel.add(lblChannelName, gbc_lblChannelName);
			
			JSpinner spinner = new JSpinner();
			spinner.setModel(new SpinnerNumberModel(channel.getDelaySetting().getMeasuresDelay(),1,10,1));
			GridBagConstraints gbc_spinner = new GridBagConstraints();
			gbc_spinner.insets = new Insets(0, 0, 0, 5);
			gbc_spinner.gridx = 1;
			gbc_spinner.gridy = row;
			contentPanel.add(spinner, gbc_spinner);
			measuresSpinners.put(channel,spinner);
			
			JSpinner spinner2 = new JSpinner();
			spinner2.setModel(new SpinnerNumberModel(channel.getDelaySetting().getAdditionalDelayGlobal(),-1000,1000,1));
			GridBagConstraints gbc_spinner2 = new GridBagConstraints();
			gbc_spinner2.insets = new Insets(0, 0, 0, 5);
			gbc_spinner2.fill = GridBagConstraints.HORIZONTAL;
			gbc_spinner2.gridx = 3;
			gbc_spinner2.gridy = row;
			contentPanel.add(spinner2, gbc_spinner2);
			generalCalibSpinners.put(channel,spinner2);
			advancedComponents.add(spinner2);
			
			JSpinner spinner3 = new JSpinner();
			spinner3.setModel(new SpinnerNumberModel(channel.getDelaySetting().getAdditionalDelayAudio(),-1000,1000,1));
			GridBagConstraints gbc_spinner3 = new GridBagConstraints();
			gbc_spinner3.insets = new Insets(0, 0, 0, 5);
			gbc_spinner3.gridx = 4;
			gbc_spinner3.fill = GridBagConstraints.HORIZONTAL;
			gbc_spinner3.gridy = row;
			contentPanel.add(spinner3, gbc_spinner3);
			audioCalibSpinners.put(channel,spinner3);
			advancedComponents.add(spinner3);
			
			JSpinner spinner4 = new JSpinner();
			spinner4.setModel(new SpinnerNumberModel(channel.getDelaySetting().getAdditionalDelayVisual(),-1000,1000,1));
			GridBagConstraints gbc_spinner4 = new GridBagConstraints();
			gbc_spinner4.insets = new Insets(0, 0, 0, 5);
			gbc_spinner4.fill = GridBagConstraints.HORIZONTAL;
			gbc_spinner4.gridx = 5;
			gbc_spinner4.gridy = row;
			contentPanel.add(spinner4, gbc_spinner4);
			videoCalibSpinners.put(channel,spinner4);
			advancedComponents.add(spinner4);
			
			
			allSpinners.add(spinner);
			allSpinners.add(spinner2);
			allSpinners.add(spinner3);
			allSpinners.add(spinner4);
			
			row++;
		}
		for (JSpinner spinner : allSpinners){
			spinner.addChangeListener(e->{
				boolean changed = false;
				for (DelayManager.DelayedChannel chan : dm.getChannels()){
					if((int)measuresSpinners.get(chan).getValue() != chan.getDelaySetting().getMeasuresDelay() ||
							(int)generalCalibSpinners.get(chan).getValue() != chan.getDelaySetting().getAdditionalDelayGlobal() ||
							(int)audioCalibSpinners.get(chan).getValue() != chan.getDelaySetting().getAdditionalDelayAudio() ||
							(int)videoCalibSpinners.get(chan).getValue() != chan.getDelaySetting().getAdditionalDelayVisual()){
						changed = true;
						break;
					}
				}
				okButton.setEnabled(changed);
				revertButton.setEnabled(changed);
				
			});
		}
	}

}
