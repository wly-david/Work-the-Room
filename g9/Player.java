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
			//if (chat_ids[i] == -1) continue; // Chatting to someone else
			if (W[players[i].id] == 0) continue; // No value
			double dd = getDistance(players[i], players[self_idx]);
			if (W[players[i].id] > maxWisdom && (chat_ids[i]==players[i].id) && dd>=0.25)
			{
				maxWisdom = W[players[i].id];
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

		double dist = Double.POSITIVE_INFINITY;
		int index = -1;

		for (int i = 0; i < players.length; i++) {
			if (i == self_idx) continue; // Self
			//if (chat_ids[i] == -1) continue; // Chatting to someone else
			//if (W[players[i].id] == 0) continue; // No value
			if (W[players[i].id] == 0) continue; // No value
			
			double dd = getDistance(players[i], players[self_idx]);

			if (dd < dist && dd>=0.25 && dd<=4.0 && (chat_ids[i]==players[i].id)) {
				dist  = dd ;
				index = i;
			}
		}
		return index;
	}
	
	public boolean isPlayerChatting(Integer player_id, Point[] players, int[] chat_ids){
		int i = 0, j = 0;
		while (players[i].id != player_id) i++;
		while (players[j].id != chat_ids[i]) j++;
		if (i == j)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	
	
	public Point moveCloser(Point self, Point chat)
	{
		double slope = Math.abs(Math.atan((chat.y - self.y)/(chat.x - self.x)));
		double xOffset = 0.5*Math.cos(slope);
		double yOffset = 0.5*Math.sin(slope);
		

		double newX = (self.x <=chat.x) ? (self.x + xOffset) : (self.x - xOffset);
		double newY = (self.y <=chat.y) ? (self.y + yOffset) : (self.y - yOffset);
		
		// Move to a new position 0.5 metres away from the current position
		Point newSelf = new Point(newX, newY, self_id);
		
		double dx = (chat.x - newSelf.x)*0.50;
		double dy = (chat.y - newSelf.y)*0.50;
		
		System.out.println("Moving to : My Player id : " + self_id   + ", Chat id " + chat.id + " (" + (self.x + dx)+", " + (self.y + dy) +" )" + " From :(" + self.x+", " + self.y + " )");
		
		return new Point(dx, dy, self_id);	
		
	}

	
	// play function
	public Point play(Point[] players, int[] chat_ids,
	                  boolean wiser, int more_wisdom)
	{
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
		W[chat.id] = more_wisdom;
		
		
		
		// If there's somone closer, move on
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

		
		if(wiser){
			
			if(closeEnoughToChatter && distFromChatter>=0.25 && distFromChatter<=4.0){
				System.out.println("Wiser : My Player id : " + self_id  + ", Chatting with id : " + chat.id);
				return new Point(0.0, 0.0, chat.id);
			}
			else{
				// If can move closer, move to min dist
				if(minDistFromOther > 0.25){
					
					next_id = chat.id;
					return moveCloser( self,  chat);

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
				}
			}
		}

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

		
		// try to initiate chat if previously not chatting
		if (i == j)
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
