package worldjam.exe;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.audio.AudioSample;
import worldjam.audio.AudioSubscriber;
import worldjam.gui.ClientGUI;
import worldjam.gui.ClientSetupGUI;
import worldjam.gui.ClientSetupGUI_P2P_multiPeer;
import worldjam.net.WJConstants;
import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.time.DelayManager;
import worldjam.util.DefaultObjects;
import worldjam.video.VideoFrame;
import worldjam.video.WebcamThread;

public class Client implements ClockSubscriber {

	private ClientConnectionManager base;
	public static void main(String arg[]) throws LineUnavailableException, UnknownHostException, IOException{

		//if(arg.length >= 1 && arg[0].equals("-g")){
		ClientSetupGUI_P2P_multiPeer.main(arg);

	}

	private Mixer inputMixer, outputMixer;

	private boolean debug;

	private ListenForConnectionsRequestThread listenForConnectionsRequestThread;
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

	public Client(int listeningPort, String displayName, InputThread input, PlaybackManager playback, ClockSetting clock,WebcamThread webcamThread) 
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
		if(input != null){
			input.addSubscriber(sample -> {this.broadcastAudioSample(sample);});
			input.start();
		}
		if(playback != null && input != null){
			try {
				input.setSenderID(this.selfDescriptor.clientID);
				playback.addChannel(input.getSenderID(), this.selfDescriptor.displayName);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			input.addSubscriber(playback);
		}
		this.listenForConnectionsRequestThread = new ListenForConnectionsRequestThread(listeningPort);
		listenForConnectionsRequestThread.start();
		this.checkForTimeoutThread = new CheckForTimeoutThread();
		checkForTimeoutThread.start();
		this.changeClockSettingsNow(clock);
		if(webcamThread != null){
			attachWebcam(webcamThread);
		}
	}

	Map<Long,Connection> connections = new HashMap<Long,Connection>();
	void addConnection(ClientDescriptor peer, Socket socket, DataInputStream dis, DataOutputStream dos, boolean isServer){
		delayManager.addChannel(peer.clientID, peer.displayName);
		try {
			if(playback != null){
				playback.addChannel(peer.clientID,peer.displayName);
				delayManager.getChannel(peer.clientID).addListener(playback.getChannel(peer.clientID));
			}
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		connections.put(peer.clientID, new Connection(socket, dis, dos, peer, isServer));
		if(gui != null)
			gui.channelsChanged();
	}

	private void removeConnection(long id){
		connections.remove(id);
		playback.removeChannel(id);
		if(gui != null)
			gui.channelsChanged();
	}

	public void sendChatMessage(String message){

	}

	public void chatMessageReceived(String sender, String message){
		if(gui != null){
			//gui.getChat().
		}
	}

	private ClockSetting beatClock;

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



	public ClockSetting getBeatClock(){
		return beatClock;
	}

	boolean loopbackAudio;

	public void changeClockSettingsNow(ClockSetting clock){
		if(clock == null)
			throw new NullPointerException();
		this.beatClock = clock;
		if(playback == null){
			playback = new PlaybackManager(outputMixer, beatClock, DefaultObjects.defaultFormat);
			reactors.add(playback);
			//System.out.println("playback initialized");
		}
		else{
			playback.changeClockSettingsNow(beatClock);
		}
		if(input == null && inputMixer != null){
			try {
				input = new InputThread(inputMixer, DefaultObjects.defaultFormat, beatClock);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			input.addSubscriber(sample -> {this.broadcastAudioSample(sample);});
			input.start();
			if(loopbackAudio){
				try {
					playback.addChannel(input.getSenderID(), this.selfDescriptor.displayName);
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
				input.addSubscriber(playback);
			}
		}else{
			if(input != null) input.changeClockSettingsNow(beatClock);
		}
		if(gui == null){
			gui = new ClientGUI(this);
			gui.setVisible(true);
		}
		gui.changeClockSettingsNow(clock);
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

		Connection(Socket socket, DataInputStream dis, DataOutputStream dos, ClientDescriptor peer, boolean isToServer){
			this.dis = dis;
			this.dos = dos;
			this.socket = socket;
			this.peer = peer;
			this.isToServer = isToServer;
			new ReceiverThread().start();
			//new ImAliveThread().start();
		}

		boolean alive = true;
		boolean isToServer;
		private DataOutputStream dos;
		private DataInputStream dis;
		private Socket socket;

		private class ReceiverThread extends Thread{


			@SuppressWarnings("rawtypes")
			public void run(){
				ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
				try {
					while(alive){
						synchronized(dis){
							byte code;
							code = dis.readByte();
							if(code == WJConstants.VIDEO_FRAME){
								//System.out.println("recevied video frame");
								//long senderID = dis.readLong();
								//long timestamp = dis.readLong();
								//System.out.println(Arrays.toString(ImageIO.getReaderFormatNames()));
								//BufferedImage image = ImageIO.read(dis);
								VideoFrame frame = VideoFrame.readFromStream(dis);
								gui.videoFrameReceived(frame);
								synchronized(recordingLock){
									if(recordToFile != null && recordVideo){
										try{
											dos.write(WJConstants.VIDEO_FRAME);
											frame.writeToStream(dos, baos);
										}catch(IOException e){
											e.printStackTrace();
										}
									}
								}
							} else if(code == WJConstants.AUDIO_SAMPLE){								
								AudioSample sample = AudioSample.readFromStream(dis);
								audioSampleReceived(sample);
								synchronized(recordingLock){
									if(recordToFile != null  && recordAudio){
										try{
											recordToFile.writeByte(WJConstants.AUDIO_SAMPLE);
											sample.writeToStream(recordToFile);
										}catch(IOException e){
											e.printStackTrace();
										}
									}
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
								ClockSetting beatClock = ClockSetting.readFromStream(dis);
								System.out.println(displayName + ": received clock " + beatClock);
								changeClockSettingsNow(beatClock);
								synchronized(recordingLock){
									if(recordToFile != null && recordVideo){
										try{
											recordToFile.writeByte(WJConstants.TIME_CHANGED);
											beatClock.writeToStream(recordToFile);
										}catch(IOException e){
											e.printStackTrace();
										}
									}
								}
							} else throw new Exception("unrecognized code " + (char)code + " (" +(int)code + ")");

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
				} catch (SocketException e) {
					System.out.println("closing connection");
					removeConnection(peer.clientID);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//reuseable auxilliary stream used for converting video data to bytes.    
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
		public void sendVideoFrame(VideoFrame frame) {
			synchronized(dos){
				try {
					dos.write(WJConstants.VIDEO_FRAME);
					frame.writeToStream(dos, baos);
				} catch (SocketException e) {
					System.out.println("closing connection");
					removeConnection(peer.clientID);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void close() throws IOException {
			socket.close();
			this.alive = false;
		}
		ClientDescriptor peer;

	}

	public void audioSampleReceived(AudioSample sample) {
		for(AudioSubscriber reactor: reactors){
			reactor.sampleReceived(sample);

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

	public void startNewSession(ClockSetting clock) throws IOException{
		this.changeClockSettingsNow(clock);
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
		synchronized(recordingLock){
			if(recordToFile != null && recordAudio){
				sample.sourceID = selfDescriptor.clientID;
				try {
					recordToFile.writeByte(WJConstants.AUDIO_SAMPLE);
					sample.writeToStream(recordToFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	ClientDescriptor selfDescriptor;

	private CheckForTimeoutThread checkForTimeoutThread;
	/**
	 * auxilliary stream for writing video frames to a stream.  
	 * This is part of a patch for allowing the use of a stream of jpeg images,
	 * whose lengths are not known a priori.   
	 */
	private ByteArrayOutputStream baosForRecording;

	public ClientDescriptor getDescriptor() {
		return this.selfDescriptor;
	}


	public class CheckForTimeoutThread extends Thread implements AudioSubscriber {
		//number of ms to wait between checks
		int sleepTime = 500;
		//max amount of time between the (recorded) start of the sample
		//and the current time.  
		int maxDiff = 10000;
		boolean closed = false;
		public void run(){
			while(!closed){
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
		void close(){
			this.closed = true;
		}
	}

	public void broadcastClockChange(){
		for(Connection con : connections.values()){
			DataOutputStream dos = con.dos;
			synchronized(dos){
				try {
					dos.writeByte(WJConstants.TIME_CHANGED);
					beatClock.writeToStream(dos);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		synchronized(recordingLock){
			if(recordToFile != null){
				try {
					recordToFile.writeByte(WJConstants.TIME_CHANGED);
					beatClock.writeToStream(recordToFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public class ListenForConnectionsRequestThread extends Thread {
		int port;
		ListenForConnectionsRequestThread(int port){
			this.port = port;
		}
		ServerSocket serverSocket;
		public void run(){
			try {
				serverSocket = new ServerSocket(port);
				while(!closed){
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
						addConnection(peer, socket, dis, dos, false);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		boolean closed = false;
		void close(){
			closed = true;
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
		addConnection(peerDescriptor, socket, dis, dos, debug);
	}

	void welcomeIntoSessionP2P(){

	}
	public ClientGUI getGUI() {
		return gui;

	}
	public void exit() throws IOException {
		System.out.println("pre: "+  Thread.activeCount() + " active thread");
		for(Connection c : connections.values()){
			c.close();
		}
		if(playback != null){
			playback.close();
		}
		if(input != null)
			input.close();
		listenForConnectionsRequestThread.close();
		checkForTimeoutThread.close();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("post: "+  Thread.activeCount() + " active thread");
		for (Thread t : Thread.getAllStackTraces().keySet()){
			System.out.println(t.getClass());
		}
	}
	public void attachWebcam(WebcamThread webcamThread){
		webcamThread.addSubscriber((image,timestamp)->{
			broadcastVideoFrame(new VideoFrame(this.selfDescriptor.clientID,timestamp, image));
			gui.videoFrameReceived(0L, timestamp, image);
		});
		webcamThread.start();
	}
	private void broadcastVideoFrame(VideoFrame frame){
		//System.out.println("subs send frame");
		for (Connection con : connections.values()){
			//System.out.println("con send frame");
			con.sendVideoFrame(frame);
		}
		
		synchronized(recordingLock){
			if(recordToFile != null && recordVideo){
				try {
					recordToFile.write(WJConstants.VIDEO_FRAME);
					frame.writeToStream(recordToFile, baosForRecording);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//gui.videoFrameReceived(0, timestamp, image);
	}

	private DelayManager delayManager = new DelayManager();
	public DelayManager getDelayManager(){
		return delayManager;
	}
	Object recordingLock = new Object();
	DataOutputStream recordToFile;

	private boolean recordVideo;

	private boolean recordAudio;
	/*
	 * Begin recording all signals sent to/from this client, and write to a file  
	 * I will later need to create a program that merges the recorded signals to an audio or video file. 
	 */
	public void startRecording(File file, boolean recordVideo, boolean recordAudio) throws FileNotFoundException{

		synchronized(recordingLock){
			this.recordVideo = recordVideo;
			this.recordAudio = recordAudio;
			this.recordToFile = new DataOutputStream(new FileOutputStream(file));
			try {
				recordToFile.writeByte(WJConstants.TIME_CHANGED);
				beatClock.writeToStream(recordToFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void stopRecording(){
		synchronized(recordingLock){
			try {
				recordToFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.recordToFile = null;
		}

	}

}
