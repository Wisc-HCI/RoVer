package checkers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import model.*;
import model.Group;
import model_ctrl.FilterUtil;
import model_ctrl.GraphEncoder;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismLog;
import prism.Result;

public class GraphChecker {

private Interaction ia;
	
	// properties
	ArrayList<Property> properties;
	HashMap<Integer, Group> idx2group;
	
	public GraphChecker(Interaction ia) {
		this.ia = ia;
		this.properties = ia.getGraphProperties();
		
		// temporarily hard-code properties to list
		//properties.put("P>=1 [ Greeter | Wait ];", 0);
		//properties.put("P>=1 [ G (\"deadlock\" <=> (Farewell & outer)) ];", 1);
		//properties.put("filter(print, Pmax=? [X (Greeter & inner)], Greeter);", 2);
		//properties.put("filter(print, Pmax=? [ ((\"deadlock\" | !(X Remark)) | ((X X \"deadlock\") | !(X X X Wait))) & (\"deadlock\" | !(X (Wait & Remark))) ], Instruction & busy);", 3);
		//properties.put("filter(print, Pmax=? [ ((\"deadlock\" | !(X Answer)) & (\"deadlock\" | !(X Remark) | (X X \"deadlock\") | !(X X X Answer))) ], Instruction & ignore);", 4);
	}
	
	public void check(Prism prism, PrismLog mainLog) {
		
		/*
		 * initialize the prism file
		 */
		ModulesFile modulesFile = null;
		PropertiesFile propertiesFile = null;
		
		/*
		 * get the variables ready to accept results
		 */
		ArrayList<Group> filterResults;
		
		/*
		 * create the prism file of the whole interaction
		 */
		GraphEncoder ge = new GraphEncoder(ia);
		File prismFile = ge.encode();
		idx2group = ge.getIdx2Group()
;		
		try {
			//prism.setEngine(prism.HYBRID);
			prism.setFairness(true);
			modulesFile = prism.parseModelFile(prismFile);
			prism.loadPRISMModel(modulesFile);
		} catch (FileNotFoundException | PrismLangException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// iterate through the properties
		boolean result = true;
		Result rawResult = new Result(false);
		
		for (Property property : properties) {

	    	// filter property
	    	if (property.getProperty().contains("filter")) {
	    		clearPrismOutputFile();
	    		
	    		rawResult = runProperty(prism, mainLog, modulesFile, propertiesFile, property.getProperty());
	    		
	    		filterResults = extractFilterResults();
	    		for (Group m : ia.getGroups()) {
	    			if (filterResults.contains(m)) {
	    				m.setGraphProp(property, false);
	    			}
	    			else {
	    				m.setGraphProp(property, true);
	    			}
	    			
	    		}
	    	}
	    	
	    	// non-filter property
	    	else {
	    		rawResult = runProperty(prism, mainLog, modulesFile, propertiesFile, property.getProperty());
	    		if (rawResult.toString().equals("false")) {
	    			ia.setProp(property, false);
	    		}
	    		else {
	    			ia.setProp(property, true);
	    		}
	    		
	    	}
	    }
		
		try {
			//prism.setEngine(prism.EXPLICIT);
			prism.setFairness(false);
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Result runProperty(Prism prism, PrismLog mainLog, ModulesFile modulesFile, PropertiesFile propertiesFile, String property) {
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
			//e.printStackTrace();
		}
		return rawResult;
	}
	
	private void clearPrismOutputFile() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File("tempout.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.print("");
		writer.close();// wipe the log file
	}
	
	private ArrayList<Group> extractFilterResults() {
		ArrayList<String[]> rawFilterResults = (new FilterUtil()).extractRawResults();
		ArrayList<Group> filterResults = new ArrayList<Group>();
		for (String[] strs : rawFilterResults) {
			int groupId = Integer.parseInt(strs[strs.length-1]);
			Group m = idx2group.get(groupId);
			if (!filterResults.contains(m))
				filterResults.add(m);
		}
		return filterResults;
	}
}
