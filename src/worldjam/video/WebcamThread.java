package worldjam.video;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;


import com.github.sarxos.webcam.*;
public class WebcamThread extends Thread{
	private int msBetweenFrames = 30;
	private Webcam webcam;
	public WebcamThread(Webcam webcam){
		this.webcam = webcam;
		//this.webcam.addWebcamListener(new WebcamListener());
	}
	public void run(){
		try{
			
			while(true){
				long timestamp = System.currentTimeMillis();
				BufferedImage image = webcam.getImage();
				
				if(image == null){
					//System.out.println("image from webcam is null");
				}
				else {

					//System.out.println("created image");
					for(VideoSubscriber sub : subscribers){
						sub.imageReceived(image, timestamp);
					}
				}
				Thread.sleep(msBetweenFrames);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	private List<VideoSubscriber> subscribers = new ArrayList();
	public void addSubscriber(VideoSubscriber sub){
		this.subscribers.add(sub);
	}
}
