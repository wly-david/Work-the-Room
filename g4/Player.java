package wtr.g4;

import wtr.sim.Point;

import java.util.Random;
import java.util.*;

import java.lang.Math;

public class Player implements wtr.sim.Player {

	// your own id
	private int self_id = -1;

	// the remaining wisdom per player
	private int[] W = null;
	private int ticks = 0;
	private int f = 0;
	
	// random generator
	private Random random = new Random();

	// friends
	ArrayList<Integer> friends = new ArrayList<>();

	ArrayList<ArrayList<Point>> priority = new ArrayList<>();

	// number of times failed initiating the talk
	private int fails = 0;

	// last player that we wanted to chat with
	private int lastPlayer = -1;

	// number of interfered chats
	private int interferedChats = 0;

	// init function called once
	public void init(int id, int[] friend_ids, int strangers)
	{
		self_id = id;
		// initialize the wisdom array
		int N = friend_ids.length + strangers + 2;
		W = new int [N];
		for (int i = 0 ; i != N ; ++i)
			W[i] = i == self_id ? 0 : -1;
		for (int friend_id : friend_ids) {
			friends.add(friend_id);
			W[friend_id] = 50;
		}
		int nStrangers = strangers;
	}

	//decide where to move
	private Point move(Point[] players, int[] chat_ids, Point self)
	{
		//Move to a new position within the room anywhere within 6 meters of your current position. Any conversation you were previously engaged in is terminated: you have to be stationary to chat.
		//Initiate a conversation with somebody who is standing at a distance between 0.5 meters and 2 meters of you. In this case you do not move. You can only initiate a conversation with somebody on the next turn if they and you are both not currently engaged in a conversation with somebody else. 	
		int i = 0;
		while (players[i].id != self_id) i++;
		//look through players who are within 6 meters => Point[] players
		for (int target =0; target<players.length; ++target) {
			Point p = players[target];
			//if we do not contain any information on them, they are our target => W[]
			if (ticks < 1200) {
				if (W[p.id] != -1 || p.id == self.id) {
					continue;
				}
			}
			else {
				if (W[p.id] == 0 || p.id == self.id) {
					continue;
				}
			}
			// compute squared distance
			double dx1 = self.x - p.x;
			double dy1 = self.y - p.y;
			double dd1 = dx1 * dx1 + dy1 * dy1;
			//check if they are engaged in conversations with someone
			int chatter = 0;
			while (chatter<players.length && players[chatter].id != chat_ids[target]) chatter++;

			//check if they are engaged in conversations with someone and whether that person is in our vicinity
			if(chat_ids[target]!=p.id && chatter!=players.length)
			{
				//if they are, we want to stand in front of them, .5 meters in the direction of who they're conversing with => check if result is within 6meters
				Point other = players[chatter];
				double dx2 = self.x - other.x;
				double dy2 = self.y - other.y;
				double dd2 = dx2 * dx2 + dy2 * dy2;

				double dx3 = dx2-dx1;
				double dy3 = dy2-dy1;
				double dd3 = Math.sqrt(dx3 * dx3 + dy3 * dy3);

				double dx4 = dx2 - .5*(dx3/dd3);
				double dy4 = dy2 - .5*(dy3/dd3);
				double dd4 = dx4 * dx4 + dy4 * dy4;

				if (dd4 <= 2.0) {
					return new Point(dx4, dy4, self.id);
				}

			}
			//if not chatting to someone or don't know position of chatter, just get as close to them as possible
			else {
				dx1 = dx1-.5*(dx1/dd1);
				dy1 = dy1-.5*(dy1/dd1);
				return new Point(dx1, dy1, self.id);
			}
		}

		boolean found = false;
		double dir = 0.0, num = 0.0, dx = 0.0, dy = 0.0;

		while (!found) {
			dir = random.nextDouble() * 2 * Math.PI;
			num = random.nextDouble() * 6.0;
			dx = 6 * Math.cos(dir);
			dy = 6 * Math.sin(dir);
			if ((self.x + dx <= 20) && (self.x + dx >= 0) && (self.y + dy <= 20) && (self.y + dy >= 0) && 
					(dx * dx + dy * dy <= 36)) {
				found = true;
			}
		}

		
		return new Point(dx, dy, self_id);
	}

	public class PlayerComparator implements Comparator<Point> {
		/**
		 * compare players by wisdom left
		 * expected wisdom of strangers are 12
		 */
		public int compare(Point p1, Point p2) {
			int s1 = W[p1.id] >= 0 ? W[p1.id] : 12;
			int s2 = W[p2.id] >= 0 ? W[p2.id] : 12;

			return s2 - s1;
		}
	}

	// play function
	public Point play(Point[] players, int[] chat_ids,
			boolean wiser, int more_wisdom)
	{
		// find where you are and who you chat with
		int i = 0, j = 0;
		ticks++;
		while (players[i].id != self_id) i++;
		while (players[j].id != chat_ids[i]) j++;
		Point self = players[i];
		Point chat = players[j];

		// record known wisdom
		W[chat.id] = more_wisdom;

		// if (self_id == 1 && i != j)
		// 	System.err.println("Wiser " + wiser + ", more_wisdom " + more_wisdom + ", W " + W[chat.id]);

		// Handle the case where the player is chatting
		if (wiser) {
			interferedChats = 0;
			return new Point(0.0, 0.0, chat.id);
		} /*else if (interferedChats < 3) {
			interferedChats++;
			//return new Point(0.0, 0.0, chat.id);
			return new Point(0.0, 0.0, self.id);
		}*/

		// try to initiate chat if previously not chatting
		if (i == j) {
			priority.clear();
			priority.add(new ArrayList<Point>());
			priority.add(new ArrayList<Point>());

			for (int index = 0; index < players.length; index++) {
				Point p = players[index];
				int chattedByP = chat_ids[index];

				// skip if no more wisdom to gain
				if (W[p.id] == 0) continue;

				// compute squared distance
				double dx = self.x - p.x;
				double dy = self.y - p.y;
				double dd = dx * dx + dy * dy;

				// put qualified players into priority
				if (p.id == chattedByP) {
					if (dd >= 0.25 && dd <= 4.0) {
						
						priority.get(0).add(p);  // available, within talking distance
					}
				} else if (dd >= 0.25) {
					priority.get(1).add(p);  // within range
				}
			}

			ArrayList<Point> myList = priority.get(0);
			Collections.sort(myList, new PlayerComparator());

			// get the player with highest wisdom
			for (Point p : myList) {
				if (fails > 3 && p.id == lastPlayer) {
					continue;
				}

				fails++;
				lastPlayer = p.id;
				// if (self_id == 1)
				// 	System.err.println(self_id + " list0, talking to " + p.id + " wisdom " + W[p.id]);
				return new Point(0.0, 0.0, p.id);
			}
			fails = 0;

			myList = priority.get(1);
			if (!myList.isEmpty()) {
				Collections.sort(myList, new PlayerComparator());
				Point p = myList.get(0);

				// compute squared distance
				double dx = p.x - self.x;
				double dy = p.y - self.y;
				double dd = dx * dx + dy * dy;

				// get close to the player
				// if (self_id == 1)
				// 	System.err.println(self_id + " list1, talking to " + p.id + " wisdom " + W[p.id]);
				return new Point(0.8 * dx, 0.8 * dy, self_id);
			}
		}

		return move(players, chat_ids, self);
	}
}
