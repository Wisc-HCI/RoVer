package checkers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import controller.MainController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.Group;
import repair.Fix;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.TreeItem;

public class Conflict extends TreeItem<HBox> {
	
	TextFlow description;
	ComboBox choice;
	
	// images for property violations
	private Image violation;
	private Image warning;
	private Image good;
	private Image greeting;
	private Image farewell;
	private Image task;
	private Image instruct;
	private Image sequential;
	private Image speech;
	private Image branch;
	private Image turntake;
	private Image other;
	private Image waiting;
	
	// helper image
	private Image helperImage;
	
	private ModBehPair mbp;
	private Group group;
	private Property prop;
	private ArrayList<String> choices;
	
	private MainController mc;
	
	private String describe;
	
	// hbox for storing each component
	private HBox components;
	
	@SuppressWarnings({ "restriction", "unchecked" })
	public Conflict(Property prop, String describe, String type, ArrayList<String> choices, ModBehPair mbp, Group group, MainController mc, Image helperImage, Image icon) {
		this.mc = mc;
		this.group = group;
		this.mbp = mbp;
		this.prop = prop;
		this.describe = describe;
		this.choices = choices;
		this.components = new HBox();
		components.setStyle("-fx-padding: 10 0 0 10;");
		
		components.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
         	
             @Override
             public void handle(MouseEvent event) {
             }
             
         });
         
         components.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
         	
             @Override
             public void handle(MouseEvent event) {
             }
             
         });
		
		//TreeItem<HBox> root = new TreeItem<HBox>(components);
		//this.setRoot(root);
		this.setValue(components);
				
		// add fixes to the tree, if applicable
		if (prop!=null) {
			for (Fix fix : prop.getFixes()) {
				HBox fixBox = new HBox();
				fixBox.setStyle("-fx-padding: 10 0 0 10;");
				Text txt = new Text(fix.getDescription());
				txt.setFont(Font.font("Veranda", FontWeight.LIGHT, 11));
				TextFlow fixDescription = new TextFlow(txt);
				fixDescription.setMaxWidth(210);
				fixBox.getChildren().add(fixDescription);
				this.getChildren().add(new TreeItem<HBox>(fixBox));
				
				Button fixer = new Button("fix");
				fixer.setStyle("-fx-font-size: 8px");
				fixer.setOnAction(new EventHandler<ActionEvent>() {
		            @Override
		            public void handle(ActionEvent event) {
		            	Platform.runLater(
		    					() -> {
		    		                mc.repairInteraction(fix.getID(), group, false);
		    					}
		    				);}
		        });
				
				fixBox.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
		         	
		             @Override
		             public void handle(MouseEvent event) {
		             	Platform.runLater(
		    					() -> {
		    		                //mc.repairInteraction(fix.getID(), group, true);
		    					}
		    				);}
		             }
		             
		         );
		         
		         fixBox.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
		         	
		             @Override
		             public void handle(MouseEvent event) {
		             	Platform.runLater(
		    					() -> {
		    		                //mc.removeRepairPreview();
		    					}
		    				);
		             }
		             
		         });
				
				//fixBox.getChildren().add(fixer);
			}
		}
				
		String path = "Icons" + File.separator;
		this.helperImage = helperImage;
		try {
			violation = new Image(new FileInputStream(path + "violation.png"));
			warning = new Image(new FileInputStream(path + "warning.png"));
			good = new Image(new FileInputStream(path + "Icon_Check.png"));
			path += "property_icons" + File.separator;
			greeting = new Image(new FileInputStream(path + "greeting.png"));
			farewell = new Image(new FileInputStream(path + "farewell.png"));
			task = new Image(new FileInputStream(path + "task.png"));
			instruct = new Image(new FileInputStream(path + "instruct.png"));
			sequential = new Image(new FileInputStream(path + "sequential.png"));
			speech = new Image(new FileInputStream(path + "speech_flubs.png"));
			branch = new Image(new FileInputStream(path + "branch.png"));
			turntake = new Image(new FileInputStream(path + "turntake.png"));
			waiting = new Image(new FileInputStream(path + "waiting.png"));
			other = new Image(new FileInputStream(path + "other.png"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		components.setSpacing(10);
		
		String[] describeComponents = describe.split("FIX:");
		if (describeComponents.length > 1) {
			ArrayList<Text> txts = new ArrayList<Text>();
			for (String str : describeComponents) {
				Text txt = new Text(str);
				txt.setFont(Font.font("Veranda", FontWeight.LIGHT, 11));
				txts.add(txt);
			}
			Text fixText = new Text("FIX: ");
			fixText.setFont(Font.font("Veranda", FontWeight.EXTRA_BOLD, 11));

			description = new TextFlow(txts.get(0), fixText, txts.get(1));
		}
		else {
			Text tempDescription = new Text(describe);
			tempDescription.setFont(Font.font("Verdana", FontWeight.LIGHT, 11));
			tempDescription.setWrappingWidth(300);
			description = new TextFlow(tempDescription);
		}
		description.setMaxWidth(200);
		
		
		if (choices != null) {
			choice = new ComboBox();
			Text txt = null;
			if (mbp.getFix() == null)
				txt = new Text("set behavior");
			else
				txt = new Text(mbp.getFix());
			
			txt.setFont(Font.font("Verdana", FontWeight.LIGHT, 10));
			choice.setValue(txt);
			choice.setStyle(".combo-box > .list-cell {" +
					"-fx-padding: 0 0 0 0;" +
    				"-fx-border-insets: 0 0 0 0;" +
				"}");
			choice.setMaxWidth(120);
			description.setMaxWidth(130);
			
			for (String str : choices) {
				Text t = new Text(str);
				t.setFont(Font.font("Verdana", FontWeight.LIGHT, 10));
				choice.getItems().add(t);
			}
			if (type.equals("canfix")) {
				Canvas warningCanvas = new Canvas(15,15);
				warningCanvas.getGraphicsContext2D().drawImage(warning, 0, 0, 15, 15);
				components.getChildren().add(warningCanvas);
			}
			else {
				Canvas goodCanvas = new Canvas(15,15);
				goodCanvas.getGraphicsContext2D().drawImage(good, 0, 0, 15, 15);
				components.getChildren().add(goodCanvas);
			}
			components.getChildren().add(choice);
			
			choice.valueProperty().addListener(new ChangeListener<Text>() {
		        @Override public void changed(ObservableValue ov, Text t, Text t1) {
		          if (mbp.getFix() == null) {
		        	  group.setFixed(mbp);
		        	  mc.verifyConcurrentAndGraph();
		          }
		          mbp.addFix(t1.getText());
		        }    
		    });
			
		}
		else {
			Canvas violationCanvas = new Canvas(20,20);
			if (type.equals("nofix") && prop != null) 
				violationCanvas.getGraphicsContext2D().drawImage(prop.getIcon(), 0, 0, 15, 15);
			else if (type.equals("nofix")) {
				if (describe.equals("Greeting Flubs"))
					violationCanvas.getGraphicsContext2D().drawImage(greeting, 0, 0, 20, 20);
				else if (describe.equals("Farewell Flubs"))
					violationCanvas.getGraphicsContext2D().drawImage(farewell, 0, 0, 20, 20);
				else if  (describe.equals("Answer Flubs"))
					violationCanvas.getGraphicsContext2D().drawImage(task, 0, 0, 20, 20);
				else if  (describe.equals("Instruction Flubs"))
					violationCanvas.getGraphicsContext2D().drawImage(instruct, 0, 0, 20, 20);
				else if  (describe.equals("Jams"))
					violationCanvas.getGraphicsContext2D().drawImage(sequential, 0, 0, 20, 20);
				else if  (describe.equals("Speech Flubs") || describe.contains("interrupt"))
					violationCanvas.getGraphicsContext2D().drawImage(speech, 0, 0, 20, 20);
				else if (describe.equals("Branching Errors"))
					violationCanvas.getGraphicsContext2D().drawImage(branch, 0, 0, 20, 20);
				else if (describe.equals("Turn-taking Flubs"))
					violationCanvas.getGraphicsContext2D().drawImage(turntake, 0, 0, 20, 20);
				else if (describe.equals("Waiting Flubs"))
					violationCanvas.getGraphicsContext2D().drawImage(waiting, 0, 0, 20, 20);
				else if (describe.equals("Task-Related Errors")) 
					violationCanvas.getGraphicsContext2D().drawImage(task, 0, 0, 20, 20);
				else
					violationCanvas.getGraphicsContext2D().drawImage(other, 0, 0, 20, 20);
			}
			else
				violationCanvas.getGraphicsContext2D().drawImage(warning, 0, 0, 15, 15);
			components.getChildren().add(violationCanvas);
		}
		
		// lastly, decide whether to add a helperImage or not
		if (this.helperImage != null) {
			// add the helper image
			VBox descriptionComponents = new VBox();
			descriptionComponents.getChildren().add(description);
			Canvas helperImageCanvas = new Canvas(350,100);
			helperImageCanvas.getGraphicsContext2D().drawImage(helperImage, 0, 0, 350, 100);
			descriptionComponents.getChildren().add(helperImageCanvas);
			components.getChildren().add(descriptionComponents);
		}
		else
			components.getChildren().add(description);
	}
	
	public Group getGroup() {
		return group;
	}
	
	public String getDescription() {
		return describe;
	}
	
	public Property getProp() {
		return prop;
	}
	
	public ArrayList<String> getChoices() {
		return this.choices;
	}
}