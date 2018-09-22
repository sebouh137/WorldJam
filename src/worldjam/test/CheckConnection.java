package worldjam.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class CheckConnection {
	public static void main(String arg[]) throws NumberFormatException, UnknownHostException, IOException{
		Socket socket = new Socket(arg[0], Integer.parseInt(arg[1]));
	}
}
