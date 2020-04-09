package worldjam.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigurationsXML {
	private static String FILE_LOCATION = System.getProperty("user.home")+"/.worldjam/config.xml";
	private static void loadConfigs() {
		File configFile = new File(FILE_LOCATION);
		if (!configFile.exists()) {
			try {
				//copy it from the resource file
				configFile.getParentFile().mkdirs();
				InputStream input = ConfigurationsXML.class.getResource("/worldjam/config/default.xml").openStream();
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
		try   
		{  

			//an instance of factory that gives a document builder  
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
			//an instance of builder to parse the specified xml file  
			DocumentBuilder db = dbf.newDocumentBuilder();  
			Document doc = db.parse(configFile);  
			doc.getDocumentElement().normalize();  
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  

			//get userName
			{
				NodeList nodeList = doc.getElementsByTagName("userName");
				if(nodeList.getLength() >0)
					userName = nodeList.item(0).getTextContent();
			}


			NodeList nodeList = doc.getElementsByTagName("audioDevice");
			// nodeList is not iterable, so we are using for loop  
			for (int itr = 0; itr < nodeList.getLength(); itr++)   
			{  
				Node node = nodeList.item(itr);  
				System.out.println("\nNode Name :" + node.getNodeName());  
				if (node.getNodeType() == Node.ELEMENT_NODE)   
				{  
					Element eElement = (Element) node; 
					String name = eElement.getElementsByTagName("name").item(0).getTextContent();
					System.out.println(name);
					int inputTimeCalib = 0;
					int outputTimeCalib = 0;
					if(eElement.getElementsByTagName("inputTimeCalib").getLength() != 0)
						inputTimeCalib = Integer.parseInt(eElement.getElementsByTagName("inputTimeCalib").item(0).getTextContent());
					if(eElement.getElementsByTagName("outputTimeCalib").getLength() != 0)
						outputTimeCalib = Integer.parseInt(eElement.getElementsByTagName("outputTimeCalib").item(0).getTextContent());
					AudioDeviceConfig config = new AudioDeviceConfig();
					config.name = name;
					config.inputTimeCalib = inputTimeCalib;
					config.outputTimeCalib = outputTimeCalib;
					audioDeviceConfigs.add(config);
				}
			}
		}   
		catch (Exception e)   
		{  
			e.printStackTrace();  
		}  

	}
	private static ArrayList<AudioDeviceConfig> audioDeviceConfigs = new ArrayList();

	private static class AudioDeviceConfig{
		String name;
		int inputTimeCalib;
		int outputTimeCalib;
	}

	private static String userName;
	


	private static final String defaultAudioDevice = "Default Audio Device";
	public static int getInputTimeCalib(String deviceName) {
		if(deviceName.equals(defaultAudioDevice)) {
			deviceName = getDefaultMixerName(true);
		}
		for(AudioDeviceConfig conf : audioDeviceConfigs) {
			if(deviceName.matches(conf.name)) {
				return conf.inputTimeCalib;
			}
		}
		return 0;
	}
	
	public static int getOutputTimeCalib(String deviceName) {
		if(deviceName.equals(defaultAudioDevice)) {
			deviceName = getDefaultMixerName(false);
		}
		for(AudioDeviceConfig conf : audioDeviceConfigs) {
			if(deviceName.matches(conf.name)) {
				return conf.outputTimeCalib;
			}
		}
		return 0;
	}
	
	/**
	 * Get the actual name of the default mixer (patches a quirk in Mac OS
	 * @param var
	 * @return
	 */
	private static String getDefaultMixerName(boolean isInput) {
		
		for(Mixer.Info info : AudioSystem.getMixerInfo()) {
			System.out.println(info);
			if(info.getName().equals(defaultAudioDevice))
				continue;
			Class<?> clazz = isInput ? TargetDataLine.class : SourceDataLine.class;
			if(AudioSystem.getMixer(info).isLineSupported(
					new DataLine.Info(clazz, DefaultObjects.defaultFormat))) {
				System.out.println("chosen");
				return info.getName();
			}
		}

		return "Default Audio Device";
	}
	
	public static String getDefaultUserName() {
		if(userName != null)
			return userName;
		if(System.getenv("USERNAME") != null)
			return System.getenv("USERNAME");
		if(System.getProperty("user.name") != null)
			return System.getProperty("user.name");
		return "user1";
	}
	
	static {
		loadConfigs();
	}
	
	public static void main(String args[]) {
		loadConfigs();
	}
	/**
	 * modify the entry in the config.xml file for the userName
	 * @param txtUser
	 */
	public static void setDefaultUserName(String userName) {
		File file = new File(FILE_LOCATION);
		Scanner scanner;
		StringBuilder sb = new StringBuilder();
		try {
			scanner = new Scanner(file);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.matches(".*<userName>.*</userName>.*")) {
					line = "  <userName>"+userName+"</userName>";
				}
				sb.append(line + "\n");
			}
			scanner.close();
			PrintWriter pw = new PrintWriter(file);
			pw.print(sb.toString());
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}
