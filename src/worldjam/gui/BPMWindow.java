package worldjam.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import worldjam.gui.conductor.Conductor;
import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.time.MutableClock;
import worldjam.util.AdaptiveTempoCalculator;
import worldjam.util.DefaultObjects;
import worldjam.util.FittedTempoCalculator2;
import worldjam.util.TempoCalculator;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JSeparator;
import java.awt.Color;

public class BPMWindow extends JFrame {

	private JPanel contentPane;
	private JTextField txtBPM;
	private ButtonGroup buttonGroup_1;
	private JSpinner spinner;
	private JSpinner spinner_1;
	private JSpinner spinner_msPerBeat;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BPMWindow frame = new BPMWindow(new MutableClock(DefaultObjects.bc0), null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public BPMWindow(MutableClock clock, ClockSubscriber listener) {
		this.mutableClock = clock;
		tc = new FittedTempoCalculator2(clock.getSetting());
		setTitle("Tempo Settings");
		setBounds(100, 100, 281, 291);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{65, 70};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0,0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0,0.0, 1.0, 0.0};
		contentPane.setLayout(gbl_contentPane);

		ChangeListener changeTimeSignature = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				mutableClock.adjustTimeSignature((int)spinner.getValue(), mutableClock.getSetting().beatDenominator);
			}
		};



		JLabel lblTimeSignature = new JLabel("      Time Signature");
		lblTimeSignature.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblTimeSignature = new GridBagConstraints();
		gbc_lblTimeSignature.gridwidth = 2;
		gbc_lblTimeSignature.anchor = GridBagConstraints.EAST;
		gbc_lblTimeSignature.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeSignature.gridx = 0;
		gbc_lblTimeSignature.gridy = 0;
		contentPane.add(lblTimeSignature, gbc_lblTimeSignature);

		spinner = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.EAST;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 2;
		gbc_spinner.gridy = 0;
		spinner.setModel(new SpinnerNumberModel(clock.getSetting().beatsPerMeasure,1,64,1));
		contentPane.add(spinner, gbc_spinner);
		spinner.addChangeListener(changeTimeSignature);

		spinner_1 = new JSpinner();
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_1.gridx = 3;
		gbc_spinner_1.gridy = 0;

		spinner_1.setModel(new SpinnerListModel(new Integer[]{1, 2, 4, 8, 16, 32, 64}));
		spinner_1.getModel().setValue(clock.getSetting().beatDenominator);
		contentPane.add(spinner_1, gbc_spinner_1);
		spinner_1.addChangeListener(changeTimeSignature);

		separator = new JSeparator();
		separator.setForeground(Color.GRAY);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 4;
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 1;
		contentPane.add(separator, gbc_separator);

		txtBPM = new JTextField();
		txtBPM.setHorizontalAlignment(SwingConstants.RIGHT);
		txtBPM.setText("120");
		GridBagConstraints gbc_txtBPM = new GridBagConstraints();
		gbc_txtBPM.insets = new Insets(0, 0, 5, 5);
		gbc_txtBPM.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtBPM.gridx = 0;
		gbc_txtBPM.gridy = 2;
		contentPane.add(txtBPM, gbc_txtBPM);
		txtBPM.setColumns(1);

		txtBPM.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				int val = (int)(60000./Double.parseDouble(txtBPM.getText()));
				val = val-(val%10);
				spinner_msPerBeat.setValue(val);
				txtBPM.setText(String.format("%.2f", 60000./val));
				mutableClock.adjustTempo(val);
			}

		});

		lblBpm = new JLabel("bpm");
		lblBpm.setLabelFor(txtBPM);
		GridBagConstraints gbc_lblBpm = new GridBagConstraints();
		gbc_lblBpm.anchor = GridBagConstraints.WEST;
		gbc_lblBpm.insets = new Insets(0, 0, 5, 5);
		gbc_lblBpm.gridx = 1;
		gbc_lblBpm.gridy = 2;
		contentPane.add(lblBpm, gbc_lblBpm);


		spinner_msPerBeat = new JSpinner();
		spinner_msPerBeat.setModel(new SpinnerNumberModel(500, 10, 1000, 10));
		GridBagConstraints gbc_txt_msPerBeat = new GridBagConstraints();
		gbc_txt_msPerBeat.insets = new Insets(0, 0, 5, 5);
		gbc_txt_msPerBeat.gridx = 2;
		gbc_txt_msPerBeat.gridy = 2;
		contentPane.add(spinner_msPerBeat, gbc_txt_msPerBeat);

		spinner_msPerBeat.addChangeListener(new ChangeListener(){


			@Override
			public void stateChanged(ChangeEvent e) {
				int val = (int)spinner_msPerBeat.getValue();
				val = val-(val%10);
				spinner_msPerBeat.setValue(val);
				txtBPM.setText(String.format("%.2f", 60000./val));
				mutableClock.adjustTempo(val);
			}

		});

		lblMs = new JLabel("   ms   ");
		lblMs.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblMs = new GridBagConstraints();
		gbc_lblMs.anchor = GridBagConstraints.WEST;
		gbc_lblMs.insets = new Insets(0, 0, 5, 0);
		gbc_lblMs.gridx = 3;
		gbc_lblMs.gridy = 2;
		contentPane.add(lblMs, gbc_lblMs);

		btnNewButton = new JButton("tap rhythm");
		btnNewButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.getWhen() + " " + System.currentTimeMillis());
				tc.newBeat(e.getWhen());
				float BPM = (float) tc.getClockSetting().getBPM();
				if(BPM != 0){
					int val = (int)(60000./BPM);
					spinner_msPerBeat.setValue(val);
					txtBPM.setText(String.format("%.2f", 60000./val));
					
					mutableClock.adjustTempo(val);
				}


			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridwidth = 2;
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 3;
		contentPane.add(btnNewButton, gbc_btnNewButton);
		
		button = new JButton("Apply");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clock.changeSettingsNow(tc.getClockSetting());
				if(listener != null)
					listener.changeClockSettingsNow(clock.getSetting());
				System.out.println("changing clock setting to " + clock.getSetting());
			}
		});
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.insets = new Insets(0, 0, 0, 5);
		gbc_button.gridx = 1;
		gbc_button.gridy = 6;
		contentPane.add(button, gbc_button);
		
		btnReset = new JButton("Reset");
		GridBagConstraints gbc_btnReset = new GridBagConstraints();
		gbc_btnReset.insets = new Insets(0, 0, 0, 5);
		gbc_btnReset.gridx = 2;
		gbc_btnReset.gridy = 6;
		contentPane.add(btnReset, gbc_btnReset);
		mutableClock.addChangeSubscriber(tc);
		
		GridBagConstraints gbc_conductor = new GridBagConstraints();
		gbc_conductor.insets = new Insets(0, 0, 0, 0);
		gbc_conductor.gridx = 1;
		gbc_conductor.gridy = 5;
		gbc_conductor.gridwidth=2;
		conductor = new Conductor(clock.getSetting());
		mutableClock.addChangeSubscriber(conductor);
		contentPane.add(conductor, gbc_conductor);
		//contentPane.add(button, gbc_button);
		
		
		Random random = new Random();
		btnNewButton = new JButton("Randomize");
		btnNewButton.setToolTipText("randomly selects a time signature and tempo.");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double  mu = 110;
				double sigma = 30;
				double BPM = mu+random.nextGaussian()*sigma;
				
				int val = (int)(60000./BPM);
				spinner_msPerBeat.setValue(val);
				txtBPM.setText(String.format("%.2f", 60000./val));
				
				int newBeatCount = 0;
				int denom = 4;
				float r = random.nextFloat();
				if(r<.5) {
					newBeatCount=4;
					denom =4;
				} else if (r<.7) {
					newBeatCount=3;
					denom =4;
				} else if (r<.8) {
					newBeatCount=6;
					denom =8;
				}else if (r<.9) {
					newBeatCount=5;
					denom =4;
				} else if (r<.95) {
					newBeatCount=7;
					denom =8;
				} else {
					newBeatCount=2;
					denom =4;
				}
				

				spinner.setValue(newBeatCount);
				spinner_1.setValue(denom);
				
				ClockSetting clk = conductor.getClock();
				clk = clk.createWithDifferentBeatCount(newBeatCount);
				clk = clk.createWithDifferentTempo(val);
				conductor.changeClockSettingsNow(clk);
			}
		});
		gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridwidth = 2;
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 4;
		contentPane.add(btnNewButton, gbc_btnNewButton);
		
	}
	FittedTempoCalculator2 tc;
	
	private MutableClock mutableClock;
	private JButton btnNewButton;
	private JLabel lblBpm;
	private JLabel lblMs;
	private JSeparator separator;
	private JButton button;
	private JButton btnReset;
	private Conductor conductor;

}
