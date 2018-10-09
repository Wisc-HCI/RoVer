package checkers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import model.*;
import model.Group;
import model_ctrl.ModuleStatePair;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismLog;
import prism.Result;

public class ReachabilityChecker {
	
	private Interaction ia;
	private HashMap<Microinteraction,String> Micro2File;
	private HashMap<Microinteraction, HashMap<State, ModuleStatePair>> State2Label;
	
	public ReachabilityChecker(Interaction ia, 
			HashMap<Microinteraction,String> Micro2File, 
			HashMap<Microinteraction, HashMap<State, ModuleStatePair>> State2Label) {
		this.ia = ia;
		this.State2Label = State2Label;
		this.Micro2File = Micro2File;
	}
	
	public void check(Prism prism, PrismLog mainLog) {
		for (Microinteraction m : ia.getMicros()) {
			check(m, prism, mainLog);
		}
	}
	
	public void check(Microinteraction m, Prism prism, PrismLog mainLog) {
		// generate properties
		ArrayList<String> properties = getReachabilityProperties(m);
		ModulesFile modulesFile;
		PropertiesFile propertiesFile = null;
		Result result;
		
		try {
			modulesFile = prism.parseModelFile(new File(Micro2File.get(m)));
			prism.loadPRISMModel(modulesFile);
			
			for (String property : properties) {
				propertiesFile = prism.parsePropertiesString(modulesFile, property);
				result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public ArrayList<String> getReachabilityProperties(Microinteraction m) {
		ArrayList<String> properties = new ArrayList<String>();
		
		Iterator it = State2Label.get(m).entrySet().iterator();
	    while (it.hasNext()) {
	        HashMap.Entry pair = (HashMap.Entry)it.next();

	        ModuleStatePair val = (ModuleStatePair) pair.getValue();
	        
	        // add the property
	        properties.add("P>=1[F " + val.mod + "=" + val.state + "]");
	        properties.add("E [ F " + val.mod + "=" + val.state + " ]");
	    }
		
		return properties;
	}
	
	public void checkEndStates(Prism prism, PrismLog mainLog, Group group) {
		// if we can access the state (we can do that...it's an integer!)
		// then we can access the ModuleStatePair! Which is what we need.
		
		// first we must obtain lists of properties, each property a combination of microinteraction end states!
		
		ArrayList<String> properties = new ArrayList<String>();
		
		HashMap<Microinteraction,ArrayList<Integer>> tempEndStateIdxs = group.getEndStateIdxs();
		ArrayList<Microinteraction> microinteractions = group.getMicrointeractions();
		
		ArrayList<Integer> currIdxs = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> endStateIdxs = new ArrayList<ArrayList<Integer>>();
		for (Microinteraction micro : microinteractions) {
			currIdxs.add(0);
			endStateIdxs.add(tempEndStateIdxs.get(micro));
		}
		
		HashMap<State,ModuleStatePair> s2msp = State2Label.get(group.getMacrointeraction());
		Iterator it = s2msp.entrySet().iterator();
		while (it.hasNext()) {
		    HashMap.Entry pair = (HashMap.Entry)it.next();
		   // System.out.println(((State) pair.getKey()).getName() + " - " + pair.getValue());
		}
		//if (group.getMicrointeractions().size() > 1)
		//	System.exit(0);
		
		
		checkEndStatesHelper(properties, currIdxs, endStateIdxs, group);

		ModulesFile modulesFile;
		PropertiesFile propertiesFile = null;
		Result result;
		
		try {
			modulesFile = prism.parseModelFile(new File(Micro2File.get(group.getMacrointeraction())));
			prism.loadPRISMModel(modulesFile);
			
			for (String property : properties) {
				propertiesFile = prism.parsePropertiesString(modulesFile, property);
				result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(0));
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private void checkEndStatesHelper(ArrayList<String> properties, ArrayList<Integer> currIdxs,
			ArrayList<ArrayList<Integer>> endStateIdxs, Group group ) {
		
		// take the current endstate indexes from endStateIdxs and form a property!
		String property = "E [ F  (";
		ArrayList<Microinteraction> microinteractions = group.getMicrointeractions();
		for (int i = 0; i < currIdxs.size(); i++) {
			// what is the current end state index for his microinteraction?
			int currIdx = endStateIdxs.get(i).get(currIdxs.get(i));
			State st = null;
			
			// need to convert currIdx to the ModuleStatePair
			// first find the correct state
			Microinteraction tempMicro = group.getMacrointeraction();
			ArrayList<Module> modules = tempMicro.getModules();
			for (int j = 0; j < modules.size(); j++ ) {
				// check that the module name matches the microinteraction at the corresponding index
				String microName = microinteractions.get(i).getName();
				String moduleName = modules.get(j).getName();
				if (microName.equals(moduleName))
					st = modules.get(j).getState(currIdx + "");
			}
			
			// so we found the correct state, now we must obtain the module state pair
			ModuleStatePair msp = State2Label.get(tempMicro).get(st);
			
			// write out the property
			property += " " + msp.mod + "=" + msp.state + " ";
			
			// add the &
			if (i < currIdxs.size()-1)
				property += "&";
		}
		property += ") ]";
		properties.add(property);
		
		// update the endStateIdx
		boolean done = true;
		for (int i = currIdxs.size() - 1; i >= 0; i--) {
			int newVal = currIdxs.get(i) + 1;
			if (newVal >= endStateIdxs.get(i).size()) {
				newVal = 0;
				currIdxs.set(i, newVal);
			}
			else {
				currIdxs.set(i, newVal);
				done = false;
				break;
			}
		}
		
		// base case
		if (done)
			return;
		
		// recursive case
		else 
			checkEndStatesHelper(properties, currIdxs, endStateIdxs, group);
	}	

}
