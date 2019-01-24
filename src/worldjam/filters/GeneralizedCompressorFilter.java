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
		for(int i = 0; i< sampleData.length/(format.getFrameSize()/format.getChannels()); i++){
			double x = dac.getConvertedSample(sampleData, i);
			runningRMS2+=(x*x-runningRMS2)/(tau*format.getFrameRate());
			x *= rescale(runningRMS2);
			dac.setConvertedSample(sampleData, i, x);
			
		}
		return sampleData;
	}
	/**
	 * factor by which to scale the samples
	 * @param runningRMS22
	 * @return
	 */
	protected abstract double rescale(double runningRMS22);
}
