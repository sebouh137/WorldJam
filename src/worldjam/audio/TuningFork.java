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
	
	private double freq = 440;
	
	@Override
	public float[] createSamples(double frameRate, ClockSetting clock) {
		//force there to be an integer number of cycles per measure.  
		double Tloop = clock.beatsPerMeasure*clock.msPerBeat/1000.;
		double realfreq = ((int)(freq*Tloop))/Tloop;
		
		int nSamples = (int)(clock.beatsPerMeasure*clock.msPerBeat*frameRate/1000.);
		float floatBuffer[] = new float[nSamples];

		double A = .7;
	
		double tau = 2*clock.msPerBeat/1000.;
		double attack = .01; 
		//double shape_pow = 3;
		//double factor = Math.pow(shape_pow, shape_pow)*Math.exp(-shape_pow);
		for(int i = 0; i<nSamples; i++){
			double t = i/frameRate;
			double a = A;
			if(percussive) {
				double tp = t%(clock.getMsPerMeasure()/1000.);
				//double a = A*Math.pow(tp/tau,shape_pow)*Math.exp(-tp/tau)/factor;
				a *= Math.exp(-tp/tau)*(1-Math.exp(-tp/attack));
			}
			//else System.out.println("other beat");
			floatBuffer[i] = (float) (Math.sin(t*realfreq*2*Math.PI)*a);
		}
		System.out.println(floatBuffer.length/frameRate);
		return floatBuffer;
	}
	private boolean percussive = true;
	public void setPercussiveMode(boolean percussive) {
		this.percussive = percussive;
	}
	public boolean getPercussiveMode() {
		return percussive;
	}

}
