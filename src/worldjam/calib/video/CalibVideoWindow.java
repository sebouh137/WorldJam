package worldjam.calib.video;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import worldjam.time.ClockSetting;
import worldjam.util.DefaultObjects;
import worldjam.video.VideoFrame;
import worldjam.video.ViewPanel;

public class CalibVideoWindow extends JFrame {

	static BufferedImage allBlack;
	static BufferedImage allWhite;
	
	static {
		allBlack = new BufferedImage(1800,1800, BufferedImage.TYPE_3BYTE_BGR);
		allWhite = new BufferedImage(1800,1800, BufferedImage.TYPE_3BYTE_BGR);
		for(int x = 0; x< allBlack.getWidth(); x++){
			for(int y = 0; y< allBlack.getWidth(); y++){
				allBlack.setRGB(x, y, 0);
				allWhite.setRGB(x, y, 0xffffffff);
			}
		}
	}
	
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CalibVideoWindow frame = new CalibVideoWindow(null);

					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
	
	public CalibVideoWindow(CalibVideo calibrator) {
		setBounds(0, 0, 800, 1800);
		this.setState(MAXIMIZED_BOTH);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		ClockSetting clock = DefaultObjects.bc0;
		ViewPanel panel = new ViewPanel(clock);
		panel.imageReceived(new VideoFrame(allBlack, System.currentTimeMillis(), 0));
		contentPane.add(panel, BorderLayout.CENTER);
		JPanel panel2 = new JPanel();
		contentPane.add(panel2, BorderLayout.SOUTH);
		JButton button = new JButton("Calibrate");
		button.setBackground(Color.BLACK);
		button.setForeground(Color.GREEN);
		panel2.add(button);
		panel2.setBackground(Color.BLACK);
		int duration = 10;
		button.addActionListener(e->{
			new Thread(()->{
				long timestamp = System.currentTimeMillis();
				calibrator.actualFlashTime = timestamp + clock.getMsPerMeasure();
				calibrator.initiate();
				panel.imageReceived(new VideoFrame(allWhite, timestamp, 0));
				panel.imageReceived(new VideoFrame(allBlack, timestamp+duration, 0));
			}).start();
		});
	}

}
