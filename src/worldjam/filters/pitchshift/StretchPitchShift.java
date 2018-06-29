package worldjam.filters.pitchshift;


import javax.sound.sampled.AudioFormat;

import worldjam.audio.AudioFilter;

/**
 * Stretches a signal by a pitch-shift factor, cuts it into segments,
 * and then merges it with some segment-overlap to fit the original signal's duration. 
 * So far, I am only supporting stereo input.
 * @author spaul
 *
 */
public class StretchPitchShift extends AudioFilter{
	
	private double factor;

	public StretchPitchShift(AudioFormat format, int shiftInCents){
		super(format);
		setShiftInCents(shiftInCents);
		
	}
	
	public StretchPitchShift(AudioFormat format, double factor){
		super(format);
		setStretchFactor(factor);
		
	}
	
	private void setStretchFactor(double factor) {
		this.factor = factor;
	}

	private void setShiftInCents(int shiftInCents) {
		this.setStretchFactor(Math.pow(2,  -shiftInCents/1200.));
	}

	float floatBuffer[];
	float stretchedFloatBufferA[];
	float stretchedFloatBufferB[];
	private float[] activeBuffer;
	private float[] prevBuffer;
	
	
	
	protected byte[] process(byte[] sampleData, AudioFormat format){
		
		if(floatBuffer == null || floatBuffer.length != sampleData.length/Float.BYTES){
			floatBuffer = new float[sampleData.length/(format.getSampleSizeInBits()/8)];
		}
		dac.convert(sampleData, floatBuffer);
		
		if(stretchedFloatBufferA == null || stretchedFloatBufferA.length != (int)(floatBuffer.length*factor)){
			stretchedFloatBufferA = new float[(int)(floatBuffer.length*factor)];
		}
		if(stretchedFloatBufferB == null || stretchedFloatBufferB.length != (int)(floatBuffer.length*factor)){
			stretchedFloatBufferB = new float[(int)(floatBuffer.length*factor)];
		}
		if(activeBuffer == null || activeBuffer == stretchedFloatBufferB){
			activeBuffer = stretchedFloatBufferA;
			prevBuffer = stretchedFloatBufferB;
		}
		else if(activeBuffer == stretchedFloatBufferA){
			activeBuffer = stretchedFloatBufferB;
			prevBuffer = stretchedFloatBufferA;
		}
		stretch(floatBuffer, activeBuffer);
		merge(activeBuffer, prevBuffer, floatBuffer);
		
		dac.convert(floatBuffer,sampleData);
		return sampleData.clone();
	}

	double msPerSegment = 100;//1000./44100*10;
	
	
	int samplePhase;
	

	private void stretch(float[] in, float[] out) {
		for(int i = 0; i< out.length; i++){
			double x = i/factor;
			if(x+1>=in.length){
				out[i] = in[in.length-1];
				continue;
			}
			
			float y1 = in[(int)x];
			float y2 = in[(int)x+1];
			out[i] = (float)(y1*(1-x%1.)+y2*(x%1.));
		}
	}

	private void merge(float[] in, float[] inPrev, float[] out) {
		int inSegmentLength = (int)(format.getSampleRate()*msPerSegment/1000.);
		int outSegmentLength = (int)(format.getSampleRate()*msPerSegment/1000./factor);
		//int segmentLength = (int)(format.getSampleRate()*msPerSegment/1000.);
		for(int i = 0; i < out.length; i++){
			int ip = out.length-i-1;
			int index = in.length - 1 - ((ip/outSegmentLength)*inSegmentLength + ip%outSegmentLength);
			if(i%100 == 0)
			System.out.println(i + " " + index + " " + in.length + " " + out.length + " " + inSegmentLength + " " + outSegmentLength);
			if(index >= 0)
				out[i] = (float)in[index];
			else
				out[i] = (float)inPrev[index+inPrev.length];
		}
	}
	
}
