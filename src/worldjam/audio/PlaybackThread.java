package worldjam.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import worldjam.core.BeatClock;
import worldjam.util.DigitalAnalogConverter;

public class PlaybackThread extends Thread implements PlaybackChannel{
	private SourceDataLine sdl;
	/**
	 * The PlaybackThread uses the clock information in order to wait until the beginning of a measure to start,
	 * and so that the delay time can be set as an integer number of beats or measures in the 
	 * {@link #setReplayOffsetInBeats} and {@link #setReplayOffsetInMeasures} methods
	 */
	private BeatClock clock;
	private AudioFormat format;
	private Mixer mixer;
	public PlaybackThread(Mixer mixer, AudioFormat format, BeatClock clock) throws LineUnavailableException{
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		this.mixer = mixer;
		sdl = (SourceDataLine)mixer.getLine(info);

		this.clock = clock;
		this.format = format;
		setReplayOffset(1, 0, 0);
		//a buffer of 10 measures long is overkill
		int nMeasuresInBuffer = 5;
		int bufferSize = (int)(format.getFrameSize()*
				format.getFrameRate()*
				clock.msPerBeat/1000.*
				clock.beatsPerMeasure*
				nMeasuresInBuffer);
		buffer = new byte[bufferSize];
		this.start();
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
				total_offset_ms*format.getFrameRate()/1000.)*format.getFrameSize();
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
	public void sampleReceived(SampleMessage sample) {
		int dt = (int) (sample.sampleStartTime-loopStartTime);
		int destPos = (int) (dt*format.getFrameRate()/1000.)*format.getFrameSize();
		destPos += replayOffsetInBytes;
		destPos %= buffer.length;
		if(destPos < 0)
			destPos += buffer.length;
		if(filter != null){
			sample.sampleData = filter.process(sample.sampleData, format);
		}
		AudioUtils.arrayCopyWrapped(sample.sampleData, 0, buffer, destPos, sample.sampleData.length);
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
		int N_BYTES_PLAYED_AT_ONCE = (int) (format.getFrameRate()*format.getFrameSize());
		
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
	public AudioFormat getFormat() {
		return format;
	}


	public void setClock(BeatClock clock) {
		this.clock = clock;
		this.setReplayOffset(offsetMeasures, offsetBeats, offset_ms);
	}

	public double getRMS(double windowInMS){
		long t = System.currentTimeMillis()-loopStartTime;
		int offsetInBytes = (((int) (t*format.getFrameRate()/1000.))*format.getFrameSize())%buffer.length;

		int nSamples = (int)(windowInMS/1000.*format.getSampleRate());
		double sumSqr = 0;
		DigitalAnalogConverter dac = new DigitalAnalogConverter(format);
		for(int i = 0; i<nSamples; i++){
			int index = offsetInBytes/(format.getSampleSizeInBits()/8)-i;
			if(index <0)
				index+= buffer.length/(format.getSampleSizeInBits()/8);
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
	
	public int getDelayInMS() {
		return total_offset_ms;
	}

	public BeatClock getClock() {
		// TODO Auto-generated method stub
		return clock;
	}

	public Mixer getMixer() {
		return mixer;
	}

	@Override
	public AudioFilter getFilter() {
		return filter;
	}

	
}
