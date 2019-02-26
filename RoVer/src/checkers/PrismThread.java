package checkers;
import java.util.ArrayList;

import controller.ConsoleCT;
import controller.MainController;
import model.Group;
import model.GroupTransition;
import model.Interaction;
import model.Microinteraction;
import javafx.application.Platform;

public class PrismThread implements Runnable {
	
	// IMPORTANT: flip this switch to "true" to disable prism
	private final boolean disablePrism = false;
	
	// necessary references
	private Checker c;
	private ConsoleCT console;
	private Interaction ia;
	private MainController mc;
	private Microinteraction micro;
	private Group group;
	private GroupTransition mt;
	
	private Thread t;
	private String type;
	
	public PrismThread(ConsoleCT console, Interaction ia, MainController mc) {
		this.mc = mc;
		this.micro = null;
		this.mt = null;
		this.group = null;
		initialize(console, ia);
	}
	
	public PrismThread(ConsoleCT console, Interaction ia, MainController mc, Microinteraction micro) {
		this.mc = mc;
		this.micro = micro;
		this.mt = null;
		this.group = null;
		initialize(console, ia);
	}
	
	public PrismThread(ConsoleCT console, Interaction ia, MainController mc, GroupTransition mt) {
		this.mc = mc;
		this.micro = null;
		this.group = null;
		this.mt = mt;
		initialize(console, ia);
	}
	
	public PrismThread(ConsoleCT console, Interaction ia, MainController mc, Group group) {
		this.mc = mc;
		this.group = group;
		this.micro = null;
		this.mt = null;
		initialize(console, ia);
	}
	
	private void initialize(ConsoleCT console, Interaction ia) {
		this.c = ia.getChecker();
		this.console = console;
		this.ia = ia;
		t = new Thread(this, "prism");
	}
	
	public Thread getThread() {
		return t;
	}
	
	public void start(String type, String engine) {
		c.switchEngine(engine);
		start(type);
	}

	public void start(String type) {
		this.type = type;
		t.start();
	}
	
	public void run() {
		if (!disablePrism) {
			if (c == null)
				this.startPrism();
			
			if (type.equals("concurrent")) {
				c.generatePrismFile(group.getMacrointeraction());
				c.checkConcurrent(group);
			}
			else if (type.equals("startEndStates")) {
				System.out.println("PRISM THREAD: start-end states");
				c.generatePrismFile(micro);
				//c.dotExporter(micro);
				c.getStartEndStates(micro);
				System.out.println("PRISM THREAD: obtained start end states");
			}
			else if (type.equals("sequential")) {
				if (mt == null)
					c.checkSequential();
				else
					c.checkSequential(mt);
			}
			else if (type.equals("graph")) {
				c.checkGraph(ia);
			}
			else if (type.equals("allConcurrent")) {
				for (Group group : ia.getGroups()) {
					if (!group.getMicrointeractions().isEmpty()) {
						group.createReducedMergedMacrointeraction(c, console, ia, mc);
						c.generatePrismFile(group.getMacrointeraction());
						c.checkConcurrent(group);
					}
				}
			}
			else if (type.equals("concurrentAndGraph")) {
				// TODO: THIS SHOULD NOT NEED TO BE A THING
				ArrayList<Group> copy = new ArrayList<Group>(ia.getGroups());
				for (Group group : copy) {
					if (!group.getMicrointeractions().isEmpty()) {
						group.createReducedMergedMacrointeraction(c, console, ia, mc);
						c.generatePrismFile(group.getMacrointeraction());
						c.checkConcurrent(group);
					}
				}
				c.checkGraph(ia);
				// update the conflict pane
				Platform.runLater(
						() -> {
							mc.updateConflictPane();
						}
					);
			}
			else if (type.equals("reachability")) {
				c.checkReachability();
			}
			else if (type.equals("endStateReachability")) {
				c.checkEndStateReachability(group);
			}
			else if (type.equals("exportToDot")) {
				c.generatePrismFile(micro);
				c.dotExporter(micro);
			}
			else if (type.equals("exportToTM")) {
				System.out.println("Calling the exporter");
				c.tmExporter(micro);
			}
			else if (type.equals("generatePrismFile")) {
				c.generatePrismFile(micro);
			}
		}
	}
	
	@SuppressWarnings("restriction")
	private void startPrism() {
		Platform.runLater(
			() -> {
				mc.notifyInitPrism();
			}
		);
		c = new Checker(ia, console, mc.getNonAssistedSwitch(), ia.getGraphProperties());
		
		c.generatePrismFiles();
		c.startPrism();
		
		if (ia.getIsCopy()) {
			ia.setChecker(c, mc, console);
		}
		else {
			Platform.runLater(
				() -> {
					mc.notifyFinishedInitPrism();
					ia.setChecker(c,mc,console);
				}
			);
		}
		Platform.runLater(
			() -> {
				mc.notifyFinishedInitPrism();
				mc.startDesign();
			}
		);
	}
	
}
