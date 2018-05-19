package worldjam.core;

public class BeatClock {
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

	public void startClock(){
		startTime = (System.currentTimeMillis()/10)*10;
	}
	
	public int getMsPerMeasure() {
		// TODO Auto-generated method stub
		return msPerBeat*beatsPerMeasure;
	}
	
}
