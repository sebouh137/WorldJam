package worldjam.test.generators;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;


public class Generator {
	private ByteConverter bc;
	AudioFormat format;
	public Generator(AudioFormat format) {
		this.bc = new ByteConverter(format);
		this.format = format;
	}
	public byte[] sine(double amplitude, double freq, double length){
		double[] d = new double[format.getFrameSize()*(int)(
				format.getFrameRate()*length/1000.)];
		for(int i = 0; i< d.length; i++){
			d[i] = amplitude*Math.sin(i*freq*2*Math.PI/format.getSampleRate());
		}
		return bc.asBytes(d);
	}
	public byte[] square(double amplitude, double freq, double length){
		double[] d = new double[format.getFrameSize()*(int)(
				format.getFrameRate()*length/1000.)];
		for(int i = 0; i< d.length; i++){
			d[i] = amplitude*(2*((int)(i*freq*2/format.getSampleRate())%2)-1)/2.;
		}
		return bc.asBytes(d);
	}
	
}
