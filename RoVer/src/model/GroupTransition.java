package model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import image.Conditions;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.util.Duration;
import study.BugTracker;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Circle;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;

public class GroupTransition extends Polyline {
	private Group source;
	private Group target;
	
	// arrowhead
	Polygon poly;
	
	// indicator
	Circle indicator;	
	Circle indicOutline;
	Tooltip t;
	
	// branching conditions
	Conditions conditions;
	
	// branching
	boolean[] humanBranching;
	boolean[] breakdownBranching;
	boolean el;
	
	// images
	private Image ready;
	private Image busy;
	private Image ignore;
	private Image noBreakdown;
	private Image breakdown;
	
	// temp
	private double X;
	private double Y;
	private boolean linked;
	// used for removing temp points (top left corner of the target)
	private double savedX;
	private double savedY;
	
	// breakpoint
	private boolean broke;
	
	// condition
	private Boolean isNonAssisted;
	
	// bug tracking
	BugTracker bt;
	
	// feedback
	private boolean allowNegativeFeedback;
	
	// for the bug tracker
	private ArrayList<ArrayList<Microinteraction>> badConnections;
	
	public GroupTransition(Group s, Point2D tempTarget, BugTracker bt, Boolean isNonAssisted) {
		source = s;
		target = null;
		poly = null;
		indicator = null;
		this.X = tempTarget.getX();
		this.Y = tempTarget.getY();
		linked = false;
		this.bt = bt;
		this.isNonAssisted = isNonAssisted;
		
		initialize();
		initializeIndicator();
	}
	
	public GroupTransition(Group s, Group t, BugTracker bt) {
		source = s;
		target = t;
		poly = null;
		linked = true;
		this.bt = bt;
		
		initialize();
		initializeIndicator();
	}
	
	public void initialize() {
		// initialize the branching
		humanBranching = new boolean[3];
		for (int i = 0; i < humanBranching.length; i++)
			humanBranching[i] = true;
		breakdownBranching = new boolean[2];
		for (int i = 0; i < breakdownBranching.length; i++)
			breakdownBranching[i] = true;
		el = false;
		source.addOutputMacroTrans(this);
		getPoints().addAll(source.getLayoutX() + source.getWidth()/2, source.getLayoutY() + source.getHeight()/2);
		
		if (linked) {
			target.addInputMacroTrans(this);
			getPoints().addAll(target.getLayoutX(), target.getLayoutY());
		}
		else {
			getPoints().addAll(X, Y);
		}
		setStrokeWidth(3);

		// bug tracking
		badConnections = new ArrayList<ArrayList<Microinteraction>>();
		
		allowNegativeFeedback = false;
		}
	
	public void initWithOrientation(){
		getPoints().removeAll(savedX, savedY);
		getPoints().removeAll(source.getLayoutX() + source.getWidth()/2, source.getLayoutY() + source.getHeight()/2);
		//getPoints().addAll();//add source four middle position

		String orient = calculateOrientation("input");
		double TminX = target.getLayoutX();
		double TmaxX = target.getLayoutBounds().getMaxX();
		double TminY = target.getLayoutY();
		double TmaxY = target.getLayoutBounds().getMaxY();
		double SminX = source.getLayoutX();
		double SmaxX = source.getLayoutBounds().getMaxX();
		double SminY = source.getLayoutY();
		double SmaxY = source.getLayoutBounds().getMaxY();
		if (orient == "top"){
			getPoints().addAll((TminX + TmaxX)/2, TminY);
			getPoints().addAll((SminX + SmaxX)/2, SmaxY);
		}
		else if (orient == "left"){
			getPoints().addAll(TminX, (TminY + TmaxY)/2);
			getPoints().addAll(SmaxX, (SminY + SmaxY)/2);
		}
		else if (orient == "right"){
			getPoints().addAll(TmaxX, (TminY + TmaxY)/2);
			getPoints().addAll(SminX, (SminY + SmaxY)/2);
			System.out.println(source.getLayoutX());
			System.out.println(SminX+" "+TminX);
		}
		else if (orient == "bottom"){
			getPoints().addAll((TminX + TmaxX)/2, TmaxY);
			getPoints().addAll((SminX + SmaxX)/2, SminY);
		}
	}
	
	public void addBugTracker(BugTracker bt) {
		this.bt = bt;
	}
	
	/*
	 * Getters
	 */
	public ArrayList<ArrayList<Microinteraction>> getBadConnections() {
		return badConnections;
	}
	
	public Group getSource() {
		return source;
	}
	
	public Group getTarget() {
		return target;
	}
	
	public Point2D getTempTarget() {
		return new Point2D(X, Y);
	}
	
	public boolean isLinked() {
		return linked;
	}
	
	public boolean[] getHumanBranching() {
		return humanBranching;
	}
	
	public boolean[] getBreakdownBranching() {
		return breakdownBranching;
	}
	
	public boolean[] getAllBranching() {
		boolean[] allBranching = new boolean[humanBranching.length + breakdownBranching.length];
		for (int i = 0; i < breakdownBranching.length; i++)
			allBranching[i] = breakdownBranching[i];
		for (int i = 0; i < humanBranching.length; i++)
			allBranching[i + breakdownBranching.length] = humanBranching[i];
		
		return allBranching;
	}
	
	public void setAllHumanBranching(boolean val) {
		humanBranching[0] = val;
		humanBranching[1] = val;
		humanBranching[2] = val;
	}
	
	public boolean setAllHumanBranching(boolean[] vals) {
		System.out.println(humanBranching[0] + " " + humanBranching[1] + " " + humanBranching[2]);
		System.out.println(vals[0] + " " + vals[1] + " " + vals[2]);
		boolean changed = false;
		if (vals[0] != humanBranching[0] || vals[1] != humanBranching[1] || vals[2] != humanBranching[2])
			changed = true;
		humanBranching[0] = vals[0];
		humanBranching[1] = vals[1];
		humanBranching[2] = vals[2];
		return changed;
	}
	
	public boolean getElse() {
		return el;
	}
	
	/*
	 * Setters
	 */
	
	public void unGray() {
		this.setStroke(Color.BLACK);
		this.poly.setFill(Color.BLACK);
		this.indicOutline.setFill(Color.BLACK);
	}
	
	public void grayOut() {
		this.setStroke(Color.LIGHTGRAY);
		this.poly.setFill(Color.LIGHTGRAY);
		this.indicOutline.setFill(Color.LIGHTGRAY);
	}
	
	public void setElse(boolean val) {
		el = val;
	}
	
	public void setLinked(boolean linked) {
		this.linked = linked;
	}
	
	public void setTempTarget(double x, double y) {
		X = x;
		Y = y;
		
		getPoints().set(getPoints().size() - 2, (X));
		getPoints().set(getPoints().size() - 1, (Y));
	}
	
	public void setTarget(Group group) {
		
		target = group;
		getPoints().set(getPoints().size() - 2, (target.getLayoutX()));
		getPoints().set(getPoints().size() - 1, (target.getLayoutY()));
		savedX = target.getLayoutX();
		savedY = target.getLayoutY();
		
		//initializeIndicator();
		if (bt != null)
			bt.addCommand("set mt target");
		
		// grayOut if necessary
		if (!source.checkBranchingPartition()[0]) {
			source.greyAllMacroTransitionsOut();
		}
		
		if (!source.checkBranchingPartition()[1]) {
			if (source.getWasGoodPartition()) {
				bt.addBug("branching", source);
				source.setGoodPartition(false);
			}
		}

	}
	
	public void updateBreakpoint(double x, double y) {
		if (getPoints().size() == 4) {
			double endX = getPoints().get(getPoints().size() - 2);
			double endY = getPoints().get(getPoints().size() - 1);
			
			getPoints().set(getPoints().size() - 2, x);
			getPoints().set(getPoints().size() - 1, y);
			getPoints().add(endX);
			getPoints().add(endY);
			broke = true;
		}
		else {
			getPoints().set(getPoints().size() - 4, x);
			getPoints().set(getPoints().size() - 3, y);
		}
	}
	
	public void removeBreakpoint() {
		
		if (getPoints().size() == 6) {
			double endX = getPoints().get(getPoints().size() - 2);
			double endY = getPoints().get(getPoints().size() - 1);
			
			getPoints().remove(getPoints().size() - 1);
			getPoints().remove(getPoints().size() - 1);
			getPoints().remove(getPoints().size() - 1);
			getPoints().remove(getPoints().size() - 1);
			getPoints().add(endX);
			getPoints().add(endY);
			broke = false;
		}
	}
	
	public String calculateOrientation(String io) {
		String result;
		
		// treat problem as though io is source
		double sX = source.getLayoutX() + source.getWidth()/2.0;
		double sY = source.getLayoutY() + source.getHeight()/2.0;
		double tX = target.getLayoutX() + target.getWidth()/2.0;
		double tY = target.getLayoutY() + target.getHeight()/2.0;
		
		// if the Y difference is less than the X difference
		if (Math.abs(sX - tX) >= Math.abs(sY - tY)) {
			// case source X is less than target X
			if (sX > tX)
				result = "left";
			// case target X is less than source X
			else
				result = "right";
		}
			
		// if the X difference is less than the Y difference
		else {
		// case source X is less than target X and the Y difference is less than the X difference
			if (sY > tY)
				result = "top";
		// case target X is less than source X and the Y difference is less than the X difference
			else
				result = "bottom";
		}
		
		// switch everything if io is target
		if (io.equals("input")) {
			if (result.equals("bottom")) result = "top";
			else if (result.equals("top")) result = "bottom";
			else if (result.equals("right")) result = "left";
			else result = "right";
		}
		
		return result;
	}
	
	public void updateArrow(double x1, double y1, double x2, double y2, double x3, double y3) {
		if (poly.getPoints().size() == 0) {
			poly.getPoints().addAll(x1, y1, x2, y2, x3, y3);
		}
		else {
			poly.getPoints().set(0, x1);
			poly.getPoints().set(1, y1);
			poly.getPoints().set(2, x2);
			poly.getPoints().set(3, y2);
			poly.getPoints().set(4, x3);
			poly.getPoints().set(5, y3);
		}
	}
	
	public void setPoly(Polygon poly) {
		this.poly = poly;
	}
	
	public Polygon getPoly() {
		return poly;
	}
	
	private void initializeIndicator() {
		indicator = new Circle();
		indicOutline = new Circle();
		indicator.setRadius(5);
		indicOutline.setRadius(7);
		indicator.setFill(Color.GRAY);
		indicOutline.setFill(Color.BLACK);
		
		// tooltip initialization
		t = new Tooltip("There are no connections\nbetween microinteractions.");
		hackTooltipStartTiming(t);
		
		if (isNonAssisted != null && !isNonAssisted)
			Tooltip.install(indicator,  t);
		
		conditions = new Conditions(this);
		
		String path = "Icons" + File.separator;
		try {
			ready = new Image(new FileInputStream(path + "Icon_HumReady.png"));
			busy = new Image(new FileInputStream(path + "Icon_HumBusy.png"));
			ignore = new Image(new FileInputStream(path + "Icon_HumIgnore.png"));
			noBreakdown = new Image(new FileInputStream(path + "Icon_Check.png"));
			breakdown = new Image(new FileInputStream(path + "Icon_Breakdown.png"));
		} catch (Exception e) {
			System.out.println("Error: indicator images did not load.");
		}
		
		updateIndicatorLocation();
	}
	
	public Conditions getConditions() {
		return conditions;
	}
	
	//public void displayElseCondition() {
	//	this.conditions.getGraphicsContext2D().clearRect(0, 0, 62, 10);
	//	this.conditions.getGraphicsContext2D().fillText("(else)", 0, 10);
	//}
	
	public void updateAndDisplayConditions() {
		Group group = getSource();
		boolean[] goodPartition = group.checkBranchingPartition();
		
		if (goodPartition[0]) {
			for (GroupTransition mtran : group.getOutputMacroTransitions()) {
				mtran.unGray();
			}
		}
		// else, change all macrotransitions to gray
		else {
			for (GroupTransition mtran : group.getOutputMacroTransitions()) {
				mtran.grayOut();
				if (mtran.getElse()) {
					boolean[] conditions = {true, true, false, false, false};
					//mtran.updateConditions(conditions, true);
					//mtran.displayElseCondition();
				}
			}
			
			group.disableOutputTransitions();
		}
		
		if (goodPartition[1]) {
			if (!group.getWasGoodPartition()) {
				bt.removeBug("branching", group);
				group.setGoodPartition(true);
			}
		}
		else {
			if (group.getWasGoodPartition()) {
				bt.addBug("branching", group);
				group.setGoodPartition(false);
			}
		}
		
		// update macrotransition condition indicator
		boolean noBreakCondition = breakdownBranching[0];
		boolean breakCondition = breakdownBranching[1];
		boolean readyCondition = humanBranching[0];
		boolean busyCondition = humanBranching[1];
		boolean ignoreCondition = humanBranching[2];
	}
	
	private boolean[] getElseConditions(Group group) {
		boolean noBreakCondition = true;
		boolean breakCondition = true;
		boolean readyCondition = true;
		boolean busyCondition = true;
		boolean ignoreCondition = true;
		for (GroupTransition mtrans : group.getOutputMacroTransitions()) {
			/*
			 * BREAKDOWN BRANCHING
			boolean[] tempBreakBranch = mtrans.getBreakdownBranching();
			if (tempBreakBranch[0])
				noBreakCondition = false;
			if (tempBreakBranch[1])
				breakCondition = false;
			*/
			
			boolean[] tempHumanBranch = mtrans.getHumanBranching();
			if (tempHumanBranch[0])
				readyCondition = false;
			if (tempHumanBranch[1])
				busyCondition = false;
			if (tempHumanBranch[2])
				ignoreCondition = false;
		}
		
		boolean[] toReturn = {noBreakCondition, breakCondition, readyCondition, busyCondition, ignoreCondition};
		return toReturn;
	}
	
	public void updateIndicatorLocation() {
		
		if (!broke) {
			double x1 = this.getPoints().get(0);
			double y1 = this.getPoints().get(1);
			double x2 = this.getPoints().get(2);
			double y2 = this.getPoints().get(3);
					
			indicator.setCenterX((x2 > x1)?(x1+(x2-x1)/2):(x2+(x1-x2)/2));
			indicator.setCenterY((y2 > y1)?(y1+(y2-y1)/2):(y2+(y1-y2)/2));
			indicOutline.setCenterX((x2 > x1)?(x1+(x2-x1)/2):(x2+(x1-x2)/2));
			indicOutline.setCenterY((y2 > y1)?(y1+(y2-y1)/2):(y2+(y1-y2)/2));
			
			conditions.setLayoutX((x2 > x1)?(x1-18+(x2-x1)/2):(x2-18+(x1-x2)/2));
			conditions.setLayoutY((y2 > y1)?(y1+10+(y2-y1)/2):(y2+10+(y1-y2)/2));
		}
	}
	
	public Point2D getMidpoint() {
		double x1 = this.getPoints().get(0);
		double y1 = this.getPoints().get(1);
		double x2 = this.getPoints().get(getPoints().size() - 2);
		double y2 = this.getPoints().get(getPoints().size() - 1);
		
		Point2D point = new Point2D((x2 > x1)?(x1+(x2-x1)/2):(x2+(x1-x2)/2), (y2 > y1)?(y1+(y2-y1)/2):(y2+(y1-y2)/2));
		return point;
	}
	
	public void updateIndicatorColor(Color color) {
		indicator.setFill(color);
	}
	
	public Circle getIndicator() {
		return indicator;
	}
	
	public Circle getIndicatorOutline() {
		return indicOutline;
	}
	
	public Tooltip getIndicatorTooltip() {
		return t;
	}
	
	public ArrayList<Circle> getIndicatorComponents() {
		ArrayList<Circle> indics = new ArrayList<Circle>();
		indics.add(indicator);
		indics.add(indicOutline);
		return indics;
	}
	
	/*
	 * copy
	 */
	public GroupTransition copy(Group source, Group target) {
		// initialize new and link everything up
		GroupTransition macroCopy = new GroupTransition(source, target, null);
		
		// branching
		boolean[] humanBranchCopy = macroCopy.getHumanBranching();
		humanBranchCopy[0] = humanBranching[0];
		humanBranchCopy[1] = humanBranching[1];
		humanBranchCopy[2] = humanBranching[2];
		
		macroCopy.setPoly(new Polygon());
		
		return macroCopy;
	}
	
	public String toString() {
		String str = "";
		str += source.getName();
		str += target.getName();
		return str;
	}
	
	public static void hackTooltipStartTiming(Tooltip tooltip) {
	    try {
	        Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
	        fieldBehavior.setAccessible(true);
	        Object objBehavior = fieldBehavior.get(tooltip);

	        Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
	        fieldTimer.setAccessible(true);
	        Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

	        objTimer.getKeyFrames().clear();
	        objTimer.getKeyFrames().add(new KeyFrame(new Duration(250)));
	        
	        fieldTimer = objBehavior.getClass().getDeclaredField("hideTimer");
	        fieldTimer.setAccessible(true);
	        objTimer = (Timeline) fieldTimer.get(objBehavior);

	        objTimer.getKeyFrames().clear();
	        objTimer.getKeyFrames().add(new KeyFrame(new Duration(20000)));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void activateNegativeFeedback() {
		allowNegativeFeedback = true;
	}
	
	public void deactivateNegativeFeedback() {
		allowNegativeFeedback = false;
	}
	
	public boolean getNegativeFeedbackAllowance() {
		return allowNegativeFeedback;
	}
	
	public void setSelected(boolean flag) {
		if(flag) {
			this.setStroke(Color.LIGHTGREEN);
			this.poly.setFill(Color.LIGHTGREEN);
			this.indicOutline.setFill(Color.LIGHTGREEN);
		}
		else {
			if (this.source.checkBranchingPartition()[0]) {
				this.setStroke(Color.BLACK);
				this.poly.setFill(Color.BLACK);
				this.indicOutline.setFill(Color.BLACK);
			}
			else {
				this.setStroke(Color.LIGHTGRAY);
				this.poly.setFill(Color.LIGHTGRAY);
				this.indicOutline.setFill(Color.LIGHTGRAY);
			}
			//setStyle("-fx-stroke: BLACK;");
			//indicOutline.setStyle("-fx-fill: BLACK; -fx-stroke: BLACK;");
			//poly.setFill(Color.BLACK);
		}
	}
}
