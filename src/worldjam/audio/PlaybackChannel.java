package worldjam.audio;

import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;

public interface PlaybackChannel extends RMS, AudioSubscriber, ClockSubscriber{
	public void setReplayOffset(int nMeasures, int nBeats, int n_ms);
	public void close();
	public void changeClockSettingsNow(ClockSetting beatClock);
	public ClockSetting getClock();
	public Line getLine();
	public Mixer getMixer();
	public int getAddDelayMeasures();
	public int getAddDelayBeats();
	public int getAddDelayMS();
	public int getDelayInMS();
	public void setFilter(AudioFilter filter);
	public AudioFormat getInputFormat();
	public AudioFormat getPlaybackFormat();
	public AudioFilter getFilter();
	public String getSourceName();
	public long getSenderID();
	void startRecording(OutputStream output, long startTime);
	public void stopRecording(long timestamp);
	public void setMuted(boolean muted);
	public boolean isMuted();
	public boolean canBeMuted();
}
