package model;

import java.awt.Point;
import java.awt.geom.Point2D;
import javafx.scene.shape.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import checkers.Checker;
import checkers.ModBehPair;
import checkers.PrismThread;
import checkers.PropModsBeh;
import checkers.Property;
import controller.ConsoleCT;
import controller.MainController;
import enums.StateClass;
import image.EndIndicators;
import image.StartIndicators;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import study.BugTracker;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Tooltip;


/*
 * Holds a list of microinteractions. 
 */

public class Group extends VBox {

	@FXML
	private Text name;
	@FXML
	private GridPane microList;
	@FXML
	private ScrollPane sp;
	
	private int pos = 0;
	
	// branching/end state stuff
	private Canvas canvas;
	private GraphicsContext gc;
	
	private StartIndicators starters;
	private EndIndicators enders;
	
	private Image readyGray;
	private Image busyGray;
	private Image ignoreGray;
	private Image noBreakdownGray;
	private Image breakdownGray;
	
	private Image ready;
	private Image busy;
	private Image ignore;
	private Image noBreakdown;
	private Image breakdown;
	
	private Text start;
	private Text end;
	
	private HashMap<String,Boolean> bugsFixed;
	
	// interaction
	private Interaction ia;
	
	// list of start-state StateClasses, and whether the corresponding start states are enabled or not
	private HashMap<StateClass,Boolean> enabledStartStates;
	
	// microinteraction stuff
	private ArrayList<Microinteraction> microinteractions;
	private HashMap<Microinteraction,HashMap<Integer,ArrayList<State>>> micro2idx2states;
	private HashMap<Microinteraction,ArrayList<Integer>> endStateIdxs;
	
	// macrointeraction stuff
	private Microinteraction macrointeraction;
	
	// transitions
	private ArrayList<GroupTransition> outputTrans;
	private ArrayList<GroupTransition> inputTrans;
	
	// stuff to do with reading in the interaction from supreme
	private int id;
	
	// initial group?
	private boolean init;
	
	// properties
	HashMap<Integer,Boolean> graphPropertyValues;
	
	// required parameterization?
	private boolean requiredParam;
	
	// marking for a forced network propagation update
	private boolean markedForUpdate;
	
	// flag for whether the microcollection is violating or not
	private boolean violating;
	private boolean greetingProp;
	private boolean doubleGreetingProp;
	private boolean authAfterHandoffProp;
	private boolean handoffAfterAuthProp;
	private boolean busyCommentWaitProp;
	
	private boolean instActionAskAnswer;
	
	// booleans set by violations
	private boolean speaks;
	private boolean hspeaks;
	private boolean hspeaksFirst;
	private boolean speaksFirst;
	
	// tooltip for conveying what properties have been violated!
	private Tooltip violationInfo;
	
	@FXML
	private StackPane titlebox;
	@FXML
	private Rectangle titlerect;
	
	private ArrayList<ModBehPair> fixedBehaviorConflicts;
	private ArrayList<ModBehPair> unresolvedBehaviorConflicts;
	
	// bug tracking
	private BugTracker bt;
	private ArrayList<ArrayList<Microinteraction>> badPairs;
	
	// negative feedback allowance
	private boolean allowNegativeFeedback;
	private boolean allowBlinker;
	
	// was good partition
	private boolean wasGoodPartition;

	public Group(boolean isInit, BugTracker bt) {
		this.setStyle("-fx-background-radius: 5;" +
                      "-fx-border-radius: 5;");
		init = isInit;
		initialize();
		name.setText("untitled");
		id = -1;
		this.bt = bt;
		microinteractions = new ArrayList<Microinteraction>();
	}

	public Group(String name, boolean isInit) {
		init = isInit;
		initialize();
		this.name.setText(name);
		id = -1;
		this.bt = bt;
		microinteractions = new ArrayList<Microinteraction>();
	}

	public Group(String name, ArrayList<Microinteraction> m, boolean isInit) {
		init = isInit;
		initialize();
		this.name.setText(name);
		id = -1;
		this.bt = bt;
		microinteractions = new ArrayList<Microinteraction>(m);
	}

	private void initialize() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource("resources" + File.separator + "Microcollection.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		getStylesheets().add(this.getClass().getClassLoader()
				.getResource("resources" + File.separator + "ScrollBar.css").toExternalForm());
		
		//setMaxHeight(250);
		//setMaxWidth(200);
		wasGoodPartition = true;
		
		macrointeraction = new Microinteraction();
		
		enabledStartStates = new HashMap<StateClass, Boolean>();
		initEnabledStartStates();
		
		inputTrans = new ArrayList<GroupTransition>();
		outputTrans = new ArrayList<GroupTransition>();
		initializeIndicatorLights();
		
		if (init) {
			titlebox.setStyle("-fx-background-color: lightgreen;");
			greetingProp = false;
		}
		else
			greetingProp = true;
		
		badPairs = new ArrayList<ArrayList<Microinteraction>>();
		
		markedForUpdate = false;
		violating = false;
		doubleGreetingProp = true;
		authAfterHandoffProp = true;
		handoffAfterAuthProp = true;
		busyCommentWaitProp = true;
		instActionAskAnswer = true;
		
		bugsFixed = new HashMap<String,Boolean>(){{
		     put("speech", false);
		     put("greeting", false);
		     put("doublegreeting", false);
		     put("authafterhandoff", false);
		     put("handoffafterauth", false);
		     put("instBusy", false);
		     put("instSuspended", false);
		     put("behaviorConflict", false);
		}};;
		
		violationInfo = new Tooltip();
		violationInfo.setText("Group \'" + this.getName() + "\' does not violate any properties.");
		//Tooltip.install(this,violationInfo);
		hackTooltipStartTiming(violationInfo);
		
		fixedBehaviorConflicts = new ArrayList<ModBehPair>();
		unresolvedBehaviorConflicts = new ArrayList<ModBehPair>();
		
		allowNegativeFeedback = false;
		allowBlinker = false;
		speaks = false;
		hspeaks = false;
		speaksFirst = false;
		hspeaksFirst = false;
	}
	
	public void addInteraction(Interaction ia) {
		this.ia = ia;
		
		graphPropertyValues = new HashMap<Integer,Boolean>();
		resetGraphPropertyValues();
	}
	
	public HashMap<Integer,Boolean> getGraphPropertyValues() {
		return graphPropertyValues;
	}
	
	public void resetGraphPropertyValues() {
		ArrayList<Property> graphProperties = ia.getGraphProperties();
		for (Property prop : graphProperties) {
			if (prop.getTies().equals("group") || prop.getTies().equals("init")) {
				graphPropertyValues.put(prop.getID(), prop.getInitVal());
			}
		}
	}
	
	public void addBugTracker(BugTracker bt) {
		this.bt = bt;
	}
	
	public Tooltip getTooltip() {
		return violationInfo;
	}
	
	public GridPane getGp() {
		return microList;
	}
	
	public GridPane getMicroList() {
		return microList;
	}
	
	public ScrollPane getScrollPane() {
		return sp;
	}

	public StackPane getTitlebox() { return titlebox; }

	public Rectangle getTitlerect() { return titlerect; }
	
	public void setGraphProp(Property prop, boolean val) {
		boolean propVal = graphPropertyValues.get(prop.getID());
		String propBugID = prop.getBugtrackID();
		if (propVal != val && val == true) {
			bt.removeGraphBug(prop, this);
		}
		else if (propVal != val && val == false) {
			bt.addGraphBug(prop, this);
		}
		graphPropertyValues.put(prop.getID(), val);
	}
	
	public boolean getGraphProp(Property prop) {
		return graphPropertyValues.get(prop.getID());
	}
	
	public void setViolating(boolean val) {
		this.violating = val;
	}
	
	public boolean getViolating() {
		return violating;
	}

	public void setGreetingProp(boolean val) {
		if (this.greetingProp != val && val == true) {
			bt.removeBug("greet", this);
			setBugsFixed("greeting",true);
		}
		else if (this.greetingProp != val && val == false)
			bt.addBug("greet", this);
		this.greetingProp = val;
	}
	
	public boolean getGreetingProp() {
		return greetingProp;
	}
	
	public void setDoubleGreetingProp(boolean val) {
		if (this.doubleGreetingProp != val && val == true) {
			bt.removeBug("doublegreet", this);
			setBugsFixed("doublegreeting",true);
		}
		else if (this.doubleGreetingProp != val && val == false)
			bt.addBug("doublegreet", this);
		this.doubleGreetingProp = val;
	}
	
	public boolean getDoubleGreetingProp() {
		return doubleGreetingProp;
	}
	
	public void setAuthAfterHandoffProp(boolean val) {
		if (this.authAfterHandoffProp != val && val == true) {
			bt.removeBug("authAfterHandoff", this); 
			setBugsFixed("authafterhandoff",true);
		}
		else if (this.authAfterHandoffProp != val && val == false)
			bt.addBug("authAfterHandoff", this);
		this.authAfterHandoffProp = val;
	}
	
	public boolean getAuthAfterHandoffProp() {
		return authAfterHandoffProp;
	}
	
	public boolean getHandoffAfterAuthProp() {
		return handoffAfterAuthProp;
	}
	
	public boolean getBusyCommentWaitProp() {
		return busyCommentWaitProp;
	}
	
	public boolean getInstActionAskAnswerProp() {
		return instActionAskAnswer;
	}
	
	public boolean getIsViolatingSomething(boolean tutorial, String design) {
		if (tutorial || design.equals("Instruction-Action"))
			return violating || !greetingProp || !busyCommentWaitProp || !instActionAskAnswer || unresolvedBehaviorConflicts.size() > 0;
		else
			return violating || !greetingProp || !doubleGreetingProp || !authAfterHandoffProp || !handoffAfterAuthProp || !busyCommentWaitProp || !instActionAskAnswer
				|| unresolvedBehaviorConflicts.size() > 0;
	}
	
	public boolean getIsViolatingFlub() {
		boolean val = false;
		ArrayList<Property> graphProperties = ia.getGraphProperties();
		for (Property prop : graphProperties) {
			if (prop.getTies().equals("group") || prop.getTies().equals("init")) {
				if (graphPropertyValues.get(prop.getID()))
					val = true;
			}
		}
		return val;
	}
	
	public boolean getIsViolatingBehaviorConflict() {
		return unresolvedBehaviorConflicts.size() > 0;
	}
	
	public ArrayList<ArrayList<Microinteraction>> getBadPairs() {
		return badPairs;
	}
	
	public ArrayList<ModBehPair> getFixedBehaviorConflicts() {
		return fixedBehaviorConflicts;
	}
	
	public void setBugsFixed(String key, boolean val) {
		this.bugsFixed.put(key, val);
	}
	
	public HashMap<String,Boolean> getBugsFixed(String key) {
		return this.bugsFixed;
	}
	
	public void checkIfViolating(PropModsBeh pmb, boolean isNonProperty) {

		ModBehPair convertedPmb = new ModBehPair(pmb.mod2beh, isNonProperty);
		
		boolean existsInFixedModBehPairs = false;
		boolean existsInUnresolvedModBehPairs = false;
		for (ModBehPair mbp : fixedBehaviorConflicts) {
			System.out.println("LOOPING through the current fixed behavior conflicts");
			if (convertedPmb.equalsOther(mbp)) {
				System.out.println("EXISTS in fixed!");
				existsInFixedModBehPairs = true;
				break;
			}
		}
		for (ModBehPair mbp : unresolvedBehaviorConflicts) {
			if (convertedPmb.equalsOther(mbp)) {
				System.out.println("EXISTS in unresolved!");
				existsInUnresolvedModBehPairs = true;
				break;
			}
		}
		
		if (!existsInFixedModBehPairs && !existsInUnresolvedModBehPairs) {
			System.out.println("CHECK RESULTS: yes");
			unresolvedBehaviorConflicts.add(convertedPmb);
		}
		
		
	}
	
	public void wipeUnresolvedBehConflicts() {
		unresolvedBehaviorConflicts = new ArrayList<ModBehPair>();
	}
	
	public void setFixed(ModBehPair mbp) {
		unresolvedBehaviorConflicts.remove(mbp);
		fixedBehaviorConflicts.add(mbp);
		setBugsFixed("behaviorConflict",true);
	}
	
	public ArrayList<ModBehPair> getUnresolvedBehaviorConflicts() {
		return unresolvedBehaviorConflicts;
	}
	
	public ArrayList<ModBehPair> allBehaviorConflicts() {
		ArrayList<ModBehPair> allMBPs = new ArrayList<ModBehPair>();
		allMBPs.addAll(unresolvedBehaviorConflicts);
		allMBPs.addAll(fixedBehaviorConflicts);
		return allMBPs;
	}
	
	public ArrayList<ModBehPair> allRelevantBehaviorConflicts(ArrayList<Module> mods) {
		ArrayList<ModBehPair> allRelevantMBPs = allBehaviorConflicts();
		ArrayList<ModBehPair> allMBPs = allBehaviorConflicts();
		
		for (ModBehPair mbp : allMBPs) {
			
			boolean isRelevant = true;
			for (int i = 0; i < mbp.size(); i++) {
				Module mod = mbp.getMod(i);
				
				/*
				 * mini algorithm to see whether mods contains mod (can't simply use "contains," because the mods are not the same
				 */
				boolean contains = false;
				for (Module m : mods) {
					if (m.getName().equals(mod.getName())) {
						contains = true;
					}
				}
				if (!contains)
					isRelevant = false;
			}
			
			if (!isRelevant) {
				allRelevantMBPs.remove(mbp);
			}
		}
		
		return allRelevantMBPs;
	}
	
	public void initEnabledStartStates() {
		enabledStartStates.put(StateClass.READY, true);
		enabledStartStates.put(StateClass.BUSY, true);
		enabledStartStates.put(StateClass.IGNORE, true);
	}
	
	private void initializeIndicatorLights() {
		// do the start indicators
		starters = new StartIndicators(42, 25);
		enders = new EndIndicators(55,25);
		
		canvas = new Canvas(75, 23);
		gc = canvas.getGraphicsContext2D();
		
		String path = "Icons" + File.separator;
		try {
			readyGray = new Image(new FileInputStream(path + "Icon_HumReady_gray.png"));
			busyGray = new Image(new FileInputStream(path + "Icon_HumBusy_gray.png"));
			ignoreGray = new Image(new FileInputStream(path + "Icon_HumIgnore_gray.png"));
			noBreakdownGray = new Image(new FileInputStream(path + "Icon_Check_gray.png"));
			breakdownGray = new Image(new FileInputStream(path + "Icon_Breakdown_gray.png"));
			
			ready = new Image(new FileInputStream(path + "Icon_HumReady.png"));
			busy = new Image(new FileInputStream(path + "Icon_HumBusy.png"));
			ignore = new Image(new FileInputStream(path + "Icon_HumIgnore.png"));
			noBreakdown = new Image(new FileInputStream(path + "Icon_Check.png"));
			breakdown = new Image(new FileInputStream(path + "Icon_Breakdown.png"));
		} catch (Exception e) {
			System.out.println("Error: indicator images did not load.");
		}
		
		/*
		 * BREAKDOWN BRANCHING
		gc.drawImage(noBreakdownGray,10, 13, 10, 10);
		gc.drawImage(breakdownGray, 23, 13, 10, 10);
		*/
		gc.drawImage(readyGray, 39, 13, 10, 10);
		gc.drawImage(busyGray, 52, 13, 10, 10);
		gc.drawImage(ignoreGray, 65, 13, 10, 10);
		
		//gc.setFill(Color.GRAY);
		/*
		 * BREAKDOWN BRANCHING
		gc.setStroke(Color.LIGHTGRAY);
		gc.strokeLine(36, 13, 36, 23);
		*/
		
		start = new Text("START");
		//end = new Text("END");
		
		gc.setFill(Color.GRAY);
		gc.setFont(Font.font("Veranda", FontWeight.THIN, 9));
		gc.fillText("ACTUAL OUT", 18, 10);
		
		calculateIndicatorPositions();
	}
	
	public void calculateIndicatorPositions() {
		canvas.setLayoutX(this.getLayoutX()+ ((this.getWidth()==0)?(114):this.getWidth()) - 75);
		canvas.setLayoutY(this.getLayoutY() - 23);
		
		starters.setLayoutX(this.getLayoutX());
		starters.setLayoutY(this.getLayoutY()- 25);
		
		enders.setLayoutX(this.getLayoutX() + ((this.getWidth()==0)?(114):this.getWidth()) - 52);
		enders.setLayoutY(this.getLayoutY()-25);
	}
	
	public void updateStartIndicators(boolean[] startingLights) {
		starters.update(startingLights[0], startingLights[1], startingLights[2], startingLights[3], startingLights[4], startingLights[5]);
	}
	
	public void updateEndIndicators(boolean[] endingLights) {
		enders.update(endingLights[0], endingLights[1], endingLights[2]);
	}
	
	/*
	 * WARNING:
	 * The following three methods are depreciated
	 */
	public void setReady(boolean val) {
		if (val)
			gc.drawImage(ready, 39, 13, 10, 10);		
		else
			gc.drawImage(readyGray, 39, 13, 10, 10);	
	}
	
	public void setBusy(boolean val) {
		if (val)
			gc.drawImage(busy, 52, 13, 10, 10);		
		else
			gc.drawImage(busyGray, 52, 13, 10, 10);	
	}
	
	public void setIgnore(boolean val) {
		if (val)
			gc.drawImage(ignore, 65, 13, 10, 10);		
		else
			gc.drawImage(ignoreGray, 65, 13, 10, 10);	
	}
	
	public void setNoBreakdown(boolean val) {
		/*
		 * BREAKDOWN BRANCHING
		if (val)
			gc.drawImage(noBreakdown, 10, 13, 10, 10);		
		else
			gc.drawImage(noBreakdownGray, 10, 13, 10, 10);	
		*/
	}
	
	public void setBreakdown(boolean val) {
		/*
		 * BREAKDOWN BRANCHING
		if (val)
			gc.drawImage(breakdown, 23, 13, 10, 10);		
		else
			gc.drawImage(breakdownGray, 23, 13, 10, 10);	
		*/
	}
	
	public Canvas getIndicatorLights() {
		return canvas;
	}
	
	public Canvas getStarterIndicatorLights() {
		return starters;
	}
	
	public Canvas getEnderIndicatorLights() {
		return enders;
	}

	public void addMicro(Microinteraction m) {
		microinteractions.add(m);
		addMicroBox(m.getMicroBox());
	}
	
	public void addMicroBoxShell(MicroBox mb) {
		addMicroBox(mb);
	}
	
	//TODO URGENT: once microbox code is integrated into microinteractions, add this code into the addMicro method
	private void addMicroBox(MicroBox mb) {
		mb.makeSmaller();
		
		// add to the end of the grid
		microList.add(mb, 0, pos);
		pos++;
	}
	
	public void removeMicro(Microinteraction m) {
		// remove all, and then add all excluding the old one
		ArrayList<MicroBox> mbs = new ArrayList<MicroBox>();
		for (int i = 0; i < microList.getChildren().size(); i++) {
			if ( !((MicroBox) microList.getChildren().get(i)).getMicrointeraction().equals(m))
				mbs.add((MicroBox) microList.getChildren().get(i));
		}
		
		microList.getChildren().clear();
		pos = 0;
		
		for (MicroBox microbox : mbs) {
			microList.add(microbox, 0, pos);
			pos++;
		}
		
		MicroBox mb = m.getMicroBox();
		//microList.getChildren().remove(mb);
		microinteractions.remove(m);
		micro2idx2states.remove(m);
		endStateIdxs.remove(m);
		//pos--;
	}
	
	public void addInputMacroTrans(GroupTransition input) {
		inputTrans.add(input);
	}
	
	public void addOutputMacroTrans(GroupTransition output) {
		outputTrans.add(output);
		checkBranchingPartition();
	}
	
	public ArrayList<GroupTransition> getAllMacroTransitions() {
		ArrayList<GroupTransition> mts = new ArrayList<GroupTransition>(inputTrans);
		mts.addAll(outputTrans);
		return mts;
	}

	public ArrayList<GroupTransition> getInputMacroTransitions() {
		return inputTrans;
	}
	
	public ArrayList<GroupTransition> getOutputMacroTransitions() {
		return outputTrans;
	}
	
	public HashMap<StateClass,Boolean> getEnabledStartStates() {
		return enabledStartStates;
	}
	
	public String getName() {
		return name.getText();
	}
	
	public void setName(String name) {
		this.name.setText(name);
	}

	public Microinteraction getMicro(int idx) {
		return microinteractions.get(idx);
	}

	public int getID() {
		return id;
	}

	public ArrayList<Microinteraction> getMicrointeractions() {
		return microinteractions;
	}

	public void setID(int id) {
		this.id = id;
	}
	
	public void markForUpdate() {
		markedForUpdate = true;
	}
	
	public void unmark() {
		markedForUpdate = false;
	}
	
	public boolean getMarking() {
		return markedForUpdate;
	}
	
	public void createReducedMergedMacrointeraction(Checker c, ConsoleCT console, Interaction ia, MainController mc) {

		// create the microinteraction
		macrointeraction = new Microinteraction();
		
		// name the microinteraction
		macrointeraction.setName(this.name.getText());
		
		// iterate through each microinteraction in the collection
		
		micro2idx2states = new HashMap<Microinteraction,HashMap<Integer,ArrayList<State>>>();
		endStateIdxs = new HashMap<Microinteraction,ArrayList<Integer>>();

		System.out.println("Iterating through each microinteraction");
		ArrayList<Variable> globals = new ArrayList<Variable>();
		ArrayList<Module> modules = new ArrayList<Module>();
		for (Microinteraction micro : microinteractions) {
			
			if (micro.getNumEnabledHumanInits() == 0) {
				endStateIdxs.put(micro, new ArrayList<Integer>());
				continue;
			}

			System.out.println("Exporting micro to TM");
			PrismThread pt = new PrismThread(console, ia, mc, micro);
			Thread thread = pt.getThread();
			pt.start("exportToTM");
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("Getting scratch disk contents");
			Object scratch = c.getScratch();
			HashMap<Integer,ArrayList<State>> idx2states = (HashMap<Integer, ArrayList<State>>) ((ArrayList<Object>) scratch).get(0);
			ArrayList<Integer> endStates = new ArrayList<Integer>();
			micro2idx2states.put(micro, idx2states);
			endStateIdxs.put(micro, endStates);
			HashMap<Integer,ArrayList<Integer>> idx2idx = (HashMap<Integer,ArrayList<Integer>>) ((ArrayList<Object>) scratch).get(1);

			System.out.println("Creating new variables");
			// add the gaze and gesture variables
			Variable gazeAt = new Variable("bool", "GAZE_AT");
			gazeAt.setValue("false");
			Variable gazeInt = new Variable("bool", "GAZE_INTIMACY");
			gazeInt.setValue("false");
			Variable gazeCog = new Variable("bool", "GAZE_COGNITIVE");
			gazeCog.setValue("false");
			Variable gazeRef = new Variable("bool", "GAZE_REFERENTIAL");
			gazeRef.setValue("false");
			Variable gazeElse = new Variable("bool", "GAZE_ELSE");
			gazeElse.setValue("false");
			Variable gestNone = new Variable("bool", "GESTURE_NONE");
			gestNone.setValue("false");
			Variable gestMet = new Variable("bool", "GESTURE_METAPHORIC");
			gestMet.setValue("false");
			Variable gestDiect = new Variable("bool", "GESTURE_DIECTIC");
			gestDiect.setValue("false");
			
			/*
			 * UNCOMMENT IF YOU WANT GAZE/GESTURE VARIABLES ADDED!
			globals.add(gazeAt);
			globals.add(gazeInt);
			globals.add(gazeCog);
			globals.add(gazeRef);
			globals.add(gazeElse);
						
			globals.add(gestNone);
			globals.add(gestMet);
			globals.add(gestDiect);
			*/
						
			// get the global variables and fill the tokens
			for (Module mod : micro.getModules()) {
				for (State state : mod.getStates()) {
					StateClass stc = state.getStateClass();
					String token = stc.getToken() + state.getAgent();
					if (stc.isExclusive()) {
						Variable glob = new Variable("bool", token);
						glob.setValue("false");
						
						boolean exists = false;
						for (Variable v : globals) {
							if (v.getName().equals(glob.getName()))
								exists = true;
						}
						
						if (!exists)
							globals.add(glob);
						/*
						Variable glob = new Variable("bool", state.getName() + "_" + micro.getName());
						glob.setValue("false");
						String tok = state.getStateClass().getToken() + "_formula";
						
						if (!tokens.containsKey(tok)) 
							tokens.put(tok, new ArrayList<Variable>());
						tokens.get(tok).add(glob);
						globals.add(glob);
						*/
					}
					
					if (stc.equals(StateClass.START)) {
						String gaze = state.getGaze();
						switch (gaze) {
						case "GAZE_AT": 
							gazeAt.setValue("true");
							break;
						case "GAZE_INTIMACY": 
							gazeInt.setValue("true");
							break;
						case "GAZE_COGNITIVE": 
							gazeCog.setValue("true");
							break;
						case "GAZE_REFERENTIAL": 
							gazeRef.setValue("true");
							break;
						case "GAZE_ELSE": 
							gazeElse.setValue("true");
							break;
						default:
							break;
						}
						
						String gesture = state.getGesture();
						switch (gesture) {
						case "GESTURE_NONE": 
							gestNone.setValue("true");
							break;
						case "GESTURE_METAPHORIC": 
							gestMet.setValue("true");
							break;
						case "GESTURE_DIECTIC": 
							gestDiect.setValue("true");
							break;
						default:
							break;
						}
					}
				}
			}


			// use the transition matrix for this microinteraction to build a module
			Module mod = new Module(micro.getName(), micro);
			modules.add(mod);
			// create ArrayList of new states
			ArrayList<State> states = new ArrayList<State>();
			for (int i = 0; i < idx2states.size(); i++) {
				State st = new State(i+"", 0, 0, Color.BLUE, true, false, false, "", null, i);
				states.add(st);
				mod.addState(st);
				
				// determine if idx2states.get(i) is an end-state
				boolean end = false;
				for (State state : idx2states.get(i)) {
					if (state.getStateClass().equals(StateClass.END))
						end = true;
				}
				if (end) {
					endStates.add(i);
				}
			}
			
			// go through each transition, linking up states
			Iterator it = idx2idx.entrySet().iterator();
			while (it.hasNext()) {
			    HashMap.Entry pair = (HashMap.Entry)it.next();
			    int s = (int) pair.getKey();
			    ArrayList<Integer> targs = (ArrayList<Integer>) pair.getValue();
			    
			    for (Integer t : targs) {
				    // add the link
				    State source = states.get(s);
				    State target = states.get(t);
				    Transition trans = new Transition(source,target, new ArrayList<Point>());
				    source.addOutputTrans(trans);
				    target.addInputTrans(trans);
				    mod.addTransition(trans);
				    
				    ArrayList<State> sStates = idx2states.get(s);
				    ArrayList<State> tStates = idx2states.get(t);
				    // go through all of the global variables
				    for (Variable glob : globals) {
				    	// determine if this global is a state in the source and/or in the trans
				    	boolean inSource = false;
				    	boolean inTarget = false;
				    	for (State ss : sStates) {
				    		if (glob.getName().equals(ss.getStateClass().getToken() + ss.getAgent())) {
				    			inSource = true;
				    			break;
				    		}
				    		else if (glob.getName().equals(ss.getGesture())) {
				    			inSource = true;
				    			break;
				    		}
				    		else if (glob.getName().equals(ss.getGaze())) {
				    			inSource = true;
				    			break;
				    		}
				    	}
				    	State targetState = null;
				    	for (State ts : tStates) {
				    		if (glob.getName().equals(ts.getStateClass().getToken() + ts.getAgent())) {
				    			inTarget = true;
				    			targetState = ts;
				    			break;
				    		}
				    		else if (glob.getName().equals(ts.getGesture())) {
				    			inTarget = true;
				    			targetState = ts;
				    			break;
				    		}
				    		else if (glob.getName().equals(ts.getGaze())) {
				    			inTarget = true;
				    			targetState = ts;
				    			break;
				    		}
				    	}
				    	
				    	// add the updates to the transition			    
				    	// for those states that exist in the source, but not in the trans, turn them off
				    	if (inSource && !inTarget) {
				    		Update u = new Update(glob, "false");
				    		trans.addUpdate(u);
				    	}
				    
				    	// for those states that exist in the trans, but not in the source, turn them on
				    	if (inTarget && !inSource) {
				    		Update u = new Update(glob, "true");
				    		trans.addUpdate(u);
				    		
				    		// add the guards to the transition -- only if the token is available!
				    		//if (!(targetState.getStateClass().getToken().equals("None"))) {
				    	//	if (!glob.getName().contains("GAZE") && !glob.getName().contains("GESTURE")) {
				    		Guard g = new Guard(glob.getName(), "false", "=");
					    	trans.addGuard(g);
				    	//	}
				    		//}
				    	}    	
				    }
			    }
			}
				
			// set the initial states
			for (int i = 0; i < states.size(); i++) {
				ArrayList<State> sts = idx2states.get(i);
				
				boolean isInit = true;
				for (State st : sts) {
					if (st.checkIsInit() == false) {
						isInit = false;
						break;
					}
				}
				
				if (isInit) {
					states.get(i).setToInit(true);
					mod.addInit(states.get(i));
					
					// set the proper global variables to true
					for (State st : sts) {
						// find the global variable associated with the state
						for (Variable v : globals) {
							if (v.getName().equals(st.getStateClass() + st.getAgent()))
								v.setValue("true");
						}
					}
				}
			}
		}
		System.out.println("Done with each microinteraction");
		
		// give one more module
		
		macrointeraction.addModules(modules);
		macrointeraction.addGlobals(globals);
		//macrointeraction.addDisjunctionFormula(tokens);
		
		macrointeraction.build();
		
		PrismThread pt = new PrismThread(console, ia, mc, macrointeraction);
		Thread thread = pt.getThread();
		pt.start("generatePrismFile");
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void greyAllMacroTransitionsOut() {
		for (GroupTransition mtran : outputTrans)
			mtran.grayOut();
	}
	
	public boolean[] checkBranchingPartition() {
		// new check for the partitions
		// the cases for a good partition are below
		// 0) there are no branches, in which case we must return true
		// 1) there is only one transition, with all checkboxes checked
		// 2) the sums of the sets of ArrayList<State> equal the full set, AND each subset is disjoint
		
		// case 0
		if (getOutputMacroTransitions().size() == 0) {
			boolean[] toReturn = {true, true};
			return toReturn;
		}
		
		// case 1
		int[] humanCounts = {0,0,0};
		int[] breakdownCounts = {0,0};
		for (GroupTransition mtran : outputTrans) {
			boolean[] hb = mtran.getHumanBranching();
			boolean[] bb = mtran.getBreakdownBranching();
			
			for (int i = 0; i < hb.length; i++) {
				if (hb[i]) 
					humanCounts[i]++;
			}
			for (int i = 0; i < bb.length; i++) {
				if (bb[i])
					breakdownCounts[i]++;
			}
		}
		
		boolean allOnes = true;
		for (int i = 0; i < humanCounts.length; i++) {
			if (humanCounts[i] != 1)
				allOnes = false;
		}
		
		for (int i = 0; i < breakdownCounts.length; i++) {
			if (breakdownCounts[i] != 1)
				allOnes = false;
		}
		
		// case 2 -- disjoint (pretty hard-cody)
		boolean overlap = false;
		ArrayList<ArrayList<Boolean>> allCombos = new ArrayList<ArrayList<Boolean>>();
		ArrayList<Boolean> bools = new ArrayList<Boolean>();
		bools.add(true);
		bools.add(false);
		
		for (Boolean successBool : bools) {
			for (Boolean breakdownBool : bools) {
				
				/*
				 * BREAKDOWN BRANCHING
				 * if (successBool ^ breakdownBool) {
				 */
				if (successBool && breakdownBool) {
				
					for (Boolean readyBool : bools) {
						for (Boolean busyBool : bools) {
							for (Boolean ignoreBool : bools) {
								if ((readyBool ^ busyBool ^ ignoreBool) && !(readyBool && busyBool && ignoreBool)) {   // if at least one of the human ends is true
									ArrayList<Boolean> combo = new ArrayList<Boolean>();
									combo.add(successBool);
									combo.add(breakdownBool);
									combo.add(readyBool);
									combo.add(busyBool);
									combo.add(ignoreBool);
									
									allCombos.add(combo);
								}
							}
						}
					}
					
				}
			}
		}
		
		/*
		 * print the combos
		 */
		for (int i = 0; i < allCombos.size(); i++) {
			ArrayList<Boolean> combo = allCombos.get(i);
		}
		
		ArrayList<ArrayList<Boolean>> partitioned = new ArrayList<ArrayList<Boolean>>();
		for (GroupTransition mt : outputTrans) {
			boolean[] hb = mt.getHumanBranching();
			boolean[] bb = mt.getBreakdownBranching();
			
			// if this is not an else
			if (!mt.getElse()) {
				boolean boolSuccess = bb[0];
				boolean boolBreak = bb[1];
				boolean boolReady = hb[0];
				boolean boolBusy = hb[1];
				boolean boolIgnore = hb[2];
				

				for (int i = 0; i < allCombos.size(); i++) {
					ArrayList<Boolean> combos = allCombos.get(i);
					if ((boolReady && combos.get(2)) ||
						(boolBusy && combos.get(3)) || 
						(boolIgnore && combos.get(4)))  {
						if (partitioned.contains(combos))
							overlap = true;
						
						partitioned.add(combos);
					}
				}
			}
		}
		/*
		
		
		// case 2 -- disjoint
		ArrayList<ArrayList<State>> partitioned = new ArrayList<ArrayList<State>>();
		boolean overlap = false;
		SequentialChecker sc = new SequentialChecker(null, null, null, null, null, false);
		for (GroupTransition mt : outputTrans) {
			// if this is not an else
			if (!mt.getElse()) {
				for (Microinteraction source : getMicrointeractions()) {
					ArrayList<ArrayList<State>> sts = sc.partitionEndStates(mt, source);
					
					for (ArrayList<State> st : sts) {
						if (partitioned.contains(st))
							overlap = true;
						partitioned.add(st);
					}
				}
			}
		}*/
		
		// full set
		boolean containsAll = true;
		int elCount = 0;
		
		for (GroupTransition mtran : outputTrans) {
			if (mtran.getElse())
				elCount++;
		}
		
		if (elCount > 1) {  // case that doesn't make sense
			containsAll = false;
		}
		
		else if (elCount == 0) {		
		// if else is not checked on any of the transitions {
			//ArrayList<ArrayList<Boolean>> allCombosCopy = (ArrayList<ArrayList<Boolean>>) allCombos.clone();
			//for (ArrayList<Boolean> existingBools : partitioned) {
			//	for(ArrayList<Boolean> allBools : allCombosCopy) {
			//		if (existingBools.get(0) == allBools.get(0) && existingBools.get(1) == allBools.get(1) && existingBools.get(2) == allBools.get(2) && existingBools.get(3) == allBools.get(3) && existingBools.get(4) == allBools.get(4)){
			//			allCombos.remove(allBools);
			//		}
			//	}
			//}
			
			//if (!allCombos.isEmpty())
			//	containsAll = false;
			
			// get the outgoing conditions:
			boolean[] ends = getHumanEndStates();
			
			boolean[] contained = {false, false, false};
			for (int i = 0; i < contained.length; i++) {
				if (!ends[i]) {
					contained[i] = true;
				}
			}
			
			for (GroupTransition mt : getOutputMacroTransitions()) {
				boolean[] hb = mt.getHumanBranching();
				for (int i = 0; i < contained.length; i++) {
					if (hb[i])
						contained[i] = true;
				}
			}
			
			containsAll = true;
			for (boolean b : contained) {
				if (!b)
					containsAll = false;
			}
		}
		
		if (ia.getIsNonAssisted()) {
			boolean[] toReturn = {(allOnes & outputTrans.size() == 1) | (!overlap), (allOnes & outputTrans.size() == 1) | (containsAll & !overlap)};
			return toReturn;
		}
		else {
			boolean isGood = (allOnes & outputTrans.size() == 1) | (containsAll & !overlap);
			boolean[] toReturn = {isGood, isGood};
			return toReturn;
		}
	}
	
	public void disableOutputTransitions() {
		
	}
	
	public Microinteraction getMacrointeraction() {
		return macrointeraction;
	}

	// refresh the location of the object and macrotransitions
	public void refresh() {
		double x = getLayoutX();
		double y = getLayoutY();
		relocate(x,y);
	}
	
	//Relocate the object. This code has been take from the node class in the javafx package
	public void relocate(double X, double Y) {
		setLayoutX(X - getLayoutBounds().getMinX());
		setLayoutY(Y - getLayoutBounds().getMinY());
		
		calculateIndicatorPositions();
				
		HashMap<String,ArrayList<GroupTransition>> orientations = new HashMap<String,ArrayList<GroupTransition>>();
		String[] orientStrs = {"right", "bottom", "left"};
		for (String orient : orientStrs) {
			orientations.put(orient,new ArrayList<GroupTransition>());
		}
		
		HashMap<Group,HashMap<String,ArrayList<GroupTransition>>> sourceOrientations =  new HashMap<Group,HashMap<String,ArrayList<GroupTransition>>>();
		HashMap<Group,HashMap<String,ArrayList<GroupTransition>>> targetOrientations =  new HashMap<Group,HashMap<String,ArrayList<GroupTransition>>>();
		for (GroupTransition mt : inputTrans) {
			sourceOrientations.put(mt.getSource(), new HashMap<String,ArrayList<GroupTransition>>());
			for (String orient : orientStrs) {
				sourceOrientations.get(mt.getSource()).put(orient,new ArrayList<GroupTransition>());
			}
		}
		for (GroupTransition mt : outputTrans) {
			targetOrientations.put(mt.getTarget(), new HashMap<String,ArrayList<GroupTransition>>());
			for (String orient : orientStrs) {
				targetOrientations.get(mt.getTarget()).put(orient,new ArrayList<GroupTransition>());
			}
		}
		
		for (GroupTransition transition : inputTrans) {
			// calculate angle between this group and the linking group
			String orient = transition.calculateOrientation("input");
			if (!orient.equals("top"))
				orientations.get(orient).add(transition);
			
			// for this group, determine the coordinates based on the orientation
			Point2D.Double xy = getOrientCoords(orient);
			
			// update the arrow
			if (orient.equals("left") || orient.equals("right")) {
				if (orient.equals("left")) 
					transition.getPoints().set(transition.getPoints().size() - 2, (xy.x - 10.0));
				else if (orient.equals("right")) 
					transition.getPoints().set(transition.getPoints().size() - 2, (xy.x + 10.0));
				transition.getPoints().set(transition.getPoints().size() - 1, (xy.y));
			}
			else if (orient.equals("top") || orient.equals("bottom")) {
				transition.getPoints().set(transition.getPoints().size() - 2, (xy.x));
				if (orient.equals("top")) 
					transition.getPoints().set(transition.getPoints().size() - 1, (xy.y - 10.0));
				else 
					transition.getPoints().set(transition.getPoints().size() - 1, (xy.y + 10.0));
			}
			updateArrow(transition, xy, orient);
			transition.updateIndicatorLocation();
			
			// for the other group, determine the coordinates based on the orientation
			String otherOrient = transition.calculateOrientation("output");
			//if (!otherOrient.equals("top"))
			//	sourceOrientations.get(transition.getSource()).get(otherOrient).add(transition);
			
			// for THAT group, determine the coordinates based on the orientation
			Point2D.Double xy2 = transition.getSource().getOrientCoords(otherOrient);
			
			transition.getPoints().set(0, (xy2.x));
			transition.getPoints().set(1, (xy2.y));
		}

		for (GroupTransition transition : outputTrans) {
			// calculate angle between this group and the linking group
			String orient = transition.calculateOrientation("output");
			if (!orient.equals("top"))
				orientations.get(orient).add(transition);
			
			// for this group, determine the coordinates based on the orientation
			Point2D.Double xy = getOrientCoords(orient);
			
			transition.getPoints().set(0, (xy.x));
			transition.getPoints().set(1, (xy.y));
			
			// for the other group, determine the coordinates based on the orientation
			String otherOrient = transition.calculateOrientation("input");
						
			// for THAT group, determine the coordinates based on the orientation
			Point2D.Double xy2 = transition.getTarget().getOrientCoords(otherOrient);
				
			// update the arrow
			if (otherOrient.equals("left") || otherOrient.equals("right")) {
				if (otherOrient.equals("left")) 
					transition.getPoints().set(transition.getPoints().size() - 2, (xy2.x - 10.0));
				else if (otherOrient.equals("right")) 
					transition.getPoints().set(transition.getPoints().size() - 2, (xy2.x + 10.0));
				transition.getPoints().set(transition.getPoints().size() - 1, (xy2.y));
			}
			else if (otherOrient.equals("top") || otherOrient.equals("bottom")) {
				transition.getPoints().set(transition.getPoints().size() - 2, (xy2.x));
				if (otherOrient.equals("top")) 
					transition.getPoints().set(transition.getPoints().size() - 1, (xy2.y - 10.0));
				else 
					transition.getPoints().set(transition.getPoints().size() - 1, (xy2.y + 10.0));
			}
			updateArrow(transition, xy2, otherOrient);
			transition.updateIndicatorLocation();

		}
		
		cleanTransPositions(orientStrs, orientations, this);
		HashMap<String,ArrayList<GroupTransition>> otherOrientations = new HashMap<String,ArrayList<GroupTransition>>();
		//for (String orient : orientStrs) {
		//	otherOrientations.put(orient,new ArrayList<GroupTransition>());
		//}
		//for (GroupTransition macro : getInputMacroTransitions()) {
		//	String orient = macro.calculateOrientation("input");
		//	if (!orient.equals("top"))
		//		otherOrientations.get(orient).add(macro);
		//}
		//for (GroupTransition macro : getOutputMacroTransitions()) {
		//	String orient = macro.calculateOrientation("output");
		//	if (!orient.equals("top"))
		//		otherOrientations.get(orient).add(macro);
		//}
		
		//cleanTransPositions(orientStrs, otherOrientations, this);
		
		for (GroupTransition mt : inputTrans) {
			Group group = mt.getSource();
		//	group.relocate(group.getLayoutX(), group.getLayoutY());
			otherOrientations = new HashMap<String,ArrayList<GroupTransition>>();
			for (String orient : orientStrs) {
				otherOrientations.put(orient,new ArrayList<GroupTransition>());
			}
			for (GroupTransition macro : group.getInputMacroTransitions()) {
				String orient = macro.calculateOrientation("input");
				if (!orient.equals("top"))
					otherOrientations.get(orient).add(macro);
			}
			for (GroupTransition macro : group.getOutputMacroTransitions()) {
				String orient = macro.calculateOrientation("output");
				if (!orient.equals("top"))
					otherOrientations.get(orient).add(macro);
			}
			
			cleanTransPositions(orientStrs, otherOrientations, group);
		}
		for (GroupTransition mt : outputTrans) {
			Group group = mt.getTarget();
		//	group.relocate(group.getLayoutX(), group.getLayoutY());
			otherOrientations = new HashMap<String,ArrayList<GroupTransition>>();
			for (String orient : orientStrs) {
				otherOrientations.put(orient,new ArrayList<GroupTransition>());
			}
			for (GroupTransition macro : group.getInputMacroTransitions()) {
				String orient = macro.calculateOrientation("input");
				if (!orient.equals("top"))
					otherOrientations.get(orient).add(macro);
			}
			for (GroupTransition macro : group.getOutputMacroTransitions()) {
				String orient = macro.calculateOrientation("output");
				if (!orient.equals("top"))
					otherOrientations.get(orient).add(macro);
			}
			
			cleanTransPositions(orientStrs, otherOrientations, group);
		}
		
		
	}
		
	public void cleanTransPositions(String[] orientStrs, HashMap<String,ArrayList<GroupTransition>> orientations, Group group) {
		// shuffle transitions around so that they aren't overlapping
		int numPositions;
		for (String orient : orientStrs) {
			numPositions = Math.max(5,orientations.get(orient).size());
			double center;
			double increment;
			if (orient.equals("bottom")) {
				center = getWidth()/2.0;
				increment = (getWidth() - 10)/(numPositions*1.0);
			}
			else {
				center = getHeight()/2.0;
				increment = (getHeight() - 10)/(numPositions*1.0);
			}
			
			// get the macrotransitions from the current orientation
			ArrayList<GroupTransition> currMacros = orientations.get(orient);
			
			// temp class for ordering macros based on position
			class MacroTransPosition implements Comparable{
				GroupTransition macro;
				Double position;
				MacroTransPosition(GroupTransition macro, Double position) {
					this.macro = macro;
					this.position = position;
				}
				
				@Override
				public int compareTo(Object other) {
					if (this.position < ((MacroTransPosition) other).position)
						return -1;
					else if (this.position > ((MacroTransPosition) other).position)
						return 1;
					else {
						return this.macro.getSource().getName().compareTo(((MacroTransPosition) other).macro.getSource().getName());
					} 
				}
			}
				
			// make a new arraylist of macrotranspositions
			ArrayList<MacroTransPosition> mtps = new ArrayList<MacroTransPosition>();
			for (GroupTransition mt : currMacros) {
				double position;
				
				if (orient.equals("bottom")) {
					if (group.getInputMacroTransitions().contains(mt))
						position = mt.getSource().getLayoutX() + mt.getSource().getWidth()/2.0;
					else
						position = mt.getTarget().getLayoutX() + mt.getTarget().getWidth()/2.0;
				}
				else {
					if (group.getInputMacroTransitions().contains(mt))
						position = mt.getSource().getLayoutY() + mt.getSource().getHeight()/2.0;
					else
						position = mt.getTarget().getLayoutY() + mt.getTarget().getHeight()/2.0;
				}
				
				mtps.add(new MacroTransPosition(mt, position));
			}
			
			// sort the macrotransitions based on their position
			Collections.sort(mtps);
			
			// compute number of bins
			int bin = Math.min(mtps.size(), numPositions);
			
			// re-do the ending locations of the transitions
			//int alternator = 1;
			//int counter = 1;
			//int currPosition = 0;
			int currPosition = -(mtps.size()/2);
			for (MacroTransPosition mtp : mtps) {
				double location;
				if (group.getInputMacroTransitions().contains(mtp.macro)) {
					int xidx = mtp.macro.getPoints().size() - 2;
					int yidx = mtp.macro.getPoints().size() - 1;
					if (orient.equals("bottom")) {
						location = mtp.macro.getTarget().getLayoutX() + mtp.macro.getTarget().getWidth()/2.0 + currPosition * increment;
						mtp.macro.getPoints().set(xidx, location);
						updateArrow(mtp.macro, new Point2D.Double(mtp.macro.getPoints().get(xidx), mtp.macro.getPoints().get(yidx)-10.0), orient);
					}
					else {
						location = mtp.macro.getTarget().getLayoutY() + mtp.macro.getTarget().getHeight()/2.0 + currPosition * increment;
						mtp.macro.getPoints().set(yidx, location);
						if (orient.equals("right"))
							updateArrow(mtp.macro, new Point2D.Double(mtp.macro.getPoints().get(xidx)-10.0, mtp.macro.getPoints().get(yidx)), orient);
						else
							updateArrow(mtp.macro, new Point2D.Double(mtp.macro.getPoints().get(xidx)+10.0, mtp.macro.getPoints().get(yidx)), orient);
					}
				}
				else {
					int xidx = 0;
					int yidx = 1;
					if (orient.equals("bottom")) {
						location = mtp.macro.getSource().getLayoutX() + mtp.macro.getSource().getWidth()/2.0 + currPosition * increment;
						mtp.macro.getPoints().set(xidx, location);
					}
					else {
						location = mtp.macro.getSource().getLayoutY() + mtp.macro.getSource().getHeight()/2.0 + currPosition * increment;
						mtp.macro.getPoints().set(yidx, location);
					}
				}
				
				mtp.macro.updateIndicatorLocation();
				//currPosition = alternator * (Math.abs(currPosition) + counter);
				//alternator = alternator * -1;
				//counter = (counter+1)%2;
				currPosition += 1;
			}
		}
	}
	
	public void updateArrow(GroupTransition transition, Point2D.Double xy, String orient) {
		if (orient.equals("left")) {
			transition.updateArrow(xy.x, xy.y,
		            xy.x - 10.0, xy.y - 5.0,
		            xy.x - 10.0, xy.y + 5.0);
		}
		else if (orient.equals("right")) {
			transition.updateArrow(xy.x, xy.y,
		            xy.x + 10.0, xy.y + 5.0,
		            xy.x + 10.0, xy.y - 5.0);
		}
		else if (orient.equals("top")) {
			transition.updateArrow(xy.x, xy.y,
		            xy.x + 5.0, xy.y - 10.0,
		            xy.x - 5.0, xy.y - 10.0);
		}
		else {
			transition.updateArrow(xy.x, xy.y,
		            xy.x - 5.0, xy.y + 10.0,
		            xy.x + 5.0, xy.y + 10.0);
		}
	}
	
	//Used to highlight the box when a new microinteraction is being added
	public void setSelected(boolean flag) {
		if(flag) {
			setStyle("-fx-border-color: LIGHTGREEN; -fx-border-width: 3;" +
					"-fx-background-radius: 5;" +
                    "-fx-border-radius: 5;");
		}
		else {
			setStyle("-fx-border-color: BLACK; -fx-border-width: 3;" +
					"-fx-background-radius: 5;" +
                    "-fx-border-radius: 5;");
		}
	}
	
	public boolean isInit() {
		return init;
	}
	
	public void setToInit() {
		init = true;
		titlebox.setStyle("-fx-background-color: lightgreen;");
		//titlerect.setFill(Color.LIGHTGREEN);
	}
	
	public void setNotInit() {
		init = false;
		titlebox.setStyle("-fx-background-color: white;");
		titlerect.setFill(Color.WHITE);
		setGreetingProp(true);
	}
	
	public Point2D.Double getOrientCoords(String orient) {
		Point2D.Double p = new Point2D.Double();
		
		if (orient.equals("top")) {
			p.x = getLayoutX() + getWidth()/2.0;
			p.y = getLayoutY();
		}
		else if (orient.equals("bottom")) {
			p.x = getLayoutX() + getWidth()/2.0;
			p.y = getLayoutY() + getHeight();
		}
		else if (orient.equals("left")) {
			p.x = getLayoutX();
			p.y = getLayoutY() + getHeight()/2.0;
		}
		else {
			p.x = getLayoutX() + getWidth();
			p.y = getLayoutY() + getHeight()/2.0;
		}
			
		
		return p;
	}
	
	public HashMap<Microinteraction,ArrayList<Integer>> getEndStateIdxs() {
		return endStateIdxs;
	}
	
	public HashMap<Microinteraction, HashMap<Integer, ArrayList<State>>> getMicro2Idx2State() {
		return micro2idx2states;
	}
	
	public void wipeEndStates() {
		endStateIdxs = null;
		micro2idx2states = null;
	}
	
	public boolean[] getHumanEndStates() {
		boolean[] ends = {false, false, false};
		boolean isNull = false;
		for (Microinteraction micro : microinteractions) {
			ArrayList<ArrayList<State>> endStates = micro.getEndStates();
			
			if (endStates == null) {
				//isNull = true;
				break;
			}
			
			for (ArrayList<State> sts : endStates) {
				for (State st : sts) {
					if (st.getStateClass().equals(StateClass.READY))
						ends[0] = true;
					if (st.getStateClass().equals(StateClass.BUSY))
						ends[1] = true;
					if (st.getStateClass().equals(StateClass.IGNORE))
						ends[2] = true;
				}
			}
		}
		
		//if (isNull)
		//	return null;
		return ends;
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
	
	public void activateBlinker() {
		allowBlinker = true;
	}
	
	public void deactivateBlinker() {
		allowBlinker = false;
	}
	
	public boolean getBlinker() {
		return allowBlinker;
	}
	
	public void setSpeaks(boolean val) {
		this.speaks = val;
	}
	
	public void setHSpeaks(boolean val) {
		this.hspeaks = val;
	}
	
	public boolean getSpeaks() {
		return speaks;
	}
	
	public boolean getHSpeaks() {
		return hspeaks;
	}
	
	public void setHSpeaksFirst(boolean val) {
		this.hspeaksFirst = val;
	}
	
	public void setSpeaksFirst(boolean val) {
		this.speaksFirst = val;
	}
	
	public boolean getHSpeaksFirst() {
		return this.hspeaksFirst;
	}
	
	public boolean getSpeaksFirst() {
		return this.speaksFirst;
	}
	
	public String macrointeractionEndStateIdxToString() {
		String str = "";
		
		for (Microinteraction micro : microinteractions) {
			str += micro.getName();
			for (Integer i : endStateIdxs.get(micro)) {
				str += " " + i + " ";
			}
			str += "\n";
		}
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
	
	public boolean getWasGoodPartition() {
		return wasGoodPartition;
	}
	
	public void setGoodPartition(boolean val) {
		wasGoodPartition = val;
	}
	
	/*
	 * Copy
	 */
	public Group copy() {
		// create the new microcollection and set the init and the name
		Group group = new Group(this.name.getText(), this.init);
		
		/*
		 * set this stuff
		 	// microinteraction stuff
			private ArrayList<Microinteraction> microinteractions;
						
			// stuff to do with reading in the interaction from supreme
			private int id;
		 */
		group.setID(this.getID());
		
		for (Microinteraction micro : microinteractions) {
			group.getMicrointeractions().add(micro.copy());
		}
		
		
		return group;
	}
	
	public String toString() {
		String str = "GROUP: \n";
		
		for (Microinteraction micro : microinteractions) {
			str += micro.getName();
			str += "\n";
		}
		
		return str;
	}
}
