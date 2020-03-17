package worldjam.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Configurations {
	public static String getStringValue(String name) {
		if(configs == null) {
			loadConfigs();
		}
		return configs.get(name);
	}
	public static ArrayList<String> getStringList(String name) {
		if(configs2 == null) {
			loadConfigs();
		}
		return configs2.get(name);
	}
	public static double getDoubleValue(String name) {
		if(configs == null) {
			loadConfigs();
		}
		return Double.parseDouble(configs.get(name));
	}
	public static int getIntValue(String name) {
		if(configs == null) {
			loadConfigs();
		}
		return Integer.parseInt(configs.get(name));
	}

	private static String FILE_LOCATION = System.getProperty("user.home")+"/.worldjam/config";
	private static void loadConfigs() {
		File configFile = new File(FILE_LOCATION);
		if (!configFile.exists()) {
			try {
				//copy it from the resource file
				configFile.getParentFile().mkdirs();
				InputStream input = Configurations.class.getResource("/worldjam/config/default").openStream();
				byte[] bytes = new byte[input.available()];
				input.read(bytes);
				input.close();
				FileOutputStream fos = new FileOutputStream(configFile);
				fos.write(bytes);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		try {
			configs = new HashMap();
			configs2 = new HashMap();
			Scanner scanner = new Scanner(configFile);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				line = line.split("#")[0];
				//first check if it is 
				if(line.contains("+=")){
					String [] split = line.split("\\+=");
					String key = split[0].trim();
					String val = split[1].trim();
					if(!configs2.containsKey(key)) {
						configs2.put(key,new ArrayList<String>());
					}
					configs2.get(key).add(val);
					continue;
				}
				String [] split = line.split("=");
				if (split.length == 2) {
					String name = split[0].trim();
					String val = split[1].trim();
					configs.put(name, val);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static HashMap<String,String> configs;
	private static HashMap<String,ArrayList<String>> configs2;

	public static String AUDIO_INPUT = "config.audio.input.timeCalibration";
	public static String AUDIO_OUTPUT = "config.audio.output.timeCalibration";
	public static String VIDEO_INPUT = "config.video.input.timeCalibration";
	public static String VIDEO_OUTPUT = "config.video.output.timeCalibration";
	public static String USER_NAME = "config.userName";


	private static final String defaultAudioDevice = "Default Audio Device";
	public static int getDefaultTimingCalibration(String varName, String deviceName) {

		if(deviceName.equals(defaultAudioDevice)) {
			deviceName = getDefaultMixerName(varName);
		}

		List<String> list = getStringList(varName);
		if(list == null)
			return 0;
		for (String line : list) {
			String split[] = line.split("->");
			String key = split[0].trim();
			if(!deviceName.matches(key))
				continue;
			String val = split[1].trim();
			return Integer.parseInt(val);
		}
		//not found
		return 0;
	}
	/**
	 * Get the actual name of the default mixer (patches a quirk in Mac OS
	 * @param var
	 * @return
	 */
	private static String getDefaultMixerName(String var) {
		System.out.println(var);
		for(Mixer.Info info : AudioSystem.getMixerInfo()) {
			System.out.println(info);
			if(info.getName().equals(defaultAudioDevice))
				continue;
			Class<?> clazz = var.contains("input") ? TargetDataLine.class : SourceDataLine.class;
			if(AudioSystem.getMixer(info).isLineSupported(
					new DataLine.Info(clazz, DefaultObjects.defaultFormat))) {
				System.out.println("chosen");
				return info.getName();
			}
		}

		return "Default Audio Device";
	}
	public static String getDefaultUsername() {
		if(getStringValue(USER_NAME) != null) 
			return getStringValue(USER_NAME);
		if(System.getenv("USERNAME") != null)
			return System.getenv("USERNAME");
		if(System.getProperty("user.name") != null)
			return System.getProperty("user.name");
		return "user1";
	}
}
