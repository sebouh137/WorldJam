package worldjam.video;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import worldjam.net.WJConstants;

public class VideoFrame {
	private long timestamp;
	private long senderID;
	private BufferedImage image;

	public VideoFrame(long senderID, long timestamp, BufferedImage image) {
		this.senderID = senderID;
		this.timestamp = timestamp;
		this.image = image;
	}

	public void writeToStream(DataOutputStream dos, ByteArrayOutputStream baos) throws IOException{
		dos.writeLong(senderID);
		dos.writeLong(timestamp);
		if(baos == null)
			baos = new ByteArrayOutputStream(10000);
		if(image == null){
			throw new RuntimeException ("writing a null image to a stream");
			
		}
		ImageIO.write(image, WJConstants.DEFAULT_IMAGE_FORMAT, baos);
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
		return new VideoFrame(senderID, timestamp, image);
	}

	public BufferedImage getImage() {
		return image;
	}

	public long getSourceID() {
		return senderID;
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
}
