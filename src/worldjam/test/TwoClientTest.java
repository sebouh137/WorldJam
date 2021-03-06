package worldjam.test;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.exe.Client;
import worldjam.time.ClockSetting;
import worldjam.util.DefaultObjects;
import worldjam.video.WebcamInterface;

public class TwoClientTest {

	static boolean useWebcam = false;
	public static void main(String arg[]){
		for (String ar : arg) {
			if(ar.equals("-w"))
				useWebcam=true;
		}
		boolean joinerHasMicrophone = true;
		int msPerBeat = 750, num = 3, denom = 4;
		ClockSetting clock = new ClockSetting(msPerBeat, num, denom);
		
		Thread thread1 = new Thread(
				()->{
					try{
						int localPort = 2901;
						Mixer inputMixer = DefaultObjects.getInputMixer();
						Mixer outputMixer = DefaultObjects.getInputMixer();
						String displayName = "initiator";
						
						
						InputThread input;
						if(!joinerHasMicrophone){
							input = new InputThread(inputMixer, DefaultObjects.defaultFormat, clock);
						}
						else 
							input = null;
						
						PlaybackManager playback = new PlaybackManager(outputMixer, clock, DefaultObjects.defaultFormat);
						System.out.println("user name is " + displayName);
						Client client = new Client(localPort, displayName, input, playback, clock,null);
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
		Thread joiner = new Thread(
				()->{
					try {
						int localPort = 2902;
						Mixer inputMixer = DefaultObjects.getInputMixer();
						Mixer outputMixer = DefaultObjects.getInputMixer();
						String displayName = "joiner";
						ClockSetting defaultClock = DefaultObjects.bc0;
						InputThread input;
						if(joinerHasMicrophone)
							input = new InputThread(inputMixer, DefaultObjects.defaultFormat, defaultClock);
						else 
							input = null;
						PlaybackManager playback = new PlaybackManager(outputMixer, defaultClock, DefaultObjects.defaultFormat);
						Client client;
						WebcamInterface webcamThread = null;
						if(useWebcam) {
							Webcam webcam = Webcam.getDefault();
							webcam.open(true);
							webcamThread = new WebcamInterface(webcam);
						}
						client = new Client(localPort, displayName, input, playback, defaultClock,webcamThread);
						String serverIP = "127.0.0.1";
						int peerPort = 2901;
						client.joinSessionP2P(serverIP+":"+ peerPort);
						client.getGUI().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					} catch (LineUnavailableException | IOException e) {
						e.printStackTrace();
					}
				}
				);
		joiner.start();

	}
}
