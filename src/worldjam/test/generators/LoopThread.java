package worldjam.test.generators;

import java.util.Random;

import javax.sound.sampled.AudioFormat;

import worldjam.audio.AudioUtils;
import worldjam.time.ClockSetting;
import worldjam.audio.AudioSample;
import worldjam.audio.AudioSubscriber;

public abstract class LoopThread extends Thread {
	
	protected AudioFormat format;
	protected ClockSetting clock;
	private AudioSubscriber rec;
	public LoopThread(ClockSetting clock, AudioFormat format){
		this.format = format;
		this.clock = clock;
	}
	private boolean alive = true;
	
	private long time;
	
	public abstract void generateLoop();
	
	protected byte loop[];
	protected int msPerLoop;
	
	public void run(){
		generateLoop();
		
		
		
				
		int msPerClip = 100;
		
		time = (System.currentTimeMillis()/10)*10;
		while(alive){
			
			byte[] sample = AudioUtils.getClip(loop, (int)((time-clock.startTime)%msPerLoop), msPerClip, format);
			if(rec != null)
				rec.sampleReceived(new AudioSample(senderID, time, sample));
			try {
				Thread.sleep(msPerClip);
				time+=msPerClip;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setReceiver(AudioSubscriber rec) {
		this.rec = rec;
		
	}

	private long senderID;
	{
		Random random = new Random();
		senderID = random.nextLong();
	}
	public long getSenderID() {
		return senderID;
	}
	
}
