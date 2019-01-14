package worldjam.test;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.swing.JFrame;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.core.BeatClock;
import worldjam.exe.Client;
import worldjam.util.DefaultObjects;

public class TwoClientTest {
	public static void main(String arg[]){
		Thread thread1 = new Thread(
				()->{
					try{
						int localPort = 2901;
						Mixer inputMixer = DefaultObjects.getInputMixer();
						Mixer outputMixer = DefaultObjects.getInputMixer();
						String displayName = "initiator";
						int msPerBeat = 300, num = 3, denom = 4;
						BeatClock clock = new BeatClock(msPerBeat, num, denom);
						InputThread input = new InputThread(inputMixer, DefaultObjects.defaultFormat, clock);
						PlaybackManager playback = new PlaybackManager(outputMixer, clock, DefaultObjects.defaultFormat);
						System.out.println("user name is " + displayName);
						Client client = new Client(localPort, displayName, input, playback, clock);
						client.getGUI().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					} catch(LineUnavailableException | IOException e){
						e.printStackTrace();

					}

				}
				);
		thread1.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Thread thread2 = new Thread(
				()->{
					try {
						int localPort = 2902;
						Mixer inputMixer = DefaultObjects.getInputMixer();
						Mixer outputMixer = DefaultObjects.getInputMixer();
						String displayName = "joiner";
						InputThread input = new InputThread(inputMixer, DefaultObjects.defaultFormat, DefaultObjects.bc0);
						BeatClock clock = DefaultObjects.bc0;
						PlaybackManager playback = new PlaybackManager(outputMixer, clock, DefaultObjects.defaultFormat);
						Client client;

						client = new Client(localPort, displayName, input, playback, clock);
						String serverIP = "127.0.0.1";
						int peerPort = 2901;
						client.joinSessionP2P(serverIP, peerPort);
						client.getGUI().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					} catch (LineUnavailableException | IOException e) {
						e.printStackTrace();
					}
				}
				);
		thread2.start();

	}
}
