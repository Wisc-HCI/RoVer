package model;
import java.awt.Point;
import java.util.ArrayList;
import controller.Annotation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polyline;

/*
 * Class to store each instance of a line between two states in the model
 * 
 * Tells you the start state and end state of each point
 * 
 * Contains an arrayList that holds each of the nails in the line
 */
public class Transition extends Polyline {
	private State source, target;
	private ObservableList<Nail> nail;
	private ArrayList<Update> updates;
	private ArrayList<Guard> guards;
	private ArrayList<Sync> syncs;
	//private Annotation annotation;
	private Annotation guardAnnot;
	private Annotation syncAnnot;
	private Annotation updateAnnot;

	public Transition(State source, State target, ArrayList<Point> points) {

		super(source.getX(), source.getY());
		
		if(points!=null){
			for(Point point: points){
				getPoints().addAll(point.getX(), point.getY());
			}
			nail = createControlAnchorsFor(getPoints());
		}
		
		getPoints().addAll(target.getX(), target.getY());
		
		
		this.source = source;
		this.target = target;
		setStrokeWidth(3);
		//setStroke(Color.BLACK);
		LinearGradient linearGradient = new LinearGradient(source.getX(), source.getY(),
				target.getX(), target.getY(), false,
				CycleMethod.NO_CYCLE, new Stop(0,Color.RED),new Stop(1,Color.BLUE));
		setStroke(linearGradient);
		
		initializeAnnotations();
	}
	
	public Transition() {
		source = null;
		target = null;
		initializeAnnotations();
	}
	
	private void initializeAnnotations() {
		guards = new ArrayList<Guard>();
		updates = new ArrayList<Update>();
		syncs = new ArrayList<Sync>();
	}
	
	public Transition copyWithoutStates(Microinteraction parentMicro, Module parentModule) {
		Transition trans = new Transition();
		
		// add the updates
		for (Update u : updates) {
			// get the var, match it to a current var in the parents
			Variable v = u.getVar();
			Variable newV = parentModule.findVariable(v.getName());
			
			// determine the type of update
			Update newU;
			if (u.getAssgExpression() == null)  // regular update
				newU = new Update(newV, u.getVal());
			else {   // non-regular update
				Expression e = u.getExpression();
				String sign = e.getSign();
				String val = e.getUpdate();
				Variable eVar = parentModule.findVariable(e.getVar().getName());
				newU = new Update(newV, eVar, sign, val);
			}
			trans.addUpdate(newU);
		}
		
		// add the guards
		for (Guard g : guards) {
			Variable v = g.getVar();
			Variable vNew = parentModule.findVariable(v.getName());
			Guard newG = new Guard(vNew, g.getVal(), g.getSign());
			trans.addGuard(newG);
		}
		
		// add the syncs
		for (Sync s : syncs) {
			Variable v = s.getVar();
			Variable vNew = parentModule.findVariable(v.getName());
			Sync newS = new Sync(vNew, s.getType());
			trans.addSync(newS);
		}
		
		return trans;
	}
	
	public void setSource(State source) {
		this.source = source;
	}

	public void setTarget(State target) {
		this.target = target;
	}

	public void setColor(Color color) {
		setStroke(color);
	}

	public State getSource() {
		return source;
	}

	public State getTarget() {
		return target;
	}

	public ArrayList<Guard> getGuards() {
		return guards;
	}

	public ArrayList<Update> getUpdates() {
		return updates;
	}

	public ArrayList<Sync> getSyncs() {
		return syncs;
	}

	public void addGuard(Guard guard) {
		this.guards.add(guard);
	}

	public void addGuards(ArrayList<Guard> guards) {
		this.guards.addAll(guards);
	}

	public void removeGuards() {
		this.guards.clear();
	}

	public void removeGuards(ArrayList<Guard> toRemove) {
		for (Guard guard : toRemove)
			removeGuard(guard);
	}

	public void removeGuard(Guard guard) {
		this.guards.remove(guard);
	}

	public void updateGuards(ArrayList<Guard> retreived) {
		// Update the guards: remove any guards that aren't there anymore
		ArrayList<Guard> toRemove = new ArrayList<Guard>();
		for (Guard guard : guards) {
			boolean isPresent = false;
			for (Guard newGuard : retreived) {
				if (guard.isSame(newGuard))
					isPresent = true;
			}

			if (!isPresent)
				toRemove.add(guard);
		}

		removeGuards(toRemove);

		// Update the guards: add any guards that haven't been added
		for (Guard guard : retreived) {
			boolean isPresent = false;
			for (Guard oldGuard : guards) {
				if (oldGuard.isSame(guard))
					isPresent = true;
			}

			if (!isPresent)
				addGuard(guard);
		}
	}

	public void setUpdates(Update update) {
		this.updates.add(update);
	}

	public void addUpdates(ArrayList<Update> updates) {
		this.updates.addAll(updates);
	}

	public void updateUpdates(ArrayList<Update> retreived) {
		// Update the syncs: remove any syncs that aren't there anymore
		ArrayList<Update> toRemove = new ArrayList<Update>();
		for (Update update : updates) {
			boolean isPresent = false;
			for (Update newUpdate : retreived) {
				if (update.isSame(newUpdate))
					isPresent = true;
			}

			if (!isPresent)
				toRemove.add(update);
		}

		removeUpdates(toRemove);

		// Update the syncs: add any syncs that haven't been added
		for (Update update : retreived) {
			boolean isPresent = false;
			for (Update oldUpdate : updates) {
				if (oldUpdate.isSame(update))
					isPresent = true;
			}

			if (!isPresent)
				addUpdate(update);
		}
	}

	public void addUpdate(Update update) {
		updates.add(update);
	}

	public void removeUpdates() {
		this.updates.clear();
	}

	public void removeUpdates(ArrayList<Update> toRemove) {
		for (Update update : toRemove)
			removeUpdate(update);
	}

	public void removeUpdate(Update update) {
		this.updates.remove(update);
	}

	public void updateSyncs(ArrayList<Sync> retreived) {
		// Update the syncs: remove any syncs that aren't there anymore
		ArrayList<Sync> toRemove = new ArrayList<Sync>();
		for (Sync sync : syncs) {
			boolean isPresent = false;
			for (Sync newSync : retreived) {
				if (sync.isSame(newSync))
					isPresent = true;
			}

			if (!isPresent)
				toRemove.add(sync);
		}

		removeSyncs(toRemove);

		// Update the syncs: add any syncs that haven't been added
		for (Sync sync : retreived) {
			boolean isPresent = false;
			for (Sync oldSync : syncs) {
				if (oldSync.isSame(sync))
					isPresent = true;
			}

			if (!isPresent)
				addSync(sync);
		}
	}

	public void addSync(Sync sync) {
		this.syncs.add(sync);
	}

	public void addSyncs(ArrayList<Sync> syncs) {
		this.syncs.addAll(syncs);
	}

	public void removeSyncs() {
		this.syncs.clear();
	}

	public void removeSyncs(ArrayList<Sync> toRemove) {
		for (Sync sync : toRemove)
			removeSync(sync);
	}

	public void removeSync(Sync sync) {
		this.syncs.remove(sync);
	}

	public ObservableList<Nail> getNails() {
		return nail;
	}

	public void addNail(Nail nail) {
		this.nail.add(nail);
	}

	public void removeNail(int index) {
		nail.remove(index);
	}

	//public void setAnnotation(Annotation ann) {
	//	annotation = ann;
	//}
	
	public void setGuardAnnot(Annotation ann) {
		guardAnnot = ann;
	}
	
	public void setUpdateAnnot(Annotation ann) {
		updateAnnot = ann;
	}
	
	public void setSyncAnnot(Annotation ann) {
		syncAnnot = ann;
	}

	//public Annotation getAnnotation() {
	//	return annotation;
	//}
	
	public Annotation getGuardAnnot() {
		return guardAnnot;
	}
	
	public Annotation getUpdateAnnot() {
		return updateAnnot;
	}
	
	public Annotation getSyncAnnot() {
		return syncAnnot;
	}
	
	public String toString() {
		String str = "Transition: \n";

		str += "source: " + source.toString();
		str += "target: " + target.toString();
		for (Guard guard : this.guards)
			str += guard.toString();
		for (Update update : this.updates)
			str += update.toString();

		return str;
	}
	
	private ObservableList<Nail> createControlAnchorsFor(final ObservableList<Double> points) {
	    ObservableList<Nail> anchors = FXCollections.observableArrayList();

	    for (int i = 2; i < points.size(); i+=2) {
	      final int idx = i;

	      DoubleProperty xProperty = new SimpleDoubleProperty(points.get(i));
	      DoubleProperty yProperty = new SimpleDoubleProperty(points.get(i + 1));

	      xProperty.addListener(new ChangeListener<Number>() {
	        @Override public void changed(ObservableValue<? extends Number> ov, Number oldX, Number x) {
	          points.set(idx, (double) x);
	        }
	      });

	      yProperty.addListener(new ChangeListener<Number>() {
	        @Override public void changed(ObservableValue<? extends Number> ov, Number oldY, Number y) {
	          points.set(idx + 1, (double) y);
	        }
	      });

	      anchors.add(new Nail( xProperty, yProperty));
	    }

	    return anchors;
	  }
}
