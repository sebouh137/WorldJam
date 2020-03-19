package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import worldjam.time.ClockSetting;

public class SessionDescriptor {
	public long sessionID;
	public ClientDescriptor [] clients;
	public ClockSetting clock;
	public SessionDescriptor(long sessionID, ClockSetting clock, ClientDescriptor[] clients) {
		this.sessionID = sessionID;
		this.clock = clock;
		this.clients = clients;
	}
	public static SessionDescriptor readFromStream(DataInputStream dis) throws IOException{
		long sessionID = dis.readLong();
		ClockSetting clock = ClockSetting.readFromStream(dis);
		int nMembers = dis.readInt();
		ClientDescriptor[] clients = new ClientDescriptor[nMembers];
		for (int i = 0; i < nMembers; i++) {
			clients[i] = ClientDescriptor.readFromStream(dis);
		}
		return new SessionDescriptor(sessionID,clock,clients);
	}
	public void writeToStream(DataOutputStream dos) throws IOException {
		dos.writeLong(sessionID);
		clock.writeToStream(dos);
		dos.writeInt(clients.length);
		for(ClientDescriptor client : clients) {
			client.writeToStream(dos);
		}
	}
}
