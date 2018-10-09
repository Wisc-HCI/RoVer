package checkers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import enums.StateClass;
import model.*;
import model.Group;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismLog;
import prism.Result;
import study.BugTracker;
import study.GroupMBP;

public class ConcurrentChecker {
	
	private Group group;
	private Checker c;
	private BugTracker bt;
	private Boolean isNonAssisted;
	
	public ConcurrentChecker(Group group, Checker c, BugTracker bt, Boolean isNonAssisted) {
		this.group = group;
		this.c = c;
		this.bt = bt;
		this.isNonAssisted = isNonAssisted;
	}
	
	public void check(Prism prism, PrismLog mainLog) {
		
		/*
		 * create an aggregate-result boolean
		 */
		boolean aggregateResults = true;
		
		/*
		 * initialize the prism file
		 */
		
		ModulesFile modulesFile = null;
		PropertiesFile propertiesFile = null;
		Result result;
		
		try {
			modulesFile = prism.parseModelFile(new File("prism" + File.separator + group.getMacrointeraction().getName() + ".pm"));
			prism.loadPRISMModel(modulesFile);
		} catch (FileNotFoundException | PrismLangException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*
		 * GLOBAL PROPERTIES
		 * 1) The robot should not be speaking at the same time that the human is speaking
		 */
		
		// 1
		// count to see if the microcollection even has a human and robot speech component
		boolean h_speech = false;
		boolean r_speech = false;
		boolean r_speech_reachable = false;
		boolean h_speech_reachable = false;
		
		// we need to set the engine to hybrid temporarily because CTL is not supported by explicit
		try {
			prism.setEngine(prism.HYBRID);
		} catch (PrismException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (Microinteraction micro : group.getMicrointeractions()) {
			for (Module mod : micro.getModules()) {
				for (State st : mod.getStates()) {
					if (st.getStateClass().equals(StateClass.SPEAKING) && st.getAgent().equals("human")) {
						h_speech = true;
						String property = "E [ F (Speechhuman) ];";
						try {
							propertiesFile = prism.parsePropertiesString(modulesFile, property);
							result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
							String resultStr = result.getResultString();
							if (resultStr.contains("true")) {
								h_speech_reachable = true;
							}
						} catch (PrismLangException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PrismException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (st.getStateClass().equals(StateClass.SPEAKING) && st.getAgent().equals("robot")) {
						r_speech = true;
						String property = "E [ F (Speechrobot) ];";
						try {
							propertiesFile = prism.parsePropertiesString(modulesFile, property);
							result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
							String resultStr = result.getResultString();
							if (resultStr.contains("true")) {
								r_speech_reachable = true;
							}
						} catch (PrismLangException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (PrismException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		group.setSpeaks(r_speech_reachable);
		group.setHSpeaks(h_speech_reachable);
		
		// check to see whether speech human comes before speech robot or vice-versa!
		if (r_speech_reachable && !h_speech_reachable) {
			group.setSpeaksFirst(true);
			group.setHSpeaksFirst(false);
		}
		else if (h_speech_reachable && !r_speech_reachable) {
			group.setSpeaksFirst(false);
			group.setHSpeaksFirst(true);
		}
		else if (h_speech_reachable && r_speech_reachable) {
			String propertyHuman = "Pmax=? [ ( !(Speechhuman | Speechrobot)) U Speechhuman ];";
			String propertyRobot = "Pmax=? [ ( !(Speechhuman | Speechrobot)) U Speechrobot ];";
			try {
				propertiesFile = prism.parsePropertiesString(modulesFile, propertyHuman);
				result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
				String resultStr = result.getResultString();
				System.out.println(resultStr);
				if (resultStr.contains("1.0")) {
					group.setHSpeaksFirst(true);
				}
				else
					group.setHSpeaksFirst(false);
				
				propertiesFile = prism.parsePropertiesString(modulesFile, propertyRobot);
				result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
				resultStr = result.getResultString();
				System.out.println(resultStr);
				if (resultStr.contains("1.0")) {
					group.setSpeaksFirst(true);
				}
				else 
					group.setSpeaksFirst(false);
				
			} catch (PrismLangException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PrismException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			group.setSpeaksFirst(false);
			group.setHSpeaksFirst(false);
		}
		
		try {
			//prism.setEngine(prism.EXPLICIT);
			prism.setEngine(prism.HYBRID);
		} catch (PrismException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (h_speech && r_speech) {
			// form the property
			String property = "P<=0 [ F (Speechrobot & Speechhuman) ];";

			try {
				propertiesFile = prism.parsePropertiesString(modulesFile, property);
				PrintWriter writer = new PrintWriter(new File("tempout.txt"));
				writer.print("");
				writer.close();// wipe the log file
				result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
				mainLog.flush();

				if (result.toString().equals("false"))
					aggregateResults = false;
			} catch (PrismLangException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PrismException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/*
		 * determine if the microcollection satisfies the aggregate results
		 * FIRST by updating the bugs!
		 */	
		// update the list of bad pairs -- 1) get the current list 2) distinguish between those to ADD and those to REMOVE
		ArrayList<ArrayList<Microinteraction>> prevBadPairs = group.getBadPairs();
		
		// 2)
		ArrayList<ArrayList<Microinteraction>> currBadPairs = new ArrayList<ArrayList<Microinteraction>>();
		for (Microinteraction micro : group.getMicrointeractions()) {
			if (micro.getName().equals("Greeter") || micro.getName().equals("Farewell") || micro.getName().equals("Instruction")
					|| micro.getName().equals("Remark") || micro.getName().equals("Answer") || micro.getName().equals("Ask")) {
				
				// ok so we have an offending microinteraction (in which the robot speaks)
				for (Microinteraction hmicro : group.getMicrointeractions()) {
					if (!hmicro.equals(micro)) { // can't look at the same micro!
						
						if (hmicro.getName().equals("Answer") || hmicro.getName().equals("Ask") || hmicro.getName().equals("Instruction") 
								|| hmicro.getName().equals("Greeter") || hmicro.getName().equals("Farewell")
								|| (hmicro.getName().equals("Wait") && hmicro.findVariable("allow_speech").getValue().equals("true"))) {
							// ok so we have a microinteraction in here with human speech
							ArrayList<Microinteraction> pair = new ArrayList<Microinteraction>();
							pair.add(micro);
							pair.add(hmicro);
							currBadPairs.add(pair);
						}
					}
				}
			}
		}
		
		ArrayList<ArrayList<Microinteraction>> toAdd = new ArrayList<ArrayList<Microinteraction>>();
		ArrayList<ArrayList<Microinteraction>> toRemove = new ArrayList<ArrayList<Microinteraction>>();

		// see what is in currBadPairs that is not in prevBadPairs (new bugs)
		for (ArrayList<Microinteraction> currPair : currBadPairs) {
			if (!prevBadPairs.contains(currPair)) {
				bt.addBug("speech", currPair);
				toAdd.add(currPair);
			}
		}
		
		// see what is in prevBadPairs that is not in currBadPairs (old bugs)
		for (ArrayList<Microinteraction> micros : prevBadPairs) {
			if (!currBadPairs.contains(micros)) {
				bt.removeBug("speech", micros);
				toRemove.add(micros); // (remove the old bugs that are still in the badConnection variable in mt)
			}
		}
		
		group.getBadPairs().addAll(toAdd);
		group.getBadPairs().removeAll(toRemove);
		
		if (!aggregateResults) {
			group.setViolating(true);
		}
		else {
			group.setViolating(false);
		}
		
		/*
		 * GAZE AND GESTURE "SOFT" PROPERTIES
		 */
		// get a list of gaze/gesture variables from each module in the macrointeraction
		Microinteraction ma = group.getMacrointeraction();
		HashMap<Microinteraction,HashMap<Integer,ArrayList<State>>> micro2idx2states = group.getMicro2Idx2State();
		
		HashMap<Module,HashMap<Integer,String>> moduleGazeStates = new HashMap<Module,HashMap<Integer,String>>();
		HashMap<Module,HashMap<Integer,String>> moduleGestureStates = new HashMap<Module,HashMap<Integer,String>>();
		for (Module mod : ma.getModules()) {
			HashMap<Integer,String> gazeStates = new HashMap<Integer,String>();
		    HashMap<Integer,String> gestureStates = new HashMap<Integer,String>();
			for (Microinteraction mi : group.getMicrointeractions()) {
				if (mod.getName().equals(mi.getName())) {
					HashMap<Integer,ArrayList<State>> idx2states = micro2idx2states.get(mi);
					
					Iterator it = idx2states.entrySet().iterator();
					while (it.hasNext()) {
					    HashMap.Entry pair = (HashMap.Entry)it.next();
					    
					    int idx = (int) pair.getKey();
					    ArrayList<State> states = (ArrayList<State>) pair.getValue();
					    
					    for (State st : states) {
					    	if (!st.getGaze().equals("GAZE_ELSE") && !st.getGaze().equals("GAZE_AT") && !st.getGaze().equals("NONE"))  {
					    		gazeStates.put(idx, st.getGaze());
					    	}
					    	if (!st.getGesture().equals("GESTURE_NONE") && !st.getGesture().equals("NONE")) {
					    		gestureStates.put(idx, st.getGesture());
					    	}
					    }
					}
				}
			}
			moduleGazeStates.put(mod, gazeStates);
			moduleGestureStates.put(mod, gestureStates);
		}	
		
		ArrayList<Module> mods = ma.getModules();
		ArrayList<ArrayList<Module>> combinations = new ArrayList<ArrayList<Module>>();
		for (int i = 2; i <= mods.size(); i++) {
		    int r = i;
		    int n = mods.size();
		    int[] idxs = new int[n];
		    for (int j = 0; j < r; j++) {
		    	idxs[j] = 1;
		    }
		    
	        findCombinations(idxs, r, combinations, mods);
		}
		
		// now for each combination, get a possible combination of states and check that they don't collide!
		ArrayList<PropModsBeh> gazeProperties = getAllProperties(combinations, moduleGazeStates, ma);
		ArrayList<PropModsBeh> gestureProperties = getAllProperties(combinations, moduleGestureStates, ma);
		
		// post-process
		ArrayList<PropModsBeh> propsToRemove = new ArrayList<PropModsBeh>();
		for (PropModsBeh prop : gazeProperties) {
			HashMap<Module,String> mod2beh = prop.mod2beh;
			Set<String> values = new HashSet<String>(mod2beh.values());
			if (values.size() == 1)
				propsToRemove.add(prop);
		}
		for (PropModsBeh prop : propsToRemove) 
			gazeProperties.remove(prop);
		for (PropModsBeh prop : gestureProperties) {
			HashMap<Module,String> mod2beh = prop.mod2beh;
			Set<String> values = new HashSet<String>(mod2beh.values());
			if (values.size() == 1)
				propsToRemove.add(prop);
		}
		for (PropModsBeh prop : propsToRemove) 
			gestureProperties.remove(prop);
		
		// now perform the checking of each property!
		group.wipeUnresolvedBehConflicts();
		for (PropModsBeh pmb : gazeProperties) {
			Result rawResult = runProperty(prism, mainLog, pmb.property, modulesFile, propertiesFile);
			if (rawResult.toString().equals("false"))
				group.checkIfViolating(pmb, false);
			else if (this.isNonAssisted) {
				group.checkIfViolating(pmb, true);
			}
		}
		
		for (PropModsBeh pmb : gestureProperties) {
			Result rawResult = runProperty(prism, mainLog, pmb.property, modulesFile, propertiesFile);
			if (rawResult.toString().equals("false"))
				group.checkIfViolating(pmb, false);
			else if (this.isNonAssisted) {
				group.checkIfViolating(pmb, true);
			}
		}
		
		// check the unresolved behavior conflicts in a manner similar to speech and sequential!
		// count how many bugs were fixes/introduced
		ArrayList<GroupMBP> prevBadMBP = bt.getBehViolations();
		
		// determine which bugs are new (those in currBadConnect that aren't in prevBadConnect)
		for (ModBehPair mbp : group.getUnresolvedBehaviorConflicts()) {
			boolean inPrevBadMBP = false;
			for (GroupMBP mmbp : prevBadMBP) {
				if (mmbp.group.equals(group)) {
					if (mmbp.mbp.equalsOther(mbp))
						inPrevBadMBP = true;
				}
			}
			
			if (!inPrevBadMBP && !mbp.getNonProperty()) {
				GroupMBP mmbp = new GroupMBP(group, mbp);
				bt.addBug("behConflict", mmbp);
			}
		}	
	
		// determine which bugs are have been dealt with (those in prevBadConnect that are not in currBadConnect)
		for (GroupMBP mmbp : prevBadMBP) {
			if (mmbp.group.equals(group)) {
				boolean inCurrUnresolved = false;
				for (ModBehPair mbp : group.getUnresolvedBehaviorConflicts()) {
						if (mbp.equalsOther(mmbp.mbp))
							inCurrUnresolved = true;
				}
				
				if (!inCurrUnresolved  && !mmbp.mbp.getNonProperty()) {
					bt.removeBug("behConflict", mmbp);
				}
			}
		}
	}
	
	private Result runProperty(Prism prism, PrismLog mainLog, String property, ModulesFile modulesFile, PropertiesFile propertiesFile) {
		Result rawResult = null;
		try {
			propertiesFile = prism.parsePropertiesString(modulesFile, property);
			rawResult = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
			mainLog.flush();
		} catch (PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rawResult;
	}
	
	private void generateProperties(Microinteraction ma, ArrayList<Module> combo, HashMap<Module,ArrayList<Integer>> stateIndices, HashMap<Module,Integer> currIdxs, ArrayList<PropModsBeh> properties, HashMap<Module,HashMap<Integer,String>> moduleBehStates) {
		// base case
		boolean done = false;
		for (Module mod : combo) {
			HashMap<Integer,String> states = moduleBehStates.get(mod);
			if (currIdxs.get(mod) >= stateIndices.get(mod).size()) {
				done = true;
				break;
			}
			int currIdx = stateIndices.get(mod).get(currIdxs.get(mod));
			if (states.get(currIdx) == null) {
				done = true;
				break;
			}
		}
		
		if (done)
			return;
		
		// recursive case
		HashMap<Module,String> moduleStateStrs = c.getModuleStateStrings().get(ma);
		
		String property = "P>=1 [ G !(";
		HashMap<Module, String> modBeh = new HashMap<Module, String>();
		
		for (int i = 0; i < combo.size(); i++) {
			Module mod = combo.get(i);
			property += moduleStateStrs.get(mod) + "=" + stateIndices.get(mod).get(currIdxs.get(mod)) + " ";
			if (i != combo.size()-1)
				property += " & ";
			
			modBeh.put(mod, moduleBehStates.get(mod).get(stateIndices.get(mod).get(currIdxs.get(mod))));
		}
		
		property += ") ];";
		
		PropModsBeh pmb = new PropModsBeh(property, modBeh);
		
		// check if already exists
		if (!properties.contains(pmb)) {
			properties.add(pmb);
			
			// more recursion
			for (Module mod : combo) {
				int idx = currIdxs.get(mod);
				HashMap<Module,Integer> newCurrIdxs = (HashMap<Module, Integer>) currIdxs.clone(); 
				newCurrIdxs.put(mod, idx+1);
				generateProperties(ma, combo, stateIndices, newCurrIdxs, properties, moduleBehStates);
			}
		}
	}
	
	private void findCombinations(int[] idxs, int r, ArrayList<ArrayList<Module>> combinations, ArrayList<Module> mods) {
		// add the current combination
		ArrayList<Module> combo = new ArrayList<Module>();
		for (int i = 0; i < idxs.length; i++) {
			if (idxs[i] == 1) {
				combo.add(mods.get(i));
			}
		}
		combinations.add(combo);
		
		// break if cannot increment anymore
		boolean done = true;
		for (int i = 0; i < r; i++) {
			if (idxs[idxs.length - 1 - i] == 0) {
				done = false;
				break;
			}
		}
		if (done)
			return;
		
		// increment
		for (int i = 0; i < idxs.length - 1; i++) {
			if (idxs[i] == 1 && idxs[i+1] == 0) {
				int[] newIdxs = idxs.clone();
				newIdxs[i] = 0;
				newIdxs[i+1] = 1;
				findCombinations(newIdxs, r, combinations, mods);
			}
		}
	}
	
	private ArrayList<PropModsBeh> getAllProperties(ArrayList<ArrayList<Module>> combinations, HashMap<Module,HashMap<Integer,String>> moduleBehStates, Microinteraction ma) {
		ArrayList<PropModsBeh> properties = new ArrayList<PropModsBeh>();
		
		for (ArrayList<Module> combo : combinations) {
			
			// put the integers into arraylist
			HashMap<Module,ArrayList<Integer>> stateIndices = new HashMap<Module,ArrayList<Integer>>();
			boolean empty = false;
			for (Module mod : combo) {
				ArrayList<Integer> indices = new ArrayList<Integer>();
				HashMap<Integer,String> hashIdxs = moduleBehStates.get(mod);
				Iterator it = hashIdxs.entrySet().iterator();
				while (it.hasNext()) {
				    HashMap.Entry pair = (HashMap.Entry)it.next();
				    indices.add((Integer) pair.getKey());
				}
				stateIndices.put(mod,indices);
				if (indices.isEmpty()) {
					empty = true;
					break;
				}
			}
			
			if (empty) 
				continue;
			
			// current index of each arraylist
			HashMap<Module,Integer> currIdxs = new HashMap<Module,Integer>();
			for (Module mod : combo)
				currIdxs.put(mod, 0);
			
			generateProperties(ma, combo, stateIndices, currIdxs, properties, moduleBehStates);
		}
		
		return properties;
	}
}


