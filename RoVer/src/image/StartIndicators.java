package image;
import java.io.File;
import java.io.FileInputStream;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StartIndicators extends Canvas{
	
	private double width;
	private double height;
	private GraphicsContext gc;
	
	private Image ready;
	private Image busy;
	private Image ignore;
	private Image readyGray;
	private Image busyGray;
	private Image ignoreGray;
	
	public StartIndicators(double width, double height) {
		super(width, height);
		this.width = width;
		this.height = height;
		gc = this.getGraphicsContext2D();
		
		String path = "Icons" + File.separator;
		try {
			ready = new Image(new FileInputStream(path + "Icon_HumReady_Start.png"));
			busy = new Image(new FileInputStream(path + "Icon_HumBusy_Start.png"));
			ignore = new Image(new FileInputStream(path + "Icon_HumIgnore_Start.png"));
			readyGray = new Image(new FileInputStream(path + "Icon_HumReady_gray.png"));
			busyGray = new Image(new FileInputStream(path + "Icon_HumBusy_gray.png"));
			ignoreGray = new Image(new FileInputStream(path + "Icon_HumIgnore_gray.png"));
		} catch (Exception e) {
			System.out.println("Error: starter indicator images did not load.");
		}
		
		gc.setFill(Color.GRAY);
		gc.setFont(Font.font("Veranda", FontWeight.THIN, 9));
		gc.fillText("AVAIL IN", 0, 10);
		gc.fillText("<empty>", 0, 20);
	}
	
	public void update(boolean isReady, boolean isBusy, boolean isIgnore, boolean readyAvail, boolean busyAvail, boolean ignoreAvail) {
		gc.clearRect(0, 0, width, height);
		gc.fillText("AVAIL IN", 0, 10);
		
		if (isReady)
			gc.drawImage(readyAvail?ready:readyGray,0, 12, 12, 12);
		if (isBusy)
			gc.drawImage(busyAvail?busy:busyGray, 14, 12, 12, 12);
		if (isIgnore)
			gc.drawImage(ignoreAvail?ignore:ignoreGray, 28, 12, 12, 12);
		
		if (!isReady && !isBusy && !isIgnore)
			gc.fillText("<empty>", 0, 20);
		
		/*
		if (isReady && isBusy && isIgnore) {
			gc.drawImage(readyAvail?ready:readyGray,0, 13, 10, 10);
			gc.drawImage(busyAvail?busy:busyGray, 13, 13, 10, 10);
			gc.drawImage(ignoreAvail?ignore:ignoreGray, 26, 13, 10, 10);
		}
		else if (isReady && isBusy && !isIgnore) {
			gc.drawImage(readyAvail?ready:readyGray,0, 13, 10, 10);
			gc.drawImage(busyAvail?busy:busyGray, 13, 13, 10, 10);
		}
		else if (isReady && !isBusy && isIgnore) {
			gc.drawImage(readyAvail?ready:readyGray,0, 13, 10, 10);
			gc.drawImage(ignoreAvail?ignore:ignoreGray, 13, 13, 10, 10);
		}
		else if (!isReady && isBusy && isIgnore) {
			gc.drawImage(busyAvail?busy:busyGray,0, 13, 10, 10);
			gc.drawImage(ignoreAvail?ignore:ignoreGray, 13, 13, 10, 10);
		}
		else if (!isReady && !isBusy && isIgnore) {
			gc.drawImage(ignoreAvail?ignore:ignoreGray, 0, 13, 10, 10);
		}
		else if (!isReady && isBusy && !isIgnore) {
			gc.drawImage(busyAvail?busy:busyGray, 0, 13, 10, 10);
		}
		else if (isReady && !isBusy && !isIgnore) {
			gc.drawImage(readyAvail?ready:readyGray, 0, 13, 10, 10);
		}
		else {
			gc.fillText("<empty>", 0, 20);
		}
		*/
		
	}

}
