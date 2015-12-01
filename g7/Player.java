package wtr.g7;

import wtr.sim.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

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
		totalWisdomofStranger = 200 + 10 * strangers;
		unknownPeople = strangers + 1;
	}

	// play function
	public Point play(Point[] players, int[] chat_ids,
	                  boolean wiser, int more_wisdom)
	{
		// find where you are and who you chat with
		int i = 0, j = 0;
		while (players[i].id != self_id) i++;
		while (players[j].id != chat_ids[i]) j++;
		Point self = players[i];
		Point chat = players[j];
		// record known wisdom
		if (W[chat.id] == -1) {
			totalWisdomofStranger = totalWisdomofStranger - more_wisdom;
			unknownPeople --;
		}
		W[chat.id] = more_wisdom;
		
		// attempt to keep talking
//		if (!wiser && i != j)
//			wait_time ++;
//		else
//			wait_time = 0;
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
		if (distance >= 0.25 && distance <= 4.0 && distance < distToPersonWithNowisdom) {
//			System.out.println("my ID: " + self_id +"\n"+"chat with: (" +closetPerson.id+")");
			return new Point(0,0,closetPerson.id);
		}
		// move to some place where you can talk with the most people
//		if (players.length <= 20)
//			return moveToTheCrowdedZone(players, self);
		// move to the person who is far away from others
//		else
			return newMoveFunction(players, self);
//			return moveToThePersonAlone(players, self);
	}
	// Move to the person with the most wisdom and away from others
	private Point newMoveFunction(Point[] players, Point self) {
		double dir = random.nextDouble() * 2 * Math.PI;
		double dx = 6 * Math.cos(dir);
		double dy = 6 * Math.sin(dir);
		Point position = new Point(dx, dy, self_id);
		
        // find all valid cuts
		PriorityQueue<Point> candidateTomove = new PriorityQueue<Point>(
				new Comparator<Point>()
                {
                    public int compare( Point x, Point y )
                    {
                    	int vx = (W[x.id] < 0) ? totalWisdomofStranger / unknownPeople : W[x.id];
                    	int vy = (W[y.id] < 0) ? totalWisdomofStranger / unknownPeople : W[y.id];
                    	return (vy - vx);
                    }
                });		
		int i,j;
		for (i = 0; i < players.length; i ++) {
			if (W[players[i].id] == 0) continue;
			dx = players[i].x - self.x;
			dy = players[i].y - self.y;
			
			double minDistance = Double.MAX_VALUE;
			for (j = 0; j < players.length; j ++) {
				if (players[j].id ==self_id || i == j) continue;
				dx = players[i].x - players[j].x;
				dy = players[i].y - players[j].y;
				double dd = Math.sqrt(dx * dx + dy * dy);
				if (dd < minDistance)
					minDistance = dd;
			}
			if (minDistance > 0.6)
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
		position = new Point( dx * (distanceTotarget - expectedDistance) / distanceTotarget,
				dy * (distanceTotarget - expectedDistance) / distanceTotarget, self_id);
//		System.out.println("my ID: " + self_id+ "\n"+ "my move: (" +position.x+","+position.y+")");
		return position;
	}
	
	private Point moveToThePersonAlone(Point[] players, Point self) {
		double dir = random.nextDouble() * 2 * Math.PI;
		double dx = 6 * Math.cos(dir);
		double dy = 6 * Math.sin(dir);
		Point position = new Point(dx, dy, self_id);
		double max = 0;
		double min = Double.MAX_VALUE;
		int target = 0;
		boolean find_target = false;
		int i,j;
		for (i = 0; i < players.length; i ++) {
			if (W[players[i].id] == 0) continue;
			dx = players[i].x - self.x;
			dy = players[i].y - self.y;
			double distanceTotarget = Math.sqrt(dx * dx + dy * dy);
			
			double minDistance = Double.MAX_VALUE;
			for (j = 0; j < players.length; j ++) {
				if (players[j].id ==self_id || i == j) continue;
				dx = players[i].x - players[j].x;
				dy = players[i].y - players[j].y;
				double dd = Math.sqrt(dx * dx + dy * dy);
				if (dd < minDistance)
					minDistance = dd;
			}
			if (minDistance > distanceTotarget) {
				if (distanceTotarget < min) {
					target = i;
					min = distanceTotarget;
					find_target = true;
				}
			}
			else if (!find_target && minDistance > max) {
				target = i;
				max = minDistance;
			}
		}
		double expectedDistance;
		if (!find_target && max < 1)
			return position;
		else 
			expectedDistance = 0.6;
		dx = players[target].x - self.x;
		dy = players[target].y - self.y;
		double distanceTotarget = Math.sqrt(dx * dx + dy * dy);
		position = new Point( dx * (distanceTotarget - expectedDistance) / distanceTotarget,
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
		int i,j;
		for (i = 0; i < players.length; i ++) {
			if (W[players[i].id] == 0) continue;
			for (j = 0; j < players.length; j ++) {
				if (W[players[j].id] == 0 || i == j) continue;
				dx = players[i].x - players[j].x;
				dy = players[i].y - players[j].y;
				double dd = dx * dx + dy * dy;
				if (dd > 16.0) continue;
				double dist = Math.sqrt(dd);
				double theta = Math.acos(dist/2.0);
				
				int count = 0;
				double x = players[j].x + dx/2.0*Math.tan(theta);
				double y = players[j].y + dy/2.0*Math.tan(theta);
				for (int k = 0; k < players.length; k ++) {
					if (W[players[i].id] == 0 ) continue;
					double ddToNewPos = (players[k].x - x) * (players[k].x - x) 
							+ (players[k].y - y) * (players[k].y - y);
					if (ddToNewPos >= 0.25 && ddToNewPos <= 4.0) count ++;
				}
				if (count > max && (x - self.x) * (x - self.x) + (y - self.y) * (y - self.y) <= 36.0) {
					max = count;
					position = new Point(x - self.x, y - self.y, self_id);
				}
				
				count = 0;
				x = players[j].x - dx/2.0*Math.tan(theta);
				y = players[j].y - dy/2.0*Math.tan(theta);
				for (int k = 0; k < players.length; k ++) {
					if (W[players[i].id] == 0 ) continue;
					double ddToNewPos = (players[k].x - x) * (players[k].x - x) 
							+ (players[k].y - y) * (players[k].y - y);
					if (ddToNewPos >= 0.25 && ddToNewPos <= 4.0) count ++;
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
}
