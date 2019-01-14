package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.audio.AudioSample;
import worldjam.audio.AudioSubscriber;
import worldjam.core.BeatClock;
import worldjam.gui.ClientGUI;
import worldjam.gui.ClientSetupGUI;
import worldjam.gui.ClientSetupGUI_P2P;
import worldjam.net.WJConstants;
import worldjam.util.DefaultObjects;

public class Client {

	private ClientConnectionManager base;
	public static void main(String arg[]) throws LineUnavailableException, UnknownHostException, IOException{

		//if(arg.length >= 1 && arg[0].equals("-g")){
		ClientSetupGUI_P2P.main(arg);

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

	public Client(int listeningPort, String displayName, InputThread input, PlaybackManager playback, BeatClock clock) 
			throws LineUnavailableException, UnknownHostException, IOException{
		this.selfDescriptor = new ClientDescriptor(displayName, displayName.hashCode());
		this.displayName = displayName;
		//setup the playback
		if(playback != null){
			this.playback = playback;
			reactors.add(playback);
		}
		//now setup the input
		this.input = input;
		input.addSubscriber(sample -> {this.broadcastAudioSample(sample);});
		input.start();
		if(playback != null){
			try {
				input.setSenderID(this.selfDescriptor.clientID);
				playback.addChannel(input.getSenderID(), this.selfDescriptor.displayName);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			input.addSubscriber(playback);
		}
		new ListenForConnectionsRequestThread(listeningPort).start();
		this.setBeatClock(clock);
	}

	Map<Long,Connection> connections = new HashMap<Long,Connection>();
	void addConnection(long id, String name, Socket socket, DataInputStream dis, DataOutputStream dos, boolean isServer){
		try {
			if(playback != null)
				playback.addChannel(id, name);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		connections.put(id, new Connection(socket, dis, dos, isServer));
	}

	private void removeConnection(long id){
		connections.remove(id);
	}

	private BeatClock beatClock;

	private ClientGUI gui;

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
					e.printStackTrace();
				}
			}
		}
	}



	public BeatClock getBeatClock(){
		return beatClock;
	}

	public void setBeatClock(BeatClock clock){
		if(clock == null)
			throw new NullPointerException();
		this.beatClock = clock;
		if(playback == null){
			playback = new PlaybackManager(outputMixer, beatClock, DefaultObjects.defaultFormat);
			reactors.add(playback);
			//System.out.println("playback initialized");
		}
		else{
			playback.setClock(beatClock);
		}
		if(input == null && inputMixer != null){
			try {
				input = new InputThread(inputMixer, DefaultObjects.defaultFormat, beatClock);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			input.addSubscriber(sample -> {this.broadcastAudioSample(sample);});
			input.start();
			try {
				playback.addChannel(input.getSenderID(), this.selfDescriptor.displayName);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			input.addSubscriber(playback);
		}else{
			if(input != null) input.setClock(beatClock);
		}
		if(gui == null){
			gui = new ClientGUI(this);
			gui.setVisible(true);
		}
		gui.setClock(clock);
	}

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
	private class Connection{

		Connection(Socket socket, DataInputStream dis, DataOutputStream dos, boolean isToServer){
			this.dis = dis;
			this.dos = dos;
			this.isToServer = isToServer;
			new ReceiverThread().start();
			//new ImAliveThread().start();
		}
		
		boolean alive;
		boolean isToServer;
		private DataOutputStream dos;
		private DataInputStream dis;
		private Socket socket;

		private class ReceiverThread extends Thread{


			@SuppressWarnings("rawtypes")
			public void run(){

				try {
					while(true){
						synchronized(dis){
							byte code;
							code = dis.readByte();
							if(code == WJConstants.AUDIO_SAMPLE){								
								AudioSample sample = AudioSample.readFromStream(dis);
								for(AudioSubscriber reactor: reactors){
									reactor.sampleReceived(sample);

								}
							} else if(code == WJConstants.LIST_CLIENTS){
								int N = dis.readInt();
								ArrayList<ClientDescriptor> clientList = new ArrayList<ClientDescriptor>();
								for(int i = 0; i<N; i++){
									ClientDescriptor descriptor = ClientDescriptor.readFromStream(dis);
									clientList.add(descriptor);
								}
								processClientList(clientList);
							} else if(code == WJConstants.TIME_CHANGED){
								//dis.readInt();
								BeatClock beatClock = BeatClock.readFromStream(dis);
								System.out.println(displayName + ": received clock " + beatClock);
								setBeatClock(beatClock);
							} else throw new Exception("unrecognized code");

						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		/*private class ImAliveThread extends Thread{
			public void run(){
				while(alive){
					Thread.sleep(1000);
					synchronized(dos){
						dos.writeByte(WJConstants.IM_ALIVE);
						new ImAliveMessage().writeToStream(dos);
					}
				}

			}
		}*/
		private void sendAudioSample(AudioSample sample) {
			synchronized(dos){
				try {
					//change the sourceID to the client's id number
					sample.sourceID = selfDescriptor.clientID;
					dos.writeByte(WJConstants.AUDIO_SAMPLE);
					sample.writeToStream(dos);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void close() throws IOException {
			socket.close();
		}
	}
	//list of objects that react to new audio samples received
	ArrayList<AudioSubscriber> reactors = new ArrayList();

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
		for(Connection connection : connections.values()){
			if(connectionMode == ConnectionMode.DIRECT && connection.isToServer)
				continue;
			connection.sendAudioSample(sample);
		}
	}

	ClientDescriptor selfDescriptor;

	public ClientDescriptor getDescriptor() {
		return this.selfDescriptor;
	}


	public class CheckForTimeoutThread extends Thread implements AudioSubscriber {
		//number of ms to wait between checks
		int sleepTime = 500;
		//max amount of time between the (recorded) start of the sample
		//and the current time.  
		int maxDiff = 10000;

		public void run(){
			while(true){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long now = System.currentTimeMillis();
				for(Map.Entry<Long,Long> entry : lastTimestamps.entrySet()){
					long id = entry.getKey();
					long timestamp = entry.getValue();
					if(now-timestamp>maxDiff){
						removeConnection(id);
					}
				}
			}
		}

		Map<Long, Long> lastTimestamps = new HashMap<Long, Long>();
		@Override
		public void sampleReceived(AudioSample sample) {
			lastTimestamps.put(sample.sourceID, sample.sampleStartTime);
		}
	}

	public class ListenForConnectionsRequestThread extends Thread {
		int port;
		ListenForConnectionsRequestThread(int port){
			this.port = port;
		}
		public void run(){
			try {
				ServerSocket serverSocket = new ServerSocket(port);
				while(true){
					Socket socket = serverSocket.accept();
					System.out.println("socket accepted");
					
					DataInputStream dis = new DataInputStream(socket.getInputStream());
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					if(dis.readByte() == WJConstants.COMMAND_JOIN){
						ClientDescriptor peer = ClientDescriptor.readFromStream(dis);
						System.out.println(displayName + ":  received join request from peer " + peer.displayName);
						synchronized(dos){
							dos.writeByte(WJConstants.COMMAND_JOIN_RECEIVED);
							selfDescriptor.writeToStream(dos);
							dos.writeByte(WJConstants.TIME_CHANGED);
							beatClock.writeToStream(dos);
						}
						addConnection(peer.clientID, peer.displayName, socket, dis, dos, false);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void joinSessionP2P(String peerIpAddress, int port) throws UnknownHostException, IOException{
		Socket socket = new Socket(peerIpAddress, port);
		System.out.println("socket connected");
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		dos.writeByte(WJConstants.COMMAND_JOIN);
		selfDescriptor.writeToStream(dos);
		dis.readByte();
		ClientDescriptor peerDescriptor = ClientDescriptor.readFromStream(dis); 
		System.out.println(displayName + ":  received welcome from peer " + peerDescriptor.displayName);
		addConnection(peerDescriptor.clientID, peerDescriptor.displayName, socket, dis, dos, debug);
	}

	void welcomeIntoSessionP2P(){

	}
	public ClientGUI getGUI() {
		return gui;
		
	}
	public void exit() throws IOException {
		for(Connection c : connections.values()){
			c.close();
		}
		if(playback != null){
			playback.close();
		}
		if(input != null)
			input.close();
	}
}
