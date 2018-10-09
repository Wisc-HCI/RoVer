package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class ExperimentController {
	
	private Button assisted, nonassisted, testButton;
	private ComboBox experimentChooser;
	private TextField pid;
	private TextField ip;
	private ArrayList<String> experimentInfo;
	private Interaction ia;
	private MainController mc;
	
	public ExperimentController(Interaction ia, MainController mc) {
		this.experimentInfo = experimentInfo;
		this.ia = ia;
		this.mc = mc;
	}
	
	private boolean flag;
	
	@SuppressWarnings("restriction")
	public boolean display(){
		
		try{
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "Experiment.fxml"));
			Scene scene = new Scene(root, 319, 216);
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Experimental Conditions");
			window.setResizable(false);
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
		
			assisted = (Button) scene.lookup("#assisted");
			nonassisted = (Button) scene.lookup("#nonassisted");
			testButton = (Button) scene.lookup("#testButton");
			experimentChooser = (ComboBox) scene.lookup("#experimentChooser");
			ip = (TextField) scene.lookup("#ip");
			pid = (TextField) scene.lookup("#pid");	
			
			// autopopulate the ip field
			BufferedReader br = new BufferedReader(new FileReader("/home/parallels/Documents/Verification_working/Verification/IP.txt"));
			try {
			    StringBuilder sb = new StringBuilder();
			    String line = br.readLine();
			    ip.setText(line);
			} finally {
			    br.close();
			}
			
			experimentChooser.getItems().add("Instruction-Action_1");
			experimentChooser.getItems().add("Instruction-Action_2");
			experimentChooser.getItems().add("Instruction-Action_3");
			experimentChooser.getItems().add("None");
			
			assisted.setOnAction(e -> {
				flag = true;
				handleAction(window);
			});
			
			nonassisted.setOnAction(e -> {
				flag = false;
				handleAction(window);
			});
			
			testButton.setOnAction(e -> {

				Process p;
				String line;
				String output = "";
				try {
					p = Runtime.getRuntime().exec("ssh nao@" + ip.getText() + " python prepareExperiment.py");         // for UW Net					
					Thread.sleep(2000);
				} catch (IOException | InterruptedException err) {
					// TODO Auto-generated catch block
					err.printStackTrace();
				}
			});
			
			window.showAndWait();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return flag;
	}
	
	private void handleAction(Stage window) {
		if (experimentChooser.getValue() == null || ip.getText().equals("") || (pid.getText().equals("") && !((String) experimentChooser.getValue()).equals("None")))
			return;
		
		mc.setPid(pid.getText());
		mc.setIP(ip.getText());
		String inst_a = (String) experimentChooser.getValue();
		if (inst_a.contains("Instruction")) {
			int designSubtype = Integer.parseInt(inst_a.substring(inst_a.length()-1));
			ia.setCurrDesign("Delivery");
			System.out.println("CURR INSTRUCTION: " + designSubtype);
			ia.setCurrInstruction(designSubtype);
		}
		else
			ia.setCurrDesign(inst_a);
		window.close();
	}
}

