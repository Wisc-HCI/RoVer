package checkers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import model.Module;

public class ModBehPair {
	
	ArrayList<Module> mods;
	ArrayList<String> behs;
	
	String fix;
	
	boolean nonProperty;
	
	public ModBehPair(HashMap<Module,String> modstr, boolean nonProperty) {
		this.nonProperty = nonProperty;
		mods = new ArrayList<Module>();
		behs = new ArrayList<String>();
		fix = null;
		Iterator it = modstr.entrySet().iterator();
		while (it.hasNext()) {
		    HashMap.Entry pair = (HashMap.Entry)it.next();
		    mods.add((Module) pair.getKey());
		    behs.add((String) pair.getValue());
		}
	}
	
	public void addFix(String beh) {
		fix = beh;
	}
	
	public String getFix() {
		return fix;
	}
	
	public String[] getFixPair() {
		if (fix == null)
			return null;
		String[] pair = new String[2];
		String micro = null;
		
		for (int i = 0; i < size(); i++) {
			if (getBeh(i).equals(fix)) {
				micro = getMod(i).getName();
				break;
			}
		}
		
		pair[0] = micro;
		pair[1] = fix;
		
		return pair;
		
	}
	
	public boolean getNonProperty() {
		return this.nonProperty;
	}
	
	public Module getMod(int idx) {
		return mods.get(idx);
	}
	
	public String getBeh(int idx) {
		return behs.get(idx);
	}
	
	public int size() {
		return mods.size();
	}
	
	public boolean equalsOther(ModBehPair mbp) {
		if (mbp.size() != this.size())
			return false;
		
		boolean isSame = true;
		for (int i = 0; i < size(); i++) {
			boolean found = false;
			
			for (int j = 0; j < mbp.size(); j++) {
				if (this.getMod(i).getName().equals(mbp.getMod(j).getName()) && this.getBeh(i).equals(mbp.getBeh(j)))
					found = true;
			}
			
			if (!found) {
				isSame = false;
				break;
			}
		}
		return isSame;
	}
	
}