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

public class MetronomeAndPlayback extends PseudoClient{

	private Mixer outputMixer;
	public MetronomeAndPlayback(String serverIP, String displayName, Mixer outputMixer) throws UnknownHostException, IOException {
		super(serverIP, displayName);
		this.outputMixer = outputMixer;
	}
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

		String serverIP = scanner.nextLine();
		Mixer outputMixer = getMixer(scanner, SourceDataLine.class);
		new MetronomeAndPlayback(serverIP, displayName, outputMixer);

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
				System.out.println("writing "+ sample.sampleData.length + " bytes");
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

	private static PlaybackManager playback;
	
	MetronomeThread input;
	protected void setBeatClock(BeatClock clock){
		super.setBeatClock(clock);
		playback = new PlaybackManager(outputMixer, beatClock, DefaultObjects.defaultFormat);
		System.out.println("playback initialized");
		
		input = new MetronomeThread(beatClock, DefaultObjects.defaultFormat, 440, .2);
		
		input.setReceiver(this);
		input.start();
	}
	
}
