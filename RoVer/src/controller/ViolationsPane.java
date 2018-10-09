package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import checkers.Conflict;
import checkers.ModBehPair;
import checkers.Property;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import model.Group;
import model.GroupTransition;
import model.Interaction;
import model.Module;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeCell;
import javafx.util.Callback;
import javafx.scene.layout.AnchorPane;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Tooltip;

public class ViolationsPane {
	
	private TreeView conflictBox;
	private MainController mc;
	private Interaction ia;
	
	// conflicts
	TreeItem root;
	//Conflict greeting;
	//Conflict farewell;
	Conflict noMicrosInInit;
	HashMap<Integer,Conflict> graphPropIAConflictLookup;
	HashMap<Group,HashMap<Integer,Conflict>> graphPropGroupConflictLookup;
	HashMap<String,Conflict> propertyCategories;
	ArrayList<String> addedPropertyCategories;
	
	public ViolationsPane(TreeView conflictBox, Interaction ia, ArrayList<String> propertyCategories, MainController mc) {
		this.ia = ia;
		this.mc = mc;
		
		noMicrosInInit = new Conflict(null, "Initial state contains no microinteractions.", "nofix", null, null, null, null, null, null);
		graphPropIAConflictLookup = new HashMap<Integer,Conflict>();
		for (Property prop : ia.getGraphProperties()) {
			if (prop.getTies().equals("interaction")) {
				Conflict conf = new Conflict(prop, ((prop.getContext() != null)?(prop.getContext() + ", "):"") + prop.getDescription(), "nofix", null, null, null, null, null, null);
				graphPropIAConflictLookup.put(prop.getID(),conf);
			}
		}
		graphPropGroupConflictLookup = new HashMap<Group,HashMap<Integer,Conflict>>();
		this.propertyCategories = new HashMap<String,Conflict>();
		this.addedPropertyCategories = new ArrayList<String>();
		
		for (String str : propertyCategories) {
			this.propertyCategories.put(str, new Conflict(null, str, "nofix", null, null, null, null, null, null));
		}
		
		//tv = conflictBox.getItems();
		this.conflictBox = conflictBox;
		root = new TreeItem("Property Violations");
		this.conflictBox.setRoot(root);
		this.conflictBox.setShowRoot(false);
		this.conflictBox.setPrefHeight(1000);
		this.conflictBox.setStyle("-fx-focus-color: transparent;");
		////this.conflictBox.setSelectionModel(null);
		this.conflictBox.setFocusTraversable(false);
		conflictBox.setStyle("-fx-background-color: white;");
		
		this.conflictBox.setCellFactory(new Callback<TreeView, TreeCell<HBox>>() {
            @Override
            public TreeCell<HBox> call(TreeView p) {
                return new AlertTreeCell();
            }
        });
		
		this.conflictBox.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
        	
            @Override
            public void handle(MouseEvent event) {
            	conflictBox.setStyle("-fx-background-color: white;");
            }
            
        });
		
		this.conflictBox.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
        	
            @Override
            public void handle(MouseEvent event) {
            	conflictBox.setStyle("-fx-background-color: white;");
            }
            
        });

	}
		
	public void update() {
		Iterator it = propertyCategories.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry)it.next();
			((Conflict) pair.getValue()).getChildren().clear();
		}
		root.getChildren().clear();
		addedPropertyCategories.clear();
		if (mc.getNonAssistedSwitch()) {
			for (Group group : ia.getGroups())
				addGazeGestureConflicts(group);
			return;
		}
		
		// look at all property violations
		if (ia.getInit().getMicrointeractions().isEmpty()) {
			root.getChildren().add(noMicrosInInit);
			//tv.add(noMicrosInInit);
		}
		else {
			//tv.remove(noMicrosInInit);
			if (!ia.getAuthProp() && ia.getCurrDesign().equals("Delivery")) {
				root.getChildren().add(new Conflict(null,"Delivery interaction must ALWAYS reach a group named \"Auth\" that verifies the human's identity. Either this group does not exist, or it is not guaranteed reachable!", "nofix", null, null, null, null, null, null));
			}
			
			if (ia.isViolatingBranch()) {
				if (!addedPropertyCategories.contains("Branching Errors")) {
					addedPropertyCategories.add("Branching Errors");
					root.getChildren().add(propertyCategories.get("Branching Errors"));
				}
				propertyCategories.get("Branching Errors").getChildren().add(new Conflict(null, "Branch conditions insufficient (See grayed-out transitions. Are you using else statements appropriately?).", "ia", null, null, null, null, null, null));
			}
			if (ia.isViolatingSequential()) {
				if (!addedPropertyCategories.contains("Jams")) {
					addedPropertyCategories.add("Jams");
					root.getChildren().add(propertyCategories.get("Jams"));
				}
				propertyCategories.get("Jams").getChildren().add(new Conflict(null, "Sequential composition of groups insufficient (see transitions with red or yellow indicators).", "ia", null, null, null, null, null, null));
			}
			
			HashMap<Integer,Boolean> graphPropertyValues;
			
			/*
			 * Group-specific proeprties
			 */
			for (Group group : ia.getGroups()) {
				if (group.getViolating()) {
					if (!addedPropertyCategories.contains("Speech Flubs")) {
						addedPropertyCategories.add("Speech Flubs");
						root.getChildren().add(propertyCategories.get("Speech Flubs"));
					}
					propertyCategories.get("Speech Flubs").getChildren().add(new Conflict(null, "In " + group.getName() + ", robot may interrupt the human's speech.", "nofix", null, null, group, mc, null, null));
				}
				
				// initialize the group lookup if not already initialized
				if (!graphPropGroupConflictLookup.containsKey(group))
					graphPropGroupConflictLookup.put(group, new HashMap<Integer,Conflict>());
				
				// add conflicts!
				graphPropertyValues = group.getGraphPropertyValues();
				for (Property prop : ia.getGraphProperties()) {
					if (prop.getTies().equals("group") || prop.getTies().equals("init")) {
						
						// if the property if violated
						if (!graphPropertyValues.get(prop.getID())) {
							// if the list of group conflicts does not already contain this one, add it
							if (!graphPropGroupConflictLookup.get(group).containsKey(prop.getID())) {
								graphPropGroupConflictLookup.get(group).put(prop.getID(), new Conflict(prop, ((prop.getContext() != null)?(prop.getContext() + " " + group.getName() + ", "):"") + prop.getDescription(), "nofix", null, null, group, mc, null, null));
							}
							if (!addedPropertyCategories.contains(prop.getCategory())) {
								addedPropertyCategories.add(prop.getCategory());
								root.getChildren().add(propertyCategories.get(prop.getCategory()));
							}
							propertyCategories.get(prop.getCategory()).getChildren().add(graphPropGroupConflictLookup.get(group).get(prop.getID()));
						}
					}
				}
				
				// get list of modules
				addGazeGestureConflicts(group);
			}
			
			/*
			 * interaction-specific properties
			 */
			graphPropertyValues = ia.getGraphPropertyValues();
			for (Property prop : ia.getGraphProperties()) {
				if (prop.getTies().equals("interaction")) {
					if (!graphPropertyValues.get(prop.getID())) {
						if (!addedPropertyCategories.contains(prop.getCategory())) {
							addedPropertyCategories.add(prop.getCategory());
							root.getChildren().add(propertyCategories.get(prop.getCategory()));
						}
						propertyCategories.get(prop.getCategory()).getChildren().add(graphPropIAConflictLookup.get(prop.getID()));
					}
				}
			}
		}
	}
	
	public void addGazeGestureConflicts(Group group) {
		ArrayList<Module> allMods = group.getMacrointeraction().getModules();
		
		for (ModBehPair mbp : group.allRelevantBehaviorConflicts(allMods)) {
			
			String desc = "";
			if (mbp.getFix() != null) 
				desc += "(RESOLVED) ";
				
			desc += "In " + group.getName() + ", robot might use ";
			
			ArrayList<String> behaviors = new ArrayList<String>();
			for (int i = 0; i < mbp.size(); i++) {
				desc += mbp.getBeh(i) + " in " + mbp.getMod(i).getName();
				String beh = mbp.getBeh(i);
				if (!behaviors.contains(beh))
					behaviors.add(beh);
				
				if (i >= mbp.size()-1);
				else
					desc += " and ";
			}
			
			desc += " at the same time.";
			
			root.getChildren().add(new Conflict(null, desc, (mbp.getFix() == null)?"canfix":"fix", behaviors, mbp, group, mc, null, null));
		}
	}

	private final class AlertTreeCell extends TreeCell<HBox> {

        private final AnchorPane anchorPane;
        private Tooltip tool;

        public AlertTreeCell() {
            anchorPane = new AnchorPane();
            anchorPane.setStyle("-fx-border-color: darkgray;"
        			+ "-fx-focus-color: blue;" 
        			+ "-fx-background-color: white;");
            anchorPane.setPadding(new Insets(5));
            anchorPane.setFocusTraversable(false);
            tool = new Tooltip("Cannot pipoint error to a specific point in the interaction.");
            this.setFocusTraversable(false);
            //this.setDisclosureNode(null);
            this.setStyle("-fx-focus-color: transparent;"
        			+ "-fx-background-color: white;");
            TreeCell tc = this;
            
            this.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            	
                @Override
                public void handle(MouseEvent event) {
                	TreeItem ti = tc.getTreeItem();
                	if (ti != null) {
                		Group group = ((Conflict) ti).getGroup();
                		if (group != null) {
                			if (((Conflict) ti).getChoices() == null)
                				group.activateNegativeFeedback();
                			else
                				group.activateBlinker();
                		}
                   		/* need to redo this asap */
                		else if (group == null && ((Conflict) ti).getDescription().contains("Sequential composition")) {
                			for (GroupTransition macro : ia.getMacroTransitions()) {
                				if (!macro.getBadConnections().isEmpty()) {
                					macro.activateNegativeFeedback();
                				}
                			}
                		}
                		
                		else if (group == null && ((Conflict) ti).getDescription().contains("Branch conditions")) {
            				for (Group g : ia.getGroups()) {
            					if (!g.checkBranchingPartition()[1]) {
		            				for (GroupTransition macro : g.getOutputMacroTransitions()) {
		            					macro.activateNegativeFeedback();
		            				}
            					}
            				}
            			}
                		else {
            				Tooltip.install(anchorPane, tool);
                		}
                	}
                }
                
            });
            
            this.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            	
                @Override
                public void handle(MouseEvent event) {
                	TreeItem ti = tc.getTreeItem();
                	if (ti != null) {
                		Group group = ((Conflict) ti).getGroup();
                		if (group != null) {
                			if (((Conflict) ti).getChoices() == null)
                				group.deactivateNegativeFeedback();
                			else
                				group.deactivateBlinker();
                		}
                		
                		/* need to redo this asap */
                		else if (group == null && ((Conflict) ti).getDescription().contains("Sequential composition")) {
                			for (GroupTransition macro : ia.getMacroTransitions()) {
                				if (!macro.getBadConnections().isEmpty()) {
                					macro.deactivateNegativeFeedback();
                				}
                			}
                		}
                		
                		else if (group == null && ((Conflict) ti).getDescription().contains("Branch conditions")) {
            				for (Group g : ia.getGroups()) {
            					if (!g.checkBranchingPartition()[1]) {
		            				for (GroupTransition macro : g.getOutputMacroTransitions()) {
		            					macro.deactivateNegativeFeedback();
		            				}
            					}
            				}
            			}
                		else {
            				Tooltip.uninstall(anchorPane, tool);
                		}
                	}
                }
                
            });
            
            this.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            	
                @Override
                public void handle(MouseEvent event) {
                	setStyle("-fx-background-color: white;");
                }
                
            });
            
            this.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            	
                @Override
                public void handle(MouseEvent event) {
                	setStyle("-fx-background-color: white;");
                }
                
            });
            
            anchorPane.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            	
                @Override
                public void handle(MouseEvent event) {
                	TreeItem ti = getTreeItem();
                	if (ti.isExpanded())
                		ti.setExpanded(false);
                	else
                		ti.setExpanded(true);
                }
            });
            
            anchorPane.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            	
                @Override
                public void handle(MouseEvent event) {
                	//anchorPane.setStyle("-fx-border-color: darkgray;"
                	//		+ "-fx-focus-color: blue;" 
                	//		+ "-fx-background-color: white;");
                }
            });
        }

        @Override
        public void updateItem(HBox item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                //setText(null);
                setGraphic(null);
                anchorPane.getChildren().clear();
                //anchorPane.setStyle("-fx-border-color: darkgray;"
            	//		+ "-fx-focus-color: blue;" 
            	//		+ "-fx-background-color: white;");
                this.setStyle("-fx-focus-color: transparent;"
            			+ "-fx-background-color: white;");
            } else {
                //setText(null);
                //label.setText(item.getStatus());
                //button.setText(item.getName());
                //setGraphic(anchorPane);
            	anchorPane.getChildren().add(item);
            	setGraphic(anchorPane);
            	this.setPadding(new Insets(2));
            	this.setFocusTraversable(false);
            	//this.setDisclosureNode(null);
            	//anchorPane.setStyle("-fx-border-color: darkgray;"
            	//		+ "-fx-focus-color: blue;" 
            	//		+ "-fx-background-color: white;");
            	this.setStyle("-fx-focus-color: transparent;"
            			+ "-fx-background-color: white;");
            }
        }
    }
}
