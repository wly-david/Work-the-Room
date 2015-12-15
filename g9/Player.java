package wtr.g9;

import wtr.sim.Point;

import java.util.Random;

public class Player implements wtr.sim.Player {

	// your own id
	private int self_id = -1;

	// the remaining wisdom per player
	private int[] W = null;

	// random generator
	private Random random = new Random();

	// Who talk to next
	private int next_id = -1;

	private int soulmate_id = -1;

	// How many consecutive frames you've been interrupted while chatting
	private int frames_waiting = -1;

	private int time = -1;

	private int undiscovered_pts;

	private int undiscovered_num;
	
	private int count_consecutive = 0;

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
		undiscovered_num = strangers + 1;
		undiscovered_pts = 10 * undiscovered_num + 400;
	}

	
	private int getMaxWisdomPlayer(Point[] players, int[] chat_ids) {
		//Pick the non-chatting player within the 6 meter radius with the maximum wisdom ..
		//Here strangers will be given less priority than friends
		// even if the friends remaining wisdom is less than the strangers wisdom
		
		double maxWisdom = Double.MIN_VALUE;
		// value by index in the array
		int self_idx = 0;
		while (players[self_idx].id != self_id) self_idx++;
		
		int index = -1;
		for (int i = 0; i < players.length; i++) {
			if (i == self_idx) continue; // Self
			if (W[players[i].id] == 0) continue; // No value
			
			double dd = getDistance(players[i], players[self_idx]);
			
			if (players[i].id == soulmate_id && W[players[i].id] >= 20) return i; // ALWAYS GO TO SOULMATE

			//if (isPlayerChatting(i, players, chat_ids)) continue; // Chatting to someone else
			
			double w = W[players[i].id];
			if (w == -1) w = ((double)undiscovered_pts) / undiscovered_num; // Expected for unknown strangers
			//if (w > maxWisdom &&  w >= 10 && (chat_ids[i]==players[i].id))
			//if (w > maxWisdom &&  w >= 10 && (chat_ids[i]==players[i].id))
			if (w > maxWisdom && (chat_ids[i]==players[i].id))
			{
				maxWisdom = w;
				index = i;
			}
		}
		return index;
	}

	
	private int pick_player(Point[] players, int[] chat_ids) {
		// Pick the closest not-chatting player with positive or unknown W
		// value by index in the array
		int self_idx = 0;
		while (players[self_idx].id != self_id) self_idx++;

		// See if soulmate is in range

		if (soulmate_id != -1) {
			for (int soulmate_idx = 0; soulmate_idx < players.length; soulmate_idx++) {
				if (players[soulmate_idx].id != soulmate_id) continue;

				if (W[players[soulmate_idx].id] <= 50) {soulmate_id = -1; break;} // No additional value to a friend...
				if (isPlayerChatting(soulmate_idx, players, chat_ids)) break; // Chatting to someone else
				double dd = getDistance(players[self_idx], players[soulmate_idx]);
				//if (dd < 0.25 || dd > 4.0) return -1; // Cannot reach, go there
				if (dd < 0.25 || dd > 4.0) break; // Cannot reach, go there
				// Prioritize
				return soulmate_idx;
			}
		}

		double dist = Double.POSITIVE_INFINITY;
		int index = -1;

		for (int i = 0; i < players.length; i++) {
			if (i == self_idx) continue; // Self
			if (isPlayerChatting(i, players, chat_ids)) continue; // Chatting to someone else
			if (W[players[i].id] == 0) continue; // No value
			
			double dd = getDistance(players[i], players[self_idx]);

			if (dd < 0.25)
			{
				return -1;
			}
			
			if (dd < dist && dd>=0.25 && dd<=4.0) {
				dist  = dd;
				index = i;
			}
		}
		return index;
	}
	
	public boolean isPlayerChatting(int idx, Point[] players, int[] chat_ids){
		return !(chat_ids[idx] == players[idx].id);
	}
	
	public Point moveCloser(Point self, Point chat)
	{
		double slope = Math.atan2(chat.y - self.y, chat.x - self.x);
		
		double xOffset = (0.52) * Math.cos(slope);
		double yOffset = (0.52) * Math.sin(slope);

		double newX = chat.x - xOffset;
		double newY = chat.y - yOffset;

		// Move to a new position 0.5 metres away from the chat position
		double dx = (newX - self.x);
		double dy = (newY - self.y);
		
		
		/*dx = chat.x - self.x;
        dy = chat.y - self.y;
        double targetDistace = Math.sqrt(dx * dx + dy * dy);
		
		dx = (targetDistace - 0.52) * (chat.x - self.x) / targetDistace;
	    dy = (targetDistace - 0.52) * (chat.y - self.y) / targetDistace;*/

		System.out.println("Moving to : My Player id : " + self_id   + ", Chat id " + chat.id + " (" + (self.x + dx)+", " + (self.y + dy) +" )" + " From :(" + self.x+", " + self.y + " )");
		
		return new Point(dx, dy, self_id);	
		
	}

	
	// play function
	public Point play(Point[] players, int[] chat_ids,
	                  boolean wiser, int more_wisdom)
	{
		time ++;
		// Print each of the players coordinates
		/*for(Point p : players)
		{
			System.out.println("Player : " + " id : " + p.id + ", x: " + p.x + ", y: " + p.y);
		}*/
		
		// find where you are and who you chat with
		int i = 0, j = 0;
		// After this, players[i].id = self_id
		while (players[i].id != self_id) i++; 
		// After this, players[j] is someone whom I'm currently talking to
		while (players[j].id != chat_ids[i]) j++; 
		Point self = players[i];
		Point chat = players[j];
		// record known wisdom (more_wisdom is remaining wisdom)
		if (W[chat.id] == -1) {
			undiscovered_num --;
			if (more_wisdom > 198) {
				undiscovered_pts -= 400;
			} else if (more_wisdom > 18) {
				undiscovered_pts -= 20;
			} else if (more_wisdom > 8) {
				undiscovered_pts -= 10;
			}
		}
		W[chat.id] = more_wisdom;;
		if (more_wisdom > 50) {
			// Soulmate found
			soulmate_id = chat.id;
		}
		
		
		if (wiser)
		{
			count_consecutive = 0;
		}
		else
		{
			count_consecutive++;
		}
		if (count_consecutive == 15)
		{
			count_consecutive = 0;
			// return a random move
			return getRandom(self);
		}
		

		// If there's someone closer, move on
		boolean closeEnoughToChatter = true;
		double distFromChatter = getDistance(self, chat); 
		double minDistFromOther = Double.MAX_VALUE;
		for(Point p : players){
			if((p.id == self.id) || (p.id == chat.id)) continue;
			double dist = getDistance(self, p);
			if(dist < minDistFromOther){
				minDistFromOther = dist;
			}
		}
		if(minDistFromOther < distFromChatter) closeEnoughToChatter = false;

		if (more_wisdom > 0) { // Chat still meaningful
			if (wiser || frames_waiting != -1) {

				if (distFromChatter >= 0.25 && distFromChatter <= 4.0) {
					System.out.println("Wiser : My Player id : " + self_id + ", Chatting with id : " + chat.id);
					if (closeEnoughToChatter) {
						frames_waiting = -1;
						//return new Point(0.0, 0.0, chat.id);
						if (distFromChatter < 0.8)
						{
							return new Point(0.0, 0.0, chat.id);
						}
						else
						{
							return moveCloser(self, chat);
						}			
					} else {
						frames_waiting++;
						if (frames_waiting < 2) {
							return new Point(0.0, 0.0, chat.id);
						} else {
							// If waited more than 2 frames with continuous interruption, move on and try someone else
							frames_waiting = -1;

							// If can move closer, move to min dist
							if (minDistFromOther > 0.25) {
								next_id = chat.id;
								if (self != chat) {
									return moveCloser(self, chat);
								}
							}

						}
					}
				}
			}
		}


					/*double dx = self.x;
					double dy = self.y;
					int factorX = self.x < chat.x ? 1 : -1;
					int factorY = self.y < chat.y ? 1 : -1;
					Point newPoint = new Point(dx,dy,0);
					double newDist = getDistance(chat, newPoint);
					boolean useX = true;
					boolean useY = true;
					double increaseFactor = 0.1;
					while(newDist > 0.25){
						double updatedDist;
						Point updatedPoint;
						if(useX){
							dx += increaseFactor*factorX; 
							updatedPoint = new Point(dx,dy, 0);							
							updatedDist = getDistance(updatedPoint,chat);
							if(updatedDist > newDist || updatedDist < 0.25){
								dx -= increaseFactor*factorX; 
								useX = false;
							}
							else{
								newDist = updatedDist;
							}
						}
						else if(useY){
							dy += increaseFactor*factorY; 
							updatedPoint = new Point(dx,dy, 0);							
							updatedDist = getDistance(updatedPoint,chat);
							if(updatedDist > newDist || updatedDist < 0.25){
								dy -= increaseFactor*factorY; 
								useY = false;
							}
							else{
								newDist = updatedDist;
							}							
						}
						else{
							break;
						}
					}
					// Eventually, try to move in a circle around 
					next_id = chat.id;
					return new Point(dx, dy, self_id);		*/
		/*
		if(next_id != -1){
			// See if player is nearby and isn't chatting with anyone
			int index = 0;
			boolean playerNearby = false;
			for (Point p : players) {
				if(p.id == next_id){
					playerNearby = true;
					break;
				}
				index++;
			}
			next_id = -1;
			if(playerNearby == true && (chat_ids[index] == -1)){
				System.out.println("Not expected ");
				return new Point(0.0, 0.0, chat.id);				
			}
		}
		*/
		
		// try to initiate chat if giving up / not chatting
		if (true)
		{
			int ind = pick_player(players, chat_ids);
			if (ind != -1)
			{
				System.out.println("Picking the closest player : My Player id : " + self_id + " : Picked player ID " + players[ind].id);
				return new Point(0.0, 0.0, players[ind].id);
			}
			else
			{
				//Move closer to player that has the maximum wisdom and is not chatting with anyone....
				ind = getMaxWisdomPlayer(players, chat_ids);

				if (ind != -1)
				{
					System.out.println("Moving closer to player : " + players[ind].id);
					return moveCloser( self,  players[ind]);
				}
				
			}
		}

			/*for (Point p : players) {
				// skip if no more wisdom to gain
				if (W[p.id] == 0) continue;
				// compute squared distance
				double dx = self.x - p.x;
				double dy = self.y - p.y;
				double dd = dx * dx + dy * dy;
				// start chatting if in range
				if (dd >= 0.25 && dd <= 4.0)
					return new Point(0.0, 0.0, p.id);
			}*/
		// return a random move
		return getRandom(self);
	}
	
	
	public Point getRandom(Point self)
	{
		boolean isSafe = false;
		double dir = 0;
		double dx = 0;
		double dy = 0;
		while (!isSafe)
		{
			dir = random.nextDouble() * 2 * Math.PI;
			dx = 6 * Math.cos(dir);
			dy = 6 * Math.sin(dir);
			double x = self.x + dx;
			double y = self.y + dy;
			if (x>=0 && x<=20 && y>=0 && y<=20)
			{
				isSafe = true;
			}
		}
		
		System.out.println("Random move: My Player id : " + self_id   + " (" + (self.x + dx)+", " + (self.y + dy) +" )" + " From :(" + self.x+", " + self.y + " )");
		
		return new Point(dx, dy, self_id);
	}
	
	
	public double getDistance(Point p1, Point p2){
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		double dd = dx * dx + dy * dy;
		return dd;
		
	}
}