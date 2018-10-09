package checkers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import controller.ConsoleCT;
import model.*;
import model.Group;
import model_ctrl.MicroEncoder;
import model_ctrl.ModuleStatePair;
import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import study.BugTracker;

public class Checker {
	
	// store the interaction to perform the checks on
	Interaction ia;
		
	// must map microinteractions to the files, and the states within microinteractions to the state labels
	private HashMap<Microinteraction,String> Micro2File;
	private HashMap<Microinteraction, HashMap<State,ModuleStatePair>> State2Label;
	private HashMap<Microinteraction, HashMap<ModuleStatePair,State>> Label2State;
	private HashMap<Microinteraction, HashMap<Module,String>> moduleStateStrings;
	
	// prism stuff
	PrismLog mainLog;
	Prism prism;
	
	// gui stuff
	ConsoleCT console;
	
	// is nonassisted?
	Boolean isNonAssisted;
	
	// storage
	private Object scratch;
	
	// bug tracking
	private BugTracker bt;
	
	// graph properties
	private ArrayList<Property> graphProperties;
	
	/*
	 * Constructor
	 */
	public Checker(Interaction ia, ConsoleCT console, Boolean isNonAssisted, ArrayList<Property> graphProperties) {
		this.isNonAssisted = isNonAssisted;
		this.ia = ia;
		this.console = console;
		initialize();
	}
	
	private void initialize() {
		Micro2File = new HashMap<Microinteraction,String>();
		State2Label = new HashMap<Microinteraction, HashMap<State,ModuleStatePair>>();
		Label2State = new HashMap<Microinteraction, HashMap<ModuleStatePair,State>>();
		moduleStateStrings = new HashMap<Microinteraction, HashMap<Module,String>>();
		prism = null;
		mainLog = null;
	}
	
	public void addBugTracker(BugTracker bugTrack) {
		bt = bugTrack;
	}
	
	/*
	 * Prism stuff
	 */
	public void startPrism() {
		// Init
		console.updateText(" Initializing the Prism Model Checker...");
		mainLog = new PrismFileLog("tempout.txt");
		prism = new Prism(mainLog, mainLog);
		
		try {
			prism.initialise();
			console.updateText(" Prism successfully initialized.");
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			console.updateText(" ERROR: Prism was unable to start.");
		}
		
	}
	
	public void closePrism() {
		if (prism != null)
			prism.closeDown();
	}
	
	public void generatePrismFiles() {
		console.updateText(" Generating prism files...");
		for (Microinteraction m : ia.getMicros()) {
			generatePrismFilesHelper(m);
		}
	}
	
	public void generatePrismFile(Microinteraction m) {
		generatePrismFilesHelper(m);
	}
	
	private void generatePrismFilesHelper(Microinteraction m) {
		String filename = "prism" + File.separator + m.getName() + ".pm";
		Micro2File.put(m, filename);
		MicroEncoder enc = new MicroEncoder(m, filename);
		HashMap<State,ModuleStatePair> states = enc.encode();
		HashMap<Module,String> moduleStateStr = enc.getMSS();
		moduleStateStrings.put(m,moduleStateStr);
		
		// reverse the key-value pairs in states
		HashMap<ModuleStatePair,State> revstates = new HashMap<ModuleStatePair,State>();
		Iterator it = states.entrySet().iterator();
	    while (it.hasNext()) {
	        HashMap.Entry pair = (HashMap.Entry)it.next();
	        revstates.put((ModuleStatePair) pair.getValue(), (State) pair.getKey());
	    }
		
		if (states != null) {
			State2Label.put(m,states);
			Label2State.put(m,revstates);
		}
		else
			return;
	}
	
	/*
	 * Settings
	 */
	public void switchEngine(String engine) {
		if (engine.equals("explicit")) {
			try {
				prism.setEngine(prism.EXPLICIT);
			} catch (PrismException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (engine.equals("hybrid")) {
			try {
				prism.setEngine(prism.HYBRID);
			} catch (PrismException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public HashMap<Microinteraction, HashMap<Module,String>> getModuleStateStrings() {
		return moduleStateStrings;
	}
	
	public Object getScratch() {
		return scratch;
	}
	
	public HashMap<Microinteraction, String> getMicro2File() {
		return Micro2File;
	}
	
	/*
	 * Checkers
	 */
	// reachability #1
	public void checkReachability() {
		scratch = null;
		ReachabilityChecker reach = new ReachabilityChecker(ia, Micro2File, State2Label);
		reach.check(prism, mainLog);
	}
	
	// reachability #2
	public void checkReachability(Microinteraction m) {
		scratch = null;
	}
	
	public void checkEndStateReachability(Group group) {
		scratch = null;
		boolean hasStartStates = true;
		for (Microinteraction micro : group.getMicrointeractions()) {
			if (micro.getNumEnabledHumanInits() == 0)
				hasStartStates = false;
		}
		
		if (group.getMicrointeractions().size() > 0 && hasStartStates) {
			ReachabilityChecker reach = new ReachabilityChecker(ia, Micro2File, State2Label);
			reach.checkEndStates(prism, mainLog, group);
		}
	}
	
	// sequential
	public void checkSequential() {
		scratch = null;
		SequentialChecker seq = new SequentialChecker(ia, Micro2File, State2Label, Label2State, console, isNonAssisted, bt);
		seq.check(prism, mainLog);
	}
	
	public void checkSequential(GroupTransition mt) {
		scratch = null;
		SequentialChecker seq = new SequentialChecker(ia, Micro2File, State2Label, Label2State, console, isNonAssisted, bt);
		seq.check(prism, mainLog, mt);
	}
	
	public void getStartEndStates(Microinteraction m) {
		System.out.println("Getting start states");
		scratch = null;
		SequentialChecker seq = new SequentialChecker(ia, Micro2File, State2Label, Label2State, console, isNonAssisted, bt);
		System.out.println("Getting start end states");
		seq.getStartEndStates(prism, mainLog, m);
		System.out.println("Done getting start end states");
	}
	
	// concurrent
	public void checkConcurrent(Group group) {
		scratch = null;
		ConcurrentChecker con = new ConcurrentChecker(group,this,bt,isNonAssisted);
		con.check(prism, mainLog);
	}
	
	// graph
	public void checkGraph(Interaction ia) {
		scratch = null;
		GraphChecker graph = new GraphChecker(ia);
		graph.check(prism, mainLog);
	}
	
	// export to dot
	public void dotExporter(Microinteraction m) {
		scratch = null;
		DotExporter dot = new DotExporter(Micro2File, State2Label, Label2State);
		dot.exportToDotFile(prism, mainLog, m);
	}
	
	// export to transition matrix
	public void tmExporter(Microinteraction m) {
		scratch = null;

		System.out.println("In the exporter checker method");

		// first export to the dot file and obtain the hashmap of idxs2states
		DotExporter dot = new DotExporter(Micro2File, State2Label, Label2State);
		System.out.println("About to export to dot file");
		HashMap<Integer, ArrayList<State>> idx2states = dot.exportToDotFile(prism, mainLog, m);
		dot.removeDotFile();

		// for debugging
		Iterator it = idx2states.entrySet().iterator();
		while (it.hasNext()) {
		    HashMap.Entry pair = (HashMap.Entry)it.next();

		    for (State state : ( (ArrayList<State>) pair.getValue())) {
		    }
		}

		TMExporter tm = new TMExporter(Micro2File, m);
		tm.exportToFile(prism, mainLog);
		HashMap<Integer, ArrayList<Integer>> int2int = tm.parseTM(prism, mainLog, idx2states);
		tm.removeTMFile();

		it = int2int.entrySet().iterator();
		while (it.hasNext()) {
		    HashMap.Entry pair = (HashMap.Entry)it.next();
		}

		scratch = new ArrayList<Object>();
		((ArrayList<Object>) scratch).add(idx2states);
		((ArrayList<Object>) scratch).add(int2int);
	}
	
}
