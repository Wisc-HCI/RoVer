package image;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import model.GroupTransition;

public class Conditions extends HBox {
	
	private Canvas ready;
	private Canvas busy;
	private Canvas ignore;
	
	private Image readyImg;
	private Image busyImg;
	private Image ignoreImg;
	
	private GroupTransition mt;
	
	public Conditions(GroupTransition mt) {
		this.mt = mt;
		
		String path = "Icons" + File.separator;
		try {
			ready = new Canvas(12,12);
			readyImg = new Image(new FileInputStream(path + "Icon_HumReady.png"));
			ready.getGraphicsContext2D().drawImage(readyImg, 1,1, 10, 10);
			busy = new Canvas(12,12);
			busyImg = new Image(new FileInputStream(path + "Icon_HumBusy.png"));
			busy.getGraphicsContext2D().drawImage(busyImg, 1,1, 10, 10);
			ignore = new Canvas(12,12);
			ignoreImg = new Image(new FileInputStream(path + "Icon_HumIgnore.png"));
			ignore.getGraphicsContext2D().drawImage(ignoreImg, 1,1, 10, 10);
		} catch (Exception e) {
			System.out.println("Error: starter indicator images did not load.");
		}
		
		this.getChildren().addAll(ready, busy, ignore);
		
	}
	
	public ArrayList<Canvas> getCanvases() {
		ArrayList<Canvas> canvases = new ArrayList<Canvas>();
		canvases.add(ready);
		canvases.add(busy);
		canvases.add(ignore);
		return canvases;
	}
	
	public ArrayList<Image> getImages() {
		ArrayList<Image> images = new ArrayList<Image>();
		images.add(readyImg);
		images.add(busyImg);
		images.add(ignoreImg);
		return images;
	}
	
	public ArrayList<String> getTypes() {
		ArrayList<String> types = new ArrayList<String>();
		types.add("ready");
		types.add("busy");
		types.add("ignore");
		return types;
	}
	
	public GroupTransition getMt() {
		return mt;
	}
	
	public boolean update(boolean[] humanBranching) {
		boolean changed = mt.setAllHumanBranching(humanBranching);
		mt.updateAndDisplayConditions();
		for (Node node : this.getChildren()) {
			Canvas canv = (Canvas) node;
			canv.getGraphicsContext2D().clearRect(0, 0, 12, 12);
		}
		
		if (humanBranching[0])
			ready.getGraphicsContext2D().drawImage(readyImg, 1,1, 10, 10);
		if (humanBranching[1])
			busy.getGraphicsContext2D().drawImage(busyImg, 1,1, 10, 10);
		if (humanBranching[2])
			ignore.getGraphicsContext2D().drawImage(ignoreImg, 1,1, 10, 10);
		
		return changed;
	}
	
}
