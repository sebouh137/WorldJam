package worldjam.gui.extras;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;

import worldjam.audio.AudioSample;
import worldjam.audio.InputThread;
import worldjam.util.DefaultObjects;

public class Tuner extends SpectrumVisualizer{
	
	@Override
	public void sampleReceived(AudioSample sample) {
		super.sampleReceived(sample);
		if(auto) {
			selectedNote = autoDetectNote();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -79441570662889396L;

	boolean auto = true;
	int selectedNote = 0;


	private Tuning tuning = new Equi12Tuning();

	public Tuner() {
		super(10, 36, 6);
	}
	
	public void paint(Graphics g1){
		Graphics2D g = (Graphics2D)g1;
		super.paint(g);
		int w = getWidth();
		int h = getHeight();
		double r1 = h/2.;
		double r2 = h*.9;
		double thetamax = 27*Math.PI/180.;

		int d = divisionsPerSemitone;

		//determine which note best matches the one being played,
		


		double weights[] = new double[12*d];
		for(int j = 0; j<fourierResults.length;  j++){
			weights[j%(12*d)] += fourierResults[j];
		}


		//draw lines to show the target pitch, and a grid of nearby pitches
		for(int i = 0; i<=10; i++) {
			if(i %5 ==0)
				g.setColor(Color.BLACK);
			else
				g.setColor(Color.LIGHT_GRAY);
			//int x = (int)(w*(i/10.+tuning.getAdjustment(selectedNote)/100.));
			//g.drawLine(x, 0, x, h/2);
			double theta = thetamax*(2*i/10.-1);
			
			g.drawLine(w/2+(int)(r1*Math.sin(theta)), h-(int)(r1*Math.cos(theta)), w/2+(int)(r2*Math.sin(theta)), h-(int)(r2*Math.cos(theta)));
		}

		//draw boxes to represent weights from the Fourier transform
		if(drawBoxes) {
			for(int i = 0; i<=d; i++) {
				int index = (i+d*selectedNote)%weights.length;
				double weight = weights[index];
				double lw = (Math.log10(weight+.00001)+5)/5.;
				g.fillRect(i*w/d - w/(2*d), h-(int)(h*lw), w/d+1, (int)(h*lw));
			}
		}

		//draw the name of the selected note
		g.setFont(FONT1);
		g.setColor(COLOR1);
		if(noteNames[selectedNote].length() == 1) {
			int offsetX = g.getFontMetrics().stringWidth(noteNames[selectedNote])/2;
			g.drawString(noteNames[selectedNote], w/2-offsetX, h*3/4);
		} else {
			//superscript the sharp and flat.  
			String note = noteNames[selectedNote];
			FontMetrics fm1 = g.getFontMetrics(FONT1);
			FontMetrics fm2 = g.getFontMetrics(FONT2);
			char[] chars = note.toCharArray();
			int w0 = fm1.charWidth(chars[0]);
			int w1 = fm2.charWidth(chars[1]);
			int w2 = fm1.charWidth(chars[2]);
			int w3 = fm1.charWidth(chars[3]);
			int w4 = fm2.charWidth(chars[4]);
			int wtot = w0+w1+w2+w3+w4;
			int voffs = fm1.getAscent()-fm2.getAscent();
			g.setFont(FONT1);
			g.drawChars(chars, 0, 1, w/2-wtot/2+w0/2, h*3/4);
			g.setFont(FONT2);
			g.drawChars(chars, 1, 1, w/2-wtot/2+w0+w1/2, h*3/4-voffs);
			g.setFont(FONT1);
			g.drawChars(chars, 2, 1, w/2-wtot/2+w0+w1+w2/2, h*3/4);
			g.setFont(FONT1);
			g.drawChars(chars, 3, 1, w/2-wtot/2+w0+w1+w2+w3/2, h*3/4);
			g.setFont(FONT2);
			g.drawChars(chars, 4, 1, w/2-wtot/2+w0+w1+w2+w3+w4/2, h*3/4-voffs);
		}
		


		//determine the actual pitch by averaging the weights from the Fourier transform
		double sumW = 0;
		double sumWi = 0;
		for(int i = -d/2; i<=d+d/2; i++) {
			int index = ((i+d*selectedNote)+weights.length)%weights.length;
			double weight = weights[index];
			sumW += weight;
			sumWi += weight*i;
		}
		double avg = sumWi/(sumW+1e-15);
		g.setStroke(STROKE1);
		g.setColor(Color.red);
		//g.drawLine((int)((avg*w)/d), h/2, (int)((avg*w)/d), 0);
		double theta = thetamax*(2*(avg/d-tuning.getAdjustment(selectedNote)/100.)-1);
		g.drawLine(w/2+(int)(r1*Math.sin(theta)), h-(int)(r1*Math.cos(theta)), w/2+(int)(r2*Math.sin(theta)), h-(int)(r2*Math.cos(theta)));
	
	}
	Font FONT1 = new Font(Font.SANS_SERIF, Font.BOLD, 30);
	Font FONT2 = new Font(Font.SANS_SERIF, Font.BOLD, 15);
	Color COLOR1 = new Color(0,128,255);
	private Stroke STROKE1 = new BasicStroke(3);

	public int autoDetectNote() {
		int d = divisionsPerSemitone;
		double weights[] = new double[12];
		for(int j = 0; j<fourierResults.length;  j++){
			weights[(j/d)%12] += fourierResults[j];
		}
		int bestMatch = 0;
		double maxWeight = 0;
		for(int i = 0; i<weights.length; i++) {
			if(weights[i]>maxWeight) {
				maxWeight = weights[i];
				bestMatch = i;
			}
		}
		return bestMatch;
	}
	boolean drawBoxes = false;
	public static void main(String arg[]) throws LineUnavailableException {
		JFrame frame = new JFrame();
		SpectrumVisualizer sv = new Tuner();
		frame.add(sv);
		frame.setSize(500,500);
		frame.setVisible(true);
		InputThread it = new InputThread(DefaultObjects.getInputMixer(), DefaultObjects.defaultFormat, null, 100);
		it.addSubscriber(sv);
		it.start();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
