package worldjam.net;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import worldjam.exe.SessionDescriptor;

public class ScanForJamSessions {
	public static void main(String arg[]) {
		
		for(Map.Entry<String, SessionDescriptor>e :getResponses("192.168.193.0/24", 2901, 1000).entrySet()) {
			System.out.println(e.getKey() + "\n" + e.getValue());
		}
	}

	private static String[] getIpAddresses(String arg) {
		String split1[] = arg.split("[/.]");
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
		String []ret = new String[(int) (max-min)+1]; 
		for(long i = min; i<=max; i++) {
			ret[(int) (i-min)] = (i >> 24) + "." + ((i >> 16) & 0xff) + "." + ((i >> 8)&0xff) + "." + (i & 0xff);
		}
		return ret;
	}
	public static ArrayList<SessionConnectionInfo> scanRange(String arg, int port, int timeout) {
		Map<String,SessionDescriptor> responses = getResponses(arg, port, timeout);
		return consolidateResponses(responses);
	}
	
	private static Map<String,SessionDescriptor> getResponses(String arg, int port, int timeout) {
		String addrs[] = getIpAddresses(arg); 
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
	public static ArrayList<SessionConnectionInfo> consolidateResponses(Map<String,SessionDescriptor> responses) {
		Map<Long,SessionConnectionInfo> map = new HashMap<Long,SessionConnectionInfo>();
		for(Map.Entry<String,SessionDescriptor> entry : responses.entrySet()) {
			if(!map.containsKey(entry.getValue().sessionID)) {
				map.put(entry.getValue().sessionID, new SessionConnectionInfo(entry.getValue()));
			}
			map.get(entry.getValue().sessionID).addresses.add(entry.getKey());
			
		}
		System.out.println(map.size() + " sessions");
		return new ArrayList(map.values());
	}
}