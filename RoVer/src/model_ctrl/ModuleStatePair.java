package model_ctrl;

public class ModuleStatePair {
	
	public String mod;
	public int state;
	
	public ModuleStatePair(String mod, int state) {
		this.mod = mod;
		this.state = state;
	}
	
	public String toString() {
		String str = "";
		
		str += mod + "=" + state;
		
		return str;
	}

}
