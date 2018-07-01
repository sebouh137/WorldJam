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
	
	private double _factor;

	public StretchPitchShift(AudioFormat format, int shiftInCents){
		super(format);
		setShiftInCents(shiftInCents);
		
	}
	
	public StretchPitchShift(AudioFormat format, double factor){
		super(format);
		setStretchFactor(factor);
		
	}
	
	private void setStretchFactor(double factor) {
		this._factor = factor;
	}

	public void setShiftInCents(int shiftInCents) {
		this.setStretchFactor(Math.pow(2,  -shiftInCents/1200.));
	}

	float floatBuffer[];
	float stretchedFloatBufferA[];
	float stretchedFloatBufferB[];
	private float[] activeBuffer;
	private float[] prevBuffer;
	
	
	
	protected byte[] process(byte[] sampleData, AudioFormat format){
		double factor = this._factor;
		//System.out.println(factor);
		if(floatBuffer == null || floatBuffer.length != sampleData.length/Float.BYTES){
			floatBuffer = new float[sampleData.length/(format.getSampleSizeInBits()/8)];
		}
		dac.convert(sampleData, floatBuffer);
		
		if(stretchedFloatBufferA == null || stretchedFloatBufferA.length != (int)(floatBuffer.length*factor)){
			stretchedFloatBufferA = new float[(int)(floatBuffer.length*factor)];
			System.out.println("changing buffer A length ");
		}
		if(stretchedFloatBufferB == null || stretchedFloatBufferB.length != (int)(floatBuffer.length*factor)){
			stretchedFloatBufferB = new float[(int)(floatBuffer.length*factor)];
			System.out.println("changing buffer A length ");
		}
		if(activeBuffer == null || activeBuffer == stretchedFloatBufferB){
			activeBuffer = stretchedFloatBufferA;
			prevBuffer = stretchedFloatBufferB;
		}
		else {
			activeBuffer = stretchedFloatBufferB;
			prevBuffer = stretchedFloatBufferA;
		}
		stretch(floatBuffer, activeBuffer, factor);
		merge(activeBuffer, prevBuffer, floatBuffer, factor);
		
		dac.convert(floatBuffer,sampleData);
		return sampleData.clone();
	}

	private double msPerSegment = 50;//1000./44100*10;
	
	
	

	private void stretch(float[] in, float[] out, double factor) {
		////factor = (in.length-1.)/(out.length-1.);
		for(int i = 0; i< out.length; i++){
			double x = i/factor;
			if(x+1>=in.length){
				out[i] = in[(int)x];
			} else{
				float y1 = in[(int)x];
				float y2 = in[(int)x+1];
				out[i] = (float)(y1*(1-x%1.)+y2*(x%1.));
			}
			if(factor != 1) System.out.println(i + " "+ (int)x + " " + in.length + " " + out.length + " " + out[i]);
		}
	}

	private void merge(float[] in, float[] inPrev, float[] out, double factor) {
		int inSegmentLength = (int)(format.getSampleRate()*msPerSegment/1000.);
		int outSegmentLength = (int)(format.getSampleRate()*msPerSegment/1000./factor);
		//int segmentLength = (int)(format.getSampleRate()*msPerSegment/1000.);
		for(int i = 0; i < out.length; i++){
			int ip = out.length-i-1;
			int index = in.length - 1 - ((ip/outSegmentLength)*inSegmentLength + (ip%outSegmentLength));
			
			if(index >= 0)
				out[i] = (float)in[index];
			else
				out[i] = (float)inPrev[index+inPrev.length];
			//if(i%100 == 0) System.out.println(i + " " + index + " " + out[i]);
		}
	}

	public double getShiftInCents() {
		return -Math.log(_factor)*1200/Math.log(2);
	}
	
}
