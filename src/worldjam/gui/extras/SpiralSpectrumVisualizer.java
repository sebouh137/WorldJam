package worldjam.gui.extras;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;

import worldjam.audio.InputThread;
import worldjam.util.DefaultObjects;

public class SpiralSpectrumVisualizer extends SpectrumVisualizer{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2803980870751956754L;

	public static void main(String arg[]) throws LineUnavailableException {
		JFrame frame = new JFrame();
		SpectrumVisualizer sv = new SpiralSpectrumVisualizer();
		frame.add(sv);
		frame.setSize(500,500);
		frame.setVisible(true);
		InputThread it = new InputThread(DefaultObjects.getInputMixer(), DefaultObjects.defaultFormat, null, 100);
		it.addSubscriber(sv);
		it.start();
	}
	public void paint(Graphics g1){
		Graphics2D g = (Graphics2D)g1;
		super.paint(g);
		int w = getWidth();
		int h = getHeight();
		
		for(int i = 0; i < 12; i++)	{
			int r = Math.min(w, h)*9/20;
			int x = w/2 + (int)(r*Math.cos(i*2*Math.PI/12));
			int y = h/2 + (int)(r*Math.sin(i*2*Math.PI/12));
			if(noteNames[i].length()==1)
				g.setFont(font1);
			else
				g.setFont(font2);
			g.setColor(Color.BLACK);
			x-=g.getFontMetrics().stringWidth(noteNames[i])/2;
			y+=g.getFontMetrics().getMaxAscent()/2;
			g.drawString(noteNames[i], x,y);
			g.setColor(Color.RED);
			g.drawLine(w/2,h/2,
					w/2+ (int)(.95*r*Math.cos(i*2*Math.PI/12)),
					h/2+ (int)(.95*r*Math.sin(i*2*Math.PI/12)));
		}
		int d = divisionsPerSemitone;
		
		for(int j = 0; j<fourierResults.length;  j++){
			double offset = 3.*d*12;
			int r = (int)(Math.min(w,h)*(fourierResults.length-j+offset)/(fourierResults.length+offset)*.4); 
			double jp = j-(d/2.);
			int startAngle = (int)(-(jp+1)*360./(12*d));
			int arcAngle = (int)(-(jp)*360./(12*d))-startAngle;

			float db = (float) Math.log10(fourierResults[j])*10+50;

			float b = db/50.f; 
			if(b<0)
				b = 0;
			Color color = Color.getHSBColor(.3f, 1, b);
			g.setColor(color);
			g.fillArc(w/2-r, h/2-r, 2*r, 2*r, startAngle, arcAngle);
		}
		for (int i = 0; i < 12; i++) {
			int r = Math.min(w,h)/2;
			double theta = 2*Math.PI/12*(i-0.5);
			g.drawLine(w/2,h/2,w/2+(int)(r*Math.cos(theta)),h/2+(int)(r*Math.sin(theta)));
		}

	}
}
