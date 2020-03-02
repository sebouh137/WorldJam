package worldjam.gui.extras;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;

import worldjam.audio.InputThread;
import worldjam.util.DefaultObjects;

public class Tuner extends SpectrumVisualizer{

	/**
	 * 
	 */
	private static final long serialVersionUID = -79441570662889396L;

	boolean auto = true;
	int selectedNote = 0;


	private Tuning tuning = new Equi12Tuning();


	public void paint(Graphics g1){
		Graphics2D g = (Graphics2D)g1;
		super.paint(g);
		int w = getWidth();
		int h = getHeight();


		int d = divisionsPerSemitone;

		//determine which note best matches the one being played,
		if(auto) {
			selectedNote = autoDetectNote();
		}


		double weights[] = new double[12*d];
		for(int j = 0; j<fourierResults.length;  j++){
			weights[j%(12*d)] += fourierResults[j];
		}


		//draw lines to show the target pitch, and a grid of nearby pitches
		for(int i = 0; i<=10; i++) {
			if(i == 5)
				g.setColor(Color.BLACK);
			else
				g.setColor(Color.LIGHT_GRAY);
			int x = (int)(w*(i/10.+tuning.getAdjustment(selectedNote)/100.));
			g.drawLine(x, 0, x, h/2);
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
		int offsetX = g.getFontMetrics().stringWidth(noteNames[selectedNote])/2;
		g.drawString(noteNames[selectedNote], w/2-offsetX, h*3/4);

		


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
		g.drawLine((int)((avg*w)/d), h/2, (int)((avg*w)/d), 0);

	}
	Font FONT1 = new Font(Font.SANS_SERIF, Font.BOLD, 30);
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
