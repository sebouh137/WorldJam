package worldjam.gui.conductor;

public class Segment {
	int type;
	double x[] = {0,0,0,0};
	double y[] = {0,0,0,0};
	
	double interpolate(double u, double z[]){
		switch(type){
		case 1:
			return (1-u)*z[0] + u*z[1];
		case 2:
			return (1-u)*(1-u)*z[0] + 2*u*(1-u)*z[1] + u*u*z[2];
		case 3:
			return (1-u)*(1-u)*(1-u)*z[0] + 3*u*(1-u)*(1-u)*z[1] + 3*u*u*(1-u)*z[2] + u*u*u*z[3];
		default:
			return (1-u)*z[0] + u*z[1];
		}
	}
	double interpolateX(double u){
		return interpolate(u, x);
	}
	double interpolateY(double u){
		return interpolate(u, y);
	}
}