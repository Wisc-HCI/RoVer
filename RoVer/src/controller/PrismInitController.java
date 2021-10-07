package controller;

import java.io.File; 
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PrismInitController {
	
	Stage window;

	public void display() {
		try{
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "PrismInitDialogue.fxml"));
			Scene scene = new Scene(root, 450, 159);
			window = new Stage();
			window.setScene(scene);
			window.setTitle("Please wait");
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
			window.show();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}

	public void close() {
		// TODO Auto-generated method stub
		window.close();
	}
	
}
