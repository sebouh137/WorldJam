package worldjam.time;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClockSetting {
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
	
	public int getCurrentMeasure(){
		return ((int)(System.currentTimeMillis()-startTime))/(msPerBeat*beatsPerMeasure);
	}
	
	public ClockSetting(int msPerBeat, int beatsPerMeasure, int beatDenominator){
		this(msPerBeat, beatsPerMeasure, beatDenominator, (System.currentTimeMillis()/10)*10);
	}
	
	public ClockSetting(int msPerBeat, int beatsPerMeasure, int beatDenominator, long startTime){
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
	public ClockSetting createWithDifferentTempo(int newMsPerBeat, long whenEffective){
		//long t = System.currentTimeMillis();
		long newStartTime = whenEffective - (long)((newMsPerBeat/(double)msPerBeat*(whenEffective- this.startTime)));
		newStartTime = (newStartTime/10)*10;
		return new ClockSetting(newMsPerBeat, beatsPerMeasure, beatDenominator, newStartTime);
	}
	
	/**
	 * creates another instance of BeatClock with the same time signature,
	 * but a different tempo.  At the time that the create Both the original and the created 
	 * @param newMsPerBeat
	 * @return another instance of BeatClock with a different tempo
	 */
	public ClockSetting createWithDifferentTempo(int newMsPerBeat){
		return createWithDifferentTempo(newMsPerBeat, System.currentTimeMillis());
	}
	
	/**
	 * creates another instance of BeatClock with the same time signature,
	 * but a different tempo.  At the time that the create Both the original and the created 
	 * @param newMsPerBeat
	 * @return another instance of BeatClock with a different tempo
	 */
	public ClockSetting createWithDifferentBeatCount(int newBeatCount){
		return new ClockSetting(msPerBeat, newBeatCount, beatDenominator, startTime);
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
	
	public static ClockSetting readFromStream(DataInputStream dis) throws IOException{
		int msPerBeat = dis.readInt();
		int beatsPerMeasure = dis.readInt();
		int beatDenominator = dis.readInt();
		long startTime = dis.readLong();
		return new ClockSetting(msPerBeat, beatsPerMeasure, beatDenominator, startTime);
			
	}
	public String toString(){
		return beatsPerMeasure + "/" + beatDenominator + ",  " + msPerBeat + "ms per measure, offset = " + startTime;
	}
}
