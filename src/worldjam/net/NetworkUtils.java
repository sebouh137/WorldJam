package worldjam.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtils {
	public static String getIP() {
		// TODO Auto-generated method stub
		try {
			InetAddress ipAddr = InetAddress.getLocalHost();
			return ipAddr.getHostAddress();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		};
		return null;
	}
}
