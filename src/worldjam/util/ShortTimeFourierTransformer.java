package worldjam.util;

public class ShortTimeFourierTransformer implements Cloneable {
	public double Bi[];
	public double Br[];
	public double w[];
	private double dw[];
	private int nFreq;
	public double dt; 
	private double cr[]; // = real(exp((-a*dt + i*w)*dt))
	private double ci[]; // = imag(exp((-a*dt + i*w)*dt))
	//window function is a*exp(-a*(tau-t))
	private double a;
	
	private static double[] rangeLinear(double min, double max, int n){
		double [] ret = new double[n+1];
		for(int i = 0; i<n+1; i++){
			ret[i] = min + i*(max-min)/(double)n;
		}
		return ret;
	}
	
	private static double[] rangeLog(double min, double max, int n){
		double [] ret = new double[n+1];
		for(int i = 0; i<n+1; i++){
			ret[i] = min*Math.pow(max/min, i/(double)n);
		}
		return ret;
	}
	
	void setupMatrix(){
		nFreq = w.length;
		cr = new double[nFreq];
		ci = new double[nFreq];
		dw = new double[nFreq];
		for(int i = 0; i<nFreq; i++){
			cr[i] = Math.exp(-a*dt)*Math.cos(dt*w[i]);
			ci[i] = Math.exp(-a*dt)*Math.sin(dt*w[i]);
			if(i == 0)
				dw[i] = (w[1]-w[0])/2;
			else if(i == nFreq -1)
				dw[i] = (w[nFreq-1]-w[nFreq-2])/2;
			else{
				dw[i] = (w[i]+w[i+1])/2 - (w[i]+w[i-1])/2;
			}
		}
		
		
	}
	
	public ShortTimeFourierTransformer(double a, double dt, double fmin, double fmax, int n, boolean isLog){
		this.a = a;
		this.dt = dt;
		
		double wmin = 2*Math.PI*fmin;
		double wmax = 2*Math.PI*fmax;
		if(isLog)
			this.w = rangeLog(wmin, wmax, n);
		else
			this.w = rangeLinear(wmin, wmax, n);
		
		setupMatrix();
		this.Br = new double[nFreq];
		this.Bi = new double[nFreq];
	}
	
	public ShortTimeFourierTransformer(double a, double dt, double [] w){
		this.a = a;
		this.w = w;
		this.dt = dt;
		
		setupMatrix();
		this.Br = new double[nFreq];
		this.Bi = new double[nFreq];
	}
	public void nextSample(double x){
		
		double axdt = a*x*dt;
		double brnew, binew;
		for(int i = 0; i< w.length; i++){
			brnew = axdt+cr[i]*Br[i]-ci[i]*Bi[i];
			binew = ci[i]*Br[i]+cr[i]*Bi[i]; 
			Br[i] = brnew; 
			Bi[i] = binew;
		}
	}
	
	/*public double reverseTransform(double dt){
		double x = 0;
		double c = 1;
		double s = 0;
		for(int i = 0; i < w.length; i++){
			double br = Br[i];
			double bi = Bi[i];
			
			double dw = 0;
			if(i != 0 && i != w.length-1)
				dw = (w[i+1]-w[i-1])/2;
			else if(i == 0)
				dw = (w[1]-w[0])/2;
			else if(i == w.length-1)
				dw = (w[w.length-1]-w[w.length-2])/2;
			
				c = Math.cos(w[i]*dt);
				s = Math.sin(w[i]*dt);
			
			
			x+= 2*(c*br-s*bi)*dw;
		}
		return x*Math.exp(a*dt)/(2*Math.PI);
	}*/
	
	public double reverseTransform(){
		double x = 0;
		for(int i = 0; i < w.length; i++){
			x += Br[i]*dw[i];
		}
		return x/(Math.PI*a);
	}
	
	public double reverseTransform(double Brp[]){
		double x = 0;
		for(int i = 0; i < w.length; i++){
			x += Brp[i]*(w[i+1]-w[i]);
		}
		return x/(Math.PI*a);
	}
	
	public static void main(String arg[]) throws Exception{
		/*
		//resolution is 10 cents.  nsteps = 10 octaves * 1200 cents/octave / 10 cents
		ShortTimeFourierTransformer transf = new ShortTimeFourierTransformer(10, 20, 20000, 1200, true);
		
		double dt = 1/44100.;
		double f = 440;
		for(int i = 0; i<4000; i++){
			double t = i*dt;
			double x = Math.sin(2*Math.PI*i*dt*f);
			transf.nextSample(x,dt);
			double xp = transf.reverseTransform(.001);
			System.out.println(t + "\t" + x + "\t" +xp);
		}
		*/
		

		double dt = 1/44100.;
		//ShortTimeFourierTransformer transf = new ShortTimeFourierTransformer(10, dt, 0, 22050, 22050/5, false);
		ShortTimeFourierTransformer transf = new ShortTimeFourierTransformer(10, dt, 22050/1024., 22050, 1200, true);
		
		double f = 440;
		int N = 4410000;
		double x[] = new double[N];
		for(int i = 0; i<N; i++){
			double t = i*dt;
			 
			
			x[i] = Math.sin(2*Math.PI*t*f);
			//if(t>50)
				//x[i] = 0;
		}
		long t0 = System.currentTimeMillis();
		
		for(int i = 0; i<N; i++){

			double t = i*dt;
			transf.nextSample(x[i]);
			double rev = transf.reverseTransform();
			if(i%10000 == 0)
			  System.out.println(t + "\t" + x[i] + "\t" + Math.hypot(transf.Br[440], transf.Bi[440]) + "\t" + rev);
		}
		long t1 = System.currentTimeMillis();
		System.out.println("wall time:" + (t1-t0) + " ms");
		
	}
	
	@Override
	public Object clone(){
		try {
			ShortTimeFourierTransformer transformer = (ShortTimeFourierTransformer)super.clone();
			transformer.Br = transformer.Br.clone();
			transformer.Bi = transformer.Bi.clone();
			return transformer;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
