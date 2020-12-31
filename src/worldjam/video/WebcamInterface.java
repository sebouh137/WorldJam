package worldjam.video;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;



import com.github.sarxos.webcam.*;

public class WebcamInterface implements WebcamListener {
	private Webcam webcam;
	public WebcamInterface(Webcam webcam){
		this.webcam = webcam;
		this.webcam.addWebcamListener(this);
		//this.webcam.addWebcamListener(new WebcamListener());
	}
	
	private List<VideoSubscriber> subscribers = new ArrayList();
	public void addSubscriber(VideoSubscriber sub){
		this.subscribers.add(sub);
	}
	public Webcam getWebcam() {
		return webcam;
	}
	
	public void setWebcam(Webcam webcam) {
		this.webcam = webcam;
	}
	boolean enabled = true;
	public void setEnabled(boolean val) {
		enabled = val;
	}
	
	
	@Override
	public void webcamOpen(WebcamEvent we) { }
	@Override
	public void webcamClosed(WebcamEvent we) { }
	@Override
	public void webcamDisposed(WebcamEvent we) { }
	@Override
	public void webcamImageObtained(WebcamEvent we) {
		BufferedImage image = we.getImage();
		long timestamp = System.currentTimeMillis();
		VideoFrame frame = new VideoFrame(image, timestamp, 0);	
		for(VideoSubscriber sub : subscribers){
			sub.imageReceived(frame);
		}
	}
}
