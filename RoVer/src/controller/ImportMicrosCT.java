package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import model.MicroBox;
import model_ctrl.FSManager;

/*
 * Class to view and add microinteractions to microcollections in the project
 */
public class ImportMicrosCT extends VBox {

	//@FXML
	//private TextField searchBar;
	//@FXML
	//private Button searchButton;
	//@FXML
	private ComboBox<String> selectCatagory;
	@FXML
	private GridPane viewMicros;

	private MainController mc;
	private ArrayList<MicroBox> allMicros;
	private String currItem = null;

	public ImportMicrosCT(MainController mc) {

		//Set the layout
		this.mc = mc;
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getClassLoader().getResource("resources" + File.separator + "ImportMicroPane.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		//Set the style sheet
		getStylesheets().add(this.getClass().getClassLoader()
				.getResource("resources" + File.separator + "SearchBox.css").toExternalForm());

		//viewMicros.setPadding(new Insets(50, 50, 50, 50));
		viewMicros.prefWidthProperty().bind(this.widthProperty());
		viewMicros.prefHeightProperty().bind(this.heightProperty());

		initialize();

	}

	private void initialize() {

		FSManager fsm = new FSManager();
		
		//Searches through all the micros to and displays the ones' containing a the search string
		/*searchBar.textProperty().addListener(e -> {
			String text = searchBar.getText();
			if (!text.equals("")) {
				searchButton.setStyle(
						"-fx-background-image: url('@../../Icons/clearSearch.png');" + " -fx-background-size: 12px;");
				populateGrid(fsm, "SearchBox", searchBar.getText());
			} else
				searchButton.setStyle(
						"-fx-background-image: url('@../../Icons/search.png');" + " -fx-background-size: 15px;");
		});

		searchButton.setOnAction(e -> {
			searchBar.setText("");
		});*/

		//Add the All option to the categories menu
		selectCatagory = new ComboBox();
		selectCatagory.getItems().add("All");
		//Create an arraylist containing all the micros in the library
		allMicros = fsm.getAllMicros(this);

		//Populate the categories menu with all the different types of micros in the library
		/*for (File file : fsm.getMicroDirs()) {
			if (!file.getName().equals(".DS_Store") && !file.getName().equals("Supreme")) {
				selectCatagory.getItems().add(file.getName());
			}
		}*/
		//Add a listener for the categories menu items
		selectCatagory.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			populateGrid(fsm, newValue, "");
			currItem = newValue;
		});

		//Rearrange the microboxes depending when the window is resized
		widthProperty().addListener(e -> {
			if (currItem != null) {
				populateGrid(fsm, currItem, "");
			}
		});
		
		populateGrid(fsm, "All", "");
	}

	//Get the dimensions of the tab
	private int[] getDimensions() {
		//int prefWidth = 300, prefHeight = 75;
		int dimensions[] = new int[2];

		dimensions[0] = 1;
		dimensions[1] = 8;

		return dimensions;
	}

	//Removes all the microboxes from the gridpane
	private void clearGrid() {
		viewMicros.getChildren().clear();
	}

	//Adds the  microboxes to the gridpane 
	private void populateGrid(FSManager fsm, String catagory, String search) {
		int[] dimensions = getDimensions();
		ArrayList<MicroBox> microsList;
		int pos = 0;
		if (catagory.equals("All")) {
			clearGrid();
			microsList = allMicros;
		}

		else if (catagory.equals("SearchBox")) {
			microsList = new ArrayList<>();
			clearGrid();
			for (MicroBox mb : allMicros) {
				if (mb.getName().toLowerCase().contains(search.toLowerCase())) {
					microsList.add(mb);
				}
			}
		}

		else {
			clearGrid();
			String path = fsm.getPath() + File.separator + "Lib" + File.separator + catagory;
			Color color = colorPick(selectCatagory.getItems().indexOf(catagory) - 1);
			microsList = fsm.getMicrosInDir(new File(path), color);
		}

		while (dimensions[0] * dimensions[1] <= microsList.size()) {
			dimensions[1] += 1;
		}

		for (int i = 0; i < dimensions[1] && pos < microsList.size(); i++) {
			for (int j = 0; j < dimensions[0] && pos < microsList.size(); j++) {
				MicroBox mb = microsList.get(pos);
				addMBoxEventHandler(mb);
				if (!mb.getName().equals("Start.xml") && !mb.getName().equals("End.xml"))
					viewMicros.add(mb, j, i);
				pos++;
			}
		}

	}

	//Used to set the color of the microbox to correspond with it's category for easy identification
	public Color colorPick(int pos) {

		switch (pos) {
		case 1:
			return Color.LIGHTPINK;
		case 2:
			return Color.LIGHTSEAGREEN;
		case 3:
			return Color.LIGHTBLUE;
		case 4:
			return Color.LIGHTYELLOW;
		case 5:
			return Color.LIGHTGRAY;
		case 6:
			return Color.ANTIQUEWHITE;
		case 7:
			return Color.PALEVIOLETRED;
		case 8:
			return Color.ALICEBLUE;
		default:
			return Color.AQUAMARINE;
		}

	}
	
	//Find a microbox by id
	public MicroBox getMBbyID(String name) {
		for(MicroBox mb: allMicros) {
			if(mb.getFile().getAbsolutePath().equals(name)) {
				MicroBox mbCopy = new MicroBox(mb.getFile(), mb.getType(), colorPick(selectCatagory.getItems().indexOf(mb.getType())-1), mb);
				return mbCopy;
			}
			
		}
		return null;
	}
	
	public File getMBFileByKeyword(String name) {
		for(MicroBox mb: allMicros) {
			if(mb.getFile().getAbsolutePath().contains(name)) {
				return mb.getFile();
			}
			
		}
		return null;
	}
	
	public MicroBox getRandomMB() {
		Random rand = new Random();
		MicroBox mb = allMicros.get(rand.nextInt(allMicros.size()));
		MicroBox mbCopy = new MicroBox(mb.getFile(), mb.getType(), colorPick(selectCatagory.getItems().indexOf(mb.getType())-1), mb);
		return mbCopy;
	}

	//Used to implement the drag drop functionality between the MBox and Group
	private void addMBoxEventHandler(Node node) {
		node.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				
				Dragboard db = node.startDragAndDrop(TransferMode.COPY);
				WritableImage snapshot = ((MicroBox) node).snapshot(new SnapshotParameters(), null);
				db.setDragView(snapshot);
				ClipboardContent content = new ClipboardContent();
				content.putString("TransferMicro");
				List<File> list = new ArrayList<>();
				list.add(((MicroBox) node).getFile());
				content.putFiles(list);
				db.setContent(content);

				event.consume();
			}

		});
	}
}
