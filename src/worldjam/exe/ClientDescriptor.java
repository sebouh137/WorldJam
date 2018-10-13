package worldjam.exe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class ClientDescriptor {
	static ClientDescriptor readFromStream(DataInputStream dis) throws IOException{
		String displayName = dis.readUTF();
		long clientID = dis.readLong();
		return new ClientDescriptor(displayName, clientID);
	}
	
	public ClientDescriptor(String displayName, long clientID){
		this.displayName = displayName;
		this.clientID = clientID;
	}
	
	public long clientID;
	public String displayName;
	public void writeToStream(DataOutputStream dos) throws IOException {
		dos.writeUTF(displayName);
		dos.writeLong(clientID);
	}
}
