package worldjam.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BeatClock {
	//should be divisible by 10
	public final long startTime;
	//should be divisible by 10, so that at 44100 samples per second, 
	// there should be an integer number of samples per beat. 
	public final int msPerBeat;  
	public final int beatsPerMeasure;
	public final int beatDenominator;
	public double getBPM(){
		return 60000./msPerBeat; 
	}
	
	public BeatClock(int msPerBeat, int beatsPerMeasure, int beatDenominator){
		this(msPerBeat, beatsPerMeasure, beatDenominator, (System.currentTimeMillis()/10)*10);
	}
	
	public BeatClock(int msPerBeat, int beatsPerMeasure, int beatDenominator, long startTime){
		this.msPerBeat = msPerBeat;
		this.beatsPerMeasure = beatsPerMeasure;
		this.beatDenominator = beatDenominator;
		this.startTime = startTime;
	}
	
	/**
	 * creates another instance of BeatClock with the same time signature,
	 * but a different tempo.  At the time that the create Both the original and the created 
	 * @param newMsPerBeat
	 * @return another instance of BeatClock with a different tempo
	 */
	public BeatClock createWithDifferentTempo(int newMsPerBeat){
		long t = System.currentTimeMillis();
		long newStartTime = t - (long)((newMsPerBeat/(double)msPerBeat*(t- this.startTime)));
		newStartTime = (newStartTime/10)*10;
		return new BeatClock(newMsPerBeat, beatsPerMeasure, beatDenominator, newStartTime);
	}
	
	/**
	 * creates another instance of BeatClock with the same time signature,
	 * but a different tempo.  At the time that the create Both the original and the created 
	 * @param newMsPerBeat
	 * @return another instance of BeatClock with a different tempo
	 */
	public BeatClock createWithDifferentBeatCount(int newBeatCount){
		return new BeatClock(msPerBeat, newBeatCount, beatDenominator, startTime);
	}
	
	
	public int getMsPerMeasure() {
		return msPerBeat*beatsPerMeasure;
	}

	public void writeToStream(DataOutputStream dos) throws IOException {
		dos.writeInt(msPerBeat);
		dos.writeInt(beatsPerMeasure);
		dos.writeInt(beatDenominator);
		dos.writeLong(startTime);
	}
	
	public static BeatClock readFromStream(DataInputStream dis) throws IOException{
		int msPerBeat = dis.readInt();
		int beatsPerMeasure = dis.readInt();
		int beatDenominator = dis.readInt();
		long startTime = dis.readLong();
		return new BeatClock(msPerBeat, beatsPerMeasure, beatDenominator, startTime);
			
	}
}
