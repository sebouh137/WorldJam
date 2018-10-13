package worldjam.test.generators;

import javax.sound.sampled.AudioFormat;

import worldjam.audio.PlaybackThread;
import worldjam.audio.AudioSample;
import worldjam.audio.AudioSubscriber;
import worldjam.core.BeatClock;

public class DroneThread extends LoopThread {
	private double freq;
	private double amp;
	/*private AudioFormat format;
	private BeatClock clock;
	private SampleReceiver rec;*/
	public DroneThread(BeatClock clock, AudioFormat format, double freq, double amp){
		super(clock, format); 
		this.freq = freq;
		this.amp = amp;
	}
	
	public void generateLoop(){
		Generator g = new Generator(format); 
		msPerLoop = 1000;
		loop = g.sine(amp, freq, msPerLoop);
		
	}
	
	
}
