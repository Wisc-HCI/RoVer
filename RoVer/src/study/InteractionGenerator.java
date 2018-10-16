package study;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import checkers.PrismThread;
import checkers.Property;
import controller.ImportMicrosCT;
import controller.MainController;
import controller.NetworkPropagator;
import javafx.scene.shape.Polygon;
import model.*;
import model.Group;
import model_ctrl.Decoder;
import model_ctrl.Exporter;

public class InteractionGenerator implements Runnable {
	
	private MainController mc;
	private NetworkPropagator np;
	private ImportMicrosCT imct;
	private Thread t;
	private int numGenerated;
	private int numExported;
	
	public InteractionGenerator(MainController mc, NetworkPropagator np, ImportMicrosCT imct) {
		this.mc = mc;
		this.np = np;
		this.imct = imct;
		this.numGenerated = 0;
		this.numExported = 0;
		t = new Thread(this,"randoInteract");
	}
	
	public void start() {
		t.start();
	}
	
	@Override
	public void run() {
		begin(5);
	}
	
	@SuppressWarnings("restriction")
	private void begin(int iterations) {
		
		// copy the interaction
		Interaction original = mc.getInteraction();
		Interaction starter = original.copy();
		starter.setName("COPY");
		verify(starter);
		
		if (!checkDuplicates(original, starter))
			return;
		
		ArrayList<Property> allProperties = starter.getGraphProperties();
		int[] propsToAddTemp = {0,1,11,4};
		ArrayList<Integer> propsToSatisfy = new ArrayList<Integer>();
		for (int i = 0; i < propsToAddTemp.length; i++) {
			propsToSatisfy.add(propsToAddTemp[i]);
		}
		
		generate(starter, starter.getInit(), propsToSatisfy, getAllOriginalPropertyViolations(original, propsToSatisfy), 2, 3, 3);
		
	}
	
	/*
	 * the main recursive algorithm:
	 * input: 
	 * 		- the starting interaction
	 * 		- list of properties to satisfy
	 * 		- static list of properties not being satisfied by the original interaction (we will assume that it is never OK to violate sequential 
	 * 			or concurrent props, and it is not OK to violate branching at the end)
	 * 		- maximum BFS path length
	 * 		- source node for BFS
	 * 		- maximum number of groups
	 * 		- maximum number of microinteractions (should be around same as groups)
	 */
	public void generate(Interaction starter, Group source, ArrayList<Integer> toSatisfy, HashMap<Integer, java.lang.Boolean> originalInteractionProps, int maxBfsLen, int maxGroupNum, int maxMicroNum) {
		System.gc();
		
		numGenerated += 1;
		//if (numGenerated%10 == 0 && numGenerated > 0)
		System.out.println("Generated and tested " + numGenerated + " interactions.");
		
		// preliminaries
		verify(starter);
		HashMap<Integer, java.lang.Boolean> starterInteractionProps = getAllOriginalPropertyViolations(starter, toSatisfy);
		
		Exporter exp = new Exporter(starter, numGenerated+"");
		String name = exp.export();
		
		/*
		 * 8*********************8
		 * 8     BASE CASES!     8
		 * 8*********************8
		 * 8*********************8
		 */
		
		/*
		 * Base case 1: make sure that no additional graph properties have not been violated, and no concurrent or sequential properties exist
		 */
		// graph
		Iterator it = originalInteractionProps.entrySet().iterator();
		boolean noAddedViolations = true;
		int violatingProperty = -1;
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			int key = (int) pair.getKey();
			boolean val = (boolean) pair.getValue();
			
			if (starterInteractionProps.get(key) == false && val == true) {
				noAddedViolations = false;
				violatingProperty = key;
				break;
			}
		}
		
		
		
		if (!noAddedViolations) {
			System.out.println("BREAK -- detected added GRAPH violation in generated interaction, graph property " + violatingProperty);
			return;
		}
		
		// sequential
		GroupTransition violatingTransition = null;
		for (GroupTransition macro : starter.getMacroTransitions()) {
			if (macro.getBadConnections().size() > 0) {
				violatingTransition = macro;
				break;
			}
		}
		
		if (violatingTransition != null) {
			System.out.println("BREAK -- detected added SEQUENTIAL violation in generated interaction, transition " + violatingTransition.toString());
			return;
		}
		
		// concurrent
		Group violatingGroup = null;
		for (Group group : starter.getGroups()) {
			if (group.getViolating()) {
				violatingGroup = group;
				break;
			}
		}
		
		if (violatingGroup != null) {
			System.out.println("BREAK -- detected added SPEECH violation in generated interaction, group " + violatingGroup.getName());
			return;
		}
		
		/*
		 * Base case 2: check path length, number of groups, and number of microinteractions
		 */
		// number of groups
		if (starter.getGroups().size() > maxGroupNum) {
			System.out.println("BREAK -- number of groups is greater than allowed " + starter.getGroups().size() + " > " + maxGroupNum);
			return;
		}
		
		// number of micros
		if (starter.getMicros().size() > maxMicroNum) {
			System.out.println("BREAK -- number of groups is greater than allowed " + starter.getMicros().size() + " > " + maxMicroNum);
			return;
		}
		
		// max path length
		int maxPathLength = bfsPathLen(source, new ArrayList<GroupTransition>());
		if (maxPathLength > maxBfsLen) {
			System.out.println("BREAK -- number of paths is greater than allowed " + maxPathLength + " > " + maxBfsLen);
			return;
		}
		
		/*
		 * Base case 3: then check if the starting interaction satisfies the property WITHOUT violating a branch condition!
		 */
		boolean satisfies = true;
		for (Integer prop : toSatisfy) {
			if (!starterInteractionProps.get(prop)) {
				satisfies = false;
			}
		}
		
		// double check the branching...
		boolean branching = true;
		if (satisfies) {
			for (Group group : starter.getGroups()) {
				if (!group.checkBranchingPartition()[1])
					branching = false;
			}
			if (!branching) {
				System.out.println("BREAK -- the interaction satisfies all required properties, but branching partitions are bad.");
				return;
			}
		}
		
		if (satisfies){
			System.out.println("WRITING INTERACTION -- the interaction satisfies all required properties");
			exp = new Exporter(starter, numExported+"");
			exp.export();
			numExported += 1;
			return;
		}
		else {
			// we get to continue with the method, then
			System.out.println("the interaction does not satisfy all required properties");
		}
		
		/*
		 * 8*********************8
		 * 8   RECURSIVE CASES!  8
		 * 8*********************8
		 * 8*********************8
		 */
		
		// try adding a microinteraction -- only do this if there exists a group with < 2 microinteractions already in it
		for (Group group : starter.getGroups()) {
			if (group.getMicrointeractions().size() < 2 && (starter.getMicros().size() < maxMicroNum)) {
				// try adding every single microinteraction to this group
				Interaction nextStep = starter.copy();
				Microinteraction micro = addMicrointeractionHelper(group.getName(), imct.getMBFileByKeyword("Farewell"), nextStep);
				if (micro != null) {
					System.out.println("Trying to add a microinteraction.");
					
					// prep to nullify
					ArrayList<Property> graphProperties = starter.getGraphProperties();
					starter = null;
					generate(nextStep, nextStep.getGroup(source.getName()), toSatisfy, originalInteractionProps, maxBfsLen, maxGroupNum, maxMicroNum);
					
					// re-read
					starter = new Interaction(graphProperties);
					Decoder d = new Decoder(mc.getNonAssistedSwitch());
					d.readSupreme(name+".xml", starter);
				}
			}
		}
		
		// try adding a group
		boolean existsEmpty = false;
		for (Group group : starter.getGroups()) {
			if (group.getMicrointeractions().size() == 0) {
				existsEmpty = true;
				break;
			}
		}
		if (!existsEmpty && (starter.getGroups().size() < maxGroupNum)) {
			Interaction nextStep = starter.copy();
			Group group = new Group(false,nextStep.getBugTracker());
			// set the name
			group.setName("untitled" + nextStep.getGroups().size());
			group.addInteraction(nextStep);
			nextStep.getGroups().add(group);
			System.out.println("Trying to add a group.");
			
			// prep to nullify
			ArrayList<Property> graphProperties = starter.getGraphProperties();
			starter = null;
			generate(nextStep, nextStep.getGroup(source.getName()), toSatisfy, originalInteractionProps, maxBfsLen, maxGroupNum, maxMicroNum);
			// re-read
			starter = new Interaction(graphProperties);
			Decoder d = new Decoder(mc.getNonAssistedSwitch());
			d.readSupreme(name+".xml", starter);
		}
		
		
		// try adding a transition
		for (Group sourceGroup : starter.getGroups()) {
			
			// search for places to ~~start~~ transitions
			for (Group targetGroup : starter.getGroups()) {
				// find compatabilities
				boolean[] allowed = {true, true, true};
				findCompatibleBranches(sourceGroup, targetGroup, allowed, starter, source, toSatisfy, originalInteractionProps, maxBfsLen, maxGroupNum, maxMicroNum, name);
			}
		}
		
		System.gc();
	}
	
	private void findCompatibleBranches(Group source, Group target, boolean[] allowed, Interaction starter, Group iaSource,
                                        ArrayList<Integer> toSatisfy, HashMap<Integer, java.lang.Boolean> originalInteractionProps, int maxBfsLen, int maxGroupNum, int maxMicroNum, String name) {
		
		// base case 1: check that source is even reachable from the interaction source!
		if (!bfsFindGroup(iaSource, new ArrayList<GroupTransition>(), source)) {
			System.out.println("Can't find the transition source node " + source.getName() + " from the interaction source node " + iaSource.getName());
			return;
		}
		
		// base case 2:
		if (allowed[0] == false && allowed[1] == false && allowed[2] == false) {
			System.out.println("All of the conditions are false!");
			return;
		}
		
		boolean[] sourceOutputs = source.getHumanEndStates();
		
		//TODO: move this function inside of interaction, or microcollection
		boolean[] targetInputs = starter.obtainStarters(target);
		
		// check whether the branch is allowable!
		boolean goodBranch = true;
		for (int i = 0; i < allowed.length; i++) {
			if (allowed[i] == true && (sourceOutputs[i] == false || targetInputs[i] == false))
				goodBranch = false;
		}
		
		// check whether the branch is already taken up!
		for (GroupTransition macro : source.getOutputMacroTransitions()) {
			boolean[] humanBranching = macro.getHumanBranching();
			for (int i = 0; i < allowed.length; i++) {
				if (allowed[i] == true && humanBranching[i] == true)
					goodBranch = false;
			}
		}
		
		// copy the interaction and recurse!
		if (goodBranch) {
			Interaction nextStep = starter.copy();
			Group newSource = nextStep.findGroup(source.getName());
			Group newTarget = nextStep.findGroup(target.getName());
			GroupTransition newMacro = new GroupTransition(newSource, newTarget, nextStep.getBugTracker());
			boolean[] humanBranching = newMacro.getHumanBranching();
			humanBranching[0] = allowed[0];
			humanBranching[1] = allowed[1];
			humanBranching[2] = allowed[2];
			
			newMacro.setPoly(new Polygon());
			nextStep.addTransition(newMacro);
			System.out.println("Trying to add a transition.");
			
			// prep to nullify
			ArrayList<Property> graphProperties = starter.getGraphProperties();
			starter = null;
			generate(nextStep, nextStep.getGroup(iaSource.getName()), toSatisfy, originalInteractionProps, maxBfsLen, maxGroupNum, maxMicroNum);
			
			// re-read
			starter = new Interaction(graphProperties);
			Decoder d = new Decoder(mc.getNonAssistedSwitch());
			d.readSupreme(name+".xml", starter);
		}
		
		for (int i = 0; i < allowed.length; i++) {
			if (allowed[i]) {
				boolean[] newAllowed = new boolean[allowed.length];
				for (int j = 0; j < allowed.length; j++) {
					if (i!=j) {
						newAllowed[j] = allowed[j];
					}
				}
				findCompatibleBranches(source, target, newAllowed, starter, iaSource, toSatisfy, originalInteractionProps, maxBfsLen, maxGroupNum, maxMicroNum, name);
			}
		}
	}
	
	public Microinteraction addMicrointeractionHelper(String name, File file, Interaction ia) {
		// find the group
		Group node = null;
		for (Group group : ia.getGroups()) {
			if (group.getName().equals(name)) {
				node = group;
				break;
			}
		}
		
		MicroBox mb;
		if (file != null) {
			mb = imct.getMBbyID(file.getAbsolutePath());
		}
		else {
			mb = imct.getRandomMB();
			file = mb.getFile();
		}
				
		Microinteraction newMicro = new Microinteraction();
		(new Decoder(mc,new Boolean(false))).readMicrointeraction(file, file.getAbsolutePath(), newMicro, mb);
		newMicro.build();
		
		boolean alreadyExists = false;
		int microCount = 0;
		for (Microinteraction micro : ((Group) node).getMicrointeractions()) {
			if (micro.getName().equals(newMicro.getName())) 
				alreadyExists = true;
			microCount += 1;
		}
		
		if (alreadyExists) {
			System.out.println("Cannot add microinteraction " + newMicro.getName() + " because it already exists in this grouping.");
			return null;
		}
		
		// actually add it
		ia.addMicro(newMicro);
		((Group) node).addMicro(newMicro);
		
		return newMicro;
	}
	
	public boolean bfsFindGroup(Group source, ArrayList<GroupTransition> alreadyTakenTransitions, Group target) {
		if (source.equals(target)) {
			return true;
		}
		
		boolean found = false;
		for (GroupTransition macro : source.getOutputMacroTransitions()) {
			if (!alreadyTakenTransitions.contains(macro)) {
				alreadyTakenTransitions.add(macro);
				found = bfsFindGroup(macro.getTarget(), alreadyTakenTransitions, target);
			}
			if (found)
				break;
		}
		
		return found;
	}
	
	public int bfsPathLen(Group source, ArrayList<GroupTransition> alreadyTakenTransitions) {
		int maxPathLen = 0;
		for (GroupTransition macro : source.getOutputMacroTransitions()) {
			int currPathLen = 0;
			if (!alreadyTakenTransitions.contains(macro)) {
				alreadyTakenTransitions.add(macro);
				currPathLen = 1 + bfsPathLen(macro.getTarget(), alreadyTakenTransitions);
			}
			if (currPathLen > maxPathLen)
				maxPathLen = currPathLen;
		}
		
		return maxPathLen;
	}
	
	public boolean checkDuplicates(Interaction original, Interaction starter) {
		// check that the property evaluations don't differ between the copy and the original
		HashMap<Integer, java.lang.Boolean> originalInteractionProps = original.getGraphPropertyValues();
		HashMap<Integer, java.lang.Boolean> starterInteractionProps = starter.getGraphPropertyValues();
			
		boolean goodCopy = checkDuplicatesHelper(originalInteractionProps, starterInteractionProps, "Interaction");	
				
		// check that the size is the same
		if (!goodCopy) {
			System.out.println("ERROR: property values not the same in interaction copy");
			return false;
		}
		
		for (int i = 0; i < original.getGroups().size(); i++) {
			HashMap<Integer, java.lang.Boolean> originalInteractionPropsGroup = original.getGroups().get(i).getGraphPropertyValues();
			HashMap<Integer, java.lang.Boolean> starterInteractionPropsGroup = starter.getGroups().get(i).getGraphPropertyValues();
			
			if (!checkDuplicatesHelper(originalInteractionPropsGroup, starterInteractionPropsGroup, original.getGroups().get(i).getName() + " " + starter.getGroups().get(i).getName())) {
				goodCopy = false;
				break;
			}
		}
		
		if (!goodCopy) {
			System.out.println("ERROR: property values not the same in interaction copy");
			return false;
		}
		
		return true;
	}
	
	public boolean checkDuplicatesHelper(HashMap<Integer, java.lang.Boolean> originalInteractionProps, HashMap<Integer, java.lang.Boolean> starterInteractionProps, String loc) {
		// check that the size is the same
		if (originalInteractionProps.size() != starterInteractionProps.size()) {
			System.out.println("ERROR: property size not the same in interaction copy, in " + loc);
			return false;
		}
						
		Iterator it = originalInteractionProps.entrySet().iterator();
		boolean goodCopy = true;
		int currProp = -1;
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			int key = (int) pair.getKey();
			currProp = key;
			if (((boolean) pair.getValue()) != starterInteractionProps.get(key)) {
				goodCopy = false;
				break;
			}
		}
						
						
		// check that the size is the same
		if (!goodCopy) {
			System.out.println("ERROR: property value " + currProp + " not the same in interaction copy, in " + loc);
			return false;
		}
		return true;		
	}
	
	public HashMap<Integer, java.lang.Boolean> getAllOriginalPropertyViolations(Interaction ia, ArrayList<Integer> relevantProps) {
		HashMap<Integer, java.lang.Boolean> newMap = new HashMap<Integer, java.lang.Boolean>();
		Iterator it = ia.getGraphPropertyValues().entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			int key = (int) pair.getKey();
			boolean val = (boolean) pair.getValue();
			if (relevantProps.contains(key))
				newMap.put(key, val);
		}
		
		for (Group group : ia.getGroups()) {
			HashMap<Integer, java.lang.Boolean> groupPropVals = group.getGraphPropertyValues();
			it = groupPropVals.entrySet().iterator();
			while (it.hasNext()) {
				HashMap.Entry pair = (HashMap.Entry)it.next();
				int key = (int) pair.getKey();
				boolean val = (boolean) pair.getValue();
				
				// if the existing map already contains this key, but the new key is true
				if (relevantProps.contains(key)) {
					if (newMap.containsKey(key) && val == false)
						newMap.put(key, val);
					else if (!newMap.containsKey(key))
						newMap.put(key, val);
				}
			}
		}
		
		return newMap;
	}
	
	public void verify(Interaction starter) {
		starter.nullifyChecker();
		PrismThread pt = new PrismThread(mc.getConsole(), starter, mc);
		Thread t = pt.getThread();
		pt.start("");
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		starter.getChecker().addBugTracker(starter.getBugTracker());
		
		// initialize the starting and ending states for each microinteraction
		for (Microinteraction micro : starter.getMicros()) {
			pt = new PrismThread(mc.getConsole(), starter, mc, micro);
			t = pt.getThread();
			pt.start("startEndStates");
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// verify the interaction
		// start over again by wiping the end states!
		for (Group group : starter.getGroups()) {
			System.out.println(group.getName());
			group.wipeEndStates();
			//group.initEnabledStartStates();
			// mark all of the microcollections as needing an update!
			group.markForUpdate();
		}
				 
		// find the init!
		Group init = starter.getInit();
		ArrayList<Group> groupsToUpdate = new ArrayList<Group>();
		groupsToUpdate.add(init);
			
		// find any other disjoint "inits"
				
		np.propagateSequentialChanges(groupsToUpdate, mc.getConsole(), starter, mc, false);
				
		// verify concurrent and graph
		pt = new PrismThread(mc.getConsole(), starter, mc, starter.getInit());
		t = pt.getThread();
		pt.start("allConcurrent");
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pt = new PrismThread(mc.getConsole(), starter, mc, starter.getInit());
		t = pt.getThread();
		pt.start("graph");
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
