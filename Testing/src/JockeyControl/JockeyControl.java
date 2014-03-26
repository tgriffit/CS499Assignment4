package JockeyControl;

import java.awt.Color;
import java.awt.Panel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;

import lejos.nxt.*;


public class JockeyControl extends JFrame {

	private static final long serialVersionUID = 1L;

	private enum Mode {
		Stop, Wait, BangBang, P, PI, PD, PID
	}
	
	private enum Track {
		One, Two, Three, Four
	}

	private static Mode mode = Mode.Wait;
	private static Track track = Track.One;

	static MotorPort leftMotor = MotorPort.C;
	static MotorPort rightMotor = MotorPort.A;

	static LightSensor lightSensor = new LightSensor(SensorPort.S2);
	static UltrasonicSensor rightUltrasound = new UltrasonicSensor(SensorPort.S3);
	static UltrasonicSensor frontUltrasound = new UltrasonicSensor(SensorPort.S4);
	static UltrasonicSensor backUltrasound = new UltrasonicSensor(SensorPort.S1);

	public static JockeyControl NXTrc;

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

	public JockeyControl() {
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
				+ "b: Bang Bang" + "<br>"
				+ "p: Proportional" + "<br>"
				+ "<br>"
				+ "s: Stop<br>" 
				+ "<br>" 
				+ "q: Quit</html>";

		commands = new JLabel(cmds);
		p.add(commands);

		add(p);
	}

	public static void main(String[] args) {
		NXTrc = new JockeyControl();
		NXTrc.setVisible(true);
		NXTrc.requestFocusInWindow();
		NXTrc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setKValues(track);
		
		while (mode != Mode.Stop) {
			switch (mode) {
			
			case BangBang:
				doBangBang();
				break;
			case P:
				doP();
				break;
			case PI:
				doPI();
				break;
				
			case PID:
				doPID();
				break;
			
			default:
				stahp();
				break;
			}
		}

		stahp();
		lightSensor.setFloodlight(false);

		System.exit(0);
	}
	
	private static void doBangBang() {
		if (onTape()) {
			driveForwardAndLeft(20);
		}
		else {
			driveForwardAndRight(20);
		}
	}
	
	private static void doP() {
		int lightval = lightSensor.getLightValue();
		int error = lightval - offset;
		int turn = kp * error;
		turn /= 100;
		
		//int power = (int)(targetpower * (1.0 - (error / 10.0)));
		
		//driveProportionally(turn);
	}
	
	private static void doPI() {
		int lightval = lightSensor.getLightValue();
		int error = lightval - offset;
		integral += error;
		int turn = kp*error + ki*integral;
		turn /= 100;
		
		//int power = (int)(targetpower * (1.0 - (error / 10.0)));
		
		//driveProportionally(turn);
	}
	
	private static void doPID() {
		int lightval = lightSensor.getLightValue();
		int error = lightval - offset;
		integral += error;
		int derivative = error - oldError;
		int turn = kp*error + ki*integral + kd*derivative;
		turn /= 100;
		int power = targetpower;

		if (speedmult > 1) {
			if (Math.abs(turn) < 1 && Math.abs(oldError) < 1) {
				currentmult = Math.min(speedmult, currentmult + 0.25);
			}
			else
			{
				currentmult = Math.max(1.0, currentmult - 0.25);
			}
		}

		power = (int)(targetpower * currentmult);
		
		driveProportionally(power, turn);
		oldError = error;
	}
	
	private static void driveProportionally(int power, int turn) {
		leftMotor.controlMotor(power - turn, BasicMotorPort.FORWARD);
		rightMotor.controlMotor(power + turn, BasicMotorPort.FORWARD);
	}
	
	private static void resetValues() {
		integral = 0;
		oldError = 0;
	}
	
	// Used by bang bang
	private static void driveForwardAndLeft(int power) {
		leftMotor.controlMotor(0, BasicMotorPort.FORWARD);
		rightMotor.controlMotor(power, BasicMotorPort.FORWARD);
	}
	
	private static void driveForwardAndRight(int power) {
		leftMotor.controlMotor(power, BasicMotorPort.FORWARD);
		rightMotor.controlMotor(0, BasicMotorPort.FORWARD);
	}
	
	private static boolean onTape() {
		return lightSensor.getLightValue() > offset;
	}
	
	private static void setKValues(Track t) {
		switch (t) {
		case One:
			kp = 300;
			ki = 0;
			kd = 0;
			speedmult = 2;
			break;
		case Two:
			kp = 300;
			ki = 0;
			kd = 0;
			speedmult = 1;
			break;
		case Three:
			kp = 0;
			ki = 0;
			kd = 0;
			speedmult = 1;
			break;
		case Four:
			kp = 0;
			ki = 0;
			kd = 0;
			speedmult = 1;
			break;
		}
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
			case 'b':
				mode = Mode.BangBang;
				break;
			case 'p':
				mode = Mode.P;
				break;
			case 'i':
				mode = Mode.PI;
				break;
			case 'd':
				mode = Mode.PD;
				break;
			case 'a':
				mode = Mode.PID;
				break;
				
			case '1':
				track = track.One;
				setKValues(track);
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
