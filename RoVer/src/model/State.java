package model;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.ArrayList;

import controller.Annotation;
import enums.StateClass;

/*
 * Class that holds all the information about a specific state
 * 
 * What type of state it is, i.e. start state and so on
 * 
 * Variable to hold the color of the state
 * 
 * Should hold all all the logical statements of the state
 */
public class State extends Circle {
	private String name;
	private StateClass stateClass;
	private String gestureType;
	private String gazeType;
	private Annotation annotation;
	private double X, Y;
	private boolean isInit = false;
	private Color color;
	private boolean visible;
	public static int RADIUS;
	private String type; 
	private boolean isCommitted = false;
	private boolean isBreakdown;
	private int ID;
	private String agent;

	private ArrayList<Transition> outputTrans;
	private ArrayList<Transition> inputTrans;

	public State(String name, double X, double Y, Color color, boolean visible, boolean isInitialState,
			boolean isCommitted, String type, StateClass sc, int ID) {
		super(X, Y, RADIUS, color);
		this.name = name;
		this.stateClass = sc;
		this.isInit = isInitialState;
		this.X = X;
		this.Y = Y;
		this.visible = visible;
		this.color = color;
		this.isCommitted = isCommitted;
		this.type = type;
		this.ID = ID;
		this.agent = null;
		this.isBreakdown = false;
		this.gestureType = "NONE";
		this.gazeType = "NONE";

		outputTrans = new ArrayList<Transition>();
		inputTrans = new ArrayList<Transition>();
	}

	public void relocate(double X, double Y) {
		setLayoutX(X - getLayoutBounds().getMinX());
		setLayoutY(Y - getLayoutBounds().getMinY());

		if (annotation != null) {
			annotation.relocate(X - this.X, Y - this.Y, 1);
		}

		for (Transition transition : inputTrans) {
			transition.getPoints().set(transition.getPoints().size() - 2, (X + RADIUS));
			transition.getPoints().set(transition.getPoints().size() - 1, (Y + RADIUS));
		}

		for (Transition transition : outputTrans) {
			transition.getPoints().set(0, (X + RADIUS));
			transition.getPoints().set(1, (Y + RADIUS));
		}
		this.X = X;
		this.Y = Y;
		;
	}
	
	/*
	private String name;
	private StateClass stateClass;
	private Annotation annotation;
	private double X, Y;
	private boolean isInit = false;
	private Color color;
	private boolean visible;
	public static int RADIUS;
	private String type; 
	private boolean isCommitted = false;
	private int ID;

	private ArrayList<Transition> outputTrans;
	private ArrayList<Transition> inputTrans;
	*/

	public State copyWithoutTrans() {
		State newState = new State(name, X, Y, color, visible, isInit, isCommitted, type, stateClass, ID);
		newState.setAgent(this.agent);
		return newState;
	}

	/*
	 * Setters
	 */
	public void setGesture(String gest) {
		this.gestureType = gest;
	}
	
	public void setGaze(String gaze) {
		this.gazeType = gaze;
	}
	
	public void setIsBreakdown(boolean val) {
		isBreakdown = val;
	}
	
	public void setStateClass(StateClass sc) {
		this.stateClass = sc;
	}
	
	public void setAgent(String agent) {
		this.agent = agent;
	}
	
	public void setColor(Color color) {
		setFill(color);
		this.color = color;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVisbility(boolean visible) {
		this.visible = visible;
	}

	public void setToInit(boolean isInit) {
		this.isInit = isInit;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setAnnotation(Annotation ann) {
		annotation = ann;
	}
	
	public void setInputTrans(ArrayList<Transition> trans) {
		inputTrans = new ArrayList<Transition>(trans);
	}
	
	public void setOutputTrans(ArrayList<Transition> trans) {
		inputTrans = new ArrayList<Transition>(trans);
	}

	/*
	 * Getters
	 */
	public String getGesture() {
		return gestureType;
	}
	
	public String getGaze() {
		return gazeType;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isBreakdown() {
		return isBreakdown;
	}
	
	public StateClass getStateClass() {
		return stateClass;
	}
	
	public String getAgent() {
		return agent;
	}

	public Color getColor() {
		return color;
	}

	public double getX() {
		return X;
	}

	public double getY() {
		return Y;
	}

	public boolean getVisibility() {
		return visible;
	}

	public boolean checkIsInit() {
		return isInit;
	}

	public boolean checkIsCommited() {
		return isCommitted;
	}

	public int getNormRadiusSize() {
		return RADIUS;
	}

	public int getID() {
		return ID;
	}
	
	public ArrayList<Transition> getAllTrans() {
		outputTrans.addAll(inputTrans);
		return outputTrans;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	//Add Transitions
	
	public void addInputTrans(Transition trans) {
		inputTrans.add(trans);
	}

	public void addOutputTrans(Transition trans) {
		outputTrans.add(trans);
	}

	//Remove transitions
	
	public void removeInputTrans(Transition trans) {
		inputTrans.remove(trans);
	}

	public void removeOutputTrans(Transition trans) {
		outputTrans.remove(trans);
	}

	public String toString() {
		String str = "";

		// name
		str += name + "(" + stateClass + ")";

		// Check if it is an initial state
		if (isInit)
			str += " (init)\n";
		else
			str += "\n";

		return str;
	}

}
