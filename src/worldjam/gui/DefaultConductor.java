package worldjam.gui;

import java.util.Scanner;

import javax.swing.JFrame;

import worldjam.core.BeatClock;
/**
 * Mimics a conductor's baton.
 * @author spaul
 *
 */
public class DefaultConductor extends Conductor{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9144008189374506436L;
	double x[];
	double y[];
	public DefaultConductor(BeatClock clock) {
		super(clock);	
	}

	public static void main(String arg[]){

		System.out.println("[ms per beat] [numerator] [denominator]");
		Scanner scanner = new Scanner(System.in);
		int msPerBeat = scanner.nextInt();
		int num = scanner.nextInt();
		int denominator = scanner.nextInt();
		scanner.close();
		BeatClock clock = new BeatClock(msPerBeat, num, denominator);

		JFrame frame = new DefaultConductor(clock).showInFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/*for(int i = 0; i<5; i++){
			try {
				Thread.sleep(new java.util.Random().nextInt(300));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			JFrame frame2 = new JFrame();
			frame2.add(new Conductor(clock));
			frame2.setSize(200, 200);
			frame2.setVisible(true);
			frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}*/
	}
	

	private double interpolate(double d, double e, double a) {
		return d+(e-d)*a;
	}
	
	protected double x(double t){
		
		double a = t%1;
		int i = (int)(t-a);
		return interpolate(x[i],x[(i+1)%x.length], Math.pow(a, 1.5));
	}

	protected double y(double t){
		double a = t%1;
		int i = (int)(t-a);
		return 1-interpolate(y[i],y[(i+1)%y.length], Math.pow(a, 3));
	}
	
	@Override
	public void setClock(BeatClock clock) {
		super.setClock(clock);
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
}
