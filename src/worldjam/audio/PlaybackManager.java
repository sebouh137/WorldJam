package worldjam.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.core.BeatClock;

public class PlaybackManager implements AudioSubscriber{
	
	public PlaybackManager(Mixer mixer, BeatClock clock, AudioFormat format) {
		super();
		this.mixer = mixer;
		this.clock = clock;
		this.format = format;
	}
	Mixer mixer;
	BeatClock clock;
	AudioFormat format;
	Map<Long, PlaybackChannel> channels = new HashMap<Long, PlaybackChannel>();
	private Map<Long, String> channelNames = new HashMap();
	public void addChannel(long senderID, String name) throws LineUnavailableException{
		PlaybackChannel channel = new PlaybackThread(mixer, format, clock);
		
		channels.put(senderID, channel);
		channelNames.put(senderID, name);
		channelsChanged();
	}
	public void removeChannel(long senderID){
		channels.get(senderID).close();
		channels.remove(senderID);
		channelsChanged();
	}
	
	public String getChannelName(long id){
		return channelNames.get(id);
	}
	
	private void channelsChanged(){
		for(ChannelChangeListener channelChangeListener : channelChangeListeners){
			channelChangeListener.channelsChanged();
		}
	}
	
	public void addChannelChangeListener(ChannelChangeListener l){
		this.channelChangeListeners.add(l);
	}
	
	public static interface ChannelChangeListener{
		public void channelsChanged();
	}
	
	private ArrayList<ChannelChangeListener> channelChangeListeners = new ArrayList();
	
	@Override
	public void sampleReceived(SampleMessage sample) {
		long senderID = sample.senderID;
		if(channels.containsKey(senderID))
			channels.get(senderID).sampleReceived(sample);
		else
			System.out.println(channels.keySet() + " does not contain " + senderID);
	}
	
	public Line getLine(long lineID){
		return channels.get(lineID).getLine();
	}
	
	public void printControls(){
		System.out.println("mixer controls");
		for(Control c : mixer.getControls()){
			System.out.println("  " + c);
		}
		System.out.println("line controls");
		for(Line line : mixer.getSourceLines()){
			System.out.println("  " + line);
			for(Control c : line.getControls()){
				System.out.println("    " + c);
			}
		}
	}
	public Set<Long> getIDs(){
		return channels.keySet();
	}
	
	public void setFilter(int id, AudioFilter filter){
		channels.get(id).setFilter(filter);
	}
	public PlaybackChannel getChannel(Long id) {
		return channels.get(id);
	}
	public void setClock(BeatClock beatClock) {
		this.clock = beatClock;
		for(PlaybackChannel channel : channels.values()){
			channel.setClock(clock);
		}
	}
}
