package worldjam.gui;

import java.awt.Canvas;

import javax.swing.JFrame;

import worldjam.core.BeatClock;

public class VisualMetronome extends Canvas {
	protected BeatClock clock;

	public VisualMetronome(BeatClock clock){
		this.clock = clock;
		Thread th = new Thread(){
			public void run(){
				while(true){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					repaint();
				}
			}
		};
		th.start();
	}
	public JFrame showInFrame(){
		JFrame frame = new JFrame();
		frame.add(this);
		frame.setSize(200, 200);
		frame.setVisible(true);
		return frame;
	}
}
