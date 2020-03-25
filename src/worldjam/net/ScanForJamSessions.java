package worldjam.net;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import worldjam.exe.SessionDescriptor;

public class ScanForJamSessions {
	public static void main(String arg[]) throws SocketException {
		System.out.println(String.join(",", defaultSearchRanges()));
		for(Map.Entry<String, SessionDescriptor>e :getResponses("192.168.193.0/24", 2901, 1000).entrySet()) {
			System.out.println(e.getKey() + "\n" + e.getValue());
		}
	}

	private static String[] getPeerIpAddresCandidates(String arg) {
		String split2[] = arg.split(",");
		ArrayList<String> allCandidates = new ArrayList();
		for(String arg0 : split2) {
			String split1[] = arg0.split("[/.]");
			String prefix = split1[0];
			int sigBits = Integer.parseInt(split1[4]);
			//use long because unsigned ints don't exist in Java  
			long a = 0;
			for(int i = 0; i<4; i++) {
				a |= (((long)Integer.parseInt(split1[i]))<<(8*(3-i)));
			}
			long mask = 0xffffffff << (32-sigBits);
			long min = a & mask;
			long max = ((a & mask) | (~mask))&0xffffffff;
			//System.out.println(min + " " + max);
			for(long i = min; i<=max; i++) {
				allCandidates.add((i >> 24) + "." + ((i >> 16) & 0xff) + "." + ((i >> 8)&0xff) + "." + (i & 0xff));
			}
		}
		return allCandidates.toArray(new String[0]);
	}
	public static List<SessionConnectionInfo> scanRange(String arg, int port, int timeout) {
		Map<String,SessionDescriptor> responses = getResponses(arg, port, timeout);
		return consolidateResponses(responses);
	}

	private static Map<String,SessionDescriptor> getResponses(String arg, int port, int timeout) {
		String addrs[] = getPeerIpAddresCandidates(arg); 
		Vector<Thread> threads = new Vector(); 
		Map<String,SessionDescriptor> ret = new HashMap<String,SessionDescriptor>();
		for (final String addr : addrs) {
			Thread thread = new Thread(()-> {
				Socket socket = new Socket();
				try {
					socket.connect(new InetSocketAddress(addr, port),timeout);
					System.out.println("socket connected");
					socket.getOutputStream().write(WJConstants.COMMAND_GET_SESSION_INFO);
					System.out.println("sent request");
					SessionDescriptor val = SessionDescriptor.readFromStream(new DataInputStream(socket.getInputStream()));
					//scanner.useDelimiter("#done");//end of file 
					System.out.println("response received");
					synchronized (ret){
						ret.put(addr,val);
					}
					socket.close();
				} catch (IOException e) {
					return;
				} 

			});
			thread.start();
		}
		try {
			Thread.sleep(timeout + 100);
			for(Thread t : threads) {
				t.join(2*timeout);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(ret.size() + " responses");
		return ret;
	}
	public static List<SessionConnectionInfo> consolidateResponses(Map<String,SessionDescriptor> responses) {
		Map<Long,SessionConnectionInfo> map = new HashMap<Long,SessionConnectionInfo>();
		List<Long> clientIDs = new ArrayList();
		for(Map.Entry<String,SessionDescriptor> entry : responses.entrySet()) {
			long respondingClientID = entry.getValue().clients[0].clientID;
			if(!map.containsKey(entry.getValue().sessionID)) {
				
				map.put(entry.getValue().sessionID, new SessionConnectionInfo(entry.getValue()));
			}
			if(clientIDs.contains(respondingClientID)) {
				// duplicate response from the same peer.  This can happen if a peer has more than one ip address
				// that is accessible to this host
				continue;
			}
			clientIDs.add(respondingClientID);
			map.get(entry.getValue().sessionID).addresses.add(entry.getKey());

		}
		System.out.println(map.size() + " sessions");
		return new ArrayList(map.values());
	}

	public static List<String> defaultSearchRanges() throws SocketException {
		List<String> ret = new ArrayList();
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			if(!netint.isUp())
				continue;
			if(netint.isLoopback())
				continue;

			
			//out.printf("Name: %s\n", netint.getName());
			Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
			for (InetAddress inetAddress : Collections.list(inetAddresses)) {
				if(inetAddress.isLinkLocalAddress())
					continue;
				if(inetAddress instanceof Inet4Address)
					ret.add(inetAddress.getHostAddress().replaceAll("\\.[0-9]+$", ".0/24"));

			}
		}
		return ret;
	}
}