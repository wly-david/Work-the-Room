package wtr.g77;

import wtr.sim.Point;

public class ClosestDist {
    private Point person = new Point(0.0, 0.0, -1);
    private double dist = Double.MAX_VALUE;

    ClosestDist() {}

//    ClosestDist(Point p, double d) {
//        this.set(p, d);
//    }

    public void set(Point p, double d) {
        person = p;
        dist = d;
    }

    public Point get_point() {
        return person;
    }

    public double get_dist() {
        return dist;
    }
}