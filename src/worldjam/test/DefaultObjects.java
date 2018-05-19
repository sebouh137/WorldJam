package worldjam.test;

import java.net.SocketImpl;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.core.BeatClock;

public class DefaultObjects {
	public static BeatClock bc0 = new BeatClock(500, 4, 4);
	public static Mixer inputMixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[1]);
	public static Mixer outputMixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[2]);
	public static AudioFormat defaultFormat = new AudioFormat(44100, 16, 2, true, true);
	public static int defaultPort = 2901;
	static {
		try {
			inputMixer.open();
			outputMixer.open();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
