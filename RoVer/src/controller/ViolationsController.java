package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import checkers.Conflict;
import checkers.ModBehPair;
import javafx.scene.control.ListView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;
import model.Group;
import model.Interaction;
import model.Module;

public class ViolationsController {
	
	private ListView conflictBox;
	
	private Button okay, cancel;
	private TextField name;
	private boolean flag;
	private Group group;
	private Interaction ia;
	private MainController mc;
	private Image helperBusyCommentWait;
	private Image helperInstActionAskAnswer;
	
	public ViolationsController(Interaction ia, MainController mc) {
		this.ia = ia;
		this.mc = mc;
		
		// load up some of the helper images
		String path = "Icons" + File.separator;
		try {
			helperBusyCommentWait = new Image(new FileInputStream(path + "helper1.png"));
			helperInstActionAskAnswer = new Image(new FileInputStream(path + "helper2.png"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean display(){
		
		try{
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "Violations.fxml"));
			Scene scene = new Scene(root, 559, 396);
			Stage window = new Stage();
			window.setScene(scene);
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
			
			okay = (Button) scene.lookup("#createButton");
			cancel = (Button) scene.lookup("#cancelButton");
			conflictBox = (ListView) scene.lookup("#conflictBox");
				
			conflictBox.setDisable(true);
			
			// look at all property violations
			if (ia.getInit().getMicrointeractions().isEmpty()) {
				conflictBox.setDisable(false);
				conflictBox.getItems().add(new Conflict(null, "Initial state contains no microinteractions.", "nofix", null, null, null, null, null, null));
			}
			else {
				
				if (!ia.getAuthProp() && ia.getCurrDesign().equals("Delivery")) {
					conflictBox.getItems().add(new Conflict(null, "Delivery interaction must ALWAYS reach a group named \"Auth\" that verifies the human's identity. Either this group does not exist, or it is not guaranteed reachable!", "nofix", null, null, null, null, null, null));
					conflictBox.setDisable(false);
				}
				if (!ia.getInit().getGreetingProp()) {
					conflictBox.getItems().add(new Conflict(null, "Interaction must contain a greeting at the start.", "ia", null, null, null, null, null, null));
					conflictBox.setDisable(false);
				}
				if (!ia.getFarewellProp()) {
					conflictBox.getItems().add(new Conflict(null, "Interaction must end with a farewell. If you think it does, perhaps your interaction ends prematurely?", "ia", null, null, null, null, null, null));
					conflictBox.setDisable(false);
				}
				if (ia.isViolatingBranch()) {
					conflictBox.getItems().add(new Conflict(null, "Branch conditions insufficient (See grayed-out transitions. Are you using else statements appropriately?).", "ia", null, null, null, null, null, null));
					conflictBox.setDisable(false);
				}
				if (ia.isViolatingSequential()) {
					conflictBox.getItems().add(new Conflict(null, "Sequential composition of groups insufficient (see transitions with red or yellow indicators).", "ia", null, null, null, null, null, null));
					conflictBox.setDisable(false);
				}
				
				for (Group group : ia.getGroups()) {
					if (group.getIsViolatingSomething(ia.getTutorial(), ia.getCurrDesign())) {
						conflictBox.setDisable(false);
					}
					
					if (group.getViolating()) {
						conflictBox.getItems().add(new Conflict(null, "In " + group.getName() + ", robot may interrupt the human's speech.", "nofix", null, null, group, null, null, null));
					}
					if (!group.getGreetingProp()) {
						conflictBox.getItems().add(new Conflict(null, "In " + group.getName() + ", robot must start interaction with a greeting or by waiting for the human's attention.", "nofix", null, null, group, null, null, null));
					}
					if (!group.getDoubleGreetingProp() && ia.getCurrDesign().equals("Delivery")) {
						conflictBox.getItems().add(new Conflict(null, "In " + group.getName() + ", robot should not issue multiple greetings in a row.", "nofix", null, null, group, null, null, null));
					}
					if (!group.getAuthAfterHandoffProp() && ia.getCurrDesign().equals("Delivery")) {
						conflictBox.getItems().add(new Conflict(null, "In " + group.getName() + ", robot must authenticate the human's identity before completing the handoff.", "nofix", null, null, group, null, null, null));
					}
					if (!group.getHandoffAfterAuthProp() && ia.getCurrDesign().equals("Delivery")) {
						conflictBox.getItems().add(new Conflict(null, "In " + group.getName() + ", if the human's identity is authenticated, the robot must complete the handoff. Otherwise, abort the handoff.", "nofix", null, null, group, null, null, null));
					}
					if (!group.getBusyCommentWaitProp()) {
						conflictBox.getItems().add(new Conflict(null, "In " + group.getName() + ", when the human says nothing at all (they are BUSY), try providing a helpful remark and then waiting for them to indicate that they have completed the instruction.", "nofix", null, null, group, null, helperBusyCommentWait, null));
					}
					if (!group.getInstActionAskAnswerProp()) {
						conflictBox.getItems().add(new Conflict(null, "In " + group.getName() + ", when the human says \"I have a question,\" (they are SUSPENDED) the robot should listen for a question with the Answer microinteraction. Optionally, you can place a Remark before the Answer microinteraction to indicate that the robot is ready to listen.", "nofix", null, null, group, null, helperInstActionAskAnswer, null));
					}
					
					// get list of modules
					addGazeGestureConflicts(group);
				}
			}
			
			
			okay.setOnAction(e -> {
				flag = true;
				window.close();
			});
			
			cancel.setOnAction(e -> {
				flag = false;
				window.close();
			});
			window.showAndWait();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return flag;
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
			
			conflictBox.getItems().add(new Conflict(null, desc, (mbp.getFix() == null)?"canfix":"fix", behaviors, mbp, group, mc, null, null));
		}
	}
}
