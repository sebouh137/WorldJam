package worldjam.util;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFormat.Encoding;

import worldjam.core.BeatClock;

public class DefaultObjects {
	public static BeatClock bc0 = new BeatClock(500, 4, 4);
	public static AudioFormat defaultFormat = new AudioFormat(44100, 16, 1, true, true);
	public static AudioFormat playbackFormat = new AudioFormat(44100, 16, 2, true, true);
	//public static AudioFormat defaultFormat = new AudioFormat(Encoding.ALAW, 44100, 8, 1, 1, 44100, false);
	//public static AudioFormat playbackFormat = new AudioFormat(Encoding.ALAW, 88200, 8, 2, 2, 44100, false);
	
	
	public static int defaultPort = 2901;
	public static Mixer getInputMixer() {
		for(Mixer.Info info : AudioSystem.getMixerInfo()){
			if(AudioSystem.getMixer(info).isLineSupported(
					new DataLine.Info(TargetDataLine.class, DefaultObjects.defaultFormat)))
				return AudioSystem.getMixer(info);
		}
		return null;
	}
	public static Mixer getOutputMixer() {
		for(Mixer.Info info : AudioSystem.getMixerInfo()){
			if(AudioSystem.getMixer(info).isLineSupported(
					new DataLine.Info(SourceDataLine.class, DefaultObjects.defaultFormat)))
				return AudioSystem.getMixer(info);
		}
		return null;
	}
	
	
}
