package worldjam.test;

import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.core.BeatClock;
import worldjam.exe.Client;
import worldjam.exe.Server;

public class ServerClientSingleJVM {
	static Mixer getMixer(String regex){
		for(Mixer.Info info : AudioSystem.getMixerInfo()){
			System.out.println(info);
			if (info.getName().toLowerCase().matches(regex.toLowerCase())){
				return AudioSystem.getMixer(info);
			}
		}
		return null;
	}

	/**
	 * Creates a server, and one or two clients,
	 * all running on the same JVM, for testing purposes
	 * @param arg
	 * @throws LineUnavailableException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String arg[]) throws LineUnavailableException, IOException, InterruptedException{
		boolean twoClients = false;
		if(arg.length > 0 && arg[0].equals("2"))
			twoClients = true;
		String sessionName = "test";
		int port = 2901;

		new Thread(()->{try {
			Server.main(new String[]{Integer.toString(port)});
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}).start();

		Thread.sleep(2000);

		Mixer inputMixer = getMixer("Default.*");
		Mixer outputMixer = getMixer("Default.*");

		System.out.println(inputMixer);
		System.out.println(outputMixer);

		new Thread(()->{try {
			Client client1 = new Client("127.0.0.1", port, sessionName, "user1", inputMixer, outputMixer);
			BeatClock clock = new BeatClock(500, 3, 4);
			client1.setDebug(true);
			client1.startNewSession(clock);

		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}).start();
		
		//if the two-client option is selected, run with 
		//two clients
		if(twoClients){
			Thread.sleep(2000);
			new Thread(()->{try {
				Client client2 = new Client("127.0.0.1", port, sessionName, "user2", null, outputMixer);
				client2.setDebug(true);
				client2.joinSession();
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}).start();
		}
	}
}
