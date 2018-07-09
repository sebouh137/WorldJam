package worldjam.gui.conductor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConductingPattern {
	
	
	public int getBeatsPerMeasure() {
		return segments.size();
	}
	
	public ConductingPattern(List<Segment> segments) {
		super();
		this.segments = segments;
	}
	
	List<Segment> getSegments(){
		return segments;
	}
	List<Segment> segments;
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Segment segment: segments){
			sb.append(String.format("%d, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f\n", segment.type, 
					segment.x[0],segment.y[0],
					segment.x[1],segment.y[1],
					segment.x[2],segment.y[2],
					segment.x[3],segment.y[3]));
		}
		return sb.toString();
	}
	
	public void read(String data){
		read(new Scanner(data));
	}
	 
	public void read(Scanner scanner){
		segments = new ArrayList();
		while(scanner.hasNextLine()){
			String line[] = scanner.nextLine().split("[ ]*,[ ]*");
			Segment segment = new Segment();
			segment.type = Integer.parseInt(line[0]);
			segment.x[0] = Double.parseDouble(line[1]);
			segment.y[0] = Double.parseDouble(line[2]);
			segment.x[1] = Double.parseDouble(line[3]);
			segment.y[1] = Double.parseDouble(line[4]);
			segment.x[2] = Double.parseDouble(line[5]);
			segment.y[2] = Double.parseDouble(line[6]);
			segment.x[3] = Double.parseDouble(line[7]);
			segment.y[3] = Double.parseDouble(line[8]);
			segments.add(segment);
		}
	}
	public void read(File file) throws FileNotFoundException{
		Scanner scanner = new Scanner(file);
		read(scanner);
		scanner.close();
		
	}
	
}
