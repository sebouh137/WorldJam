package worldjam.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import worldjam.core.BeatClock;

public interface PlaybackChannel extends RMS, AudioSubscriber{
	public void setReplayOffset(int nMeasures, int nBeats, int n_ms);
	public void close();
	public void setClock(BeatClock beatClock);
	public BeatClock getClock();
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
}
