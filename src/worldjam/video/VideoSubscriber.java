package worldjam.video;

import java.awt.image.BufferedImage;

public interface VideoSubscriber {
	public void imageReceived(VideoFrame frame);
}
