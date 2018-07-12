package worldjam.filters.pitchshift;


import javax.sound.sampled.AudioFormat;

import worldjam.audio.AudioFilter;

/**
 * Based on the Waveform Similarity Overlay (WFSO) algorithm.
 * Stretches a signal by a pitch-shift factor, cuts it into segments,
 * and then merges it with some segment-overlap to fit the original signal's duration, 
 * offsetting each segment by some amount depending on the similarity between the 
 * head of the segment and the tail of the previous segment
 * So far, I am only supporting stereo input.  
 * @author spaul
 *
 */
public class WfsoPitchShift extends AudioFilter{

	private double _factor;

	public WfsoPitchShift(AudioFormat format, int shiftInCents){
		super(format);
		setShiftInCents(shiftInCents);
	}

	public WfsoPitchShift(AudioFormat format, double factor){
		super(format);
		setStretchFactor(factor);	
	}

	private void setStretchFactor(double factor) {
		this._factor = factor;
	}

	public void setShiftInCents(int shiftInCents) {
		this.setStretchFactor(Math.pow(2,  -shiftInCents/1200.));
	}

	private float floatBuffer[];
	private float stretchedFloatBuffer[];


	private int _headroom;
	protected byte[] process(byte[] sampleData, AudioFormat format){
		double factor = this._factor;
		double msPerSegment = this._msPerSegment;
		double overlapMS = this._overlapMS;
		double searchMS = this._searchMS;
		int headroom = this._headroom;
		
		//if(factor != 1)
			//System.out.println(factor);
		if(floatBuffer == null || floatBuffer.length != sampleData.length/Float.BYTES){
			floatBuffer = new float[sampleData.length/(format.getSampleSizeInBits()/8)];
		}
		dac.convert(sampleData, floatBuffer);

		int segmentLength = (int)(format.getSampleRate()*msPerSegment/1000.);
		int stretchedSegmentLength = (int)(format.getSampleRate()*msPerSegment/1000.*factor);
		int overlapLength = (int)(format.getSampleRate()*overlapMS/1000.);
		int searchLength = (int)(format.getSampleRate()*searchMS/1000.);
		
		if(stretchedFloatBuffer == null || stretchedFloatBuffer.length != (int)(floatBuffer.length*factor)+ headroom){
			headroom = 2*Math.max(segmentLength, stretchedSegmentLength);
			this._headroom = headroom;
			stretchedFloatBuffer = new float[(int)(floatBuffer.length*factor)+ headroom];
			System.out.println("changing buffer A length to " + stretchedFloatBuffer.length);
		}


		System.arraycopy(stretchedFloatBuffer, stretchedFloatBuffer.length-headroom, stretchedFloatBuffer, 0, headroom);
		stretch(floatBuffer, stretchedFloatBuffer, factor, headroom);


		merge(stretchedFloatBuffer, floatBuffer, stretchedSegmentLength, segmentLength, overlapLength, searchLength, headroom);

		dac.convert(floatBuffer,sampleData);
		return sampleData.clone();
	}

	private double _msPerSegment = 50;
	private double _overlapMS = 10;
	private double _searchMS = 15;

	public void setMsPerSearch(double searchMS){
		this._searchMS = searchMS;
	}
	
	public void setMsPerSegment(double msPerSegment){
		this._msPerSegment = msPerSegment;
	}
	public void setMsPerOverlap(double overlapMS){
		this._overlapMS = overlapMS;
	}

	public double getMsPerOverlap(){
		return this._overlapMS;
	}

	public double getMsPerSegment(){
		return this._msPerSegment;
	}
	
	public double getMsPerSearch(){
		return this._searchMS;
	}

	private void stretch(float[] in, float[] stretched, double factor, int headroom) {
		////factor = (in.length-1.)/(out.length-1.);
		for(int i = 0; i< stretched.length-headroom; i++){
			double x = i/factor;
			if(x+1>=in.length){
				float val = in[in.length-1];
				stretched[i+headroom] = val;
			} else{
				float y1 = in[(int)x];
				float y2 = in[(int)x+1];
				stretched[i+headroom] = (float)(y1*(1-x%1.)+y2*(x%1.));
			}
			//if(factor != 1 && (i%1000 == 0)) System.out.println(i + " "+ (int)x + " " + in.length + " " + (stretched.length-headroom) + " " + stretched[i+headroom] + " " + factor);
		}
	}

	private void merge(float[] stretched, float[] merged, int stretchedSegmentLength, int outSegmentLength, int overlapLength, int searchLength, int headroom) {

		//int segmentLength = (int)(format.getSampleRate()*msPerSegment/1000.);
		int nSegments = (int)Math.ceil(merged.length/outSegmentLength);

		int offset = 0;
		int prevOffset = 0;
		for(int i = 0; i< nSegments; i++){
			
			int kBest = 0;
			double corrBest = 0;
			for(int k = 0; k< searchLength; k++){
				double corr = 0;
				for(int j = 0; j<overlapLength; j++){
					int index1 = i*stretchedSegmentLength+j-k;
					int index2 = i*stretchedSegmentLength+j + (outSegmentLength - stretchedSegmentLength)+prevOffset;
					corr += stretched[index1+headroom]*stretched[index2+headroom];
				}
				if(corr > corrBest){
					kBest = k;
					corrBest = corr;
				}
			}
			offset = -kBest;
			
			for(int j = 0; j<outSegmentLength; j++){
				int inIndex = i*stretchedSegmentLength+j;
				if(stretchedSegmentLength < outSegmentLength)
					inIndex += -outSegmentLength+stretchedSegmentLength;
				int outIndex = i*outSegmentLength+j;
				
				//if(factor != 1)
					//System.out.println(i + " " + stretchedSegmentLength + " " + j + " " + inIndex + " " + headroom + " " + stretched.length + " " + nSegments);
				//if(inIndex >= stretched.length -headroom)
					//continue;
				float val = stretched[inIndex + headroom+ offset];
				int altInIndex = 0;
				if(j<overlapLength){
					altInIndex = inIndex + (outSegmentLength - stretchedSegmentLength);
					float val2 = stretched[altInIndex + headroom + prevOffset];
					val = (val*j+val2*(overlapLength-j))/overlapLength;
				}
				//if(outIndex<merged.length){
					merged[outIndex] = val;
					if(stretchedSegmentLength != outSegmentLength && outIndex % 100 ==0) 
						System.out.println(inIndex + " "
							+ altInIndex + " " + outIndex + " " + val + " " + overlapLength + " " + outSegmentLength + " " + searchLength + " " + offset + " " + prevOffset + " " + j);
				//}
			}
			prevOffset = offset; 
		}
	}

	public double getShiftInCents() {
		return -Math.log(_factor)*1200/Math.log(2);
	}

}
