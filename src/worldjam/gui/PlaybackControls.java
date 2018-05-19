package worldjam.gui;

import java.awt.GridLayout;

import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;
import javax.swing.JPanel;

import worldjam.test.DefaultObjects;

public class PlaybackControls extends JPanel {
	public static void main(String arg[]){
		JFrame frame = new JFrame();
		frame.setSize(600, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			frame.add(new PlaybackControls());
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.setVisible(true);
	}
	
	/**
	 * Create the panel.
	 */
	public PlaybackControls(Line line) {
		this.setLayout(new GridLayout(line.getControls().length, 1));
		for(Control c : line.getControls()){
			if(c instanceof FloatControl)
				this.add(new FloatControlGUI((FloatControl)c));
		}
	}

	static SourceDataLine defaultLine;
	static {
		try {
			DefaultObjects.outputMixer.open();
			defaultLine =(SourceDataLine) DefaultObjects.outputMixer.getLine(new SourceDataLine.Info(SourceDataLine.class, DefaultObjects.defaultFormat));
			defaultLine.open();
			defaultLine.start();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public PlaybackControls() throws LineUnavailableException{
		
		this(defaultLine);
	}
}
