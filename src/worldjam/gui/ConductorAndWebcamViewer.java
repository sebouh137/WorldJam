package worldjam.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import worldjam.exe.Client;
import worldjam.gui.conductor.Conductor;
import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.time.DelayManager;
import worldjam.video.VideoFrame;
import worldjam.video.VideoSubscriber;
import worldjam.video.ViewPanel;

public class ConductorAndWebcamViewer extends JPanel implements ClockSubscriber, VideoSubscriber {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2249827020866967439L;
	Conductor conductor;
	JPanel viewerGrid = new JPanel(); 
	private Map<Long, ViewPanel> viewers = new HashMap<Long, ViewPanel>();
	ViewPanel selfieViewer;
	private int nRows;
	private int nColumns;
	private DelayManager delayManager;
	private Client client;
	static {
		System.setProperty("sun.awt.noerasebackground", "true");
	}
	//= new BezierConductor();
	public ConductorAndWebcamViewer(Conductor conductor, DelayManager dm, Client client){
		this.client = client;
		conductor.setOpaque(false);
		conductor.setStroke(new BasicStroke(8));
		conductor.setDrawShadow(true);
		//conductor.setBattonColor(new Color(1.f, 1.f, 1.f, .5f));
		conductor.setBattonColor(new Color(1.f, 0.f, 0.f, .7f));
		this.clock = conductor.getClock();
		
		
		this.conductor = conductor;
		
		this.setLayout(new OverlayLayout(this));
		this.add(conductor);
		selfieViewer = new ViewPanel(clock);
		selfieViewer.setVisible(false);
		selfieViewer.setMaximumSize(new Dimension(200,150));
		selfieViewer.setPreferredSize(new Dimension(200,150));
		selfieViewer.setLocation(0,0);
		//selfieViewer.setDebugMessage("selfieViewer")
		selfieViewer.setBounds(0, 0, 200,150);
		JPanel invisible = new JPanel();
		invisible.setOpaque(false);
		invisible.setLayout(new BorderLayout());
		JPanel invisible2 = new JPanel();
		invisible.add(invisible2, BorderLayout.NORTH);
		invisible2.setOpaque(false);
		invisible2.setLayout(new BorderLayout());
		invisible2.add(selfieViewer,BorderLayout.WEST);
		this.add(invisible);
		this.add(viewerGrid);
		viewerGrid.setBackground(Color.BLACK);
		viewerGrid.setLayout(new GridLayout(1,1));
		nRows = 1;
		nColumns = 1;
		this.delayManager = dm;
		this.setDoubleBuffered(true);
	}
	
	
	public void imageReceived(VideoFrame frame) {
		long sourceID = frame.getSourceID();
		//System.out.println("Source id " + sourceID);
		ViewPanel viewer = this.viewers.get(sourceID);
		if(sourceID == client.getSelfDescriptor().clientID){
			selfieViewer.setVisible(true);
			viewer = selfieViewer;
		}
		else if(viewer == null){
			viewer = new ViewPanel(clock);
			if(client != null)
				viewer.setDisplayName(client.getPeerDisplayName(frame.getSourceID()));
			if(delayManager != null){ 
				//the delay manager is only null during certain tests.  
				//otherwise connect the delay manager to the viewer.  
				DelayManager.DelayedChannel dc = delayManager.getChannel(sourceID);
				dc.addListener(viewer);
			}
			
			addViewer(viewer,sourceID);
			
		}
		viewer.imageReceived(frame);
	}
	
	

	private void addViewer(ViewPanel viewer, long id){
		//this.add(viewer, BorderLayout.CENTER);
		//viewers.put(id,viewer);
		//if(1==1)
		//	return;
		//this.add(viewer);
		//this.add(viewer);
		System.out.println("adding new view panel " + "id = " + id);
		viewers.put(id, viewer);
		//this.add(viewer);
		//if(true) return;
		if(viewers.size() > nRows*nColumns){
			int nChannels = viewers.size();
			nRows = (int)Math.ceil(Math.sqrt((double)nChannels));
			nColumns = (int)Math.ceil(nChannels/(double)nRows);
			viewerGrid.removeAll();
			viewerGrid.setLayout(new GridLayout(nRows,nColumns));
			//int i = 0;
			for(ViewPanel panel : viewers.values()){
				viewerGrid.add(panel);
			}
		}
		else {
			viewerGrid.add(viewer);
		}

		viewerGrid.revalidate();
	}
	/*public void updateChannels(HashMap<Long, Integer> activeIdsAndDelays) {
		boolean viewerListChanged = false;
		//add any new channels
		for(Long id : activeIdsAndDelays.keySet()){
			if(!viewers.containsKey(id)){
				viewers.put(id, new ViewPanel(clock));
				viewers.get(id).setDelay(activeIdsAndDelays.get(id));
				viewerListChanged=true;
			}
		}
		System.out.println("there are " + viewers.size() + " viewers");
		//remove any inactive channels
		ArrayList<Long> viewersToRemove = new ArrayList();
		for(long id : viewers.keySet()){
			if(activeIdsAndDelays.get(id) == null && id != 0){
				viewersToRemove.add(id);
			}
		}
		for(Long id : viewersToRemove){
			viewers.remove(id);
			viewerListChanged=true;
		}
		
		if(viewerListChanged){
			viewerGrid.removeAll();
			int nChannels = activeIdsAndDelays.size();
			int nRows = (int)Math.ceil(Math.sqrt((double)nChannels));
			int nCols = (int)Math.ceil(nChannels/(double)nRows);
			System.out.println(nRows + " " + nCols);
			viewerGrid.setLayout(new GridLayout(nRows,nCols));
			int i = 0;
			for(ViewPanel panel : viewers.values()){
				viewerGrid.add(panel, i);
				i++;
			}
		}
		System.out.println(viewers.size());
	}*/

	ClockSetting clock;
	@Override
	public void changeClockSettingsNow(ClockSetting clock) {
		this.clock = clock;
		for(ViewPanel vp : viewers.values()){
			vp.changeClockSettingsNow(clock);
		}
		if(selfieViewer != null){
			selfieViewer.changeClockSettingsNow(clock);
		}
	}
	

}
