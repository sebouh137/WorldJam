package worldjam.audio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AudioSample {
	public AudioSample(long sourceID, long sampleStartTime, byte[] sampleData) {
		super();
		this.sampleData = sampleData;
		this.sampleStartTime = sampleStartTime;
		this.sourceID = sourceID;
	}
	public byte[] sampleData;
	public long sampleStartTime;
	public long sourceID;
	public AudioSample() {}
	
	public void writeToStream(DataOutputStream dos) throws IOException{
		int overhead = 2*Long.BYTES;
		
		dos.writeInt(sampleData.length+overhead);
		int start = dos.size();
		dos.writeLong(sampleStartTime);
		dos.writeLong(sourceID);
		dos.write(sampleData);
		if(dos.size() - start != sampleData.length + overhead){
			System.out.println("oops, wrote the wrong number of bytes");
			System.exit(0);
		}
	}
	
	public static AudioSample readFromStream(DataInputStream dis) throws IOException{
		
		int overhead = 2*Long.BYTES;
		int datalength = dis.readInt()-overhead;
		long sampleStartTime = dis.readLong();
		long sourceID = dis.readLong();
		byte[] data = new byte[datalength];
		dis.readFully(data);
		AudioSample sample = new AudioSample(sourceID, sampleStartTime, data);
		return sample;
	}
}
