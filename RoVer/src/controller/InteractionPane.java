package controller;

import java.util.ArrayList;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import model.Group;

//Holds microcollections Similar to drawArea
public class InteractionPane extends Pane{

	DoubleProperty myScale = new SimpleDoubleProperty(1.0);
	private MainController mc;
	private ArrayList<Group> collections;
	
	public InteractionPane(MainController mc) {
		this.mc = mc;
		collections = new ArrayList<Group>();
		setStyle("-fx-background-color: #F9FFFF");

		setPrefSize(600, 600);
//		setStyle("-fx-background-color: #FFFFFF; -fx-border-color: blue;");

		// add scale transform
		scaleXProperty().bind(myScale);
		scaleYProperty().bind(myScale);
	}

	/**
	 * Add a grid to the canvas, send it to back
	 */
	public void addGrid() {

		double w = getBoundsInLocal().getWidth();
		double h = getBoundsInLocal().getHeight();

		// add grid
		Canvas grid = new Canvas(w, h);

		// don't catch mouse events
		grid.setMouseTransparent(true);

		GraphicsContext gc = grid.getGraphicsContext2D();

		gc.setStroke(Color.GRAY);
		gc.setLineWidth(1);

		// draw grid lines
		double offset = 50;
		for( double i=offset; i < w; i+=offset) {
			gc.strokeLine( i, 0, i, h);
			gc.strokeLine( 0, i, w, i);
		}

		getChildren().add( grid);

		grid.toBack();
	}

	public double getScale() {
		return myScale.get();
	}

	public void setScale( double scale) {
		myScale.set(scale);
	}

	public void setPivot( double x, double y) {
		setTranslateX(getTranslateX()-x);
		setTranslateY(getTranslateY()-y);
	}
	
	public void addCollection(Group collection, double x, double y){
		collections.add(collection);
		
		collection.setLayoutX(x);
		collection.setLayoutY(y);
		getChildren().add(collection);
		getChildren().add(collection.getEnderIndicatorLights());
		getChildren().add(collection.getStarterIndicatorLights());
		collection.refresh();
	}
	
	public void removeIndicators(Group collection) {
		getChildren().remove(collection.getEnderIndicatorLights());
		getChildren().remove(collection.getStarterIndicatorLights());
	}
	
	public void addIndicators(Group collection) {
		getChildren().add(collection.getEnderIndicatorLights());
		getChildren().add(collection.getStarterIndicatorLights());
	}
	
	public void removeCollection(Group collection){
		collections.remove(collection);
		getChildren().remove(collection);
	}
	
	public void removeAllCollections() {
		collections.clear();
		getChildren().clear();
	}
}


/**
 * Mouse drag context used for scene and nodes.
 */
class DragContext {

	double mouseAnchorX;
	double mouseAnchorY;

	double translateAnchorX;
	double translateAnchorY;

}

/**
 * Listeners for making the nodes draggable via left mouse button. Considers if parent is zoomed.
 */
class NodeGestures {

	private DragContext nodeDragContext = new DragContext();

	InteractionPane canvas;

	public NodeGestures( InteractionPane canvas) {
		this.canvas = canvas;

	}

	public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
		return onMousePressedEventHandler;
	}

	public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
		return onMouseDraggedEventHandler;
	}

	private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

		public void handle(MouseEvent event) {

			// left mouse button => dragging
			if( !event.isPrimaryButtonDown())
				return;

			nodeDragContext.mouseAnchorX = event.getSceneX();
			nodeDragContext.mouseAnchorY = event.getSceneY();

			Node node = (Node) event.getSource();

			nodeDragContext.translateAnchorX = node.getTranslateX();
			nodeDragContext.translateAnchorY = node.getTranslateY();

		}

	};

	private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
		public void handle(MouseEvent event) {

			// left mouse button => dragging
			if( !event.isPrimaryButtonDown())
				return;

			double scale = canvas.getScale();

			Node node = (Node) event.getSource();

			node.setTranslateX(nodeDragContext.translateAnchorX + (( event.getSceneX() - nodeDragContext.mouseAnchorX) / scale));
			node.setTranslateY(nodeDragContext.translateAnchorY + (( event.getSceneY() - nodeDragContext.mouseAnchorY) / scale));

			event.consume();

		}
	};
}

/**
 * Listeners for making the scene's canvas draggable and zoomable
 */
class SceneGestures {

	private static final double MAX_SCALE = 2.0d;
	private static final double MIN_SCALE = 0.5d;

	private DragContext sceneDragContext = new DragContext();

	InteractionPane canvas;

	public SceneGestures( InteractionPane canvas) {
		this.canvas = canvas;
	}

	public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
		return onMousePressedEventHandler;
	}

	public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
		return onMouseDraggedEventHandler;
	}

	public EventHandler<ScrollEvent> getOnScrollEventHandler() {
		return onScrollEventHandler;
	}

	private EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

		public void handle(MouseEvent event) {

			// right mouse button => panning
			if( !event.isSecondaryButtonDown())
				return;

			sceneDragContext.mouseAnchorX = event.getSceneX();
			sceneDragContext.mouseAnchorY = event.getSceneY();

			sceneDragContext.translateAnchorX = canvas.getTranslateX();
			sceneDragContext.translateAnchorY = canvas.getTranslateY();

		}

	};

	private EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
		public void handle(MouseEvent event) {

			// right mouse button => panning
			if( !event.isSecondaryButtonDown())
				return;

			canvas.setTranslateX(sceneDragContext.translateAnchorX + event.getSceneX() - sceneDragContext.mouseAnchorX);
			canvas.setTranslateY(sceneDragContext.translateAnchorY + event.getSceneY() - sceneDragContext.mouseAnchorY);

			event.consume();
		}
	};

	/**
	 * Mouse wheel handler: zoom to pivot point
	 */
	private EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

		@Override
		public void handle(ScrollEvent event) {

			double delta = 1.05;

			double scale = canvas.getScale(); // currently we only use Y, same value is used for X
			double oldScale = scale;

			if (event.getDeltaY() < 0)
				scale /= delta;
			else
				scale *= delta;

			scale = clamp( scale, MIN_SCALE, MAX_SCALE);

			double f = (scale / oldScale)-1;

			double dx = (event.getSceneX() - (canvas.getBoundsInParent().getWidth()/2 + canvas.getBoundsInParent().getMinX()));
			double dy = (event.getSceneY() - (canvas.getBoundsInParent().getHeight()/2 + canvas.getBoundsInParent().getMinY()));

			canvas.setScale( scale);

			// note: pivot value must be untransformed, i. e. without scaling
			canvas.setPivot(f*dx, f*dy);

			event.consume();

		}

	};


	public static double clamp( double value, double min, double max) {

		if( Double.compare(value, min) < 0)
			return min;

		if( Double.compare(value, max) > 0)
			return max;

		return value;
	}
}
