package worldjam.exe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import worldjam.audio.AudioSubscriber;
import worldjam.audio.SampleMessage;
import worldjam.core.BeatClock;
import worldjam.net.WJConstants;
import worldjam.test.DefaultObjects;

public abstract class PseudoClient implements AudioSubscriber{
	protected DataOutputStream dos;

	protected DataInputStream dis;

	protected String displayName;
	protected int clientID;
	public PseudoClient(String serverIP, String displayName) throws UnknownHostException, IOException {
		socket = new Socket(serverIP, DefaultObjects.defaultPort);
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.dis = new DataInputStream(socket.getInputStream());
		this.displayName = displayName;

		this.receiverThread.start();
		joinSession();

	}
	Socket socket;

	private void joinSession() throws IOException{
		synchronized (this){
			dos.writeByte(WJConstants.COMMAND_JOIN);
			dos.writeUTF(this.displayName);
			clientID = displayName.hashCode();
			dos.writeLong(clientID);
		}

	}
	protected BeatClock beatClock;

	protected AudioSubscriber subs;

	Thread receiverThread = new Thread(){
		public void run(){

			try {
				while(true){
					synchronized(dis){
						byte code;
						code = dis.readByte();
						System.out.println("received code " + (char)code);
						if(code == WJConstants.AUDIO_SAMPLE){
							int datalength = dis.readInt()-2*Long.BYTES;
							long sampleStartTime = dis.readLong();
							long senderID = dis.readLong();
							byte[] data = new byte[datalength];
							dis.read(data);
							SampleMessage sample = new SampleMessage();
							sample.sampleData = data;
							sample.senderID = senderID;
							sample.sampleStartTime = sampleStartTime;
							System.out.println("received sample (" + datalength + " bytes)");
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
							BeatClock beatClock = new BeatClock(msPerBeat, beatsPerMeasure, denom);
							beatClock.startTime = startTime;
							System.out.println("received time information");
							setBeatClock(beatClock);
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
	protected void setBeatClock(BeatClock beatClock) {
		this.beatClock = beatClock;
	}
	protected abstract void processClientList(ArrayList<String> names, ArrayList<Long> ids);
}
