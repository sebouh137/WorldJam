package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import worldjam.audio.AudioSubscriber;
import worldjam.audio.SampleMessage;
import worldjam.core.BeatClock;
import worldjam.net.NetworkUtils;
import worldjam.net.WJConstants;

/**
 * Base-class for programs that send data to/from the server
 * @author spaul
 *
 */
public abstract class BaseClient implements AudioSubscriber{
	//all output streams that are being broadcast to.  
	private ArrayList<DataOutputStream> broadcastStreams = new ArrayList<DataOutputStream>();

	DataOutputStream outServer; //output stream to server

	protected String displayName;
	protected int clientID;

	private String serverIP;
	private String sessionName;
	private String clientIP;
	public BaseClient(String serverIP, int port, String sessionName, String displayName) throws UnknownHostException, IOException {
		this.serverIP = serverIP;
		this.sessionName = sessionName;
		System.out.println("attempting to connect to server at " + serverIP);
		socket = new Socket(serverIP, port);
		System.out.println("connected to server at " + serverIP);
		socket.setTcpNoDelay(true);
		//socket.setSoTimeout(10000);
		this.outServer = new DataOutputStream(socket.getOutputStream());
		broadcastStreams.add(outServer);
		//this.dis = ;
		this.displayName = displayName;
		this.clientIP = NetworkUtils.getLocalIP();

		new ReceiverThread(new DataInputStream(socket.getInputStream())).start();
		//joinSession();

	}
	Socket socket;

	public void joinSession() throws IOException{
		synchronized (outServer){
			outServer.writeByte(WJConstants.COMMAND_JOIN);
			outServer.writeUTF(this.sessionName);
			outServer.writeUTF(this.displayName);
			clientID = displayName.hashCode();
			outServer.writeLong(clientID);
		}
		//System.out.println("sent join request to session");

	}
	
	public void startNewSession(BeatClock clock) throws IOException{
		this.beatClock = clock;
		synchronized (outServer){
			outServer.writeByte(WJConstants.COMMAND_CREATE_NEW_SESSION);
			outServer.writeUTF(this.sessionName);
			
			outServer.writeInt(beatClock.msPerBeat);
			outServer.writeInt(beatClock.beatsPerMeasure);
			outServer.writeInt(beatClock.beatDenominator);
			outServer.writeLong(beatClock.startTime);
			
			outServer.writeUTF(this.displayName);
			clientID = displayName.hashCode();
			outServer.writeLong(clientID);
		}
		
	}
	
	protected BeatClock beatClock;

	protected AudioSubscriber subs;

	private class ReceiverThread extends Thread{
		DataInputStream dis;
		ReceiverThread(DataInputStream dis){
			this.dis = dis;
		}
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
							if(subs != null)
								subs.sampleReceived(sample);
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




	};
	
	public String getUserName(){
		return this.displayName;
	}
	
	public String getSessionName(){
		return this.sessionName;
	}
	
	protected void printClientConfiguration() {
		System.out.println("Joined jam session with the following configuration:");
		System.out.printf("  BPM: %.2f  (%d ms per beat)\n", 60000./beatClock.msPerBeat, beatClock.msPerBeat);
		System.out.println("  Time Signature: " + beatClock.beatsPerMeasure + "/" + beatClock.beatDenominator);
		System.out.println("  Server IP:  " + serverIP);
		System.out.println("Client configuration:");
		System.out.println("  client ID Number:  " + clientID);
		System.out.println("  client IP Address:  " + clientIP);
	}
	
	protected void setBeatClock(BeatClock beatClock) {
		this.beatClock = beatClock;
	}
	protected abstract void processClientList(ArrayList<String> names, ArrayList<Long> ids);
	
	public BeatClock getClock(){
		return beatClock;
	}
	
	protected void sendSample(SampleMessage sample){
		for(int i = 0; i< broadcastStreams.size(); i++)
			this.sendSample(sample, broadcastStreams.get(i));
	}
	
	private void sendSample(SampleMessage sample, DataOutputStream dos) {
		synchronized(dos){
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
