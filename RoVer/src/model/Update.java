package model;

public class Update {

	private Variable var;
	private String assgVal;    // for simple updates
	private Expression assgExpression;    // for more complex updates
	
	public Update(Variable var, String assgVal){
		this.var = var;
		this.assgVal = assgVal;
		assgExpression = null;
	}
	
	public Update(Variable var, Variable assgVar, String sign, String updateVal) {
		this.var = var;
		this.assgVal = null;
		this.assgExpression = new Expression(assgVar, sign, updateVal);
	}
	
	public String getVal(){
		return assgVal;
	}
	
	public Expression getAssgExpression() {
		return assgExpression;
	}
	
	public Variable getVar(){
		return var;
	}
	
	public void setVal(String val){
		assgVal = val;
	}
	
	public void setVar(Variable var){
		this.var = var;
	}
	
	public Expression getExpression() {
		return assgExpression;
	}
	
	public boolean isSame(Update update) {
		boolean isSameVar = update.getVar().equals(this.getVar());
		boolean isSameVal = update.getVal().equals(this.getVal());
		return isSameVar & isSameVal;
	}
	
	public Update copy(){
		return new Update(var.copy(), assgVal);
	}
	
	public String stringify() {
		return var.getName() + " = " + assgVal;
	}
	
	public String toString() {
		String str = "update: ";
		str += var.getName() + "=" + assgVal + "\n";
		return str;
	}
}
