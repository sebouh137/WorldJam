package worldjam.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import worldjam.exe.Client;
import worldjam.time.DelaySetting;
import worldjam.util.DefaultObjects;
import worldjam.video.ViewPanel;
import worldjam.video.WebcamInterface;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.awt.Insets;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

public class WebcamControlDialog extends JFrame {

	private final String NO_WEBCAM = "[none]";
	private JPanel contentPane;
	private JComboBox cmbxWebcams;
	private JComboBox cmbxResolution;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WebcamControlDialog frame = new WebcamControlDialog(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	WebcamInterface webcamInterface;
	/**
	 * Create the frame.
	 */
	public WebcamControlDialog(Client client) {
		Webcam selectedWebcam = null;
		if (client != null) webcamInterface = client.getWebcamInterface();
		if(webcamInterface != null)
			selectedWebcam = webcamInterface.getWebcam();
		setTitle("WorldJam: Video Input Settings");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(500, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		JPanel panel1 = new JPanel();
		GridBagLayout gbl_panel1 = new GridBagLayout();
		gbl_panel1.columnWidths = new int[]{133, 116, 0};
		gbl_panel1.rowHeights = new int[]{27, 0, 0};
		gbl_panel1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel1.setLayout(gbl_panel1);

		JLabel lblVideoInputDevice = new JLabel("Video input device");
		GridBagConstraints gbc_lblVideoInputDevice = new GridBagConstraints();
		gbc_lblVideoInputDevice.anchor = GridBagConstraints.EAST;
		gbc_lblVideoInputDevice.insets = new Insets(0, 0, 5, 5);
		gbc_lblVideoInputDevice.gridx = 0;
		gbc_lblVideoInputDevice.gridy = 0;
		panel1.add(lblVideoInputDevice, gbc_lblVideoInputDevice);

		List<Webcam> webcams = Webcam.getWebcams();
		Vector<String> names = new Vector();
		names.add(NO_WEBCAM);
		for (Webcam webcam : webcams){
			names.add(webcam.getName());
		}

		cmbxWebcams = new JComboBox();
		if(selectedWebcam != null){
			cmbxWebcams.setSelectedItem(selectedWebcam.getName());
		}
		else 
			cmbxWebcams.setSelectedItem(NO_WEBCAM);
		contentPane.add(panel1);

		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel1.add(cmbxWebcams, gbc_comboBox);
		cmbxWebcams.setModel(new DefaultComboBoxModel<String>(names));

		cmbxWebcams.addItemListener(e->{
			setResolutionModel();
		});

		JLabel lblResolution = new JLabel("Resolution");
		GridBagConstraints gbc_lblResolution = new GridBagConstraints();
		gbc_lblResolution.anchor = GridBagConstraints.EAST;
		gbc_lblResolution.insets = new Insets(0, 0, 0, 5);
		gbc_lblResolution.gridx = 0;
		gbc_lblResolution.gridy = 1;
		panel1.add(lblResolution, gbc_lblResolution);

		cmbxResolution = new JComboBox();
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 1;
		panel1.add(cmbxResolution, gbc_comboBox_1);

		setResolutionModel();

		viewer = new ViewPanel(DefaultObjects.bc0);
		viewer.changeDelaySetting(new DelaySetting(0,50));
		panel1.add(viewer);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);

		JButton btnApply = new JButton("Apply");
		panel.add(btnApply);
		btnApply.addActionListener(e->{
			if(!cmbxWebcams.getSelectedItem().equals(NO_WEBCAM)) {
				Webcam cam = Webcam.getWebcamByName((String)cmbxWebcams.getSelectedItem());
				cam.close();
				
				if(webcamInterface == null) {
					webcamInterface = new WebcamInterface(cam);
					client.attachWebcam(webcamInterface);
					webcamInterface.setEnabled(true);
				} else {
					
				}
				webcamInterface.setWebcam(cam);
				String[] selectedResStr = ((String)cmbxResolution.getSelectedItem()).split("x");
				
				cam.setViewSize(new Dimension(Integer.parseInt(selectedResStr[0]),Integer.parseInt(selectedResStr[1])));
				if(!cam.isOpen())
					cam.open(false);
				webcamInterface.setEnabled(true);
			} else { //disable webcam
				if(client.getWebcamInterface() != null) {
					client.getWebcamInterface().setEnabled(false);
					Webcam cam = client.getWebcamInterface().getWebcam();
					cam.close();
				}
			}
		});

	}
	ViewPanel viewer;

	private void setResolutionModel() {
		if(cmbxWebcams.getSelectedItem().equals(NO_WEBCAM)){
			cmbxResolution.setEnabled(false);

		}
		else{
			cmbxResolution.setEnabled(true);
			Vector<String> strings = new Vector();

			Webcam cam = Webcam.getWebcamByName((String)cmbxWebcams.getSelectedItem());
			for(Dimension dim : cam.getViewSizes()){
				strings.add(dim.width + "x" + dim.height);
			}
			cmbxResolution.setModel(new DefaultComboBoxModel<String>(strings));
			if(cam != null && cam.getViewSize() != null) {
				cmbxResolution.setSelectedItem(cam.getViewSize().width + "x" + cam.getViewSize().height);
			}

			System.out.println(strings);
		}
	}
	@Override
	public void dispose(){
		viewer.close();
		super.dispose();
	}

}
