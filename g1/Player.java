package wtr.g1;

import wtr.sim.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class Player implements wtr.sim.Player {

	// your own id
	private int self_id = -1;

	private int soulmate = -1;
	private ClusterPoint closestCluster;
	//time counter 
	int time=0;
	// the remaining wisdom per player
	private int[] W = null;

	// random generator
	private Random random = new Random();
	private ArrayList<Integer> friends;

	ArrayList<Point> nearby_friends;
	ArrayList<Point> nearby_strangers;
	ArrayList<Point> available_friends;
	ArrayList<Point> available_strangers;

	//Ignore list
	List<NavigableSet<Integer>> ignore;
	int levelsBeforeIgnore=2;
	int steps2ConsiderForIgnore=10;
	int historySize=10;
	Map<Integer, Integer> p2w;
	boolean[] Wb=null;
	int strangerPoints=20;
	int[] lastChats=new int[historySize];
	
	// init function called once
	public void init(int id, int[] friend_ids, int strangers)
	{
		self_id = id;
		friends = new ArrayList<Integer>();
		nearby_friends = new ArrayList<Point>();
		nearby_strangers = new ArrayList<Point>();
		available_friends = new ArrayList<Point>();
		available_strangers = new ArrayList<Point>();
		// initialize the wisdom array
		int N = friend_ids.length + strangers + 2;
		
        W = new int[N];
        Wb = new boolean[N];
        for (int i = 0; i != N; ++i) {
            W[i] = (i == self_id) ? 0 : strangerPoints;
            Wb[i] = i == self_id;
        }
        Wb[self_id] = true;

        for (int friend_id : friend_ids) {
            W[friend_id] = 50;
            Wb[friend_id] = true;
    		friends.add(friend_id);
        }

		ignore=new ArrayList<NavigableSet<Integer>>();
		for(int i=0;i<levelsBeforeIgnore;i++){
			ignore.add(new TreeSet<Integer>());
			ignore.add(new TreeSet<Integer>());
		}
		
		for(int i=0; i<lastChats.length;i++){
			lastChats[i]=-1;
		}
	}

	/**.
	 * Update the people we talked with in past x time.. can be kept as history 
	 * @param recentChat
	 */
	void updateLastChats(int recentChat){
		for(int i=0; i<lastChats.length-1;i++){
			lastChats[i]=lastChats[i+1];
		}
		lastChats[lastChats.length-1]=recentChat;
	}
	
	boolean ignorePlayer(int id){
		if( ignore.get(levelsBeforeIgnore-1).contains(id)){
			//player was suposed to be ignored.. but ignore only if 
			// we talked with him in last x steps
			for(int i=lastChats.length-1; i> Math.max(((lastChats.length-1)- steps2ConsiderForIgnore),0) ;i--){
				if(lastChats[i] == id) return true; //ignore
			}
			/**
			 * remove the player from ignore list ? to give him another chance
			 */
			for(int i=0;i<levelsBeforeIgnore;i++){
				if(ignore.get(i).contains(id)){
					ignore.get(i).remove(id);
				}
				return false;
			}
		}
		return false;
	}
	
	Point getReturnPoint(double x, double y, int chatId){
		updateLastChats(chatId);
		return new Point(x,  y,  chatId);
	}
	// play function
	public Point play(Point[] players, int[] chat_ids,
	                  boolean wiser, int more_wisdom)
	{
		time=time+1;
		// find where you are and who you chat with
		int i = 0, j = 0;
		while (players[i].id != self_id) i++;
		while (players[j].id != chat_ids[i]) j++;
		Point self = players[i];
		Point chat = players[j];
		KMeans kmeans = new KMeans(3, players, self);
		double mindistance = Double.MAX_VALUE;
		HashMap<Double, ClusterPoint> distMap = new HashMap<Double, ClusterPoint>();
		ArrayList<Double> distances = new ArrayList<Double>();
		for(Cluster cluster: kmeans.clusters)
		{
			ClusterPoint cur_centroid = cluster.getCentroid();
			double dist = ClusterPoint.distance(cur_centroid, new ClusterPoint(self.x, self.y, self.id));
			distances.add(dist);
			distMap.put(dist, new ClusterPoint(cur_centroid.getX(), cur_centroid.getY(), cluster.id));
			if (dist < mindistance)
			{
				mindistance = dist;
				closestCluster = new ClusterPoint(cur_centroid.getX(), cur_centroid.getY(), cluster.id);
			}
		}
		ClusterPoint distToClosestPoint;
		// ClusterPoint midPoint;
		// Collections.sort(distances);
		// if(distances.size() >=2)
		// {
		// 	ClusterPoint closestCluster1 = distMap.get(distances.get(0));
		// 	ClusterPoint closestCluster2 = distMap.get(distances.get(1));
		// 	midPoint = ClusterPoint.getMidPoint(closestCluster1, closestCluster2);
		// 	distToClosestPoint = new ClusterPoint(midPoint.x - self.x, midPoint.y - self.y, self.id);
		// }
		// else
		// {
		// 	distToClosestPoint = new ClusterPoint(0, 0, self.id);
		// 	midPoint = new ClusterPoint(self.x, self.y, self.id);
		// }

		distToClosestPoint = new ClusterPoint(closestCluster.x - self.x, closestCluster.y - self.y, self.id);

		// record known wisdom
		W[chat.id] = more_wisdom;

		// attempt to continue chatting if there is more wisdom
		if (wiser) 
		{
			if(!ignorePlayer(chat.id))
			{
				return getReturnPoint(0.0, 0.0, chat.id);
			}
		}

		nearby_friends.clear();
		nearby_strangers.clear();
		available_friends.clear();
		available_strangers.clear();

		// try to initiate chat if previously not chatting
		if (i == j) {
			for (int k=0; k<players.length; k++) {
				Point p = players[k];
				if (friends.contains(p.id)) {
					nearby_friends.add(p);
					if (chat_ids[k] == p.id) {
						available_friends.add(p);
					}
				} else {
					nearby_strangers.add(p);
					if (chat_ids[k] == p.id) {
						available_strangers.add(p);
					}
				}
			}

			// SM is first priority
			if (soulmate != -1) {
				for (Point p : available_strangers) {
					// skip if not soulmate
					if (W[p.id] != soulmate) {
						continue;
					}
					// skip if soulmate is out of wisdom
					if (W[p.id] > 20 && W[p.id] <= 50) {
						friends.add(p.id);
						soulmate = -1;
						continue;
					}
					if (W[p.id] <= 20) {
						soulmate = -1;
						continue;
					}
					// compute squared distance
					double dx = p.x - self.x;
					double dy = p.y - self.y;
					double dd = dx * dx + dy * dy;
					// start chatting if in range, else move to SM
					if (dd >= 0.25 && dd <= 4.0) {
						return getReturnPoint(0.0,0.0, p.id);
					} /*else {
						return getReturnPoint(dx/1.2, dy/1.2, self_id);
					}*/
				}
			}

			// find a friend to talk to
			for (Point p : available_friends) {
				// skip if no more wisdom
				if (W[p.id] == 0) {
					continue;
				}
				// compute squared distance
				double dx = p.x - self.x;
				double dy = p.y - self.y;
				double dd = dx * dx + dy * dy;
				// start chatting if in range
				if (dd >= 0.25 && dd <= 4.0){
					if(!ignorePlayer(p.id)){
						return getReturnPoint(0.0, 0.0, p.id);
					}
				}
			}

			// find a stranger to talk to
			for (Point p : available_strangers) {
				// skip if no more wisdom to gain
				if (W[p.id] == 0) {
					continue;
				}
				if (W[p.id] > 20) {
					soulmate = p.id;
				}
				// compute squared distance
				double dx = p.x - self.x;
				double dy = p.y - self.y;
				double dd = dx * dx + dy * dy;
				// start chatting if in range
				if (dd >= 0.25 && dd <= 4.0)
					if(!ignorePlayer(p.id)){
						return getReturnPoint(0.0, 0.0, p.id);
					}
			}
		}

		// find a friend out of distance, go to that friend
		for (Point p : nearby_friends) 
		{
				// skip if no more wisdom to gain
				if (W[p.id] == 0) 
				{
					continue;
				}
				// compute squared distance
				double dx = p.x - self.x;
				double dy = p.y - self.y;
				double dd = dx * dx + dy * dy;
				// start chatting if in range
				if (dd>= 0.25 && dd <= 4.0) 
				{
					//TODO: check if we should have self_id here or p.id and less then 4 or > 4?
					if(!ignorePlayer(p.id))
						return getReturnPoint(dx/1.2, dy/1.2, p.id);
				}
		}


		// System.out.println("GOING TO MOST CONCENTRATED AREA");
		double dir = random.nextDouble() * 2 * Math.PI;
		double jump = 6;
		double dx = jump * distToClosestPoint.unit().x;
		double dy = jump * distToClosestPoint.unit().y;
		if((dy + self.y > 19 || dx + self.x > 19) && jump >1)
		{
			jump-= 0.1;
			dx = jump * distToClosestPoint.unit().x;
			dy = jump * distToClosestPoint.unit().y;
		}
		if (jump == 1)
		{
			System.out.println("NO MORE MOVES");
		}
		return getReturnPoint(dx, dy, self_id);
		// return getReturnPoint(distToClosestPoint.x, distToClosestPoint.y, self_id);
	}
}
