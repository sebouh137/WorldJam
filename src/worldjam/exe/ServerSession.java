package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

import worldjam.net.NetworkUtils;
import worldjam.net.WJConstants;
import worldjam.time.ClockSetting;

public class ServerSession {
	private  ClockSetting beatClock;
	private  String localIP;
	private String sessionName;


	ServerSession(ClockSetting beatClock, String sessionName){
		this.beatClock = beatClock;
		this.sessionName = sessionName;
		localIP = NetworkUtils.getLocalIP();


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
		//String displayName;
		//long id;
		public ClientDescriptor clientDescriptor;
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
				//dos.writeInt(beatClock.msPerBeat);
				//dos.writeInt(beatClock.beatsPerMeasure);
				//dos.writeInt(beatClock.beatDenominator);
				//dos.writeLong(beatClock.startTime);
				//System.out.println("sent time information");
				beatClock.writeToStream(dos);
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
				this.clientDescriptor = ClientDescriptor.readFromStream(dis);
				synchronized(System.out){
					System.out.println(new Date() + ":  client '" + clientDescriptor.displayName + "' (" + clientDescriptor.clientID + ") joined session '" + sessionName + "'");
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
						ClientDescriptor descriptor = handler2.clientDescriptor;
						descriptor.writeToStream(handler.dos);
						//handler.dos.writeUTF(handler2.displayName);
						//handler.dos.writeLong(handler2.id);
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
				System.out.println(new Date() + ":  client '" + handler.clientDescriptor.displayName + "' (id = " + handler.clientDescriptor.clientID + ") has disconnected from session '" + sessionName + "'");
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
