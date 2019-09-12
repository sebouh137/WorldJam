package worldjam.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DelayManager {
	public class DelayedChannel{
		DelayedChannel(String name, DelaySetting delay){
			this.name = name;
			this.delay = delay;
		}
		private String name;
		private DelaySetting delay;
		public void setDelay(DelaySetting newDelaySetting){
			for(DelayChangeListener listener : listeners){
				listener.changeDelaySetting(newDelaySetting);
			}
		}
		List<DelayChangeListener> listeners = new ArrayList();
		public void addListener(DelayChangeListener listener){
			listeners.add(listener);
		}
		public String getName(){
			return name;
		}
		public DelaySetting getDelaySetting(){
			return this.delay;
		}
	}
	private Map<Long, DelayedChannel> channels = new HashMap<Long,DelayedChannel>();
	public void addChannel(long id, String name){
		this.channels.put(id, new DelayedChannel(name, DelaySetting.defaultDelaySetting));
	}
	public int getChannelCount(){
		return channels.size();
	} 
	public Collection<DelayedChannel> getChannels(){
		return channels.values();
	}
	public DelayedChannel getChannel(long id){
		return channels.get(id);
	}
}
