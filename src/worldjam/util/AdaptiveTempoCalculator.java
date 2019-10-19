package worldjam.util;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;

public class AdaptiveTempoCalculator implements TempoCalculator, ClockSubscriber{

	int transitionDelay = 0;
	double changePerBeatTolerance = 0.55;
	double mixParameter = 0.1;
	
	public AdaptiveTempoCalculator(ClockSetting currentSetting){
		this.currentSetting = currentSetting;
	}
	
	public AdaptiveTempoCalculator(){
		this(DefaultObjects.bc0);
	}
	ClockSetting currentSetting;
	private long transitionTime;
	
	int Nprev = 0;
	
	@Override
	public void newBeat(long time) {
		

		long t0 = currentSetting.startTime;
		long dt = currentSetting.msPerBeat;
		
		double currentBeatNumberFloat = (time-t0)/dt;  
		int N = (int) Math.round(currentBeatNumberFloat);
		if(N== Nprev)
			N+=1;
		if (Math.abs(currentBeatNumberFloat - N)>changePerBeatTolerance)
			return;
		System.out.println("new beat");

		long prevBeat = t0+(N-1)*dt;
		long tTrans = time + transitionDelay;
		long tProj = t0+N*dt;
		long tMix = (long)(mixParameter*(time-t0)+(1-mixParameter)*(tProj-t0)) +t0;
		this.transitionTime = tTrans;
		
		
		int new_msPerBeat =  (int)((tMix-tTrans) *dt
				/(tProj - tTrans));
		long newStart = t0-(t0-tTrans)*(tProj-tMix)
				/(tProj - tTrans);
		System.out.println("new beat " + new_msPerBeat + "  " + newStart + " " + N);
		
		System.out.println(new_msPerBeat*(N-1)+newStart-(dt*(N-1)+t0));
		System.out.println(time-(N*new_msPerBeat+newStart));

		System.out.println((time-prevBeat)/new_msPerBeat);
		
		
		this.currentSetting = new ClockSetting(new_msPerBeat,currentSetting.beatsPerMeasure, 
				currentSetting.beatDenominator, newStart);
		Nprev = N;
	}
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ClockSetting getClockSetting() {
		// TODO Auto-generated method stub
		return currentSetting;
	}

	@Override
	public void changeClockSettingsNow(ClockSetting clock) {
		this.currentSetting = clock;
	}

}
