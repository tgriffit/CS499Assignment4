package JockeyControl;


public enum MovementType {
	Stop, ForwardVerySlow, ForwardSlow, ForwardFast, ForwardVeryFast, Backward, TurnRight, TurnLeft;
	
	// Weka classification gives us an integer
	public static MovementType intToMovementType(int intType) {
		switch(intType) {
		// These are used by part 1
		case 0:
			return ForwardVerySlow;
		case 1:
			return ForwardSlow;
		case 2:
			return ForwardFast;
		case 3:
			return ForwardVeryFast;
			
		// These aren't
		case 4:
			return Backward;
		case 5:
			return TurnRight;
		case 6:
			return TurnLeft;
		default:
			return Stop;
		}
	}
	
	// Movement types for arff files
	public static String movementTypes() {
		return "{ Stop, ForwardVerySlow, ForwardSlow, ForwardFast, ForwardVeryFast, Backward, TurnRight, TurnLeft }";
	}
	
	public static int getPower(MovementType mode) {
		//System.out.println(mode.toString());
		switch (mode) {
		case ForwardVerySlow:
			return 10;
		case ForwardSlow:
			return 20;
		case ForwardFast:
			return 30;
		case ForwardVeryFast:
			return 40;
		}
		
		return 30;
	}
}
