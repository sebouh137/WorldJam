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
	private static final int N_BYTES_PER_LOOP = 1200;
	private SourceDataLine sdl;
	private BeatClock clock;
	private AudioFormat format;
	public PlaybackThread(Mixer mixer, AudioFormat format, BeatClock clock) throws LineUnavailableException{
		Line.Info info = new DataLine.Info(SourceDataLine.class, format);
		sdl = (SourceDataLine)mixer.getLine(info);
		this.clock = clock;
		this.format = format;

		//a buffer of 10 measures long is overkill
		int nMeasuresInBuffer = 2;
		int bufferSize = (int)(format.getFrameSize()*
				format.getFrameRate()*
				clock.msPerBeat/1000.*
				clock.beatsPerMeasure*
				nMeasuresInBuffer);
		buffer = new byte[bufferSize];
		replayOffsetInBytes = (int) (clock.beatsPerMeasure*clock.msPerBeat*format.getFrameSize()/1000.)*format.getFrameSize();
		sdl.open();
		sdl.start();
	}
	private byte[] buffer;
	private int bufferPosition;

	public void setReplayOffsetInBytes(int offset){
		this.replayOffsetInBytes = offset;
	}
	private int replayOffsetInBytes;
	public void sampleReceived(SampleMessage sample) {
		int dt = (int) (sample.sampleStartTime-clock.startTime);
		//System.out.println(dt);
		int destPos = (int) (dt*format.getFrameRate()/1000.)*format.getFrameSize();
		destPos+= replayOffsetInBytes;
		destPos %= buffer.length;
		if(destPos < 0)
			destPos += buffer.length;
		AudioUtils.arrayCopyWrapped(sample.sampleData, 0, buffer, destPos, sample.sampleData.length);
		/*if(destPos + sample.sampleData.length < buffer.length) {
			//System.out.println(sample.sampleData.length + " " + destPos + " " + buffer.length);
			System.arraycopy(sample.sampleData, 0, buffer, destPos, sample.sampleData.length);
		}
		else {
			//System.out.println(sample.sampleData.length + " " + destPos + " " + buffer.length);
			System.arraycopy(sample.sampleData, 0, buffer, destPos, buffer.length - destPos);
			//System.out.println(sample.sampleData.length + " " + (buffer.length-destPos) + " " + buffer.length);
			System.arraycopy(sample.sampleData, buffer.length - destPos, buffer, 0, sample.sampleData.length - (buffer.length - destPos));
		}*/
	}


	//status flags
	private boolean alive = true;

	public void kill(){
		alive = false;
	}

	public void run(){
		/*
		 * Must start at the beginning of a measure
		 */
		try {
			int msPerMeasure = clock.getMsPerMeasure();
			int sleepTime = (int) (System.currentTimeMillis()-clock.startTime);
			sleepTime %= msPerMeasure;
			if(sleepTime<0) // weird feature of modding in jova
				sleepTime += msPerMeasure;
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(alive){
			if(bufferPosition + N_BYTES_PER_LOOP < buffer.length){
				sdl.write(buffer, bufferPosition, N_BYTES_PER_LOOP);
				bufferPosition+= N_BYTES_PER_LOOP;
			}
			else {
				sdl.write(buffer, bufferPosition, buffer.length - bufferPosition);
				sdl.write(buffer, 0, N_BYTES_PER_LOOP - (buffer.length - bufferPosition));
				bufferPosition = N_BYTES_PER_LOOP - (buffer.length - bufferPosition);
			}


		}
	}

	public Control[] getLineControls(){
		return sdl.getControls();
	}
}
