package worldjam.test;

import javax.sound.sampled.LineUnavailableException;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.audio.PlaybackThread;
import worldjam.test.generators.DroneThread;
import worldjam.test.generators.LoopThread;
import worldjam.test.generators.MetronomeThread;
import worldjam.util.DefaultObjects;

public class TestDefault {
	public static void main(String arg[]) throws LineUnavailableException{
		PlaybackManager playback = new PlaybackManager(
				DefaultObjects.outputMixer, 
				DefaultObjects.bc0,
				DefaultObjects.defaultFormat);
		
		/*InputThread input = new InputThread(
				DefaultObjects.inputMixer, 
				DefaultObjects.defaultFormat, 
				DefaultObjects.bc0, 
				playback);*/
		LoopThread drone = new DroneThread(DefaultObjects.bc0, DefaultObjects.defaultFormat, 440*2/3, .2);
		LoopThread metronome = new MetronomeThread(DefaultObjects.bc0, DefaultObjects.defaultFormat, 440, .1);
		
		playback.addThread(drone.getSenderID(), "drone");
		playback.addThread(metronome.getSenderID(), "metronome");
		
		metronome.setReceiver(playback);
		drone.setReceiver(playback);
		//playback.start();
		drone.start();
		metronome.start();
		//input.start();
		playback.printControls();
	}
}
