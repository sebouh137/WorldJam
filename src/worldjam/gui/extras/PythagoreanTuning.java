package worldjam.gui.extras;

public class PythagoreanTuning implements Tuning{
	private static double corrections[] = {
			0,
			-9.77500432693913,
			3.91000173077484,
			-5.86500259615521,
			7.82000346154968,
			-1.95500086539182,
			11.7300051923245,
			1.95500086538739,
			-7.82000346154636,
			5.86500259616229,
			-3.91000173077279,
			9.77500432693718
	};
	@Override
	public double getAdjustment(int note) {
		return corrections[note];
	}

}
