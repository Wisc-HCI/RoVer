package model;

/*
 * Holds the different aspects of a guard
 */
public class Guard {

	private Variable var;
	private String label;
	private String sign;
	private String val;
	
//	public Guard(Variable var, String val){
//		this.var = var;
//		this.val = val;
//	}
//	
	public Guard(Variable var, String val, String sign){
		this.var = var;
		this.val = val;
		this.sign = sign;
		this.label = null;
	}
	
	public Guard(String label, String val, String sign) {
		this.label = label;
		this.val = val;
		this.sign = sign;
		this.var = null;
	}
	
	public Variable getVar(){
		return var;
	}
	
	public String getVal(){
		return val;
	}
	
	public String getSign(){
		return sign;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setVar(Variable var){
		this.var = var;
	}
	
	public void setVal(String val){
		this.val = val;
	}
	
	public boolean isSame(Guard guard) {
		boolean isSameVar = guard.getVar().equals(this.getVar());
		boolean isSameSign = guard.getSign().equals(this.getSign());
		boolean isSameVal = guard.getVal().equals(this.getVal());
		
		return isSameVar & isSameSign & isSameVal;
	}
	
	public void setSign(String sign){
		this.sign = sign;
	}
	
	public Guard copy(){
		return new Guard(var.copy(), val, sign);
	}
	
	public String stringify() {
		return var.getName() + " " + sign + " " + val;
	}
	
	public String toString() {
		String str = "guard: ";
		
		if (var != null)
			str += var.getName();
		else
			str += label;
		
		str += sign;
		str += val + "\n";
		return str;
	}
}
