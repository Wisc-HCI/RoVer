package image;

import java.io.File;
import java.io.FileInputStream;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class EndIndicators extends Canvas{
	private double width;
	private double height;
	private GraphicsContext gc;
	
	private Image ready;
	private Image busy;
	private Image ignore;
	private Image readyGray;
	private Image busyGray;
	private Image ignoreGray;
	
	public EndIndicators(double width, double height) {
		super(width, height);
		this.width = width;
		this.height = height;
		gc = this.getGraphicsContext2D();
		
		String path = "Icons" + File.separator;
		try {
			ready = new Image(new FileInputStream(path + "Icon_HumReady.png"));
			busy = new Image(new FileInputStream(path + "Icon_HumBusy.png"));
			ignore = new Image(new FileInputStream(path + "Icon_HumIgnore.png"));
			readyGray = new Image(new FileInputStream(path + "Icon_HumReady_gray.png"));
			busyGray = new Image(new FileInputStream(path + "Icon_HumBusy_gray.png"));
			ignoreGray = new Image(new FileInputStream(path + "Icon_HumIgnore_gray.png"));
		} catch (Exception e) {
			System.out.println("Error: starter indicator images did not load.");
		}
		
		gc.setFill(Color.GRAY);
		gc.setFont(Font.font("Veranda", FontWeight.THIN, 9));
		gc.fillText("TOTAL OUT", 0, 10);
		gc.fillText("<empty>", 13, 20);
	}
	
	public void update(boolean isReady, boolean isBusy, boolean isIgnore) {
		gc.clearRect(0, 0, width, height);
		gc.fillText("TOTAL OUT", 0, 10);
		
		if (isReady)
			gc.drawImage(ready,12, 12, 12, 12);
		if (isBusy)
			gc.drawImage(busy, 26, 12, 12, 12);
		if (isIgnore)
			gc.drawImage(ignore, 40, 12, 12, 12);
		
		if (!isReady && !isBusy && !isIgnore)
			gc.fillText("<empty>", 13, 20);
		
		/*
		if (isReady && isBusy && isIgnore) {
			gc.drawImage(ready,0, 13, 10, 10);
			gc.drawImage(busy, 13, 13, 10, 10);
			gc.drawImage(ignore, 26, 13, 10, 10);
		}
		else if (isReady && isBusy && !isIgnore) {
			gc.drawImage(ready,0, 13, 10, 10);
			gc.drawImage(busy, 13, 13, 10, 10);
		}
		else if (isReady && !isBusy && isIgnore) {
			gc.drawImage(ready,0, 13, 10, 10);
			gc.drawImage(ignore, 13, 13, 10, 10);
		}
		else if (!isReady && isBusy && isIgnore) {
			gc.drawImage(busy,0, 13, 10, 10);
			gc.drawImage(ignore, 13, 13, 10, 10);
		}
		else if (!isReady && !isBusy && isIgnore) {
			gc.drawImage(ignore, 0, 13, 10, 10);
		}
		else if (!isReady && isBusy && !isIgnore) {
			gc.drawImage(busy, 0, 13, 10, 10);
		}
		else if (isReady && !isBusy && !isIgnore) {
			gc.drawImage(ready, 0, 13, 10, 10);
		}
		else {
			gc.fillText("<empty>", 0, 20);
		}
		*/
		
	}
}
