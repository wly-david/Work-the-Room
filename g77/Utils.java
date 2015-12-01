package wtr.g77;
import wtr.sim.Point;

public class Utils {

	public static double dist(Point a,Point b){
		double dx = a.x - b.x;
		double dy = a.y - b.y;
		double dd = dx * dx + dy * dy;
		
		return dd;
	}
}