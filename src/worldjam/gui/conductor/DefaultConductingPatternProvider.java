package worldjam.gui.conductor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultConductingPatternProvider implements ConductingPatternProvider{
	public static DefaultConductingPatternProvider getInstance(){
		return instance;
	}
	private static DefaultConductingPatternProvider instance = new DefaultConductingPatternProvider();
	private ResourceConductingPatternProvider resourceCPP;
	private DefaultConductingPatternProvider(){
		this.resourceCPP = new ResourceConductingPatternProvider();
	}
	private Map<Integer, List<ConductingPattern>> createdPatterns = new HashMap();
	@Override
	public List<ConductingPattern> getAvailablePatterns(int beats) {
		if(resourceCPP.hasAvailablePatterns(beats))
			return resourceCPP.getAvailablePatterns(beats);
		if(createdPatterns.containsKey(beats))
			return createdPatterns.get(beats);
		List list =  new ArrayList();
		list.add(createConductingPattern(beats));
		createdPatterns.put(beats, list);
		return list;
	}
	private ConductingPattern createConductingPattern(int beats) {
		List<BezierSegment> segments = new ArrayList();
		for(int i = 0; i<beats; i++){
			BezierSegment segment = new BezierSegment();
			segment.type = 3;
			for(int j = 0;j<4; j++){
				//Initialize to a star 
				double r = (j==0 || j==3 ) ? .5 : .3;
				segment.y[j] = .5+r*Math.cos((3*i+j)*2*Math.PI/(3*beats));
				segment.x[j] = .5-r*Math.sin((3*i+j)*2*Math.PI/(3*beats));
				segment.t1 = i;
				segment.t2 = i+1;
				//System.out.println(segment.x[j] + " " + segment.y[j]);
			}
			segments.add(segment);
		}

		ConductingPattern pattern = new ConductingPattern(segments);
		return pattern;
	}
	@Override
	public ConductingPattern getDefaultPattern(int beats) {
		if(resourceCPP.hasAvailablePatterns(beats))
			return resourceCPP.getDefaultPattern(beats);
		else if (createdPatterns.containsKey(beats))
			return createdPatterns.get(beats).get(0);
		List list =  new ArrayList();
		ConductingPattern pattern = createConductingPattern(beats);
		list.add(pattern);
		createdPatterns.put(beats, list);
		return pattern;
	}
	@Override
	public boolean hasAvailablePatterns(int beats) {
		return beats >0;
	}
	
	
}
