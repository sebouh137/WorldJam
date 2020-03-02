package worldjam.gui.extras;
/**
 * Equal Temperament 12 tone 
 * @author spaul
 *
 */
public class Equi12Tuning implements Tuning {

	private double offset = 0;
	public Equi12Tuning() {
		this(0.);
	}
	
	public Equi12Tuning(double offsetInCents) {
		this.offset = offsetInCents;
	}

	@Override
	public double getAdjustment(int note) {
		return offset;
	}

}
