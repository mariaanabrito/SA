package sample;
import robocode.*;
import robocode.TeamRobot;
import java.util.Random;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.util.concurrent.ThreadLocalRandom;

public class Clyde extends TeamRobot implements Droid
{
	private double heightMax, widthMax;
	private double height1, width1, height2, width2;
	private int whereIamGoing;
	private boolean sentidoUnclock, bateu, semLider, posInicial;
	private double X_START,Y_START;
	
	public void run() {
			
			sentidoUnclock = true;
			bateu = false;
			semLider = false;
			posInicial = false;

			heightMax = getBattleFieldHeight();
			widthMax = getBattleFieldWidth();

			height1 = heightMax - 100;
			width1 = 100;
			height2 = 100;
			width2 = widthMax - 100;
			
			X_START = width1;
			Y_START = height1;
			gotoXY(X_START, Y_START);
			posInicial = true;
			whereIamGoing = 2;
			while(true){
				if(!semLider)
					percorreTrajetoria(height1, width1, height2, width2);
				else andaAsVoltas();
			}
		}
		
	private void goToMelhor(double x, double y) 
	{
		double dx = x - getX();
		double dy = y - getY();
		double turnAngle;

		turnAngle = (Math.toDegrees(Math.atan2(dx, dy)) - getHeading()) % 360;
		setTurnRight(turnAngle);
		ahead(Math.sqrt(dx*dx+dy*dy));

	} 
	
	private void andaAsVoltas()
	{
		int ranX = ThreadLocalRandom.current().nextInt((int)(widthMax * 0.1f), (int)(widthMax * 0.9f));
		int ranY = ThreadLocalRandom.current().nextInt((int)(heightMax * 0.1f), (int)(heightMax * 0.9f));

		setMaxVelocity(8);
		goToMelhor(ranX, ranY);
	}

	private void percorreTrajetoria(double height1, double width1, double height2, double width2) {
			if(sentidoUnclock) {
				if (whereIamGoing == 1) {
					gotoXY(width1, height1);
					if(bateu)
						bateu = false;
					else whereIamGoing = 2;
				}
				else if (whereIamGoing == 2) {
					gotoXY(width1, height2);
					if(bateu)
						bateu = false;
					else whereIamGoing = 3;
				}
				else if (whereIamGoing == 3) {
					gotoXY(width2, height2);
					if(bateu)
						bateu = false;
					else whereIamGoing = 4;
				}
				else if (whereIamGoing == 4) {
					gotoXY(width2, height1);
					if(bateu)
						bateu = false;
					else whereIamGoing = 1;
				}
			}
			else {
				if (whereIamGoing == 1) {
					gotoXY(width1, height1);
					if(bateu)
						bateu = false;
					else whereIamGoing = 4;
				}
				else if (whereIamGoing == 2) {
					gotoXY(width1, height2);
					if(bateu)
						bateu = false;
					else whereIamGoing = 1;
				}
				else if (whereIamGoing == 3) {
					gotoXY(width2, height2);
					if(bateu)
						bateu = false;
					else whereIamGoing = 2;
				}
				else if (whereIamGoing == 4) {
					gotoXY(width2, height1);
					if(bateu)
						bateu = false;
					else whereIamGoing = 3;
				}
			}
			
	}

	public void onHitRobot(HitRobotEvent e) {
		if(!semLider){
			Random rand = new Random();
			if (!posInicial) 
			{
				if(rand.nextInt()%2 == 0) {
					turnRight(180);
					ahead(40);
					turnLeft(90);
					ahead(60);
				}
				else {
					turnLeft(180);
					ahead(40);
					turnRight(90);
					ahead(60);
				}
				gotoXY(X_START,Y_START);
			}
			else
			{
				bateu = true;
				sentidoUnclock = !sentidoUnclock;
				if(sentidoUnclock)
				{
					if(whereIamGoing == 1)
						whereIamGoing = 4;
					else if(whereIamGoing == 4)
						whereIamGoing = 1;
					else whereIamGoing++;
				}
				else 
				{
					if(whereIamGoing == 4)
						whereIamGoing = 1;
					else if (whereIamGoing == 1)
						whereIamGoing = 4;
					else whereIamGoing--;
				}
			}
		}
		else {
			back(40);
		}
	}
	
	public void onRobotDeath(RobotDeathEvent event)
	{
		if(event.getName().equals("sample.TheBoss"))
			semLider = true;
	}
	
	private void gotoXY(double x, double y) {
		double dx = x - getX();
		double dy = y - getY();
		double turnAngle;

		turnAngle = (Math.toDegrees(Math.atan2(dx, dy)) - getHeading()) % 360;
		turnRight(turnAngle);
		turnGunTo(x,y);
		setAhead(Math.sqrt(dx*dx+dy*dy));
		waitFor(new MoveCompleteCondition(this));
	}
	
	public void turnGunTo(double x, double y) {
		double dx = x - getX();
		double dy = y - getY();
			
		double theta = Math.toDegrees(Math.atan2(dx, dy));

		turnGunRight(normalRelativeAngleDegrees(theta - getGunHeading()));	
	} 
	
	public void onMessageReceived(MessageEvent e) {
		if (e.getMessage() instanceof Inimigo) {
			setAhead(0);
			execute();
			// Como continuar o movimento?
			Inimigo msg = (Inimigo) e.getMessage();
			
			turnGunTo(msg.getX(), msg.getY());
			
			fire(3);
		}
	}
}
