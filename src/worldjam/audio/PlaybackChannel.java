package worldjam.audio;

import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.time.DelayChangeListener;
import worldjam.time.DelaySetting;

public interface PlaybackChannel extends HasAudioLevelStats, AudioSubscriber, ClockSubscriber, DelayChangeListener{
	public void close();
	public void changeClockSettingsNow(ClockSetting beatClock);
	public ClockSetting getClock();
	public SourceDataLine getLine();
	public Mixer getMixer();
	public void setFilter(AudioFilter filter);
	public AudioFormat getInputFormat();
	public AudioFormat getPlaybackFormat();
	public AudioFilter getFilter();
	public String getChannelName();
	public long getChannelID();
	public void startRecording(OutputStream output, long startTime);
	public void stopRecording(long timestamp);
	public void setMuted(boolean muted);
	public boolean isMuted();
	public boolean canBeMuted();
	public int getTotalDelayInMS();
	public DelaySetting getDelaySetting();
	public void validateDelays();
	public LoopBuilder getLoopBuilder();
	public void rebuildLoop();
	public void setConvoMode(boolean b);
}
