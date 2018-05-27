package worldjam.gui;

import java.awt.Graphics;
import java.util.Scanner;

import javax.swing.JFrame;

import worldjam.core.BeatClock;
import worldjam.test.DefaultObjects;
/**
 * Mimics a conductor's baton.
 * @author spaul
 *
 */
public class Conductor extends VisualMetronome{

	double x[];
	double y[];
	public Conductor(BeatClock clock) {
		super(clock);
		int N = clock.beatsPerMeasure;
		x = new double[N];
		y = new double[N];
		x[0] = .5;
		y[0] = 0;
		for(int i = 1; i<=N-2; i++){
			x[i] = (i+N+1)%2 == 0 ? 0 : 1;
			y[i] = i/(double)(N-1);
		}
		x[N-1] = .5;
		y[N-1] = 1;
	}

	public static void main(String arg[]){
		
		System.out.println("[ms per beat] [numerator] [denominator]");
		Scanner scanner = new Scanner(System.in);
		int msPerBeat = scanner.nextInt();
		int num = scanner.nextInt();
		int denominator = scanner.nextInt();
		BeatClock clock = new BeatClock(msPerBeat, num, denominator);
		
		JFrame frame = new JFrame();
		frame.add(new Conductor(clock));
		frame.setSize(200, 200);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		/*try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JFrame frame2 = new JFrame();
		frame2.add(new Conductor(clock));
		frame2.setSize(200, 200);
		frame2.setVisible(true);
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/
	}
	public void paint(Graphics g){
		double t = ((System.currentTimeMillis() - clock.startTime)%(clock.msPerBeat*clock.beatsPerMeasure))/(double)clock.msPerBeat;
		
		
		//g.fillOval(x(t), y(t), 20, 20);
		
		double a = t%1;
		int i = (int)(t-a);
		double x1 =  interpolate(x[i],x[(i+1)%x.length], Math.pow(a, 1.5));
		double y1 =  1-interpolate(y[i],y[(i+1)%y.length], Math.pow(a, 3));
		
		g.drawLine(
				(int)(getWidth()*(.1+.8*x1)), 
				(int)(getHeight()*(.1+.8*y1)), 
				(int)(getWidth()*(.1+.4*x1)), 
				(int)(getHeight()*(.25+.4*y1))
			);
		
	}
	
	
	
	
	private double interpolate(double d, double e, double a) {
		// TODO Auto-generated method stub
		return d+(e-d)*a;
	}

	int x(double t){
		/*if(t>clock.beatsPerMeasure-2 && t < clock.beatsPerMeasure-1){
			double a = (t-(clock.beatsPerMeasure-2));
			return (int)(getWidth()/3*(1+4*(a-a*a)));
		}
		else{
			return (int) (getWidth()*.3);
		}*/
		double a = t%1;
		int i = (int)(t-a);
		return (int)(getWidth()*(.1+.8*interpolate(x[i],x[(i+1)%x.length], Math.pow(a, 1.5))));
	}
	
	int y(double t){
		//t = t%1;
		//return (int)(getHeight()-getHeight()*(t*t-t*t*t)*6);
		double a = t%1;
		int i = (int)(t-a);
		return (int)(getHeight()*(.9-.8*interpolate(y[i],y[(i+1)%y.length], Math.pow(a, 3))));
	}
}
