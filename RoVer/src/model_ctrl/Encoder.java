package model_ctrl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import model.Expression;
import model.Guard;
import model.Microinteraction;
import model.Module;
import model.State;
import model.Sync;
import model.Transition;
import model.Update;
import model.Variable;

public class Encoder {
	
	private final static char[] stateChars = {'m', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	
	public static void encodeGlobalVars(Microinteraction micro, PrintWriter writer) {
		encodeGlobs(micro, writer, "");
	}
	
	public static void encodeGlobalVars(Microinteraction micro, PrintWriter writer, String suf) {
		encodeGlobs(micro, writer, suf);
	}
	
	private static void encodeGlobs(Microinteraction micro, PrintWriter writer, String suf) {
		ArrayList<Variable> globals = micro.getGlobalVars();
		for (Variable glob : globals) {
			if (!glob.isSync() && !glob.getPrismIgnore()) {
				String type = glob.getType();
				if (type.equals("int")) {
					String bounds = glob.getBound();
					//System.out.println(bounds);
					int lb = Integer.parseInt(bounds.substring(0, bounds.indexOf(".")));
					int ub = Integer.parseInt(bounds.substring(bounds.indexOf(".")+2, bounds.length()));
					writer.println("global " + appendSuf(glob.getName(),suf) + ": " + "[" + lb + ".." + ub + "]" + ";");
				}
				else 
					writer.println("global " + appendSuf(glob.getName(),suf) + ": " + glob.getType() + ";");
			}
		}
		writer.println();
	}
	
	public static void encodeInits(Microinteraction micro, PrintWriter writer, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds, boolean isLast) {
		encodeInit(micro, writer, "", ModuleStateStrs, ModelIds, isLast);
	}
	
	public static void encodeInits(Microinteraction micro, PrintWriter writer, String suf, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds, boolean isLast) {
		encodeInit(micro, writer, suf, ModuleStateStrs, ModelIds, isLast);
	}
	
	private static void encodeInit(Microinteraction micro, PrintWriter writer, String suf, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds, boolean isLast) {
		// encode initial states
		// for now, variables (global and local) cannot have multiple initial states (why? I forgot). This will likely change.
		for (Variable g : micro.getGlobalVars()) {
			if (!g.isSync() && !g.getPrismIgnore())
				writer.println("\t" + appendSuf(g.getName(),suf) + "=" + g.getValue() + " &");
		}
		for (int i = 0; i < micro.getModules().size(); i++) {
			Module mod = micro.getModules().get(i);
			// we get enabled inits here!
			ArrayList<State> inits = mod.getEnabledInits();
						
			writer.print("\t");
			if (inits.size() == 1) {
				State init = inits.get(0);
				writer.print(ModuleStateStrs.get(mod) + "=" + ModelIds.get(init));
			}
			else {
				for (int j = 0; j < inits.size(); j++) {
					State init = inits.get(j);
					if (j == 0)
						writer.print("(");
					if (j > 0)
						writer.print(" | ");
					writer.print(ModuleStateStrs.get(mod) + "=" + ModelIds.get(init));
									
					if (j == inits.size()-1)
						writer.print(")");
								
				}
			}
			if ((i < micro.getModules().size() - 1 || !isLast) && inits.size() > 0)
				writer.println(" &");
			else
				writer.println();
			
		}
		
		
	}
	
	public static void encodeModules(Microinteraction micro, PrintWriter writer, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds) {
		encodeMods(micro, writer, "", ModuleStateStrs, ModelIds);
	}
	
	public static void encodeModules(Microinteraction micro, PrintWriter writer, String suf, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds) {
		encodeMods(micro, writer, suf, ModuleStateStrs, ModelIds);
	}
	
	private static void encodeMods(Microinteraction micro, PrintWriter writer, String suf, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds) {
		
		for (Module mod : micro.getModules()) {
			writer.println("module " + appendSuf(mod.getName(),suf) + "\n");
			
			// encode the local variables, beginning with assigning states ID's
			int numStates = mod.getStates().size();
			if (numStates == 0) numStates = 2;
			String stateStr = ModuleStateStrs.get(mod);
			writer.println("\t" + stateStr + ": [0.." + (numStates-1) + "];");				
		
			// encode the transitions
			for (Transition trans : mod.getTransitions()) {
				State source = trans.getSource();
				State target = trans.getTarget();
				ArrayList<Sync> syncs = trans.getSyncs();
				ArrayList<Guard> guards = trans.getGuards();
				ArrayList<Update> updates = trans.getUpdates();
				
				// get the sync(s)
				// for now, assume that there is only one
				writer.print("\t[");
				if (syncs.size() > 0)
					writer.print(appendSuf(syncs.get(0).getVar().getName(),suf));
				writer.print("] ");
				
				// encode the source
				writer.print(ModuleStateStrs.get(mod) + "=" + ModelIds.get(source));
				
				// encode any guards
				for (Guard g : guards) {
					if (g.getVar() == null)   // it's a label
						writer.print(" & " + g.getLabel() + g.getSign() + g.getVal());
					else if (isGlobal(g.getVar(), micro))
						writer.print(" & " + appendSuf(g.getVar().getName(),suf) + g.getSign() + g.getVal());
					else
						writer.print(" & " + g.getVar().getName() + g.getSign() + g.getVal());
				}
				
				// arrow
				writer.print(" -> ");
				
				// encode the target
				writer.print("(" + ModuleStateStrs.get(mod) + "'=" + ModelIds.get(target) + ")");
				
				// encode any updates
				for (Update u : updates) {
					String uVal = getUVal(u, micro, suf);
					if (isGlobal(u.getVar(), micro)) {
						writer.print(" & (" + appendSuf(u.getVar().getName(),suf) + "'=" + uVal + ")"); 
					}
					else {
						writer.print(" & (" + u.getVar().getName() + "'=" + uVal + ")");
					}
				}
				
				// done
				writer.println(";");
			}
			
			writer.println("\nendmodule\n");
		}
	}
	
	private static String getUVal(Update u, Microinteraction micro, String suf) {
		String uVal = "";
		if (u.getVal() == null) {
			Expression e = u.getAssgExpression();
			Variable uv = e.getVar();
			if (isGlobal(uv, micro)) 
				uVal += appendSuf(uv.getName(), suf);
			else
				uVal += uv.getName();
			uVal += e.getSign() + e.getUpdate();
		}
		else
			uVal = u.getVal();
		
		return uVal;
	}
	
	public static void encodeLabels(Microinteraction micro, PrintWriter writer, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds) {
		encodeLabs(micro, writer, "", ModuleStateStrs, ModelIds);
	}
	
	public static void encodeLabels(Microinteraction micro, PrintWriter writer, String suf, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds) {
		encodeLabs(micro, writer, suf, ModuleStateStrs, ModelIds);
	}
	
	private static void encodeLabs(Microinteraction micro, PrintWriter writer, String suf, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds) {
		for (Module mod : micro.getModules()) {
			
			String stateStr;
			String stateLab;
			for (State st : mod.getStates()) {
				
				stateStr = ModuleStateStrs.get(mod) + "=" + ModelIds.get(st).toString();
				stateLab = appendSuf(st.getName(), suf);
				writer.println("label \"" + stateLab + "\" = " + stateStr + ";");
				
			}
		}
	}
	
	public static void encodeDisjunctiveFormulas(Microinteraction micro, PrintWriter writer) {
		HashMap<String,ArrayList<Variable>> orLabels = micro.getOrLabels();
		
		if (orLabels != null) {
			Iterator it = orLabels.entrySet().iterator();
			while (it.hasNext()) {
			    HashMap.Entry pair = (HashMap.Entry)it.next();
			    writer.print("formula " + pair.getKey() + " = ");
			    ArrayList<Variable> vars = (ArrayList<Variable>) pair.getValue();
			    for (int i = 0; i < vars.size(); i++) {
			    	writer.print(vars.get(i).getName());
			    	if (i < vars.size()-1)
			    		writer.print(" | ");
			    	else
			    		writer.println(";");
			    }
			}
		}
	}
		
	public static HashMap<State,Integer> populateModelIds(Microinteraction micro) {
		HashMap<State,Integer> ModelIDs = new HashMap<State,Integer>();
		for (Module mod : micro.getModules()) {
			
			int counter = 0;
			//System.out.println(micro);
			for (State state : mod.getStates()) {
				ModelIDs.put(state,counter);
				counter++;
			}
		}
		
		return ModelIDs;
	}
	
	public static HashMap<Module,String> populateModuleStateStrs(Microinteraction micro) {
		return populateModuleStateStrs(micro, "");
	}
	
	public static HashMap<Module,String> populateModuleStateStrs(Microinteraction micro, String suf) {
		return populateModuleStateStr(micro, suf);
	}
	
	private static HashMap<Module,String> populateModuleStateStr(Microinteraction micro, String suf) {
		HashMap<Module,String> ModuleStateStrs = new HashMap<Module,String>();
		int counter = 0;
		for (Module mod : micro.getModules()) {
			ModuleStateStrs.put(mod, appendSuf("st",suf) + "_" + stateChars[counter]);
			counter++;
		}
		return ModuleStateStrs;
	}
	
	public static HashMap<State,ModuleStatePair> fillModuleStatePairs(Microinteraction micro, HashMap<Module,String> ModuleStateStrs, HashMap<State,Integer> ModelIds) {
		HashMap<State,ModuleStatePair> smsp = new HashMap<State,ModuleStatePair>();

		for (Module mod : micro.getModules()) {
			String modString = ModuleStateStrs.get(mod);
			
			for (State state : mod.getStates()) {
				int id = ModelIds.get(state);
				
				smsp.put(state, new ModuleStatePair(modString, id));
			}
		}
		
		return smsp;
	}
	
	/*
	 * Helper method for appending microinteraction name to a variable
	 */
	public static String appendSuf(String str, String suf) {
		if (suf == null || suf.equals("") )
			return str;
		else
			return str + "_" + suf;
	}
	
	private static boolean isGlobal(Variable g, Microinteraction micro) {
		boolean gIsGlobal = false;
		for (Variable v : micro.getGlobalVars()) {
			if (g.equals(v))
				gIsGlobal = true;
		}
		
		return gIsGlobal;
	}

}
