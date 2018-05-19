package worldjam.test.generators;

import javax.sound.sampled.AudioFormat;

public class ByteConverter {
	public ByteConverter(AudioFormat format){
		this.audioFormat = format;
	}
	private AudioFormat audioFormat;
	public double[] asDoubles(byte[] b){

		switch(audioFormat.getSampleSizeInBits()){
		case 8:
			int length = b.length;
			allocateDoublesIfNotAllocated(length);
			for(int i = length-1; i>= 0; i--)
				d[i] = b[i]/128.;
			return d;
		
		case 16:
			length = b.length/2;
			allocateDoublesIfNotAllocated(length);
			if (!audioFormat.isBigEndian())
				for(int i = length-1; i>= 0; i--)
					d[i] = ((b[2*i+1] << 8) | (b[2*i] & 0xff))/(32768.);
			else
				for(int i = length-1; i>= 0; i--)
					d[i] = ((b[2*i] << 8) | (b[2*i+1] & 0xff))/(32768.);
			return d;
		case 24:
			length = b.length/3;
			allocateDoublesIfNotAllocated(length);
			if (!audioFormat.isBigEndian())
				for(int i = length-1; i>= 0; i--)
					d[i] = ((b[3*i+2] << 16) | ((b[3*i+1]& 0xff) << 8) | (b[3*i] & 0xff))/8388608.;
			else
				for(int i = length-1; i>= 0; i--)
					d[i] = ((b[3*i] << 16) | ((b[3*i+1]& 0xff) << 8) | (b[3*i+2] & 0xff))/8388608.;
			return d;
		}
		return null;
	}
	private void allocateDoublesIfNotAllocated(int length){
		
		if(d.length != length)
			d = new double[length];
	}
	private double d[] = {};
	private static int makeInt24(byte c, byte d, byte e) {
		
		return (c << 16) | ((d& 0xff) << 8) | (e & 0xff);
	}
	static private int makeInt16(byte b1, byte b0) {
		return (b1 << 8) | (b0 & 0xff);
	}
	public byte[] asBytes(double[] d){
		allocateBytesIfNotAllocated(d.length*audioFormat.getSampleSizeInBits()/8);
		switch(audioFormat.getSampleSizeInBits()){
		case 8:
			return toBytes1(d);
		case 16:
			return toBytes2(d);
		case 24:
			return toBytes3(d);
		}
		return null;
	}
	void allocateBytesIfNotAllocated(int length){
		if(b.length != length)
			b = new byte[length];
	}
	private byte[] b = {};
	private byte[] toBytes1(double[] d) {
		for(int i = b.length-1; i>= 0; i--)
			b[i] = (byte)(d[i]*128);
		return b;
	}
	private byte[] toBytes2(double[] d){
		int length = d.length;
		if(!audioFormat.isBigEndian())
			for(int i = length-1; i>= 0; i--)
			{
				int intvalue = (int)(d[i]*32768);
				b[2*i + 1] = (byte)(0x00FF&(intvalue>>8));
				b[2*i] = (byte)(0x000000FF & intvalue);
			}
		else
			for(int i = length-1; i>= 0; i--)
			{
				int intvalue = (int)(d[i]*32768);
				b[2*i] = (byte)(0x00FF&(intvalue>>8));
				b[2*i+1] = (byte)(0x000000FF & intvalue);
			}
		return b;
	}
	private byte[] toBytes3(double[] d){
		int length = d.length;
		if(!audioFormat.isBigEndian())
			for(int i = length-1; i>= 0; i--)
			{
				int intvalue = (int)(d[i]*8388608);
				b[3*i + 2] = (byte)(0x00FF&(intvalue>>16));
				b[3*i + 1] = (byte)(0x00FF&(intvalue>>8));
				b[3*i] = (byte)(0x000000FF & intvalue);
			}
		else
			for(int i = length-1; i>= 0; i--)
			{
				int intvalue = (int)(d[i]*8388608);

				b[3*i] = (byte)(0x00FF&(intvalue>>16));
				b[3*i+1] = (byte)(0x00FF&(intvalue>>8));
				b[3*i+2] = (byte)(0x000000FF & intvalue);
			}
		return b;
	}
}
