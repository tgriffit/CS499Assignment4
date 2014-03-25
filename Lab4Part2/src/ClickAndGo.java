import lejos.util.Delay;

public class ClickAndGo {
	public static TrackerReader tracker;
	
	public static void main (String[] args) {
        tracker = new TrackerReader();
        tracker.start();
        while (true) {
            Delay.msDelay(1000);
            //System.out.println("targ: " + tracker.x + " " + tracker.y);
    		//System.out.println("last point: (" + tracker.lastpointx + "," + tracker.lastpointy + ")");
            System.out.println("Points: " + tracker.points);
        }
    }
}