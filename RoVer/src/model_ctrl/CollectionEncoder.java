package model_ctrl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import model.Group;
import model.Microinteraction;
import model.Module;
import model.State;

public class CollectionEncoder {
	
	private Group group;
	private HashMap<State,Integer> ModelIds;
	private HashMap<Module,String> ModuleStateStrs;
	private final char[] stateChars = {'s', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	private int currStateChar;
	private String filename;
	private HashMap<State,String> collectionStateStrPairs;
	
	public CollectionEncoder(Group group, String filename) {
		this.group = group;
		ModelIds = new HashMap<State,Integer>();
		ModuleStateStrs = new HashMap<Module,String>();
		this.filename = filename;
		currStateChar = 0;
		collectionStateStrPairs = new HashMap<State,String>();
		
		for (Microinteraction m : group.getMicrointeractions()) {
			ModelIds.putAll(Encoder.populateModelIds(m));
			ModuleStateStrs.putAll(Encoder.populateModuleStateStrs(m, m.getName()));
		}
	}
	
	public HashMap<State,ModuleStatePair> encode() {
		// check to ensure that the microinteraction is not null
		if (group == null) {
			System.out.println("Error: microinteraction collection to be encoded does not exist.");
			return null;
		}
		
		// begin the output file
		try{
			// fill the State2Label
			//System.out.println("encoding!");
			HashMap<State,ModuleStatePair> State2Label = new HashMap<State,ModuleStatePair>();
			for (Microinteraction micro : group.getMicrointeractions())
				State2Label.putAll(Encoder.fillModuleStatePairs(micro, ModuleStateStrs, ModelIds));
					
			PrintWriter writer = new PrintWriter(filename, "UTF-8");
			writer.println("mdp\n");
				    
			// encode the global variables
			for (Microinteraction micro : group.getMicrointeractions())
				Encoder.encodeGlobalVars(micro, writer, micro.getName());
				    
			// encode the initial states
			writer.println("init");
			ArrayList<Microinteraction> micros = group.getMicrointeractions();
			for (int i = 0; i < micros.size() - 1; i++) 
				Encoder.encodeInits(micros.get(i), writer, micros.get(i).getName(), ModuleStateStrs, ModelIds, false);
			Encoder.encodeInits(micros.get(micros.size()-1), writer, micros.get(micros.size()-1).getName(), ModuleStateStrs, ModelIds, true);
			writer.println("endinit\n");
				    
			// encode each module
			for (Microinteraction micro : group.getMicrointeractions())
				Encoder.encodeModules(micro, writer, micro.getName(), ModuleStateStrs, ModelIds);
					
			// encode the labels
			for (Microinteraction micro : group.getMicrointeractions())
				Encoder.encodeLabels(micro, writer, micro.getName(), ModuleStateStrs, ModelIds);
			
			writer.close();
			return State2Label;
		} catch (IOException e) {
			return null;
		}
	}

}
