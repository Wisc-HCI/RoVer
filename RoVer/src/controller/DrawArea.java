package controller;
import java.io.File;
import java.util.ArrayList;
import javafx.scene.layout.Pane;
import model.Microinteraction;
import model.Module;

/*
 * Class used to hold states, transitions and annotations in the microinteraction editor
 * An instance of this class is used to populate a tab in the microinteraction editor
 * It also holds the undo and redo stack for the microinteraction associated with it
 */
public class DrawArea extends Pane {
	
	private ArrayList<editStackObject> undoStack, redoStack;
	private Module module;
	private Microinteraction rootMicro;
	
	public DrawArea(boolean isGrid, Microinteraction micro, Module module){
		undoStack = new ArrayList<>();
		redoStack = new ArrayList<>();
		this.module = module;
		rootMicro = micro;
		if (isGrid) {
			setStyle("-fx-background-color: #F9FFFF");
			isGrid = true;
		} else {
			setStyle(
					"-fx-background-color: #F9FFFF,"
					+ "linear-gradient(from 0.5px 0px to 15.5px 0px, repeat, lightgray 1%, transparent 5%),"
					+ "linear-gradient(from 0px 0.5px to 0px 15.5px, repeat, lightgray 1%, transparent 5%);");
			isGrid = false;
		}
	}
	
	public ArrayList<editStackObject> getUndoStack(){
		return undoStack;
	}
	
	public ArrayList<editStackObject> getRedoStack(){
		return redoStack;
	}
	
	public void setUndoStack(ArrayList<editStackObject> undoStack){
		this.undoStack = undoStack;
	}
	
	public void setRedoStack(ArrayList<editStackObject> redoStack){
		this.redoStack = redoStack;
	}
	
	public Microinteraction getRootMicro(){
		return rootMicro;
	}
	
	public Module getModule(){
		return module;
	}
}
