package worldjam.audio;

import javax.sound.sampled.AudioFormat;

import worldjam.time.ClockSetting;

public interface ChannelAutomator {
	public float[] createSamples(double frameRate, ClockSetting clock);
}
