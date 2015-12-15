package wtr.g3;

import wtr.sim.Point;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

public class Player implements wtr.sim.Player {

    public static final int NONE = -1;
    public static final double MIN_DISTANCE = 1.0;
    public static final double TARGET_DISTANCE = 0.55;
    public static final double COMP_DIST = 0.8;

    public static final int AVERAGE_STRANGER_POINTS = 10;
    public static final int FRIEND_POINTS = 50;
    public static final int SOULMATE_POINTS = 400;
    public static final int TICKS = 1800;
    public static final double TICK_MULTIPLIER = 1.5;

    public int population;

    // your own id
    private int self_id = NONE;

    // player we don't want to talk to right now due to interruptions
    private int illegal = NONE;

    // last chat id
    private int lastChat = NONE;

    private int turnsWaited = 0;

    private boolean canGatherAllWisdom = false;

    private HashMap<Integer, PlayerStats> stats;

    private HashMap<Integer, Point> locations;
    private HashMap<Integer, Point> chats;

    // random generator
    private Random random = new Random();

    private int totalWisdomofStranger = 0;
    private int unknownPeople = 0;

    // init function called once
    public void init(int id, int[] friend_ids, int strangers) {
        population = 2 + friend_ids.length + strangers;

        canGatherAllWisdom = (TICKS * TICK_MULTIPLIER < (friend_ids.length * FRIEND_POINTS + SOULMATE_POINTS + strangers * AVERAGE_STRANGER_POINTS));

        self_id = id;
        // initialize the wisdom array
        stats = new HashMap<>();

        for (int friend_id : friend_ids) {
            stats.put(friend_id, new PlayerStats(friend_id, 50));
        }

        totalWisdomofStranger = SOULMATE_POINTS + AVERAGE_STRANGER_POINTS * strangers;
    }

    // play function
    public Point play(Point[] players, int[] chat_ids,
                      boolean wiser, int more_wisdom) {
        // find where you are and who you chat with
        int i = 0, j = 0;
        while (players[i].id != self_id) i++;
        while (players[j].id != chat_ids[i]) j++;
        Point self = players[i];
        Point chat = players[j];
        illegal = NONE;

        locations = new HashMap<Integer, Point>();
        chats = new HashMap<Integer, Point>();

        for (int k = 0; k < players.length; k++) {
            locations.put(players[k].id, players[k]);
        }
        for (int k = 0; k < players.length; k++) {
            chats.put(players[k].id, locations.get(chat_ids[i]));
        }

        if (!stats.containsKey(chat.id)) {
            stats.put(chat.id, new PlayerStats(chat.id, more_wisdom));
            totalWisdomofStranger = totalWisdomofStranger - more_wisdom;
            unknownPeople--;
        } else {
            stats.get(chat.id).setWisdomRemaining(more_wisdom);
        }

        if (chat.id != lastChat) {
            turnsWaited = 0;
        }

        if (!wiser && stats.get(chat.id).hasWisdom()) {
            if (++turnsWaited > waitTime(chat.id)) {
                illegal = chat.id;
            }
        }

        if (wiser) {
            lastChat = chat.id;
            if (distance(self, chat) > MIN_DISTANCE) {
                return getCloserWithID(self, chat, self.id);
            }

            return new Point(0.0, 0.0, chat.id);
        }

        if (i == j || illegal >= 0) {
            Point closestTarget = pickClosestTarget(players, self);

            if (closestTarget != null) {
                return closestTarget;
            }

            Point maxWisdomTarget = pickTargetWithMaximumRemainingWisdom(players, chat_ids, self);
            if (maxWisdomTarget != null) {
                if (distance(self, maxWisdomTarget) < MIN_DISTANCE) {
                    return maxWisdomTarget;
                } else {
                    return getCloserWithID(self, maxWisdomTarget, self.id);
                }
            }

        }
        // return a random move
        return newMoveFunction(players, self);
    }

    public Point pickClosestTarget(Point[] players, Point self) {

        double minDist = Double.MAX_VALUE;
        int targetId = NONE;

        for (Point p : players) {
            if (p.id == self.id || p.id == illegal)
                continue;

            // compute squared distance
            double dx = self.x - p.x;
            double dy = self.y - p.y;
            double dd = dx * dx + dy * dy;
            if (dd < 0.25 && dd > 0) {
                return null;
            }
            if (dd >= 0.25 && dd <= 4.0 && dd < minDist) {
                targetId = p.id;
                minDist = dd;
            }
        }
        if (targetId != NONE && playerHasWisdom(targetId)) {
            lastChat = targetId;
            return new Point(0.0, 0.0, targetId);
        }

        return null;
    }

    public Point pickTargetWithMaximumRemainingWisdom(Point[] players, int[] chat_ids, Point self) {

        int maxWisdom = 0;
        Point target = null;

        for (int i = 0; i < players.length; i++) {

            Point p = players[i];
            // compute squared distance
            double dx = self.x - p.x;
            double dy = self.y - p.y;
            double dd = dx * dx + dy * dy;
            if (dd < 0.25 && dd > 0) {
                return null;
            }

            if (players[i].id != chat_ids[i] || players[i].id == illegal || isBusy(players[i].id))
                continue;

            if (stats.containsKey(players[i].id) && stats.get(players[i].id).wisdomRemaining > maxWisdom) {
                maxWisdom = stats.get(players[i].id).wisdomRemaining;
                target = players[i];
            }
        }


        return target;
    }

    public boolean playerHasWisdom(int playerID) {
        return !stats.containsKey(playerID) ||
                (stats.containsKey(playerID) && stats.get(playerID).hasWisdom());
    }

    public Point randomMove() {
        // return a random move
        double dir = random.nextDouble() * -2 * Math.PI;
        double dx = 6 * Math.cos(dir);
        double dy = 6 * Math.sin(dir);
        return new Point(dx, dy, self_id);
    }

    public Point getCloserWithID(Point self, Point target, int id) {
        double targetDis = TARGET_DISTANCE;
        double dis = distance(self, target);
        double x = (dis - targetDis) * (target.x - self.x) / dis;
        double y = (dis - targetDis) * (target.y - self.y) / dis;
        return new Point(x, y, id);
    }

    public double distance(Point p1, Point p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public int getDivisor() {
        return (int) population;
    }

    public int waitTime2(int id) {
        if (population >= 100 && stats.get(id).wisdomRemaining <= 50) {
            return 0;
        } else {
            return stats.get(id).wisdomRemaining / (int) Math.sqrt(population);
        }
    }

    public int waitTime(int id) {
        if (canGatherAllWisdom) {
            if (stats.get(id).isSpecial()) {
                return stats.get(id).wisdomRemaining / 10;
            } else {
                return 0;
            }
        }

        return stats.get(id).wisdomRemaining / 10;
    }

    public boolean isBusy(int id) {
        if (chats.get(id).id == id || chats.get(id).id == self_id) {
            return false;
        }
        return true;
    /* Attempt at checking distances of conversation partners. Doesn't really
     * work.
    Point self = locations.get(self_id);
    Point target = locations.get(id);
    Point chat = locations.get(chats.get(id));

    if (chat == null || distance(self, target) > distance(target, chat)) {
      return true;
    } else {
      return false;
    } */
    }

    private Point newMoveFunction(Point[] players, Point self) {
        double dir = random.nextDouble() * 2 * Math.PI;
        double dx = 6 * Math.cos(dir);
        double dy = 6 * Math.sin(dir);
        Point position = new Point(dx, dy, self_id);

        PriorityQueue<Point> candidateTomove = new PriorityQueue<Point>(
                new Comparator<Point>() {
                    public int compare(Point x, Point y) {
                        int vx = (!stats.containsKey(x.id)) ? totalWisdomofStranger / unknownPeople : stats.get(x.id).wisdomRemaining;
                        int vy = (!stats.containsKey(y.id)) ? totalWisdomofStranger / unknownPeople : stats.get(y.id).wisdomRemaining;
                        return (vy - vx);
                    }
                });
        int i, j;
        for (i = 0; i < players.length; i++) {
            if (stats.containsKey(players[i].id) && stats.get(players[i].id).wisdomRemaining == 0) continue;

            double minDistance = Double.MAX_VALUE;
            for (j = 0; j < players.length; j++) {
                if (players[j].id == self_id || i == j) continue;
                dx = players[i].x - players[j].x;
                dy = players[i].y - players[j].y;
                double dd = Math.sqrt(dx * dx + dy * dy);
                if (dd < minDistance)
                    minDistance = dd;
            }
            if (minDistance > COMP_DIST)
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
        return position;
    }
}
