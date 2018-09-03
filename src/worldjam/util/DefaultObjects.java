package worldjam.util;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.core.BeatClock;

public class DefaultObjects {
	public static BeatClock bc0 = new BeatClock(500, 4, 4);
	//public static Mixer inputMixer;
	//public static Mixer outputMixer;
	public static AudioFormat defaultFormat = new AudioFormat(44100, 16, 1, true, true);
	public static AudioFormat playbackFormat = new AudioFormat(44100, 16, 2, true, true);
	public static int defaultPort = 2901;
	
	
}
