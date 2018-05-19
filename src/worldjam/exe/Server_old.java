package worldjam.exe;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.core.BeatClock;
import worldjam.test.DefaultObjects;

public class Server_old {
	private static BeatClock beatClock;


	public static void main(String arg[]) throws LineUnavailableException, IOException{
		Scanner scanner = new Scanner(System.in);
		System.out.println("Type 'new' to start a new jam session or 'join' to join an existing jam session");
		boolean isAdmin = false;
		while(true){
			String input = scanner.nextLine();
			if(input.equalsIgnoreCase("new")){
				isAdmin=true;
				break;
			}
			else if(input.equalsIgnoreCase("join")){
				isAdmin=false;
				break;
			} else {
				System.out.println("Type 'new' to start a new jam session or 'join' to join an existing jam session");
			}
		}

		try {
            InetAddress ipAddr = InetAddress.getLocalHost();
            localIP = ipAddr.getHostAddress();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
		
		if(isAdmin) {
			beatClock = configureClock(scanner);
			
		} else{
			joinExistingSession(scanner);
		}

		printConfiguration();
		
		Mixer outputMixer = getMixer(scanner, SourceDataLine.class);
		playback = new PlaybackManager(outputMixer, beatClock, DefaultObjects.defaultFormat);
		Mixer inputMixer = getMixer(scanner, TargetDataLine.class);
		input = new InputThread(inputMixer, DefaultObjects.defaultFormat, beatClock);
		
		if(isAdmin){
			startNewSession();
		}
	}
	
	private static void joinExistingSession(Scanner scanner) {
		System.out.println("What is this jam session's admin's IP address?");
		String adminIP = scanner.nextLine();
		
		//TODO write this method.  
	}
	
	private static void startNewSession() throws IOException {
		ServerSocket socket = new ServerSocket();
		while(true){
		socket.accept();
		}
	}
	private static Mixer getMixer(Scanner scanner, Class<? extends DataLine> clazz) {
		ArrayList<Mixer> availableMixers = new ArrayList();
		for(Mixer.Info info : AudioSystem.getMixerInfo()){
			if(AudioSystem.getMixer(info).isLineSupported(
					new DataLine.Info(clazz, DefaultObjects.defaultFormat)))
				availableMixers.add(AudioSystem.getMixer(info));
		}
		System.out.println("Select output device");
		int i = 0;
		for(Mixer mixer : availableMixers){
			System.out.println("(" + i++ + ") " +  mixer.getMixerInfo());
		}
		
		return availableMixers.get(scanner.nextInt());
	}
	static String localIP; 
	static PlaybackManager playback;
	
	
	private static void printConfiguration() {
		System.out.println("Jam session started with the following configuration:");
		System.out.println("BPM: " + 60000/beatClock.msPerBeat);
		System.out.println("Time Signature: " + beatClock.beatsPerMeasure + "/" + beatClock.beatDenominator);
		
		System.out.println("Local IP:  " + localIP);
	}


	static BeatClock configureClock(Scanner scanner){
		System.out.println("How many beats per minute? (this will be rounded to an integer number of tens of milliseconds"
				+ " per beat)");
		double bpm = Double.parseDouble(scanner.nextLine());
		int msPerBeat = (int)(60000/bpm);
		msPerBeat = (msPerBeat/10)*10;
		
		System.out.println("Time signature?  (for instance, type '3/4' or '4/4')");
		
		String sig[] = scanner.nextLine().split("[ /\t]+");
		int num = Integer.parseInt(sig[0]);
		int denom = Integer.parseInt(sig[1]);
		
		return new BeatClock(msPerBeat, num, denom);
		
	}

	private static InputThread input;
}
