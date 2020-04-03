package worldjam.audio;

public interface HasAudioLevelStats {
	public double getRMS(double window);
	public double getPeakAmp(double window);
}
