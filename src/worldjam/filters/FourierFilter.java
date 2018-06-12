package worldjam.filters;


import javax.sound.sampled.AudioFormat;

import worldjam.audio.AudioFilter;
import worldjam.util.DigitalAnalogConverter;
import worldjam.util.ShortTimeFourierTransformer;

public class FourierFilter extends AudioFilter{
	protected ShortTimeFourierTransformer stft;
	//fourier transformer for second channel in stereo
	protected ShortTimeFourierTransformer stft2;
	protected FourierFilter(AudioFormat format, ShortTimeFourierTransformer stft){
		super(format);
		this.stft = stft;
		if(format.getChannels() == 2)
			stft2 = (ShortTimeFourierTransformer) stft.clone();

		stftp = (ShortTimeFourierTransformer) stft.clone();
	}
	ShortTimeFourierTransformer stftp;
	@Override
	protected byte[] process(byte[] sampleData, AudioFormat format) {
		for(int i = 0; i<sampleData.length/(format.getFrameSize()/format.getChannels()); i++){
			
			double x = dac.getConvertedSample(sampleData, i);
			dac.setConvertedSample(sampleData, i, x);
			
			//System.out.println(i);
			if(i%2 == 0 || format.getChannels() == 1){
				stft.nextSample(x);
				remap(stft, stftp);
				double xp = stftp.reverseTransform();
				dac.setConvertedSample(sampleData, i, xp);
			}
			else {
				stft2.nextSample(x);
				remap(stft2, stftp);
				double xp = stftp.reverseTransform();
				dac.setConvertedSample(sampleData, i, xp);
			}
			
		}
		return sampleData;
	}
	protected void remap(ShortTimeFourierTransformer originalSpectrum, ShortTimeFourierTransformer newSpectrum){
		for(int i = 0; i<originalSpectrum.Br.length; i++){
			newSpectrum.Br[i] = originalSpectrum.Br[i];
			newSpectrum.Bi[i] = originalSpectrum.Bi[i];
		}
	}
}
