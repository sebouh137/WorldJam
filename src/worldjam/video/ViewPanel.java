package worldjam.video;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.imageio.ImageIO;

public class ViewPanel extends Canvas implements VideoSubscriber{
	private DataInputStream inputStream;
	int delayMS = 500;
	/**
	 * sets the delay
	 * @param delay in ms
	 */
	public void setDelay(int delay){
		this.delayMS = delay;
	}
	private class ImageAndTimestamp{
		public ImageAndTimestamp(BufferedImage bufferedImage, long timestamp) {
			super();
			this.bufferedImage = bufferedImage;
			this.timestamp = timestamp;
		}
		BufferedImage bufferedImage;
		long timestamp;
	}
	private LinkedList<ImageAndTimestamp> images = new LinkedList();

	public ViewPanel(){

		refreshThread.start();
	}
	
	
	public void imageReceived(BufferedImage image, long timestamp){
		ImageAndTimestamp entry = new ImageAndTimestamp(image,timestamp);
		System.out.println("received image");
		if(image != null){
			synchronized(ViewPanel.this){
				images.add(entry);
			}
		}
		
	}
	

	private Thread refreshThread = new Thread(()->{
		while(true){
			while(images.size()==0){
				try {
					Thread.sleep(30);
					//System.out.println("waiting for images buffer");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			synchronized(ViewPanel.this){

				ImageAndTimestamp entry = images.removeFirst();
				try {
					Thread.sleep(Math.max(0,entry.timestamp + delayMS - System.currentTimeMillis()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				currentImage = entry.bufferedImage;
				repaint();

			}
		}

	});


	private BufferedImage currentImage;
	public void paint(Graphics g){

		if(currentImage != null)
		{
			//System.out.println("painting image");
			g.drawImage(currentImage, 0, 0,null);
		}
		else{
			System.out.println("buffered image to be painted is null");
		}
	}
}
