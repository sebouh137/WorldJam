package worldjam.gui;

import javax.swing.JPanel;
import javax.swing.JSlider;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.FloatControl;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class FloatControlGUI extends JPanel {
	private JTextField textField;

	/**
	 * Create the panel.
	 */
	public FloatControlGUI(FloatControl control) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{94, 77, 83, 53, 32, 0};
		gridBagLayout.rowHeights = new int[]{39, 23, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		int nPositions = (int)((control.getMaximum()-control.getMinimum())/control.getPrecision());
		int currentPosition = (int)((control.getValue()-control.getMinimum())/control.getPrecision());
		
		JLabel lblNewLabel = new JLabel(control.getType().toString());
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		JSlider slider = new JSlider(0, nPositions, currentPosition);
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
		gbc_slider.gridy = 0;
		add(slider, gbc_slider);
		
		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 3;
		gbc_textField.gridy = 0;
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
		gbc_lblUnits.gridy = 0;
		add(lblUnits, gbc_lblUnits);
		
		JLabel lblMin = new JLabel(control.getMinLabel());
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblMin.insets = new Insets(0, 0, 0, 5);
		gbc_lblMin.gridx = 1;
		gbc_lblMin.gridy = 1;
		add(lblMin, gbc_lblMin);
		
		JLabel lblMax = new JLabel(control.getMaxLabel());
		GridBagConstraints gbc_lblMax = new GridBagConstraints();
		gbc_lblMax.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblMax.insets = new Insets(0, 0, 0, 5);
		gbc_lblMax.gridx = 2;
		gbc_lblMax.gridy = 1;
		add(lblMax, gbc_lblMax);
	}
	//for testing purposes
	public FloatControlGUI(){
		this(createTestControl());
	}
	private static FloatControl createTestControl() {
		FloatControl fc = new FloatControl(FloatControl.Type.MASTER_GAIN, -80, (float) 6.0206, 1, 10, 0, "dB", "-80", "-43", "+6"){
			
		};
		return fc;
	}

}
