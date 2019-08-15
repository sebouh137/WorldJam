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
import worldjam.util.DefaultObjects;

import worldjam.video.ViewPanel;
import worldjam.video.WebcamThread;

public class ConductorAndWebcamViewer extends JPanel{
	BezierConductor conductor;
	JPanel viewerGrid = new JPanel(); 
	private Map<Long, ViewPanel> viewers = new HashMap<Long, ViewPanel>();
	//= new BezierConductor();
	public ConductorAndWebcamViewer(BezierConductor conductor, ViewPanel viewer){
		conductor.setOpaque(false);
		conductor.setStroke(new BasicStroke(8));
		//conductor.setBattonColor(new Color(1.f, 1.f, 1.f, .5f));
		conductor.setBattonColor(new Color(1.f, 0.f, 0.f, .7f));
		
		this.conductor = conductor;
		this.setLayout(new OverlayLayout(this));
		this.add(conductor);
		this.add(viewerGrid);
		if(viewer != null) {
			//viewer.setPreferredSize(new Dimension(500,500));
			viewers.put(0L, viewer);
			viewerGrid.setLayout(new GridLayout(3,3));
			viewerGrid.add(viewer);
		}
		//conductor.setBounds(this.getBounds());
		//viewer.setBounds(this.getBounds());
	}
	public void imageReceived(long senderID, BufferedImage image, long timestamp) {
		ViewPanel viewer = this.viewers.get(senderID);
		if(viewer != null)
			viewer.imageReceived(image, timestamp);
	}
	public void updateChannels(HashMap<Long, Integer> activeIdsAndDelays) {
		boolean viewerListChanged = false;
		//add any new channels
		for(Long id : activeIdsAndDelays.keySet()){
			if(!viewers.containsKey(id)){
				viewers.put(id, new ViewPanel());
				viewers.get(id).setDelay(activeIdsAndDelays.get(id));
				viewerListChanged=true;
			}
		}
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
			viewerGrid.setLayout(new GridLayout(2,2));
			int i = 0;
			for(ViewPanel panel : viewers.values()){
				viewerGrid.add(panel, i);
				i++;
			}
		}
		System.out.println(viewers.size());
	}

}
