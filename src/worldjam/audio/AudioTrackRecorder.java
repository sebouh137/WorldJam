package worldjam.audio;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;

public class AudioTrackRecorder implements AudioSubscriber{

	private OutputStream output;
	private AudioFormat format;
	private long startTime;

	public AudioTrackRecorder(OutputStream output, AudioFormat format, long startTime) {
		this.output = output;
		this.format = format;
		this.startTime = startTime;
	}

	public void stopRecording(long timestamp) {
		// TODO Auto-generated method stub

	}

	boolean receivedFirstSample = false;
	@Override
	public void sampleReceived(AudioSample sample) {
		try {
			if(receivedFirstSample){
				//pad with 0's.  
				int msPad = (int)(sample.sampleStartTime-startTime);
				int bytesPad = (int)(msPad*format.getFrameSize()*format.getFrameRate()/1000);
				byte pad[] = new byte[bytesPad];
				output.write(pad);
			}
			output.write(sample.sampleData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receivedFirstSample = true;
	}

}
