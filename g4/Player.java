package wtr.g4;

import wtr.sim.Point;

import java.util.Random;

import java.lang.Math;

public class Player implements wtr.sim.Player {

	// your own id
	private int self_id = -1;

	// the remaining wisdom per player
	private int[] W = null;

	// random generator
	private Random random = new Random();

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
		for (int friend_id : friend_ids)
			W[friend_id] = 50;
	}

	//decide where to move
	private Point move(Point[] players, int[] chat_ids, Point self)
	{
		//Move to a new position within the room anywhere within 6 meters of your current position. Any conversation you were previously engaged in is terminated: you have to be stationary to chat.
		//Initiate a conversation with somebody who is standing at a distance between 0.5 meters and 2 meters of you. In this case you do not move. You can only initiate a conversation with somebody on the next turn if they and you are both not currently engaged in a conversation with somebody else. 	
		int i = 0, target = 0;
		while (players[i].id != self_id) i++;
		//look through players who are within 6 meters => Point[] players
		Point p = self;
        for (target = 0; target<players.length; ++target) {
			//if we do not contain any information on them, they are our target => W[]
			if (W[p.id] != -1 || p.id == self.id) continue;
            p = players[target];
        }
        if (p.equals(self)) {
            for (target = 0; target < players.length; ++target) {
                if (W[p.id] == 0 || p.id == self.id) continue;
                p = players[target];
            }
        }
        if (!p.equals(self)) {
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

				double dx4 = (dx2 - 0.5*(dx3/dd3)) * -1;
				double dy4 = (dy2 - 0.5*(dy3/dd3)) * -1;
				double dd4 = dx4 * dx4 + dy4 * dy4;

				if (dd4 <= 2.0)
					return new Point(dx4, dy4, self.id);
            }
			//if not chatting to someone or don't know position of chatter, just get as close to them as possible
			else return new Point((dx1-0.5*(dx1/dd1)) * -1, (dy1-0.5*(dy1/dd1)) * -1, self.id);				
		}

			double dir = random.nextDouble() * 2 * Math.PI;
			double dx = 6 * Math.cos(dir);
			double dy = 6 * Math.sin(dir);
			return new Point(dx, dy, self_id);
	}

	// play function
	public Point play(Point[] players, int[] chat_ids,
			boolean wiser, int more_wisdom)
	{
		// find where you are and who you chat with
		int i = 0, j = 0, chatCount = 3;
		while (players[i].id != self_id) i++;
		while (players[j].id != chat_ids[i]) j++;
		Point self = players[i];
		Point chat = players[j];
		// record known wisdom
		W[chat.id] = more_wisdom;
        if (more_wisdom > 50) chatCount = 6;
		// attempt to continue chatting if there is more wisdom
		if (wiser) {
			interferedChats = 0;
            return new Point(0.0, 0.0, chat.id);
		}
        else if (interferedChats < chatCount) {
            interferedChats++;
            return new Point(0.0, 0.0, chat.id);
        }
		// try to initiate chat if previously not chatting
		if (i == j) {
            int id = -1;
            double max_distance = Double.MAX_VALUE;
			for (Point p : players) {
				// skip if no more wisdom to gain
				if (W[p.id] == 0) continue;
				// compute squared distance
				double dx = self.x - p.x;
				double dy = self.y - p.y;
				double dd = dx * dx + dy * dy;
				// start chatting if in range
				if (dd >= 0.25 && dd <= 4.0 && dd < max_distance) {
                    id = p.id;
                }
			}
            if (id != -1) return new Point(0.0, 0.0, id);
		}
		return move(players, chat_ids, self);
	}
}
