package worldjam.net;

import java.util.ArrayList;

import worldjam.exe.SessionDescriptor;

public class SessionConnectionInfo {
	public ArrayList<String> addresses = new ArrayList();
	public SessionDescriptor descriptor;
	public SessionConnectionInfo(SessionDescriptor descriptor){
		this.descriptor = descriptor;
	}
}
