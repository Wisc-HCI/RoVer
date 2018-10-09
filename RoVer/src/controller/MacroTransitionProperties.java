package controller;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.GroupTransition;
import study.BugTracker;

public class MacroTransitionProperties {
	
	//@FXML
	private boolean flag;
	private CheckBox ready_check, busy_check, suspended_check;
	private Button okay, cancel;
	private GroupTransition macro;
	
	// for bug tracking
	BugTracker bt;
	
	public MacroTransitionProperties(GroupTransition macro, BugTracker bt) {
		this.macro = macro;
		this.bt = bt;
	}
	
	@SuppressWarnings({ "restriction", "unchecked" })
	public boolean display(){
		
		try{
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "MacroTransProps.fxml"));
			Scene scene = new Scene(root, 508, 174);
			Stage window = new Stage();
			window.setResizable(false);
			window.setScene(scene);
			window.setTitle("Branch Conditions");
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
						
			boolean[] humanBranching = macro.getHumanBranching();
			boolean[] breakdownBranching = macro.getBreakdownBranching();
			boolean elBranching = macro.getElse();
			
			ready_check = (CheckBox) scene.lookup("#ready_check");
			ready_check.setSelected(false);
			busy_check = (CheckBox) scene.lookup("#busy_check");
			busy_check.setSelected(false);
			suspended_check = (CheckBox) scene.lookup("#suspended_check");
			suspended_check.setSelected(false);
			
			okay = (Button) scene.lookup("#okay");
			cancel = (Button) scene.lookup("#cancel");
			
			boolean[] conditionals = macro.getHumanBranching();
			
			if (conditionals[0])
				ready_check.setSelected(true);
			if (conditionals[1])
				busy_check.setSelected(true);
			if (conditionals[2])
				suspended_check.setSelected(true);
			
			cancel.setOnAction(e -> {
				flag = false;
				window.close();
			});
			
			okay.setOnAction(e -> {
				if (ready_check.isSelected())
					conditionals[0] = true;
				else
					conditionals[0] = false;
				if (busy_check.isSelected())
					conditionals[1] = true;
				else
					conditionals[1] = false;
				if (suspended_check.isSelected())
					conditionals[2] = true;
				else
					conditionals[2] = false;
				
				macro.getConditions().update(conditionals);
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