package controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.MicroBox;

public class ConditionBoard extends HBox {
	
	private Canvas ready;
	private Canvas busy;
	private Canvas ignore;
	
	private Image readyImg;
	private Image busyImg;
	private Image ignoreImg;

	public ConditionBoard() {
		//imageHolder.setStyle("-fx-border-color: lightgrey;");
		String path = "Icons" + File.separator;
		Canvas ready = new Canvas(100,50);
		Canvas busy = new Canvas(50,50);
		Canvas ignore = new Canvas(50,50);
		try {
			
			// add the images
			readyImg = new Image(new FileInputStream(path + "Icon_HumReady.png"));
			ready.getGraphicsContext2D().drawImage(readyImg, 55, 0, 40, 40);
			addConditionEventHandler(ready, readyImg, "ready");
			
			busyImg = new Image(new FileInputStream(path + "Icon_HumBusy.png"));
			busy.getGraphicsContext2D().drawImage(busyImg, 5, 0, 40, 40);
			addConditionEventHandler(busy, busyImg, "busy");
			
			ignoreImg = new Image(new FileInputStream(path + "Icon_HumIgnore.png"));
			ignore.getGraphicsContext2D().drawImage(ignoreImg, 5, 0, 40, 40);
			addConditionEventHandler(ignore, ignoreImg, "ignore");
			
			// add the text
			GraphicsContext readyGC = ready.getGraphicsContext2D();
			readyGC.setFill(Color.GRAY);
			readyGC.setFont(Font.font("Veranda", FontWeight.THIN, 10));
			readyGC.fillText("human is:    ready", 0, 48);
			
			GraphicsContext busyGC = busy.getGraphicsContext2D();
			busyGC.setFill(Color.GRAY);
			busyGC.setFont(Font.font("Veranda", FontWeight.THIN, 10));
			busyGC.fillText("busy", 13, 48);
			
			GraphicsContext ignoreGC = ignore.getGraphicsContext2D();
			ignoreGC.setFill(Color.GRAY);
			ignoreGC.setFont(Font.font("Veranda", FontWeight.THIN, 10));
			ignoreGC.fillText("susp'd", 9, 48);
			
		} catch (Exception e) {
			System.out.println("Error: starter indicator images did not load.");
		}
				
		this.getChildren().addAll(ready, busy, ignore);
	}
	
	private void addConditionEventHandler(Node node, Image img, String type) {
		node.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				
				Dragboard db = node.startDragAndDrop(TransferMode.COPY);
				//WritableImage snapshot = ((Canvas) node).snapshot(new SnapshotParameters(), null);
				//db.setDragView(snapshot);
				Canvas dragCanvas = new Canvas(20,20);
				dragCanvas.getGraphicsContext2D().drawImage(img, 0, 0, 20, 20);
				WritableImage snapshot = dragCanvas.snapshot(new SnapshotParameters(), null);
				db.setDragView(snapshot);
				
				ClipboardContent content = new ClipboardContent();
				content.putString("TransferCondition_" + type);
				//content.put(DataFormat.PLAIN_TEXT, type);
				//content.putString(type);
				db.setContent(content);

				event.consume();
			}

		});
	}
	
}
