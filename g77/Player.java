package wtr.g77;

import wtr.sim.Point;

import java.math.BigDecimal;
import java.util.*;

public class Player implements wtr.sim.Player {

    // debugging mode
    private final static boolean debug = false;

    // your own id
    private int self_id = -1;

    // the remaining wisdom per player
    private int[] W = null;
    //	private int[] est_W = null;
    private int totalWisdomofStranger = 0;
    private int unknownPeople = 0;
    private int wait_time = 0;
    // random generator
    private Random random = new Random();

    // init function called once
    public void init(int id, int[] friend_ids, int strangers) {
        self_id = id;
        // initialize the wisdom array
        int N = friend_ids.length + strangers + 2;
        W = new int[N];
        for (int i = 0; i != N; ++i)
            W[i] = i == self_id ? 0 : -1;
        for (int friend_id : friend_ids)
            W[friend_id] = 50;
        totalWisdomofStranger = 400 + 10 * strangers;
        unknownPeople = strangers + 1;
    }

    // play function
    public Point play(
            Point[] players, int[] chat_ids, boolean wiser, int more_wisdom
    ) {
        // find where you are and who you chat with
        int i = 0, j = 0;
        while (players[i].id != self_id) i++;
        while (players[j].id != chat_ids[i]) j++;
        Point self = players[i];
        Point chat = players[j];


        // THIS IS JUST A TESTING SETUP. IGNORE IT EXCEPT FOR THE
        // hassle_free_chat function call. That is how you would call the function.
//        Point[] players_hist = new Point[] {
//                new Point(2.4045231, 6.15373314, -1),
//                new Point(2.76770798, 2.32619762, -1),
//                new Point(2.9888714, 0.14495442, -1),
//                new Point(2.46871276, 4.2739273, -1),
//                new Point(0.85789718, 1.64596912, -1),
//                new Point(8.8602994, 2.1694945, -1),
//                new Point(7.36159982, 5.20429626, -1),
//                new Point(8.17712566, 3.4316635, -1),
//                new Point(9.08287942, 1.53857356, -1),
//                new Point(5.7462481, 4.07204926, -1),
//                new Point(0.55163694, 5.48335058, -1),
//                new Point(1.59145798, 0.88825328, -1),
//                new Point(6.76659736, 0.48818988, -1),
//                new Point(4.28604106, 1.158474, -1),
//                new Point(9.45855528, 1.86189804, -1),
//                new Point(3.58450382, 2.89771512, -1),
//                new Point(3.36875014, 3.14970812, -1),
//                new Point(4.05316268, 2.85280364, -1),
//                new Point(6.71813548, 5.67119638, -1),
//                new Point(1.80222654, 3.6763708, -1),
//
//        };
//
//        Point no_hassle = hassle_free_chat(13, players_hist[16], players_hist, 0.52, false);

        // record known wisdom
        if (W[chat.id] == -1) {
            if (wiser)
                totalWisdomofStranger--;
            totalWisdomofStranger = totalWisdomofStranger - more_wisdom;
            unknownPeople--;
        }
        W[chat.id] = more_wisdom;

        // attempt to keep talking
        if (!wiser && i != j)
            wait_time++;
        else
            wait_time = 0;
//		if (wait_time <= more_wisdom / 10 && i != j)
//			return new Point(0,0,chat.id);

        // attempt to chat with the closet one if you're the closet one to him, too
        Point closetPerson = null;
        double distToPersonWithNowisdom = Double.MAX_VALUE;
        double distance = Double.MAX_VALUE;
        for (Point p : players) {
            // skip if no more wisdom to gain
            if (W[p.id] == 0) {
                if (p.id == self_id) continue;
                double dx = self.x - p.x;
                double dy = self.y - p.y;
                double dd = dx * dx + dy * dy;
                if (dd < distToPersonWithNowisdom) {
                    distToPersonWithNowisdom = dd;
                }
                continue;
            }
            // compute squared distance
//			int closet_ID = -1;
//			double dist_pq = Double.MAX_VALUE;
//			for (Point q : players) {
//				if (q.id == p.id) continue;
//				double dx_pq = q.x - p.x;
//				double dy_pq = q.y - p.y;
//				double dd_pq = dx_pq * dx_pq + dy_pq * dy_pq;
//				if (dd_pq < dist_pq) {
//					closet_ID = q.id;
//					dist_pq = dd_pq;
//				}
//			}
//			if (closet_ID != self_id) continue;
            double dx = self.x - p.x;
            double dy = self.y - p.y;
            double dd = dx * dx + dy * dy;
            // start chatting if in range
            if (dd < distance) {
                closetPerson = p;
                distance = dd;
            }
        }
        if (distance >= 0.25 && distance <= 4.0 && distance < distToPersonWithNowisdom
                && (closetPerson.id != chat.id || wait_time <= more_wisdom / 2)) {
//			System.out.println("my ID: " + self_id +"\n"+"chat with: (" +closetPerson.id+")");
            return new Point(0, 0, closetPerson.id);
        }
        // move to some place where you can talk with the most people
//		if (players.length <= 20)
//			return moveToTheCrowdedZone(players, self);
        // move to the person who is far away from others
//		else
        return newMoveFunction(players, chat_ids, self);
//			return moveToThePersonAlone(players, self);
    }

    // Move to the person with the most wisdom and away from others
    private Point newMoveFunction(Point[] players, int[] chat_ids, Point self) {
        double dir = random.nextDouble() * 2 * Math.PI;
        double dx = 6 * Math.cos(dir);
        double dy = 6 * Math.sin(dir);
        Point position = new Point(dx, dy, self_id);

        // find all valid cuts
        PriorityQueue<Point> candidateTomove = new PriorityQueue<>(
                (x, y) -> {
                    int vx = (W[x.id] < 0) ? totalWisdomofStranger / unknownPeople : W[x.id];
                    int vy = (W[y.id] < 0) ? totalWisdomofStranger / unknownPeople : W[y.id];
                    return (vy - vx);
                });
        int i, j;
        for (i = 0; i < players.length; i++) {
            if (W[players[i].id] == 0) continue;
            dx = players[i].x - self.x;
            dy = players[i].y - self.y;

            double minDistance = Double.MAX_VALUE;
            for (j = 0; j < players.length; j++) {
                if (players[j].id == self_id || i == j) continue;
                dx = players[i].x - players[j].x;
                dy = players[i].y - players[j].y;
                double dd = Math.sqrt(dx * dx + dy * dy);
                if (dd < minDistance)
                    minDistance = dd;
            }
//            if (minDistance > 0.6)
                candidateTomove.offer(players[i]);
        }
        double expectedDistance;
        if (candidateTomove.size() == 0)
            return position;
        else {
            Point target = candidateTomove.peek();
            i = 0;
            while (players[i].id != target.id) i++;
            if (players[i].id != chat_ids[i]) {
                expectedDistance = 0.5;
                dx = target.x - self.x;
                dy = target.y - self.y;
                double distanceTotarget = Math.sqrt(dx * dx + dy * dy);
                Point pos = hassle_free_chat(self_id, target, players, expectedDistance, false);
                if (pos!= null)
                        position = new Point(pos.x, pos.y, self_id);
//                position = new Point(dx * (distanceTotarget - expectedDistance) / distanceTotarget,
//                        dy * (distanceTotarget - expectedDistance) / distanceTotarget, self_id);
//				System.out.println("my ID: " + self_id+ "\n"+ "my move: (" +position.x+","+position.y+")");
                return position;
            } else {
                if (W[target.id] < 20) {
                    expectedDistance = 0.5;
                    dx = target.x - self.x;
                    dy = target.y - self.y;
                    double distanceTotarget = Math.sqrt(dx * dx + dy * dy);
                    Point pos = hassle_free_chat(self_id, target, players, expectedDistance, false);
                    if (pos!= null)
                            position = new Point(pos.x, pos.y, self_id);
//                    position = new Point(dx * (distanceTotarget - expectedDistance) / distanceTotarget,
//                            dy * (distanceTotarget - expectedDistance) / distanceTotarget, self_id);
//					System.out.println("my ID: " + self_id+ "\n"+ "my move: (" +position.x+","+position.y+")");
                } else {
                    dx = target.x - self.x;
                    dy = target.y - self.y;
                    double distanceTotarget = Math.sqrt(dx * dx + dy * dy);
                    expectedDistance = distanceTotarget / 3;
                    position = new Point(dx * (distanceTotarget - expectedDistance) / distanceTotarget,
                            dy * (distanceTotarget - expectedDistance) / distanceTotarget, self_id);
//					System.out.println("my ID: " + self_id+ "\n"+ "my move: (" +position.x+","+position.y+")");
                }
                return position;
            }
        }
    }

    private Point moveToThePersonAlone(Point[] players, Point self) {
        double dir = random.nextDouble() * 2 * Math.PI;
        double dx = 6 * Math.cos(dir);
        double dy = 6 * Math.sin(dir);
        Point position = new Point(dx, dy, self_id);

        // find all valid cuts
        PriorityQueue<Point> candidateTomove = new PriorityQueue<>(
                (x, y) -> {
                    double dx1 = x.x - self.x;
                    double dy1 = x.y - self.y;
                    double distanceTotargetx = Math.sqrt(dx1 * dx1 + dy1 * dy1);
                    dx1 = y.x - self.x;
                    dy1 = y.y - self.y;
                    double distanceTotargety = Math.sqrt(dx1 * dx1 + dy1 * dy1);
                    if (distanceTotargetx - distanceTotargety < 0)
                        return -1;
                    else if (distanceTotargetx - distanceTotargety == 0)
                        return 0;
                    else
                        return 1;
                });

        int i, j;
        for (i = 0; i < players.length; i++) {
            if (W[players[i].id] == 0) continue;

            double minDistance = Double.MAX_VALUE;
            for (j = 0; j < players.length; j++) {
                if (players[j].id == self_id || i == j) continue;
                dx = players[i].x - players[j].x;
                dy = players[i].y - players[j].y;
                double dd = Math.sqrt(dx * dx + dy * dy);
                if (dd < minDistance)
                    minDistance = dd;
            }
            if (minDistance > 1)
                candidateTomove.offer(players[i]);
        }
        double expectedDistance;
        if (candidateTomove.size() == 0)
            return position;
        else
            expectedDistance = 0.5;
        dx = candidateTomove.peek().x - self.x;
        dy = candidateTomove.peek().y - self.y;
        double distanceTotarget = Math.sqrt(dx * dx + dy * dy);
        position = new Point(dx * (distanceTotarget - expectedDistance) / distanceTotarget,
                dy * (distanceTotarget - expectedDistance) / distanceTotarget, self_id);
//		System.out.println("my ID: " + self_id+ "\n"+ "my move: (" +position.x+","+position.y+")");
        return position;
    }

    private Point moveToTheCrowdedZone(Point[] players, Point self) {
        double dir = random.nextDouble() * 2 * Math.PI;
        double dx = 6 * Math.cos(dir);
        double dy = 6 * Math.sin(dir);
        Point position = new Point(dx, dy, self_id);
        int max = 0;
        int i, j;
        for (i = 0; i < players.length; i++) {
            if (W[players[i].id] == 0) continue;
            for (j = 0; j < players.length; j++) {
                if (W[players[j].id] == 0 || i == j) continue;
                dx = players[i].x - players[j].x;
                dy = players[i].y - players[j].y;
                double dd = dx * dx + dy * dy;
                if (dd > 16.0) continue;
                double dist = Math.sqrt(dd);
                double theta = Math.acos(dist / 2.0);

                int count = 0;
                double x = players[j].x + dx / 2.0 * Math.tan(theta);
                double y = players[j].y + dy / 2.0 * Math.tan(theta);
                for (Point player : players) {
                    if (W[players[i].id] == 0) continue;
                    double ddToNewPos = (player.x - x) * (player.x - x)
                            + (player.y - y) * (player.y - y);
                    if (ddToNewPos >= 0.25 && ddToNewPos <= 4.0) count++;
                }
                if (count > max && (x - self.x) * (x - self.x) + (y - self.y) * (y - self.y) <= 36.0) {
                    max = count;
                    position = new Point(x - self.x, y - self.y, self_id);
                }

                count = 0;
                x = players[j].x - dx / 2.0 * Math.tan(theta);
                y = players[j].y - dy / 2.0 * Math.tan(theta);
                for (Point player : players) {
                    if (W[players[i].id] == 0) continue;
                    double ddToNewPos = (player.x - x) * (player.x - x)
                            + (player.y - y) * (player.y - y);
                    if (ddToNewPos >= 0.25 && ddToNewPos <= 4.0) count++;
                }
                if (count > max && (x - self.x) * (x - self.x) + (y - self.y) * (y - self.y) <= 36.0) {
                    max = count;
                    position = new Point(x - self.x, y - self.y, self_id);
                }
            }
        }
//		System.out.println("my ID: " + self_id+ "\n"+ "my move: (" +position.x+","+position.y+")");
        return position;
    }

    @SuppressWarnings("UnnecessaryContinue")
    public Point hassle_free_chat(
            int self_id, Point target, Point[] players, double rad, boolean alone
    ) {
        ArrayList<Point> jumps = new ArrayList<>();
        for (int a = 0; a < 360; a++) {
            jumps.add(new Point(target.x + (rad * Math.cos((a * 2 * Math.PI) / 360.0)), target.y + (rad * Math.sin((a * 2 * Math.PI) / 360.0)), -1));
        }
        int self = 0;
        for (int i = 0; i < players.length; i++) {
            if (players[i].id == self_id) {
            	self = i;
            	continue;
            }
            if (players[i].id == target.id) {
            	continue;
            }
//            if (target == players[i]) continue;

            Point[] intersection = circle_circle_intersection(target.x, target.y, rad, players[i].x, players[i].y, rad);
            if (intersection == null) continue;
            else if (intersection[0] == null && intersection[1] == null) {
                if (alone) return null;
                else continue;
            } else {
                double angle = angle_between_vectors(target.x, target.y, intersection[0].x, intersection[0].y, intersection[1].x, intersection[1].y);
                for (Iterator<Point> itr = jumps.iterator(); itr.hasNext();) {
                    Point p = itr.next();
                    double c1 = Math.abs(angle_between_vectors(target.x, target.y, intersection[0].x, intersection[0].y, p.x, p.y));
                    double c2 = Math.abs(angle_between_vectors(target.x, target.y, p.x, p.y, intersection[1].x, intersection[1].y));
                    Double c12 = new BigDecimal(c1 + c2).setScale(6, BigDecimal.ROUND_FLOOR).doubleValue();

                    if (angle >= c12) {
                        itr.remove();
                    } else {
                        continue;
                    }
                }
            }
        }

        for (Iterator<Point> itr = jumps.iterator(); itr.hasNext();) {
            Point p = itr.next();

            double d = dist(players[self], p);
            if (d >= 6) {
                itr.remove();
                continue;
            }

            if (p.x > 20 || p.x < 0 || p.y > 20 || p.y < 0) {
                itr.remove();
                continue;
            }
        }

        if (jumps.size() != 0) {
            int idx = random.nextInt(jumps.size());
            return jumps.get(idx);
        } else {
            return null;
        }
    }

    /**
     * Calculates the angle between given three ponits.
     *
     * @param x0 x coordinates of the 1st and pivot point
     * @param y0 y coordinates of the 1st and pivot point
     * @param x1 x coordinates of the 2nd point
     * @param y1 y coordinates of the 2nd point
     * @param x2 x coordinates of the 3rd point
     * @param y2 y coordinates of the 3rd point
     * @return The angle between the points in double precision.
     */
    public double angle_between_vectors(
            double x0, double y0,
            double x1, double y1,
            double x2, double y2
    ) {
        Point v1 = new Point(x1 - x0, y1 - y0, -1);
        Point v2 = new Point(x2 - x0, y2 - y0, -1);

        return Math.acos((v1.x * v2.x + v1.y * v2.y) / (Math.sqrt(v1.x * v1.x + v1.y * v1.y) * Math.sqrt(v2.x * v2.x + v2.y * v2.y)));
//        return angleRad * 180 / Math.PI;
    }

    /**
     * This algorith was implemented as a copy to paul bourke's c algorith
     * for finding the intersection of two circles found on the website at
     * http://paulbourke.net/geometry/circlesphere/tvoght.c
     *
     * @param x0 x coordinate of the 1st circle
     * @param y0 y coordinate of the 1st circle
     * @param r0 radius of the 1st circle
     * @param x1 x coordinate of the 2st circle
     * @param y1 y coordinate of the 2st circle
     * @param r1 radius of the 2st circle
     * @return If intersection exists then an array of two points otherwise
     * a null is returned. A null is return in the cases when
     * one circle is consumed by the other, the two circles lie on
     * top of each other, and when the two circles don't overlap.
     */
    public Point[] circle_circle_intersection(
            double x0, double y0, double r0,
            double x1, double y1, double r1
    ) {
        double a, dx, dy, d, h, rx, ry;
        double x2, y2;

        /* dx and dy are the vertical and horizontal distances between
         * the circle centers.
         */
        dx = x1 - x0;
        dy = y1 - y0;

        /* Determine the straight-line distance between the centers. */
        d = Math.sqrt((dy * dy) + (dx * dx));

        /* Check for solvability. */
        if (d >= (r0 + r1)) {
        /* no solution. circles do not intersect. */
            return null;
        }
        if (d <= r0) {
        /* no solution. one circle is contained in the other */
            return new Point[]{null, null};
        }

        /* 'point 2' is the point where the line through the circle
         * intersection points crosses the line between the circle
         * centers.
         */

        /* Determine the distance from point 0 to point 2. */
        a = ((r0 * r0) - (r1 * r1) + (d * d)) / (2.0 * d);

        /* Determine the coordinates of point 2. */
        x2 = x0 + (dx * a / d);
        y2 = y0 + (dy * a / d);

        /* Determine the distance from point 2 to either of the
         * intersection points.
         */
        h = Math.sqrt((r0 * r0) - (a * a));

        /* Now determine the offsets of the intersection points from
         * point 2.
         */
        rx = -dy * (h / d);
        ry = dx * (h / d);

        /* Determine the absolute intersection points. */
        return new Point[]{new Point(x2 + rx, y2 + ry, -1), new Point(x2 - rx, y2 - ry, -1)};
    }

    public double dist(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return dx * dx + dy * dy;
    }
}
