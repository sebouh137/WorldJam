package worldjam.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

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

	public BeatClock createWithDifferentTempo(int newMsPerBeat){
		long t = System.currentTimeMillis();
		long newStartTime = t - (long)((newMsPerBeat/(double)msPerBeat*(t- this.startTime)));
		newStartTime = (newStartTime/10)*10;
		return new BeatClock(newMsPerBeat, beatsPerMeasure, beatDenominator, newStartTime);
	}
	
	public BeatClock createWithDifferentBeatCount(int newBeatCount){
		return new BeatClock(msPerBeat, newBeatCount, beatDenominator, startTime);
	}
	
	
	public int getMsPerMeasure() {
		return msPerBeat*beatsPerMeasure;
	}
	
}
