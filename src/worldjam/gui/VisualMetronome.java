package worldjam.gui;

import java.awt.Canvas;

import worldjam.core.BeatClock;

public class VisualMetronome extends Canvas {
	protected BeatClock clock;

	public VisualMetronome(BeatClock clock){
	this.clock = clock;
	Thread th = new Thread(){
		public void run(){
			while(true){
				try {
					Thread.sleep(5);
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

}
