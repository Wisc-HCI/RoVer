package model_ctrl;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import model.Guard;
import model.Microinteraction;
import model.Module;
import model.State;
import model.Sync;
import model.Transition;
import model.Update;
import model.Variable;

public class MicroEncoder {
	
	private Microinteraction micro;
	private HashMap<State,Integer> ModelIds;
	private HashMap<Module,String> ModuleStateStrs;
	private final char[] stateChars = {'s', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	private int currStateChar;
	private String filename;
	private boolean isCollection;
	
	public MicroEncoder(Microinteraction micro, String filename){
		this.micro = micro.getBuiltMicro();
		ModuleStateStrs = new HashMap<Module,String>();
		this.filename = filename;
		ModelIds = Encoder.populateModelIds(micro);
		ModuleStateStrs = Encoder.populateModuleStateStrs(micro);
		currStateChar = 0;
		isCollection = false;
	}
	
	public HashMap<State,ModuleStatePair> encode() {
		// check to ensure that the microinteraction is not null
		if (micro == null) {
			System.out.println("Error: microinteraction to be encoded does not exist. Perhaps it was not built?");
			return null;
		}
		
		// begin the output file
		try{
			// fill the State2Label
			HashMap<State,ModuleStatePair> State2Label = Encoder.fillModuleStatePairs(micro, ModuleStateStrs, ModelIds);
			
		    PrintWriter writer = new PrintWriter(filename, "UTF-8");
		    writer.println("mdp\n");
		    
		    // encode the global variables
		    Encoder.encodeGlobalVars(micro, writer);
		    
		    // encode the initiali states
		    writer.println("init");
		    Encoder.encodeInits(micro, writer, ModuleStateStrs, ModelIds, true);
		    writer.println("endinit");
		    
		    // encode each module
		    Encoder.encodeModules(micro, writer, ModuleStateStrs, ModelIds);
			
			// encode the labels
		    Encoder.encodeDisjunctiveFormulas(micro, writer);
		    
		    writer.close();
		    return State2Label;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public HashMap<Module,String> getMSS() {
		return ModuleStateStrs;
	}
	
}