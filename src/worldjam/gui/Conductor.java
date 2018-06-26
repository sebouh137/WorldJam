package worldjam.gui;

import java.awt.Graphics;

import worldjam.core.BeatClock;
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
		double t = ((System.currentTimeMillis() - clock.startTime)%(clock.msPerBeat*clock.beatsPerMeasure))/(double)clock.msPerBeat;


		//g.fillOval(x(t), y(t), 20, 20);

		//double a = t%1;
		//int i = (int)(t-a);
		double x1 =  x(t);//interpolate(x[i],x[(i+1)%x.length], Math.pow(a, 1.5));
		double y1 =  y(t);//1-interpolate(y[i],y[(i+1)%y.length], Math.pow(a, 3));

		g.drawLine(
				(int)(getWidth()*(.1+.8*x1)), 
				(int)(getHeight()*(.1+.8*y1)), 
				(int)(getWidth()*(.1+.4*x1)), 
				(int)(getHeight()*(.25+.4*y1))
				);

	}

	
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
