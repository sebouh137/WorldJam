package worldjam.gui.extras;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;
import javax.swing.JPanel;

import worldjam.audio.AudioSample;
import worldjam.audio.AudioSubscriber;
import worldjam.audio.InputThread;
import worldjam.util.DefaultObjects;
import worldjam.util.DigitalAnalogConverter;
import worldjam.util.ShortTimeFourierTransformer;

public class SpectrumViewer extends JPanel implements AudioSubscriber{

	private int firstNote;
	private ShortTimeFourierTransformer fourier;
	/**
	 * Create the panel.
	 * @param divisionsPerSemitone divisions per semitone
	 * @param firstNote 69 = A440; calculate from there
	 * @param nOctaves number of octaves
	 */
	public SpectrumViewer(int divisionsPerSemitone, int firstNote, int nOctaves) {
		this.divisionsPerSemitone = divisionsPerSemitone;
		this.firstNote = firstNote;
		this.nOctaves = nOctaves;
		fourierResults = new double[12*nOctaves*divisionsPerSemitone];
		double fmin = 440*Math.pow(2, (firstNote-69-0.5)/12.); 
		//start at -50 cents below the lowest note, to get then bin centered at the lowest note
		double fmax = fmin*Math.pow(2,nOctaves);
		this.fourier = new ShortTimeFourierTransformer(1,
				1/format.getFrameRate(), 
				fmin, fmax, nOctaves*12*divisionsPerSemitone, true);
	}
	AudioFormat format = DefaultObjects.defaultFormat;

	public SpectrumViewer(){
		this(10, 36, 6);
	}

	//String noteNames[] = "C G D A E B F#/Gb C#/Db G#/Ab D#/Eb A#/Bb F".split(" ");
	protected String noteNames[] = "C C#/Db D D#/Eb E F F#/Gb G G#/Ab A A#/Bb B".split(" ");
	protected int divisionsPerSemitone;
	protected int nOctaves;
	protected double [] fourierResults;
	
	Font font1 = new Font(Font.SERIF, Font.PLAIN, 35);
	Font font2 = new Font(Font.SERIF, Font.PLAIN, 18);
	//Canvas mainCanvas = new Canvas(){
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
	//};



	DigitalAnalogConverter dac;
	@Override
	public void sampleReceived(AudioSample sample) {
		if(dac == null)
			dac = new DigitalAnalogConverter(format);
		int N = sample.sampleData.length/(format.getSampleSizeInBits()/8);
		for (int i = 0; i< N; i++){
			double val = dac.getConvertedSample(sample.sampleData, i);
			fourier.nextSample(val);
		}
		refreshFourierResults();
	}
	void refreshFourierResults(){
		for (int i = 0; i<fourierResults.length; i++){
			fourierResults[i] = Math.hypot(fourier.Bi[i],fourier.Br[i]);
		}
		repaint();
	}
	public static void main(String arg[]) throws LineUnavailableException {
		JFrame frame = new JFrame();
		SpectrumViewer kd = new SpectrumViewer();
		frame.add(kd);
		frame.setSize(500,500);
		frame.setVisible(true);
		InputThread it = new InputThread(DefaultObjects.getInputMixer(), DefaultObjects.defaultFormat, null, 100);
		it.addSubscriber(kd);
		it.start();
	}

}
