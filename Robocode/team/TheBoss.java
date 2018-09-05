package sample;

import robocode.*;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * Looks around for enemies, and orders teammates to fire
 *
 */
public class TheBoss extends TeamRobot 
{

	private double WIDTH;
	private double HEIGHT;
	private Map<Integer, Position> corners;
	private boolean threatened;
	private int currCorner;
	private int nextCorner;
	private boolean clockwise;
	private int droidsVivos;
	

	/**
	 * run:  Leader's default behavior
	 */
	public void run() 
	{
		
		this.WIDTH = getBattleFieldWidth();
		this.HEIGHT = getBattleFieldHeight();

		this.corners = new HashMap<>();
		this.corners.put(1, new Position(25,25));
		this.corners.put(2, new Position(this.WIDTH-25,25));
		this.corners.put(3, new Position(this.WIDTH-25,this.HEIGHT-25));
		this.corners.put(4, new Position(25,this.HEIGHT-25));
		this.clockwise = true;
		closestCorner();
		gotoCorner(this.currCorner);
		turnRadarRight(360 - this.getHeading());
		this.threatened = false;
		droidsVivos = 2;

		while (true) 
		{
			if(droidsVivos > 0)
			{
				if (this.clockwise)
					this.nextCorner = (this.currCorner == 1) ? 4 : this.currCorner-1;
				else
					this.nextCorner = (this.currCorner == 4) ? 1 : this.currCorner+1;
		
				gotoCorner(this.nextCorner);
			}
			else
			{
				vagueiaEDestroi();
			}
		}
	}
	
	/**
	 * onScannedRobot:  What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e)
	{
		double bearing = e.getBearing();
		double heading = this.getHeading();
		double distance = e.getDistance();

		double x,y;
		x = getX();
		y = getY();

		if (Math.abs(bearing) < 10 &&
			(heading == 0 || heading == 90 ||
			heading == 180 || heading == 270))
		{		
				this.threatened = true;
				Position src = new Position(getX(), getY());
				Position corner = this.corners.get(this.currCorner);
				back(distance(corner, src));
				if (this.clockwise) 
				{
					this.nextCorner = (this.currCorner == 4) ? 1 : this.currCorner+1;
					this.clockwise = false;
				}
				else 
				{
					this.nextCorner = (this.currCorner == 1) ? 4 : this.currCorner-1;
					this.clockwise = true;
				}
				gotoCorner(this.nextCorner);
		}
		
		// Don't fire on teammates
		if(e.getName().equals("sample.Marega") || e.getName().equals("sample.KarmaChameleon") || e.getName().equals("sample.Bonnie") 
			|| e.getName().equals("sample.Clyde")) 
			return;
		

		if(droidsVivos > 0)
		{
			// Calculate enemy bearing
			double enemyBearing = heading + bearing;
			// Calculate enemy's position
			double enemyX = x + distance * Math.sin(Math.toRadians(enemyBearing));
			double enemyY = y + distance * Math.cos(Math.toRadians(enemyBearing));


			try 
			{
				// Send enemy position to teammates
				broadcastMessage(new Inimigo(enemyX, enemyY, e.getHeading()));
			} catch (IOException ex) 
			{
				ex.printStackTrace(out);
			}
		}
		else
			fire(2);
	}

	public void onHitRobot(HitRobotEvent event) 
	{
		
		if(droidsVivos == 0)
		{
			if(!event.getName().equals("sample.Marega") && !event.getName().equals("sample.KarmaChameleon") 
						&& !event.getName().equals("sample.Bonnie") && !event.getName().equals("sample.Clyde"))
				fire(3);
			else
				back(100);
		}
	}

	public void vagueiaEDestroi()
	{
		int ranX = ThreadLocalRandom.current().nextInt((int) (WIDTH * 0.1d), (int) (WIDTH * 0.9d));
		int ranY = ThreadLocalRandom.current().nextInt((int) (HEIGHT * 0.1d), (int) (HEIGHT * 0.9d));

		goToMelhor(ranX, ranY);
	}

	
	public double distance(Position end, Position start) 
	{
		double powX = Math.pow(end.getX() - start.getX(), 2);
		double powY = Math.pow(end.getY() - start.getY(), 2);
		return Math.sqrt(powX + powY);
	}
	
	public void closestCorner() 
	{
		Position src = new Position(getX(),getY());
		double distance, min = 10000;

		for(Map.Entry<Integer, Position> corner : this.corners.entrySet()) 
		{
			distance = distance(corner.getValue(), src);
			if (distance < min) 
			{
				min = distance;
				this.currCorner = corner.getKey();
			}
		}
	}

	public void gotoCorner(int corner) 
	{
		setTurnRadarRight(10000);

		double x = this.corners.get(corner).getX();
		double y = this.corners.get(corner).getY();
		double dx = x - getX();
		double dy = y - getY();
		double turnAngle;

		turnAngle = (Math.toDegrees(Math.atan2(dx, dy)) - getHeading()) % 360;
		turnRight(turnAngle);
		setAhead(Math.sqrt(dx*dx+dy*dy));
		waitFor(new MoveCompleteCondition(this));

		long endX = Math.round(getX());
		long endY = Math.round(getY());

		if (endX == x && endY == y) 
		{
			this.currCorner = corner;
			
			if (corner < 3) 
			{
				if (this.clockwise)
					turnRight(90);
				else
					turnLeft(90);
			}
			return;
		}
		else if (this.threatened) 
		{
			this.threatened = false;
			return;
		}
		else
			gotoCorner(corner);
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
	
	public void onRobotDeath(RobotDeathEvent event)
	{
		if(event.getName().equals("sample.Bonnie") || event.getName().equals("sample.Clyde"))
		{
			droidsVivos--;
		}
	}

	public void onDeath(DeathEvent e) 
	{
		try 
		{
			// Send message of death
			broadcastMessage(new AnuncioDeMorte("O vosso líder morreu!"));
		} catch (IOException ex) 
		{
			ex.printStackTrace(out);
		}
	}
	
	public class Position 
	{
		private double x;
		private double y;

		public Position(double x, double y) 
		{
			this.x = x;
			this.y = y;
		}

		public void setX(double x) 
		{
			this.x = x;
		}

		public void setY(double y) 
		{
			this.y = y;
		}

		public double getX() 
		{
			return x;
		}
	
		public double getY() {
			return y;
		}	
	}
	
	public class Enemy 
	{
		private double x, y;
		private String name;
		
		public Enemy(double x, double y, String n) 
		{
			this.x = x;
			this.y = y;
			this.name = n;
		}
		
		public Enemy(double x, double y) 
		{
			this.x = x;
			this.y = y;
		}
		
		public double getX() 
		{
			return x;
		}
		
		public double getY() 
		{
			return y;
		}
		
		public String getName() 
		{
			return name;
		}
		
		public String toString() 
		{
			return "Robot " + this.name +" ---> x: " + x + ", y: " + y; 
		}
	}
}
