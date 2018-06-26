package worldjam.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;


public class AudioUtils {
	public static byte[] getByteArray(int ms, AudioFormat format){
		 return new byte[format.getFrameSize()*(int)(ms*format.getFrameRate()/1000)];
	 }
	public static int nBytes(int ms, AudioFormat format){
		return format.getFrameSize()*(int)(ms*format.getFrameRate()/1000);
	}
	public static byte[] getClip(byte[] loop, int clipPos, int msPerClip, AudioFormat format) {
		
		byte[] by = getByteArray(msPerClip, format);
		int srcPos = nBytes(clipPos, format);
		//System.out.println(loop.length + " " + srcPos + " " + by.length);
		arrayCopyWrapped(loop, srcPos, by, 0, by.length);
		return by;
	}
	public static void arrayCopyWrapped(byte[] src, int srcPos, byte[] dest, int destPos, int length){
		if(destPos + length <= dest.length && srcPos + length <= src.length) {
			System.arraycopy(src, srcPos, dest, destPos, length);
		} else if (srcPos + length <= src.length){
			System.arraycopy(src, srcPos, dest, destPos, dest.length - destPos);
			System.arraycopy(src, srcPos + dest.length - destPos, dest, 0, length - (dest.length - destPos));
		} else if (destPos + length <= dest.length){
			System.arraycopy(src, srcPos, dest, destPos, src.length - srcPos);
			System.arraycopy(src, 0, dest, destPos + (src.length - srcPos), length - (src.length - srcPos));
		} else {
			//TODO add code here for double wrapping
		}
		
	}
	
	
	
}
