package model_ctrl;
import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import model.*;
import model.Group;
import study.BugTracker;

import java.awt.Point;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import controller.Annotation;
import controller.MainController;
import enums.StateClass;
import enums.String2StateClass;

/*
* Decodes xml file into states and transitions
*/
public class Decoder {
	
	private MainController mc;
	private Boolean isNonAssisted;

	public Decoder(Boolean isNonAssisted) {
		this.mc = null;
		this.isNonAssisted = isNonAssisted;
	}
	
	public Decoder(MainController mc, Boolean isNonAssisted) {
		this.mc = mc;
		this.isNonAssisted = isNonAssisted;
	}
	
	public void readMicrointeraction(File file, String filename, Microinteraction micro, MicroBox mb) {
		readMicrointeractionHelper(file, filename, micro, mb);
	}
	
	public void readMicrointeraction(File file, String filename, Microinteraction micro) {
		readMicrointeractionHelper(file, filename, micro, null);
	}
	
	private void readMicrointeractionHelper(File file, String filename, Microinteraction micro, MicroBox mb) {
			Document doc = initDoc(filename);

			// get name
			micro.setName(getDocName(doc));
			
			// get description
			micro.addDescription(getDocDescription(doc));
			
			// link an mb
			if (mb == null)
				micro.linkMicroBox(new MicroBox(file, "end", Color.WHITE));
			else
				micro.linkMicroBox(mb);

			// get globals
			ArrayList<Variable> globals = getDocGlobals(doc);
			for (Variable g : globals)
				micro.addGlobal(g);
			
			//micro.addParameterizer(new MicroParameterizer(globals, mc));
			
			// get modules
			NodeList modules = doc.getElementsByTagName("module");
			for (int i = 0; i < modules.getLength(); i++) {
				String namestr = "";                                   // name of the module
				ArrayList<State> statesArray = new ArrayList<State>();            
				ArrayList<Transition> transitionsArray = new ArrayList<Transition>();
				ArrayList<Variable> localvars = new ArrayList<Variable>();
				ArrayList<Integer> initStates = new ArrayList<Integer>();
				
				Element m = (Element) modules.item(i);
				
				NodeList names = m.getElementsByTagName("name");
				Element name = (Element) names.item(0);
				namestr = name.getTextContent();
				
				// are we dealing with the robot or human?
				NodeList agents = m.getElementsByTagName("agent");
				Element agent = (Element) agents.item(0);
				String agentstr = agent.getTextContent();
				
				// get the states
				NodeList states = m.getElementsByTagName("state");
				for (int j = 0; j < states.getLength(); j++) {
					Element s = (Element) states.item(j);
					int ID = Integer.parseInt(s.getAttribute("id"));
					int X = Integer.parseInt(s.getAttribute("x"));
					int Y = Integer.parseInt(s.getAttribute("y"));
					
					// get name
					Element stateName = (Element) s.getElementsByTagName("name").item(0);
					String stateNameStr = stateName.getTextContent();
					
					// get type if not the human or robot control processes
					StateClass stateClassValue = null;
					Element stateClass = (Element) s.getElementsByTagName("type").item(0);
					String stateClassStr = stateClass.getTextContent();
					//System.out.println("STATE CLASS STR: " + stateClassStr);
					StateClass sc = String2StateClass.findStateClass(stateClassStr);
					//sc.changeAgent(agentstr);
					//System.out.println(agentstr);
					
					// get the gesture and gaze
					NodeList gest = s.getElementsByTagName("gesture");
					Element gestureClass = null;
					String gestureClassStr = "NONE";
					if (gest.getLength() > 0) {
						gestureClass = (Element) gest.item(0);
						gestureClassStr = gestureClass.getTextContent();
					}
					NodeList gaze = s.getElementsByTagName("gaze");
					Element gazeClass = null;
					String gazeClassStr = "NONE";
					if (gaze.getLength() > 0) {
						gazeClass = (Element) gaze.item(0);
						gazeClassStr = gazeClass.getTextContent();
					}
					
					State temp = new State(stateNameStr, X, Y, Color.BLUE, false, false, false, "state", sc, ID);
					temp.setStateClass(stateClassValue);
					//temp.setAgent(agentstr);
					State st = new State(stateNameStr, X, Y, Color.BLUE, false, false, false, "state", sc, ID);
					st.setAgent(agentstr);
					statesArray.add(st);
					st.setGaze(gazeClassStr);
					st.setGesture(gestureClassStr);
					
					// get whether it is a breakdown state or not
					NodeList breakdowns = s.getElementsByTagName("breakdown");
					if (!(breakdowns.getLength() == 0)) {
						//System.out.println("BREAKDOWN STATE");
						st.setIsBreakdown(true);
					}
				}
				
				// check to make sure that none of the states share an ID
				ArrayList<Integer> ids = new ArrayList<Integer>();
				for (State state : statesArray) {
					if (ids.contains(state.getID())) {
						System.out.println("Error: states in XML file contain duplicate IDs");
					}
					else {
						ids.add(state.getID());
					}
				}
				
				// get the initial states
				NodeList inits = m.getElementsByTagName("init");
				for (int j = 0; j < inits.getLength(); j++) {
					Element init = (Element) inits.item(j);
					int initID = Integer.parseInt(init.getAttribute("ref"));
					initStates.add(initID);
				}
				
				// get the variables
				Element locals = (Element) m.getElementsByTagName("locals").item(0);
				NodeList vars = locals.getElementsByTagName("var");
				for (int j = 0; j < vars.getLength(); j++) {
					Element v = (Element) vars.item(j);
					Variable l = new Variable(v.getAttribute("type"), v.getAttribute("name"));
					l.initialize(v.getAttribute("init"));
					localvars.add(l);
				}
				
				// create a module
				Module mod = new Module(namestr, statesArray, initStates, localvars, micro);
				
				// once we have a module, add the transitions
				NodeList trans = m.getElementsByTagName("transition");
				for (int j = 0; j < trans.getLength(); j++) {
					Element t = (Element) trans.item(j);
					Element source = (Element) t.getElementsByTagName("source").item(0);
					Element target = (Element) t.getElementsByTagName("target").item(0);
					int sourceID = Integer.parseInt(source.getAttribute("ref"));
					int targetID = Integer.parseInt(target.getAttribute("ref"));
					
					// declare the annotations
					Annotation syncAnnot = null;
					Annotation guardAnnot = null;
					Annotation updateAnnot = null;
					
					// search for states with the appropriate IDs
					State sourceState = null;
					State targetState = null;
					for (State st : statesArray) {
						if (st.getID() == sourceID)
							sourceState = st;
						if (st.getID() == targetID)
							targetState = st;
					}
					
					// ensure that the source and target states aren't null
					if (sourceState == null || targetState == null)
						System.out.println("Error: source or target state is null");

					// obtain any synchronizations
					ArrayList<Object> syncsToAdd = new ArrayList<Object>();
					NodeList syncs = t.getElementsByTagName("sync");
					Point annotCoords = new Point();
					for (int k = 0; k < syncs.getLength(); k++) {
						Element s = (Element) syncs.item(k);
						
						String varname = s.getAttribute("var");
						Variable syncvar = null;
						
						syncvar = mod.findVariable(varname);
						
						// if this is a broadcast sync, is it a listener or enforcer?
						String role = "none";
						if (syncvar.getType().contains("bsync"))
							role = s.getAttribute("role");
						
						syncsToAdd.add(new Sync(syncvar, role));
						
						// get the coordinates
						annotCoords.x = Integer.parseInt(s.getAttribute("x"));
						annotCoords.y = Integer.parseInt(s.getAttribute("y"));
					}
					
					// create the sync annotation
					if (syncsToAdd.size() > 0)
						syncAnnot = new Annotation(syncsToAdd, "Sync", annotCoords, null, null);
						
					// obtain any updates
					ArrayList<Object> updatesToAdd = new ArrayList<Object>();
					NodeList updates = t.getElementsByTagName("update");
					for (int k = 0; k < updates.getLength(); k++) {
						Element u = (Element) updates.item(k);
						
						String varname = u.getAttribute("var");
						Variable updateVar = null;
						
						String updateval = u.getAttribute("val");
						String updateAssgVarStr = u.getAttribute("assgVar");
						Variable updateAssgVar = null;
						String updateAssgSign = u.getAttribute("assgSign");
						String updateAssgVal = u.getAttribute("assgVal");
						
						//System.out.println(updateval);
						//if (updateval.equals(""))
						//	System.out.println("(updateval is the empty string)");
						//System.out.println(updateAssgVarStr);
						//System.out.println(updateAssgSign);
						//System.out.println(updateAssgVal);
						//System.exit(0);
						
						// find the actual variable, whether it be local or global
						updateVar = mod.findVariable(varname);
						
						if (!updateval.equals(""))
							updatesToAdd.add(new Update(updateVar, updateval));
						else {
							updateAssgVar = mod.findVariable(updateAssgVarStr);
							updatesToAdd.add(new Update(updateVar, updateAssgVar, updateAssgSign, updateAssgVal));
						}
						
						// get the coordinates
						annotCoords.x = Integer.parseInt(u.getAttribute("x"));
						annotCoords.y = Integer.parseInt(u.getAttribute("y"));
					}
					
					// create the update annotation
					if (updatesToAdd.size() > 0)
						updateAnnot = new Annotation(updatesToAdd, "Update", annotCoords, null, null);
					
					// obtain any guards
					ArrayList<Object> guardsToAdd = new ArrayList<Object>();
					NodeList guards = t.getElementsByTagName("guard");
					for (int k = 0; k < guards.getLength(); k++) {
						Element g = (Element) guards.item(k);
						
						String varname = g.getAttribute("var");
						Variable guardVar = null;
						String guardrel = g.getAttribute("rel");
						String guardval = g.getAttribute("val");
						
						// find the actual variable, whether it be local or global
						guardVar = mod.findVariable(varname);
						guardsToAdd.add(new Guard(guardVar, guardval, guardrel));
						
						// get the coordinates
						annotCoords.x = Integer.parseInt(g.getAttribute("x"));
						annotCoords.y = Integer.parseInt(g.getAttribute("y"));
					}
					
					// create the guard annotation
					if (guardsToAdd.size() > 0)
						guardAnnot = new Annotation(guardsToAdd, "Guard", annotCoords, null, null);
					
					// obtain any nails
					ArrayList<Point> nailsToAdd  = new ArrayList<Point>();
					NodeList nails = t.getElementsByTagName("nail");
					for (int k = 0; k < nails.getLength(); k++) {
						Element n = (Element) nails.item(k);
						
						int x = Integer.parseInt(n.getAttribute("x"));
						int y = Integer.parseInt(n.getAttribute("y"));
						
						nailsToAdd.add(new Point(x,y));
					}
					
					//System.out.println(sourceState);
					//System.out.println(targetState);
					Transition temp = new Transition(sourceState, targetState, nailsToAdd);
					sourceState.addOutputTrans(temp);
					targetState.addInputTrans(temp);
					temp.addGuards((ArrayList<Guard>)(ArrayList<?>) guardsToAdd);
					temp.addUpdates((ArrayList<Update>)(ArrayList<?>) updatesToAdd);
					temp.addSyncs((ArrayList<Sync>)(ArrayList<?>) syncsToAdd);
					
					temp.setGuardAnnot(guardAnnot);
					temp.setUpdateAnnot(updateAnnot);
					temp.setSyncAnnot(syncAnnot);
					
					transitionsArray.add(temp);
				}
				
				// add the transitions to the module
				mod.addTransitions(transitionsArray);
				
				// re-do the ID's in the module
				mod.assignStateIDs();
				
				// and finally, add the module to the microinteraction
				micro.addModule(mod);
			}
			micro.build();
			//System.exit(0);

	}
	
	public void readSupreme(String supreme, Interaction ia) {
		ia.initializeInteraction();
		//ia.setNonAssistedSwitch(this.isNonAssisted);
		Document doc = initDoc(supreme);
		
		// get name
		ia.setName(getDocName(doc));
		//System.out.println("Reading an interaction with the name " + ia.getName());
			
		// get micro start/end times, confirm name matches with ID
		NodeList groups = doc.getElementsByTagName("group");
		for (int i = 0; i < groups.getLength(); i++) {
			//System.out.println("Reading a group");
			Element e = (Element) groups.item(i);
			int ID = Integer.parseInt(e.getAttribute("id"));
			boolean isInit = (e.getAttribute("init").equals("true"))?true:false;
			
			Element name = (Element) e.getElementsByTagName("name").item(0);
			String namestr = name.getTextContent();
			//System.out.println("GROUP NAME: " + namestr);
			
			Group mc = new Group(namestr, isInit);
			mc.setID(ID);
			mc.setLayoutX(Double.parseDouble(e.getAttribute("x")));
			mc.setLayoutY(Double.parseDouble(e.getAttribute("y")));
			
			// get all of the microinteractions within the group
			NodeList micros = e.getElementsByTagName("micro");
			for (int j = 0; j < micros.getLength(); j++) {
				Element m = (Element) micros.item(j);
				name = (Element) m.getElementsByTagName("name").item(0);
				namestr = name.getTextContent();
				
				Microinteraction micro = new Microinteraction();
				// search Lib for the correct microinteraction
				File dir = new File("Lib");
				for (File dirFile : dir.listFiles()) {
					if (dirFile.isDirectory() && !dirFile.getName().equals("Supreme"))
					for (File file : dirFile.listFiles()) {
						//System.out.println(file.getName() + " --- " + namestr);
						if (file.getName().equals(namestr + ".xml")) {
							//System.out.println(file);
							readMicrointeraction(file, file.getAbsolutePath(), micro);
						}
					}
				}
								
				NodeList params = m.getElementsByTagName("parameter");
				for (int k = 0; k < params.getLength(); k++) {
					Element p = (Element) params.item(k);
					String type = p.getAttribute("type");
					String paramName;
					if (type.equals("array")) {
						Element paramElementName = (Element) p.getElementsByTagName("name").item(0);
						paramName = paramElementName.getTextContent();
						
						Variable var = matchNameWithVar(paramName, micro.getGlobalVars());
						
						NodeList items = p.getElementsByTagName("item");
						for (int l = 0; l < items.getLength(); l++) {
							Element item = (Element) items.item(l);
							String val = item.getAttribute("val");
							String link = item.getAttribute("link");
							var.addItemToArray(val, link);
						}
					}
					else {
						paramName = p.getTextContent();
						
						// match up name with variable
						Variable var = matchNameWithVar(paramName, micro.getGlobalVars());
						var.setValue(p.getAttribute("val"));
					}
				}
				
				mc.addMicro(micro);
			}
			ia.addGroup(mc);
			if(mc.isInit())
				ia.setInit(mc);
			ia.updateGroupID(mc);
		}
			
		// get transitions
		NodeList trans = doc.getElementsByTagName("transition");
		for (int i = 0; i < trans.getLength(); i++) {
			//System.out.println("reading a macrotransition");
			Element e = (Element) trans.item(i);
			
			Element source = (Element) e.getElementsByTagName("source").item(0);
			int sref = Integer.parseInt(source.getAttribute("ref"));
			Group sourceGroup = ia.getGroup(sref);
			
			Element target = (Element) e.getElementsByTagName("target").item(0);
			int tref = Integer.parseInt(target.getAttribute("ref"));
			Group targetGroup = ia.getGroup(tref);
			
			BugTracker bt = ia.getBugTracker();
			GroupTransition mt = new GroupTransition(sourceGroup, targetGroup, bt);
			Polygon poly = new Polygon();
			mt.setPoly(poly);
			mt.setAllHumanBranching(false);
			mt.setTarget(targetGroup);
			
			NodeList guards = e.getElementsByTagName("guard");
			for (int j = 0; j < guards.getLength(); j++) {
				Element g = (Element) guards.item(j);
				String condition = g.getAttribute("condition");
				
				boolean[] branching = mt.getHumanBranching();
				if (condition.equals("human_ready"))
					branching[0] = true;
				if (condition.equals("human_busy"))
					branching[1] = true;
				if (condition.equals("human_ignore"))
					branching[2] = true;
			}
			
			ia.addTransition(mt);
		}
		
		ia.setBuilt(true);
		//System.out.println("Done reading supreme");
		//System.out.println(ia);
	}
	
	// match up variable name with variable
	private Variable matchNameWithVar(String name, ArrayList<Variable> vars) {
		Variable varToReturn = null;
		for (Variable var : vars) {
			if (var.getName().equals(name)) {
				varToReturn = var;
				break;
			}
		}
		return varToReturn;
	}
	
	// helper methods
	public Document initDoc(String filename) {
		try {
			File xmlFile = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			
			return doc;
		} catch (Exception e) {
			System.out.println(filename);
			System.exit(0);
			return null;
		}
	}
	
	public String getDocName(Document doc) {
		Element name = (Element) doc.getElementsByTagName("name").item(0);
		return name.getTextContent();
	}
	
	public String getDocDescription(Document doc) {
		Element desc = (Element) doc.getElementsByTagName("description").item(0);
		return desc.getTextContent();
	}
	
	public ArrayList<Variable> getDocGlobals(Document doc) {
		ArrayList<Variable> globalvars = new ArrayList<Variable>();
		NodeList globals = doc.getElementsByTagName("globals");
		for (int i = 0; i < globals.getLength(); i++) {
			Element e = (Element) globals.item(i);
	
			// get the variables that are global
			NodeList vars = e.getElementsByTagName("var");
			for (int j = 0; j < vars.getLength(); j++) {
				Element v = (Element) vars.item(j);
				String type = v.getAttribute("type");
				Variable g = new Variable(v.getAttribute("type"), v.getAttribute("name"));
				
				// if there is a bound, set the bound
				if (type.equals("int"))
					g.setBound(v.getAttribute("bound"));
					
				// get the init, if there is one
				String initStr = v.getAttribute("init");
				//if (!initStr.equals(""))
				g.initialize(v.getAttribute("init"));
				
				// get whether it is parameterizable
				String param = v.getAttribute("parameter");
				if (param != null && param.equals("true"))
					g.setParameterizable(true);
				
				String desc = v.getAttribute("description");
				if (desc != null) {
					g.addDescription(desc);
				}
				
				// if a nominal val, get the values and set the array
				if (type.equals("nominal")) {
					ArrayList<String> nominalVals = new ArrayList<String>();
					
					NodeList vals = e.getElementsByTagName("val");
					for (int k = 0; k < vals.getLength(); k++) {
						String content = vals.item(k).getTextContent();
						nominalVals.add(content);
					}
					
					g.setNominalValue(nominalVals);
				}
				
				// array requires no additional setup
				
				// get is required
				String required = v.getAttribute("required");
				if (param != null && param.equals("true"))
					g.setRequired(true);
				
				// get prismIgnore
				String pIgnore = v.getAttribute("prismIgnore");
				if (pIgnore != null && pIgnore.equals("true"))
					g.setPrismIgnore(true);
				
				// cannot be a nominal or an array unless ignored by Prism!
				if ((g.getType().equals("array") || g.getType().equals("nominal")) && !g.getPrismIgnore()) {
					System.out.println("Error: cannot have an array or nominal variable unless ignored by Prism");
					System.exit(0);
				}
				
				globalvars.add(g);
			}
		}
		
		return globalvars;
	}
	
	/*
	 * TEMPORARY PARSE METHODS.
	 */
	public static ArrayList<Sync> parseSync(String input, Microinteraction micro) {
		// check if microinteraction is null
		if (micro == null)
			return null;
		
		ArrayList<Sync> retreived = new ArrayList<Sync>();
		String[] syncs = input.split("\n");    // gather the individual syncs
		for (String sync : syncs ) { 
			boolean isListener = false;
			boolean isEnforcer = false;
			sync = sync.trim();                // remove trailing and leading white space
			char last = sync.charAt(sync.length()-1);
				
			if (last == '!') {
				isEnforcer = true;
				sync = sync.substring(0, sync.length()-1);
			}
			else if (last == '?') {
				isListener = true;
				sync = sync.substring(0, sync.length()-1);
			}
			
			Variable syncvar = micro.findVariable(sync);
			String role = "none";
			if (isListener) 
				role = "listener";
			else if (isEnforcer) 
				role = "enforcer";
	
			retreived.add(new Sync(syncvar, role));
		}
		
		return retreived;
	}
	
	public static ArrayList<Guard> parseGuard(String input, Microinteraction micro, Module mod) {
		// check if microinteraction is null
		if (micro == null)
			return null;
		
		ArrayList<Guard> retreived = new ArrayList<Guard>();
		//System.out.println("RETREIVED: " + retreived);
		String[] guards = input.split("&");
		for (String guard : guards) {
			guard = guard.trim();
			
			// extract the var, sign, and val
			// FOR NOW, ASSUME THAT ALL GUARDS TAKE THE FORM X[=<>]Y
			
			// determine the sign
			String delimiter;
			if (guard.contains(">="))
				delimiter = ">=";
			else if (guard.contains("<="))
				delimiter = "<=";
			else if (guard.contains("<"))
				delimiter = "<";
			else if (guard.contains(">"))
				delimiter = ">";
			else if (guard.contains("="))
				delimiter = "=";
			else {
				System.out.println("Error: no relational operator found within guard");
				return null;
			}
			
			String[] splitGuard = guard.split(delimiter);
			//System.out.println("GUARD: " + guard);
			//System.out.println("SPLIT: " + splitGuard[0]);
			//System.out.println("FINDING THE VARIABLE");
			Variable guardvar = mod.findVariable(splitGuard[0].trim());
			Guard g = new Guard(guardvar, splitGuard[1], delimiter);
			//System.out.println("ACTUAL GUARD: " + g);
			retreived.add(new Guard(guardvar, splitGuard[1], delimiter));
		}
		
		//System.out.println("RETREIVED: " + retreived);
		return retreived;
	}
	
	public static  ArrayList<Update> parseUpdate(String input, Microinteraction micro, Module mod) {
		// check if microinteraction is null
		if (micro == null)
			return null;
				
		ArrayList<Update> retreived = new ArrayList<Update>();
		String[] updates = input.split(",");
		for (String update : updates) {
			update = update.trim();
			
			String[] splitUpdate = update.split("=");
			for (String part : splitUpdate) 
				part = part.trim();
			
			Variable updatevar = mod.findVariable(splitUpdate[0]);
			retreived.add(new Update(updatevar, splitUpdate[1]));
		}
		
		return retreived;
	}
	
	public static ArrayList<Variable> parseVariable(String Input, Microinteraction micro, Module mod){
		return null;
	}

}
