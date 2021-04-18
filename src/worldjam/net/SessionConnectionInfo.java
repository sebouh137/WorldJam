package worldjam.net;

import java.util.ArrayList;

import worldjam.exe.SessionDescriptor;

public class SessionConnectionInfo {
	// in the form:  address + "/" + port
	// port is DefaultObjects.defaultPort unless otherwise specified (for instance,
	// when doing tests involving multiple clients on one machine).  
	public ArrayList<String> addressesAndPorts = new ArrayList();
	public SessionDescriptor descriptor;
	public SessionConnectionInfo(SessionDescriptor descriptor){
		this.descriptor = descriptor;
	}
	public String toString() {
		String ret = descriptor + "\n";
		for(String addr : addressesAndPorts)
			ret += addr + "\n";
		return ret;
	}
}
