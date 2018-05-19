package worldjam.test;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class ListMixers {
	public static void main(String arg[]){
		Mixer.Info infos[] = AudioSystem.getMixerInfo();
		for(int i = 0; i<infos.length; i++){
			System.out.println(infos[i]);
		}
	}
}
