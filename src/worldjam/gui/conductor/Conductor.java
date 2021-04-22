package worldjam.gui.conductor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;


import worldjam.exe.Client;
import worldjam.gui.VisualMetronome;
import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
public class Conductor extends VisualMetronome implements ClockSubscriber{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3856681830782446266L;
	private ConductingPattern pattern;

	/**
	 * Use a visual cue to let the user know that the session is in 
	 * convo mode (low latency).   
	 * @param val
	 */
	public void setConvoMode(boolean val) {
		this.convoMode = val;
	}
	private boolean convoMode;

	public Conductor(ClockSetting clock, ConductingPattern pattern) {
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
	private Font infoFont = new Font(Font.MONOSPACED, Font.PLAIN, 16);

	private Font convoModeFont = new Font(Font.MONOSPACED, Font.PLAIN, 32);

	public Conductor(ClockSetting clock) {
		this(clock, DefaultConductingPatternProvider.getInstance().getDefaultPattern(clock != null ? clock.beatsPerMeasure : 4));
		//this.setBackground(Color.BLACK);
		this.setOpaque(false);
	}
	
	private boolean drawShadow;
	public void setDrawShadow(boolean b) {
		this.drawShadow = b;
	}
	
	@Override
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
		if(convoMode) {
			g2.setStroke(stroke_convoMode);
		} else {
			g2.setStroke(stroke);
		}
		if(drawShadow) {
			g2.setColor(Color.black);
			g2.drawLine(
					(int)(getWidth()*(.1+.8*x)+1), 
					(int)(getHeight()*(.1+.8*y)+1), 
					(int)(getWidth()*(.1+.4*x)+1), 
					(int)(getHeight()*(.25+.4*y)+1)
					);
		}
		if(convoMode) {
			g2.setColor(battonColor_convoMode);
		}
		else {
			g2.setColor(battonColor);
		}
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
		if(showInfoAtBottom) {
			g2.setFont(infoFont );
			g2.drawString(String.format("%d/%d", clock.beatsPerMeasure,clock.beatDenominator), 10, getHeight()-10);
			String bpmStr = String.format("%.1f BPM",clock.getBPM());
			g2.drawString(bpmStr, 
					getWidth()/2-g2.getFontMetrics().stringWidth(bpmStr)/2, getHeight()-10);
			String mspbStr = String.format("(%d ms/beat)",clock.msPerBeat);
			g2.drawString(mspbStr, 
					getWidth()-10-g2.getFontMetrics().stringWidth(mspbStr), getHeight()-10);
		
		}
		
		//display visual cue corresponding to convo mode
		if(convoMode) {
			g2.setFont(convoModeFont);
			String str ="convo mode enabled.";
			g2.drawString(str, 
					getWidth()/2-g2.getFontMetrics().stringWidth(str)/2, getHeight()/2);
			str = String.format("(latency = %d ms)", Client.getConvoModeLatency());
			g2.drawString(str, 
					getWidth()/2-g2.getFontMetrics().stringWidth(str)/2, getHeight()*3/5);
			
		}
		
	}
	
	
	public static void main(String arg[]) {
		//new ConductorStandaloneWindow();
	}
	
	public void setPattern(ConductingPattern pattern) {
		this.pattern = pattern;
		this.segments = pattern.getSegments();
	}
	private Stroke stroke = new BasicStroke(3);
	private Stroke stroke_convoMode = new BasicStroke(3.f, BasicStroke.CAP_SQUARE,BasicStroke.JOIN_BEVEL, 10.f,new float[]{5,5},0);
			
	private boolean showMeasureNumber;
	private Color battonColor = Color.BLACK;
	private Color battonColor_convoMode = new Color(64,64,255); // a slightly lighter blue than primary
	public void setStroke(Stroke stroke){
		this.stroke = stroke;
	}

	public void setMeasureNumberVisible(boolean b) {
		this.showMeasureNumber=false;
	}

	public void setBattonColor(Color color){
		this.battonColor  = color;
	}
	private boolean showInfoAtBottom = false;
	public void setShowInfoAtBottom(boolean val) {
		this.showInfoAtBottom = val;
	}
	
}
