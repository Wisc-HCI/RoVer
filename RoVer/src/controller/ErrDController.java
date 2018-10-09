package controller;
import java.io.File;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/*
 * Error Dialogue Controller Class
 * 
 * Takes an error message and all relevant information and displays it to the user
 */
public class ErrDController {

	private Button okay, details;
	private Text errorText;
	
	public void display(String errorMessage, Exception ex){
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "ErrorDialogue.fxml"));
			Scene scene = new Scene(root, 450, 200);
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Program Error");
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
			
			okay = (Button) scene.lookup("#okayButton");
			details = (Button) scene.lookup("#detailsButton");
			errorText = (Text) scene.lookup("#errorText");
						
			okay.setOnAction(e -> {
				window.close();
			});
			
			//TODO implement more details action
			details.setOnAction(e -> {
				errorText.setText(ex.toString());
				details.setVisible(false);
			});
			
			errorText.setText(errorMessage);
			
			window.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
