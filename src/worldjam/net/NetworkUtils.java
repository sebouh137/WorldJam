package worldjam.net;

import java.net.*; 
import java.io.*; 
import java.util.*;
import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

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

	public static String getPublicIP(InetAddress localInetAddress) {
		String ipServers[] = {"http://icanhazip.com/", "http://checkip.amazonaws.com", "http://www.trackip.net/ip", "http://myexternalip.com/raw"};
		for(String ipServer : ipServers){

			BufferedReader in = null;
			try {

				int timeout = 1000;
				Socket socket = new Socket();
				socket.bind(new InetSocketAddress(localInetAddress,0));
				//System.out.println("connecting");
				socket.connect(new InetSocketAddress(Inet4Address.getByName(ipServer.replace("http://","").replaceAll("/.*", "")), 80),timeout);
				//System.out.println("connected");
				PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))); 
				//out.println("GET /index.html HTTP/1.0"); 
				out.println("GET " + ipServer +" HTTP/1.0"); 
				out.println("Host: " +ipServer.replace("http://","").replaceAll("/.*",""));
				out.println(); 
				out.flush(); 
				InputStream inputStream = socket.getInputStream();
				in = new BufferedReader(new InputStreamReader(
						inputStream));
				//skip through the header.  A blank line separates the header from the body
				for(int i = 0; i<100; i++) {
					String line = in.readLine();
					//System.out.println(line);
					if(line.length() == 0)
						break;
				} 
				
				String ip = in.readLine();
				socket.close();
				return ip;
			} catch ( UnknownHostException | NoRouteToHostException | ConnectException e2) {
				//do nothing.  don't clog up the command line with junk
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


	public static void main(String arg[]) throws SocketException {
		System.out.println("unfiltered:\n" + getNetworkInterfaceInfo(false));
		System.out.println("\n\n\nfiltered:\n" + getNetworkInterfaceInfo(true));
		System.out.println("public IP" + getPublicIP());
	}

	static HashMap<String,String> commonInterfaceDescriptions = new HashMap();
	static {
		commonInterfaceDescriptions.put("ham0", "Hamatchi VPN");
		commonInterfaceDescriptions.put("en0", "Wifi");
	}


	public static String getDescription(String interfaceName) {
		if(interfaceName.matches("en[0-9]")) {
			return "Physical network (" + interfaceName + ")";
		}
		if(interfaceName.matches("ham[0-9]")) {
			return "Hamatchi VPN (" + interfaceName + ")";
		}
		return String.format("Other network interface (%s)",interfaceName);
	}

	public static String getNetworkInterfaceInfo(boolean filtered)  throws SocketException{
		String string = "";
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			if(!netint.isUp() && filtered)
				continue;
			if(netint.isLoopback() && filtered)
				continue;

			string +=  "interface: " + netint.getName();
			if (!netint.getDisplayName().equals(netint.getName())) {
				string += " [" + netint.getDisplayName() + "]";
			}
			if(netint.isVirtual()) string += "  (virtual)";
			string += ":\n";
			//out.printf("Name: %s\n", netint.getName());
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				if(inetAddress.isLinkLocalAddress() && filtered)
					continue;
				String version = inetAddress instanceof Inet4Address ? "  ipv4" : "  ipv6";
				string +=  version + " " + inetAddress.getHostAddress();


				if(inetAddress.isSiteLocalAddress()) {
					string+="  (site local)";
				}
				if(inetAddress.isLinkLocalAddress()) {
					string+="  (link local)";
				}
				if(inetAddress.isLoopbackAddress()) {
					string+="  (loopback)";
				}

				String publicAddr = getPublicIP(inetAddress);
				if(publicAddr != null)	
					string += "  public: " + publicAddr;

				string += "\n";

			}
			/*for (InterfaceAddress addr : netint.getInterfaceAddresses()) {
			System.out.println("InterfaceAddress:" + addr.toString());
		}*/
			string += "\n";
		}
		return string;
	}
}
