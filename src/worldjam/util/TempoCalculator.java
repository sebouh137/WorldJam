
package worldjam.util;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;

public interface TempoCalculator extends ClockSubscriber{
	
	public void clear();
	
	/**
	 * 
	 * @param time in microseconds
	 */
	public void newBeat(long time);
	
	public ClockSetting getClockSetting();
}