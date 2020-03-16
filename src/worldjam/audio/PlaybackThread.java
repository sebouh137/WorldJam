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
import worldjam.util.Configurations;
import worldjam.util.DigitalAnalogConverter;

public class PlaybackThread extends Thread implements PlaybackChannel, DelayChangeListener{
	AudioTrackRecorder recorder;
	private boolean convertToStereo = true;
	protected SourceDataLine sdl;
	/**
	 * The PlaybackThread uses the clock information in order to wait until the beginning of a measure to start,
	 * and so that the delay time can be set as an integer number of beats or measures in the 
	 * {@link #setReplayOffsetInBeats} and {@link #setReplayOffsetInMeasures} methods
	 */
	private ClockSetting clock;
	private AudioFormat playbackFormat;
	private AudioFormat inputFormat;
	private Mixer mixer;
	private String channelName;
	private long channelID;
	private PlaybackManager manager;
	
	public PlaybackThread(Mixer mixer, AudioFormat inputFormat, ClockSetting clock, String sourceName, long senderID, PlaybackManager manager) throws LineUnavailableException{
		this(mixer, inputFormat, clock, sourceName, senderID, manager, null);
	}
		
	public PlaybackThread(Mixer mixer, AudioFormat inputFormat, ClockSetting clock, String sourceName, long senderID, PlaybackManager manager, LoopBuilder loopBuilder) throws LineUnavailableException{
		this.manager = manager;
		this.channelName = sourceName;
		this.channelID = senderID;
		this.inputFormat = inputFormat;
		this.loopBuilder = loopBuilder;
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
		this.changeDelaySetting(DelaySetting.defaultDelaySetting);
		int nMeasuresInBuffer = 5;
		int bufferSize = (int)(playbackFormat.getFrameSize()*
				playbackFormat.getFrameRate()*
				clock.msPerBeat/1000.*
				clock.beatsPerMeasure*
				nMeasuresInBuffer);
		buffer = new byte[bufferSize];
		this.start();
	}
	public String getChannelName() {
		return channelName;
	}


	public long getChannelID() {
		return channelID;
	}
	private byte[] buffer;



	/*public void setReplayOffset(int nMeasures, int nBeats, int n_ms){
		this.offsetMeasures = nMeasures;
		this.offsetBeats = nBeats;
		this.offset_ms = n_ms;
		this.total_offset_ms = (nMeasures*clock.beatsPerMeasure+nBeats)*clock.msPerBeat+n_ms;
		replayOffsetInBytes = (int) (
				total_offset_ms*playbackFormat.getFrameRate()/1000.)*playbackFormat.getFrameSize();
	}*/

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

		byte data [] = sample.sampleData;
		if(recorder != null)
			try{
				recorder.sampleReceived(sample);
			} catch (Exception e){
				e.printStackTrace();
			}
		if(filter != null){
			try{
				data = filter.process(data, inputFormat);
			} catch (Exception e){
				e.printStackTrace();
			}
		} 

		if(convertToStereo){
			data = stereoConvert(data);
		}
		AudioUtils.arrayCopyWrapped(data, 0, buffer, destPos, data.length);
	}

	//recycled byte array
	private boolean recycleStereoByteArray = true;
	private byte stereoConverted[];
	
	protected byte[] stereoConvert(byte[] mono) {
		
		byte[] stereo;
		if(!recycleStereoByteArray) {
			stereo = new byte[mono.length*2];
		} else {
			if(stereoConverted == null || stereoConverted.length != mono.length*2) {
				stereo = new byte[mono.length*2];
				stereoConverted = stereo;
			} else {
				stereo = stereoConverted;
			}
		}
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
	private byte[] replacementBuffer;
	public void run(){
		//write such and such seconds at a time
		int defaultBytesPerCycle = (int) (playbackFormat.getFrameRate()*1)*playbackFormat.getFrameSize();
		//avoid writing less than a certain number of bytes in a cycle by extending the current cycle 
		//to the end of the buffer if necessary. 
		int minBytesPerCycle = (int) (playbackFormat.getFrameRate()*0.05)*playbackFormat.getFrameSize();


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
			loopStartTime = System.currentTimeMillis();
			sdl.start();
			long postStart = System.currentTimeMillis()-loopStartTime;
			System.out.println("sdl takes " + postStart + " ms to start");
			//System.out.println("blahhhh" + ((System.currentTimeMillis()-loopStartTime)*1000 - sdl.getMicrosecondPosition()));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		int bufferPositionNextCycle = 0;
		while(alive){
			// this should be thread-safe
			byte buff[] = this.buffer;
			int bufferPos = bufferPositionNextCycle;
			int len = buff.length;


			// determine how many bytes to play on this cycle, as well
			// as the buffer position for the next cycle. 
			int bytesToBePlayed = defaultBytesPerCycle;
			if(bufferPos + bytesToBePlayed > len || bufferPos + defaultBytesPerCycle + minBytesPerCycle > len) {
				bytesToBePlayed = len -bufferPos;
				bufferPositionNextCycle = 0;
				//if(loopBuilder != null) System.out.println("new cycle");
			} else if (bufferPos + bytesToBePlayed == len) {
				bufferPositionNextCycle = 0;
				//if(loopBuilder != null) System.out.println("new cycle");
			} else {
				bufferPositionNextCycle = bufferPos + bytesToBePlayed;
			}
			sdl.write(buff, bufferPos, bytesToBePlayed);

			if (rebuildLoopFlag) {
				this.buffer = replacementBuffer;
				long now = System.currentTimeMillis();
				//now += 500; // TODO Figure out a better solution than this. 
				int framePos = (int)(((now-clock.startTime)%clock.getMsPerMeasure())*playbackFormat.getFrameRate()/1000.);
				System.out.println("rebuilding loop\nframePos =" + framePos + "\nbufferLength=" + 
						buffer.length + "\nLoopDuration (s) =" + buffer.length/(playbackFormat.getFrameRate()*playbackFormat.getFrameSize()));
				bufferPositionNextCycle = framePos*playbackFormat.getFrameSize();
				replacementBuffer = null;
				rebuildLoopFlag = false;
			}
			if (resyncFlag) {
				long now = System.currentTimeMillis();
				int framePos = (int)(((now-this.loopStartTime)%clock.getMsPerMeasure())*playbackFormat.getFrameRate()/1000.);
				// TODO include code to resync the playback with System.currentTimeMillis()
				resyncFlag = true;
			}
		}
	}
	private boolean resyncFlag;
	private boolean rebuildLoopFlag;
	private LoopBuilder loopBuilder;

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
	
	private byte [] generatedLoop;

	public void changeClockSettingsNow(ClockSetting clock) {
		this.clock = clock;
		if(this.loopBuilder != null) {
			float[] floatBuffer =loopBuilder.createSamples(inputFormat.getFrameRate(), clock);
			byte[] byteBuffer = new byte[floatBuffer.length*inputFormat.getFrameSize()];
			new DigitalAnalogConverter(inputFormat).convert(floatBuffer,byteBuffer);
			this.generatedLoop = stereoConvert(byteBuffer);
			
		}
		
		this.validateDelays();
	}
	@Override
	public void validateDelays() {
		// Different mechanisms are used for delaying streamed versus looped channels.
		// For looped channels, you shift everything once, and replace the buffer contents.  
		// For streamed channels, you change the offset in the position where the newly received samples are written 
		// into the buffer.  
		if(loopBuilder != null && generatedLoop != null) {
			this.replacementBuffer = addDelayToLoop(generatedLoop, delaySetting);
			rebuildLoopFlag = true;
		} else {
			int total_offset_ms = (delaySetting.getMeasuresDelay()*clock.beatsPerMeasure)*clock.msPerBeat+
					delaySetting.getAdditionalDelayAudio()+manager.getTimeCalibration();
			replayOffsetInBytes = (int) (total_offset_ms*playbackFormat.getFrameRate()/1000.)*playbackFormat.getFrameSize();
			
		}
	}

	private byte[] addDelayToLoop(byte[] input, DelaySetting delaySetting) {
		int offsetInMs = delaySetting.getAdditionalDelayAudio()+manager.getTimeCalibration();
		int offsetInBytes = ((int)(offsetInMs*playbackFormat.getFrameRate()/1000.)*playbackFormat.getFrameSize());
		byte output[]; 
		if(offsetInBytes > 0) {
			output = new byte[input.length];
			System.arraycopy(input, 0, output, offsetInBytes, output.length-offsetInBytes);
			System.arraycopy(input, output.length-offsetInBytes, output, 0, offsetInBytes);
		} else if(offsetInBytes < 0){
			offsetInBytes = input.length + offsetInBytes;
			output = new byte[input.length];
			System.arraycopy(input, 0, output, offsetInBytes, output.length-offsetInBytes);
			System.arraycopy(input, output.length-offsetInBytes, output, 0, offsetInBytes);
		}
		else {
			output = input.clone();
		}
		return output;
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

	/*public int getAddDelayMeasures(){
		return offsetMeasures;
	}

	public int getAddDelayBeats(){
		return offsetBeats;
	}

	public int getAddDelayMS(){
		return offset_ms;
	}*/

	

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
		
		this.delaySetting = newDelaySetting;
		validateDelays();
		
	}
	private DelaySetting delaySetting;

	@Override
	public int getTotalDelayInMS() {
		return delaySetting.getAdditionalDelayAudio()+delaySetting.getAdditionalDelayGlobal()+delaySetting.getMeasuresDelay()*clock.getMsPerMeasure();
	}

	@Override
	public DelaySetting getDelaySetting() {
		return this.delaySetting;
	}

}
