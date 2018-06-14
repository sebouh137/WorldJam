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
import java.util.HashMap;
import java.util.Map;
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
					BeatClock beatClock = new BeatClock(msPerBeat, beatsPerMeasure, denom);
					beatClock.startTime = startTime;

					ServerSession session = new ServerSession(beatClock, sessionName);

					session.addClientHandler(socket);
					synchronized(sessions){
						sessions.put(sessionName, session);
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
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
					// TODO Auto-generated catch block
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
