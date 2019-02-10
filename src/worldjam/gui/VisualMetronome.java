package worldjam.gui;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import worldjam.core.BeatClock;

public abstract class VisualMetronome extends Canvas {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9058766362349172334L;
	protected BeatClock clock;

	//int MS_PER_FRAME= 40;
	int MS_PER_FRAME= 16;
	public VisualMetronome(BeatClock clock){
		setClock(clock);
		Thread th = new Thread(){
			public void run(){
				while(!closed){
					try {
						Thread.sleep(MS_PER_FRAME);
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
	private boolean closed = false;
	public void close(){
		this.closed = true;
	}
	public JFrame showInFrame(){
		JFrame frame = new JFrame();
		frame.add(this);
		frame.setSize(200, 200);
		frame.setVisible(true);
		return frame;
	}
	public void setClock(BeatClock clock){
		this.clock = clock;
	}
	public BeatClock getClock() {
		return clock;
	}
	boolean useBufferedImage = false;
	public void paint(Graphics g){
		//super.paint(g);
		if(useBufferedImage){
			super.paint(g);
			if(prev != null)
				g.drawImage(prev, 0, 0, null);
			BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g2 = (Graphics2D)img.createGraphics();
			this.paint(g2, System.currentTimeMillis());
			g.drawImage(img, 0, 0, null);

			prev = img;
		}
		else paint((Graphics2D)g, System.currentTimeMillis());
	}

	BufferedImage prev = null;

	public abstract void paint(Graphics2D g, long time); 
}
