package worldjam.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

public class Configurations {
	public static String getStringValue(String name) {
		if(configs == null) {
			loadConfigs();
		}
		return configs.get(name);
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
			Scanner scanner = new Scanner(configFile);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				line = line.split("#")[0];
				String[] split = line.split("=");
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
}
