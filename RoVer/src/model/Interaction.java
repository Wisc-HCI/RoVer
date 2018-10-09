package model;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import checkers.Checker;
import checkers.PrismThread;
import checkers.Property;
import controller.ConsoleCT;
import controller.MainController;
import controller.NetworkPropagator;
import javafx.scene.paint.Color;
import model_ctrl.Decoder;
import model_ctrl.MicroParameterizer;
import model_ctrl.TooltipViz;
import study.BugTracker;

/*
 * Class Interaction:
 * Stores the entire interaction, including all microinteractions.
 */
public class Interaction { 
	// model checker for the interaction
	private Checker c;
	
	// initialized immediately
	private String name;
	private ArrayList<Group> groups;
	private ArrayList<Microinteraction> microinteractions;   // simple list of microinteractions
	private HashMap<Integer,Microinteraction> ID2Micro;      // maps ID to microinteraction
	private HashMap<String,Microinteraction> Name2Micro;      // maps ID to microinteraction
	private HashMap<Integer, Group> ID2Group;      // maps ID to microinteraction
	private HashMap<String, Group> Name2Group;      // maps ID to microinteraction
	
	// initilized after reading supreme.xml
	private ArrayList<GroupTransition> transitions;            // transitions between microinteractions
	private ArrayList<Variable> globals;
	private Group init;
	
	// flags
	private boolean built;
	
	// properties
	private ArrayList<Property> graphProperties;
	private HashMap<Integer, java.lang.Boolean> graphPropertyValues;
	
	// bugs
	private boolean authProp;
	private boolean farewellProp;
	
	// Bug tracking
	private BugTracker bugtrack;

	// the network propagation algorithm
	private NetworkPropagator networkPropagator;
	
	// experiment stuff
	private String currDesign;
	private int currInstruction;
	private java.lang.Boolean tutorial;
	
	// color picking
	private int colorPickerIdx;
	private ArrayList<Color> colors;
	
	// is copy
	private boolean isCopy;
	
	// static enders
	private HashMap<String, java.lang.Boolean[]> staticEnders;
	
	
	// isNonAssisted
	private Boolean isNonAssisted;
	
	public Interaction(ArrayList<Property> properties) {
		this.graphProperties = properties;
		initialize();
	}
	
	public Interaction(ArrayList<Microinteraction> micros, ArrayList<Property> properties) {
		this.graphProperties = properties;
		Collections.sort(this.graphProperties);
		initialize();
		for (Microinteraction m : micros) {
			addMicro(m);
		}
	}
	
	public void initialize() {
		isCopy = false;
		c=null;
		built=false;
		groups = new ArrayList<Group>();
		microinteractions = new ArrayList<Microinteraction>();
		ID2Micro = new HashMap<Integer,Microinteraction>();
		Name2Micro = new HashMap<String,Microinteraction>();
		ID2Group = new HashMap<Integer, Group>();
		Name2Group = new HashMap<String, Group>();
		
		staticEnders = new HashMap<String, java.lang.Boolean[]>();
		
		authProp = true;
		farewellProp = true;
		currDesign = null;
		currInstruction = -1;
		tutorial = false;
		
		// properties
		graphPropertyValues = new HashMap<Integer, java.lang.Boolean>();
		for (Property prop : graphProperties) {
			if (prop.getTies().equals("interaction")) {
				graphPropertyValues.put(prop.getID(), prop.getInitVal());
			}
		}

		// bug tracker
		bugtrack = null;

		networkPropagator = new NetworkPropagator(bugtrack);
	}

	public void startDesign(ConsoleCT console, MainController mainController) {
		for (Group group : this.getGroups()) {
			for (Microinteraction micro : group.getMicrointeractions()) {
				this.addMicro(micro);
				PrismThread pt = new PrismThread(console, this, mainController, micro);
				Thread t = pt.getThread();
				pt.start("startEndStates");
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public Checker getChecker() {
		return c;
	}
	
	public void setIsCopy(boolean val) {
		isCopy = val;
	}
	
	public boolean getIsCopy() {
		return isCopy;
	}
	
	public void nullifyChecker() {
		c = null;
	}
	
	public void setNonAssistedSwitch(Boolean val) {
		isNonAssisted = val;
	}
	
	public boolean getIsNonAssisted() {
		return isNonAssisted;
	}
	
	public void setChecker(Checker c, MainController mc, ConsoleCT console) {
		this.c = c;
		//c.switchEngine("explicit");

		isNonAssisted = mc.getNonAssistedSwitch();
		HashMap<String, TooltipViz> staticTooltips = mc.getStaticTooltips();
		
		// if isNonAssisted, get static images of the start and end states 
		if (isNonAssisted && staticTooltips.isEmpty()) {
			
			// we need to one-at-a-time add each microinteraction to init, and calculate the visualization\
			Decoder d = new Decoder(mc, isNonAssisted);
			ArrayList<Microinteraction> micros = new ArrayList<Microinteraction>();
			Microinteraction m;
			// greeter
			m = new Microinteraction();
			d.readMicrointeraction(new File("Lib/Initiate/Greeter.xml"), "Lib/Initiate/Greeter.xml", m);
			micros.add(m);
			// farewell
			m = new Microinteraction();
			d.readMicrointeraction(new File("Lib/End/Farewell.xml"), "Lib/End/Farewell.xml", m);
			micros.add(m);
			// inst_action
			m = new Microinteraction();
			d.readMicrointeraction(new File("Lib/Task_Instruction/Instruction.xml"), "Lib/Task_Instruction/Instruction.xml", m);
			micros.add(m);
			// handoff
			m = new Microinteraction();
			d.readMicrointeraction(new File("Lib/Joint_Action/Handoff.xml"), "Lib/Joint_Action/Handoff.xml", m);
			micros.add(m);
			// comment
			m = new Microinteraction();
			d.readMicrointeraction(new File("Lib/Remark/Remark.xml"), "Lib/Remark/Remark.xml", m);
			micros.add(m);
			// wait
			m = new Microinteraction();
			d.readMicrointeraction(new File("Lib/Wait/Wait.xml"), "Lib/Wait/Wait.xml", m);
			micros.add(m);
			// question
			m = new Microinteraction();
			d.readMicrointeraction(new File("Lib/Ask/Ask.xml"), "Lib/Ask/Ask.xml", m);
			micros.add(m);
			// answer
			m = new Microinteraction();
			d.readMicrointeraction(new File("Lib/Answer/Answer.xml"), "Lib/Answer/Answer.xml", m);
			micros.add(m);
			
			for (Microinteraction micro : micros) {
				micro.build();
				micro.addParameterizer(new MicroParameterizer(micro.getGlobalVars(), mc));
				PrismThread pt = new PrismThread(console, this, mc, micro);
				Thread t = pt.getThread();
				pt.start("startEndStates");
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				TooltipViz staticVis = (TooltipViz) micro.getTooltipViz();
				java.lang.Boolean[] enders = staticVis.draw(isNonAssisted);
				staticEnders.put(micro.getName(), enders);
				staticTooltips.put(micro.getName(), staticVis);
			}
		}
	}
	
	public void initializeInteraction() {
		transitions = new ArrayList<GroupTransition>();
	}

	public java.lang.Boolean[] getStaticEnders(String microName) {
		return staticEnders.get(microName);
	}
	
	public void setCurrDesign(String design) {
		this.currDesign = design;
	}
	
	public String getCurrDesign() {
		return currDesign;
	}
	
	public void setCurrInstruction(int inst) {
		this.currInstruction = inst;
	}
	
	public int getCurrInstruction() {
		return this.currInstruction;
	}
	
	public void setTutorial(boolean val) {
		this.tutorial = val;
	}
	
	public boolean getTutorial() {
		return tutorial;
	}
	
	public ArrayList<Property> getGraphProperties() {
		return graphProperties;
	}
	
	public HashMap<Integer, java.lang.Boolean> getGraphPropertyValues() {
		return graphPropertyValues;
	}
	
	public void addMicro(Microinteraction m) {
		microinteractions.add(m);
		if (m.getID() >= 0)
			ID2Micro.put(m.getID(), m);
		Name2Micro.put(m.getName(), m);
		m.getMicroBox().setMBColor(colorPick(m));
	}
	
	public void addGroup(Group group) {
		groups.add(group);
		group.addInteraction(this);
		if (group.getID() >= 0)
			ID2Group.put(group.getID(), group);
		Name2Group.put(group.getName(), group);
	}
	
	public void updateMicroID(Microinteraction m) {
		ID2Micro.put(m.getID(), m);
	}
	
	public void updateGroupID(Group group) {
		ID2Group.put(group.getID(), group);
	}
	
	public void setInit(Group init) {
		this.init = init;
	}
	
	public void addTransition(Group source, Group target) {
		transitions.add(new GroupTransition(source,target, bugtrack));
	}
	
	public void addTransition(GroupTransition mac) {
		transitions.add(mac);
	}
	
	public void updateAllAndDisplayConditions() {
		for (GroupTransition mt : transitions) {
			mt.unGray();
			mt.updateAndDisplayConditions(); 
		}
	}
	
	public Microinteraction getMicro(int ID) {
		return ID2Micro.get(ID);
	}
	
	public Microinteraction getMicro(String name) {
		if (Name2Micro.get(name) == null) {
			System.out.println(name + " does not exist as a microinteraction!");
			return null;
		}
		return Name2Micro.get(name);
	}
	
	public ArrayList<Microinteraction> getMicros() {
		return microinteractions;
	}
	
	public Group getGroup(int ID) {
		return ID2Group.get(ID);
	}
	
	public Group getGroup(String name) {
		if (Name2Group.get(name) == null) {
			System.out.println(name + " does not exist as a Group!");
			return null;
		}
		return Name2Group.get(name);
	}
	
	public ArrayList<Group> getGroups() {
		return groups;
	}
	
	public ArrayList<GroupTransition> getMacroTransitions() {
		return transitions;
	}
	
	public BugTracker getBugTracker() {
		return bugtrack;
	}

	public void addBugTracker(BugTracker bugtrack) {
		this.bugtrack = bugtrack;
	}

	public void killBugTracker(String name) {
		bugtrack.kill(name);
	}

	public void makeBugtrackerNull() {
		bugtrack = null;
	}

	public NetworkPropagator getNetworkPropagator() {
		return networkPropagator;
	}

	public void reinitializeBugTracker() {
		bugtrack = new BugTracker(this);
		c.addBugTracker(bugtrack);
		networkPropagator = new NetworkPropagator(bugtrack);
		for (Group group : getGroups()) {
			group.addBugTracker(bugtrack);
		}
		for (GroupTransition mt : getMacroTransitions()) {
			mt.addBugTracker(bugtrack);
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setAuthProp(boolean val) {
		if (this.authProp != val && val == true)
			bugtrack.removeBug("authProp", this);
		else if (this.authProp != val && val == false)
			bugtrack.addBug("authProp", this);
		authProp = val;
	}
	
	public boolean getAuthProp() {
		return authProp;
	}
	
	public void setFarewellProp(boolean val) {
		if (this.farewellProp != val && val == true)
			bugtrack.removeBug("farewellProp", this);
		else if (this.farewellProp != val && val == false)
			bugtrack.addBug("farewellProp", this);
		farewellProp = val;
	}
	
	public boolean getFarewellProp() {
		return farewellProp;
	}
	
	// graph properties
	public void setProp(Property prop, boolean val) {
		if (prop.getTies().equals("init")) {
			getInit().setGraphProp(prop,val);
			System.out.println("******************INIT: " + getInit().getName() + ": greeter satisfied? " + val);
		}
		else {
			boolean propVal = graphPropertyValues.get(prop.getID());
			String propBugID = prop.getBugtrackID();
			if (propVal != val && val == true) {
				bugtrack.removeGraphBug(prop, null);
			}
			else if (propVal != val && val == false) {
				bugtrack.addGraphBug(prop, null);
			}
			graphPropertyValues.put(prop.getID(), val);
		}
	}
	
	public boolean getProp(Property prop) {
		return graphPropertyValues.get(prop.getID());
	}
	
	public boolean isViolatingBranch() {
		boolean violation = false;
		for (Group group : groups) {
			if (!group.checkBranchingPartition()[1]) {
				violation = true;
				break;
			}
		}
		return violation;
	}
	
	public boolean isViolatingSequential() {
		boolean violation = false;
		for (GroupTransition mt : transitions) {
			ArrayList<ArrayList<Microinteraction>> badConnections = mt.getBadConnections();
			if (!badConnections.isEmpty()) {
				violation = true;
				break;
			}
		}
		return violation;
	}
	
	public boolean isViolatingSomething() {
		if (!farewellProp || !authProp || !getInit().getGreetingProp() || isViolatingBranch() || isViolatingSequential()) {
			return true;
		}
		return false;

	}
	
	public void setBuilt(boolean val) {
		built = val;
	}
	
	public boolean[] obtainStarters(Group group) {
		boolean anyReady = false;
		boolean anyBusy = false;
		boolean anyIgnore = false;
		
		boolean readyAvailable = true;
		boolean busyAvailable = true;
		boolean ignoreAvailable = true;
		
		for (Microinteraction micro : group.getMicrointeractions()) {
			boolean[] endStates = micro.getHumanStartStates();
			if (endStates[0])
				anyReady = true;
			else
				readyAvailable = false;
			if (endStates[1])
				anyBusy = true;
			else
				busyAvailable = false;
			if (endStates[2])
				anyIgnore = true;
			else
				ignoreAvailable = false;
		}
		
		
		
		boolean[] aggregate = {anyReady, anyBusy, anyIgnore, readyAvailable, busyAvailable, ignoreAvailable};
		return aggregate;
	}
	
	public Group isWithinMicroCollection(double X, double Y) {
		Group within = null;
		for (Group group : groups) {
			double groupX = group.getLayoutX();
			double groupY = group.getLayoutY();
			
			double xDim = group.getWidth();
			double yDim = group.getHeight();

			if (X >= groupX && X <= groupX + xDim && Y >= groupY && Y <= groupY + yDim) {
				within = group;
				break;
			}
		}
		
		return within;
	}
	
	public boolean testIsCyclic() {
		ArrayList<Group> groupsToTraverse = new ArrayList<Group>();
		groupsToTraverse.add(init);
		ArrayList<Group> seen = new ArrayList<Group>();
		
		boolean cycle = false;
		
		while (!groupsToTraverse.isEmpty()) {
			Group curr = groupsToTraverse.get(0);
			groupsToTraverse.remove(0);
			if (seen.contains(curr)) {
				cycle = true;
				break;
			}
			
			seen.add(curr);
			
			for (GroupTransition macro : curr.getOutputMacroTransitions()) {
				groupsToTraverse.add(macro.getTarget());
			}
		}
		
		return cycle;
	}
	
	
	
	public Color colorPick(Microinteraction m) {

		switch (m.getName()) {
		case "Farewell":
			return Color.PALEVIOLETRED;
		case "Greeter":
			return Color.LIGHTGRAY;
		case "Instruction":
			return Color.LIGHTPINK;
		case "Handoff":
			return Color.AQUAMARINE;
		case "Answer":
			return Color.LIGHTYELLOW;
		case "Remark":
			return Color.LIGHTBLUE;
		case "Ask":
			return Color.ANTIQUEWHITE;
		case "Wait":
			return Color.LIGHTSEAGREEN;
		case "Start":
			return Color.WHITE;
		case "End":
			return Color.WHITE;
		default:
			return Color.AQUAMARINE;
		}
	}
	
	public String toString() {
		String str = "INTERACTION: " + this.name + "\n";
		
		for (Group group : groups)
			str += group.toString();
		
		return str;
	}

	public Group getInit() {
		return init;
	}
	
	public Interaction copy() {
		// init the interaction and load up the properties
		Interaction iaCopy = new Interaction(graphProperties);
		BugTracker btCopy = new BugTracker(iaCopy);
		iaCopy.initializeInteraction();
		
		// handle everything that got initialized from the get-go
		/*
		built = false
		groups = new ArrayList<Group>();
		microinteractions = new ArrayList<Microinteraction>();
		ID2Micro = new HashMap<Integer,Microinteraction>();
		Name2Micro = new HashMap<String,Microinteraction>();
		ID2Group = new HashMap<Integer, Group>();
		Name2Group = new HashMap<String, Group>();
		
		authProp = true;
		farewellProp = true;
		
		// properties
		graphPropertyValues = new HashMap<Integer,Boolean>();
		for (Property prop : graphProperties) {
			if (prop.getTies().equals("interaction")) {
				graphPropertyValues.put(prop.getID(), prop.getInitVal());
			}
		}
		*/
		//built
		iaCopy.setBuilt(true);
		
		// do the microcollections and the microinteractions at the same time
		for (Group group : groups) {
			Group newGroup = group.copy();
			iaCopy.addGroup(newGroup);
			if (group.isInit()) {
				iaCopy.setInit(newGroup);
			}
			
			for (Microinteraction micro : newGroup.getMicrointeractions()) {
				iaCopy.addMicro(micro);
			}
		}
						
		// do we need to set any of the properties? I don't think so.
		
		// link the properties
		for (Group group : iaCopy.getGroups()) {
			group.addInteraction(iaCopy);
			group.addBugTracker(btCopy);
		}
		
		// now... go through the macrotransitions
		for (GroupTransition macro : transitions) {
			Group source = iaCopy.findGroup(macro.getSource().getName());
			Group target = iaCopy.findGroup(macro.getTarget().getName());
			
			GroupTransition newMacro = macro.copy(source, target);
			newMacro.addBugTracker(btCopy);
			iaCopy.addTransition(newMacro);
		}
		
		iaCopy.addBugTracker(btCopy);
		
		iaCopy.setIsCopy(true);
		
		return iaCopy;
	}
	
	// to help with copying
	public Group findGroup(String name) {
		return Name2Group.get(name);
	}
}
