package worldjam.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class BeatClock implements Externalizable, Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2908773893271885882L;
	//should be divisible by 10
	public long startTime;
	//should be divisible by 10, so that at 44100 samples per second, 
	// there should be an integer number of samples per beat. 
	public int msPerBeat;  
	public int beatsPerMeasure;
	public int beatDenominator;
	public double getBPM(){
		return 60000./msPerBeat; 
	}
	
	public BeatClock(int msPerBeat, int beatsPerMeasure, int beatDenominator){
		this.msPerBeat = msPerBeat;
		this.beatsPerMeasure = beatsPerMeasure;
		this.beatDenominator = beatDenominator;
		this.startTime = (System.currentTimeMillis()/10)*10;
	}

	public BeatClock() {
		// TODO Auto-generated constructor stub
	}

	public void startClock(){
		startTime = (System.currentTimeMillis()/10)*10;
	}
	
	public int getMsPerMeasure() {
		return msPerBeat*beatsPerMeasure;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {

		out.writeInt(msPerBeat);
		out.writeInt(beatsPerMeasure);
		out.writeInt(beatDenominator);
		out.writeLong(startTime);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		msPerBeat = in.readInt();
		beatsPerMeasure = in.readInt();
		beatDenominator = in.readInt();
		startTime = in.readLong();
	}
	
	@Override
	public Object clone(){
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
