package worldjam.calib.video;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class FlashPanel extends JPanel {
	FlashPanel(){
		this.setBackground(Color.BLACK);
	}
	Color color = Color.BLACK;
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.setColor(color);
		g.fillRect(0, 0, getWidth(), getHeight());;
	}
	public void flash(int delayInMS, int durationInMS, CalibVideo calibrator){
		
		new Thread(()->{
			try {
				long time = System.currentTimeMillis();
				if(calibrator != null){
					calibrator.actualFlashTime = time + delayInMS;
				}
				Thread.sleep(delayInMS);
				setBackground(Color.WHITE);
				color = Color.WHITE;
				this.getTopLevelAncestor().repaint();
				Thread.sleep(durationInMS);
				setBackground(Color.BLACK);
				color = Color.BLACK;
				repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		
	}

}
