package enums;

public class String2StateClass {
	
	public static StateClass findStateClass(String str) {
		StateClass sc = null;
		
		switch(str) {
		case "START":
			sc = StateClass.START;
			break;
		case "END":
			sc = StateClass.END;
			break;
		case "INTERACTING":
			sc = StateClass.INTERACTING;
			break;
		case "READY":
			sc = StateClass.READY;
			break;
		case "IGNORE":
			sc = StateClass.IGNORE;
			 break;
		case "BUSY":
			sc = StateClass.BUSY;
			break;
		case "SILENT":
			sc = StateClass.SILENT;
			break;
		case "SPEAKING":
			sc = StateClass.SPEAKING;
			break;
		case "LAUGHING":
			sc = StateClass.LAUGHING;
			break;
		case "REFERENTIAL":
			sc = StateClass.REFERENTIAL;
			break;
		case "AFFILIATIVE":
			sc = StateClass.AFFILIATIVE;
			break;
		case "ELSEWHERE":
			sc = StateClass.ELSEWHERE;
			break;
		case "NORMAL":
			sc = StateClass.NORMAL;
			break;
		case "CLOSE":
			sc = StateClass.CLOSE;
			 break;
		case "FAR":
			sc = StateClass.FAR;
			break;
		case "MOVING":
			sc = StateClass.MOVING;
			break;
		case "LOCOMOTION_STILL":
			sc = StateClass.LOCOMOTION_STILL;
			break;
		case "LOOKAT":
			sc = StateClass.LOOKAT;
			break;
		case "LOOKAWAY":
			sc = StateClass.LOOKAWAY;
			break;
		case "GLANCEAT_REFERENCE":
			sc = StateClass.GLANCEAT_REFERENCE;
			break;
		case "GLANCEAT_NONREFERENCE":
			sc = StateClass.GLANCEAT_NONREFERENCE;
			break;
		case "GLANCEAT_TARGET":
			sc = StateClass.GLANCEAT_TARGET;
			break;
		case "RETRACTED":
			sc = StateClass.RETRACTED;
			 break;
		case "EXTENDED_HANDOFF":
			sc = StateClass.EXTENDED_HANDOFF;
			break;
        case "EXTENDED_HANDSHAKE":
            sc = StateClass.EXTENDED_HANDSHAKE;
            break;
		case "GESTURE_DIECTIC":
			sc = StateClass.GESTURE_DIECTIC;
			break;
		case "GESTURE_ICONIC_METAPHORIC":
			sc = StateClass.GESTURE_ICONIC_METAPHORIC;
			break;
		case "GESTURE_BEAT":
			sc = StateClass.GESTURE_BEAT;
			break;
		case "HEAD_STILL":
			sc = StateClass.HEAD_STILL;
			break;
		case "NOD":
			sc = StateClass.NOD;
			break;
		case "SHAKE":
			sc = StateClass.SHAKE;
			break;
		default:
			break;
		}
			
		return sc;
	}

}
