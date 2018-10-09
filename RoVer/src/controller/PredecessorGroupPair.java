package controller;

import model.Group;

public class PredecessorGroupPair {
	
	public Group predecessor;
	public Group group;
	
	public PredecessorGroupPair(Group predecessor, Group group) {
		this.predecessor = predecessor;
		this.group = group;
	}

}
