package worldjam.test;

import java.net.*;
import java.util.*;

import worldjam.net.NetworkUtils;

import static java.lang.System.out;
public class ListNets{

	public static void main(String args[]) throws SocketException {
		System.out.println(NetworkUtils.getNetworkInterfaceInfo(false));
	}

	
} 

