package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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

public class GroupPropertiesController {

	//private ListView conflictBox;

	private Button okay, cancel;
	private TextField name;
	private boolean flag;
	private Group group;
	private Interaction ia;
	private MainController mc;
	private Image helperBusyCommentWait;
	private Image helperInstActionAskAnswer;

	public GroupPropertiesController(Group group, Interaction ia, MainController mc) {
		this.group = group;
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
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "GroupProperties.fxml"));
			Scene scene = new Scene(root, 325, 107);
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Group: " + group.getName());
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);

			okay = (Button) scene.lookup("#createButton");
			cancel = (Button) scene.lookup("#cancelButton");
			name = (TextField) scene.lookup("#textField");

			/*
			conflictBox = (ListView) scene.lookup("#conflictBox");

			// look at all of the (hardcoded :/ ) property violations
			if (!group.getIsViolatingSomething(ia.getTutorial(), ia.getCurrDesign()) || mc.getNonAssistedSwitch().getIsNonAssisted()) {
				conflictBox.setStyle("-fx-background-color: lightgray");

				addGazeGestureConflicts();
				if (conflictBox.getItems().isEmpty()) {
					conflictBox.setDisable(true);
				}
			}
			else {
				conflictBox.setDisable(false);

				if (group.getViolating()) {
					conflictBox.getItems().add(new Conflict(null, "robot may interrupt the human's speech.", "nofix", null, null, group, null, null, null));
				}
				if (!group.getGreetingProp()) {
					conflictBox.getItems().add(new Conflict(null, "robot must start interaction with a greeting or by waiting for the human's attention.", "nofix", null, null, group, null, null, null));
				}
				if (!group.getDoubleGreetingProp() && ia.getCurrDesign().equals("Delivery")) {
					conflictBox.getItems().add(new Conflict(null, "robot should not issue multiple greetings in a row.", "nofix", null, null, group, null, null, null));
				}
				if (!group.getAuthAfterHandoffProp() && ia.getCurrDesign().equals("Delivery")) {
					conflictBox.getItems().add(new Conflict(null, "robot must authenticate the human's identity before completing the handoff.", "nofix", null, null, group, null, null, null));
				}
				if (!group.getHandoffAfterAuthProp() && ia.getCurrDesign().equals("Delivery")) {
					conflictBox.getItems().add(new Conflict(null, "If the human's identity is authenticated, the robot must complete the handoff. Otherwise, abort the handoff.", "nofix", null, null, group, null, null, null));
				}
				if (!group.getBusyCommentWaitProp()) {
					conflictBox.getItems().add(new Conflict(null, "When the human says nothing at all (they are BUSY), try providing a helpful remark and then waiting for them to indicate that they have completed the instruction.", "nofix", null, null, group, null, helperBusyCommentWait, null));
				}
				if (!group.getInstActionAskAnswerProp()) {
					conflictBox.getItems().add(new Conflict(null, "When the human says \"I have a question,\" (they are SUSPENDED) the robot should listen for a question with the Answer microinteraction. Optionally, you can place a Remark before the Answer microinteraction to indicate that the robot is ready to listen.", "nofix", null, null, group, null, helperInstActionAskAnswer, null));
				}

				// get list of modules
				addGazeGestureConflicts();

			}*/

			name.setText(group.getName());

			okay.setOnAction(e -> {
				flag = true;
				// parse name
				String txt = name.getText();
				txt = txt.replaceAll("\\s+","_");
				if (txt.length() > 20)
					txt = txt.substring(0, 16);
				group.setName(txt);
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

	/*
	public void addGazeGestureConflicts() {
		ArrayList<Module> allMods = group.getMacrointeraction().getModules();

		for (ModBehPair mbp : group.allRelevantBehaviorConflicts(allMods)) {

			String desc = "";
			if (mbp.getFix() != null)
				desc += "(RESOLVED) ";

			desc += "Robot might use ";

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
	*/
}
