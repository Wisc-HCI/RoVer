package model;

import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;


/*
 * Class that holds all the information about a Nail in a Transition
 */
class Nail extends Circle{
	Nail(DoubleProperty x, DoubleProperty y){
		super(x.get(), y.get(), 3);
	      setFill(Color.GOLD);
	      setStroke(Color.GOLD);
	      setStrokeWidth(2);
	      setStrokeType(StrokeType.OUTSIDE);

	      x.bind(centerXProperty());
	      y.bind(centerYProperty());
	      enableDrag();
	}
	
	private void enableDrag() {
	      final Delta dragDelta = new Delta();
	      setOnMousePressed(new EventHandler<MouseEvent>() {
	        @Override public void handle(MouseEvent mouseEvent) {
	          // record a delta distance for the drag and drop operation.
	          dragDelta.x = getCenterX() - mouseEvent.getX();
	          dragDelta.y = getCenterY() - mouseEvent.getY();
	          getScene().setCursor(Cursor.MOVE);
	        }
	      });
	      setOnMouseReleased(new EventHandler<MouseEvent>() {
	        @Override public void handle(MouseEvent mouseEvent) {
	          getScene().setCursor(Cursor.HAND);
	        }
	      });
	      setOnMouseDragged(new EventHandler<MouseEvent>() {
	        @Override public void handle(MouseEvent mouseEvent) {
	          double newX = mouseEvent.getX() + dragDelta.x;
	          if (newX > 0 && newX < getScene().getWidth()) {
	            setCenterX(newX);
	          }
	          double newY = mouseEvent.getY() + dragDelta.y;
	          if (newY > 0 && newY < getScene().getHeight()) {
	            setCenterY(newY);
	          }
	        }
	      });
	      setOnMouseEntered(new EventHandler<MouseEvent>() {
	        @Override public void handle(MouseEvent mouseEvent) {
	          if (!mouseEvent.isPrimaryButtonDown()) {
	            getScene().setCursor(Cursor.HAND);
	          }
	        }
	      });
	      setOnMouseExited(new EventHandler<MouseEvent>() {
	        @Override public void handle(MouseEvent mouseEvent) {
	          if (!mouseEvent.isPrimaryButtonDown()) {
	            getScene().setCursor(Cursor.DEFAULT);
	          }
	        }
	      });
	    }

	    // records relative x and y co-ordinates.
	    private class Delta { double x, y; }
	  }

