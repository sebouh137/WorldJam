package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
import worldjam.test.DefaultObjects;

public class Server {
	private static BeatClock beatClock;
	private static String localIP;


	public static void main(String arg[]) throws LineUnavailableException, IOException{
		Scanner scanner = new Scanner(System.in);		

		localIP = NetworkUtils.getIP();

		beatClock = configureClock(scanner);

		printServerConfiguration();
		joinHandlerThread.start();
		refreshClientList.start();

	}




	private static void printServerConfiguration() {
		System.out.println("Jam session started with the following configuration:");
		System.out.printf("BPM: %.2f  (%d ms per beat)\n", 60000./beatClock.msPerBeat, beatClock.msPerBeat);
		System.out.println("Time Signature: " + beatClock.beatsPerMeasure + "/" + beatClock.beatDenominator);

		System.out.println("Server IP:  " + localIP);
	}


	static BeatClock configureClock(Scanner scanner){
		System.out.println("How many beats per minute? (this will be converted to ms per beat, which will be "
				+ "rounded to the nearest 10 ms)");
		double bpm = Double.parseDouble(scanner.nextLine());
		int msPerBeat = (int)(60000/bpm);
		msPerBeat = (msPerBeat/10)*10;

		System.out.println("Time signature?  (for instance, type '3/4' or '4/4')");

		String sig[] = scanner.nextLine().split("[ /\t]+");
		int num = Integer.parseInt(sig[0]);
		int denom = Integer.parseInt(sig[1]);

		return new BeatClock(msPerBeat, num, denom);

	}

	static Thread joinHandlerThread = new Thread(){
		public void run(){
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(DefaultObjects.defaultPort);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while(true){
				try{
					Socket socket = serverSocket.accept();
					ClientHandler handler = new ClientHandler(socket);
					handlers.add(handler);
					handler.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	static ArrayList<ClientHandler> handlers = new ArrayList();

	static class ClientHandler extends Thread{

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

				while(true){
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//next send a list of other clients.  
			//broadcastClientList();
		}

		void readJoinStatement() throws Exception{
			synchronized(dis){
				byte join = dis.readByte();
				if(join != WJConstants.COMMAND_JOIN)
					throw new Exception("wrong initial byte when joining");
				displayName = dis.readUTF();
				id = dis.readLong();
				System.out.println("client '" + displayName + "' (" + id + ") just joined ");
				
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
	static void broadcastClientList() throws IOException{
		for(ClientHandler handler : handlers){
			synchronized(handler){
				handler.dos.writeByte(WJConstants.LIST_CLIENTS);
				handler.dos.writeInt(handlers.size());
				for(ClientHandler handler2 : handlers){
					handler.dos.writeUTF(handler2.displayName);
					handler.dos.writeLong(handler2.id);
				}
			}
		}
	}

	static Thread refreshClientList = new Thread(){
		public void run(){
			while(true){
				try {

					Thread.sleep(1000);
					//System.out.println("broadcasting client list");
					broadcastClientList();
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};


}
