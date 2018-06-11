package worldjam.audio;

import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import worldjam.core.BeatClock;

public class InputThread extends Thread{
	static Random random = new Random();
	private final long lineID = random.nextLong();
	private TargetDataLine tdl;
	
	private int nBytesPerLoop;
	byte[] buffer;
	private AudioSubscriber receiver;
	private AudioFormat format;
	public InputThread(Mixer mixer, AudioFormat format, BeatClock clock) throws LineUnavailableException{
		this.format = format;
		this.mixer = mixer;
		Line.Info info = new DataLine.Info(TargetDataLine.class, format);
		tdl = (TargetDataLine)mixer.getLine(info);
		nMsPerLoop = 1000;
		nBytesPerLoop = format.getFrameSize()*(int)(format.getFrameRate()*nMsPerLoop/1000.);
		buffer = new byte[nBytesPerLoop];
	}
	double nMsPerLoop;
	//status flags
	boolean alive = true, paused = false;
	private Mixer mixer;
	public void run(){
		try {
			tdl.open();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tdl.start();
		long time = System.currentTimeMillis();
		time = (time*format.getFrameSize())/format.getFrameSize();
		while(alive){
			tdl.read(buffer, 0, buffer.length);
			SampleMessage message = new SampleMessage();
			message.sampleData = buffer.clone();
			message.senderID = this.lineID;
			message.sampleStartTime = time;
			time+= nMsPerLoop;
			if(receiver!= null)
				receiver.sampleReceived(message);
			
		}
	}
	public void setReceiver(AudioSubscriber rec){
		this.receiver = rec;
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
}
