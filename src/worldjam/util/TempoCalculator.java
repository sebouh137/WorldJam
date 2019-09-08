package worldjam.util;

import java.util.Arrays;

import worldjam.time.ClockSetting;

public interface TempoCalculator {
	
	public void clear();
	
	/**
	 * 
	 * @param time in microseconds
	 */
	public void newBeat(long time);
	
	public ClockSetting getClockSetting();
}
