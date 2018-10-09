package model;

public class Expression {

	private Variable var;
	private String sign;
	private String update;
	
	public Expression(Variable var, String sign, String update) {
		this.var = var;
		this.sign = sign;
		this.update = update;
	}
	
	public Variable getVar() {
		return var;
	}
	
	public String getSign() {
		return sign;
	}
	
	public String getUpdate() {
		return update;
	}
	
}
