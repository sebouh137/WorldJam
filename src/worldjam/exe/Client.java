package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.audio.AudioSubscriber;
import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.audio.SampleMessage;
import worldjam.core.BeatClock;
import worldjam.gui.ClientGUI;
import worldjam.gui.ClientSetupGUI;
import worldjam.net.WJConstants;
import worldjam.util.DefaultObjects;

public class Client implements AudioSubscriber{

	ClientConnectionManager base;
	public static void main(String arg[]) throws LineUnavailableException, UnknownHostException, IOException{

		//if(arg.length >= 1 && arg[0].equals("-g")){
		ClientSetupGUI.main(arg);

	}

	Mixer inputMixer, outputMixer;

	public Client(String serverIP, int port, String sessionName, String displayName, Mixer inputMixer, Mixer outputMixer) throws LineUnavailableException, UnknownHostException, IOException{
		base = new ClientConnectionManager(serverIP, port, sessionName, displayName, this);
		this.sessionName = sessionName;
		this.displayName = displayName;
		this.inputMixer = inputMixer;
		this.outputMixer = outputMixer;

	}

	ArrayList<Connection> connections = new ArrayList();
	void addConnection(DataInputStream dis, DataOutputStream dos, boolean isServer){
		connections.add(new Connection(dis, dos, isServer));
	}

	/*private static Mixer getMixer(Scanner scanner, Class<? extends DataLine> clazz) {
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
	}*/

	private static InputThread input;

	private static PlaybackManager playback;

	//when the client receives an audio sample from the microphone,
	//it sends it out.  
	@Override
	public void sampleReceived(SampleMessage sample) {
		broadcastAudioSample(sample);

	}



	protected void processClientList(ArrayList<String> names, ArrayList<Long> ids) {
		if(playback == null)
			return;

		for(int i = 0; i< names.size(); i++){

			if(!playback.getIDs().contains(ids.get(i))){
				try {
					playback.addChannel(ids.get(i), names.get(i));
					//System.out.println("added playback thread");
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	protected void printClientConfiguration(){
		base.printClientConfiguration();
		System.out.println("  Audio configurations:");
		System.out.println("    output mixer: " + outputMixer.getMixerInfo());
		System.out.println("    input mixer: " + inputMixer.getMixerInfo());
	}

	private BeatClock beatClock;
	public BeatClock getBeatClock(){
		return beatClock;
	}
	protected void setBeatClock(BeatClock clock){
		if(playback == null){
			playback = new PlaybackManager(outputMixer, beatClock, DefaultObjects.defaultFormat);
			//System.out.println("playback initialized");
		}
		else{
			playback.setClock(beatClock);
		}
		if(input == null){
			try {
				input = new InputThread(inputMixer, DefaultObjects.defaultFormat, beatClock);
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			input.setReceiver(this);
			input.start();
		}else{
			input.setClock(beatClock);
		}



		gui = new ClientGUI(this);
		gui.setVisible(true);

	}

	void audioSampleReceived(SampleMessage sample){

		playback.sampleReceived(sample);
	}

	ClientGUI gui;

	public PlaybackManager getPlaybackManager(){
		return playback;
	}
	public InputThread getInput(){
		return input;
	}

	private class Connection extends Thread{
		Connection(DataInputStream dis, DataOutputStream dos, boolean isServer){
			this.dis = dis;
			this.dos = dos;
			this.isServer = isServer;
			new ReceiverThread().start();
			
		}
		boolean isServer;
		private DataOutputStream dos;
		private DataInputStream dis;
		
		private class ReceiverThread extends Thread{
			
			
			@SuppressWarnings("rawtypes")
			public void run(){

				try {
					while(true){
						synchronized(dis){
							byte code;
							code = dis.readByte();
							//System.out.println("received code " + (char)code);
							if(code == WJConstants.AUDIO_SAMPLE){
								int datalength = dis.readInt()-2*Long.BYTES;
								long sampleStartTime = dis.readLong();
								long senderID = dis.readLong();
								byte[] data = new byte[datalength];
								dis.readFully(data);
								SampleMessage sample = new SampleMessage();
								sample.sampleData = data;
								sample.senderID = senderID;
								sample.sampleStartTime = sampleStartTime;
								//System.out.println("received sample (" + datalength + " bytes)");
								if(playback != null)
									playback.sampleReceived(sample);
							} else if(code == WJConstants.LIST_CLIENTS){
								int N = dis.readInt();
								ArrayList<String> names = new ArrayList();
								ArrayList<Long> ids = new ArrayList();
								for(int i = 0; i<N; i++){
									names.add(dis.readUTF());
									ids.add(dis.readLong());
								}
								processClientList(names, ids);
							} else if(code == WJConstants.TIME_CHANGED){
								dis.readInt();

								int msPerBeat = dis.readInt();
								int beatsPerMeasure = dis.readInt();
								int denom = dis.readInt();
								long startTime = dis.readLong();
								BeatClock beatClock = new BeatClock(msPerBeat, beatsPerMeasure, denom, startTime);

								//System.out.println("received time information");
								setBeatClock(beatClock);
								printClientConfiguration();
							} else throw new Exception("unrecognized code");

						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}




		}
		
		private void sendAudioSample(SampleMessage sample) {
			synchronized(dos){
				try {
					dos.writeByte(WJConstants.AUDIO_SAMPLE);
					int overhead = 2*Long.BYTES;

					dos.writeInt(sample.sampleData.length+overhead);
					int start = dos.size();
					dos.writeLong(sample.sampleStartTime);
					dos.writeLong(displayName.hashCode());
					dos.write(sample.sampleData);
					if(dos.size() - start != sample.sampleData.length + overhead){
						System.out.println("oops, wrote the wrong number of bytes");
						System.exit(0);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private String displayName;
	private String sessionName; 
	public String getUserName(){
		return this.displayName;
	}

	public String getSessionName(){
		return this.sessionName;
	}
	public void joinSession() throws IOException{
		this.base.joinSession();
	}

	public void startNewSession(BeatClock clock) throws IOException{
		this.beatClock = clock;
		this.base.startNewSession(clock);
	}
	
	private enum ConnectionMode{
		DIRECT, THROUGH_SERVER;
	}
	
	ConnectionMode connectionMode = ConnectionMode.THROUGH_SERVER;
	
	public void broadcastAudioSample(SampleMessage sample){
		for(int i = 0; i< connections.size(); i++){
			if(connectionMode == ConnectionMode.DIRECT && connections.get(i).isServer)
				continue;
			connections.get(i).sendAudioSample(sample);
		}
	}

	
	
}
