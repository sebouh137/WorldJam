package worldjam.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import worldjam.exe.Client;

import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.SpringLayout;

public class RecordDialog extends JFrame {

	private Client client;

	private static final Icon RECORD_ICON = new ImageIcon(RecordDialog.class.getResource("/worldjam/gui/icons/record.png"));
	private static final Icon STOP_ICON = new ImageIcon(RecordDialog.class.getResource("/worldjam/gui/icons/stop.png"));
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;

	private JLabel lblTime;

	private File outputFile;



	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			RecordDialog dialog = new RecordDialog(null);
			dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void stopRecording() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Create the dialog.
	 */
	public RecordDialog(Client client) {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowListener(){

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				if(isRecording){
					stopRecording();
					isRecording = false;
				}
			}

			

			@Override
			public void windowClosed(WindowEvent e) {
				if(isRecording){
					stopRecording();
					isRecording = false;
				}
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

		});
		setTitle("Record to file");
		setBounds(100, 100, 505, 174);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblSelectFile = new JLabel("Select file");
			GridBagConstraints gbc_lblSelectFile = new GridBagConstraints();
			gbc_lblSelectFile.insets = new Insets(0, 0, 5, 5);
			gbc_lblSelectFile.anchor = GridBagConstraints.EAST;
			gbc_lblSelectFile.gridx = 0;
			gbc_lblSelectFile.gridy = 0;
			contentPanel.add(lblSelectFile, gbc_lblSelectFile);
		}
		{
			{
				textField = new JTextField();
				GridBagConstraints gbc_textField = new GridBagConstraints();
				gbc_textField.insets = new Insets(0, 0, 5, 5);
				gbc_textField.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField.gridx = 1;
				gbc_textField.gridy = 0;
				contentPanel.add(textField, gbc_textField);
				textField.setColumns(10);
			}
		}
		JButton btnSelect = new JButton("...");
		btnSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("World jam raw signal files", "wjr"));

				fileChooser.showSaveDialog(RecordDialog.this);
				File file = fileChooser.getSelectedFile();
				if(file != null){
					if(!file.getPath().endsWith(".wjr")){
						file = new File(file.getPath() + ".wjr");
					}
					textField.setText(file.getPath());
				}
			}
		});
		GridBagConstraints gbc_btnSelect = new GridBagConstraints();
		gbc_btnSelect.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelect.gridx = 2;
		gbc_btnSelect.gridy = 0;
		contentPanel.add(btnSelect, gbc_btnSelect);
		{
			chckbxRecordVideo = new JCheckBox("Record Video");
			chckbxRecordVideo.setSelected(true);
			GridBagConstraints gbc_chckbxRecordVideo = new GridBagConstraints();
			gbc_chckbxRecordVideo.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxRecordVideo.gridx = 0;
			gbc_chckbxRecordVideo.gridy = 1;
			contentPanel.add(chckbxRecordVideo, gbc_chckbxRecordVideo);
		}
		{
			chckbxRecordAudio = new JCheckBox("Record Audio");
			chckbxRecordAudio.setSelected(true);
			GridBagConstraints gbc_chckbxRecordAudio = new GridBagConstraints();
			gbc_chckbxRecordAudio.insets = new Insets(0, 0, 0, 5);
			gbc_chckbxRecordAudio.gridx = 0;
			gbc_chckbxRecordAudio.gridy = 2;
			contentPanel.add(chckbxRecordAudio, gbc_chckbxRecordAudio);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(Color.BLACK);
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			SpringLayout sl_buttonPane = new SpringLayout();
			//buttonPane.setLayout(sl_buttonPane);
			{
				JButton recordButton = new JButton();
				recordButton.setBackground(Color.BLACK);
				buttonPane.add(recordButton);
				recordButton.setIcon(RECORD_ICON);
				recordButton.addActionListener(e->{
					if (!isRecording){
						startRecording(new File(textField.getText()),
								chckbxRecordVideo.isSelected(), 
								chckbxRecordAudio.isSelected());
						outputFile = new File(textField.getText());

						isRecording = true;
						timeStartRecording = System.currentTimeMillis();
						new RefreshStatsThread().start();
						recordButton.setIcon(STOP_ICON);
					} else {
						isRecording = false;
						stopRecording();
						recordButton.setIcon(RECORD_ICON);
					}
				});
			}
			{
				lblTime = new JLabel("  00:00:00      0  B");
				lblTime.setFont(new Font("Monospaced", Font.PLAIN, 30));
				lblTime.setForeground(Color.RED);
				lblTime.setBackground(Color.BLACK);
				buttonPane.add(lblTime);
			}
		}
	}

	private void startRecording(File file, boolean selected, boolean selected2) {
		// TODO Auto-generated method stub
		
	}

	class RefreshStatsThread extends Thread{
		public void run(){
			while(isRecording){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long dtime = System.currentTimeMillis() - timeStartRecording; 
				int hours = (int)(dtime / 3600000);
				int minutes = (int)((dtime / 60000)%60);
				int seconds = (int)((dtime / 1000)%60);
				long filesize = outputFile != null ? outputFile.length() : 0;
				String filesizeTxt="";
				if(filesize< 1024)
					filesizeTxt = String.format("%4d  B", filesize);
				else if(filesize< 1024*1024)
					if(filesize< 10*1024)
						filesizeTxt = String.format(" %.1f KB", filesize/1024.);
					else 
						filesizeTxt = String.format("%4d KB", filesize/1024);
				else if(filesize< 1024*1024*1024)
					if(filesize< 10*1024*1024)
						filesizeTxt = String.format(" %.1f MB", filesize/(1024*1024.));
					else 
						filesizeTxt = String.format("%4d MB", filesize/(1024*1024));
				else //I doubt anyone will ever record a terabyte on worldjam.  
					// maybe a GB if they record both video and audio for a full concert.
					if(filesize< 10*1024*1024*1024)
						filesizeTxt = String.format(" %.1f GB", filesize/(1024*1024*1024.));
					else 
						filesizeTxt = String.format("%4d GB", filesize/(1024*1024*1024));
				lblTime.setText(String.format("    %02d:%02d:%02d   ", hours, minutes,seconds) + filesizeTxt);

			}
		}
	}

	long timeStartRecording;
	boolean isRecording;

	private JCheckBox chckbxRecordVideo;

	private JCheckBox chckbxRecordAudio;

	private JLabel lblFilesize;

}
