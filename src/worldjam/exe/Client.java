package worldjam.exe;

import java.io.IOException;
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
import worldjam.audio.SampleMessage;
import worldjam.core.BeatClock;
import worldjam.net.WJConstants;
import worldjam.test.DefaultObjects;
import worldjam.test.generators.MetronomeThread;

public class Client extends PseudoClient{

	public static void main(String arg[]) throws LineUnavailableException, UnknownHostException, IOException{
		Scanner scanner = new Scanner(System.in);
		boolean isAdmin = false;
		while(true){
			System.out.println("Type 'new' to start a new jam session or 'join' to join an existing jam session");
			String input = scanner.nextLine();
			if(input.equalsIgnoreCase("new")){
				isAdmin=true;
				break;
			}
			else if(input.equalsIgnoreCase("join")){
				isAdmin=false;
				break;
			} 
		}
		System.out.println("Enter your displayname:");
		String displayName = scanner.nextLine();
		System.out.println("Enter the IP address of the server:");

		String serverIP = scanner.nextLine();
		Mixer outputMixer = getMixer(scanner, SourceDataLine.class);
		Mixer inputMixer = getMixer(scanner, TargetDataLine.class);
		new Client(serverIP, displayName, inputMixer, outputMixer);

	}

	Mixer inputMixer, outputMixer;

	public Client(String serverIP, String displayName, Mixer inputMixer, Mixer outputMixer) throws LineUnavailableException, UnknownHostException, IOException{
		super(serverIP, displayName);
		this.inputMixer = inputMixer;
		this.outputMixer = outputMixer;
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

	private static InputThread input;

	private static PlaybackManager playback;

	@Override
	public void sampleReceived(SampleMessage sample) {
		synchronized(this){
			try {
				dos.writeByte(WJConstants.AUDIO_SAMPLE);
				int overhead = 2*Long.BYTES;
				
				dos.writeInt(sample.sampleData.length+overhead);
				int start = dos.size();
				dos.writeLong(sample.sampleStartTime);
				dos.writeLong(clientID);
				dos.write(sample.sampleData);
				if(dos.size() - start != sample.sampleData.length + overhead){
					System.out.println("oops, wrote the wrong number of bytes");
					System.exit(0);
				}
				//System.out.println("writing "+ sample.sampleData.length + " bytes");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	@Override
	protected void processClientList(ArrayList<String> names, ArrayList<Long> ids) {
		if(playback == null)
			return;
		for(long l : ids){
			
			if(!playback.getIDs().contains(l)){
				try {
					playback.addThread(l);
					System.out.println("added playback thread");
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	protected void printClientConfiguration(){
		super.printClientConfiguration();
		System.out.println("  Audio configurations:");
		System.out.println("    output mixer: " + outputMixer.getMixerInfo());
		System.out.println("    input mixer: " + inputMixer.getMixerInfo());
	}

	protected void setBeatClock(BeatClock clock){
		super.setBeatClock(clock);
		playback = new PlaybackManager(outputMixer, beatClock, DefaultObjects.defaultFormat);
		System.out.println("playback initialized");
		try {
			input = new InputThread(inputMixer, DefaultObjects.defaultFormat, beatClock);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		input.setReceiver(this);
		input.start();
		this.subs = playback;
		MetronomeThread metronome = new MetronomeThread(clock, DefaultObjects.defaultFormat, 440, .1);
		metronome.setReceiver(playback);
		try {
			playback.addThread(metronome.getSenderID());
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		metronome.start();
		
	}



}
