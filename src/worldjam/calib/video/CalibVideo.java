package worldjam.calib.video;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;

import worldjam.video.VideoFrame;
import worldjam.video.VideoSubscriber;
import worldjam.video.WebcamThread;

public class CalibVideo implements VideoSubscriber {

	public static void main(String arg[]){
		CalibVideo cv = new CalibVideo();
		CalibVideoWindow window = new CalibVideoWindow(cv);
		window.setVisible(true);

		Webcam webcam = Webcam.getDefault();
		webcam.open(false);
		WebcamThread wct = new WebcamThread(webcam);
		wct.addSubscriber(cv);
		wct.start();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	double getBrightness(BufferedImage image){
		int stepx = 10, stepy = 10;
		int sum = 0;
		for (int i = 0; i<image.getWidth(); i+=stepx){
			for(int j = 0; j< image.getHeight(); j+=stepy){
				int pixel = image.getRGB(i, j);

				int r =  (pixel >> 16) & 255; //green
				int g = (pixel >> 8) & 255; //green 
				int b = pixel & 255; //blue
				sum += r+g+b;
			}
		}
		return sum/((255.*3)*image.getWidth()*image.getHeight()/(stepx*stepy));
	}

	LinkedList<Double> recentBrightnesses = new LinkedList();
	public long actualFlashTime;

	boolean initiated = false;
	public void initiate() {
		this.initiated = true;
	}
	
	@Override
	public void imageReceived(VideoFrame frame) {
		if(!initiated) {
			return;
		}
		double timestamp = frame.getTimestamp();
		BufferedImage image = frame.getImage();
		double threshold = 5;


		double brightness = getBrightness(image);

		if(recentBrightnesses.size()>3){
			double recentBrightnessAvg = 0;
			for(double d : recentBrightnesses){
				recentBrightnessAvg += d/recentBrightnesses.size(); 
			}
			//System.out.println(brightness + " " + recentBrightnessAvg);
			if(brightness/recentBrightnessAvg > threshold){
				System.out.println("flash difference =  " + (timestamp-actualFlashTime) + " ms");
				flashDifferences.add((double) (timestamp-actualFlashTime));
				double avg = 0;
				for (int i = 0; i<flashDifferences.size(); i++) avg += flashDifferences.get(i)/flashDifferences.size();
				System.out.println(String.format("average flash differences = %.1f ms", avg));
			}

			if(recentBrightnesses.size() > 5)
				recentBrightnesses.removeFirst();
		}
		recentBrightnesses.add(brightness);
	}
	ArrayList <Double> flashDifferences = new ArrayList();
}
