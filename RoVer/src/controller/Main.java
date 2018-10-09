package controller;
import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 * Main class for Prism GUI
 */
public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception{
		
		Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources"+ File.separator + "MainStageSparse.fxml"));
		Scene scene = new Scene(root, 1920, 1080);
		primaryStage.setScene(scene);
		//primaryStage.setFullScreen(true);
		primaryStage.setTitle("RoVer");
		primaryStage.show();
		
		// This is where the shutdown sequence needs to be called
		primaryStage.setOnCloseRequest(e -> {
			ExitController ec = new ExitController();
			if (ec.display()) {
				Platform.exit();
				System.exit(0);
			}
			else{
				e.consume();
			}
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
}

