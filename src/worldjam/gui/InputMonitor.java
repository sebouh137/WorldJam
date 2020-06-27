package worldjam.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import worldjam.audio.InputThread;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JSpinner;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class InputMonitor extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2515574942742641615L;
	private JPanel contentPane;
	private SoundLevelBar slb;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InputMonitor frame = new InputMonitor(null, null);
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
	public InputMonitor(InputThread input, String title) {
		this.setTitle(title);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 413, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		if(input != null){
			slb = new SoundLevelBar(input, SoundLevelBar.VERTICAL);
			contentPane.add(slb, BorderLayout.WEST);
		}
		tabbedPane.addTab("Line", null, new LineControls(input.getLine()), null);
		tabbedPane.addTab("Info", null, createInfoPanel(input), null);
		tabbedPane.addTab("Misc", null, createMiscControlPanel(input), null);
	}

	private Component createMiscControlPanel(InputThread input) {
		JPanel panel = new JPanel();
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 79, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		{
			JLabel lblNewLabel = new JLabel("Bytes read per cyle");
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			panel.add(lblNewLabel, gbc_lblNewLabel);
		}

		JSpinner spinner = new JSpinner();
		int frameSize = input.getLine().getFormat().getFrameSize();
		float frameRate = input.getLine().getFormat().getFrameRate();
		spinner.setModel(new SpinnerNumberModel(input.getBufferLengthInBytes(), frameSize, (int)(frameSize*10*frameRate), frameSize));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		panel.add(spinner, gbc_spinner);

		JButton btnApply = new JButton("Apply");
		btnApply.setEnabled(false);
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				input.setBufferLengthInBytes((int)spinner.getValue());
				btnApply.setEnabled(false);
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		panel.add(btnApply, gbc_btnNewButton);
		
		spinner.addChangeListener(e->{
			btnApply.setEnabled(true);
		});

		return panel;
	}

	private Component createInfoPanel(InputThread input) {

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		String text = "mixer: \n" + input.getMixer().getMixerInfo().toString();
		text += "\n\ntarget data line:\n" + input.getLine().getLineInfo();
		text += "\n\nbuffersize = " + input.getLine().getBufferSize();
		textArea.setText(text);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		return textArea;
	}
	@Override
	public void dispose(){
		super.dispose();
		this.slb.stop();
	}

}
