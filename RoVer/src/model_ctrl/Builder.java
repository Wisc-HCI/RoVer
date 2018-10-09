package model_ctrl;
import model.Microinteraction;

public class Builder {
	
	private Microinteraction oldMicro;
	private Microinteraction newMicro;
	
	public Builder(Microinteraction oldMicro) {
		this.oldMicro = oldMicro;
		this.newMicro = new Microinteraction();
	}
	
	public Microinteraction build() {
		// temp
		newMicro = oldMicro;
		// end temp
		
		return newMicro;
	}
	
	private Microinteraction convertBsync() {
		
		return null;
	}

}
