package worldjam.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;

public class PlaybackManager implements AudioSubscriber, ClockSubscriber{
	
	public PlaybackManager(Mixer mixer, ClockSetting clock, AudioFormat format) {
		super();
		this.mixer = mixer;
		this.clock = clock;
		this.format = format;
		Metronome metronome = new Metronome(); 
		long metronomeChanID = 1234;
		PlaybackChannel metronomeChannel;
		try {
			metronomeChannel = new PlaybackThread(mixer, format, clock, "metronome", metronomeChanID, metronome);
			channels.put(metronomeChanID, metronomeChannel);
			channelNames.put(metronomeChanID, "metronome");
			channelsChanged();
			Thread.sleep(300);
			//mute the metronome by default
			metronomeChannel.setMuted(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	Mixer mixer;
	ClockSetting clock;
	AudioFormat format;
	Map<Long, PlaybackChannel> channels = new HashMap<Long, PlaybackChannel>();
	private Map<Long, String> channelNames = new HashMap();
	public void addChannel(long senderID, String name) throws LineUnavailableException{
		PlaybackChannel channel = new PlaybackThread(mixer, format, clock, name, senderID, null);
		
		channels.put(senderID, channel);
		channelNames.put(senderID, name);
		channelsChanged();
	}
	public void removeChannel(long senderID){
		channels.get(senderID).close();
		channels.remove(senderID);
		channelsChanged();
	}
	
	public void updateChannels(){
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
	public void sampleReceived(AudioSample sample) {
		long senderID = sample.sourceID;
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
	public void changeClockSettingsNow(ClockSetting beatClock) {
		this.clock = beatClock;
		for(PlaybackChannel channel : channels.values()){
			channel.changeClockSettingsNow(clock);
		}
	}
	public void startRecording(File directory, String ext) throws FileNotFoundException{
		directory.mkdir();
		long timestamp = System.currentTimeMillis();
		//round to the 10 ms.  This is because at 44100 frames/second,
		// 10 ms is the smallest integer number of ms that are an integer number
		// of frames.
		timestamp/=10; timestamp*= 10; 
		for(PlaybackChannel channel : channels.values()){
			File trackFile = new File(directory.getPath() + File.separatorChar +"trk_"+ channel.getSourceName() + ext);
			channel.startRecording(new FileOutputStream(trackFile), timestamp);
		}
	}
	public void stopRecording(){
		long timestamp = System.currentTimeMillis();
		for(PlaybackChannel channel : channels.values()){
			channel.stopRecording(timestamp);
		}
	}
	public void close() {
		for(PlaybackChannel channel : channels.values()){
			channel.close();
		}
	}
	public Collection<PlaybackChannel> getChannels() {
		return channels.values();
	}
}
