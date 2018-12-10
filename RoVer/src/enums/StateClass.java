package enums;

/*
 * TODO: Must ensure that each cluster of state classes has a 'default' value,
 * where it doesn't hold a token
 */
public enum StateClass {
	
	// robot
	START ("None"),
	END ("End"),
	INTERACTING ("None"),
	
	// human
	READY ("None"),
	BUSY ("Busy"),
	IGNORE ("Ignore"),
	
	// speech
	SILENT ("None"),
	SPEAKING ("Speech"),
	LAUGHING ("Speech"),
	
	// gaze
	REFERENTIAL ("None"),
	AFFILIATIVE ("None"),
	ELSEWHERE ("None"),
	
	// proximity
	NORMAL ("None"),
	CLOSE ("None"),
	FAR ("None"),
	
	// locomotion
	MOVING ("None"),
	LOCOMOTION_STILL ("None"),
	
	// gaze
	LOOKAT ("Gaze"),
	LOOKAWAY ("None"), 
	GLANCEAT_REFERENCE ("Gaze"),
	GLANCEAT_NONREFERENCE ("Gaze"),
	GLANCEAT_TARGET ("Gaze"),
	
	// arm
	RETRACTED ("None"),
	EXTENDED_HANDSHAKE ("Arm"),
	EXTENDED_HANDOFF ("Arm"),
	GESTURE_DIECTIC ("Arm"),
	GESTURE_ICONIC_METAPHORIC ("Arm"),
	GESTURE_BEAT ("Arm"),
	
	// head movement
	HEAD_STILL ("None"),
	NOD ("None"),
	SHAKE ("None");	
	
	private final String token;
	
	StateClass(String token) {
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}
	
	public boolean isExclusive() {
		boolean result = false;
		switch(token) {
		case ("Arm"):
			result=true;
			break;
		case ("Speech"):
			result=true;
			break;
		}
		return result;
	}
	
	public String toString() {
		return "";
		//return this.name() + " " + this.token;
	}
	
}
