package controller;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.Label;

public class InfoPanel {

	private Label info;
	
	private Button okay;
	private boolean flag;
	private String message;
	
	public InfoPanel(String message){
		this.message = message;
	}
	
	public boolean display(){
		
		try{
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "InfoPanel.fxml"));
			Scene scene = new Scene(root, 319, 135);
			Stage window = new Stage();
			window.setScene(scene);
			window.setResizable(false);
			window.setTitle("Info");
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
			
			okay = (Button) scene.lookup("#okaybutton");
			info = (Label) scene.lookup("#info");
			
			info.setText(message);
			
			okay.setOnAction(e -> {
				flag = true;
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
