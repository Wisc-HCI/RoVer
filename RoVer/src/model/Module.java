package model;
import java.util.ArrayList;

import enums.StateClass;

public class Module {

	private ArrayList<State> states;
	private ArrayList<Transition> transitions;
	private ArrayList<Variable> localvars;
	private ArrayList<State> inits;
	private ArrayList<State> enabledInits;
	private String name;
	private boolean built;
	private Microinteraction parent;
	
	// unused fields
	private ArrayList<State> committedStates;
	
	// for creating a new module
	public Module(Microinteraction parent){
		states = new ArrayList<State>();
		transitions = new ArrayList<Transition>();
		inits = new ArrayList<State>();
		enabledInits = new ArrayList<State>();
		localvars = new ArrayList<Variable>();
		this.name = "unnamed";
		this.parent = parent;
		this.built = false;
	}
	
	public Module(String name, Microinteraction parent){
		states = new ArrayList<State>();
		transitions = new ArrayList<Transition>();
		inits = new ArrayList<State>();
		enabledInits = new ArrayList<State>();
		localvars = new ArrayList<Variable>();
		this.name = name;
		this.parent = parent;
		this.built = false;
	}
	
	// for importing an existing module from XML
	public Module(String name, ArrayList<State> states, ArrayList<Integer> inits, ArrayList<Variable> localvars, Microinteraction parent) {
		this.states = new ArrayList<State>(states);
		this.transitions = new ArrayList<Transition>();
		this.localvars = new ArrayList<Variable>(localvars);
		this.inits = new ArrayList<State>();
		enabledInits = new ArrayList<State>();
		this.parent = parent;
		this.built = false;
		
		// set the initial states
		for (Integer init : inits) {
			for (State state : this.states) {
				if (state.getID() == init) {
					state.setToInit(true);     // set the state to init
					this.inits.add(state);     // add the state to the list of inits
					this.enabledInits.add(state); // everything starts off as enabled
				}
			}
		}
		
		this.name = name;
	}
	
	
	public void addState(State s){
		states.add(s);
	}
	
	public void addInit(State s){
		inits.add(s);
		enabledInits.add(s);
	}
	
	public void assignName(String name){
		this.name = name;
	}
	
	public void setParent(Microinteraction micro) {
		this.parent = micro;
	}
	
	public void addTransition(Transition t){
		transitions.add(t);
	}
	
	public void addTransitions(ArrayList<Transition> trans) {
		if (!transitions.isEmpty())
			return;
		transitions = new ArrayList<Transition>(trans);
	}
	
	public void addLocalVariable(Variable v) {
		localvars.add(v);
	}
	
	public void disableInits(boolean ready, boolean busy, boolean ignore) {
		enabledInits.clear();
		enabledInits.addAll(inits);
		ArrayList<State> toRemove = new ArrayList<State>();
		for (State st : enabledInits) {
			if (!ready && st.getStateClass().equals(StateClass.READY))
				toRemove.add(st);
			if (!busy && st.getStateClass().equals(StateClass.BUSY))
				toRemove.add(st);
			if (!ignore && st.getStateClass().equals(StateClass.IGNORE))
				toRemove.add(st);
		}
		
		//if (toRemove.size() == 0)
		//	System.out.println("No states are getting disabled");
		//else
		//	System.out.println("Some states are getting disabled");
		
		for (State st : toRemove) 
			enabledInits.remove(st);
	}
	
	public boolean isBuilt(){
		return built;
	}
	
	public ArrayList<State> getStates(){
		return states;
	}
	
	public State getState(String name) {
		State s = null;
		
		for (State st : states) {
			if (st.getName().equals(name)) {
				s = st;
				break;
			}
		}
		
		return s;
	}
	
	public ArrayList<Variable> getLocalVars() {
		return localvars;
	}
	
	public ArrayList<Transition> getTransitions(){
		return transitions;
	}
	
	public ArrayList<State> getInits(){
		return inits;
	}
	
	public ArrayList<State> getEnabledInits() {
		return enabledInits;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void build(){
		// add transitions
		for (Transition t : transitions){
			State source = t.getSource();
			source.addOutputTrans(t);
			State target = t.getTarget();
			target.addInputTrans(t);
		}
		
		built = true;
	}
	
	/*
	 * Call before writing to an XML file
	 */
	public void assignStateIDs() {
		int curr = 0;
		for (State state : states) {
			state.setID(curr);
			curr++;
		}
	}
	
	/*
	 * Find a variable based only on its name.
	 * A variable can be found in the module's
	 * local variables or the microinteraction's
	 * global variables.
	 */
	public Variable findVariable(String varname) {
		Variable retreived = null;
		boolean foundVar = false;
		for (Variable var : localvars) {
			if (var.getName().equals(varname)) {
				foundVar = true;
				retreived = var;
				break;
			}
		}
		if (!foundVar) 
			retreived = parent.findVariable(varname);
		
		return retreived;
	}
	
	public Module copy(Microinteraction parent) {
		Module mod = new Module(this.name, parent);

		// copy the local vars
		for (Variable loc : localvars) {
			// add a copy to the macrointeraction
			Variable temp = loc.copy();
			//temp.setName(temp.getName() + "_" + parent.getName());
			mod.addLocalVariable(temp);
		}
		
		// copy the states, but don't fill in the transitions
		for (State state : states) {
			State temp = state.copyWithoutTrans();
			System.out.println(temp.getStateClass());
			mod.addState(temp);
		}
		
		// copy the transitions, filling in the states
		for (Transition trans : transitions) {
			Transition temp = trans.copyWithoutStates(parent, mod);
			
			// fill in the states to the transition
			for (State state : mod.getStates()) {
				// search for source
				if (state.getName().equals(trans.getSource().getName())) {
					temp.setSource(state);
					state.addOutputTrans(temp);
				}
				
				// search for target
				if (state.getName().equals(trans.getTarget().getName())) {
					temp.setTarget(state);
					state.addInputTrans(temp);
				}
			}
			mod.addTransition(temp);
		}
		
		// designate the inits to the transitions
		for (State init : inits) {
			for (State state : mod.getStates()) {
				if (state.getName().equals(init.getName()))
					mod.addInit(state);
			}
		}
		
		// go through all variables and change the names
		for (Variable v : mod.getLocalVars())
			v.setName(v.getName() + "_" + parent.getName());
		
		return mod;
	}
	
	public String toString() {
		String str = "";
		
		// print name
		str += "Name: " + name + "\n";
		
		// print local variables
		str += "Local variables: \n";
		for (Variable var : localvars) 
			str += var.toString();
		
		// print states
		str += "States: \n";
		for (State st : states)
			str += st.toString();
		
		// print initial states
		str += "Initial states: \n";
		for (State st : inits)
			str += st.toString();
		
		// print transitions
		str += "Transitions: \n";
		for (Transition trans : transitions)
			str += trans.toString();
		
		return str;
	}
	/*
	private void mergeBranchPoint(){
		ArrayList<Location> statesToRemove = new ArrayList<Location>();
		ArrayList<Transition> transToRemove = new ArrayList<Transition>();
		for (State l : states){
			if (l.getType() == "BRANCHPOINT"){
				statesToRemove.add(l);
				
				for (Transition t : l.getInputTrans()){
					transToRemove.add(t);
				}
				for (Transition t : l.getOutputTrans()){
					transToRemove.add(t);
				}
				
				for (Transition input : l.getInputTrans()){
					for (Transition output : l.getOutputTrans()){
						Transition newTrans = input.copy();
						newTrans.setSource(output.getSource());
						newTrans.getSource().addInputTrans(newTrans);
						for (Update u : output.getUpdates()){
							newTrans.addUpdates(u);
						}
						transitions.add(newTrans);
						
					}
				}
				
				for (Transition output : l.getOutputTrans()){
					output.getTarget().getInputTrans().remove(output);
				}
			
				
			}
		}
		
		for (State l : statesToRemove){
			states.remove(l);
		}
		for (Transition t : transToRemove){
			transitions.remove(t);
		}
		
	}
	*/
	
}
