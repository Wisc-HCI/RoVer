package checkers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import explicit.Model;
import model.Microinteraction;
import model.State;
import parser.ast.ModulesFile;
import prism.Prism;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismLog;

public class TMExporter {
	
	private HashMap<Microinteraction,String> Micro2File;
	private ModulesFile modulesFile;
	private File tmFile;
	private Microinteraction m;
	
	public TMExporter(HashMap<Microinteraction,String> Micro2File, Microinteraction m) {
		this.Micro2File = Micro2File;
		this.m = m;
	}

	public void exportToFile(Prism prism, PrismLog mainLog) {

		try {
			modulesFile = prism.parseModelFile(new File(Micro2File.get(m)));
			prism.loadPRISMModel(modulesFile);
			
			File f = new File(m.getName()+".txt");
			prism.exportTransToFile(true, Prism.EXPORT_PLAIN, f);
			tmFile = f;
			
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
	
	/*
	 * Precondition -- prism has already loaded up the modules file and exported a TM file
	 */
	public HashMap<Integer,ArrayList<Integer>> parseTM(Prism prism, PrismLog mainLog, HashMap<Integer, ArrayList<State>> idx2states) {
		HashMap<Integer,ArrayList<Integer>> int2int = new HashMap<Integer,ArrayList<Integer>>();
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(tmFile));
			String line;
			
			// discard the first line
			reader.readLine();
			
			// read the rest
			while ((line = reader.readLine()) != null) {
				String[] parsedLine = line.split(" ");
				int key = Integer.parseInt(parsedLine[0]);
				int val = Integer.parseInt(parsedLine[2]);
				
				if (!(int2int.containsKey(key)))
					int2int.put(key, new ArrayList<Integer>());
				int2int.get(key).add(val);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return int2int;
	}
	
	public void removeTMFile() {
		tmFile.delete();
	}
	
}
