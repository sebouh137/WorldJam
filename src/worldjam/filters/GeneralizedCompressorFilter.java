package worldjam.filters;
import javax.sound.sampled.AudioFormat;

import worldjam.audio.AudioFilter;
/**
 * Abstract base class for compression, decompression and gate effects
 * @author spaul
 *
 */
public abstract class GeneralizedCompressorFilter extends AudioFilter{

	public GeneralizedCompressorFilter(AudioFormat format) {
		super(format);
	}

	public void setTau(double tau){
		this.tau = tau;
	}
	public double getTau(){
		return tau;
	}
	private double tau = .05;
	private double runningRMS2;
	protected byte[] process(byte[] sampleData, AudioFormat format){
		double a = (tau*format.getFrameRate());
		int N = sampleData.length/(format.getFrameSize()/format.getChannels());
		for(int i = 0; i< N; i++){
			double x = dac.getConvertedSample(sampleData, i);
			runningRMS2+=(x*x-runningRMS2)/a;
			double xnew = rescaled(runningRMS2,x);
			if (xnew != x)
				dac.setConvertedSample(sampleData, i, xnew);
			
		}
		System.out.println(runningRMS2);
		return sampleData;
	}
	/**
	 * returns the rescaled sample value, given the current running RMS
	 * @param runningRMS2
	 * @param the current value
	 * @return
	 */
	protected abstract double rescaled(double runningRMS2, double x);
}
