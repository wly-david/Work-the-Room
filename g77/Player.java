package wtr.g77;

import wtr.sim.Point;

import java.util.HashSet;
import java.util.Random;

public class Player implements wtr.sim.Player {

    // your own id
    private int self_id = -1;

    // the remaining wisdom per player
    private int[] W = null;

    // random generator
    private Random random = new Random();

    HashSet<Integer> friendSet = new HashSet<Integer>();

    // init function called once
    public void init(int id, int[] friend_ids, int strangers) {
        self_id = id;
        // initialize the wisdom array
        int N = friend_ids.length + strangers + 2;
        W = new int[N];
        for (int i = 0; i != N; ++i)
            W[i] = i == self_id ? 0 : -1;
        for (int friend_id : friend_ids) {
            friendSet.add(friend_id);
            W[friend_id] = 50;
        }
    }

    // play function
    public Point play(
            Point[] players,
            int[] chat_ids,
            boolean wiser,
            int more_wisdom
    ) {
        // find where you are and who you chat with
        int i = 0, j = 0;
        while (players[i].id != self_id) i++;
        while (players[j].id != chat_ids[i]) j++;
        Point self = players[i];
        Point chat = players[j];
        // record known wisdom
        W[chat.id] = more_wisdom;
        // attempt to continue chatting if there is more wisdom
        if (wiser) return new Point(0.0, 0.0, chat.id);
        // try to initiate chat if previously not chatting
        if (i == j) {
            ClosestDist[] dests = determineDestination(self, players);
            if (dests[0].get_point().id != -1) {
                double dist = dests[0].get_dist();
                if (dist >= 0.5 && dist <= 2.0) {
                    return new Point(0.0, 0.0, dests[0].get_point().id);
                } else {
                    return calc_min_dist(self, dests[0]);
                }
            } else if (dests[1].get_point().id != -1) {
                double dist = dests[1].get_dist();
                if (dist >= 0.5 && dist <= 2.0) {
                    return new Point(0.0, 0.0, dests[1].get_point().id);
                } else {
                    return calc_min_dist(self, dests[1]);
                }
            }
        }

        // return a random move
        double dir = random.nextDouble() * 2 * Math.PI;
        double dx = 6 * Math.cos(dir);
        double dy = 6 * Math.sin(dir);
        return new Point(dx, dy, self_id);
    }


    public Point calc_min_dist(Point p, ClosestDist d) {
        double dist = d.get_dist();
        double[] unit = new double[] {
                (d.get_point().x - p.x) / dist,
                (d.get_point().y - p.y) / dist
        };
        dist -= 0.5;
        return new Point(dist * unit[0], dist * unit[1], self_id);
    }


    public ClosestDist[] determineDestination(Point self, Point[] players) {
        ClosestDist[] dest = new ClosestDist[] {new ClosestDist(), new ClosestDist()};
        for (Point player : players) {
            if (friendSet.contains(player.id)) {
                if (W[player.id] == 0) continue;
                double distance = Utils.dist(self, player);
                if (distance < dest[0].get_dist()) {
                    dest[0].set(player, distance);
                }
            } else {
                if (W[player.id] == 0) continue;
                double distance = Utils.dist(self, player);
                if (distance < dest[1].get_dist()) {
                    dest[1].set(player, distance);
                }
            }

        }
        return dest;
    }
}