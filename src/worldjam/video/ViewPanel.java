package worldjam.video;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.util.LinkedList;

import javax.swing.JComponent;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.time.DelayChangeListener;
import worldjam.time.DelaySetting;

public class ViewPanel extends JComponent implements VideoSubscriber, DelayChangeListener, ClockSubscriber{
	private DataInputStream inputStream;
	int delayMS = 2000;

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

	public ViewPanel(ClockSetting clockSetting){
		this.clockSetting = clockSetting;
		this.changeDelaySetting(DelaySetting.defaultDelaySetting);
		refreshThread.start();
	}
	
	
	public void imageReceived(BufferedImage image, long timestamp){
		//System.out.println("image received: " + (image != null) + ". timestamp: " + timestamp + ".  delay=" + delayMS);
		ImageAndTimestamp entry = new ImageAndTimestamp(image,timestamp);
		//System.out.println("received image");
		if(image != null){
			synchronized(ViewPanel.this){
				images.add(entry);
			}
		}
		
	}
	

	private BufferedImage currentImage = null;
	private boolean closed = false;
	
	private Thread refreshThread = new Thread(()->{
		BufferedImage prevImage = null;
		while(!closed ){
			while(images.size()==0){
				try {
					Thread.sleep(10);
					//System.out.println("waiting for images buffer");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			//synchronized(ViewPanel.this){
				ImageAndTimestamp entry = images.removeFirst();
				try {
					Thread.sleep(Math.max(0,entry.timestamp + delayMS - System.currentTimeMillis()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				currentImage = entry.bufferedImage;
				
				this.getTopLevelAncestor().repaint();
				if(prevImage != null)
					prevImage.flush();

			//}
			prevImage = currentImage;
		}

	});


	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		super.paintComponent(g);
		if(currentImage != null)
		{
			//System.out.println("painting image");
			g2.drawImage(currentImage, 0, 0, getWidth(),getHeight(),null);
		}
		else{
			g.setColor(Color.black);
			g.fillRect(0, 0, getWidth(),getHeight());
			g.setColor(Color.WHITE);
			g.drawString("Visuals not available", getWidth()/4, getHeight()/2);
			//System.out.println("buffered image to be painted is null");
		}
	}


	ClockSetting clockSetting;
	DelaySetting delaySetting;
	@Override
	public void changeClockSettingsNow(ClockSetting clockSetting) {
		this.clockSetting = clockSetting;
		this.delayMS = delaySetting.totalDelayVisual(clockSetting);
	}


	@Override
	public void changeDelaySetting(DelaySetting newDelaySetting) {
		this.delaySetting = newDelaySetting;
		this.delayMS = delaySetting.totalDelayVisual(clockSetting);
	}


	public void close() {
		this.closed = true;
	}
}
