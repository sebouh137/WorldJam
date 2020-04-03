package worldjam.util;

import javax.sound.sampled.AudioFormat;

public class DigitalAnalogConverter {
	private AudioFormat format;

	public DigitalAnalogConverter(AudioFormat format){
		this.format = format;
	}

	/**
	 * Determine the peak value in the range of bytes.  
	 * Don't convert to floating point until the end,
	 * in order to save time 
	 * @param bytes
	 * @param i
	 * @return
	 */
	public double getPeak(byte[] bytes, int min, int max){
		int peak = 0;
		switch(format.getSampleSizeInBits()){
		case 8:
			for(int i = min; i<max; i++) {
				byte val = bytes[i];
				if(val>peak)
					peak = val;
				else if (-val>peak)
					peak = -val;
			}
			return peak/128.;
		case 16:
			if (!format.isBigEndian()) {
				for(int i = min; i<max; i++) {
					int val = ((bytes[2*i+1] << 8) | (bytes[2*i] & 0xff));
					if(val>peak)
						peak = val;
					else if (-val>peak)
						peak = -val;
				}
				return peak/(32768.);
			}
			else {
				for(int i = min; i<max; i++) {
					int val = ((bytes[2*i] << 8) | (bytes[2*i+1] & 0xff));
					if(val>peak)
						peak = val;
					else if (-val>peak)
						peak = -val;
				}
				return peak/(32768.);
			}
		case 24:
			if (!format.isBigEndian()) {
				for(int i = min; i<max; i++) {
					int val = ((bytes[3*i+2] << 16) | ((bytes[3*i+1]& 0xff) << 8) | (bytes[3*i] & 0xff));
					if(val>peak)
						peak = val;
					else if (-val>peak)
						peak = -val;
				}
				return peak/8388608.;
			}
			else {
				for(int i = min; i<max; i++) {
					int val = ((bytes[3*i] << 16) | ((bytes[3*i+1]& 0xff) << 8) | (bytes[3*i+2] & 0xff));
					if(val>peak)
						peak = val;
					else if (-val>peak)
						peak = -val;
				}
				return peak/8388608.;
			}
		}
		return Double.NaN;
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
			else{
				return ((bytes[3*i] << 16) | ((bytes[3*i+1]& 0xff) << 8) | (bytes[3*i+2] & 0xff))/8388608.;
			}
		}
		return Double.NaN;
	}

	public float getConvertedSample32Bit(byte[] bytes, int i){
		switch(format.getSampleSizeInBits()){
		case 8:
			return bytes[i]/128.f;
		case 16:
			if (!format.isBigEndian())
				return ((bytes[2*i+1] << 8) | (bytes[2*i] & 0xff))/(32768.f);
			else
				return ((bytes[2*i] << 8) | (bytes[2*i+1] & 0xff))/(32768.f);
		case 24:
			if (!format.isBigEndian())
				return ((bytes[3*i+2] << 16) | ((bytes[3*i+1]& 0xff) << 8) | (bytes[3*i] & 0xff))/8388608.f;
			else{
				return ((bytes[3*i] << 16) | ((bytes[3*i+1]& 0xff) << 8) | (bytes[3*i+2] & 0xff))/8388608.f;
			}
		}
		return 0;
	}
	
	public void setConvertedSample(byte[] bytes, int i, double sample){
		switch(format.getSampleSizeInBits()){
		case 8:
			bytes[i] = (byte)(sample*128);
			break;
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
			break;
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
	
	public void setConvertedSample(byte[] bytes, int i, float sample){
		switch(format.getSampleSizeInBits()){
		case 8:
			bytes[i] = (byte)(sample*128);
			break;
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
			break;
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

	public void convert(float[] floatBuffer, byte[] sampleData) {
		for(int i = 0; i < floatBuffer.length; i++){
			 setConvertedSample(sampleData, i, floatBuffer[i]);
		}
		
	}

	public void convert(byte[] sampleData, float[] floatBuffer) {
		for(int i = 0; i < floatBuffer.length; i++){
			floatBuffer[i] = getConvertedSample32Bit(sampleData, i);
		}
	}
}
