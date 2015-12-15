package wtr.g1;

import wtr.sim.Point;

import java.io.*;
import java.util.Random;
import java.util.Set;

public class Player implements wtr.sim.Player {

	// debugging mode
	private final static boolean debug = false;

	// your own id
	private int self_id = -1;
	private int soulmate = -1;

	private int last_chat = -1;
	private int turns_waited = 0;
	private boolean skip;

	// the remaining wisdom per player
	private int[] W = null;

	// random generator
	private Random random = null;

	// init function called once
	public void init(int id, int[] friend_ids, int strangers)
	{
		self_id = id;
		random = new Random(id);
		// create debugging file for player
		if (debug) try {
			FileOutputStream stream = new FileOutputStream(new File(self_id + ".dbg"), false);
			stream.close();
		} catch (IOException e) {}
		// initialize the wisdom array
		int N = friend_ids.length + strangers + 2;
		W = new int [N];
		for (int i = 0 ; i != N ; ++i)
			W[i] = i == self_id ? 0 : -1;
		for (int friend_id : friend_ids)
			W[friend_id] = 50;
	}

	// play function
	public Point play(Point[] players, int[] chat_ids,
	                  boolean wiser, int more_wisdom)
	{
		// find where you are and who you chat with
		int i = 0;
		int j = 0;
		while (players[i].id != self_id) {
			i++;
		}
		Point self = players[i];
		while (players[j].id != chat_ids[i]) {
			j++;
		}
		Point chat = players[j];

		if (chat.id != last_chat) {
      		turns_waited = 0;
    	}

		// record known wisdom
		W[chat.id] = more_wisdom;
		if (W[chat.id] > 50) {
			soulmate = chat.id;
		}

		// attempt to continue chatting if there is more wisdom
		skip = false;
		if (wiser) {
			last_chat = chat.id;
			return new Point(0.0, 0.0, chat.id);
		} else if (W[chat.id] > 0) {
			if (++turns_waited > W[chat.id]/5) {
				skip = true;
			}
		}

		// try to initiate chat if previously not chatting
		Point m = null;
		if (i == j || skip) {
			double best = 0;
			for (Point p : players) {
				// skip if no more wisdom to gain
				if (W[p.id] == 0 || has_players_within_radius(self, players, p, 0.6)) {
					continue;
				}
				double temp = score_player(p, players, self);
				if (temp > best) {
					best = temp;
					// start chatting if in range
					double dd = distance_squared(p, self);
					if (dd >= 0.25 && dd <= 0.5) {
						m = new Point(0.0, 0.0, p.id);
					}
				}
			}
		}

		// return a random move if no move yet
		if (m == null) {
			boolean r = true;
			double best = 0;
			for (Point p : players) {
				// skip if no more wisdom to gain
				if (W[p.id] == 0) {
					continue;
				}
				double temp = score_player(p, players, self);
				if (temp > best) {
					best = temp;
					r = false;
					double x = p.x - self.x;
					double y = p.y - self.y;
					if (x > 0) {
						x -= 0.4;
					} else {
						x += 0.4;
					}
					if (y > 0) {
						y -= 0.4;
					} else {
						y += 0.4;
					}
					m = new Point(x, y, self.id);
				}
			}
			if (r) {
				double dir = random.nextDouble() * 2 * Math.PI;
				double dx = 6 * Math.cos(dir);
				double dy = 6 * Math.sin(dir);
				m = new Point(dx, dy, self_id);
			}
		}
		// record move if in debug mode 
		if (debug) try {
			PrintStream stream = new PrintStream(new FileOutputStream(new File(self_id + ".dbg"), true));
			stream.println(m.x + ", " + m.y + ", " + m.id);
			stream.close();
		} catch (IOException e) {}
		return m;
	}

	private double score_player(Point p, Point[] players, Point self) {
		int wisdom = W[p.id];
		if (p.id == soulmate) {
			wisdom *= 1.5;
		}
		if (has_players_within_radius(p, players, self, 0.6)) {
			return wisdom - 30;
		} else if (distance_squared(p, self) < 1) {
			return wisdom + 10;
		} else {
			return wisdom;
		}

	}

	private double distance_squared(Point a, Point b) {
		double dx = a.x - b.x;
		double dy = a.y - b.y;
		return dx * dx + dy * dy;
	}

	private boolean has_players_within_radius(Point player, Point[] players, Point self, double r) {
		for (Point p : players) {
			if (p == player || p == self) {
				continue;
			}
			if (distance_squared(player, p) <= r*r) {
				return true;
			}
		}
		return false;
	}
}
