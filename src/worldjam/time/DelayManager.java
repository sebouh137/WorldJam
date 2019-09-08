package worldjam.time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DelayManager {
	class DelayedChannel{
		DelaySetting delay;
		void setDelay(DelaySetting newDelaySetting){
			for(DelayChangeListener listener : listeners){
				listener.changeDelaySetting(newDelaySetting);
			}
		}
		List<DelayChangeListener> listeners = new ArrayList();
		void addSubscriber(DelayChangeListener listener){
			listeners.add(listener);
		}
	}
	Map<Long, DelayedChannel> channels = new HashMap<Long,DelayedChannel>();
	
	
}
