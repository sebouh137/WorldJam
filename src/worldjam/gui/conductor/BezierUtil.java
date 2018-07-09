package worldjam.gui.conductor;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public class BezierUtil {
	public static List<Segment> generateSegments(Path2D path) {
		PathIterator pi = path.getPathIterator(null);
		double coords[] = new double[6];
		int type = pi.currentSegment(coords);
		
		double prevX = coords[0];
		double prevY = coords[1];
		List<Segment> segments = new ArrayList();
		pi.next();
		while(!pi.isDone()){
			
			type = pi.currentSegment(coords);
			
			Segment segment = new Segment();
			segment.type = type;
			segment.x = new double[]{prevX, coords[0], coords[2], coords[4]};
			segment.y = new double[]{prevY, coords[1], coords[3], coords[5]};
			//System.out.println(type + " " + Arrays.toString(segment.x) + " " + Arrays.toString(segment.y));
			prevX = coords[2*type-2];
			prevY = coords[2*type-1];
			segments.add(segment);

			pi.next();
		}
		return segments;
	}
}	
