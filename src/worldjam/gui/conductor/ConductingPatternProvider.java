package worldjam.gui.conductor;

import java.util.List;

public interface ConductingPatternProvider {

	public boolean hasAvailablePatterns(int beats);
	public List<ConductingPattern> getAvailablePatterns(int beats);

	public ConductingPattern getDefaultPattern(int beats);
}
