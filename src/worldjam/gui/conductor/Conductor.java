package worldjam.gui.conductor;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import worldjam.core.BeatClock;
import worldjam.gui.VisualMetronome;
/**
 * Mimics a conductor's baton.
 * @author spaul
 *
 */
public abstract class Conductor extends VisualMetronome{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8162896956489786921L;
	public Conductor(BeatClock clock) {
		super(clock);
	}

	
	public void paint(Graphics g){
		if(prev != null)
			g.drawImage(prev, 0, 0, null);
		double t = ((System.currentTimeMillis() - clock.startTime)%(clock.msPerBeat*clock.beatsPerMeasure))/(double)clock.msPerBeat;


		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		double x1 =  x(t);//interpolate(x[i],x[(i+1)%x.length], Math.pow(a, 1.5));
		double y1 =  y(t);//1-interpolate(y[i],y[(i+1)%y.length], Math.pow(a, 3));

		Graphics2D g2 = (Graphics2D)img.createGraphics();
		g2.setStroke(new BasicStroke(3));
		g2.drawLine(
				(int)(getWidth()*(.1+.8*x1)), 
				(int)(getHeight()*(.1+.8*y1)), 
				(int)(getWidth()*(.1+.4*x1)), 
				(int)(getHeight()*(.25+.4*y1))
				);
		g.drawImage(img, 0, 0, null);
		prev = img;
	}

	BufferedImage prev = null;
	
	/**
	 * 
	 * @param t time since the begining of the measure, in beats.
	 * @return x coordinate of the end of the baton, between 0 and 1
	 */
	protected abstract double x(double t);
	/**
	 * 
	 * @param t time since the begining of the measure, in beats.
	 * @return x coordinate of the end of the baton, between 0 and 1
	 */
	protected abstract double y(double t);

}
