package worldjam.test;

import javax.sound.sampled.LineUnavailableException;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.audio.PlaybackThread;
import worldjam.test.generators.DroneThread;
import worldjam.test.generators.LoopThread;
import worldjam.test.generators.MetronomeThread;
import worldjam.util.DefaultObjects;

public class Test2 {
	public static void main(String arg[]) throws LineUnavailableException{
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PlaybackManager playback = new PlaybackManager(
				DefaultObjects.outputMixer, 
				DefaultObjects.bc0,
				DefaultObjects.defaultFormat);
		
		InputThread input = new InputThread(
				DefaultObjects.inputMixer, 
				DefaultObjects.defaultFormat, 
				DefaultObjects.bc0);
		playback.addThread(input.getSenderID(), "noname");
		
		//metronome.setReceiver(playback);
		input.setReceiver(playback);
		//playback.start();
		//drone.start();
		//metronome.start();
		input.start();
	}
}
