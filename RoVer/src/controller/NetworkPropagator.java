package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import checkers.Checker;
import checkers.PrismThread;
import enums.StateClass;
import model.*;
import model.GroupTransition;
import study.BugTracker;

public class NetworkPropagator {
	
	private static BugTracker bt;
	
	public NetworkPropagator(BugTracker bt) {
		this.bt = bt;
	}

	public static void propagateSequentialChanges(ArrayList<Group> groupsToUpdate, ConsoleCT console, Interaction ia, MainController mc, boolean concurrent) {
		// get the checker
		Checker c = ia.getChecker();
		
		/*
		 * Iterate through each of the microcollections to update!
		 */
		
		while (groupsToUpdate.size() > 0) {
				
			/*
			 * Get and remove the relevant microcollection
			 */
			
			Group group = groupsToUpdate.get(0);
			groupsToUpdate.remove(group);
			
			/*
			 * Zeroth thing's zeroth! Get the current enabled/disabled inits
			 */
			
			Iterator it = group.getEnabledStartStates().entrySet().iterator();
			// will require SOOOO many changes in the future!
		    boolean ready = true;
		    boolean busy = true;
		    boolean ignore = true;
			while (it.hasNext()) {
			    HashMap.Entry pair = (HashMap.Entry)it.next();
			    
			    if ((Boolean) pair.getValue() == false) {
			    	if (pair.getKey().equals(StateClass.READY))
			    		ready = false;
			    	if (pair.getKey().equals(StateClass.BUSY))
			    		busy = false;
			    	if (pair.getKey().equals(StateClass.IGNORE))
			    		ignore = false;
			    }
			    
			}

			/*
			 * While we're at it, obtain the current end states for later use.
			 */
			
			boolean[] currEndStates = group.getHumanEndStates();
			
			/*
			 * First thing's first. Get what source state classes must now be enabled
			 */
			
			// determine whether all input macrotransitions are empty
			boolean emptyInputs = true;
			for (GroupTransition macro : group.getInputMacroTransitions()) {
				Group source = macro.getSource();
				if (!source.getMicrointeractions().isEmpty() && !source.getMarking()) {   // or WAS empty!
					emptyInputs = false;
				}
			}
			boolean[] beginnings = {false, false, false};
			if (group.getInputMacroTransitions().size() == 0 || emptyInputs) {
				beginnings[0] = true;
				beginnings[1] = true;
				beginnings[2] = true;
			}
			else {
				for (GroupTransition sourceTrans : group.getInputMacroTransitions()) {
					boolean[] humanBranching = sourceTrans.getHumanBranching();
					Group source = sourceTrans.getSource();
					if (!source.getMarking()) {
						boolean[] sourceEnds = source.getHumanEndStates();
						for (int i = 0; i < sourceEnds.length; i++) {
							if (sourceEnds[i] && humanBranching[i]) {
								beginnings[i] = true;
							}
						}
					}
				}
			}
			
			if (beginnings[0])
				group.getEnabledStartStates().put(StateClass.READY, true);
			else
				group.getEnabledStartStates().put(StateClass.READY, false);
			if (beginnings[1])
				group.getEnabledStartStates().put(StateClass.BUSY, true);
			else
				group.getEnabledStartStates().put(StateClass.BUSY, false);
			if (beginnings[2])
				group.getEnabledStartStates().put(StateClass.IGNORE, true);
			else
				group.getEnabledStartStates().put(StateClass.IGNORE, false);
			
			/*
			 * Second! Red-do enabling/disabling the start states
			 */
			
			// disable relevant inits!
			for (Microinteraction micro : group.getMicrointeractions()) {
				for (Module mod : micro.getModules())
					mod.disableInits(beginnings[0],  beginnings[1], beginnings[2]);
			}
			
			/*
			 * Re-compute the prism file and calculate the end states from that prism file!
			 */

			System.out.println("Getting the start and end states for each microinteraction");
			for (Microinteraction micro : group.getMicrointeractions()) {
				PrismThread pt = new PrismThread(console, ia, mc, micro);
				Thread t = pt.getThread();
				pt.start("startEndStates");
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Done getting the start and end states for each microinteraction");
			
			/*
			 * check the sequential composition of everything connected to the microcollection
			 */
			
			ArrayList<GroupTransition> links = group.getAllMacroTransitions();
			for (GroupTransition mt : links) {
				console.updateText(mt.toString());
				PrismThread pt = new PrismThread(console, ia, mc, mt);
				Thread t = pt.getThread();
				pt.start("sequential");
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Done with sequential checking");
			
			/*
			 * re-compute the macrointeraction
			 */
			if (!group.getMicrointeractions().isEmpty()) {
				System.out.println("Creating a reduced merged microinteraction");
				group.createReducedMergedMacrointeraction(c, console, ia, mc);
				System.out.println("Created a reduced merged microinteraction");
				if (concurrent) {
					PrismThread pt = new PrismThread(console, ia, mc, group);
					Thread t = pt.getThread();
					pt.start("concurrent");
					try {
						t.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			System.out.println("Done with concurrennt checking");
			
			/*
			 * update the indicator lights
			 */
			
			if (!mc.getNonAssistedSwitch()) {
				boolean[] result = mc.getEndStates(group);
				boolean[] humanResult = {result[0], result[1], result[2]};			
				
				boolean readyExists = result[0];
				boolean busyExists = result[1];
				boolean ignoreExists = result[2];
				
				boolean breakdownExists = result[3];
				boolean noBreakdownExists = result[4];
				
				//group.setReady(readyExists);
				//group.setBusy(busyExists);
				//group.setIgnore(ignoreExists);
				group.updateEndIndicators(humanResult);
				group.setNoBreakdown(noBreakdownExists);
				group.setBreakdown(breakdownExists);
			}
			else {
				boolean[] staticAggregate = {false, false, false};
				for (Microinteraction micro : group.getMicrointeractions()) {
					Boolean[] staticEnders = ia.getStaticEnders(micro.getName());
					staticAggregate[0] = staticAggregate[0] | staticEnders[0];
					staticAggregate[1] = staticAggregate[1] | staticEnders[1];
					staticAggregate[2] = staticAggregate[2] | staticEnders[2];
				}
				group.updateEndIndicators(staticAggregate);
				group.setNoBreakdown(true);
				group.setBreakdown(false);
			}
			
			if (!group.checkBranchingPartition()[0]) {
				group.greyAllMacroTransitionsOut();
			}
			
			if (!group.checkBranchingPartition()[1]) {
				if (group.getWasGoodPartition()) {
					bt.addBug("branching", group);
					group.setGoodPartition(false);
				}
			}
			
			/*
			 * figure out the available start states!
			 */
			boolean[] aggregateStartStates = ia.obtainStarters(group);
			group.updateStartIndicators(aggregateStartStates);
			
			/*
			 * compare the new end states to the old end states. If they differ, and the branching conditions allow for it, then add target microcollection to arraylist!
			 */
			
			boolean[] newEnds = group.getHumanEndStates();
			// if the old ends differ from the new ends
			if (newEnds[0] != currEndStates[0] || newEnds[1] != currEndStates[1] || newEnds[2] != currEndStates[2] || group.getMarking()) {
				for (GroupTransition mtrans : group.getOutputMacroTransitions()) {
					Group targetGroup = mtrans.getTarget();
					
					// if there is actually a difference based on what is allowed through the macrotransition
					boolean[] humanBranching = mtrans.getHumanBranching();
					boolean isDiff = false;
					for (int i = 0; i < humanBranching.length; i++) {
						if (humanBranching[i]) {
							if (newEnds[i] != currEndStates[i])
								isDiff = true;
						}
					}
					
					if (isDiff || targetGroup.getMarking() || group.getMarking()) {
						// add the targetGroup to the list of groups to perform network propagation with
						groupsToUpdate.add(0, targetGroup);
						group.unmark();
					}
				}
			}
			
			for (GroupTransition mtrans : group.getOutputMacroTransitions()) {
				mtrans.updateAndDisplayConditions();
			}
			
		}
	}
	
}
