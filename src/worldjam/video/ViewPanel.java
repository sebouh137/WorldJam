package worldjam.video;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.util.LinkedList;

import javax.swing.JComponent;

import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.time.DelayChangeListener;
import worldjam.time.DelaySetting;
public class ViewPanel extends JComponent implements VideoSubscriber, DelayChangeListener, ClockSubscriber{
	
	int delayMS = 2000;


	private LinkedList<VideoFrame> images = new LinkedList();

	GraphicsConfiguration gc;
	public ViewPanel(ClockSetting clockSetting){
		this.clockSetting = clockSetting;
		this.changeDelaySetting(DelaySetting.defaultDelaySetting);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		gc = gs.getDefaultConfiguration();
		refreshThread.start();


	}


	public void imageReceived(VideoFrame frame){
		if(disable)
			return;
		BufferedImage image = frame.getImage();
		long timestamp = frame.getTimestamp();
		image = makeCompatible(image);  
		//System.out.println("image received: " + (image != null) + ". timestamp: " + timestamp + ".  delay=" + delayMS);
		VideoFrame entry = new VideoFrame(image,timestamp,0);
		//System.out.println("received image");
		if(image != null){
			synchronized(ViewPanel.this){
				images.add(entry);
			}
		}

	}

	private BufferedImage makeCompatible(BufferedImage image) {
		return image;
		/*
		//System.out.println("in: " + image.getColorModel() + ", " + image.getSampleModel());
		BufferedImage bimage = gc.createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.OPAQUE);
		Graphics2D g = bimage.createGraphics();
		g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
		g.drawImage(image, 0, 0, null);
		//System.out.println("out: " + bimage.getColorModel() + ", " + bimage.getSampleModel());
		return bimage;
		*/
	}


	private BufferedImage currentImage = null;
	private boolean closed = false;
	private String displayName = "";
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
				long sleepTime = Math.max(0,entry.getTimestamp() + delayMS - System.currentTimeMillis());
				//System.out.print("sleep time: " + sleepTime);
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			currentImage = entry.getImage();

			//if(this.getTopLevelAncestor() != null)
			//	this.getTopLevelAncestor().repaint();
			//else
			this.repaint();
			if(prevImage != null) {
				prevImage.flush();
			}

			//}
			prevImage = currentImage;
		}

	}, "refresh view panel");
	public void setDisplayName(String name) {
		this.displayName = name;
	}
	long prevRenderTime; 
	boolean disable = false;
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		//long currentInvoc = System.currentTimeMillis();
		//System.out.println(currentInvoc-prevRenderTime);
		//prevRenderTime = currentInvoc;
		Graphics2D g2 = (Graphics2D)g;


		if(currentImage != null && !disable)
		{
			g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
			g2.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
			//System.out.println("painting image");
			int w1 = getWidth(), h1 = getHeight(), w2 = currentImage.getWidth(), h2 = currentImage.getHeight();
			if(w1/h1>w2/h2)
				g2.drawImage(currentImage, 0, (h1-(w1*h2)/w2)/2, w1, (w1*h2)/w2,null);
			else 
				g2.drawImage(currentImage, (w1-(h1*w2)/h2)/2, 0, (h1*w2)/h2, h1,null);
			if(!"".contentEquals(displayName)) {
				int w3 = getFontMetrics(g.getFont()).stringWidth(displayName);
				int h3 = getFontMetrics(g.getFont()).getHeight()*3/2;
				g.drawString(displayName, w1/2-w3/2,h1-h3);
			}
			//g2.drawImage(currentImage, 0, 0,null);
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
