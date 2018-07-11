package worldjam.exe;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.LineUnavailableException;

import worldjam.core.BeatClock;
import worldjam.net.NetworkUtils;
import worldjam.net.WJConstants;
import worldjam.util.DefaultObjects;

public class MultiSessionServer {
	private static String localIP;
	static Map<String, ServerSession> sessions = new HashMap();


	public static void main(String arg[]) throws LineUnavailableException, IOException{

		localIP = NetworkUtils.getIP();


		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(DefaultObjects.defaultPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized(System.out){
			System.out.println(new Date() + ":  started server on " + localIP);
		}
		new CleanupThread().start();
		while(true){
			try{
				Socket socket = serverSocket.accept();
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				byte joinOrCreate = dis.readByte();
				if(joinOrCreate == WJConstants.COMMAND_JOIN){
					String sessionName = dis.readUTF();
					ServerSession session = sessions.get(sessionName);
					session.addClientHandler(socket);
				} else if(joinOrCreate == WJConstants.COMMAND_CREATE_NEW_SESSION){
					String sessionName = dis.readUTF();
					int msPerBeat = dis.readInt();
					int beatsPerMeasure = dis.readInt();
					int denom = dis.readInt();
					long startTime = dis.readLong();
					BeatClock beatClock = new BeatClock(msPerBeat, beatsPerMeasure, denom, startTime);

					ServerSession session = new ServerSession(beatClock, sessionName);

					session.addClientHandler(socket);
					synchronized(sessions){
						sessions.put(sessionName, session);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private static class CleanupThread extends Thread{
		public void run(){
			while(true){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized(sessions){
					for(String sessionName : sessions.keySet()){
						ServerSession session = sessions.get(sessionName);
						if(session.handlers.size() == 0){
							session.close();
							sessions.remove(sessionName);
						}
					}
				}
			}
		}
	}
}
