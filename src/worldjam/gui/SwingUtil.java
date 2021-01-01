package worldjam.gui;

import javax.swing.JSpinner;

public class SwingUtil {
	public static void setSpinnerAlignment(JSpinner spinner, int alignment){
		if(spinner.getEditor() instanceof JSpinner.DefaultEditor) {
			JSpinner.DefaultEditor editor = ((JSpinner.DefaultEditor)spinner.getEditor());
			editor.getTextField().setHorizontalAlignment(alignment);
			editor.revalidate();
		}
	}
}
