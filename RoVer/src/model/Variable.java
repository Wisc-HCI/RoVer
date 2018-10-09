package model;

import java.util.ArrayList;

public class Variable implements Comparable {
	
	private String vartype;
	private String name;
	private String bound;    // only if an integer
	private String value;
	private ArrayList<String> nominalVals;
	private ArrayList<String> arrayVals;
	private ArrayList<String> arrayValLinks;
	private String description;
	private boolean prismIgnore;
	private boolean parameterizable;
	private boolean required;
	
	public Variable(String vartype, String name){
		this.vartype = vartype;
		this.name = name;
		this.bound = null;
		this.value = null;
		this.parameterizable = false;
		this.required = false;
		this.prismIgnore = false;
		this.nominalVals = new ArrayList<String>();
		this.arrayVals = new ArrayList<String>();
		this.arrayValLinks = new ArrayList<String>();
		this.description = null;
	}
	
	public Variable(String vartype, String name, String bound){
		this.vartype = vartype;
		this.name = name;
		this.bound = bound;
		this.value = null;
		this.parameterizable = false;
		this.required = false;
		this.prismIgnore = false;
		this.nominalVals = new ArrayList<String>();
		this.arrayVals = new ArrayList<String>();
		this.arrayValLinks = new ArrayList<String>();
		this.description = null;
	}
	
	public void initialize(String value){
		// type checking
		if (vartype.equals("handshake") || vartype.equals("bsync_restricted") || vartype.equals("bsync_unrestricted")) {
			return;
		}
		this.value = value;
	}
	
	public void addDescription(String str) {
		this.description = str;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public boolean isSync() {
		if (vartype.equals("handshake") || vartype.contains("sync"))
			return true;
		return false;
	}
	
	public String getValue(){ 
		return value;
	}
	
	public String getName(){
		return name;
	}
	
	public String getType(){
		return vartype;
	}
	
	public String getBound(){
		return bound;
	}
	
	public boolean isParameterizable() {
		return parameterizable;
	}
	
	public boolean getPrismIgnore() {
		return prismIgnore;
	}
	
	public ArrayList<String> getNominalValues() {
		return nominalVals;
	}
	
	public boolean getRequired() {
		return required;
	}
	
	public ArrayList<String> getArrayVals() {
		return arrayVals;
	}
	
	public ArrayList<String> getArrayValLinks() {
		return arrayValLinks;
	}
	
	public void setValue(String value) {
		setValue(value, true);
	}
	
	public void setValue(String value, boolean changeRequired) {
		this.value = value;
		if (required && changeRequired)
			setRequired(false);
	}
	
	public void setValues(String values) {
		arrayVals.clear();
		String[] parsedVals = values.split(";");
		for (int i = 0; i < parsedVals.length; i++) {
			arrayVals.add(parsedVals[i]);
		}
		setRequired(false);
	}
	
	public void setValueLinks(String values) {
		arrayValLinks.clear();
		String[] parsedVals = values.split(";");
		for (int i = 0; i < parsedVals.length; i++) {
			arrayValLinks.add(parsedVals[i]);
		}
		setRequired(false);
	}
	
	public void addItemToArray(String value, String link) {
		arrayVals.add(value);
		arrayValLinks.add(link);
		setRequired(false);
	}
	
	public void setNominalValue(ArrayList<String> values) {
		this.nominalVals = values;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setType(String type){
		vartype = type;
	}
	
	public void setBound(String bound){
		this.bound = bound;
	}
	
	public void setParameterizable(boolean val) {
		this.parameterizable = val;
	}
	
	public void setPrismIgnore(boolean val) {
		prismIgnore = true;
	}
	
	public void setRequired(boolean val) {
		required = val;
	}
	
	public Variable copy(){
		Variable newV = new Variable(vartype, name);
		newV.setBound(bound);
		newV.setValue(value);
		if (this.prismIgnore)
			newV.setPrismIgnore(this.prismIgnore);
		return newV;
	}
	
	public String arrayToTextArea() {
		String str = "";
		
		for (String string : arrayVals) {
			str += string + ";";
		}
		
		return str;
	}

	public String toString() {
		String str = "";
		str += "name: " + this.name + ", type: " + this.vartype;
		if (this.value != null)
			str += ", value: " + this.value;
		if (this.bound != null)
			str += ", bound: " + this.bound;
		str += "\n";
		return str;
	}

	@Override
	public int compareTo(Object o) {
		return (((Variable)o).getName().compareTo(this.name));
	}

}
