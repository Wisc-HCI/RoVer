package model;


public class Sync {

	private String type;
	private Variable var;
	
	public Sync(Variable var, String type){
		this.type = type;
		this.var = var;
	}
	
	public Sync(Variable var) {
		this.var = var;
		this.type = "none";
	}
	
	public String getType(){
		return type;
	}
	
	public Variable getVar(){
		return var;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public void setVar(Variable var){
		this.var = var;
	}
	
	public boolean isSame(Sync sync) {
		boolean isSameVar = sync.getVar().equals(this.getVar());
		boolean isSameType = sync.getType().equals(this.getType());
		return isSameVar & isSameType;
	}
	
	public String stringify() {
		return var.getName();
	}
	
	public Sync copy(){
		return new Sync(var.copy(), type);
	}
	
}
