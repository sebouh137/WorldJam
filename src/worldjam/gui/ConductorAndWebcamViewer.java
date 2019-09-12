package worldjam.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import worldjam.gui.conductor.BezierConductor;
import worldjam.time.ClockSetting;
import worldjam.time.ClockSubscriber;
import worldjam.time.DelayManager;
import worldjam.util.DefaultObjects;

import worldjam.video.ViewPanel;
import worldjam.video.WebcamThread;

public class ConductorAndWebcamViewer extends JPanel implements ClockSubscriber {
	BezierConductor conductor;
	JPanel viewerGrid = new JPanel(); 
	private Map<Long, ViewPanel> viewers = new HashMap<Long, ViewPanel>();
	private int nRows;
	private int nColumns;
	private DelayManager delayManager;
	//= new BezierConductor();
	public ConductorAndWebcamViewer(BezierConductor conductor, DelayManager dm){
		conductor.setOpaque(false);
		conductor.setStroke(new BasicStroke(8));
		//conductor.setBattonColor(new Color(1.f, 1.f, 1.f, .5f));
		conductor.setBattonColor(new Color(1.f, 0.f, 0.f, .7f));
		this.clock = conductor.getClock();
		
		
		this.conductor = conductor;
		this.setLayout(new OverlayLayout(this));
		this.add(conductor);
		this.add(viewerGrid);
		viewerGrid.setBackground(Color.BLACK);
		viewerGrid.setLayout(new GridLayout(1,1));
		nRows = 1;
		nColumns = 1;
		this.delayManager = dm;
	}
	
	public void imageReceived(long senderID, BufferedImage image, long timestamp) {
		
		ViewPanel viewer = this.viewers.get(senderID);
		if(viewer == null){
			viewer = new ViewPanel(clock);
			if(delayManager != null){ 
				//the delay manager is only null during certain tests.  
				//otherwise connect the delay manager to the viewer.  
				DelayManager.DelayedChannel dc = delayManager.getChannel(senderID);
				dc.addListener(viewer);
			}
			addViewer(viewer,senderID);
			
		}
		viewer.imageReceived(image, timestamp);
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
			int i = 0;
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
	}
	

}
