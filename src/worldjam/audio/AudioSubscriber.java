package worldjam.audio;

public interface AudioSubscriber {
	public void sampleReceived(SampleMessage sample);
}
