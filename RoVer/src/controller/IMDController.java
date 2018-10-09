package controller;
import java.io.File;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

/*
 * Import Microinteraction Dialogue Controller class
 * 
 * Used to populate and control the UI elements of the relevant dialogue
 * 
 * returns the name of the selected file
 * 
 * TODO: THis class needs to be deleted once the drag and drop feature between the library and the microcollections is implemented completely
 * Also delete the associated FXML file
 */


public class IMDController {
	
	private Button importMicro, cancel;
	private TreeView<File> treeView;
	private String fileToImport;
	
	
	@SuppressWarnings("unchecked")
	public String importMicroInteraction(){
		
		try{
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("resources" +
		File.separator + "ImportMicrointeractionDialogue.fxml"));
			Scene scene = new Scene(root, 500, 350);
			Stage window = new Stage();
			window.setScene(scene);
			window.setTitle("Import Microinteraction");
			window.initModality(Modality.APPLICATION_MODAL);
			window.initStyle(StageStyle.UTILITY);
			
			window.setOnCloseRequest(e -> {
				fileToImport = "NaN";
			});
			
			importMicro = (Button) scene.lookup("#importButton");
			cancel = (Button) scene.lookup("#cancelButton");
			treeView = (TreeView<File>) scene.lookup("#treeView");
			
			File dir = new File(System.getProperty("user.dir") + File.separator + "Lib");
			
			treeView.setRoot(buildFileSys(dir, null));
			treeView.setShowRoot(false);
						
			treeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {

			    public TreeCell<File> call(TreeView<File> tv) {
			        return new TreeCell<File>() {

			            @Override
			            protected void updateItem(File item, boolean empty) {
			                super.updateItem(item, empty);

			                setText((empty || item == null) ? "" : item.getName());
			                
			                setOnMouseClicked(me -> {
			                	fileToImport = ((empty || item == null) ? "" : item.getName());
			                });
			                
			            }

			        };
			    }
			});
			
			importMicro.setOnAction(e -> {
				fileToImport = "Lib" + File.separator + fileToImport;
				window.close();
			});
			
			cancel.setOnAction(e -> {
				fileToImport = "NaN";
				window.close();
			});
			window.showAndWait();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		
		return fileToImport;
	}
	
	private TreeItem<File> buildFileSys(File dir, TreeItem<File> parent) {
			TreeItem<File> root = new TreeItem<>(dir);
			root.setExpanded(false);
			
			for (File file : dir.listFiles()) {
				if (file.isDirectory()) {
					buildFileSys(file, root);
				} else{
					root.getChildren().add(new TreeItem<>(file));
				}
			}
			if (parent == null) {
				return root;
			} else{
				parent.getChildren().add(root);
			}
		return null;
	}
}
