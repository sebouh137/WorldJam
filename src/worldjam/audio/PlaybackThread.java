package worldjam.audio;

import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import worldjam.time.ClockSetting;
import worldjam.time.DelayChangeListener;
import worldjam.time.DelaySetting;
import worldjam.util.DigitalAnalogConverter;

public class PlaybackThread extends Thread implements PlaybackChannel, DelayChangeListener{
	AudioTrackRecorder recorder;
	private boolean convertToStereo = true;
	private SourceDataLine sdl;
	/**
	 * The PlaybackThread uses the clock information in order to wait until the beginning of a measure to start,
	 * and so that the delay time can be set as an integer number of beats or measures in the 
	 * {@link #setReplayOffsetInBeats} and {@link #setReplayOffsetInMeasures} methods
	 */
	private ClockSetting clock;
	private AudioFormat playbackFormat;
	private AudioFormat inputFormat;
	private Mixer mixer;
	private String sourceName;
	private long senderID;
	public PlaybackThread(Mixer mixer, AudioFormat inputFormat, ClockSetting clock, String sourceName, long senderID) throws LineUnavailableException{
		this.sourceName = sourceName;
		this.senderID = senderID;
		this.inputFormat = inputFormat;
		System.out.println("input format: " + inputFormat);
		if(convertToStereo){
			playbackFormat = new AudioFormat(inputFormat.getEncoding()
					, inputFormat.getSampleRate(), 
					inputFormat.getSampleSizeInBits(),
					2, 
					inputFormat.getFrameSize()*2, 
					inputFormat.getFrameRate(), 
					inputFormat.isBigEndian());
		} else
			playbackFormat = inputFormat;
		

		System.out.println("playback format: " + playbackFormat);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, playbackFormat);
		this.mixer = mixer;
		sdl = (SourceDataLine)mixer.getLine(info);

		this.clock = clock;
		setReplayOffset(1, 0, 0);
		//a buffer of 10 measures long is overkill
		int nMeasuresInBuffer = 5;
		int bufferSize = (int)(playbackFormat.getFrameSize()*
				playbackFormat.getFrameRate()*
				clock.msPerBeat/1000.*
				clock.beatsPerMeasure*
				nMeasuresInBuffer);
		buffer = new byte[bufferSize];
		this.start();
	}
	public String getSourceName() {
		return sourceName;
	}


	public long getSenderID() {
		return senderID;
	}
	private byte[] buffer;
	private int bufferPosition;


	private int offsetMeasures; private int offsetBeats; private int offset_ms, total_offset_ms;

	public void setReplayOffset(int nMeasures, int nBeats, int n_ms){
		this.offsetMeasures = nMeasures;
		this.offsetBeats = nBeats;
		this.offset_ms = n_ms;
		this.total_offset_ms = (nMeasures*clock.beatsPerMeasure+nBeats)*clock.msPerBeat+n_ms;
		replayOffsetInBytes = (int) (
				total_offset_ms*playbackFormat.getFrameRate()/1000.)*playbackFormat.getFrameSize();
	}

	/*public void setReplayOffsetInMeasures(int nMeasures){
		replayOffsetInBytes = (int) (nMeasures*clock.beatsPerMeasure*clock.msPerBeat*format.getFrameRate()/1000.)*format.getFrameSize();
	}
	public void setReplayOffsetInBeats(int nBeats){
		replayOffsetInBytes = (int) (nBeats*clock.msPerBeat*format.getFrameRate()/1000.)*format.getFrameSize();
	}
	public void setReplayOffsetInBytes(int offset){
		this.replayOffsetInBytes = offset;
	}*/
	private int replayOffsetInBytes;
	public void sampleReceived(AudioSample sample) {
		int dt = (int) (sample.sampleStartTime-loopStartTime);
		int destPos = (int) (dt*playbackFormat.getFrameRate()/1000.)*playbackFormat.getFrameSize();
		destPos += replayOffsetInBytes;
		destPos %= buffer.length;
		if(destPos < 0)
			destPos += buffer.length;
		if(filter != null){
			try{
				sample.sampleData = filter.process(sample.sampleData, inputFormat);
			} catch (Exception e){
				e.printStackTrace();
			}
		} 
		if(recorder != null)
			try{
				recorder.sampleReceived(sample);
			} catch (Exception e){
				e.printStackTrace();
			}
		
		if(convertToStereo){
			sample.sampleData = stereoConvert(sample.sampleData);
		}
		AudioUtils.arrayCopyWrapped(sample.sampleData, 0, buffer, destPos, sample.sampleData.length);
	}


	private byte[] stereoConvert(byte[] mono) {
		byte[] stereo = new byte[mono.length*2];
		int ifs = inputFormat.getFrameSize();
		for(int i = 0; i<mono.length/ifs; i++){
			System.arraycopy(mono, ifs*i, stereo, ifs*2*i, ifs);
			System.arraycopy(mono, ifs*i, stereo, ifs*(2*i+1), ifs);
		}
		return stereo;
	}
	
	//status flags
	private boolean alive = true;

	public void close(){
		alive = false;
		sdl.stop();
		sdl.close();
	}

	private long loopStartTime;
	public void run(){
		int N_BYTES_PLAYED_AT_ONCE = (int) (playbackFormat.getFrameRate()*playbackFormat.getFrameSize());
		
		/*
		 * For convenience, start at the beginning of a measure
		 */
		try {
			sdl.open();
			int msPerMeasure = clock.getMsPerMeasure();
			long prestart = System.currentTimeMillis();
			int sleepTime = (int) (prestart-clock.startTime);
			sleepTime %= msPerMeasure;
			if(sleepTime<0) // account for a weird feature of modding negative numbers in jova
				sleepTime += msPerMeasure;
			Thread.sleep(sleepTime);
			loopStartTime = prestart+sleepTime;
			
			sdl.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(alive){

			if(bufferPosition + N_BYTES_PLAYED_AT_ONCE < buffer.length){
				sdl.write(buffer, bufferPosition, N_BYTES_PLAYED_AT_ONCE);
				bufferPosition+= N_BYTES_PLAYED_AT_ONCE;
			}
			else {
				sdl.write(buffer, bufferPosition, buffer.length - bufferPosition);
				sdl.write(buffer, 0, N_BYTES_PLAYED_AT_ONCE - (buffer.length - bufferPosition));
				bufferPosition = N_BYTES_PLAYED_AT_ONCE - (buffer.length - bufferPosition);
			}


		}
	}

	public Line getLine(){
		return sdl;
	}

	public void setFilter(AudioFilter filter){
		this.filter = filter;
	}

	private AudioFilter filter;
	public AudioFormat getPlaybackFormat() {
		return playbackFormat;
	}
	public AudioFormat getInputFormat(){
		return inputFormat;
	}

	public void changeClockSettingsNow(ClockSetting clock) {
		this.clock = clock;
		this.setReplayOffset(offsetMeasures, offsetBeats, offset_ms);
	}

	public double getRMS(double windowInMS){
		long t = System.currentTimeMillis()-loopStartTime;
		int offsetInBytes = (((int) (t*playbackFormat.getFrameRate()/1000.))*playbackFormat.getFrameSize())%buffer.length;

		int nSamples = (int)(windowInMS/1000.*playbackFormat.getSampleRate());
		double sumSqr = 0;
		DigitalAnalogConverter dac = new DigitalAnalogConverter(playbackFormat);
		for(int i = 0; i<nSamples; i++){
			int index = offsetInBytes/(playbackFormat.getSampleSizeInBits()/8)-i;
			if(index <0)
				index+= buffer.length/(playbackFormat.getSampleSizeInBits()/8);
			double x = dac.getConvertedSample(buffer, index);
			double sqr = Math.pow(x, 2);
			sumSqr += sqr;
		}
		return Math.sqrt(sumSqr)/nSamples;

	}

	public int getAddDelayMeasures(){
		return offsetMeasures;
	}
	
	public int getAddDelayBeats(){
		return offsetBeats;
	}
	
	public int getAddDelayMS(){
		return offset_ms;
	}
	
	public int getTotalDelayInMS() {
		return total_offset_ms;
	}

	public ClockSetting getClock() {
		return clock;
	}

	public Mixer getMixer() {
		return mixer;
	}

	@Override
	public AudioFilter getFilter() {
		return filter;
	}


	@Override
	public void startRecording(OutputStream output, long startTime) {
		this.recorder = new AudioTrackRecorder(output, this.inputFormat, startTime);
		
	}


	@Override
	public void stopRecording(long timestamp) {
		this.recorder.stopRecording(timestamp);
	}
	@Override
	public void setMuted(boolean muted) {
		BooleanControl muteControl = (BooleanControl) this.getLine().getControl(BooleanControl.Type.MUTE);
		if(muteControl != null){
			muteControl.setValue(muted);
		}
	}

	@Override
	public boolean canBeMuted(){
		return (BooleanControl) this.getLine().getControl(BooleanControl.Type.MUTE) != null;
	}
	@Override
	public boolean isMuted() {
		BooleanControl muteControl = (BooleanControl) this.getLine().getControl(BooleanControl.Type.MUTE);
		if (muteControl != null){
			return muteControl.getValue();
		}
		return false;
	}
	@Override
	public void changeDelaySetting(DelaySetting newDelaySetting) {
		//I am just going to use existing methods here.  
		this.offsetMeasures = newDelaySetting.getMeasuresDelay();
		this.offsetBeats = 0;
		this.offset_ms = newDelaySetting.getAdditionalDelayAudio()+newDelaySetting.getAdditionalDelayGlobal();
		this.setReplayOffset(offsetMeasures, 0, 
				offset_ms);
	}
	
}
