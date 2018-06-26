package worldjam.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.EnumControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LineControls extends JPanel {
	public LineControls(Line line){
		if(line.getControls().length == 0){
			add(new JLabel("No controls to show"), BorderLayout.CENTER);
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
		setLayout(gridBagLayout);
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
		add(lblNewLabel, gbc_lblNewLabel);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox(control.getStateLabel(control.getValue()));
		chckbxNewCheckBox.setSelected(control.getValue());
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.gridx = 1;
		gbc_chckbxNewCheckBox.gridy = row;
		add(chckbxNewCheckBox, gbc_chckbxNewCheckBox);
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
		add(lblNewLabel, gbc_lblNewLabel);
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
		add(slider, gbc_slider);
		
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 3;
		gbc_textField.gridy = row;
		add(textField, gbc_textField);
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
		add(lblUnits, gbc_lblUnits);
		
		JLabel lblMin = new JLabel(control.getMinLabel());
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblMin.insets = new Insets(0, 0, 0, 5);
		gbc_lblMin.gridx = 1;
		gbc_lblMin.gridy = 1+row;
		add(lblMin, gbc_lblMin);
		
		JLabel lblMax = new JLabel(control.getMaxLabel());
		GridBagConstraints gbc_lblMax = new GridBagConstraints();
		gbc_lblMax.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblMax.insets = new Insets(0, 0, 0, 5);
		gbc_lblMax.gridx = 2;
		gbc_lblMax.gridy = 1+row;
		add(lblMax, gbc_lblMax);
	}
}
