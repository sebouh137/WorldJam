package worldjam.test;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import worldjam.util.DefaultObjects;

import javax.sound.sampled.Mixer;

public class TestMixers {
	public static void main(String arg[]) throws LineUnavailableException{

		for(Mixer.Info mixerInfo : AudioSystem.getMixerInfo()){
			Mixer mixer = AudioSystem.getMixer(mixerInfo);
			mixer.open();
			System.out.println(mixer.getMixerInfo());
			if(mixer.isLineSupported(Port.Info.MICROPHONE))
				System.out.println(" MICROPHONE");

			else if(mixer.isLineSupported(Port.Info.LINE_IN))
				System.out.println(" LINE_IN");

			else if(mixer.isLineSupported(Port.Info.SPEAKER))
				System.out.println(" SPEAKER");

			else if(mixer.isLineSupported(Port.Info.LINE_OUT))
				System.out.println(" LINE_OUT");

			else if(mixer.isLineSupported(Port.Info.HEADPHONE))
				System.out.println(" HEADPHONE");
			
			else if(mixer.isLineSupported(new Port.Info(Port.class,"",false))){
				System.out.println(" other");
			}
			
			if(mixer.isLineSupported(new DataLine.Info(TargetDataLine.class, DefaultObjects.defaultFormat)))
				System.out.println(" TargetDataLine");
			
			if(mixer.isLineSupported(new DataLine.Info(SourceDataLine.class, DefaultObjects.defaultFormat)))
				System.out.println(" SourceDataLine");
			
			if(mixer.isLineSupported(new DataLine.Info(Clip.class, DefaultObjects.defaultFormat)))
				System.out.println(" Clip");
			
			for(Control c: mixer.getControls()){
				System.out.println("  " + c);
				
			}
			mixer.close();
		
		}
		
		/*System.out.println("microphone:");
		AudioSystem.getLine(Port.Info.MICROPHONE).open();
		for(Control c : AudioSystem.getLine(Port.Info.MICROPHONE).getControls())
			System.out.println(c);
		((Mixer)(AudioSystem.getLine(Port.Info.MICROPHONE))).;
		System.out.println("speaker:");
		AudioSystem.getLine(Port.Info.SPEAKER).open();
		for(Control c : AudioSystem.getLine(Port.Info.SPEAKER).getControls())
			System.out.println(c);
		AudioSystem.getLine(Port.Info.SPEAKER).get
		
		System.out.println("line in:");
		AudioSystem.getLine(Port.Info.LINE_IN).open();
		for(Control c : AudioSystem.getLine(Port.Info.LINE_IN).getControls())
			System.out.println(c);
		
		System.out.println("line out:");
		AudioSystem.getLine(Port.Info.LINE_OUT).open();
		for(Control c : AudioSystem.getLine(Port.Info.LINE_OUT).getControls())
			System.out.println(c);*/
	}
}
