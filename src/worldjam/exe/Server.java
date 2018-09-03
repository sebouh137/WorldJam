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

public class Server {

	static Map<String, ServerSession> sessions = new HashMap();
	static int port;

	public static void main(String args[]) throws LineUnavailableException, IOException{

		port = DefaultObjects.defaultPort;
		
		parseArguments(args);
		
		
		ServerSocket serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(port, 50);
			System.out.println(new Date() + ":  Started server.");
		} catch (IOException e) {
			System.out.println("Cannot start server socket.  Exiting");
			e.printStackTrace();
			System.exit(0);
		}
		
		printIPinfo(serverSocket);
		
		new CleanupThread().start();

		//System.out.println("begininng server loop");
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
	
	private static void parseArguments(String[] args) {
		for(int i = 0; i<args.length; i++){
			if(args[i].equals("-p") && i<=args.length-1){
				port = Integer.parseInt(args[++i]);
			} /*else if (args[i].equals("-s")){ // In case I decide to implement a maximum limit per server later.
				maxSessions = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-u")){
				maxUsers = Integer.parseInt(args[++i]);
			}*/  
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
	private static void printIPinfo(ServerSocket socket) {

		String localIP = NetworkUtils.getLocalIP();
		String publicIP = NetworkUtils.getPublicIP();
		
		synchronized(System.out){
			
			System.out.println("Server connection information:  \nPort is " + port + ".  IP addresses are:\n"
					+ "From InetAddress.getLocalHost(): " + localIP + "\n" +
					"From online IP services: " + publicIP + "\n"
							+ "From serverSocket.getInetAddress():" + socket.getInetAddress());
		}
	}
	
}
