package worldjam.audio;

import javax.sound.sampled.AudioFormat;

import worldjam.time.ClockSetting;

public interface LoopBuilder {
	public float[] createSamples(double frameRate, ClockSetting clock);
}
