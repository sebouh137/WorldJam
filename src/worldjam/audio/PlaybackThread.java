package worldjam.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import worldjam.core.BeatClock;

public class PlaybackThread extends Thread implements AudioSubscriber{
	private SourceDataLine sdl;
	/**
	 * The PlaybackThread uses the clock information in order to wait until the beginning of a measure to start,
	 * and so that the delay time can be set as an integer number of beats or measures in the 
	 * {@link #setReplayOffsetInBeats} and {@link #setReplayOffsetInMeasures} methods
	 */
	private BeatClock clock;
	private AudioFormat format;
	public PlaybackThread(Mixer mixer, AudioFormat format, BeatClock clock) throws LineUnavailableException{
		Line.Info info = new DataLine.Info(SourceDataLine.class, format);
		sdl = (SourceDataLine)mixer.getLine(info);
		this.clock = clock;
		this.format = format;
		setReplayOffsetInMeasures(1);
		//a buffer of 10 measures long is overkill
		int nMeasuresInBuffer = 5;
		int bufferSize = (int)(format.getFrameSize()*
				format.getFrameRate()*
				clock.msPerBeat/1000.*
				clock.beatsPerMeasure*
				nMeasuresInBuffer);
		buffer = new byte[bufferSize];
		sdl.open();
		sdl.start();
	}
	private byte[] buffer;
	private int bufferPosition;

	public void setReplayOffsetInMeasures(int nMeasures){
		replayOffsetInBytes = (int) (nMeasures*clock.beatsPerMeasure*clock.msPerBeat*format.getFrameRate()/1000.)*format.getFrameSize();
		
	}
	public void setReplayOffsetInBeats(int nBeats){
		replayOffsetInBytes = (int) (nBeats*clock.msPerBeat*format.getFrameRate()/1000.)*format.getFrameSize();
		
	}
	public void setReplayOffsetInBytes(int offset){
		this.replayOffsetInBytes = offset;
	}
	private int replayOffsetInBytes;
	public void sampleReceived(SampleMessage sample) {
		int dt = (int) (sample.sampleStartTime-loopStartTime);
		int destPos = (int) (dt*format.getFrameRate()/1000.)*format.getFrameSize();
		destPos += replayOffsetInBytes;
		destPos %= buffer.length;
		if(destPos < 0)
			destPos += buffer.length;
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
			int msPerMeasure = clock.getMsPerMeasure();
			long prestart = System.currentTimeMillis();
			int sleepTime = (int) (prestart-clock.startTime);
			sleepTime %= msPerMeasure;
			if(sleepTime<0) // account for a weird feature of modding negative numbers in jova
				sleepTime += msPerMeasure;
			Thread.sleep(sleepTime);
			loopStartTime = prestart+sleepTime;
		} catch (InterruptedException e) {
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
}
