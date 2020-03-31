package worldjam.audio;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.sound.sampled.FloatControl;

public abstract class InputVolumeUtil extends FloatControl
{
	protected InputVolumeUtil(Type type, float minimum, float maximum, float precision, int updatePeriod,
			float initialValue, String units) {
		super(type, minimum, maximum, precision, updatePeriod, initialValue, units);
	}

	private static InputVolumeUtil instance;
	
	public static InputVolumeUtil getInstance() {
		if(System.getProperty("os.name").toLowerCase().contains("mac")) {
			instance = new MacInputVolumeUtil();
		}
		if(System.getProperty("os.name").toLowerCase().contains("win")) {
			instance = null;  //not implemented for windows yet
		}
		if(System.getProperty("os.name").toLowerCase().contains("nix")) {
			instance = null; //not implemented for unix yet
		}
		return instance;
	}
	
	private static class MacInputVolumeUtil extends InputVolumeUtil{
		protected MacInputVolumeUtil() {
			super(FloatControl.Type.VOLUME, 0, 100, 4, 0, 0, "");
		}

		public void setVolume(float value) {
			String command = "set volume input volume " + value;
			try
			{
				ProcessBuilder pb = new ProcessBuilder("osascript","-e",command);
				pb.directory(new File("/usr/bin"));
				//System.out.println(command);
				StringBuffer output = new StringBuffer();
				Process p = pb.start();
				p.waitFor();

				BufferedReader reader =
						new BufferedReader(new InputStreamReader(p.getInputStream()));

				String line;
				while ((line = reader.readLine())!= null)
				{
					output.append(line + "\n");
				}
				//System.out.println(output);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		public float getVolume() {
			String command = "input volume of (get volume settings)";
			try
			{
				ProcessBuilder pb = new ProcessBuilder("osascript","-e",command);
				pb.directory(new File("/usr/bin"));
				//System.out.println(command);
				StringBuffer output = new StringBuffer();
				Process p = pb.start();
				p.waitFor();

				BufferedReader reader =
						new BufferedReader(new InputStreamReader(p.getInputStream()));

				String line;
				while ((line = reader.readLine())!= null)
				{
					output.append(line + "\n");
				}
				return Float.parseFloat(output.toString());
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return -1;
			}
		}
	}
	public abstract void setVolume(float val);
	
	public abstract float getVolume();
	
	public static void main(String arg[]) {
		getInstance().setVolume(40);
		System.out.println("received value " + getInstance().getVolume());
	}

	public FloatControl volumeControl() {
		return null;
	}
	public void setValue(float val) {
		setVolume(val);
	}
	public float getValue() {
		return getVolume();
	}
}