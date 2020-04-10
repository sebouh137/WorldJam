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
	private DataInputStream inputStream;
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

		//System.out.println("in: " + image.getColorModel() + ", " + image.getSampleModel());
		BufferedImage bimage = gc.createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.OPAQUE);
		Graphics2D g = bimage.createGraphics();
		g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
		g.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
		g.drawImage(image, 0, 0, null);
		//System.out.println("out: " + bimage.getColorModel() + ", " + bimage.getSampleModel());
		return bimage;
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

	});

	long prevRenderTime; 
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		//long currentInvoc = System.currentTimeMillis();
		//System.out.println(currentInvoc-prevRenderTime);
		//prevRenderTime = currentInvoc;
		Graphics2D g2 = (Graphics2D)g;



		if(currentImage != null)
		{
			g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
			g2.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
			//System.out.println("painting image");
			//g2.drawImage(currentImage, 0, 0, getWidth(),getHeight(),null);

			g2.drawImage(currentImage, 0, 0,null);
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
