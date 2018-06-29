package worldjam.audio;

import javax.sound.sampled.AudioFormat;

import worldjam.util.DigitalAnalogConverter;

public abstract class AudioFilter{
	protected AudioFilter(AudioFormat format){
		dac = new DigitalAnalogConverter(format);
		this.format = format;
	}
	protected AudioFormat format;
	protected DigitalAnalogConverter dac;
	/**
	 * By default, this converts the sample from digital to analog and then back.  However,
	 * extensions of this class do more interesting things with the audio samples.     
	 * @param sampleData
	 * @param format
	 * @return
	 */
	protected byte[] process(byte[] sampleData, AudioFormat format){
		for(int i = 0; i< sampleData.length/(format.getFrameSize()/format.getChannels()); i++){
			double x = dac.getConvertedSample(sampleData, i);
			dac.setConvertedSample(sampleData, i, x);
		}
		return sampleData;
	}

}
