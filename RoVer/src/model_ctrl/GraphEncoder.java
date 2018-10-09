package model_ctrl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import model.*;
import model.Group;

public class GraphEncoder {
	
	private Interaction ia;
	private HashMap<Group,Integer> group2idx;
	private HashMap<Integer, Group> idx2group;
	private HashMap<String, ArrayList<Integer>> micro2idxs;
	
	public GraphEncoder(Interaction ia) {
		this.ia = ia;
		group2idx = new HashMap<Group,Integer>();
		idx2group = new HashMap<Integer, Group>();
		micro2idxs = new HashMap<String, ArrayList<Integer>>();
	}
	
	public File encode() {
		File pfile = new File("graph.pm");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(pfile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// header
		writer.println("mdp\n");
		HashMap<String, Boolean> microNames = new HashMap<String, Boolean>();
		microNames.put("Greeter", false);
		microNames.put("Farewell", false);
		microNames.put("Wait", false);
		microNames.put("Instruction", false);
		microNames.put("Handoff", false);
		microNames.put("Ask", false);
		microNames.put("Answer", false);
		microNames.put("Remark", false);
		//microNames.add("Start");
		//microNames.add("End");
		
		/*
		 * Map groups to indices and indices to microinteractions
		 */
		Iterator it = microNames.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			String m = (String) pair.getKey();
			micro2idxs.put(m, new ArrayList<Integer>());
		}

		int counter = 0;
		for (Group group : ia.getGroups()) {
			//System.out.println(ia.getName() + " - " +  group.getName());
			group2idx.put(group, counter);
			idx2group.put(counter, group);
			for (Microinteraction m : group.getMicrointeractions())
				micro2idxs.get(m.getName()).add(counter);
			counter++;	
		}
		
		/*
		 * This is the new encoding scheme
		 */
		writer.println("global start: bool init true;");
		writer.println("global end: bool init false;");
		writer.println("global hstate: [0..3] init 0;");
		writer.println("global hstate_overall: [0..2] init 2;");
		
		writer.println("\nmodule mod\n");
		
		// encode the variables
		writer.println("\ts: [0.." + ((ia.getGroups().size()-1)==0?1:(ia.getGroups().size()-1)) + "] init " + group2idx.get(ia.getInit()) + ";\n\n");

		// encode the inner workings of each group
		for (Group group : ia.getGroups()) {
			int idx = group2idx.get(group);
			writer.println("\t// " + group.getName());
			boolean[] starters = ia.obtainStarters(group);
			if (starters[3])
				writer.println("\t[] s=" + idx + " & start=true & hstate=0 -> (s'=" + idx + ") & (start'=false) & (hstate'=3);");
			if (starters[4])
				writer.println("\t[] s=" + idx + " & start=true & hstate=1 -> (s'=" + idx + ") & (start'=false) & (hstate'=3);");
			if (starters[5])
				writer.println("\t[] s=" + idx + " & start=true & hstate=2 -> (s'=" + idx + ") & (start'=false) & (hstate'=3);");
			
			boolean ends[] = group.getHumanEndStates();
			if (ends[0])
				writer.println("\t[] s=" + idx + " & start=false & end=false -> (s'=" + idx + ") & (end'=true) & (hstate'=0) & (hstate_overall'=0);");
			if (ends[1])
				writer.println("\t[] s=" + idx + " & start=false & end=false -> (s'=" + idx + ") & (end'=true) & (hstate'=1) & (hstate_overall'=1);");
			if (ends[2])
				writer.println("\t[] s=" + idx + " & start=false & end=false -> (s'=" + idx + ") & (end'=true) & (hstate'=2) & (hstate_overall'=2);");
			
			writer.println("\t// " + group.getName() + " outgoing transitions");
			for (GroupTransition macro : group.getOutputMacroTransitions()) {
				int inputIdx = group2idx.get(macro.getTarget());
				boolean[] conditions = macro.getHumanBranching();
				
				if (conditions[0]) 
					writer.println("\t[] s=" + idx + " & start=false & end=true & hstate=0 -> (s'=" + inputIdx + ") & (start'=true) & (end'=false);");
				if (conditions[1])
					writer.println("\t[] s=" + idx + " & start=false & end=true & hstate=1 -> (s'=" + inputIdx + ") & (start'=true) & (end'=false);");
				if (conditions[2])
					writer.println("\t[] s=" + idx + " & start=false & end=true & hstate=2 -> (s'=" + inputIdx + ") & (start'=true) & (end'=false);");
			}
		}

		/*
		 * THIS IS THE OLD ENCODING SCHEME
		 * 
		for (Microinteraction micro : ia.getInit().getMicrointeractions()) {
			writer.print("global " + micro.getName() + ": bool init true;");
			microNames.remove(micro.getName());
		}
		for (String str : microNames) {
			writer.println("global " + str + ": bool init false;");
		}
		// 2 additional
		writer.println("global start: bool init true;");
		writer.println("global end: bool init false;");
		writer.println("global ready: bool init false;");
		writer.println("global busy: bool init false;");
		writer.println("global ignore: bool init false;");
		
		/*
		 *  ENCODE the single module
		 
		writer.println("\nmodule mod\n");
		
		// encode the variables
		writer.println("\ts: [0.." + (4*ia.getGroups().size()-1) + "] init " + group2int.get(ia.getInit()) + ";\n\n");
		
		
		// start with the microcollections' inner states
		for (Group group: ia.getGroups()) {
			// get the possible end states!
			boolean[] ends = group.getHumanEndStates();
			Integer[] endInts = group2ends.get(group);
			
			writer.println("\n\t// inner " + group.getName());
			if (ends[0]) {
				writer.print("\t[] s=" + group2int.get(group) + " -> (s'=" + endInts[0] + ") ");
				writer.print(" & (inner'=false) & (outer'=true) & (ready'=true)");
				writer.println(";");
			}
			if (ends[1]) {
				writer.print("\t[] s=" + group2int.get(group) + " -> (s'=" + endInts[1] + ") ");
				writer.print(" & (inner'=false) & (outer'=true) & (busy'=true)");
				writer.println(";");
			}
			if (ends[2]) {
				writer.print("\t[] s=" + group2int.get(group) + " -> (s'=" + endInts[2] + ") ");
				writer.print(" & (inner'=false) & (outer'=true) & (ignore'=true)");
				writer.println(";");
			}
		}
		
		// now do the microinteractions' outer states
		for (GroupTransition macro : ia.getMacroTransitions()) {
			writer.println("\n\t// transition from " + macro.getSource().getName() + " to " + macro.getTarget().getName());
			boolean[] branch = macro.getHumanBranching();
			
			Group source = macro.getSource();
			Integer[] sourceEnds = group2ends.get(source);
			Group target = macro.getTarget();
			
			if (branch[0]) {
				writer.print("\t[] s=" + sourceEnds[0] + " -> (s'=" + group2int.get(target) + ") ");
				writer.print(" & (inner'=true) & (outer'=false) & (ready'=false) & (busy'=false) & (ignore'=false)");
				activateMicros(writer, target);
				writer.println(";");
			}
			if (branch[1]) {
				writer.print("\t[] s=" + sourceEnds[1] + " -> (s'=" + group2int.get(target) + ") ");
				writer.print(" & (inner'=true) & (outer'=false) & (ready'=false) & (busy'=false) & (ignore'=false)");
				activateMicros(writer, target);
				writer.println(";");
			}
			if (branch[2]) {
				writer.print("\t[] s=" + sourceEnds[2] + " -> (s'=" + group2int.get(target) + ") ");
				writer.print(" & (inner'=true) & (outer'=false) & (ready'=false) & (busy'=false) & (ignore'=false)");
				activateMicros(writer, target);
				writer.println(";");
			}
		}
		* END OLD ENCODING SCHEME
		*/
		
		
		/*
		 *  END ENCODING the single module
		 */
		writer.println("\nendmodule");
		
		/*
		 * encode labels
		 */
		// first do the microinteractions
		it = microNames.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			String m = (String) pair.getKey();
			ArrayList<Integer> micro2Idx = micro2idxs.get(m);
			if (!micro2Idx.isEmpty()) {
				writer.print("label \"" + m + "\" = ");
				for (int i = 0; i < micro2Idx.size() - 1; i++) 
					writer.print("s=" + micro2Idx.get(i) + " | ");
				writer.println("s=" + micro2Idx.get(micro2Idx.size()-1) + ";");
				microNames.put(m, true);
			}
		}
		

		it = microNames.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			if (!((boolean) pair.getValue()))
				writer.println("label \"" + pair.getKey() + "\" = false;");
		}
		
		// now encode the speech label
		writer.print("label \"speaks\" = ");
		boolean anySpeaks = false;
		it = group2idx.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			Group group = (Group) pair.getKey();
			if (group.getSpeaks()) {
				//System.out.println("group " + group.getName() + "speaks in " + ia.getName());
				if (anySpeaks)
					writer.print(" | ");
				anySpeaks = true;
				writer.print("s=" + pair.getValue());
			}
		}
		if (anySpeaks)
			writer.println(";");
		else {
			writer.println("false;");
		}
		
		// now encode the human speech label
		writer.print("label \"hspeaks\" = ");
		anySpeaks = false;
		it = group2idx.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			Group group = (Group) pair.getKey();
			if (group.getHSpeaks()) {
				//System.out.println("group " + group.getName() + "speaks in " + ia.getName());
				if (anySpeaks)
					writer.print(" | ");
				anySpeaks = true;
				writer.print("s=" + pair.getValue());
			}
		}
		if (anySpeaks)
			writer.println(";");
		else {
			writer.println("false;");
		}
		
		// now encode the robot speaking first label
		writer.print("label \"speaksFirst\" = ");
		anySpeaks = false;
		it = group2idx.entrySet().iterator();
		while (it.hasNext()) {
			System.out.println("finding one that speaks first");
			HashMap.Entry pair = (HashMap.Entry)it.next();
			Group group = (Group) pair.getKey();
			if (group.getSpeaksFirst()) {
				//System.out.println("group " + group.getName() + "speaks in " + ia.getName());
				if (anySpeaks)
					writer.print(" | ");
				anySpeaks = true;
				writer.print("s=" + pair.getValue());
			}
		}
		if (anySpeaks)
			writer.println(";");
		else {
			writer.println("false;");
		}
		
		// now encode the robot speaking first label
		writer.print("label \"hspeaksFirst\" = ");
		anySpeaks = false;
		it = group2idx.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			Group group = (Group) pair.getKey();
			if (group.getHSpeaksFirst()) {
				//System.out.println("group " + group.getName() + "speaks in " + ia.getName());
				if (anySpeaks)
					writer.print(" | ");
				anySpeaks = true;
				writer.print("s=" + pair.getValue());
			}
		}
		if (anySpeaks)
			writer.println(";");
		else {
			writer.println("false;");
		}
		
		// next do the state labels
		writer.println("label \"ready\" = hstate=0;");
		writer.println("label \"busy\" = hstate=1;");
		writer.println("label \"ignore\" = hstate=2;");
		
		writer.println("label \"ready_overall\" = hstate_overall=0;");
		writer.println("label \"busy_overall\" = hstate_overall=1;");
		writer.println("label \"ignore_overall\" = hstate_overall=2;");		
		
		writer.flush();
		writer.close();
		
		return pfile;
	}

	public HashMap<Integer, Group> getIdx2Group() {
		return idx2group;
	}
}
