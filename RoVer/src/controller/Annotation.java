package controller;
import java.awt.Point;
import java.util.ArrayList;

import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.Guard;
import model.State;
import model.Sync;
import model.Transition;
import model.Update;

/*
 * Class that holds the annotations for the state and transition
 * 
 * Holds all the necessary controllers for the text box
 * 
 * Processes and formats all the strings for the transitions
 */

public class Annotation extends Text {

	private double X, Y;
	private String text;
	private Color color;
	private Pane pane;
	private MainController mc;
	private int objType, isCollapse;
	public static String FONT;
	public static int FONTSIZE;
	private Object obj;
		
	public Annotation(State state, Pane pane, MainController mc) {
		obj = state;
		isCollapse = 0;
		objType = 0;
		text = formatStr(0);
		color = Color.PURPLE;
		setText(text);
		X = (state.getX() - getWidth() / 2);
		Y = (state.getY() - 25);
		this.pane = pane;
		this.mc = mc;
		initialize();
	}
	
	public Annotation(ArrayList<Object> objects, String objType, Point xy, Pane pane, MainController mc) {
		if (objType.equals("Guard")) {
			this.objType = 1;
			color = Color.GREEN;
		}
		else if (objType.equals("Update")) {
			this.objType = 2;
			color = Color.MAROON;
		}
		else {
			this.objType = 3;
			color = Color.CYAN;
		}
		
		obj = objects;
		
		isCollapse = 0;
		text = formatStr(this.objType);
		setText(text);
		X = xy.x;
		Y = xy.y;
	}
	
	public void addMC(MainController mc) {
		this.mc = mc;
	}
	
	public void addPane(Pane pane) {
		this.pane = pane;
	}

	//This code has been taken from the node class in the javafx package	
	public void relocate(double X, double Y, int mode) {
		if (mode == 0) {
			setLayoutX(X - getLayoutBounds().getMinX());
			setLayoutY(Y - getLayoutBounds().getMinY());
			this.X = X;
			this.Y = Y;
		} else {
			setLayoutX((this.X + X) - getLayoutBounds().getMinX());
			setLayoutY((this.Y + Y) - getLayoutBounds().getMinY());
			this.X = this.X + X;
			this.Y = this.Y + Y;
		}

	}

	//Formats the strings as needed depending on the mode
	private String formatStr(int mode) {
		String str = "";
		switch (mode) {
		case 0:
			return ((State) obj).getName().trim();
		case 1:
			ArrayList<Guard> guards = ((ArrayList<Guard>)(ArrayList<?>) obj);
			for (Guard g : guards) {
				System.out.println(g);
				str += g.stringify() + "\n";
			}

			return str;
		case 2:
			ArrayList<Update> updates = ((ArrayList<Update>)(ArrayList<?>) obj);
			for (Update u : updates)
				str += u.stringify() + "\n";
			return str;
		case 3:
			ArrayList<Sync> syncs = ((ArrayList<Sync>)(ArrayList<?>) obj);
			for (Sync s : syncs)
				str += s.stringify() + "\n";
			return str;
		case 4:
			if (objType == 0) {
				return (((State) obj).getName().charAt(0) + "").toUpperCase();
			} else if (objType == 1) {
				String transFin = "Transition: " + ((Transition) obj).getSource().getName() + " -> "
						+ ((Transition) obj).getTarget().getName();
				return transFin;
			}
		}
		return null;
	}

	
	/*
	 * Method used to set all the controllers for the Annotation box
	 * 
	 * Initializes context menus as well as mouse event handlers
	 */
	public void initialize() {
		setFill(color);
		setFont(Font.font(FONT, FONTSIZE));
		setX(X);
		setY(Y);

		this.setOnMouseEntered(e -> {
			setFill(Color.ORANGE);
		});
		this.setOnMouseExited(e -> {
			setFill(color);
		});

		this.setOnMouseDragged(e -> {
			Point2D locationInScene = new Point2D(e.getSceneX(), e.getSceneY());
			Point2D locationInParent = pane.sceneToLocal(locationInScene);

			if (mc.getFlag() == 3) {
				double X = locationInParent.getX();
				double Y = locationInParent.getY();
				int width = getWidth() / 2;
				int height = getHeight() / 2;
				if (X >= width && Y >= height && X < pane.getWidth() - width && Y < pane.getHeight() - height) {
					relocate(X - width, Y - height, 0);
				}
			}
		});

		this.setOnContextMenuRequested(e -> {
			ContextMenu contextMenu = new ContextMenu();
			Menu selectColor = new Menu("Color");
			Menu selectFont = new Menu("Font Size");
			MenuItem collapse;
			if (isCollapse == 0)
				collapse = new MenuItem("Collapse");

			else
				collapse = new MenuItem("Expand");
			contextMenu.getItems().addAll(selectColor, selectFont, collapse);
			contextMenu.show(this, e.getScreenX(), e.getScreenY());

			MenuItem blue = new MenuItem("Blue");

			blue.setOnAction((m) -> {
				setColor(Color.BLUE);
			});
			MenuItem red = new MenuItem("Red");
			red.setOnAction((m) -> {
				setColor(Color.RED);
			});
			MenuItem green = new MenuItem("Green");
			green.setOnAction((m) -> {
				setColor(Color.GREEN);
			});
			MenuItem black = new MenuItem("Black");
			black.setOnAction((m) -> {
				setColor(Color.BLACK);
			});
			MenuItem pink = new MenuItem("Pink");
			pink.setOnAction((m) -> {
				setColor(Color.PINK);
			});
			MenuItem gray = new MenuItem("Gray");
			gray.setOnAction((m) -> {
				setColor(Color.GRAY);
			});

			selectColor.getItems().addAll(blue, red, green, pink, gray, black);

			MenuItem f10 = new MenuItem("10");
			f10.setOnAction((m) -> {
				setFont(Font.font(FONT, 14));
			});

			MenuItem f12 = new MenuItem("12");
			f12.setOnAction((m) -> {
				setFont(Font.font(FONT, 16));
			});
			MenuItem f14 = new MenuItem("14");
			f14.setOnAction((m) -> {
				setFont(Font.font(FONT, 18));
			});
			MenuItem f16 = new MenuItem("16");
			f16.setOnAction((m) -> {
				setFont(Font.font(FONT, 20));
			});
			MenuItem f18 = new MenuItem("18");
			f18.setOnAction((m) -> {
				setFont(Font.font(FONT, 22));
			});
			MenuItem f20 = new MenuItem("20");
			f20.setOnAction((m) -> {
				setFont(Font.font(FONT, 24));
			});
			selectFont.getItems().addAll(f10, f12, f14, f16, f18, f20);

			collapse.setOnAction((m) -> {
				if (isCollapse == 0) {
					setText(formatStr(2));
					isCollapse = 1;
				} else {
					isCollapse = 0;
					setText(text);
				}
			});
		});
	}
	
	public double getXCoord() {
		return X;
	}

	public double getYCoord() {
		return Y;
	}
	
	private int getWidth() {
		return (int) getLayoutBounds().getWidth();
	}

	private int getHeight() {
		return (int) getLayoutBounds().getHeight();
	}

	public void setColor(Color color) {
		setFill(color);
		this.color = color;
	}

	public void reSetText() {
		setText(formatStr(objType));
	}
}
