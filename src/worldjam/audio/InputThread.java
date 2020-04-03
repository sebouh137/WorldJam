package worldjam.audio;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.util.Configurations;
import worldjam.util.DigitalAnalogConverter;

public class InputThread extends Thread implements HasAudioLevelStats, ClockSubscriber{
	static Random random = new Random();
	private long lineID = random.nextLong();
	private TargetDataLine tdl;
	
	private int nBytesPerLoop;
	private byte[] buffer;
	private ArrayList<AudioSubscriber> subscribers = new ArrayList();
	private AudioFormat format;
	public InputThread(Mixer mixer, AudioFormat format, ClockSetting clock) throws LineUnavailableException {
		this(mixer, format, clock, 500);
	}
	
	public InputThread(Mixer mixer, AudioFormat format, ClockSetting clock, int nMsPerLoop) throws LineUnavailableException{
		this.format = format;
		this.mixer = mixer;
		this.clock = clock;
		this.nMsPerLoop = nMsPerLoop;
		this.timeCalibration = Configurations.getDefaultTimingCalibration(
				Configurations.AUDIO_INPUT, mixer.getMixerInfo().getName());
		
		Line.Info info = new DataLine.Info(TargetDataLine.class, format);
		tdl = (TargetDataLine)mixer.getLine(info);
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
	private int timeCalibration;
	
	/**
	 * Sets the calibration of the timestamp.  
	 * @param calib The calibration of the timestamp in ms.  This value is subtracted from the timestamp of every recorded sample. 
	 */
	public void setTimeCalibration(int calib) {
		this.timeCalibration = calib;
	}
	
	public int getTimeCalibration() {
		return timeCalibration;
	}
	
	public void run(){
		try {
			tdl.open();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		tdl.start();
		timestamp = System.currentTimeMillis();
		//time = (time*format.getFrameSize())/format.getFrameSize();
		while(alive){
			tdl.read(buffer, 0, buffer.length);
			if(muted) {
				Arrays.fill(buffer, (byte)0); // software muting of the line.   
			}
			AudioSample message = new AudioSample();
			message.sampleData = buffer.clone();
			message.sourceID = this.lineID;
			message.sampleStartTime = timestamp-timeCalibration;
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
	
	DigitalAnalogConverter dac;
	public double getRMS(double windowInMS){
		if(dac == null)
			dac = new DigitalAnalogConverter(format);
		long t = System.currentTimeMillis()-(int)(windowInMS/10)-timestamp;
		int offsetInBytes = (((int) (t*format.getFrameRate()/1000.))*format.getFrameSize())%buffer.length;

		int nSamples = (int)(windowInMS/1000.*format.getSampleRate());
		double sumSqr = 0;
		double max = 0;
		int posOfMax = 0;
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
	public void removeSubscriber(AudioSubscriber sub) {
		this.subscribers.remove(sub);
	}
	
	public FloatControl inputVolumeControl() {
		if (tdl.isControlSupported(FloatControl.Type.MASTER_GAIN))
			return (FloatControl) tdl.getControl(FloatControl.Type.MASTER_GAIN);
		else if(tdl.isControlSupported(FloatControl.Type.VOLUME))
			return (FloatControl) tdl.getControl(FloatControl.Type.VOLUME);
		else 
			return InputVolumeUtil.getInstance();
		 
	}

	public boolean isMuted() {
		return muted;
	}
    boolean muted;
    float prevVol = 0;
	public void setMuted(boolean b) {
		this.muted=b;
	}

	@Override
	public double getPeakAmp(double windowInMS) {
		if(dac == null)
			dac = new DigitalAnalogConverter(format);
		long t = System.currentTimeMillis()-(int)(windowInMS/10)-timestamp;
		int offsetInBytes = (((int) (t*format.getFrameRate()/1000.))*format.getFrameSize())%buffer.length;

		int nSamples = (int)(windowInMS/1000.*format.getSampleRate());

		int sampleSizeInBytes = format.getSampleSizeInBits()/8;
		if(offsetInBytes/sampleSizeInBytes > nSamples)
			return dac.getPeak(buffer2, offsetInBytes/sampleSizeInBytes-nSamples, offsetInBytes/sampleSizeInBytes-3);
		else { //for wrap around
			double max1 = dac.getPeak(buffer2, 0, offsetInBytes/sampleSizeInBytes);
			double max2 = dac.getPeak(buffer2, 
					buffer2.length/sampleSizeInBytes-(nSamples-offsetInBytes), 
					buffer2.length/sampleSizeInBytes-1);
			return Math.max(max1, max2);
		}
		/*int sampleSizeBytes = format.getSampleSizeInBits()/8;
		for(int i = 3; i<nSamples; i++){
			int index = offsetInBytes/sampleSizeBytes-i;
			if(index <0)
				index+= buffer2.length/(sampleSizeBytes);
			double x = Math.abs(dac.getConvertedSample(buffer2, index));
			
			if(x>max){
				max = x;
			}
			
		}*/
		//System.out.println(max + " " + posOfMax);
		//return max;
	}
}
