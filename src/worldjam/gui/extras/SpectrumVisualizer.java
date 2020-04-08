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

public abstract class SpectrumVisualizer extends JPanel implements AudioSubscriber{

	private int firstNote;
	private ShortTimeFourierTransformer fourier;
	/**
	 * Create the panel.
	 * @param divisionsPerSemitone divisions per semitone
	 * @param firstNote 69 = A440; calculate from there
	 * @param nOctaves number of octaves
	 */
	public SpectrumVisualizer(int divisionsPerSemitone, int firstNote, int nOctaves) {
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
	public SpectrumVisualizer(){
		this(10, 36, 6);
	}
	
	//MUSIC SHARP SIGN
	//Unicode: U+266F, UTF-8: E2 99 AF
	//String noteNames[] = "C G D A E B F#/Gb C#/Db G#/Ab D#/Eb A#/Bb F".split(" ");
	protected String noteNames[] = "C C#/Db D D#/Eb E F F#/Gb G G#/Ab A A#/Bb B".replaceAll("b", "\u266d").replaceAll("#", "\u266f").split(" ");
	protected int divisionsPerSemitone;
	protected int nOctaves;
	protected double [] fourierResults;
	
	Font font1 = new Font(Font.SERIF, Font.PLAIN, 35);
	Font font2 = new Font(Font.SERIF, Font.PLAIN, 18);
	//Canvas mainCanvas = new Canvas(){
	
	//};



	DigitalAnalogConverter dac;
	@Override
	public void sampleReceived(AudioSample sample) {
		if(dac == null)
			dac = new DigitalAnalogConverter(format);
		byte [] sampleData = sample.sampleData;
		int N = sampleData.length/(format.getSampleSizeInBits()/8);
		for (int i = 0; i< N; i++){
			double val = dac.getConvertedSample(sampleData, i);
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
	

}
