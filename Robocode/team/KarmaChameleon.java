package sample;
import robocode.*;
import java.util.Set;
import java.util.HashSet;
import robocode.ScannedRobotEvent;
import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;
import java.lang.Math;

public class KarmaChameleon extends AdvancedRobot
{
	private estado state;
	private int numRobots;
	private int balasComidas;
	private float tamX, tamY;
	private boolean scanEnemies;
	private Set<Enemy> inimigos;
	private boolean parado;
	private boolean scanRaivoso;
	private boolean baterRaivoso;
	private int turnDirection = 1;
	private char [] estadoDasBalas;//h será hit e m miss
	private int numBala;
	private int rondasDeprimido;

	public enum estado
	{
		MEDO, CAUTELOSO, DESPREOCUPADO, RAIVOSO, EUFORICO, DEPRIMIDO
	}

	public void novoEstado(double energia)
	{

		if(!(state == estado.RAIVOSO))
		{
			if(rondasDeprimido == 0)
			{
				if(energia <= 15.0d)
				{
					state = estado.MEDO;
				}
				else if(energia > 15.0d && energia <= 40.0d)
				{
					state = estado.CAUTELOSO;

				}
				else if(energia > 100)
				{
					state = estado.EUFORICO;
					
				}
				else 
				{
					state = estado.DESPREOCUPADO;
					
				}

				if(getGunHeat() > 0 && energia > 15.0d && state != estado.EUFORICO)
				{
					state = estado.CAUTELOSO;
				}
				if(balasComidas / (getRoundNum()+1) > 0.50f && getRoundNum() > 10)
				{
					state = estado.CAUTELOSO;
					balasComidas = 0;
				}

				if(estadoDasBalas[0] == 'm'
					&& estadoDasBalas[1] == 'm'
						&& estadoDasBalas[2] == 'm')
				{
					rondasDeprimido = 1;
					state = estado.DEPRIMIDO;
				}

				if(numRobots < 5)
					state = estado.RAIVOSO;
			}
			else
			{
				if(rondasDeprimido < 1)
					rondasDeprimido++;
				else
				{
					estadoDasBalas [0] = 'h';
					estadoDasBalas [1] = 'h';
					estadoDasBalas [2] = 'h';
					rondasDeprimido = 0;

				}
			}
		}
	}

	/* ===================
		Handles Movements
	   =================== */

	private void goToMelhor(double x, double y) 
	{
		double dx = x - getX();
		double dy = y - getY();
		double turnAngle;

		turnAngle = (Math.toDegrees(Math.atan2(dx, dy)) - getHeading()) % 360;
		setTurnRight(turnAngle);
		ahead(Math.sqrt(dx*dx+dy*dy));

	} 
	
	//recursivo, caso o artolas tenha algum problema na sua translação
	private void goToEvitaObstaculos(double x, double y) 
	{
		double dx = x - getX();
		double dy = y - getY();
		double turnAngle;

		turnAngle = (Math.toDegrees(Math.atan2(dx, dy)) - getHeading()) % 360;
		turnRight(turnAngle);
		setAhead(Math.sqrt(dx*dx+dy*dy));
		waitFor(new MoveCompleteCondition(this));

		
		if(!(x == getX() && y == getY()))
			goToEvitaObstaculos(x, y);
	} 


	public double distance(Position end, Position start) 
	{
		double powX = Math.pow(end.getX() - start.getX(), 2);
		double powY = Math.pow(end.getY() - start.getY(), 2);
		return Math.sqrt(powX + powY);
	}

	public int quadrante(double x, double y)
	{
		int quad = 4;
		float meioX, meioY;
		meioX = tamX / 2.0f;
		meioY = tamY / 2.0f;
		if(x < meioX && x > meioY)
			quad = 2;
		else if(x > meioX && x > meioY)
			quad = 1;
		else if(x < meioX && x < meioY)
			quad = 3;

		return quad;
	}	

	public int melhorQuadrante()
	{
		int quad = 0;
		int q1, q2, q3, q4;
		q1 = q2 = q3 = q4 = 0;
		for(Enemy e : inimigos)
		{
			quad = quadrante(e.getX(), e.getY());
			if(quad == 1)
				q1++;
			else if(quad == 2)
				q2++;
			else if(quad == 3)
				q3++;
			else
				q4++;
		}
		
		int min = q1;

		if(q2 < min)
			min = q2;
		if(q3 < min)
			min = q3;
		if(q4 < min)
			min = q4;

		return min;
	}

	public Position posQuad(int quad)
	{
		float tamXI, tamXS, tamYI, tamYS; 
		if(quad == 1)
		{
			tamXI = (tamX/2.0f) * 0.7f;
			tamXS = tamX;

			tamYI = (tamY/2.0f) * 0.7f;
			tamYS = tamY;
		}
		else if(quad == 2)
		{
			tamXI = 0;
			tamXS = (tamX/2.0f) * 0.7f;

			tamYI = (tamY/2.0f) * 0.7f;
			tamYS = tamY;	
		}
		else if(quad == 3)
		{
			tamXI = 0;
			tamXS = (tamX/2.0f) * 0.7f;

			tamYI = 0;
			tamYS = (tamY/2.0f) * 0.7f;
		}
		else
		{
			tamXI = (tamX /2.0f) * 0.7f;
			tamXS = tamX;

			tamYI = 0;
			tamYS = (tamY/2.0f) * 0.7f;	
		}

		return new Position(ThreadLocalRandom.current().nextInt((int)tamXI, (int)tamXS),
							ThreadLocalRandom.current().nextInt((int)tamYI, (int)tamYS));
	}

	public void setBalaDisparada(char c)
	{
		estadoDasBalas[numBala] = c;
		if(numBala == 2)
			numBala = 0;
		else
			numBala++;	
	}

	/* ===================
		Handles Events
	   =================== */

	public void onBulletHitBullet(BulletHitBulletEvent event)
	{
		setBalaDisparada('h');
	}

	public void onBulletMissed(BulletMissedEvent event)
	{
		setBalaDisparada('m');
	}

	public void onBulletHit(BulletHitEvent event)
	{
		setBalaDisparada('h');
	}

	public void onHitByBullet(HitByBulletEvent event)
	{
		balasComidas++;
		if (state == estado.MEDO)
			parado = false;
	}
	   
	public void onHitRobot(HitRobotEvent event) 
	{
		
		if(state == estado.DEPRIMIDO)
		{/*não faz nada, está deprimido*/}
		else if(!event.getName().equals("sample.Marega") && !event.getName().equals("sample.TheBoss") 
					&& !event.getName().equals("sample.Bonnie") && !event.getName().equals("sample.Clyde"))
		{
			if(!baterRaivoso)
				fire(3);
			else
			{
				if (event.getBearing() >= 0) 
					turnDirection = 1;
			 	else 
					turnDirection = -1;

				turnRight(event.getBearing());
				fire(3);
				ahead(10);
			}
		}
		else
		{
			back(100);
		}
	}

	public void onScannedRobot(ScannedRobotEvent event) 
	{
		if(state == estado.DEPRIMIDO)
		{/*não faz nada, está deprimido*/}
		else if(!event.getName().equals("sample.Marega") && !event.getName().equals("sample.TheBoss") 
					&& !event.getName().equals("sample.Bonnie") && !event.getName().equals("sample.Clyde"))
		{

			if(scanRaivoso)
			{
				if (event.getBearing() >= 0)
					turnDirection = 1;
				else 
					turnDirection = -1;

				turnRight(event.getBearing());
				fire(3);
				ahead(event.getDistance() + 5);
				scan();
			}
			else if(!scanEnemies)
			{	
				if(state == estado.DESPREOCUPADO)
				{
					fire(2);
				}
				else if (state == estado.CAUTELOSO)
				{
					//verificar se está perto o suficiente para disparar
					if(event.getDistance() < Math.max(tamX, tamY) * 0.6f)
						fire(1);
				}
				else if(state == estado.EUFORICO || state == estado.RAIVOSO)
				{
					fire(3);	
				}
			}
			else
			{
				double angle, distance, x, y;
				angle = Math.toRadians(event.getBearing());
				distance = event.getDistance();
				x = getX() + distance * Math.sin(angle);
				y = getY() + distance * Math.cos(angle);

				Enemy e = new Enemy(x, y, event.getName());
				inimigos.add(e);
				
			}
		}
	}

	public void onRobotDeath(RobotDeathEvent event)
	{
		numRobots--;
	}

	/* ===================
		  Run function
	   =================== */	

	public void despreocupado()
	{
		setAllColors(Color.white);
		int ranX = ThreadLocalRandom.current().nextInt((int)(tamX * 0.05f), (int)(tamX * 0.95f));
		int ranY = ThreadLocalRandom.current().nextInt((int)(tamY * 0.05f), (int)(tamY * 0.95f));

		setMaxVelocity(7);
		goToMelhor(ranX, ranY);
	}

	public void cauteloso()
	{
		setAllColors(Color.orange);
		int ranX = ThreadLocalRandom.current().nextInt((int)(tamX * 0.05f), (int)(tamX * 0.95f));
		int ranY = ThreadLocalRandom.current().nextInt((int)(tamY * 0.05f), (int)(tamY * 0.95f));

		setMaxVelocity(5);
		goToMelhor(ranX, ranY);
	}

	public void amedrontado()
	{
		setAllColors(Color.yellow);
		if(!parado)
		{
			//scan dos inimigos
			scanEnemies = true;
			turnRadarRight(360);
			waitFor(new RadarTurnCompleteCondition(this));
			scanEnemies = false;
			setMaxVelocity(6);
			execute();
			//verificar o quadrante mais livre
			int quad = melhorQuadrante();
			//ir para o quadrante mais livre
			Position esconderijo = posQuad(quad);
			goToEvitaObstaculos(esconderijo.getX(), esconderijo.getY());
			//ficar escondido
			parado = true;
		}
		else
		{
			turnRadarRight(1);
		}
	}

	public void raivoso()
	{
		setAllColors(Color.red);
		setMaxVelocity(8);
		turnRight(5 * turnDirection);
		scanRaivoso = true;
		baterRaivoso = true;
	}

	public void euforico()
	{
		setAllColors(Color.green);
		int ranX = ThreadLocalRandom.current().nextInt((int)(tamX * 0.05f), (int)(tamX * 0.95f));
		int ranY = ThreadLocalRandom.current().nextInt((int)(tamY * 0.05f), (int)(tamY * 0.95f));

		setMaxVelocity(8);
		goToMelhor(ranX, ranY);
	}

	public void deprimido()
	{
		//não dispara e tenta fugir dos inimigos
		//fazer um x?
		setAllColors(Color.pink);
		setMaxVelocity(8);
		ahead(100);
		back(100);
		turnLeft(90);
		ahead(100);
		back(100);
	}

	public void run() 
	{
		//inicialização das variáveis
		numRobots = 10;
		//numRobots = 2;
		state = estado.DESPREOCUPADO;
		balasComidas = 0;
		tamX = (float) getBattleFieldWidth();
		tamY = (float) getBattleFieldHeight();
		scanEnemies = false;
		parado = false;
		inimigos = new HashSet<>();
		scanRaivoso = false;
		baterRaivoso = false;
		numBala = 0;
		rondasDeprimido = 0;
		estadoDasBalas = new char[3];
		estadoDasBalas [0] = 'h';
		estadoDasBalas [1] = 'h';
		estadoDasBalas [2] = 'h';

		while(true)
		{
			switch(state)
			{
				case MEDO: amedrontado();
						   break;
				case CAUTELOSO: cauteloso();
						   break;
				case DESPREOCUPADO: despreocupado();
						   break;
				case RAIVOSO: raivoso();
						   break;
				case EUFORICO: euforico();
						   break;
				case DEPRIMIDO: deprimido();
						   break;
			}
			
			novoEstado(getEnergy());
		}
	}



	public class Position {
		private double x;
		private double y;

		public Position(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public void setX(double x) {
			this.x = x;
		}

		public void setY(double y) {
			this.y = y;
		}

		public double getX() {
			return x;
		}
	
		public double getY() {
			return y;
		}	
	}
	
	public class Enemy {
		private double x, y;
		private String name;
		
		public Enemy(double x, double y, String n) {
			this.x = x;
			this.y = y;
			this.name = n;
		}
		
		public Enemy(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		public double getX() {
			return x;
		}
		
		public double getY() {
			return y;
		}
		
		public String getName() {
			return name;
		}
		
		public String toString() {
			return "Robot " + this.name +" ---> x: " + x + ", y: " + y; 
		}
	}
}