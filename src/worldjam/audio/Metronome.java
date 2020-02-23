package worldjam.audio;

import javax.sound.sampled.AudioFormat;

import worldjam.time.ClockSetting;
import worldjam.util.DefaultObjects;
import worldjam.util.DigitalAnalogConverter;

public class Metronome implements ChannelAutomator{
	long channelID = 10;
	@Override
	public float[] createSamples(double frameRate, ClockSetting clock) {
		System.out.println("generating metronome for clock setting " + clock);
		int nSamples = (int)(clock.beatsPerMeasure*clock.msPerBeat*frameRate/1000.);
		float floatBuffer[] = new float[nSamples];

		double f1 = 1760;
		double A = .3;
		double tau = .005;
		double shape_pow = 1;
		double factor = Math.pow(shape_pow, shape_pow)*Math.exp(-shape_pow);
		for(int i = 0; i<nSamples; i++){
			double t = i/frameRate;
			double tp = t%(clock.msPerBeat/1000.); 
			double a = A*Math.pow(tp/tau,shape_pow)*Math.exp(-tp/tau)/factor;
			double f = f1;
			if (i<nSamples/clock.beatsPerMeasure) {
				a*=3;
				f*=1;
				//System.out.println("first beat");
			}
			//else System.out.println("other beat");
			floatBuffer[i] = (float) (Math.sin(t*f*2*Math.PI)*a);
		}
		System.out.println(floatBuffer.length/frameRate);
		return floatBuffer;
	}
	
	
	/*AudioSample generateTestPulse(long pulseStartTime){
		double freq = 1660; //A6
		double ampl = 0.7;
		double nshape = 1; //shaping parameter for the attack
		double tdecay = .003;
		double t0 = 0;

		ampl/=Math.pow(nshape,nshape)*Math.exp(nshape);

		AudioSample sample = new AudioSample();
		float nSeconds = 2.0f;
		int nSamples = (int) (nSeconds*format.getFrameRate());
		int nbytes = nSamples*format.getFrameSize();
		sample.sampleData = new byte[nbytes];
		for (int i = 0; i <nSamples; i++) {
			double time = i/format.getFrameRate();
			double value = 0;

			if(time > t0)
				value = Math.sin(2*Math.PI*freq*time)*ampl*Math.pow((time-t0)/tdecay,nshape)*Math.exp(-(time-t0)/tdecay);
			//System.out.println(value);
			adc.setConvertedSample(sample.sampleData, i, value);
		}
		sample.sampleStartTime = pulseStartTime;
		sample.sourceID = channelID;
		System.out.println("generated test pulse");
		return sample;
	}*/
	
}
