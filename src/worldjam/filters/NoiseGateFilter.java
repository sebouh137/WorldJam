package worldjam.filters;

import javax.sound.sampled.AudioFormat;

public class NoiseGateFilter extends GeneralizedCompressorFilter {

	public NoiseGateFilter(AudioFormat format) {
		super(format);
	}

	private double threshold2 = .001;
	public double getThreshold() {
		return threshold2;
	}
	
	public void setThresholdDB(double thresholdDB){
		this.threshold2 = (Math.pow(10,thresholdDB/10.));
	}
	public double getThresholdDB(){
		return Math.log10(threshold2)/20;
	}
	
	public double getPow() {
		return pow;
	}
	public void setPow(double pow) {
		this.pow = pow;
	}

	private double pow = 0;
	@Override
	protected double rescaled(double runningRMS2, double x) {
		double y =  runningRMS2/threshold2;
		if(pow == 0){
			return y>1 ? x : 0;
		}
		else {
			double py = Math.pow(y, pow);
			return py/(py+1)*x;	
		}
	}

}
