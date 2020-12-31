package worldjam.video;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import worldjam.net.WJConstants;

public class VideoFrame {
	private long timestamp;
	private long sourceID;
	private BufferedImage image;
	private ByteBuffer byteBuffer;

	public VideoFrame(BufferedImage image, long timestamp, long sourceID) {
		this.sourceID = sourceID;
		this.timestamp = timestamp;
		this.image = image;
	}

	public void writeToStream(DataOutputStream dos, ByteArrayOutputStream baos) throws IOException{
		dos.writeLong(sourceID);
		dos.writeLong(timestamp);
		if(baos == null)
			baos = new ByteArrayOutputStream(100000);
		if(image == null){
			throw new RuntimeException ("writing a null image to a stream");	
		}
		try {

			boolean ret = ImageIO.write(image, WJConstants.DEFAULT_IMAGE_FORMAT, baos);

			if (!ret) {
				System.out.println("image writing exception");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		dos.writeInt(baos.size());
		dos.write(baos.toByteArray());
		baos.reset();
	}

	public static VideoFrame readFromStream(DataInputStream dis) throws IOException{
		long senderID = dis.readLong();
		long timestamp = dis.readLong();
		int len = dis.readInt();
		byte bytes[] = new byte[len];
		//System.out.println(len);
		dis.read(bytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		BufferedImage image = ImageIO.read(bais);
		if (image == null){
			throw new RuntimeException ("read a null image from stream");
		}
		return new VideoFrame(image, timestamp, senderID);
	}

	public BufferedImage getImage() {
		return image;
	}

	public long getSourceID() {
		return sourceID;
	}

	public long getTimestamp(){
		return this.timestamp;
	}

	public void setSourceID(long clientID) {
		this.sourceID = clientID;
	}
}
