package worldjam.audio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

//import worldjam.test.TestInputThread;
import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.util.ConfigurationsXML;
import worldjam.util.DigitalAnalogConverter;

public class InputThread extends Thread implements HasAudioLevelStats, ClockSubscriber{
	static Random random = new Random();
	private long lineID = random.nextLong();
	private TargetDataLine tdl;
	
	private int nBytesPerLoop;
	private byte[] buffer;
	private ArrayList<AudioSubscriber> subscribers = new ArrayList();
	private AudioFormat format;
	private int requestedBufferSize =  AudioSystem.NOT_SPECIFIED;
	public InputThread(Mixer mixer, AudioFormat format, ClockSetting clock) throws LineUnavailableException {
		this(mixer, format, clock, 200);
		//this.addSubscriber(new TestInputThread.MySubscriber(this));
	}
	
	public InputThread(final Mixer mixer, AudioFormat format, ClockSetting clock, int nMsPerLoop) throws LineUnavailableException{
		this.setName("input");
		this.format = format;
		this.mixer = mixer;
		this.clock = clock;
		this.nMsPerLoop = nMsPerLoop;
		this.timeCalibration = ConfigurationsXML.getInputTimeCalib(mixer.getMixerInfo().getName());
		
		Line.Info info = new DataLine.Info(TargetDataLine.class, format,requestedBufferSize);
		tdl = (TargetDataLine)mixer.getLine(info);
		nBytesPerLoop = format.getFrameSize()*(int)(format.getFrameRate()*nMsPerLoop/1000.);
		buffer = new byte[nBytesPerLoop];
		buffer2 = new byte[nBytesPerLoop];
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				alive = false;
				tdl.close();
				mixer.close();
			}
		});
		this.start();
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
	 * @param calib The calibration of the timestamp in ms.  This value is added to the timestamp of every recorded sample. 
	 */
	public void setTimeCalibration(int calib) {
		this.timeCalibration = calib;
	}
	
	public int getTimeCalibration() {
		return timeCalibration;
	}
	
	public void run(){
		try {
			tdl.open(format,requestedBufferSize);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		tdl.start();
		timestamp = System.currentTimeMillis();
		//time = (time*format.getFrameSize())/format.getFrameSize();
		while(alive){
			if(newBufferLength > 0) {
				buffer = new byte[newBufferLength];
				buffer2 = new byte[newBufferLength];
				newBufferLength = 0;
			}
			int readbytes = tdl.read(buffer, 0, buffer.length);
			if(muted) {
				Arrays.fill(buffer, (byte)0); // software muting of the line.   
			}
			AudioSample message = new AudioSample();
			message.sampleData = buffer.clone();
			message.sourceID = this.lineID;
			message.sampleStartTime = timestamp+timeCalibration;
			timestamp+= nMsPerLoop;
			
			System.arraycopy(buffer, 0, buffer2, 0, buffer.length);
			for(AudioSubscriber subscriber : subscribers)
				subscriber.sampleReceived(message);
			
		}
	}
	/**
	 * setting this to a non-zero number adjusts the buffer length in bytes just before the next read cycle
	 */
	private int newBufferLength = 0;
	/**
	 * Requests a change to the length of the buffer length; this will take place just before the next call to 
	 * {@link TargetDataLine.read}.  
	 * @param length the new length of the buffer in bytes
	 */
	public void setBufferLengthInBytes(int length) {
		this.newBufferLength = length;
	}
	/**
	 * return the current buffer length in bytes.  This is not always the same as the number provided to {@link setBufferLengthInBytes},
	 * since the change to the buffer length doesn't take place until just before the next invocation of  {@link TargetDataLine.read}.
	 * 
	 */
	public int getBufferLengthInBytes() {
		return buffer.length;
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
			return InputVolumeUtil.getInstance(mixer.getMixerInfo().getName());
		 
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
					Math.max(buffer2.length/sampleSizeInBytes-(nSamples-offsetInBytes),0), 
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

	@Override
	public int errorCode() {
		
		//dead thread
		if(!this.isAlive())
			return 1;

		//delayed reading the audio sample 
		int threshold = 300;
		if(System.currentTimeMillis()>timestamp+timeCalibration+nMsPerLoop+threshold)
			return 2;
		else
			return 0;
	}
}
