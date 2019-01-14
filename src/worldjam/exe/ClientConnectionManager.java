package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import worldjam.core.BeatClock;
import worldjam.net.NetworkUtils;
import worldjam.net.WJConstants;

/**
 * @author spaul
 *
 */
public class ClientConnectionManager {
	//all output streams that are being broadcast to.  
	//private ArrayList<DataOutputStream> broadcastStreams = new ArrayList<DataOutputStream>();

	private DataOutputStream outServer; //output stream to server
	private DataInputStream inServer; //input stream from server

	private String displayName;
	//private int clientID;

	private String serverIP;
	private String sessionName;
	private String clientIP;
	private Client client;
	private Socket socket;
	public ClientConnectionManager(String serverIP, int port, String sessionName, String displayName, Client client) throws UnknownHostException, IOException {
		this.client = client;
		this.serverIP = serverIP;
		this.sessionName = sessionName;
		System.out.println("attempting to connect to server at " + serverIP);
		socket = new Socket(serverIP, port);
		System.out.println("connected to server at " + serverIP);
		socket.setTcpNoDelay(true);
		//socket.setSoTimeout(10000);
		this.outServer = new DataOutputStream(socket.getOutputStream());
		this.inServer = new DataInputStream(socket.getInputStream());
		
		//this.dis = ;
		this.displayName = displayName;
		this.clientIP = NetworkUtils.getLocalIP();
		
		//joinSession();

	}

	public void joinSession() throws IOException{
		synchronized (outServer){
			outServer.writeByte(WJConstants.COMMAND_JOIN);
			outServer.writeUTF(this.sessionName);
			
			client.getDescriptor().writeToStream(outServer);
			//outServer.writeUTF(this.displayName);
			//long clientID = displayName.hashCode();
			//outServer.writeLong(clientID);
		}

		client.addConnection(null, socket,  inServer, outServer, true);
		//System.out.println("sent join request to session");

	}
	
	public void startNewSession(BeatClock beatClock) throws IOException{
		
		synchronized (outServer){
			outServer.writeByte(WJConstants.COMMAND_CREATE_NEW_SESSION);
			outServer.writeUTF(this.sessionName);
			
			beatClock.writeToStream(outServer);
			
			client.getDescriptor().writeToStream(outServer);
			//outServer.writeUTF(this.displayName);
			//long clientID = displayName.hashCode();
			//outServer.writeLong(clientID);
		}
		client.addConnection(null, socket, inServer, outServer, true);
	}

	/*private class ReceiverThread extends Thread{
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




	};*/
	
	
	
	protected void printClientConfiguration() {
		System.out.println("Joined jam session with the following configuration:");
		System.out.println("  Server IP:  " + serverIP);
		System.out.println("Client configuration:");
		System.out.println("  client ID Number:  " + (long)(displayName.hashCode()));
		System.out.println("  client IP Address:  " + clientIP);
	}
	

	
}
