package checkers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import model.Microinteraction;
import model.State;
import model_ctrl.ModuleStatePair;
import parser.ast.ModulesFile;
import prism.Prism;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismLog;

public class DotExporter {
	
	private HashMap<Microinteraction,String> Micro2File;
	private HashMap<Microinteraction, HashMap<State, ModuleStatePair>> State2Label;
	private HashMap<Microinteraction, HashMap<ModuleStatePair, State>> Label2State;
	
	private File dotFile;
	
	public DotExporter(HashMap<Microinteraction,String> Micro2File, 
			HashMap<Microinteraction, HashMap<State, ModuleStatePair>> State2Label,
			HashMap<Microinteraction, HashMap<ModuleStatePair, State>> Label2State) {
		this.Micro2File = Micro2File;
		this.Label2State = Label2State;
		this.State2Label = State2Label;
	}
	
	public HashMap<Integer,ArrayList<State>> exportToDotFile(Prism prism, PrismLog mainLog, Microinteraction m) {
		ModulesFile modulesFile;
		
		try {
			System.out.println("About to call prism with file " + Micro2File.get(m));
			modulesFile = prism.parseModelFile(new File(Micro2File.get(m)));
			System.out.println("Parsed model file");
			prism.loadPRISMModel(modulesFile);
			System.out.println("Loaded prism model");
			System.out.println(prism.getExplicit());
			
			File f = new File("temp.dot");
			prism.exportTransToFile(true, Prism.EXPORT_DOT_STATES, f);
			System.out.println("Called prism to export to dot file");
			
			// TODO: modularize the following code. It is the same as the code in sequential checker
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
			
			it = Idx2Label.entrySet().iterator();
			while (it.hasNext()) {
			    HashMap.Entry pair = (HashMap.Entry)it.next();
			}
			it = State2Label.get(m).entrySet().iterator();
			while (it.hasNext()) {
			    HashMap.Entry pair = (HashMap.Entry)it.next();
			}
			it = Label2State.get(m).entrySet().iterator();
			while (it.hasNext()) {
			    HashMap.Entry pair = (HashMap.Entry)it.next();
			}
						
			// to store the states
			HashMap<Integer, ArrayList<State>> idx2states = new HashMap<Integer, ArrayList<State>>();
			
			// read and rewrite the dot file
			File fNew = new File("dot_files" + File.separator + m.getName() + ".dot");
			PrintWriter writer = new PrintWriter(fNew);
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			    	if (line.contains(" [label=")) {
			    		String beg = line.substring(0, line.indexOf("=")+1);
			    		String end = line.substring(line.indexOf("(")+1, line.indexOf(")"));
			    		
			    		// adding an index to the idx2states hashmap
			    		Integer intIdx = Integer.parseInt(line.substring(line.indexOf("\"")+1, line.indexOf("\\")));
			    		idx2states.put(intIdx, new ArrayList<State>());
			    		
			    		writer.print(beg);
			    		
			    		String[] varsRaw = end.split(",");
			    		
			    		// store the state names
			    		ArrayList<String> stateNames = new ArrayList<String>();
			    		
			    		// use idx2label to obtain the label names
			    		it = Idx2Label.entrySet().iterator();
						while (it.hasNext()) {
						    HashMap.Entry pair = (HashMap.Entry)it.next();
						    int idx = (int) pair.getKey();
						    String modVal = varsRaw[idx];
						    
						    // search for the correct module state pair
						    ModuleStatePair lab = null;// = new ModuleStatePair((String)pair.getValue(), Integer.parseInt(modVal));
						    Iterator it2 = Label2State.get(m).entrySet().iterator();
							while (it2.hasNext()) {
							    HashMap.Entry p = (HashMap.Entry)it2.next();
							    ModuleStatePair msp = (ModuleStatePair) p.getKey();//).toString() + " " + ((State) pair.getValue()).getName());
							    if (msp.mod.equals(pair.getValue()) && msp.state == Integer.parseInt(modVal))
							    	lab = msp;
							}
						    
						    State result = Label2State.get(m).get(lab);
						    stateNames.add(result.getName());
						    
						    // adding stuff to the idx2states arraylist
						    idx2states.get(intIdx).add(result);
						}
						
						// print the arraylist just in case
						String conglomerate = "";
						Collections.sort(stateNames, Collections.reverseOrder());
						for (String res : stateNames) 
							conglomerate += res + "\n";
						conglomerate = conglomerate.trim();
						
			    		writer.println("\"(" + conglomerate + ")\"];");
			    	}
			    	else
			    		writer.println(line);
			    }
			    writer.close();
			    
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			dotFile = fNew;
			f.delete();
			
			return idx2states;
			
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
		return null;
	}
	
	public void removeDotFile() {
		dotFile.delete();
	}
	
}
