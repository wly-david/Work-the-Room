package wtr.sim;

public class Point {

	// location of player represented by this point
	public final double x;
	public final double y;

	// id of player represented by this point
	public final int id;

	public Point(double x, double y, int id)
	{
		this.x = x;
		this.y = y;
		this.id = id;
	}

	public Point()
	{
		throw new UnsupportedOperationException();
	}
}
