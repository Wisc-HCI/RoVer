package image;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class BugStats extends Canvas {
	
	private double width;
	private double height;
	private GraphicsContext gc;
	
	private HashMap<String,Integer> counts;
	private ArrayList<String> labels;
	private HashMap<String,Color> palette;
	
	private boolean showBars;
	
	final int maxBarWidth = 50;
	private int titleWidth;
	
	public BugStats(int width, int height, ArrayList<String> labels, HashMap<String,Color> palette, String title, int titleWidth, boolean showBars) {
		super(width,height);
		this.width = width;
		this.height = height;
		
		/*
		this.labels = labels;
		this.palette = palette;
		counts = new HashMap<String,Integer>();
		for (String lab : labels)
			counts.put(lab,0);
		
		gc = this.getGraphicsContext2D();
		this.showBars = showBars;
		
		this.titleWidth = titleWidth;
		
		gc.setFill(Color.GRAY);
		gc.setFont(Font.font("Veranda", FontWeight.THIN, 13));
		gc.fillText(title, 0, 23);
		update("initialize", null);
		*/
	}
	
	public void reinitialize() {
		/*
		for (String lab : labels)
			counts.put(lab,0);
		
		update("initialize", null);
		*/
	}
	
	public void update(String type, Integer val) {
		/*
		gc.clearRect(0+titleWidth,0,this.width, this.height);
		gc.setFill(Color.GRAY);
		gc.setFont(Font.font("Veranda", FontWeight.THIN, 11));
		//gc.setTextAlign(TextAlignment.RIGHT);

		// TODO: remove this hardcode
		gc.fillText("jams", 19.0+titleWidth, 12);
		gc.fillText("flubs", 18.5+titleWidth, 29);
		gc.fillText("conflicts", 0+titleWidth, 46);
		gc.strokeLine(50+titleWidth,0,50+titleWidth,50);
		
		// update the counts
		for (String lab : labels) {
			if (type.equals(lab)) {
				if (val == null)
					counts.put(lab, counts.get(lab) + 1);
				else
					counts.put(lab, val);
			}
		}
		
		// update the bars
		int max = 3;
		for (String lab : labels) {
			max = Math.max(max, counts.get(lab));
		}
		double pixUpdate = maxBarWidth*1.0/max;
		
		if (showBars) {
			gc.setFill(Color.BLACK);
			gc.fillRect(51+titleWidth, 0, pixUpdate*counts.get("jams") + 1, 16);
			gc.setFill(palette.get(labels.get(0)));
			gc.fillRect(51+titleWidth, 0, pixUpdate*counts.get("jams"), 15);
			gc.setFill(Color.GRAY);
			gc.fillText(counts.get("jams")+"", 53+pixUpdate*counts.get("jams")+titleWidth, 12);
			
			gc.setFill(Color.BLACK);
			gc.fillRect(51+titleWidth, 17, pixUpdate*counts.get("flubs") + 1, 16);
			gc.setFill(palette.get(labels.get(1)));
			gc.fillRect(51+titleWidth, 17, pixUpdate*counts.get("flubs"), 15);
			gc.setFill(Color.GRAY);
			gc.fillText(counts.get("flubs")+"", 53+pixUpdate*counts.get("flubs")+titleWidth, 29);
			
			gc.setFill(Color.BLACK);
			gc.fillRect(51+titleWidth, 34, pixUpdate*counts.get("conflicts") + 1, 16);
			gc.setFill(palette.get(labels.get(2)));
			gc.fillRect(51+titleWidth, 34, pixUpdate*counts.get("conflicts"), 15);
			gc.setFill(Color.GRAY);
			gc.fillText(counts.get("conflicts")+"", 53+pixUpdate*counts.get("conflicts")+titleWidth, 46);
		}
		else {
			gc.setFont(Font.font("Veranda", FontWeight.BOLD, 11));
			gc.setFill(palette.get(labels.get(0)));
			gc.fillText(counts.get("jams")+"", 53+titleWidth, 12);
			
			gc.setFill(palette.get(labels.get(1)));
			gc.fillText(counts.get("flubs")+"", 53+titleWidth, 29);
			
			gc.setFill(palette.get(labels.get(2)));
			gc.fillText(counts.get("conflicts")+"", 53+titleWidth, 46);
		}
		*/
	}

}
