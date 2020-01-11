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

	
	private LinkedList<VideoFrame> images = new LinkedList();

	public ViewPanel(ClockSetting clockSetting){
		this.clockSetting = clockSetting;
		this.changeDelaySetting(DelaySetting.defaultDelaySetting);
		refreshThread.start();
	}
	
	
	public void imageReceived(VideoFrame frame){
		BufferedImage image = frame.getImage();
		long timestamp = frame.getTimestamp();
		//System.out.println("image received: " + (image != null) + ". timestamp: " + timestamp + ".  delay=" + delayMS);
		VideoFrame entry = new VideoFrame(image,timestamp,0);
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
			//waiting for images buffer
			while(images.size()==0){
				try {
					Thread.sleep(6);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			//synchronized(ViewPanel.this){
				VideoFrame entry = images.removeFirst();
				try {
					Thread.sleep(Math.max(0,entry.getTimestamp() + delayMS - System.currentTimeMillis()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				currentImage = entry.getImage();
				
				if(this.getTopLevelAncestor() != null)
					this.getTopLevelAncestor().repaint();
				else
					this.repaint();
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
