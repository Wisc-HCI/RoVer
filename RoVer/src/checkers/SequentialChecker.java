package checkers;
import model.*;
import model.Group;
import model.Module;
import model_ctrl.FilterUtil;
import model_ctrl.ModuleStatePair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import controller.ConsoleCT;
import enums.StateClass;
import enums.StateCompatabilityLookup;
import parser.ast.*;
import prism.*;
import study.BugTracker;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

public class SequentialChecker {
	
	private Interaction ia;
	private HashMap<Microinteraction,String> Micro2File;
	private HashMap<Microinteraction, HashMap<State, ModuleStatePair>> State2Label;
	private HashMap<Microinteraction, HashMap<ModuleStatePair, State>> Label2State;
	
	// isNonAssisted
	private Boolean isNonAssisted;
	
	// gui stuff
	private ConsoleCT console;
	
	// bug tracking
	private BugTracker bt;
	
	/*
	 * Constructor
	 */
	public SequentialChecker(Interaction ia, HashMap<Microinteraction,String> Micro2File, 
			HashMap<Microinteraction, HashMap<State, ModuleStatePair>> State2Label,
			HashMap<Microinteraction, HashMap<ModuleStatePair, State>> Label2State, 
			ConsoleCT console, Boolean isNonAssisted,
			BugTracker bt) {
		this.ia = ia;
		this.isNonAssisted = isNonAssisted;
		this.console = console;
		this.State2Label = State2Label;
		this.Label2State = Label2State;
		this.Micro2File = Micro2File; 
		this.bt = bt;
	}
	
	public void getStartEndStates(Prism prism, PrismLog mainLog, Microinteraction micro) {
		ArrayList<ArrayList<State>> s = getStartStates(micro);
		micro.setStartStates(s);
		
		ArrayList<ArrayList<State>> e = getEndStates(micro, prism, mainLog);
		micro.setEndStates(e, isNonAssisted);
	}
	
	/*
	 * The actual checker, and associated helper methods
	 */
	public void check(Prism prism, PrismLog mainLog, GroupTransition mt) {
		
		ArrayList<Microinteraction> mtMicros = new ArrayList<Microinteraction>();
		mtMicros.addAll(mt.getSource().getMicrointeractions());
		mtMicros.addAll(mt.getTarget().getMicrointeractions());
		fillStartEndStates(mtMicros, prism, mainLog);
		
		checkMt(prism, mainLog, mt);
	}
	
	public void check(Prism prism, PrismLog mainLog) {
		// find collections of start/end states for each microinteraction
		fillStartEndStates(ia.getMicros(), prism, mainLog);
		checkAll(prism, mainLog);
	}
	
	private void fillStartEndStates(ArrayList<Microinteraction> micros, Prism prism, PrismLog mainLog) {
		for (Microinteraction m : micros) {
			if (m.getStartStates() == null) {
				ArrayList<ArrayList<State>> s = getStartStates(m);
				m.setStartStates(s);
			}
			if (m.getEndStates() == null) {
				ArrayList<ArrayList<State>> e = getEndStates(m, prism, mainLog);
				m.setEndStates(e, isNonAssisted);
			}
		}
	}
		
	public void checkAll(Prism prism, PrismLog mainLog) {
		// iterate through all connections in the interaction
		ArrayList<GroupTransition> mtrans = ia.getMacroTransitions();
		
		for (GroupTransition mt : mtrans)
			checkMt(prism, mainLog, mt);
	}
	
	public void checkMt(Prism prism, PrismLog mainLog, GroupTransition mt) {
		
			int goodCount = 0;
			int badCount = 0;
			
			Group sources = mt.getSource();
			Group targets = mt.getTarget();
			
			String badConnections = "";
			ArrayList<ArrayList<Microinteraction>> currBadConnect = new ArrayList<ArrayList<Microinteraction>>();
			
			for (Microinteraction source : sources.getMicrointeractions()) {
				
				// partition the end states based on the branching
				ArrayList<ArrayList<State>> trueEndStates = partitionEndStates(mt, source);
				
				for (Microinteraction target : targets.getMicrointeractions()) {
					String str = " Checking " + source.getName() + " - " + target.getName() + " connection: ";
					boolean val = checkCompatability(trueEndStates, target.getStartStates());
					str += val;
					if (val == true)
						goodCount++;
					else {
						badCount++;
						badConnections += "   " + source.getName() + " --> " + target.getName() + "\n";
						ArrayList<Microinteraction> connect = new ArrayList<Microinteraction>();
						connect.add(source);
						connect.add(target);
						currBadConnect.add(connect);
					}
					console.updateText(str);
				}
			}
			
			badConnections = badConnections.trim();
			
			// determine whether the macrotransition is good, partially good, not good at all
			Tooltip t = mt.getIndicatorTooltip();
			if (isNonAssisted) {
				mt.getIndicator().setFill(Color.CYAN);
				t.setText("");
			}
			else if (goodCount == 0 && badCount == 0) {
				console.updateText("There are no microinteraction links between " + mt.getSource().getName() + " and " + mt.getTarget().getName() + ".");
				mt.getIndicator().setFill(Color.GRAY);
				t.setText("There are no connections\nbetween microinteractions.");
			}
			else if (goodCount > 0 && badCount == 0) {
				console.updateText("The link between " + mt.getSource().getName() + " and " + mt.getTarget().getName() + " is completely satisfied.");
				mt.getIndicator().setFill(Color.LIGHTGREEN);
				t.setText("All connections between\nmicrointeractions are satisfied.");
			}
			else if (goodCount == 0 && badCount > 0) {
				console.updateText("The link between " + mt.getSource().getName() + " and " + mt.getTarget().getName() + " is completely unsatisfied.");
				mt.getIndicator().setFill(Color.RED);
				t.setText("All connections between\nmicrointeractions are unsatisfied.");
			}
			else {
				console.updateText("The link between " + mt.getSource().getName() + " and " + mt.getTarget().getName() + " is partially satisfied.");
				mt.getIndicator().setFill(Color.YELLOW);
				String str = "Some connections between\nmicrointeractions are unsatisfied:\n" + badConnections;
				t.setText(str);
			}
			
			// count how many bugs were fixes/introduced
			ArrayList<ArrayList<Microinteraction>> prevBadConnect = mt.getBadConnections();
			
			// determine which bugs are new (those in currBadConnect that aren't in prevBadConnect)
			ArrayList<ArrayList<Microinteraction>> toAdd = new ArrayList<ArrayList<Microinteraction>>();
			for (ArrayList<Microinteraction> micros : currBadConnect) {
				if (!prevBadConnect.contains(micros)) {
					bt.addBug("sequential", micros);
					toAdd.add(micros); // (add the new bugs to the badConnection variable in mt)
				}
			}	
			
			// determine which bugs are have been dealt with (those in prevBadConnect that are not in currBadConnect)
			ArrayList<ArrayList<Microinteraction>> toRemove = new ArrayList<ArrayList<Microinteraction>>();
			for (ArrayList<Microinteraction> micros : prevBadConnect) {
				if (!currBadConnect.contains(micros)) {
					bt.removeBug("sequential", micros);
					toRemove.add(micros); // (remove the old bugs that are still in the badConnection variable in mt)
				}
			}
			
			// update list
			mt.getBadConnections().addAll(toAdd);
			mt.getBadConnections().removeAll(toRemove);
			
	}
	
	public ArrayList<ArrayList<State>> partitionEndStates(GroupTransition mt, Microinteraction source) {
		// partition the end states based on the branching
		ArrayList<ArrayList<State>> allEndStates = source.getEndStates();
		ArrayList<ArrayList<State>> trueEndStates = new ArrayList<ArrayList<State>>();
		boolean[] humanBranching = mt.getHumanBranching(); // human branching
		boolean[] breakdownBranching = mt.getBreakdownBranching(); // breakdown branching

		for (ArrayList<State> states : allEndStates) {
			// first see if humanBranching is satisfied
			boolean satisfied = true;

			if (humanBranching[0] || humanBranching[1] || humanBranching[2]) {
				for (int i = 0; i < humanBranching.length; i++) {
					boolean bool = humanBranching[i];
					if (bool == false) {
						// see if states has a state that satisfied the criteria
						for (State state : states) {
							if (i == 0) {
								if (state.getStateClass().name().equals(StateClass.READY.name()))
									satisfied = false;
							} else if (i == 1) {
								if (state.getStateClass().name().equals(StateClass.BUSY.name()))
									satisfied = false;
							} else {
								if (state.getStateClass().name().equals(StateClass.IGNORE.name()))
									satisfied = false;
							}
						}
					}
				}
			}

			if (!satisfied) {
				continue;
			}

			// then see if breakdownBranching is satisfied
			if (breakdownBranching[0] || breakdownBranching[1]) {
				for (int i = 0; i < breakdownBranching.length; i++) {
					boolean bool = breakdownBranching[i];
					if (bool == false) {
						// see if states has a state that satisfied the criteria
						for (State state : states) {

							if (i == 0) {
								if (!state.isBreakdown())
									satisfied = false;
							} else {
								if (state.isBreakdown())
									satisfied = false;
							}

						}
					}
				}
				if (!satisfied) {
					continue;
				}

				// if we've made it this far, then states hasn't been eliminated
				trueEndStates.add(states);
			}
		}
		
		return trueEndStates;
	}
	
	public ArrayList<ArrayList<State>> getStartStates(Microinteraction m) {
		ArrayList<ArrayList<State>> startStates = new ArrayList<ArrayList<State>>();
		for (Module mod : m.getModules())
			startStates.add(mod.getInits());
		
		// generate all possible combinations of starting configurations
		ArrayList<ArrayList<State>> startConfigs = new ArrayList<ArrayList<State>>();
		startConfigs.add(new ArrayList<State>());  // start with a single starting configuration
		for (ArrayList<State> statesInMod : startStates) {
			if (statesInMod.size() == 1) {
				for (ArrayList<State> startConfig : startConfigs)
					startConfig.add(statesInMod.get(0));
			}
			else if (statesInMod.size() > 1) {
				int currNumConfigs = startConfigs.size();
				
				// create duplicate startConfig lists
				ArrayList<ArrayList<State>> temp = new ArrayList<ArrayList<State>>();
				for (int i = 1; i < statesInMod.size(); i++) {
					for (ArrayList<State> config : startConfigs) {
						ArrayList<State> temp2 = new ArrayList<State>(config);
						temp.add(temp2);
					}
				}
				startConfigs.addAll(temp);
				
				int counter = 0;
				int index = 0;
				for (int i = 0; i < currNumConfigs * statesInMod.size(); i++) {
					
					startConfigs.get(i).add(statesInMod.get(index));
					
					counter++;
					if (counter == currNumConfigs) {
						counter = 0;
						index++;
					}
				}
			}
		}
		
		return startConfigs;
	}
	
	public ArrayList<ArrayList<State>> getEndStates(Microinteraction m, Prism prism, PrismLog mainLog) {
		// generate properties
		ModulesFile modulesFile;
		PropertiesFile propertiesFile = null;
		Result result;

		System.out.println("SEQ: Getting end states");
		
		/*
		 * SPECIAL CASE (hack, probably should comment out): if there are no start states, then simply return an empty list!
		 */
		if (m.getNumEnabledHumanInits() == 0 || Micro2File.get(m) == null)
			return new ArrayList<ArrayList<State>>();
				
		try {

			modulesFile = prism.parseModelFile(new File(Micro2File.get(m)));
			prism.loadPRISMModel(modulesFile);

			// get and store the indices for each label (Idx2Label, then Label2State)
			HashMap<Integer,String> Idx2Label = new HashMap<Integer,String>();
			ArrayList<ModuleStatePair> labels = new ArrayList<ModuleStatePair>(); // get keys from the Label2State hashmap
			Iterator it = Label2State.get(m).entrySet().iterator();
			while (it.hasNext()) {
			    HashMap.Entry pair = (HashMap.Entry)it.next();
			    labels.add((ModuleStatePair) pair.getKey());
			}
			for (int i = 0; i < modulesFile.getNumVars(); i++) {
				String labStr = modulesFile.getVarName(i);
				boolean labIsState = false;
				String label = null;
				for (int j = 0; j < labels.size(); j++) {
					if (labels.get(j).mod.equals(labStr)) {
						labIsState = true;
						label = labels.get(j).mod;
						break;
					}
				}
				
				if (labIsState)
					Idx2Label.put(i, label);
			}
			
			// before coming up with a property, check that all arrays have the correct data
			it = Micro2File.entrySet().iterator();
						
			// come up with the filter property and check it
			State end = m.getModuleContainingString("Robot").getState("End");
			String label = State2Label.get(m).get(end).toString();
			
			//TODO: CHECK THAT THIS LABEL IS REACHABLE GIVEN THE ENABLED INITIAL STATES!!!!!!!!!!!!!
			
			String property = "filter(print, Pmax=? [ " + label + " ] );";
			System.out.println(property);

			// parse the results from the filter property, storing which collections of states the micro may result in
			propertiesFile = prism.parsePropertiesString(modulesFile, property);
			//try {
			//	prism.setEngine(prism.HYBRID);
			//} catch (PrismException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			System.out.println("Engine (Explicit is " + Prism.EXPLICIT + "): " + prism.getEngine());
			System.out.println("Fairness is " + prism.getFairness());
			prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
			//try {
			//	prism.setEngine(prism.EXPLICIT);
			//} catch (PrismException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			PrintWriter writer = new PrintWriter(new File("tempout.txt"));
			writer.print("");
			writer.close();// wipe the log file
			mainLog.flush();

			// extract the results and put the string values into an arraylist
			ArrayList<String[]> rawResults = (new FilterUtil()).extractRawResults();
						
			// run through each index and get the labels associated with each index
			ArrayList<ArrayList<State>> endSts = new ArrayList<ArrayList<State>>();
			for (String[] strs : rawResults) {
				ArrayList<State> endSt = new ArrayList<State>();
				for (int i = 0; i < strs.length; i++) {
					String lab = Idx2Label.get(i);
					if (lab != null) {
						// find the module state pair associated with index i
						ModuleStatePair temp = null;
						for (ModuleStatePair msp : labels) {
							if (msp.mod.equals(lab) && msp.state == Integer.parseInt(strs[i])) {
								temp = msp;
								break;
							}
						}
						
						HashMap<ModuleStatePair,State> map = Label2State.get(m);
						State e = map.get(temp);
						endSt.add(e);
					}
				}
				endSts.add(endSt);
			}

			return endSts;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		return null;
	}
	
	public boolean checkCompatability(ArrayList<ArrayList<State>> end, ArrayList<ArrayList<State>> start) {
		boolean isCompat = true;
		
		for (ArrayList<State> endStates : end) {
			boolean foundMatch = false;
			for (ArrayList<State> startStates : start) {
				// check to see that the set of end states is present in the start states
				boolean isMatch = checkCompatabilityHelper(endStates, startStates);
				if (isMatch)
					foundMatch = true;
			}
			
			if (foundMatch == false) {
				isCompat = false;
				break;
			}
		}
		
		return isCompat;
	}
	
	public boolean checkCompatabilityHelper(ArrayList<State> end, ArrayList<State> start) {
		StateCompatabilityLookup scc = new StateCompatabilityLookup();
		boolean isCompat = true;
		
		for (State endState : end) {
			boolean foundState = true;
			for (State startState : start) {
				if (!scc.checkCompatability(endState.getStateClass(), startState.getStateClass()))
					foundState = false;
			}
			
			if (!foundState) {
				isCompat = false;
				break;
			}
		}
		
		return isCompat;
	}
}
