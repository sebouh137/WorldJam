package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.xml.crypto.Data;

import worldjam.audio.InputThread;
import worldjam.audio.PlaybackManager;
import worldjam.core.BeatClock;
import worldjam.net.NetworkUtils;
import worldjam.net.WJConstants;
import worldjam.util.DefaultObjects;

public class ServerSession {
	private  BeatClock beatClock;
	private  String localIP;
	private String sessionName;


	ServerSession(BeatClock beatClock, String sessionName){
		this.beatClock = beatClock;
		this.sessionName = sessionName;
		localIP = NetworkUtils.getIP();


		printSessionConfiguration();
		refreshClientList.start();
	}

	private void printSessionConfiguration() {
		synchronized(System.out){
			System.out.println(new Date() + ":  Jam session started with the following configuration:");
			System.out.println("Session name: " + sessionName);
			System.out.printf("BPM: %.2f  (%d ms per beat)\n", 60000./beatClock.msPerBeat, beatClock.msPerBeat);
			System.out.println("Time Signature: " + beatClock.beatsPerMeasure + "/" + beatClock.beatDenominator);
			System.out.println("Server IP:  " + localIP);
		}
	}

	void addClientHandler(Socket socket) throws IOException{
		ClientHandler handler = new ClientHandler(socket);
		handlers.add(handler);
		handler.start();
	}

	ArrayList<ClientHandler> handlers = new ArrayList();

	class ClientHandler extends Thread{

		Socket socket;
		private DataOutputStream dos;
		private DataInputStream dis;
		String displayName;
		long id;
		public ClientHandler(Socket socket) throws IOException {
			this.socket = socket;
			this.dos = new DataOutputStream(socket.getOutputStream());
			this.dis = new DataInputStream(socket.getInputStream());
		}

		public void run() {
			try {
				readJoinStatement();
				writeAcknowledgeJoinStatement();

				while(!closed){
					synchronized(dis){
						byte code = dis.readByte();
						//System.out.println(code);
						if(shouldThisBeBroadcasted(code)){
							int len = dis.readInt();
							byte bytes[] = new byte[len];
							dis.readFully(bytes);
							broadcastData(code, bytes);
						} else{
							for(int i = 0; i<100; i++)
								System.out.println("    " + dis.readByte());
							throw new Exception("unrecognized code: " + code);

						}
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

		boolean shouldThisBeBroadcasted(byte code){
			if(code == WJConstants.AUDIO_SAMPLE)
				return true;
			return false;
		}

		private void writeAcknowledgeJoinStatement() throws IOException {
			synchronized(this){
				dos.writeByte(WJConstants.TIME_CHANGED);
				dos.writeInt(3*Integer.BYTES+Long.BYTES);
				dos.writeInt(beatClock.msPerBeat);
				dos.writeInt(beatClock.beatsPerMeasure);
				dos.writeInt(beatClock.beatDenominator);
				dos.writeLong(beatClock.startTime);
				//System.out.println("sent time information");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//next send a list of other clients.  
			//broadcastClientList();
		}

		void readJoinStatement() throws Exception{
			synchronized(dis){
				displayName = dis.readUTF();
				id = dis.readLong();
				synchronized(System.out){
					System.out.println(new Date() + ":  client '" + displayName + "' (" + id + ") joined session '" + sessionName + "'");
				}
			}
		}



		void broadcastData(byte dataType, byte data[]) throws IOException{
			for(ClientHandler handler : handlers){
				synchronized(handler){
					handler.dos.writeByte(dataType);
					handler.dos.writeInt(data.length);
					//ArrayList<ClientHandler> handlers = (ArrayList<ClientHandler>) Server.handlers.clone();
					handler.dos.write(data);
				}
			}
		}

	}
	void broadcastClientList() throws IOException{
		ArrayList<ClientHandler> brokenConnections = new ArrayList();
		for(ClientHandler handler : handlers){
			synchronized(handler){
				try{
					handler.dos.writeByte(WJConstants.LIST_CLIENTS);
					handler.dos.writeInt(handlers.size());
					for(ClientHandler handler2 : handlers){
						handler.dos.writeUTF(handler2.displayName);
						handler.dos.writeLong(handler2.id);
					}
				} catch(SocketException e){
					brokenConnections.add(handler);
				}
			}
		}
		for(ClientHandler handler : brokenConnections){
			removeClient(handler);
		}
	}

	private void removeClient(ClientHandler handler) {
		synchronized(handlers){
			synchronized(System.out){
				System.out.println(new Date() + ":  client '" + handler.displayName + "' (id = " + handler.id + ") has disconnected from session '" + sessionName + "'");
			}
			handlers.remove(handler);
		}
	}

	boolean closed = false;

	Thread refreshClientList = new Thread(){
		public void run(){
			while(!closed){
				try {

					Thread.sleep(1000);
					//System.out.println("broadcasting client list");
					broadcastClientList();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};


	public void close() {
		closed = true;
	}


}
