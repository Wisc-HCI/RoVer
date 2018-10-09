package model_ctrl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Properties;

import controller.ErrDController;
import controller.ImportMicrosCT;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.MicroBox;

/*
 * Class that handles all file and directory related queries 
 * 
 * Initializes the file system
 * 
 * Validates all files present in the Library
 * 
 * Verifies Interaction sanity
 * 
 * Loads and sets program specifications
 */
public class FSManager {

	private final File WORKSPACE;
	private Properties prop;
	private InputStream input;
	private ArrayList<String> flagFiles;

	public FSManager() {
		//Set the current workspace. This is the application directory
		WORKSPACE = new File(System.getProperty("user.dir") + File.separator);
		prop = new Properties();
		flagFiles = new ArrayList<String>();
		//Read the properties file to set the necessary conditions for the program.
		try {
			input = new FileInputStream("Master" +File.separator+ "Configuration" +File.separator+ "config.properties");
			prop.load(input);
		} catch (FileNotFoundException e) {
			ErrDController edc = new ErrDController();
			edc.display("No Config File Found", e);
		}
		catch (IOException e){
			ErrDController edc = new ErrDController();
			edc.display("Error loading Config File", e);
		}
	}

	/*
	 * Adds all the files to be ignored when populating the project explorer
	 */
	public void initialize() {
		String str[] = prop.getProperty("flagFiles").split(" | ");
		for (int i = 0; i < str.length; i++) {
			flagFiles.add(str[i]);
		}
	}

	//Get the grid state from the properties file
	public boolean getGrid() {
		if (prop.getProperty("gridState").equals("false")) {
			return false;
		} else {
			return true;
		}
	}
	//Get the font size for annotations from the properties file
	public int getFontSize() {
		return Integer.parseInt(prop.getProperty("fontSize"));
	}
	//Get the font to be used from the properties file
	public String getFont() {
		return prop.getProperty("font");
	}

	//Get the radius for states from the properties file
	public int getStateSize() {
		return Integer.parseInt(prop.getProperty("stateSize"));
	}
	
	//Get all the microinteractions types in the Library
	public ArrayList<File> getMicroDirs(){
		ArrayList<File> microDirs = new ArrayList<>();
		File dir = new File(getPath() + File.separator + "Lib");
		for(File files: dir.listFiles()){
				microDirs.add(files);
		}

		return microDirs;
	}
	
	//Get a list of microboxes from the current directory. Used to populate the Library tab
	public ArrayList<MicroBox> getMicrosInDir(File dir, Color color){
		ArrayList<MicroBox> microConfigs = new ArrayList<>();
		
		for(File file : dir.listFiles()){
			if (!file.getName().equals(".DS_Store")) {
				String fileName = dir.getName();
				MicroBox mb = new MicroBox(file, fileName, color); 
				microConfigs.add(mb);
			}
		}

		return microConfigs;
	}
	
	//Get all the microinteractions in the lib folder as microboxes. Used in the lib tab
	public ArrayList<MicroBox> getAllMicros(ImportMicrosCT imct){
		
		ArrayList<MicroBox> allMicros = new ArrayList<>();
		
		File mainDir = new File(WORKSPACE + File.separator + "Lib");
		int pos = 0;
		
		for(File file : mainDir.listFiles()){
			if(file.isDirectory() && !file.getName().equals("Supreme") && !file.getName().contains("Proc")){
				allMicros.addAll(getMicrosInDir(file, imct.colorPick(pos)));
				pos++;
			}
		}
		
		for(File file : mainDir.listFiles()){
			if(file.getName().contains("Proc")){
				allMicros.addAll(getMicrosInDir(file, imct.colorPick(pos)));
				pos++;
			}
		}
		
		return allMicros;
	}

	//TODO Implement this method so it can write to the properties file before the program shuts down
	//Save any changes such as new font, fontsize, base state size, grid status
	public void setConfigFile() {

	}
	
	//Get the current workspace
	public String getPath(){
		return WORKSPACE.getAbsolutePath();
	}

}
