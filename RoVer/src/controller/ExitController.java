package controller;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


/*
 * Controller Class for Exit Program Dialogue Box
 * 
 * Sets the necessary listeners for the Dialogue box elements
 * 
 * TODO add a checkbox to prevent this message from showing every time the application is closed. 
 * Save this condition to the properties file using the FSManage class
 */
public class ExitController {
	
	private Button okay, cancel;
	private boolean flag;
	
	@SuppressWarnings("restriction")
	public boolean display(){
		
		try{
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" + File.separator + "ExitDialogue.fxml"));
			Scene scene = new Scene(root, 450, 200);
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Confirm Exit");
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
			
			okay = (Button) scene.lookup("#exitButton");
			cancel = (Button) scene.lookup("#cancelButton");
			
			okay.setOnAction(e -> {
				flag = true;
				window.close();
				
				// remove graph and bug files, if they exist
				try {
					if ((new File("bugs.csv")).exists()) {
						Path path = Paths.get("bugs.csv");
						Files.delete(path);
					}
					if (new File("tempfile").exists()) {
						Path path = Paths.get("tempfile");
						Files.delete(path);
					}
					if (new File("tempout.txt").exists()) {
						Path path = Paths.get("tempout.txt");
						Files.delete(path);
					}
					if ((new File("graph.pm")).exists()) {
						Path path = Paths.get("graph.pm");
						Files.delete(path);
					}
					if ((new File("interaction.xml")).exists()) {
						Path path = Paths.get("interaction.xml");
						Files.delete(path);
					}
				} catch (NoSuchFileException x) {
				} catch (DirectoryNotEmptyException x) {
				} catch (IOException x) {
				    // File permission problems are caught here.
				    System.err.println(x);
				}
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
