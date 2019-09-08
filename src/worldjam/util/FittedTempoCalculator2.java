package worldjam.util;

import java.util.Arrays;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;

public class FittedTempoCalculator2  implements TempoCalculator, ClockSubscriber{
	long circularBuffer[];
	int index;
	private ClockSetting currentSetting;
	public FittedTempoCalculator2(ClockSetting setting, int length){
		circularBuffer = new long[length];

		this.currentSetting = setting;
	}
	public FittedTempoCalculator2(ClockSetting setting){
		this(setting,5);
	}
	public void clear(){
		Arrays.fill(circularBuffer, 0);
	}
	
	long timeOffset = 0;
	
	/**
	 * 
	 * @param time in seconds
	 */
	public void newBeat(long time){
		index = (index+1)%circularBuffer.length;
		circularBuffer[index] = time;
		int n = 3;
		int length = circularBuffer.length;
		float sumXY = 0, sumX = 0, sumY = 0, sumX2 = 0, sumY2 = 0;
		long yref = circularBuffer[index] + timeOffset;
		double t0 = currentSetting.startTime,
				dt = currentSetting.msPerBeat;
		
		double xref = (yref-t0)/dt;
		System.out.println(yref + ", " + t0);
		for(int i = 0 ; i < n; i++){
			double y = circularBuffer[(index+length-i)%length]-yref;
			double x = xref-Math.floor(xref)-i;//-xref;
			sumXY += x*y;
			sumX += x;
			sumY += y;
			sumX2 += x*x;
			sumY2 += y*y;
			System.out.println(y + " " + x);
		}
		double m = sumXY/sumX2;
		double b = yref - xref*m;
		System.out.println(m + "   " + b);
		
		
		double chi2alt = 0;
		for(int i = 0 ; i < n; i++){
			double y = circularBuffer[(index+length-i)%length]-yref;
			double x = xref-Math.floor(xref)-i;//-xref;
			double yb = m*(x+xref)+b-yref;
			chi2alt += Math.pow(y-yb,2);
		}
		System.out.println(chi2alt);
		if(chi2alt/n < m*m/16)
			this.currentSetting = new ClockSetting((int)m,currentSetting.beatsPerMeasure, 
				currentSetting.beatDenominator, (long)b);
	}
	
	
	public ClockSetting getClockSetting(){
		return currentSetting;
	}
	@Override
	public void changeClockSettingsNow(ClockSetting clock) {
		this.currentSetting = clock;
	}
	
}
