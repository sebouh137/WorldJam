package worldjam.test.generators;

import javax.sound.sampled.AudioFormat;

import worldjam.audio.AudioUtils;
import worldjam.audio.PlaybackThread;
import worldjam.audio.AudioSample;
import worldjam.audio.AudioSubscriber;
import worldjam.core.BeatClock;

public class MetronomeThread extends LoopThread{
	private double freq;
	private double amp;
	public MetronomeThread(BeatClock clock, AudioFormat format, double freq, double amp){
		super(clock, format);
		this.freq = freq;
		this.amp = amp;
	}
	
	
	public void generateLoop(){
		Generator g = new Generator(format);
		byte[] sinBytesHigh = g.sine(amp*1.5, freq*1.5, 500).clone();
		byte[] sinBytes = g.sine(amp, freq, 500).clone();
		
		msPerLoop = clock.getMsPerMeasure();
		loop = AudioUtils.getByteArray(msPerLoop, format);
		for(int i = 0; i<clock.beatsPerMeasure; i++){
			int destPos = (loop.length*i)/clock.beatsPerMeasure;
			//ensure an integer number of frames
			destPos = (destPos*format.getFrameSize())/format.getFrameSize();
			System.arraycopy(i == 0 ? sinBytesHigh : sinBytes, 0, loop, destPos, AudioUtils.nBytes(250, format));
			System.out.println(i);
		}
	}
	
	
	
	
}
