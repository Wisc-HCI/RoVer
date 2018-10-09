package checkers;

import java.util.HashMap;

import model.Module;

public class PropModsBeh {
	
	public String property;
	public HashMap<Module,String> mod2beh;
	
	public PropModsBeh(String prop, HashMap<Module,String> mod2beh) {
		this.property = prop;
		this.mod2beh = mod2beh;
	}
	
}