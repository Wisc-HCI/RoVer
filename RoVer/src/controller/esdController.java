package controller;
import java.io.File;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Microinteraction;

/*
 * Controller Class for Edit Transition Dialogue Box
 * 
 * Gets and Sets all the relevant information about a transition
 * 
 * Sets the necessary listeners for the Dialogue box elements
 */
public class esdController {

	private Button okay, cancel;
	private TextField nameField;
	private TextArea invarField, commentField;
	private CheckBox cbInitial, cbCommited;
	
	private Object returnObj[];

	public Object[] display(String name, boolean isCommited, boolean isInitial, Microinteraction micro) {

		Parent root;
		try {
			root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "editStateDialogue .fxml"));
			Scene scene = new Scene(root, 315, 333);
			Stage window = new Stage();
			returnObj = new Object[5];
			window.setScene(scene);
			window.setTitle("Edit State Dialogue");
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UNDECORATED);

			cancel = (Button) scene.lookup("#cancelButton");
			okay = (Button) scene.lookup("#okayButton");
			
			nameField = (TextField) scene.lookup("#nameField");
			invarField = (TextArea) scene.lookup("#invarField");
			commentField = (TextArea) scene.lookup("#commentField");
			cbInitial = (CheckBox) scene.lookup("#checkBoxInitial");
			cbCommited = (CheckBox) scene.lookup("#checkBoxCommited");
			
			nameField.setText(name);
			invarField.setText("DISABLED! DO NOT ADD DATA IN THIS FIELD");
			invarField.setEditable(false);
			cbCommited.setSelected(isCommited);
			cbInitial.setSelected(isInitial);
		
			
			cancel.setOnAction(e -> {
				returnObj = null;
				window.close();
			});
			
			okay.setOnAction(e -> {
				returnObj[0] = nameField.getText();
				returnObj[1] = cbCommited.isSelected();
				returnObj[2] = cbInitial.isSelected();
				returnObj[3] = invarField.getText();
				returnObj[4] = commentField.getText().trim();
				window.close();
			});
			
			window.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return returnObj;
	}
}
