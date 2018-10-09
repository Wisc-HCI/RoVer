package repair;

public class Fix {
	
	private String description;
	private int ID;
	
	public Fix(String description, int ID) {
		this.description = description;
		this.ID = ID;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getID() {
		return ID;
	}

}
