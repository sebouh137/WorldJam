package worldjam.gui.conductor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ResourceConductingPatternProvider implements ConductingPatternProvider {
	private Map<Integer, List<ConductingPattern>> map = new HashMap();
	public ResourceConductingPatternProvider(){
		URL url = this.getClass().getResource("/worldjam/gui/conductor/patterns/patterns.list");
		try {
			Scanner scanner = new Scanner(url.openStream());
			while(scanner.hasNextLine()){
				String line = scanner.nextLine().trim();
				if(line == "" || line.startsWith("#"))
					continue;
				String patternFilename = line;
				URL url2 = this.getClass().getResource("/worldjam/gui/conductor/patterns/" + patternFilename);
				Scanner scanner2 = new Scanner(url2.openStream());
				ConductingPattern pattern = new ConductingPattern(null);
				pattern.read(scanner2);
				scanner2.close();
				int beats = pattern.getBeatsPerMeasure();
				if(!map.containsKey(beats)){
					map.put(beats, new ArrayList());
				}
				map.get(beats).add(pattern);
			}
			scanner.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public List<ConductingPattern> getAvailablePatterns(int beats) {
		if(map.containsKey(beats))
			return map.get(beats);
		else return new ArrayList();
	}
	@Override
	public ConductingPattern getDefaultPattern(int beats) {
		if(map.containsKey(beats))
			return map.get(beats).get(0);
		return null;
	}
	public static void main(String arg[]){
		new ResourceConductingPatternProvider();
	}

	@Override
	public boolean hasAvailablePatterns(int beats) {
		return map.containsKey(beats);
	}

}
