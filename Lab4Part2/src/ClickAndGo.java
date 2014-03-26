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

	private static Mode mode = Mode.Wait;

	static MotorPort leftMotor = MotorPort.C;
	static MotorPort rightMotor = MotorPort.A;

	public static ClickAndGo NXTrc;

	public static TrackerReader tracker;
	
	public static JLabel modeLbl;
	public static JLabel commands;
	public static ButtonHandler bh = new ButtonHandler();
	
	private static int kp = 300;
	private static int ki = 0;
	private static int kd = 0;
	
	private static int speedmult = 1;
	private static double currentmult = 1.0;
	
	private static int offset = 33;
	private static int targetpower = 10;
	
	private static int integral = 0;
	private static int oldError = 0;

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

		String cmds = "<html>Buttons:<br>" 
				+ "f: Follow the line" + "<br>"
				+ "s: Stop<br>" 
				+ "<br>" 
				+ "q: Quit</html>";

		commands = new JLabel(cmds);
		p.add(commands);
		
		add(p);
	}

	public static void main(String[] args) {
		NXTrc = new ClickAndGo();
		NXTrc.setVisible(true);
		NXTrc.requestFocusInWindow();
		NXTrc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
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
	
	private static void followLine() {
		// IMPLEMENT P, PI, PD, or PID HERE!
		
		/* Idea: Each update perform the P-Whatever for movement, then 
		 * 			check to see if Jockey is (within a tolerance) at the
		 * 			target point. If yes, get next point from tracker.points
		 * 			list and continue
		 * 
		 * 	- Will probably need a "start" and "goal" point so a line can be
		 * 		calculated. That line can give error values for P-Whatever. 
		 */
		
		// Jockey's x, y coordinates
        double trackerx = tracker.x;
        double trackery = tracker.y;
        
	}
	
	private static void resetValues() {
		integral = 0;
		oldError = 0;
	}

	// Stahps.
	private static void stahp() {
		leftMotor.controlMotor(100, BasicMotorPort.STOP);
		rightMotor.controlMotor(100, BasicMotorPort.STOP);
	}

	private static class ButtonHandler implements MouseListener, KeyListener {

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
			NXTrc.requestFocusInWindow();
		}

		// ***********************************************************************
		// Keyboard action
		public void keyPressed(KeyEvent ke) {
			char key = ke.getKeyChar();

			switch (key) {
			case 'f':
				mode = Mode.FollowLine;
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

		public void keyTyped(KeyEvent ke) {}

		public void keyReleased(KeyEvent ke) {}
	}
}
