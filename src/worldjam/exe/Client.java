package worldjam.exe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.sound.sampled.LineUnavailableException;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackChannel;
import worldjam.audio.PlaybackManager;
import worldjam.audio.AudioSample;
import worldjam.audio.AudioSubscriber;
import worldjam.gui.ClientGUI;
import worldjam.gui.ClientSetupGUI;
import worldjam.gui.TimeCalibrationDialog;
import worldjam.net.WJConstants;
import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.time.DelayManager;
import worldjam.util.ByteCountDataInputStream;
import worldjam.util.ByteCountDataOutputStream;
import worldjam.video.VideoFrame;
import worldjam.video.VideoSubscriber;
import worldjam.video.WebcamInterface;

public class Client implements ClockSubscriber {
	public static boolean enableDevFeatures;
	public static void main(String args[]) throws LineUnavailableException, UnknownHostException, IOException{
		for(String arg : args) {
			if(arg.equals("--dev")) {
				enableDevFeatures = true;
			}
		}
		ClientSetupGUI.main(args);

	}


	private boolean debug;

	private BroadcasterThread broadcasterThread;

	private ListenForConnectionsRequestThread listenForConnectionsRequestThread;
	public void setDebug(boolean debug){
		this.debug = debug;
	}

	public Client(int listeningPort, String displayName, InputThread input, 
			PlaybackManager playback, ClockSetting clock,WebcamInterface webcamInterface) 
					throws LineUnavailableException, UnknownHostException, IOException{
		this.broadcasterThread = new BroadcasterThread();
		broadcasterThread.start();
		this.selfDescriptor = new ClientDescriptor(displayName, displayName.hashCode());
		this.displayName = displayName;

		this.playback = playback;
		//now setup the input
		this.input = input;
		if(input != null){
			input.addSubscriber(broadcasterThread);
			//input.start();
		}
		if(playback != null && input != null){
			try {
				input.setSenderID(this.selfDescriptor.clientID);
				playback.addChannel(input.getSenderID(), "loopback");
				try {
					Thread.sleep(700);//the thread sleep prevents problems where the channel cannot be muted 
					//right after being added.  Is this necessary?  Idk
					playback.getChannel(input.getSenderID()).setMuted(true); //by default, mute the selfy channel
				} catch (Exception e) {
					e.printStackTrace();
				}

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
		if(webcamInterface != null){
			attachWebcam(webcamInterface);
		}
		for (PlaybackChannel pbc : playback.getChannels()) {
			delayManager.addChannel(pbc.getChannelID(), pbc.getChannelName());
			delayManager.getChannel(pbc.getChannelID()).addListener(pbc);
		}
	}

	public void generateRandomSessionID() {
		Random random = new Random();
		sessionID = random.nextLong();
	}
	private long sessionID;

	private Map<Long,Connection> connections = new HashMap<Long,Connection>();
	void addConnection(ClientDescriptor peer, Socket socket, ByteCountDataInputStream dis, ByteCountDataOutputStream dos, boolean isServer){
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

	public float sampleInputByteRate(long sampleDuration) {
		long totBefore = 0;
		for(Connection con : connections.values()) {
			totBefore += con.dis.bytesProcessed();
		}
		try {
			Thread.sleep(sampleDuration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long totAfter = 0;
		for(Connection con : connections.values()) {
			totAfter += con.dis.bytesProcessed();
		}
		return (totAfter-totBefore)*1000.f/sampleDuration;

	}

	public float sampleOutputByteRate(long sampleDuration) {
		long totBefore = 0;
		for(Connection con : connections.values()) {
			totBefore += con.dos.bytesProcessed();
		}
		try {
			Thread.sleep(sampleDuration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long totAfter = 0;
		for(Connection con : connections.values()) {
			totAfter += con.dos.bytesProcessed();
		}
		return (totAfter-totBefore)*1000.f/sampleDuration;

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


	public void changeClockSettingsNow(ClockSetting clock){
		if(clock == null)
			throw new NullPointerException();
		this.beatClock = clock;
		if(playback != null)
			playback.changeClockSettingsNow(beatClock);


		if(input != null) input.changeClockSettingsNow(beatClock);

		if(gui == null){
			gui = new ClientGUI(this);
			gui.setVisible(true);
			TimeCalibrationDialog tc = new TimeCalibrationDialog(this);
			tc.setVisible(true);
		}
		//broadcasterThread.changeClockSettingsNow(clock);
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

		Connection(Socket socket, ByteCountDataInputStream dis, ByteCountDataOutputStream dos, ClientDescriptor peer, boolean isToServer){
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
		private ByteCountDataOutputStream dos;
		private ByteCountDataInputStream dis;
		private Socket socket;

		private class ReceiverThread extends Thread{
			{
				setName("receiver for " + peer.displayName);
			}

			@SuppressWarnings("rawtypes")
			public void run(){
				ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
				try {
					while(alive){
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

						} else if(code == WJConstants.AUDIO_SAMPLE){								
							AudioSample sample = AudioSample.readFromStream(dis);
							audioSampleReceivedFromPeer(sample);

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

						} else throw new Exception("unrecognized code " + (char)code + " (" +(int)code + ")");

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

			try {
				//change the sourceID to the client's id number
				sample.sourceID = selfDescriptor.clientID;
				dos.writeByte(WJConstants.AUDIO_SAMPLE);
				sample.writeToStream(dos);
			} catch (SocketException e) {
				System.out.println("closing connection");
				removeConnection(peer.clientID);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		//reuseable auxilliary stream used for converting video data to bytes.    
		ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
		private void sendVideoFrame(VideoFrame frame) {

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

		public void close() throws IOException {
			if(this.socket != null) {
				socket.close();
			}
			this.alive = false;
		}
		ClientDescriptor peer;

	}

	private void audioSampleReceivedFromPeer(AudioSample sample){
		playback.sampleReceived(sample);
	}

	private String displayName;
	private String sessionName; 
	public String getUserName(){
		return this.displayName;
	}

	public String getSessionName(){
		return this.sessionName;
	}

	private enum ConnectionMode{
		DIRECT, THROUGH_SERVER;
	}

	ConnectionMode connectionMode = ConnectionMode.THROUGH_SERVER;

	private void broadcastAudioSample(AudioSample sample){
		if(debug)
			System.out.println(this.getUserName()+": sending audio message at " + new Date());
		for(Connection connection : connections.values()){
			if(connectionMode == ConnectionMode.DIRECT && connection.isToServer)
				continue;
			connection.sendAudioSample(sample);
		}
	}

	ClientDescriptor selfDescriptor;

	private CheckForTimeoutThread checkForTimeoutThread;
	/**
	 * auxilliary stream for writing video frames to a stream.  
	 * This is part of a patch for allowing the use of a stream of jpeg images,
	 * whose lengths are not known a priori.   
	 */
	//private ByteArrayOutputStream baosForRecording;

	public ClientDescriptor getDescriptor() {
		return this.selfDescriptor;
	}


	public class CheckForTimeoutThread extends Thread implements AudioSubscriber {
		{
			setName("check for timeout");
		}
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

	private void broadcastClockChange(ClockSetting clock){
		for(Connection con : connections.values()){
			ByteCountDataOutputStream dos = con.dos;

			try {
				dos.writeByte(WJConstants.TIME_CHANGED);
				clock.writeToStream(dos);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}


	private ServerSocket serverSocket;


	private class ListenForConnectionsRequestThread extends Thread {
		
		int port;
		ListenForConnectionsRequestThread(int port){
			this.port = port;
			setName("listen for connections request");
		}
		public void run(){
			try {
				serverSocket = new ServerSocket(port);
				while(!closed){
					Socket socket = serverSocket.accept();
					System.out.println("socket accepted");

					ByteCountDataInputStream dis = new ByteCountDataInputStream(socket.getInputStream());
					ByteCountDataOutputStream dos = new ByteCountDataOutputStream(socket.getOutputStream());
					byte firstByte = dis.readByte();
					if(firstByte == WJConstants.COMMAND_JOIN){
						ClientDescriptor peer = ClientDescriptor.readFromStream(dis);
						System.out.println(displayName + ":  received join request from peer " + peer.displayName);

						dos.writeByte(WJConstants.COMMAND_JOIN_RECEIVED);
						selfDescriptor.writeToStream(dos);
						dos.writeLong(sessionID);
						dos.writeByte(WJConstants.TIME_CHANGED);
						beatClock.writeToStream(dos);

						addConnection(peer, socket, dis, dos, false);
					} else if (firstByte == WJConstants.COMMAND_GET_SESSION_INFO) {
						System.out.println("sending session info");
						sessionInfo().writeToStream(dos);
						//socket.close();
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		private SessionDescriptor sessionInfo() {
			Connection cons[] =connections.values().toArray(new Connection[connections.values().size()]);
			ClientDescriptor members[] = new ClientDescriptor[cons.length+1];
			members[0] = selfDescriptor;
			for(int i = 0; i<cons.length; i++) {
				members[i+1] = cons[i].peer;
			}
			return new SessionDescriptor(sessionID, beatClock, members);
		}
		boolean closed = false;
		void close(){
			closed = true;
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void joinSessionP2P(String string) throws NumberFormatException, UnknownHostException, IOException {
		//so far, the string must be of the form ip.add.re.ss/port for a peer on a LAN or a VPN
		//a better system would be to have the username or something user-friendly like that, and have a server
		//facilitate the connection between peers.  

		String peerIP = string.split("/")[0];
		String peerPort = string.split("/")[1];
		joinSessionP2P(peerIP, Integer.parseInt(peerPort));
	}
	private void joinSessionP2P(String peerIpAddress, int port) throws UnknownHostException, IOException{
		Socket socket = new Socket(peerIpAddress, port);
		System.out.println("socket connected");
		ByteCountDataInputStream dis = new ByteCountDataInputStream(socket.getInputStream());
		ByteCountDataOutputStream dos = new ByteCountDataOutputStream(socket.getOutputStream());
		dos.writeByte(WJConstants.COMMAND_JOIN);
		selfDescriptor.writeToStream(dos);
		dis.readByte();
		ClientDescriptor peerDescriptor = ClientDescriptor.readFromStream(dis); 
		long sessionID = dis.readLong();
		this.sessionID = sessionID;
		System.out.println(displayName + ":  received welcome from peer " + peerDescriptor.displayName);
		addConnection(peerDescriptor, socket, dis, dos, debug);
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
	public void attachWebcam(WebcamInterface webcamInterface){
		//selfie viewer
		webcamInterface.addSubscriber((frame)->{ 
			frame.setSourceID(this.selfDescriptor.clientID);
			frame.setSourceID(0L);
			gui.videoFrameReceived(frame);
		});
		webcamInterface.addSubscriber(broadcasterThread);
		//webcamInterface.start();
		this.webcamInterface = webcamInterface;
	}
	WebcamInterface webcamInterface;
	public WebcamInterface getWebcamInterface() {
		return webcamInterface;
	}
	private void broadcastVideoFrame(VideoFrame frame){
		//System.out.println("subs send frame");
		for (Connection con : connections.values()){
			//System.out.println("con send frame");
			con.sendVideoFrame(frame);
		}

		//gui.videoFrameReceived(0, timestamp, image);
	}

	private DelayManager delayManager = new DelayManager();
	public DelayManager getDelayManager(){

		return delayManager;
	}


	/**
	 * 
	 * @return a String representing the status of the session, for debug
	 * purposes
	 */
	public String getFormattedSessionStatusString() {
		String info = "";
		info += "username: " + getUserName();
		info += " (id = " + Long.toHexString(getDescriptor().clientID) + ")\n";
		info +=  "server socket listening on port " +  serverSocket.getLocalPort() ;
		if(!serverSocket.getInetAddress().getHostAddress().equals("0.0.0.0")) {
			info +=  " (address = " +  serverSocket.getInetAddress() +")\n";
		}
		else 
			info += " (listening on all local IP addresses)\n";
		info += "\nPeers:\n";
		for (Connection connection : connections.values()) {
			String name = connection.peer.displayName;
			long id = connection.peer.clientID;
			SocketAddress sa = connection.socket.getRemoteSocketAddress();
			info += name + " (id = " + Long.toHexString(id) + ")  address = " + sa.toString();
		}

		return info;
	}

	class BroadcasterThread extends Thread implements AudioSubscriber,ClockSubscriber, VideoSubscriber{
		BroadcasterThread(){
			this.setName("broadcaster thread");
		}
		public void run() {
			int interval = 50;
			while(true) {
				if(newAudioSample) {
					broadcastAudioSample(sample);
					newAudioSample=false;
				}
				if(newClockSetting) {
					broadcastClockChange(clock);
					newClockSetting = false;
				}
				if(newVideoFrame) {
					broadcastVideoFrame(videoFrame);
					newVideoFrame = false;
				}
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		AudioSample sample = new AudioSample();
		ClockSetting clock;
		VideoFrame videoFrame;
		private boolean newAudioSample;
		private boolean newClockSetting;
		private boolean newVideoFrame;
		@Override
		public void sampleReceived(AudioSample receivedSample) {
			//copy the received sample to the transmitted sample
			sample.sourceID = receivedSample.sourceID;
			sample.sampleStartTime = receivedSample.sampleStartTime;
			if(sample.sampleData == null || sample.sampleData.length != receivedSample.sampleData.length) {
				sample.sampleData = new byte[receivedSample.sampleData.length];
			}
			System.arraycopy(receivedSample.sampleData, 0, sample.sampleData,0, receivedSample.sampleData.length);
			newAudioSample= true;
		}
		@Override
		public void changeClockSettingsNow(ClockSetting clock) {
			this.clock = clock;
			newClockSetting = true;
		}
		@Override
		public void imageReceived(VideoFrame frame) {
			this.videoFrame = frame;
			newVideoFrame = true;
		}
	}
	/*
	 * tells the BroadcasterThread to send the current clock settings to 
	 * all peers
	 */
	public void broadcastClockSettings() {
		broadcasterThread.changeClockSettingsNow(beatClock);
	}


}
