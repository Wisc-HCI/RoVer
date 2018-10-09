package controller;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Interaction;

public class SaveNotifier {
	private Button okay, cancel;
	private TextField name;
	private boolean flag;
	private Interaction ia;
	
	public SaveNotifier(Interaction ia) {
		this.ia = ia;
	}
	
	public boolean display(){
		
		try{
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "IaNamer.fxml"));
			Scene scene = new Scene(root, 450, 110);
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Name the interaction");
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
			
			okay = (Button) scene.lookup("#createButton");
			cancel = (Button) scene.lookup("#cancelButton");
			name = (TextField) scene.lookup("#textField");
			
			name.setPromptText("Enter name of interaction");
			
			okay.setOnAction(e -> {
				flag = true;
				ia.setName(name.getText());
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
}
