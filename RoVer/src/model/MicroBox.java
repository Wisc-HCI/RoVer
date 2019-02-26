package model;

import java.io.File;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

//TODO URGENT: Merge code into Microinteraction class. This class is not to be used.
//All instances of microbox should be replaced with the  microinteraction class 

public class MicroBox extends StackPane {
	private File micro;
	private Microinteraction microint;
	private String type;
	private Text text;
	
	// graphical components
	Rectangle rect;
	Rectangle rectInner;
	Rectangle flashingRect;

	public MicroBox(File micro, String type, Color color) {
		this.microint = null;
		this.micro = micro;
		this.type = type;
		initialize(color);
	}
	
	public MicroBox(File micro, String type, Color color, MicroBox obj) {
		this.microint = null;
		this.micro = micro;
		this.type = type;
		initialize(color);
	}
	
	public void initialize(Color color) {
		//TODO Replace the two rectangles with a single rectangle with a black border 6px thick
		rect = new Rectangle(150, 32, Color.SILVER);
		rectInner = new Rectangle(146, 28, color);
		flashingRect = new Rectangle(146, 28, Color.TRANSPARENT);
		rect.setArcHeight(16);
		rect.setArcWidth(16);
		rectInner.setArcHeight(14);
		rectInner.setArcWidth(14);
		flashingRect.setArcHeight(14);
		flashingRect.setArcWidth(14);
		text = new Text(micro.getName().substring(0, micro.getName().lastIndexOf(".")));
		text.setStyle("-fx-font: 10 Trebuchet MS;");
		//text.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);
		getChildren().addAll(rect, rectInner, flashingRect, text);
	}
	
	public void makeSmaller() {
		rectInner.setWidth(82);
		rectInner.setHeight(16);
		flashingRect.setWidth(82);
		flashingRect.setHeight(16);
		rect.setWidth(84);
		rect.setHeight(18);
	}
	
	public void setMBColor(Color c) {
		rectInner.setFill(c);
	}

	public String getName() {
		return micro.getName();
	}
	
	public File getFile() {
		return micro;
	}
	
	public String getType() {
		return type;
	}
	
	public void setMicrointeraction(Microinteraction micro) {
		this.microint = micro;
	}
	
	public Microinteraction getMicrointeraction() {
		return microint;
	}
	
	public Rectangle getOutline() {
		return rect;
	}
	
	public Rectangle getRect() {
		return rectInner;
	}
	
	public Rectangle getFlashingRect() {
		return flashingRect;
	}
	
	public Text getText() {
		return text;
	}
	
}
