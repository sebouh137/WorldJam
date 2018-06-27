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
		setBounds(100, 100, 450, 300);
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
	}

	private Component createInfoPanel(InputThread input) {
		
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		String text = input.getMixer().getMixerInfo().toString();
		
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
