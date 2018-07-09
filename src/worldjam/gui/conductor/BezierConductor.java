package worldjam.gui.conductor;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import worldjam.core.BeatClock;
import worldjam.gui.VisualMetronome;
import worldjam.util.DefaultObjects;
public class BezierConductor extends VisualMetronome{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3856681830782446266L;
	private ConductingPattern pattern;

	

	public BezierConductor(BeatClock clock, ConductingPattern pattern) {
		super(clock);
		this.pattern = pattern;
		segments = pattern.getSegments();
		
	}

	public Object getPattern() {
		return pattern;
	}
	protected List<Segment> segments;
	

	public BezierConductor(BeatClock clock) {
		super(clock);
	}

	
	public void paint(Graphics g){
		double t = ((System.currentTimeMillis() - clock.startTime)
				%(clock.msPerBeat*clock.beatsPerMeasure))
				/(double)clock.msPerBeat;

		double u = t%1;
		Segment segment = segments.get((int)t);
		double x1 =  segment.interpolateX(u);
		double y1 =  segment.interpolateY(u);

		Graphics2D g2 = (Graphics2D)g;
		
		g2.setStroke(stroke);
		
		g.drawLine(
				(int)(getWidth()*(.1+.8*x1)), 
				(int)(getHeight()*(.1+.8*y1)), 
				(int)(getWidth()*(.1+.4*x1)), 
				(int)(getHeight()*(.25+.4*y1))
				);

	}
	
	public static void main(String arg[]){
		JFrame frame = new JFrame();
		frame.setSize(300, 300);
		
		Path2D path = new Path2D.Double();
		path.moveTo(.5, 0);
		path.curveTo(.5, 0, .5, 1, .5, 1);
		path.curveTo(.5, .8, .1, .5, 0, .5);
		path.curveTo(.3, .7, .7, .3, 1, .5);
		path.curveTo(.8,.5, .5, .1, .5, 0);
		
		
		BezierConductor tb = new BezierConductor(DefaultObjects.bc0, 
				new ConductingPattern(BezierUtil.generateSegments(path)));
		frame.add(tb);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public void setPattern(ConductingPattern pattern) {
		this.pattern = pattern;
		this.segments = pattern.getSegments();
	}
	private Stroke stroke = new BasicStroke(3);
	public void setStroke(Stroke stroke){
		this.stroke = stroke;
	}
}
