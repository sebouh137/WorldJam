package worldjam.net;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

public class NetworkUtils {
	public static String getLocalIP() {
		// TODO Auto-generated method stub
		try {
			InetAddress ipAddr = InetAddress.getLocalHost();
			return ipAddr.getHostAddress();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		};
		return null;
	}

	public static String getPublicIP() {
		String ipServers[] = {"http://icanhazip.com/", "http://checkip.amazonaws.com", "http://www.trackip.net/ip", "http://myexternalip.com/raw"};
		for(String ipServer : ipServers){

			BufferedReader in = null;
			try {
				URL whatismyip = new URL(ipServer);
				in = new BufferedReader(new InputStreamReader(
						whatismyip.openStream()));
				String ip = in.readLine();
				byte bytes[] = new byte[4];
				System.out.println(ip);
				for(int i = 0; i< 4; i++){
					bytes[i] = (byte)Integer.parseInt(ip.split("\\.")[i]);	
				}
				return InetAddress.getByAddress(bytes).getHostAddress();
			} catch (Exception e){
				e.printStackTrace();
			}
			finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
	
	public static void printNetworkInterfaceInfo()  throws SocketException{
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets))
			displayInterfaceInformation(netint);
	}
	public static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
		out.printf("Display name: %s\n", netint.getDisplayName());
		
		out.printf("Name: %s\n", netint.getName());
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			out.printf("InetAddress: %s\n", inetAddress.getHostAddress());
		}
		out.printf("\n");
	}
}
