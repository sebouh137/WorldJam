package worldjam.gui.extras;

public interface Tuning {
	/**
	 * 
	 * @param selectedNote 0=C, 1=C#/Db, etc. 
	 * @return the difference, in cents, between the given note in this tuning 
	 * and its pitch in the standard tuning (equal temperament 12-tone, with A = 440)
	 */
	public double getAdjustment(int note);
	
}
