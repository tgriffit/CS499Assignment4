import java.awt.Color;
import java.awt.Panel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;

import lejos.nxt.*;

public class ClickAndGo extends JFrame {

	private static final long serialVersionUID = 1L;

	private enum Mode {
		Stop, Wait, FollowLine
	}

	private Mode mode = Mode.Wait;

	MotorPort leftMotor = MotorPort.C;
	MotorPort rightMotor = MotorPort.A;

	public TrackerReader tracker;

	public JLabel modeLbl;
	public JLabel commands;
	public ButtonHandler bh = new ButtonHandler();

	private int kp = 300;
	private int ki = 0;
	private int kd = 200;

	private int targetpower = 10;

	private int integral = 0;
	private int oldError = 0;

	private Point lineStart;
	private Point lineEnd;
	private int pointsIndex = 0;

	public ClickAndGo() {
		// start the tracker.py interface
		tracker = new TrackerReader();
		tracker.start();

		Panel p = new Panel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		setTitle("Jockey Self-Control");
		setBounds(400, 350, 300, 200);
		addMouseListener(bh);
		addKeyListener(bh);

		modeLbl = new JLabel();
		modeLbl.setForeground(Color.BLUE);
		p.add(modeLbl);

		String cmds = "<html>Buttons:<br>" + "f: Follow the line" + "<br>"
				+ "s: Stop<br>" + "<br>" + "q: Quit</html>";

		commands = new JLabel(cmds);
		p.add(commands);

		add(p);

		setVisible(true);
		requestFocusInWindow();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		while (mode != Mode.Stop) {
			switch (mode) {

			case FollowLine:
				followLine();
				break;

			case Wait:
			default:
				stahp();
				break;
			}
		}

		stahp();

		System.exit(0);
	}

	public static void main(String[] args) {
		ClickAndGo c = new ClickAndGo();
	}

	private void followLine() {
		// Jockey's x, y coordinates
		double trackerx = tracker.x;
		double trackery = tracker.y;
		Point pos = new Point(trackerx, trackery);

		// if close enough to the line end, start on next line
		if (closeToLineEnd(pos)) {
			System.out.println("Close to lineEnd");
			pointsIndex++;
			// if there isn't another line, exit!
			if (pointsIndex == tracker.points.size())
			{
				mode = Mode.Stop;
				return;
			} else {
				// swap to next line in the path!
				lineStart = lineEnd;
				lineEnd = tracker.points.get(pointsIndex);
			}
		}

		// now do the PID controlled movement
		doPID(pos);
	}

	private void doPID(Point pos) {
		int error = (int) (whichSide(lineStart, lineEnd, pos) * ptToLineDist(
				lineStart, lineEnd, pos));
		integral += error;
		int derivative = error - oldError;
		int turn = kp * error + ki * integral + kd * derivative;
		turn /= 100;
		System.out.println("Error: " + error);
		driveProportionally(targetpower, turn);
		oldError = error;
	}

	private boolean closeToLineEnd(Point pos) {
		int ptTolerance = 10;
		if (ptToPtDist(pos, lineEnd) < ptTolerance) 
			return true;
		return false;
	}

	private void driveProportionally(int power, int turn) {
		int leftpower = power - turn;
		int rightpower = power + turn;
		
		// clamp the power levels to prevent tipping
		if (leftpower > 15) leftpower = 15;
		if (leftpower < -15) leftpower = -15;
		if (rightpower > 15) rightpower = 15;
		if (rightpower < -15) rightpower = -15;
		
		leftMotor.controlMotor(leftpower, BasicMotorPort.FORWARD);
		rightMotor.controlMotor(rightpower, BasicMotorPort.FORWARD);
	}

	private void resetValues() {
		integral = 0;
		oldError = 0;
	}

	// Calculate the distance from a point P to a line segment A-B
	// -> credit: http://www.ahristov.com/tutorial/geometry-games/point-line-distance.html
	public double ptToLineDist(Point A, Point B, Point P) {
		double normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y)
				* (B.y - A.y));
		double ret = Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x))
				/ normalLength;
		
		// get dists from pos P to ends to ensure 
		double distA = ptToPtDist(A, P);
		double distB = ptToPtDist(B, P);
		
		return Math.min(Math.min(ret, distA), distB);
	}

	// Calculate distance between two points
	// -> credit: http://wikicode.wikidot.com/get-distance-between-two-points
	public static double ptToPtDist(Point p1, Point p2) {
		double dX = p1.x - p2.x;
		double dY = p1.y - p2.y;
		return Math.sqrt(dX * dX + dY * dY);
	}

	// Calculate which side of a line a point is, -1 is one side and 1 is else
	// -> credit: modified based on http://stackoverflow.com/a/3461533
	public int whichSide(Point a, Point b, Point c) {
		double value = ((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x));

		return (value < 0) ? -1 : 1;
	}

	// Stahps.
	private void stahp() {
		leftMotor.controlMotor(100, BasicMotorPort.STOP);
		rightMotor.controlMotor(100, BasicMotorPort.STOP);
	}

	private class ButtonHandler implements MouseListener, KeyListener {

		public void mouseClicked(MouseEvent arg0) {
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent moe) {
		}

		public void mouseReleased(MouseEvent moe) {
			// If you click on the window it should remove focus from the text
			// fields (allowing us to use keyboard commands again)
			requestFocusInWindow();
		}

		// Keyboard action
		public void keyPressed(KeyEvent ke) {
			char key = ke.getKeyChar();

			switch (key) {
			case 'f':
				mode = Mode.FollowLine;
				// if Jockey is close enough to the lineEnd, start next line
				lineStart = new Point(tracker.x, tracker.y);
				lineEnd = tracker.points.get(0);
				break;
			case 's':
				mode = Mode.Wait;
				break;
			case 'q':
				mode = Mode.Stop;
				break;
			}

			resetValues();
		}

		public void keyTyped(KeyEvent ke) {
		}

		public void keyReleased(KeyEvent ke) {
		}
	}
}
