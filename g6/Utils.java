package wtr.g6;
import wtr.sim.Point;

public class Utils{
    public static double distance(Point p1, Point p2) {
        if(p1 == null || p2 == null) {
            return 0;
        }
        return Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
    }

    public static boolean inTalkRange(Point self, Point p)
    {
        double distance = Utils.distance(self, p);
        if (distance >= 0.5 && distance <= 2.0) return true;
        return false;
    }
}