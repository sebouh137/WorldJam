package worldjam.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.time.ClockSetting;
import worldjam.util.DigitalAnalogConverter;

public class AutomatedChannel extends PlaybackThread {

	private DigitalAnalogConverter dac;
	public AutomatedChannel(Mixer mixer, AudioFormat inputFormat, ClockSetting clock, String sourceName, long sourceID,
			ChannelAutomator automator)
					throws LineUnavailableException {
		super(mixer, inputFormat, clock, sourceName, sourceID);
		dac = new DigitalAnalogConverter(inputFormat);
		this.automator = automator;
		this.changeClockSettingsNow(clock);
	}
	ChannelAutomator automator;
	class FeederThread extends Thread{
		boolean stopped = false;
		AudioSample sample;
		ClockSetting clock;
		FeederThread(AudioSample sample, ClockSetting clock){
			this.sample = sample;
			this.clock = clock;
		}
		public void run() {
			long now = System.currentTimeMillis();
			sample.sampleStartTime = clock.startTime + ((now-clock.startTime)/clock.getMsPerMeasure())*clock.getMsPerMeasure();
			//while(!stopped) {
				sampleReceived(sample);
				try {
					Thread.sleep(clock.getMsPerMeasure());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sample.sampleStartTime += clock.getMsPerMeasure();
			//}
		}
	}
	FeederThread feederThread; 
	@Override
	public void changeClockSettingsNow(ClockSetting clock){
		AudioFormat format = getInputFormat();
		float[] floatBuffer = automator.createSamples(format.getFrameRate(), clock);
		int nSamples = (int) (clock.beatsPerMeasure*clock.msPerBeat*format.getFrameRate()/1000);
		int nBytes = format.getFrameSize()*nSamples;

		this.bufferPosition = 0;
		//double the length of the buffer due to the stereo
		this.buffer = new byte[nBytes*4];

		
		byte[] bytes = new byte[nBytes];
		//make it start at the beginning of the next measure
		dac.convert(floatBuffer, bytes);
		AudioSample sample = new AudioSample();
		sample.sampleData = bytes;
		long now = System.currentTimeMillis();
		int buffPos = (int)(((now-clock.startTime)%clock.getMsPerMeasure())*format.getFrameRate()/1000.)*2*format.getFrameSize();
		this.bufferPosition = buffPos;
		sample.sampleStartTime = clock.startTime + ((now-clock.startTime)/clock.getMsPerMeasure())*clock.getMsPerMeasure();
		this.sampleReceived(sample);
		sample.sampleStartTime += clock.getMsPerMeasure();
		this.sampleReceived(sample);
		//sample.sampleStartTime += clock.getMsPerMeasure();
		//this.sampleReceived(sample);
		//this.bufferPosition = 0;
		//double the length of the buffer due to the stereo
		/*this.buffer = new byte[nBytes*4];
		int buffPos = (int)(((now-clock.startTime)%clock.getMsPerMeasure())*format.getFrameRate()/1000.)*2*format.getFrameSize();

		while (buffPos <0)
			buffPos += buffer.length;
		this.bufferPosition = buffPos;
		byte repeated[] = new byte[sample.sampleData.length*2];
		System.arraycopy(sample.sampleData, 0, repeated,0, sample.sampleData.length);
		System.arraycopy(sample.sampleData, 0, repeated,sample.sampleData.length, sample.sampleData.length);
		sample.sampleData = repeated;
		this.sampleReceived(sample);*/

	}



}
