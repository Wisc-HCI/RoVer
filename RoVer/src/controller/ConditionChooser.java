package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Interaction;

public class ConditionChooser {
	
	private Button newAssisted, newNonassisted;
	private ComboBox chooser;
	private Interaction ia;
	private MainController mc;
	
	public ConditionChooser(Interaction ia, MainController mc) {
		this.ia = ia;
		this.mc = mc;
	}
	
	private boolean flag;
	
	@SuppressWarnings("restriction")
	public boolean display(){
		
		try{
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "ConditionChooser.fxml"));
			Scene scene = new Scene(root, 319, 127);
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Experimental Conditions");
			window.setResizable(false);
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
		
			newAssisted = (Button) scene.lookup("#newAssisted");
			newNonassisted = (Button) scene.lookup("#newNonassisted");
			chooser = (ComboBox) scene.lookup("#chooser");
			
			chooser.getItems().add("Delivery");
			chooser.getItems().add("Instruction-Action");
			chooser.getItems().add("Tutorial");
			chooser.getItems().add("None");
			
			newAssisted.setOnAction(e -> {
				flag = true;
				handleAction(window);
			});
			
			newNonassisted.setOnAction(e -> {
				flag = false;
				handleAction(window);
			});
			window.showAndWait();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return flag;
	}
	
	private void handleAction(Stage window) {
		if (chooser.getValue() == null || ((String) chooser.getValue()).equals("None"))
			return;
		
		if (((String)chooser.getValue()).equals("Tutorial"))
			ia.setTutorial(true);
		else {
			ia.setTutorial(false);
			ia.setCurrDesign((String)chooser.getValue());
		}
		window.close();
	}
}

