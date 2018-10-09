package controller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Guard;
import model.Microinteraction;
import model.Module;
import model.Sync;
import model.Update;
import model_ctrl.Decoder;


/*
 * Controller Class for Edit Transition Dialogue Box
 * 
 * Gets and Sets all the relevant information about a transition
 * 
 * Sets the necessary listeners for the Dialogue box elements.
 */
public class etdController {

	private Button cancel, okay;
	private TextArea guardField, updateField, syncField;
	private String guardTF, syncTF, updateTF;
	Object returnObj[];

	public Object[] display(ArrayList<Guard> gal, ArrayList<Update> ual, ArrayList<Sync> sal, Microinteraction micro, Module mod) {

		Parent root;
		try {
			root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "editTransitionDialogue.fxml"));
			Scene scene = new Scene(root, 377, 350);
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Edit Transition Dialogue");
			window.centerOnScreen();
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UNDECORATED);

			cancel = (Button) scene.lookup("#cancelButton");
			okay = (Button) scene.lookup("#okayButton");
			
			guardField = (TextArea) scene.lookup("#guardField");
			updateField = (TextArea) scene.lookup("#updateField");
			syncField = (TextArea) scene.lookup("#syncField");

			returnObj = new Object[3];

			guardTF = syncTF = updateTF = "";

			if (gal != null) {
				for (Guard guard : gal) {
					guardTF += guard.stringify() + " & ";
				}
				if (gal.size() != 0) {
					guardTF = guardTF.substring(0, guardTF.length() - 3);
					guardField.setText(guardTF);
				}
			}

			if (ual != null) {
				for (Update update : ual) {
					updateTF += update.stringify() + " & ";
				}
				if (ual.size() != 0) {
					updateTF = updateTF.substring(0, updateTF.length() - 3);
					updateField.setText(updateTF);
				}

			}

			if (sal != null) {
				for (Sync sync : sal) {
					syncTF += sync.stringify() + " & ";
				}
				if (sal.size() != 0) {
					syncTF = syncTF.substring(0, syncTF.length() - 3);
					syncField.setText(syncTF);
				}
			}

			cancel.setOnAction(e -> {
				window.close();
			});

			okay.setOnAction(e -> {
				
				syncTF = syncField.getText().trim();
				guardTF = guardField.getText().trim();
				updateTF = updateField.getText().trim();
				
				System.out.println(guardTF);
				if(guardTF.length() > 0)
					returnObj[0] = Decoder.parseGuard(guardTF, micro, mod);
				else
					returnObj[0] = null;
				if(updateTF.length() > 0)
					returnObj[1] = Decoder.parseUpdate(updateTF, micro, mod);
				else
					returnObj[1] = null;
				if(syncTF.length() > 0)
					returnObj[2] = Decoder.parseSync(syncTF, micro);
				else
					returnObj[2] = null;
				window.close();
			});

			window.showAndWait();

		} catch (IOException e) {

			e.printStackTrace();
		}

		return returnObj;
	}

}
