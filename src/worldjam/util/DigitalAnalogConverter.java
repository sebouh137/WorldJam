package worldjam.util;

import javax.sound.sampled.AudioFormat;

public class DigitalAnalogConverter {
	private AudioFormat format;

	public DigitalAnalogConverter(AudioFormat format){
		this.format = format;
	}

	public double getConvertedSample(byte[] bytes, int i){
		switch(format.getSampleSizeInBits()){
		case 8:
			return bytes[i]/128.;
		case 16:
			if (!format.isBigEndian())
				return ((bytes[2*i+1] << 8) | (bytes[2*i] & 0xff))/(32768.);
			else
				return ((bytes[2*i] << 8) | (bytes[2*i+1] & 0xff))/(32768.);
		case 24:
			if (!format.isBigEndian())
				return ((bytes[3*i+2] << 16) | ((bytes[3*i+1]& 0xff) << 8) | (bytes[3*i] & 0xff))/8388608.;
			else
				return ((bytes[3*i] << 16) | ((bytes[3*i+1]& 0xff) << 8) | (bytes[3*i+2] & 0xff))/8388608.;
		}
		return 0;
	}

	public void setConvertedSample(byte[] bytes, int i, double sample){
		switch(format.getSampleSizeInBits()){
		case 8:
			bytes[i] = (byte)(sample*128);
		case 16:
			if(!format.isBigEndian()){

				int intvalue = (int)(sample*32768);
				bytes[2*i + 1] = (byte)(0x00FF&(intvalue>>8));
				bytes[2*i] = (byte)(0x000000FF & intvalue);
			}
			else {
				int intvalue = (int)(sample*32768);
				bytes[2*i] = (byte)(0x00FF&(intvalue>>8));
				bytes[2*i+1] = (byte)(0x000000FF & intvalue);
			}
		case 24:
			if(!format.isBigEndian()) {
				int intvalue = (int)(sample*8388608);
				bytes[3*i + 2] = (byte)(0x00FF & (intvalue>>16));
				bytes[3*i + 1] = (byte)(0x00FF & (intvalue>>8));
				bytes[3*i] = (byte)(0x000000FF & intvalue);
			}
			else {
				int intvalue = (int)(sample*8388608);

				bytes[3*i] = (byte)(0x00FF & (intvalue>>16));
				bytes[3*i+1] = (byte)(0x00FF & (intvalue>>8));
				bytes[3*i+2] = (byte)(0x000000FF & intvalue);
			}
		}
	}
}
