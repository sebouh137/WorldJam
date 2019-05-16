package worldjam.gui.conductor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import worldjam.gui.VisualMetronome;
import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.util.DefaultObjects;
public class BezierConductor extends VisualMetronome implements ClockSubscriber{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3856681830782446266L;
	private ConductingPattern pattern;

	

	public BezierConductor(ClockSetting clock, ConductingPattern pattern) {
		super(clock);
		this.pattern = pattern;
		segments = pattern.getSegments();	
	}
	
	public void changeClockSettingsNow(ClockSetting clock){
		if(getClock() == null){
			super.changeClockSettingsNow(clock);
			return;
		}
		if(getClock().beatsPerMeasure < clock.beatsPerMeasure){
			setPattern(DefaultConductingPatternProvider.getInstance().getDefaultPattern(clock.beatsPerMeasure));
			super.changeClockSettingsNow(clock);
		} else if(getClock().beatsPerMeasure > clock.beatsPerMeasure){
			super.changeClockSettingsNow(clock);
			setPattern(DefaultConductingPatternProvider.getInstance().getDefaultPattern(clock.beatsPerMeasure));
		} else {
			super.changeClockSettingsNow(clock);
		}
	}

	public Object getPattern() {
		return pattern;
	}
	protected List<BezierSegment> segments;
	private Font measureNumFont = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
	

	public BezierConductor(ClockSetting clock) {
		this(clock, DefaultConductingPatternProvider.getInstance().getDefaultPattern(clock != null ? clock.beatsPerMeasure : 4));
		//this.setBackground(Color.BLACK);
	}
	
	

	
	public void paint(Graphics2D g2, long time){
		
		
		double t = ((time - clock.startTime)
				%(clock.msPerBeat*clock.beatsPerMeasure))
				/(double)clock.msPerBeat;

		double x=0,y=0;
		//BezierSegment segment = segments.get((int)t);
		for(BezierSegment s : segments){
			if (t > s.t1 && t <= s.t2){
				double u = (t-s.t1)/(s.t2-s.t1);
				x = s.interpolateX(u);
				y = s.interpolateY(u);
				break;
			}
		} 

		
		//Graphics2D g2 = (Graphics2D)g;
		

		//draw the baton
		g2.setStroke(stroke);
		g2.drawLine(
				(int)(getWidth()*(.1+.8*x)), 
				(int)(getHeight()*(.1+.8*y)), 
				(int)(getWidth()*(.1+.4*x)), 
				(int)(getHeight()*(.25+.4*y))
				);
		if(showMeasureNumber){
			g2.setFont(measureNumFont );
			g2.drawString(String.format("measure %d", clock.getCurrentMeasure()), 10, getHeight()-10);
		}
		this.paintExtras(g2);
		
	}
	void paintExtras(Graphics g){
		
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
	private boolean showMeasureNumber;
	public void setStroke(Stroke stroke){
		this.stroke = stroke;
	}

	public void setMeasureNumberVisible(boolean b) {
		this.showMeasureNumber=false;
	}

	
}
