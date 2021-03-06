package worldjam.gui.conductor;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import worldjam.audio.Metronome;
import worldjam.audio.PlaybackChannel;
import worldjam.audio.PlaybackManager;
import worldjam.gui.conductor.Conductor;
import worldjam.time.ClockSetting;
import worldjam.util.DefaultObjects;

import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public class ConductingPatternEditor extends JFrame{
	class EditorConductor extends Conductor{
		public EditorConductor(ClockSetting clock, ConductingPattern pattern) {
			super(clock, pattern);
			this.addMouseMotionListener(mouseAdapter);
			this.addMouseListener(mouseAdapter);
			this.addKeyListener(keyAdapter);
		}

		KeyAdapter keyAdapter = new KeyAdapter(){
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == 'g'){
					showGuides ^= true;
				}
				if(e.getKeyChar() == 'p'){
					showPath ^= true;
				}
				if(e.getKeyChar() == 'n'){
					showBeatNumbers ^= true;
				}
				if(e.getKeyChar() == 'm'){
					toggleMetronomeMute();
				}

			}


		};

		boolean showGuides = true;
		MouseAdapter mouseAdapter = new MouseAdapter(){

			@Override
			public void mouseDragged(MouseEvent e) {
				if(!showGuides)
					return;
				int segNum =selectedSegment;
				int anchor = selectedAnchor;
				if(segNum == -1 || anchor == -1){
					return;
				}
				BezierSegment sel = segments.get(segNum);
				sel.x[anchor]+= (e.getPoint().x - tipCoordX(sel.x[anchor]))/(.8*getWidth());
				sel.y[anchor]+= (e.getPoint().y - tipCoordY(sel.y[anchor]))/(.8*getHeight());
				//boundaries
				if(sel.x[anchor]<0) sel.x[anchor] = 0;
				if(sel.x[anchor]>1) sel.x[anchor] = 1;
				if(sel.y[anchor]<0) sel.y[anchor] = 0;
				if(sel.y[anchor]>1) sel.y[anchor] = 1;

				if(anchor == 0){
					BezierSegment prev = segments.get((selectedSegment+segments.size()-1)%segments.size());
					prev.x[prev.type] = sel.x[0];
					prev.y[prev.type] = sel.y[0];
				}

			}


			@Override
			public void mousePressed(MouseEvent e) {
				for(int j = 0; j< segments.size(); j++){
					BezierSegment segment = segments.get(j);
					for(int i = 0; i<segment.type; i++)
						if(Math.abs(e.getPoint().x - tipCoordX(segment.x[i])) <= 5
						&& Math.abs(e.getPoint().y - tipCoordY(segment.y[i])) <= 5){
							selectedSegment = j;
							selectedAnchor = i;
						}
				}
			}

			int selectedSegment;
			int selectedAnchor;

			@Override
			public void mouseReleased(MouseEvent e) {
				selectedSegment = -1;
				selectedAnchor = -1;
			}


		};
		Stroke stroke2 = new BasicStroke(1);
		private Stroke stroke3 = new BasicStroke(2);
		private boolean showPath;
		private boolean showBatton = true;
		private boolean showBeatNumbers = false;
		public void paint(Graphics g){
			super.paint(g);
			g.setColor(Color.RED);
			//System.out.println(clock.msPerBeat);
			/*if(showBatton){
				super.paint(g);
			}*/
			if(showBeatNumbers){
				for(int i = 0; i<segments.size(); i++){

					int x = tipCoordX(segments.get(i).x[0]);
					int y = tipCoordY(segments.get(i).y[0]);
					String text;
					if (segments.get(i).t1 % 1 == 0)
						text = Integer.toString((int)(segments.get(i).t1) + 1);
					else 
						text = "+";
					x-= g.getFontMetrics().stringWidth(text)/2;
					y+= g.getFontMetrics().getAscent()/2;

					BezierSegment prev = segments.get((i+segments.size()-1)%segments.size());

					//now create another offset, based on the previous segment's tangent line
					double dx = prev.x[prev.type]-prev.x[prev.type-1];
					double dy = prev.y[prev.type]-prev.y[prev.type-1];
					double d = Math.hypot(dx, dy);
					double offset = g.getFontMetrics().getAscent();
					offset+=1.5;

					x+=(int)offset*dx/d;
					y+=(int)offset*dy/d;

					g.drawString(text, x, y);
				}
			}

			Graphics2D g2 = (Graphics2D)g;
			if(showGuides){
				g2.setStroke(stroke2);
				for(BezierSegment segment : segments){
					switch(segment.type){
					case 3:
						g2.drawLine(
								tipCoordX(segment.x[2]), 
								tipCoordY(segment.y[2]), 
								tipCoordX(segment.x[3]), 
								tipCoordY(segment.y[3]));
						g2.drawRect(tipCoordX(segment.x[2])-2, tipCoordY(segment.y[2])-2, 5, 5);
					case 2:
						g2.drawLine(
								tipCoordX(segment.x[1]), 
								tipCoordY(segment.y[1]), 
								tipCoordX(segment.x[2]), 
								tipCoordY(segment.y[2]));
						g2.drawRect(tipCoordX(segment.x[1])-2, tipCoordY(segment.y[1])-2, 5, 5);
					case 1:
						g2.drawLine(
								tipCoordX(segment.x[0]), 
								tipCoordY(segment.y[0]), 
								tipCoordX(segment.x[1]), 
								tipCoordY(segment.y[1]));
					}
					g2.fillRect(tipCoordX(segment.x[0])-2, tipCoordY(segment.y[0])-2, 5, 5);
				}
			}
			if(showPath){
				g2.setStroke(stroke3 );
				for(BezierSegment segment : segments){
					int subsegments = 20;
					for(int i = 0; i<subsegments; i+=2){
						double u = i/(double)subsegments;
						double up = (i+1)/(double)subsegments;
						g2.drawLine(tipCoordX(segment.interpolateX(u)), 
								tipCoordY(segment.interpolateY(u)), 
								tipCoordX(segment.interpolateX(up)), 
								tipCoordY(segment.interpolateY(up)));
					}

				}
			}
		}
		private int tipCoordX(double d) {
			return (int)(getWidth()*(.1+.8*d));
		}
		private int tipCoordY(double d) {
			return (int)(getHeight()*(.1+.8*d));
		}


	}

	private static PlaybackManager pm;
	private static PlaybackChannel metronome;

	static private void toggleMetronomeMute() {
		metronome.setMuted(!metronome.isMuted());
		System.out.println("toggling metronome muting");
		
	}

	private static Icon metronomeIcon = new ImageIcon(ConductingPatternEditor.class.getResource("/worldjam/gui/icons/metronome.png"));
	private static Icon metronomeMutedIcon = new ImageIcon(ConductingPatternEditor.class.getResource("/worldjam/gui/icons/metronome_muted.png"));
	private JSpinner spinner;
	

	ConductingPatternEditor(){
		setSize(448, 383);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmNew = new JMenuItem("New...");
		mnFile.add(mntmNew);

		mntmNew.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel panel = new JPanel();
				panel.setLayout(new GridLayout(1,2));
				panel.add(new JLabel("enter number of beats"));
				JSpinner spinner = new JSpinner();
				spinner.setModel(new SpinnerNumberModel(4, 1, 12,1));
				panel.add(spinner);
				JOptionPane.showMessageDialog(null, panel);
				int nBeats = (int) spinner.getValue();

				newPattern(nBeats);

			}

		});

		JMenuItem mntmOpen = new JMenuItem("Open...");
		mnFile.add(mntmOpen);

		mntmOpen.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				chooser.showOpenDialog(ConductingPatternEditor.this);
				try {
					open(chooser.getSelectedFile());
					repaint();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}

		});

		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);

		mntmSave.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {

				try {
					if(openedFile == null){
						JFileChooser chooser = new JFileChooser();
						chooser.setCurrentDirectory(new File("."));
						chooser.showSaveDialog(ConductingPatternEditor.this);
						openedFile = chooser.getSelectedFile();
						setTitle(openedFile.getPath());
					}
					save(openedFile);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		});

		JMenuItem mntmSaveAs = new JMenuItem("Save As...");
		mnFile.add(mntmSaveAs);

		mntmSaveAs.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File("."));
				chooser.showSaveDialog(ConductingPatternEditor.this);
				try {
					File selectedFile = chooser.getSelectedFile();
					if(selectedFile == null )
						return;
					openedFile = selectedFile;
					save(openedFile);

					setTitle(openedFile.getPath());
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}

		});


		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);

		JCheckBoxMenuItem chckbxmntmShowGuides = new JCheckBoxMenuItem("Show guides");
		chckbxmntmShowGuides.setSelected(true);
		chckbxmntmShowGuides.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('g'));
		mnView.add(chckbxmntmShowGuides);
		chckbxmntmShowGuides.addActionListener(
				e-> {
					if(conductor != null)
						conductor.showGuides = chckbxmntmShowGuides.isSelected();
				}

				);


		JCheckBoxMenuItem chckbxmntmShowPath = new JCheckBoxMenuItem("Show Path");
		mnView.add(chckbxmntmShowPath);
		chckbxmntmShowPath.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('p'));
		chckbxmntmShowPath.addActionListener(
				e-> {if(conductor != null)
					conductor.showPath = chckbxmntmShowPath.isSelected();
				}

				);

		JCheckBoxMenuItem chckbxmntmShowBeats = new JCheckBoxMenuItem("Show Beat Numbers");
		mnView.add(chckbxmntmShowBeats);
		chckbxmntmShowBeats.setMnemonic(KeyEvent.getExtendedKeyCodeForChar('n'));

		
		chckbxmntmShowBeats.addActionListener(
				e-> {if(conductor != null)
					conductor.showBeatNumbers = chckbxmntmShowBeats.isSelected();
				}

				);

		getContentPane().setLayout(new BorderLayout());


		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		//panel.setLayout(new GridLayout(1, 3, 0, 0));

		JLabel labelBPM = new JLabel("BPM");
		labelBPM.setHorizontalAlignment(JLabel.RIGHT);
		spinner = new JSpinner();
		panel.add(spinner);
		JSpinner spinner_1 = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(500, 100, 3000, 10));
		cl = e -> {
			if(e.getSource() == spinner) {
				int val = (int)spinner.getValue();
				ClockSetting newSetting = conductor.getClock().createWithDifferentTempo(val);
				conductor.changeClockSettingsNow(newSetting);
				pm.changeClockSettingsNow(newSetting);
				spinner_1.removeChangeListener(cl);
				spinner_1.setValue(60000./val);
				spinner_1.addChangeListener(cl);
			}
			if(e.getSource() == spinner_1) {
				int val = (int)(60000./(double)spinner_1.getValue());
				ClockSetting newSetting = conductor.getClock().createWithDifferentTempo(val);
				conductor.changeClockSettingsNow(newSetting);
				pm.changeClockSettingsNow(newSetting);
				spinner.removeChangeListener(cl);
				spinner.setValue(val);
				spinner.addChangeListener(cl);
			}
		};
		spinner.addChangeListener(cl);
		panel.add(new JLabel("ms per beat      "));

		spinner_1.setModel(new SpinnerNumberModel(120., 20., 600., 1.));
		panel.add(spinner_1);
		spinner_1.addChangeListener(cl);
		//panel.add((Component)null);
		panel.add(labelBPM);
		
		button = new JButton(metronomeIcon);
		button.addActionListener(e->{
			boolean mute = !metronome.isMuted();
			metronome.setMuted(mute);
			if(mute) 
				button.setIcon(metronomeMutedIcon);
			else
				button.setIcon(metronomeIcon);
			
		});
		panel.add(button);
		
		newPattern(4);
	}

	JButton button;
	ChangeListener cl;
	void save(File file) throws FileNotFoundException {
		if(file == null)
			return;
		PrintWriter writer = new PrintWriter(file);
		writer.write(conductor.getPattern().toString());
		writer.close();
	}

	EditorConductor conductor;
	void open(File file) throws FileNotFoundException{
		ConductingPattern pattern = new ConductingPattern(null);
		if(file == null)
			return;
		pattern.read(file);
		openedFile = file;
		this.setTitle(file.getPath());
		int nBeats = pattern.getBeatsPerMeasure();
		if(conductor == null){
			conductor = new EditorConductor(new ClockSetting(500, nBeats, 4), pattern);
			getContentPane().add(conductor, BorderLayout.CENTER);
		}
		if(pattern.getBeatsPerMeasure() >= conductor.getClock().beatsPerMeasure){
			conductor.setPattern(pattern);
			ClockSetting clockSetting = new ClockSetting(500, nBeats, 4);
			conductor.changeClockSettingsNow(clockSetting);
			pm.changeClockSettingsNow(clockSetting);
		} else {
			ClockSetting clockSetting = new ClockSetting(500, nBeats, 4);
			conductor.changeClockSettingsNow(clockSetting);
			pm.changeClockSettingsNow(clockSetting);
			conductor.setPattern(pattern);
		}
	}

	void newPattern(int nBeats){
		ConductingPattern pattern = DefaultConductingPatternProvider.getInstance().getDefaultPattern(nBeats);
		int mspb = spinner != null ?  (int)spinner.getValue() : 500;
		if(conductor == null){
			conductor = new EditorConductor(new ClockSetting(mspb, nBeats, 4), pattern);
			getContentPane().add(conductor, BorderLayout.CENTER);
		}
		if(pattern.getBeatsPerMeasure() >= conductor.getClock().beatsPerMeasure){
			conductor.setPattern(pattern);
			ClockSetting clockSetting = new ClockSetting(mspb, nBeats, 4);
			conductor.changeClockSettingsNow(clockSetting);
			if(pm != null)
				pm.changeClockSettingsNow(clockSetting);
		} else {
			ClockSetting clockSetting = new ClockSetting(mspb, nBeats, 4);
			conductor.changeClockSettingsNow(clockSetting);
			if(pm != null)
				pm.changeClockSettingsNow(clockSetting);
			conductor.setPattern(pattern);
		}
		openedFile = null;
		setTitle("");
	}
	File openedFile;

	public static void main(String arg[]){
		ConductingPatternEditor editor = new ConductingPatternEditor();

		editor.setVisible(true);
		if(arg.length == 1)
			try {
				editor.open(new File(arg[0]));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		pm = new PlaybackManager(DefaultObjects.getOutputMixer(), editor.conductor.getClock(), DefaultObjects.defaultFormat);
		metronome = pm.getChannelByName("metronome");
		metronome.setMuted(true);
		pm.changeClockSettingsNow(editor.conductor.getClock());

	}
}
