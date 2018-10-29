package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.audio.AudioSample;
import worldjam.core.BeatClock;
import worldjam.gui.ClientGUI;
import worldjam.gui.ClientSetupGUI;
import worldjam.net.WJConstants;
import worldjam.util.DefaultObjects;

public class Client {

	private ClientConnectionManager base;
	public static void main(String arg[]) throws LineUnavailableException, UnknownHostException, IOException{

		//if(arg.length >= 1 && arg[0].equals("-g")){
		ClientSetupGUI.main(arg);

	}

	private Mixer inputMixer, outputMixer;

	private boolean debug;
	public void setDebug(boolean debug){
		this.debug = debug;
	}
	public Client(String serverIP, int port, String sessionName, String displayName, Mixer inputMixer, Mixer outputMixer) throws LineUnavailableException, UnknownHostException, IOException{
		this.selfDescriptor = new ClientDescriptor(displayName, displayName.hashCode());
		base = new ClientConnectionManager(serverIP, port, sessionName, displayName, this);
		this.sessionName = sessionName;
		this.displayName = displayName;
		this.inputMixer = inputMixer;
		this.outputMixer = outputMixer;
		

	}

	ArrayList<Connection> connections = new ArrayList<Connection>();
	void addConnection(DataInputStream dis, DataOutputStream dos, boolean isServer){
		connections.add(new Connection(dis, dos, isServer));
	}

	private InputThread input;

	private PlaybackManager playback;


	void processClientList(ArrayList<ClientDescriptor> descriptors) {
		if(playback == null)
			return;

		for(ClientDescriptor descriptor : descriptors){

			if(!playback.getIDs().contains(descriptor.clientID)){
				try {
					playback.addChannel(descriptor.clientID, descriptor.displayName);
					//System.out.println("added playback thread");
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	void printClientConfiguration(){
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
		if(clock == null)
			throw new NullPointerException();
		this.beatClock = clock;
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
			input.setReceiver(sample -> {this.broadcastAudioSample(sample);});
			input.start();
		}else{
			input.setClock(beatClock);
		}
		if(gui == null){
			gui = new ClientGUI(this);
			gui.setVisible(true);
		}
	}

	private ClientGUI gui;

	public PlaybackManager getPlaybackManager(){
		return playback;
	}
	public InputThread getInput(){
		return input;
	}
	
	/**
	 * represents a connection either to a relay server or another peer in a p2p system  
	 * @author spaul
	 *
	 */
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
								AudioSample sample = AudioSample.readFromStream(dis);
								//System.out.println("received sample (" + datalength + " bytes)");
								if(playback != null)
									playback.sampleReceived(sample);
							} else if(code == WJConstants.LIST_CLIENTS){
								int N = dis.readInt();
								ArrayList<ClientDescriptor> clientList = new ArrayList();
								for(int i = 0; i<N; i++){
									ClientDescriptor descriptor = ClientDescriptor.readFromStream(dis);
									clientList.add(descriptor);
								}
								processClientList(clientList);
							} else if(code == WJConstants.TIME_CHANGED){
								dis.readInt();

								//int msPerBeat = dis.readInt();
								//int beatsPerMeasure = dis.readInt();
								//int denom = dis.readInt();
								//long startTime = dis.readLong();
								//BeatClock beatClock = new BeatClock(msPerBeat, beatsPerMeasure, denom, startTime);
								BeatClock beatClock = BeatClock.readFromStream(dis); 
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
		
		private void sendAudioSample(AudioSample sample) {
			synchronized(dos){
				try {
					//change the sourceID to the client's id number
					sample.sourceID = selfDescriptor.clientID;
					dos.writeByte(WJConstants.AUDIO_SAMPLE);
					sample.writeToStream(dos);
//					int overhead = 2*Long.BYTES;
//
//					dos.writeInt(sample.sampleData.length+overhead);
//					int start = dos.size();
//					dos.writeLong(sample.sampleStartTime);
//					dos.writeLong(displayName.hashCode());
//					dos.write(sample.sampleData);
//					if(dos.size() - start != sample.sampleData.length + overhead){
//						System.out.println("oops, wrote the wrong number of bytes");
//						System.exit(0);
//					}
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
		this.setBeatClock(clock);
		this.base.startNewSession(clock);
	}
	
	private enum ConnectionMode{
		DIRECT, THROUGH_SERVER;
	}
	
	ConnectionMode connectionMode = ConnectionMode.THROUGH_SERVER;
	
	public void broadcastAudioSample(AudioSample sample){
		if(debug)
			System.out.println(this.getUserName()+": sending audio message at " + new Date());
		for(int i = 0; i< connections.size(); i++){
			if(connectionMode == ConnectionMode.DIRECT && connections.get(i).isServer)
				continue;
			connections.get(i).sendAudioSample(sample);
		}
	}
	
	ClientDescriptor selfDescriptor;

	public ClientDescriptor getDescriptor() {
		// TODO Auto-generated method stub
		return this.selfDescriptor;
	}
	
}
