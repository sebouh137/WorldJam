package worldjam.audio;

public class SampleMessage {
	public SampleMessage(long senderID, long sampleStartTime, byte[] sampleData) {
		super();
		this.sampleData = sampleData;
		this.sampleStartTime = sampleStartTime;
		this.senderID = senderID;
	}
	public byte[] sampleData;
	public long sampleStartTime;
	public long senderID;
	public SampleMessage() {}
}
