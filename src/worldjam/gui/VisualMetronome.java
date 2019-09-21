package worldjam.gui;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;

import worldjam.time.ClockSetting;

public abstract class VisualMetronome extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9058766362349172334L;
	protected ClockSetting clock;

	//int MS_PER_FRAME= 40;
	int MS_PER_FRAME= 16;
	public VisualMetronome(ClockSetting clock){
		changeClockSettingsNow(clock);
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
		this.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(e.getKeyCode());
			
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	public void enableModTempoWithKeystroke(boolean enable){
		modTempoWithKeystroke = enable;
	}
	boolean modTempoWithKeystroke = false;
	
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
	public void changeClockSettingsNow(ClockSetting clock){
		this.clock = clock;
	}
	public ClockSetting getClock() {
		return clock;
	}
	@Override
	public void paintComponent(Graphics g){
		paint((Graphics2D)g.create(), System.currentTimeMillis());
	}

	public abstract void paint(Graphics2D g, long time); 
}
