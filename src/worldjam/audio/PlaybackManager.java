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
import worldjam.util.ConfigurationsXML;

public class PlaybackManager implements AudioSubscriber, ClockSubscriber{
	
	public void setConvoMode(boolean b) {
		for(PlaybackChannel chan: channels.values()) {
			chan.setConvoMode(b);
		}
		getChannelByName("metronome").setMuted(b);
	}
	
	public PlaybackManager(Mixer mixer, ClockSetting clock, AudioFormat format) {
		super();
		this.mixer = mixer;
		this.clock = clock;
		this.format = format;
		
		this.calibrationInMs = ConfigurationsXML.getOutputTimeCalib(
				mixer.getMixerInfo().getName());
		
		Metronome metronome = new Metronome(); 
		long metronomeChanID = 1234;
		PlaybackChannel metronomeChannel;
		try {
			metronomeChannel = new PlaybackThread(mixer, format, clock, "metronome", metronomeChanID, this, metronome);
			channels.put(metronomeChanID, metronomeChannel);
			channelNames.put(metronomeChanID, "metronome");
			channelsChanged();
			Thread.sleep(500);
			//mute the metronome by default
			metronomeChannel.setMuted(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TuningFork tuningFork = new TuningFork();
		PlaybackChannel tuningForkChannel;
		long tuningForkChanID = 440;
		try {
			tuningForkChannel = new PlaybackThread(mixer, format, clock, "tuning fork", 
					tuningForkChanID, this, tuningFork);
			channels.put(tuningForkChanID, tuningForkChannel);
			channelNames.put(tuningForkChanID, "tuning fork");
			channelsChanged();
			Thread.sleep(500);
			//mute the tuning fork by default
			tuningForkChannel.setMuted(true);
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
		PlaybackChannel channel = new PlaybackThread(mixer, format, clock, name, senderID, this);
		
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
		
		for(PlaybackChannel channel : channels.values()){
			File trackFile = new File(directory.getPath() + File.separatorChar +"trk_"+ channel.getChannelName() + ext);
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
	public PlaybackChannel getChannelByName(String string) {
		for(PlaybackChannel channel : channels.values()){
			if(channel.getChannelName() == string)
				return channel;
		}
		return null;
	}
	public void setTimeCalibration(int calibrationInMs) {
		this.calibrationInMs = calibrationInMs;
		for (PlaybackChannel channel : channels.values()) {
			channel.validateDelays();
		}
	}
	public int getTimeCalibration() {
		return calibrationInMs;
	}
	private int calibrationInMs;
	public Mixer getMixer() {
		return mixer;
	}
	
}
