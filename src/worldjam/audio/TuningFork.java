package worldjam.audio;

import worldjam.time.ClockSetting;


public class TuningFork implements LoopBuilder{
	
	public void setFrequency(double freq) {
		this.freq = freq;
		System.out.println("setting frequency " + freq);
	}
	
	public double getFrequency() {
		return freq;
	}
	this is a test to see if uncompilable code can be committed to github
	private double freq = 440;
	
	@Override
	public float[] createSamples(double frameRate, ClockSetting clock) {
		int nSamples = (int)(clock.beatsPerMeasure*clock.msPerBeat*frameRate/1000.);
		float floatBuffer[] = new float[nSamples];

		double A = .7;
	
		double tau = 2*clock.msPerBeat/1000.;
		double attack = .01; 
		//double shape_pow = 3;
		//double factor = Math.pow(shape_pow, shape_pow)*Math.exp(-shape_pow);
		for(int i = 0; i<nSamples; i++){
			double t = i/frameRate;
			double tp = t%(clock.getMsPerMeasure()/1000.); 
			//double a = A*Math.pow(tp/tau,shape_pow)*Math.exp(-tp/tau)/factor;
			double a = Math.exp(-tp/tau)*(1-Math.exp(-tp/attack))*A;
			//else System.out.println("other beat");
			floatBuffer[i] = (float) (Math.sin(t*freq*2*Math.PI)*a);
		}
		System.out.println(floatBuffer.length/frameRate);
		return floatBuffer;
	}

}
