package worldjam.filters.pitchshift;

import java.text.Format;

import javax.sound.sampled.AudioFormat;

import worldjam.filters.FourierFilter;
import worldjam.util.ShortTimeFourierTransformer;

public class PitchShift extends FourierFilter{
	static int N_OCTAVES = 10;
	static int N_DIVISIONS_PER_SEMITONE = 5;
	private int shiftInSemitones;
	
	private static double nyquistFreq(AudioFormat format){
		return format.getFrameRate()/2;
	}
	
	
	
	public PitchShift(AudioFormat format, int shiftInSemitones){
		super(format, new ShortTimeFourierTransformer(10, 
				1/format.getFrameRate(), 
				nyquistFreq(format)/Math.pow(2, N_OCTAVES), 
				nyquistFreq(format), 
				12*N_OCTAVES*N_DIVISIONS_PER_SEMITONE, 
				true));
		this.shiftInSemitones = shiftInSemitones;
		
		//exp(i*(w-wp)*t)
		fr = new double[stft.Br.length];
		fi = new double[stft.Br.length];
		for(int i = 0; i<fr.length; i++){
			fr[i] = 1;
		}
		
		//exp(i*(w-wp)*dt)
		gr = new double[stft.Br.length];
		gi = new double[stft.Br.length];
		for(int i = 0; i<fr.length; i++){
			double dw = stft.w[i]*(Math.pow(2,shiftInSemitones/12.)-1);
			gr[i] = Math.cos(stft.dt*dw);
			gi[i] = Math.sin(stft.dt*dw);
			System.out.println(stft.w[i] + " " + gr[i] + " " + gi[i] );
		}
	}
	double fr[];
	double fi[];
	
	double gr[];
	double gi[];
	@Override
	protected void remap(ShortTimeFourierTransformer originalSpectrum, ShortTimeFourierTransformer newSpectrum) {
		/*if(shiftInSemitones == 0){
			super.remap(originalSpectrum, newSpectrum);
			return;
		}*/
		for(int i = 0; i < originalSpectrum.Br.length; i++){
			//int ip = i+10*shiftInSemitones;
			
			double frnew = fr[i]*gr[i]-fi[i]*gi[i];
			double finew = fr[i]*gi[i]+fi[i]*gr[i]; 
			fr[i] = frnew;
			fi[i] = finew;
			/*if(ip >= 0 && ip < originalSpectrum.Br.length)
				newSpectrum.Br[i] = originalSpectrum.Br[ip];
			else 
				newSpectrum.Br[i] = 0;*/
			newSpectrum.Br[i] = fr[i]*originalSpectrum.Br[i] - fi[i]*originalSpectrum.Bi[i];
			newSpectrum.Bi[i] = fr[i]*originalSpectrum.Bi[i] + fi[i]*originalSpectrum.Br[i];
			/*if(i == 30)
				System.out.println(stft.w[i] + " " + originalSpectrum.Br[i] + " " + newSpectrum.Br[i]);*/
		}
	}
	
	
}
