package sample;

public class Inimigo implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private double x = 0.0;
	private double y = 0.0;
	private double heading = 0.0;

	public Inimigo(double x, double y, double heading) {
		this.x = x;
		this.y = y;
		this.heading = heading;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public double getHeading() {
		return heading;
	}
}
