package model;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.SnapshotParameters;

import enums.StateClass;
import model_ctrl.Builder;
import model_ctrl.MicroParameterizer;
import model_ctrl.TooltipViz;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;

//TODO URGENT: Merge with MicroBox

public class Microinteraction {

	// stuff to do with self
	private String name;
	private String description;
	private ArrayList<Variable> globals;
	private ArrayList<Module> modules;
	private Microinteraction builtMicro;
	private MicroBox mb;    // microinteractions that are initialized should have a microbox -- aka its graphical form
	private HashMap<String,ArrayList<Variable>> orLabels;
	private HashMap<String,ArrayList<Variable>> andLabels;
	private MicroParameterizer params;
	
	// stuff to do with interaction
	private int id;
	
	// stuff to do with sequential checking
	ArrayList<ArrayList<State>> endStates;
	ArrayList<ArrayList<State>> startStates;
	
	// tooltip
	Canvas tooltipCanvas;
	Tooltip tt;
	private boolean staticTooltip;
	
	// stuff to do with concurrent microinteractions

	public Microinteraction() {
		initialize();
		id = -1;
	}
	
	public Microinteraction(int id) {
		initialize();
		this.id = id;
	}
	
	public void initialize() {
		name = "unnamed";
		description = "";
		globals = new ArrayList<Variable>();
		modules = new ArrayList<Module>();
		builtMicro = null;
		mb = null;   // no microbox initially
		orLabels = null;
		andLabels = null;
		params = null;
		
		startStates = null;
		endStates = null;
		
		// stuff to do with viz
		tooltipCanvas = new TooltipViz(450, 350, this);
		staticTooltip = false;
	}

	public void addGlobal(Variable var) {
		globals.add(var);
	}
	
	public void addGlobals(ArrayList<Variable> vars) {
		globals.addAll(vars);
	}

	public void addModule(Module m) {
		modules.add(m);
	}
	
	public void addDisjunctionFormula(HashMap<String,ArrayList<Variable>> labs) {
		this.orLabels = labs;
	}
	
	public void addModules(ArrayList<Module> mods) {
		modules.addAll(mods);
	}
	
	public void addParameterizer(MicroParameterizer params) {
		this.params = params;
	}
	
	public void addDescription(String descript) {
		description = descript;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public void linkMicroBox(MicroBox mb) {
		this.mb = mb;
		mb.setMicrointeraction(this);
	}
	
	public void setTooltip(Tooltip tt) {
		this.tt = tt;
	}
	
	public Tooltip getTooltip() {
		return tt;
	}
	
	public void setStaticTooltip(TooltipViz tool) {
		staticTooltip = true;
		
		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);         
		WritableImage image = tool.snapshot(params, null);
		this.tooltipCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
		
	}
	
	public void setIsStaticTooltip(boolean val) {
		staticTooltip = val;
	}
	
	public MicroBox getMicroBox() {
		return mb;
	}
	
	public MicroParameterizer getParameterizer() {
		return params;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ArrayList<Variable> getGlobalVars() {
		return globals;
	}
	
	public ArrayList<Module> getModules() {
		return modules;
	}
	
	public HashMap<String,ArrayList<Variable>> getOrLabels() {
		return orLabels;
	}
	
	public void setStartStates(ArrayList<ArrayList<State>> s) {
		startStates = s;
	}
	
	public void setEndStates(ArrayList<ArrayList<State>> e, boolean isNonAssisted) {
		endStates = e;
		
		/*
		 * whenever the end states are set, re-draw the tooltip canvas!
		 */
		if (!staticTooltip)
			((TooltipViz) tooltipCanvas).draw(isNonAssisted);
		
	}
	
	public Canvas getTooltipViz() {
		return tooltipCanvas;
	}
	
	public ArrayList<ArrayList<State>> getStartStates() {
		return startStates;
	}
	
	public ArrayList<ArrayList<State>> getEndStates() {
		return endStates;
	}
	
	public Module getModuleContainingString(String str) {
		return getModuleHelper(str, "contains");
	}
	
	public Module getModule(String str) {
		return getModuleHelper(str, "equals");
	}
	
	public int getNumEnabledHumanInits() {
		int numInits = 0;
		for (Module mod : modules) 
			if (mod.getName().equals("Human"))
				numInits += mod.getEnabledInits().size();
		return numInits;
	}
	
	public boolean[] getHumanStartStates() {
		boolean[] starters = {false, false, false};
		
		for (Module mod : modules) 
			if (mod.getName().equals("Human")) {
				for (State st : mod.getInits()) {
					if (st.getStateClass().equals(StateClass.READY))
						starters[0] = true;
					if (st.getStateClass().equals(StateClass.BUSY))
						starters[1] = true;
					if (st.getStateClass().equals(StateClass.IGNORE))
						starters[2] = true;
				}
			}
		
		return starters;
	}
	
	private Module getModuleHelper(String name, String flag) {
		Module m = null;
		
		for (Module mod : modules) {
			if ((mod.getName().equals(name) && flag.equals("equals")) || (mod.getName().contains(name) && flag.equals("contains"))) {
				m = mod;
				break;
			}
		}
		
		return m;
	}
	
	public int getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void build() {
		Builder bob = new Builder(this);
		builtMicro = bob.build();
		
	}
	
	public void setBuiltMicro(Microinteraction otherMic) {
		builtMicro = otherMic;
	}
	
	public Microinteraction getBuiltMicro() {
		if (builtMicro == null) {
			System.out.println("Error: microinteraction has not been built");
			return null;
		}
		return builtMicro;
	}
	
	public Variable findVariable(String varname) {
		Variable retreived = null;

		for (Variable var : globals) {
			if (var.getName().equals(varname)) {
				retreived = var;
				break;
			}
		}
		
		return retreived;
	}
	
	/*
	 * copy
	 */
	
	public Microinteraction copy() {
		// init new micro and set ID
		Microinteraction microCopy = new Microinteraction(this.id);
		
		// deal with name and description
		microCopy.setName(name);
		microCopy.addDescription(description);
		MicroBox mb = new MicroBox(new File("none.None"), "none", Color.BLACK);
		microCopy.linkMicroBox(mb);
		
		// deal with this stuff but DON'T WORRY ABOUT OR/AND LABELS
		/*
		globals = new ArrayList<Variable>();
		modules = new ArrayList<Module>();
		builtMicro = null;
		mb = null;   // no microbox initially
		orLabels = null;
		andLabels = null;
		params = null;
		*/
		// globals
		for (Variable glob : globals) {
			microCopy.getGlobalVars().add(glob.copy());
		}
		// modules
		for (Module mod : modules) {
			Module newMod = mod.copy(microCopy);
			microCopy.getModules().add(newMod);
		}
		// mb? probably don't need
		// params? probably don't need

		// built?
		microCopy.build();
		
		return microCopy;
	}
	
	/*
	 * debugging methods
	 */
	public String startStatesToString() {
		String str = "";
		
		for (ArrayList<State> states : startStates) {
			for (State state : states) {
				
				System.out.print("-" + state.getName() + " " + state.getStateClass() + "-");
				
			}
			System.out.println();
		}
		
		return str;
	}
	
	public String endStatesToString() {
		String str = "";
		
		for (ArrayList<State> states : endStates) {
			for (State state : states) {
				
				System.out.print("-" + state.getName() + " " + state.getStateClass() + "-");
				
			}
			System.out.println();
		}
		
		return str;
	}

	public String toString() {
		String str = "Microinteraction: " + this.name + ", ID=" + this.id + "\n";
				
		// print the global variables
		str += "Global Variables: \n";
		for (int i = 0; i < globals.size(); i++) {
			str += globals.get(i).toString();
		}

		// print the modules
		str += "modules: \n";
		for (Module mod : modules) {
			str += mod.toString();
		}
		
		return str;
	}
}