package wtr.g6;
import wtr.sim.Point;

public class Utils{
    public static double distance(Point p1, Point p2) {
        if(p1 == null || p2 == null) {
            return 0;
        }
        return Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
    }

    public static double distance(Point p1, Person p2) {
        if(p1 == null || p2 == null) {
            return 0;
        }
        return Math.sqrt((p1.x - p2.cur_position.x)*(p1.x - p2.cur_position.x) + (p1.y - p2.cur_position.y)*(p1.y - p2.cur_position.y));
    }

    public static boolean inTalkRange(Point self, Point p)
    {
        double distance = Utils.distance(self, p);
        if (distance >= 0.5 && distance <= 2.0) return true;
        return false;
    }

    public static double closestPersonDist(Point[] players, Person chat, Person self) {
        double dmin = Double.MAX_VALUE;
        for(Point p : players) {
            if(p.id == self.id) continue;
            if (p.id == chat.id) continue;
            double d = distance(p, chat);
            dmin = dmin < d ? dmin : d;
        }
        return dmin;
    }

    public static boolean closestToChatPlayer(Point[] players, Person chat, Person self) {
        double dmin = closestPersonDist(players, chat, self);
        double distance = distance(self.cur_position, chat.cur_position);
        return distance < dmin;
    }

    public static double closestPersonDist(Point[] players, Point chat, Point self) {
        double dmin = Double.MAX_VALUE;
        for(Point p : players) {
            if(p.id == self.id) continue;
            if (p.id == chat.id) continue;
            double d = distance(p, chat);
            dmin = dmin < d ? dmin : d;
        }
        return dmin;
    }

    public static boolean closestToChatPlayer(Point[] players, Point chat, Point self) {
        double dmin = closestPersonDist(players, chat, self);
        double distance = distance(self, chat);
        return distance < dmin;
    }
}
