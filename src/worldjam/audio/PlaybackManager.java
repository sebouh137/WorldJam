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
	Map<Long, PlaybackThread> threads = new HashMap<Long, PlaybackThread>();
	private Map<Long, String> channelNames = new HashMap();
	public void addThread(long senderID, String name) throws LineUnavailableException{
		PlaybackThread thread = new PlaybackThread(mixer, format, clock);
		threads.put(senderID, thread);
		channelNames.put(senderID, name);
		thread.start();
		channelsChanged();
	}
	public void removeThread(long senderID){
		threads.get(senderID).close();
		threads.remove(senderID);
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
		if(threads.containsKey(senderID))
			threads.get(senderID).sampleReceived(sample);
		else
			System.out.println(threads.keySet() + " does not contain " + senderID);
	}
	
	public Line getLine(long lineID){
		return threads.get(lineID).getLine();
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
		return threads.keySet();
	}
	
	public void setFilter(int id, AudioFilter filter){
		threads.get(id).setFilter(filter);
	}
}
