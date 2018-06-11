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

import worldjam.test.DefaultObjects;

import java.awt.BorderLayout;
import java.awt.Color;

public class ChannelControlsGUI extends JFrame {
	public static void main(String arg[]) throws LineUnavailableException{
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
		
	}
	
	JPanel lineControls;
	/**
	 * Create the panel.
	 */
	public ChannelControlsGUI(Line line, String title) {
		lineControls = new JPanel();
		if(line.getControls().length == 0){
			lineControls.add(new JLabel("No controls to show"), BorderLayout.CENTER);
			return;
		}
		
		int nRows = 0;
		for(Control c : line.getControls()){
			if(c instanceof FloatControl)
				nRows += 2;
			if(c instanceof EnumControl)
				nRows += 1;
			if(c instanceof BooleanControl) 
				nRows += 1;
		}
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{94, 83, 83, 83, 32};
		gridBagLayout.rowHeights = new int[nRows];
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[nRows];
		
		for(int i = 0; i<nRows; i++){
			gridBagLayout.rowHeights[i] = 35;
		}
		
		lineControls.setLayout(gridBagLayout);
		this.add(lineControls,BorderLayout.CENTER);
		
		setTitle(title);
		setSize(400, 300);
		//getContentPane().add(this);
		setVisible(true);
		
		int row = 0;
		for(Control c : line.getControls()){
			if(c instanceof FloatControl){
				addFloatControl((FloatControl)c, row);
				row +=2;
				//gridBagLayout.rowHeights[row+1] = 29;
			}
			if(c instanceof EnumControl){
				addEnumControl((EnumControl)c);
				row +=1;
			}
			if(c instanceof BooleanControl){
				addBooleanControl((BooleanControl)c, row);
				row +=1;
			}
		}
	}
	
	private void addBooleanControl(BooleanControl control, int row) {
		JLabel lblNewLabel = new JLabel(control.getType().toString());
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = row;
		lineControls.add(lblNewLabel, gbc_lblNewLabel);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox(control.getStateLabel(control.getValue()));
		chckbxNewCheckBox.setSelected(control.getValue());
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.gridx = 1;
		gbc_chckbxNewCheckBox.gridy = row;
		lineControls.add(chckbxNewCheckBox, gbc_chckbxNewCheckBox);
		chckbxNewCheckBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				control.setValue(chckbxNewCheckBox.isSelected());
				chckbxNewCheckBox.setText(control.getStateLabel(control.getValue()));
			}
			
		});
		
		
	}

	private void addEnumControl(EnumControl c) {
		// TODO Auto-generated method stub
		
	}

	private void addFloatControl(FloatControl control, int row){
		int nPositions = (int)((control.getMaximum()-control.getMinimum())/control.getPrecision());
		int currentPosition = (int)((control.getValue()-control.getMinimum())/control.getPrecision());
		
		JLabel lblNewLabel = new JLabel(control.getType().toString());
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = row;
		lineControls.add(lblNewLabel, gbc_lblNewLabel);
		JSlider slider = new JSlider(0, nPositions, currentPosition);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(10);
		final JTextField textField = new JTextField();
		slider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent event){
				float value = control.getPrecision()*slider.getValue()+control.getMinimum();
				control.setValue(value);
				textField.setText(Float.toString(value));
			}
		});
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.gridwidth = 2;
		gbc_slider.fill = GridBagConstraints.BOTH;
		gbc_slider.insets = new Insets(0, 0, 5, 5);
		gbc_slider.gridx = 1;
		gbc_slider.gridy = row;
		lineControls.add(slider, gbc_slider);
		
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 3;
		gbc_textField.gridy = row;
		lineControls.add(textField, gbc_textField);
		textField.setText(Float.toString(control.getValue()));
		textField.setColumns(6);
		textField.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					float val = (float) Double.parseDouble(textField.getText());
					if(val > control.getMaximum() || val < control.getMinimum())
						throw new Exception("unacceptable value for control");
					control.setValue(val);
					slider.setValue((int)((val-control.getMinimum())/control.getPrecision()));
				}catch(Exception ex){
					textField.setText(Float.toString(control.getValue()));
				}
			}
			
		});
		
		JLabel lblUnits = new JLabel(control.getUnits());
		GridBagConstraints gbc_lblUnits = new GridBagConstraints();
		gbc_lblUnits.insets = new Insets(0, 0, 5, 0);
		gbc_lblUnits.fill = GridBagConstraints.BOTH;
		gbc_lblUnits.gridx = 4;
		gbc_lblUnits.gridy = row;
		lineControls.add(lblUnits, gbc_lblUnits);
		
		JLabel lblMin = new JLabel(control.getMinLabel());
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblMin.insets = new Insets(0, 0, 0, 5);
		gbc_lblMin.gridx = 1;
		gbc_lblMin.gridy = 1+row;
		lineControls.add(lblMin, gbc_lblMin);
		
		JLabel lblMax = new JLabel(control.getMaxLabel());
		GridBagConstraints gbc_lblMax = new GridBagConstraints();
		gbc_lblMax.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblMax.insets = new Insets(0, 0, 0, 5);
		gbc_lblMax.gridx = 2;
		gbc_lblMax.gridy = 1+row;
		lineControls.add(lblMax, gbc_lblMax);
	}

	
	

	public JFrame showInFrame(String title) {
		JFrame frame = new JFrame();
		frame.setTitle(title);
		frame.setSize(400, 300);
		frame.getContentPane().add(this);
		frame.setVisible(true);
		return frame;
	}
}
