package worldjam.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;

import worldjam.audio.InputThread;
import worldjam.audio.RMS;
import worldjam.util.DefaultObjects;

public class SoundLevelBar extends Canvas{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4132565555186420933L;
	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;
	private RMS rms;
	public SoundLevelBar(RMS rms, int orientation){
		this.rms = rms;
		this.setMinimumSize(new Dimension(30, 100));
		this.setPreferredSize(new Dimension(30, 100));
		
		new Thread(){
			public void run(){
				while(!stopped){
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					repaint();
				}
			}
		}.start();
		this.orientation = orientation;
	}
	private int orientation;
	private boolean stopped;
	public void stop(){
		stopped = true;
	}
	
	Color darkRed = new Color(127, 0, 0);
	Color darkYellow = new Color(127, 127, 0);
	Color darkGreen = new Color(0, 127, 0);
	double log10 = Math.log(10);
	private boolean useDB = true;
	public void paint(Graphics g){
		super.paint(g);
		double level = rms.getRMS(200);
		int divisions = 10;
		double yellowThreshold = 1, redThreshold = 1;
		if (useDB){
			double minDB = 120;
			 double levelDB = -minDB;
			 if(level > 0)
				 levelDB = Math.log(level)*20/log10;
			 
			 if(level == 0 || levelDB<=-minDB)
				 levelDB = -minDB;
			 if(levelDB>=0)
				 levelDB = 0;
			 level = (levelDB + minDB)/minDB;
			 divisions = (int)(minDB/10);
			 if(getHeight()<40 && orientation == VERTICAL || getWidth()<40 && orientation == HORIZONTAL) {
				 divisions = (int)(minDB/20);
			 }
			 redThreshold = (minDB-9)/minDB;
			 yellowThreshold = (minDB-18)/minDB;
		}
		
		if(level < yellowThreshold)
			g.setColor(Color.GREEN);
		else if (level < redThreshold)
			g.setColor(Color.YELLOW);
		else 
			g.setColor(Color.RED);
		//System.out.println(level);
		switch(orientation){
		case VERTICAL:
			if(level<yellowThreshold){
				g.setColor(Color.GREEN);
				fill(g, 0, level);
				g.setColor(darkGreen);
				fill(g, level, yellowThreshold);
				g.setColor(darkYellow);
				fill(g, yellowThreshold, redThreshold);
				g.setColor(darkRed);
				fill(g, redThreshold, 1);
			} else if(level<redThreshold){
				g.setColor(Color.GREEN);
				fill(g, 0, yellowThreshold);
				g.setColor(Color.yellow);
				fill(g, yellowThreshold, level);
				g.setColor(darkYellow);
				fill(g, level, redThreshold);
				g.setColor(darkRed);
				fill(g, redThreshold, 1);
			} else {
				g.setColor(Color.GREEN);
				fill(g, 0, yellowThreshold);
				g.setColor(Color.yellow);
				fill(g, yellowThreshold, redThreshold);
				g.setColor(Color.RED);
				fill(g, redThreshold, level);
				g.setColor(darkRed);
				fill(g, level, 1);
			}
			
			
			g.setColor(Color.BLACK);
			g.drawLine(getWidth()/4, getHeight()/10, getWidth()/4, 9*getHeight()/10);
			g.drawLine(3*getWidth()/4, getHeight()/10, 3*getWidth()/4, 9*getHeight()/10);
			for(int i = 0; i<divisions+1; i++){
				int y1 = (int)((.9-.8*i/divisions)*getHeight());
				g.drawLine(getWidth()/4, y1, 3*getWidth()/4, y1);
			}
			break;
		case HORIZONTAL:
			g.fillRect(getWidth()/10, getHeight()/4, (int)(.8*getWidth()*level), getHeight()/2);

			g.setColor(Color.BLACK);
			g.drawLine(getWidth()/10, getHeight()/4, 9*getWidth()/10, getHeight()/4);
			g.drawLine(getWidth()/10, 3*getHeight()/4, 9*getWidth()/10, 3*getHeight()/4);
			for(int i = 0; i<divisions+1; i++){
				int x1 = (int)((.1+.8*i/divisions)*getWidth());
				g.drawLine(x1, getHeight()/4, x1, 3*getHeight()/4);
			}
		}
	}
	
	void fill(Graphics g, double minLevel, double maxLevel){
		g.fillRect(getWidth()/4, (int)((.9-.8*maxLevel)*getHeight()), getWidth()/2, (int)(.8*(maxLevel-minLevel)*getHeight())+1);
	}
	
	/*public static void main(String arg[]) throws LineUnavailableException{
		JFrame frame = new JFrame();
		System.out.println(DefaultObjects.inputMixer.getClass());
		DefaultObjects.inputMixer.open();
		InputThread inputThread = new InputThread(DefaultObjects.inputMixer, DefaultObjects.defaultFormat, DefaultObjects.bc0);
		inputThread.start();
		frame.add(new SoundLevelBar(inputThread, VERTICAL));
		frame.setSize(40, 140);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}	*/
}
