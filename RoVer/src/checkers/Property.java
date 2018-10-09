package checkers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import javafx.scene.image.Image;

import repair.Fix;

public class Property implements Comparable{
	
	private int ID;
	private String property;
	private String ties;
	private String bugtrackID;
	private boolean initVal;
	private String description;
	private ArrayList<Fix> fixes;
	private String context;
	private Image icon;
	private String category;
	
	public Property(int ID, String property, String ties, String bugtrackID, boolean initVal, String description, String category, String context, String iconName) {
		this.ID = ID;
		this.property = property;
		this.ties = ties;
		this.bugtrackID = bugtrackID;
		this.initVal = initVal;
		this.description = description;
		this.category = category;
		this.context = context;
		this.fixes = new ArrayList<Fix>();
		this.icon = null;
		
		try {
			this.icon = new Image(new FileInputStream("Icons" + File.separator + "property_icons" + File.separator + "specific" + File.separator + iconName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * getters
	 */
	public int getID() {
		return ID;
	}
	
	public String getProperty() {
		return property;
	}
	
	public String getTies() {
		return ties;
	}
	
	public String getBugtrackID() {
		return bugtrackID;
	}
	
	public boolean getInitVal() {
		return initVal;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ArrayList<Fix> getFixes() {
		return fixes;
	}
	
	public String getCategory() {
		return category;
	}
	
	public Image getIcon() {
		return icon;
	}
	
	public String getContext() {
		return context;
	}
	
	/*
	 * adders/setters
	 */
	public void addFix(Fix fix) {
		fixes.add(fix);
	}

	@Override
	public int compareTo(Object o) {
		Property other = (Property) o;
		if (this.ID > other.getID())
			return 1;
		else if (this.ID == other.getID())
			return 0;
		else
			return -1;
	}

}
