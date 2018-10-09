package controller;

//Class used to hold a single undo/redo command
public class editStackObject {
	public Object OBJECT;
	public String TYPE;
	public editStackObject(Object obj, String type){
		OBJECT = obj;
		TYPE = type;
	}
}
