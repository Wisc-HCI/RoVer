package model_ctrl;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import checkers.ModBehPair;
import model.*;
import model.GroupTransition;

public class Exporter {
	
	private PrintWriter writer;
	private Interaction ia;
	private HashMap<Group,Integer> group2id;
	private String pid;
	
	public Exporter(Interaction ia, String pid) {
		this.ia = ia;
		this.pid = pid;
		group2id = new HashMap<Group,Integer>();
	}
	
	public String export() {
		return export(null);
	}
	
	public String export(String folder) {
		populateGroup2ID();
		String name = beginEncoding(folder);
		//if (ia.getTutorial() || !ia.getCurrDesign().equals("Instruction-Action")) {
			encodeGroups();
			encodeTransitions();
		//}
		//else {
		//	encodeGreeter();
		//	encodeFarewell();
		//	encodeIAGroups(0, "Instruction 1");
		//	encodeIAGroups(1, "Instruction 2");
		//	encodeIAGroups(2, "Instruction 3");
		//	encodeIATransitions(0);
		//	encodeIATransitions(1);
		//	encodeIATransitions(2);
		//	encodeIAExtraTransitions();
		//}
		endEncoding();
		return name;
	}
	
	private void populateGroup2ID() {
		int counter = 0;
		for (Group group : ia.getGroups()) {
			group2id.put(group, counter);
			counter++;
		}
	}
	
	public String beginEncoding(String folder) {
		String name = "";
		if (folder != null)
			name += folder;
		
		name += pid;
		
		if (ia.getTutorial())
			name += "_tutorial";
		else
			name += "_" + ia.getCurrDesign();
		try {
			writer = new PrintWriter(name + ".xml", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// begin writing
		writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		writer.println("<nta>");
		writer.println("\t<name>" + name + "</name>");
		return name;
	}
	
	public void endEncoding() {
		if (ia.getTutorial())
			writer.println("\t<design>Delivery</design>");
		else
			writer.println("\t<design>copy</design>");
			//writer.println("\t<design>" + ia.getCurrDesign() + ((ia.getCurrDesign().contains("Instruction"))?("-" + ia.getCurrInstruction()):"") + "</design>");
		writer.println("</nta>");
		writer.close();
		//InfoPanel in = new InfoPanel("Design successfully saved!");
		//in.display();
	}
	
	public void encodeGreeter() {
		writer.println("\n\t<group id=\"" + (3*ia.getGroups().size()) + "\" init=\"true\" x=\"0\" y=\"0\">");
		writer.println("\t\t<name>start</name>");
		writer.println("\t\t<micro>");
		writer.println("\t\t\t<name>Greeter</name>");
		writer.println("\t\t</micro>");
		writer.println("\t</group>");
	}
	
	public void encodeFarewell() {
		writer.println("\n\t<group id=\"" + (3*(ia.getGroups().size())+1) + "\" init=\"false\" x=\"0\" y=\"0\">");
		writer.println("\t\t<name>end</name>");
		writer.println("\t\t<micro>");
		writer.println("\t\t\t<name>Farewell</name>");
		writer.println("\t\t</micro>");
		writer.println("\t</group>");
	}
	
	public void encodeGroups() {
		
		for (Group group : ia.getGroups()) {
			
			// identifier
			writer.print("\n\t<group ");
			writer.println("id=\"" + group2id.get(group) + "\" init=\"" + group.isInit() + "\" x=\"" + group.getLayoutX() + "\" y=\"" + group.getLayoutY() +  "\">");
			
			// name
			writer.println("\t\t<name>" + group.getName() + "</name>");
			
			// get the protocols
			for (ModBehPair mbp : group.getFixedBehaviorConflicts()) {
				writer.println("\t\t<protocol>");
				for (int i = 0; i < mbp.size(); i++) {
					writer.println("\t\t\t<pair micro=\"" + mbp.getMod(i).getName() + "\" beh=\"" + mbp.getBeh(i) + "\"></pair>");
				}
				String[] fixPair = mbp.getFixPair();
				writer.println("\t\t\t<fix micro=\"" + fixPair[0] + "\" beh=\"" + fixPair[1] + "\"></fix>");
				writer.println("\t\t</protocol>");
			}
			
			// microinteractions
			for (Microinteraction m : group.getMicrointeractions()) {
				writer.println("\t\t<micro>");
				
				// name
				writer.println("\t\t\t<name>" + m.getName() + "</name>");
				
				// parameters
				for (Variable glob : m.getGlobalVars()) {
					if (glob.isParameterizable()) {
						if (glob.getType().equals("array")) {
							writer.println("\t\t\t<parameter type=\"array\">");
							writer.println("\t\t\t\t<name>" + glob.getName() + "</name>");
							
							for (int i = 0; i < glob.getArrayVals().size(); i++) {
								String str = glob.getArrayVals().get(i);
								str = str.replace("\"", "");
								String link = glob.getArrayValLinks().get(i);
								if (link.equals("ready"))
									writer.println("\t\t\t\t<item type=\"string\" val=\"" + str + "\" link=\"human_ready\"/>");	
								else if (link.equals("suspended"))
									writer.println("\t\t\t\t<item type=\"string\" val=\"" + str + "\" link=\"human_ignore\"/>");	
								else
									writer.println("\t\t\t\t<item type=\"string\" val=\"" + str + "\" link=\"\"/>");
							}
							
							writer.println("\t\t\t</parameter>");
						}
						else {
							if (glob.getName().equals("Instruction")) {
								String str;
								//if (glob.getValue().equals("Instruction 1")) {
								//	str = "First instruction. Pick up a piece of bread and place it on the plate";
								//}
								//else if (glob.getValue().equals("Instruction 2")) {
								//	str = "Second instruction. Pick up the slices of ham and cheese, and place the ham on top of the bread, and the cheese on top of the ham";
								//}
								//else if (glob.getValue().equals("Instruction 3")){
								//	str = "Finally, the third instruction. Add the lettuce and tomato in any order, and place the other piece of bread on top to complete the sandwich";
								//}
								//else {
								//	str = glob.getValue();
								//}
								str = glob.getValue();
								str = str.replaceAll("\"", "");
								
								writer.println("\t\t\t<parameter type=\"str\" val=\"" + str + "\">" + glob.getName() + "</parameter>");
							}
							else {
								String str = glob.getValue();
								str = str.replaceAll("\"", "");
								writer.println("\t\t\t<parameter type=\"" + glob.getType() + "\" val=\"" + str + "\">" + glob.getName() + "</parameter>");
							}
						}
					}
				}
				
				writer.println("\t\t</micro>");
			}
			
			// end identifier
			writer.println("\t</group>");
		}
	}
	
	public void encodeIAGroups(int inst, String instruction) {
		
		for (Group group : ia.getGroups()) {
			if (group.getName().equals("BeginProcedure") || group.getName().equals("EndProcedure")) {
				writer.println("\n\t<group id=\"" + (group2id.get(group)+ inst*ia.getGroups().size()) + "\" init=\"false\" x=\"0\" y=\"0\">");
				writer.println("\t\t<name>dummy</name>");
				writer.println("\t\t<micro>");
				writer.println("\t\t\t<name>Wait</name>");
				writer.println("\t\t\t<parameter type=\"int\" val=\"0\">wait time (seconds)</parameter>");
				writer.println("\t\t\t<parameter type=\"bool\" val=\"true\">allow_speech</parameter>");
				writer.println("\t\t</micro>");
				writer.println("\t</group>");
				continue;
			}
			
			// identifier
			writer.print("\n\t<group ");
			writer.println("id=\"" + (group2id.get(group) + inst*ia.getGroups().size()) + "\" init=\"false\" x=\"" + group.getLayoutX() + "\" y=\"" + group.getLayoutY() +  "\">");
			
			// name
			writer.println("\t\t<name>" + group.getName() + "</name>");
			
			// get the protocols
			for (ModBehPair mbp : group.getFixedBehaviorConflicts()) {
				writer.println("\t\t<protocol>");
				for (int i = 0; i < mbp.size(); i++) {
					writer.println("\t\t\t<pair micro=\"" + mbp.getMod(i).getName() + "\" beh=\"" + mbp.getBeh(i) + "\"></pair>");
				}
				String[] fixPair = mbp.getFixPair();
				writer.println("\t\t\t<fix micro=\"" + fixPair[0] + "\" beh=\"" + fixPair[1] + "\"></fix>");
				writer.println("\t\t</protocol>");
			}
			
			// microinteractions
			for (Microinteraction m : group.getMicrointeractions()) {
				writer.println("\t\t<micro>");
				
				// name
				writer.println("\t\t\t<name>" + m.getName() + "</name>");
				
				// parameters
				for (Variable glob : m.getGlobalVars()) {
					if (glob.isParameterizable()) {
						if (glob.getType().equals("array")) {
							writer.println("\t\t\t<parameter type=\"array\">");
							writer.println("\t\t\t\t<name>" + glob.getName() + "</name>");
							
							for (int i = 0; i < glob.getArrayVals().size(); i++) {
								String str = glob.getArrayVals().get(i);
								String link = glob.getArrayValLinks().get(i);
								if (link.equals("ready"))
									writer.println("\t\t\t\t<item type=\"string\" val=\"" + str + "\" link=\"human_ready\"/>");	
								else if (link.equals("suspended"))
									writer.println("\t\t\t\t<item type=\"string\" val=\"" + str + "\" link=\"human_ignore\"/>");	
								else
									writer.println("\t\t\t\t<item type=\"string\" val=\"" + str + "\" link=\"\"/>");
							}
							writer.println("\t\t\t</parameter>");
						}
						else {
							if (glob.getName().equals("Instruction")) {
								String str;
								if (instruction.equals("Instruction 1")) {
									str = "First instruction. Pick up a piece of bread and place it on the plate";
								}
								else if (instruction.equals("Instruction 2")) {
									str = "Second instruction. Pick up the slices of ham and cheese, and place the ham on top of the bread, and the cheese on top of the ham";
								}
								else {
									str = "Finally, the third instruction. Add the lettuce and tomato in any order, and place the other piece of bread on top to complete the sandwich";
								}

								
								writer.println("\t\t\t<parameter type=\"str\" val=\"" + str + "\">" + glob.getName() + "</parameter>");
							}
							else 
								writer.println("\t\t\t<parameter type=\"" + glob.getType() + "\" val=\"" + glob.getValue() + "\">" + glob.getName() + "</parameter>");
						}
					}
				}
				
				writer.println("\t\t</micro>");
			}
			
			// end identifier
			writer.println("\t</group>");
		}
	}
	
	public void encodeTransitions() {
		for (GroupTransition mtrans : ia.getMacroTransitions()) {
			writer.println("\n\t<transition>");
			
			// source and target
			writer.println("\t\t<source ref=\"" + group2id.get(mtrans.getSource()) + "\"/>");
			writer.println("\t\t<target ref=\"" + group2id.get(mtrans.getTarget()) + "\"/>");
			
			// guards
			boolean[] bb = mtrans.getBreakdownBranching();
			boolean[] hb = mtrans.getHumanBranching();
			if (hb[0])
				writer.println("\t\t<guard condition=\"human_ready\"/>");
			if (hb[1])
				writer.println("\t\t<guard condition=\"human_busy\"/>");
			if (hb[2])
				writer.println("\t\t<guard condition=\"human_ignore\"/>");
			//if (mtrans.getElse())
			//	writer.println("\t\t<guard condition=\"else\"/>");
			
			writer.println("\t</transition>");
		}
	}
	
	public void encodeIATransitions(int inst) {
		for (GroupTransition mtrans : ia.getMacroTransitions()) {
			writer.println("\n\t<transition>");
			
			// source and target
			writer.println("\t\t<source ref=\"" + (group2id.get(mtrans.getSource()) + inst*ia.getGroups().size()) + "\"/>");
			writer.println("\t\t<target ref=\"" + (group2id.get(mtrans.getTarget()) + inst*ia.getGroups().size()) + "\"/>");
			
			// guards
			boolean[] bb = mtrans.getBreakdownBranching();
			boolean[] hb = mtrans.getHumanBranching();
			if (hb[0])
				writer.println("\t\t<guard condition=\"human_ready\"/>");
			if (hb[1])
				writer.println("\t\t<guard condition=\"human_busy\"/>");
			if (hb[2])
				writer.println("\t\t<guard condition=\"human_ignore\"/>");
			//if (mtrans.getElse())
			//	writer.println("\t\t<guard condition=\"else\"/>");
			
			writer.println("\t</transition>");
		}
	}
	
	public void encodeIAExtraTransitions() {
		Group beginProc = ia.getGroup("BeginProcedure");
		Group endProc = ia.getGroup("EndProcedure");
		
		// greeter to the first instruction
		writer.println("\n\t<transition>");
		writer.println("\t\t<source ref=\"" + (3*ia.getGroups().size()) + "\"/>");
		writer.println("\t\t<target ref=\"" + group2id.get(beginProc) + "\"/>");
		writer.println("\t\t<guard condition=\"human_ready\"/>");
		writer.println("\t\t<guard condition=\"human_busy\"/>");
		writer.println("\t\t<guard condition=\"human_ignore\"/>");
		writer.println("\t</transition>\n");
		
		// link the first instruction to the next
		writer.println("\n\t<transition>");
		writer.println("\t\t<source ref=\"" + group2id.get(endProc) + "\"/>");
		writer.println("\t\t<target ref=\"" + (group2id.get(beginProc) + ia.getGroups().size()) + "\"/>");
		writer.println("\t\t<guard condition=\"human_ready\"/>");
		writer.println("\t\t<guard condition=\"human_busy\"/>");
		writer.println("\t\t<guard condition=\"human_ignore\"/>");
		writer.println("\t</transition>");
		
		// link the second instruction to the third
		writer.println("\n\t<transition>");
		writer.println("\t\t<source ref=\"" + (group2id.get(endProc) + ia.getGroups().size()) + "\"/>");
		writer.println("\t\t<target ref=\"" + (group2id.get(beginProc) + 2*ia.getGroups().size()) + "\"/>");
		writer.println("\t\t<guard condition=\"human_ready\"/>");
		writer.println("\t\t<guard condition=\"human_busy\"/>");
		writer.println("\t\t<guard condition=\"human_ignore\"/>");
		writer.println("\t</transition>");
		
		// link the end of the third instruction to the farewell
		writer.println("\n\t<transition>");
		writer.println("\t\t<source ref=\"" + (group2id.get(endProc) + 2*ia.getGroups().size()) + "\"/>");
		writer.println("\t\t<target ref=\"" + (3*(ia.getGroups().size())+1) + "\"/>");
		writer.println("\t\t<guard condition=\"human_ready\"/>");
		writer.println("\t\t<guard condition=\"human_busy\"/>");
		writer.println("\t\t<guard condition=\"human_ignore\"/>");
		writer.println("\t</transition>");
	}

}
