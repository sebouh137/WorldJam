package worldjam.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.util.DigitalAnalogConverter;

public class InputThread extends Thread implements RMS, ClockSubscriber{
	static Random random = new Random();
	private long lineID = random.nextLong();
	private TargetDataLine tdl;
	
	private int nBytesPerLoop;
	private byte[] buffer;
	private ArrayList<AudioSubscriber> subscribers = new ArrayList();
	private AudioFormat format;
	
	public InputThread(Mixer mixer, AudioFormat format, ClockSetting clock) throws LineUnavailableException{
		this.format = format;
		this.mixer = mixer;
		this.clock = clock;
		Line.Info info = new DataLine.Info(TargetDataLine.class, format);
		tdl = (TargetDataLine)mixer.getLine(info);
		nMsPerLoop = 500;
		nBytesPerLoop = format.getFrameSize()*(int)(format.getFrameRate()*nMsPerLoop/1000.);
		buffer = new byte[nBytesPerLoop];
		buffer2 = new byte[nBytesPerLoop];
	}
	private double nMsPerLoop;
	//status flags
	private boolean alive = true, paused = false;
	private Mixer mixer;
	private ClockSetting clock;
	
	private long timestamp;
	public void run(){
		try {
			tdl.open();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tdl.start();
		timestamp = System.currentTimeMillis();
		//time = (time*format.getFrameSize())/format.getFrameSize();
		while(alive){
			tdl.read(buffer, 0, buffer.length);
			AudioSample message = new AudioSample();
			message.sampleData = buffer.clone();
			message.sourceID = this.lineID;
			message.sampleStartTime = timestamp;
			timestamp+= nMsPerLoop;
			System.arraycopy(buffer, 0, buffer2, 0, buffer.length);
			for(AudioSubscriber subscriber : subscribers)
				subscriber.sampleReceived(message);
			
		}
	}
	private byte buffer2[];
	public void addSubscriber(AudioSubscriber subs){
		this.subscribers.add(subs);
	}
	public long getSenderID() {
		return lineID;
	}
	public TargetDataLine getLine(){
		return tdl;
	}
	public Mixer getMixer() {
		return mixer;
	}
	public void changeClockSettingsNow(ClockSetting beatClock) {
		this.clock = beatClock;
	}
	public double getRMS(double windowInMS){
		long t = System.currentTimeMillis()-(int)(windowInMS/10)-timestamp;
		int offsetInBytes = (((int) (t*format.getFrameRate()/1000.))*format.getFrameSize())%buffer.length;

		int nSamples = (int)(windowInMS/1000.*format.getSampleRate());
		double sumSqr = 0;
		double max = 0;
		int posOfMax = 0;
		DigitalAnalogConverter dac = new DigitalAnalogConverter(format);
		for(int i = 3; i<nSamples; i++){
			int index = offsetInBytes/(format.getSampleSizeInBits()/8)-i;
			if(index <0)
				index+= buffer2.length/(format.getSampleSizeInBits()/8);
			double x = dac.getConvertedSample(buffer2, index);
			double sqr = Math.pow(x, 2);
			sumSqr += sqr;
			
			if(Math.abs(x)>max){
				max = Math.abs(x);
				posOfMax = i;
			}
			
		}
		//System.out.println(max + " " + posOfMax);
		return Math.sqrt(sumSqr)/nSamples;

	}
	public void setSenderID(long lineID) {
		this.lineID = lineID;
		
	}
	public void close() {
		alive = false;
	}
}
