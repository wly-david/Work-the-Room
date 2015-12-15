package wtr.g5;

import wtr.sim.Point;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;

public class Player implements wtr.sim.Player {

	// your own id
	private int self_id = -1;

	// the remaining wisdom per player
	private int[] W = null;

	// random generator
	private Random random = new Random();

	private HashSet<Integer> friendSet;

	private int interfereThreshold = 5;
	private int nTurnsMaxWait = 6;

	private int interfereCount = 0;
	private Integer preChatId;
	private Point selfPlayer;
	private HashSet<Integer> alreadyTalkedStrangers;
	private Double strangerUnknowWisdom;
	private Integer numberOfStrangers;
	private Integer totalNumber;
	private int soulmateID;
	private int cur_stranger_wisdom;
	PrintWriter po;
	boolean waitingForTarget;
	int nTurnsWaited;
	Point target;
	int tick = 0;
	
	// init function called once
	public void init(int id, int[] friend_ids, int strangers)
	{
		target = null;
		waitingForTarget = false;
		nTurnsWaited = 0;
		self_id = id;
		try {
			po = null;
			//po = new PrintWriter(new File(id+"_debug.txt"));
		} catch (Exception e) {
			po = null;
		}
		friendSet = new HashSet<Integer>();
		// initialize the wisdom array
		int N = friend_ids.length + strangers + 2;
		totalNumber = N;
		W = new int [N];
		// initialize strangers' wisdom to 5.5 (avg wisdom for 1/3 + 1/3 + 1/3 configuration)
		cur_stranger_wisdom = (int) (5.5*strangers + 400)/(strangers+1);
		// debug("strangerWisdom: "+stranger_wisdom);
		for (int i = 0 ; i != N ; ++i)
			W[i] = i == self_id ? 0 : cur_stranger_wisdom;
		for (int friend_id : friend_ids){
			friendSet.add(friend_id);
			W[friend_id] = 50;
		}
		preChatId = self_id;
		alreadyTalkedStrangers = new HashSet<Integer>();
		strangerUnknowWisdom = strangers * 5.5 + 400;
		numberOfStrangers = strangers + 1;
		soulmateID = -1;
	}
	public void updateStrangerWisdom(){
		
		cur_stranger_wisdom = (int) (strangerUnknowWisdom / numberOfStrangers);
		for(int i = 0; i < totalNumber; i++){
			if(friendSet.contains(i) || alreadyTalkedStrangers.contains(i) || i == self_id)
				continue;
			W[i] = cur_stranger_wisdom;
		}
	}

	// play function
	public Point play(Point[] players, int[] chat_ids, boolean wiser, int more_wisdom)
	{
		// find where you are and who you chat with
		// for(int i = 0; i < players.length; ++i) {
		// 	if(players[i].id != i)
		// 		System.out.println("ID NOT I: "+players[i].id+"\t"+i);
		// }
		++tick;
		int i = 0, j = 0;
		while (players[i].id != self_id) i++;
		while (players[j].id != chat_ids[i]) j++;
		
		boolean conversedLastTurn = j != i;

		Point self = players[i];
		Point chat = players[j];
		
		boolean sabotaged = false;
		
		if(waitingForTarget && ++nTurnsWaited <= nTurnsMaxWait) {
			int targetID = target.id;
			int targetIndex = 0;
			while(targetIndex < players.length && players[targetIndex].id != targetID) ++targetIndex;
			
			if(targetIndex == players.length) {
				waitingForTarget = false;
				target = null;
				nTurnsWaited = 0;
				debugNoNewline("TARGET NOT IN VISION");
			}
			
			else if(conversedLastTurn) {
				waitingForTarget = false;
				target = null;
				nTurnsWaited = 0;
				debugNoNewline("SUCCESSFUL SABOTAGE: "+i+", "+j);
				sabotaged = true;
			}
			
			else if(distance(self, players[targetIndex]) <= .6){
				debug("WAITING FOR TARGET");
				return new Point(0,0,targetID);
			}
			
			else {
				debugNoNewline("TARGET ESCAPED");
				waitingForTarget = false;
				target = null;
				nTurnsWaited = 0;
			}
		} else if (nTurnsWaited > 6) {
			debugNoNewline("TARGET ENGAGED MORE THAN 6 TURNS");
			waitingForTarget = false;
			target = null;
			nTurnsWaited = 0;
		}
		
		selfPlayer = self;
		//soul mate
		if(more_wisdom > 50 && !friendSet.contains(chat.id) && soulmateID < 0){
			alreadyTalkedStrangers.add(chat.id);
			soulmateID = chat.id;
			friendSet.add(chat.id);
			// Keep track of soulmate, so you only subtract the 400 one time.
			strangerUnknowWisdom -= 400;	
			numberOfStrangers--;
		}else if(chat.id != self_id && !friendSet.contains(chat.id) && !alreadyTalkedStrangers.contains(chat.id)){
			alreadyTalkedStrangers.add(chat.id);
			// If they have 20 wisdom to share with us, on average this conversation will give us 10 wisdom
			// (since they may have less than 20 to get from us!)
			if(more_wisdom > 10)
				strangerUnknowWisdom -= 10;
			// If they have 10 wisdom to give us, on average, this conversation will give us 6.6666 wisdom
			// (since they have 1/3 chance to get nothing from us!)
			else if(more_wisdom > 0)
				strangerUnknowWisdom -= 6.6666;
			numberOfStrangers--;
		}
		
		// record known wisdom
		W[chat.id] = more_wisdom;
		//TODO remove from blacklist
		// attempt to continue chatting if there is more wisdom
		// System.out.println("wise: " + wiser + " selfid " + self_id + " chatid " + chat.id + " W " + W[chat.id]);
		updateStrangerWisdom();
		
		if(chat.id != preChatId)
			interfereCount = 0;
		if(!wiser && (friendSet.contains(chat.id) && W[chat.id] > 0)) {
			debugNoNewline("INTERFERED");
			interfereCount++;
		}
		if (sabotaged || wiser || (friendSet.contains(chat.id) && W[chat.id] > 0)) {
			if(!wiser && interfereCount >= (W[chat.id] > cur_stranger_wisdom ? interfereThreshold : 0)){
				//If two friends has been interfered more than 5 times, then move away
				debug("RANDMOVE");
				return randomMoveInRoom(self);
			} else{
				preChatId = chat.id;
				System.out.println("DIST: "+distance(self, chat));
				if(distance(self, chat) > 0.6) {
					Point ret = getCloserWithID(self, chat, self.id);
					debug("TO CONVERSANT");
					return ret;
				}
				debug("CONTINUE CHAT");
				return new Point(0.0, 0.0, chat.id);
			}
		}
		// try to initiate chat if previously not chatting
		if (i == j){
			Point closestTarget = pickTarget1(players, chat_ids);
			if (closestTarget == null) {

				Point maxWisdomTarget = pickTarget2(players, 6, chat_ids);
				if (maxWisdomTarget == null) {
//					System.out.println("no valid target.");
					// jump to random position
					debug("no valid target.  RANDMOVE");
					return randomMoveInRoom(self);
				} else {
					// get closer to maxWisdomTarget
//					System.out.println("GET CLOSER");
					debug("GET CLOSER TO ENGAGED TARGET");
					waitingForTarget = true;
					nTurnsWaited = 0;
					target = maxWisdomTarget;
					return getCloser(selfPlayer, maxWisdomTarget);
				}
			} else {
				debug("START CHAT CLOSEST");
				return closestTarget;
			}

		}
		// return a random move
		debug("no productive move RANDMOVE");
		return randomMoveInRoom(self);
	}

	public Point randomMoveInRoom(Point current) {
		Point move = randomMove();
		while(move.x + current.x > 20 || move.y + current.y > 20 || move.x + current.x < 0 || move.y + current.y < 0) {
			move = randomMove();
		}
		// System.out.println("Self " + self_id + " Moving");
		return move;
	}

	private Point randomMove(){
		double dir = random.nextDouble() * 2 * Math.PI;
		double dx = 6 * Math.cos(dir);
		double dy = 6 * Math.sin(dir);
		preChatId = self_id;
		return new Point(dx, dy, self_id);
	}

	public boolean isAlone(Integer id, Point[] players, int[] chat_ids){
		int i = 0, j = 0;
		while (players[i].id != id) i++;
		while (players[j].id != chat_ids[i]) j++;
		return i == j;

	}
	public Point pickTarget1(Point[] players, int[] chat_ids){
		Point self = selfPlayer;
		double minDis = Double.MAX_VALUE;
		int targetId = 0;
		boolean find = false;
		for (Point p : players) {
			if(p.id == self.id)
				continue;
			// compute squared distance
			double dx = self.x - p.x;
			double dy = self.y - p.y;
			double dd = dx * dx + dy * dy;
			if(dd < .25)
				return null;
			if (dd >= 0.25 && dd <= 4.0 && dd < minDis){
				find = true;
				targetId = p.id;
				minDis = dd;
			}
		}
		if(find && isAlone(targetId, players, chat_ids) && W[targetId] != 0){
			preChatId = targetId;
			return new Point(0.0, 0.0, targetId);
		}
		return null;
	}
	public Point pickTarget2(Point[] players, double distance, int[] chat_ids){
		int maxWisdom = 0;
		Point maxTarget = null;

		if (distance > 6.0) 
			distance = 6.0;

		for (int i = 0; i < players.length; i++){

			// not conversing with anyone
			if (players[i].id != chat_ids[i]) {
				continue;
			} else {
				if (W[players[i].id] > maxWisdom) {

					maxWisdom = W[players[i].id];
					maxTarget = players[i];
				}
			}
			// swap with maxWisdom and maxTarget if wiser
			 //System.out.println("this wisdom: " + W[players[i].id]);
			
//			System.out.println("max wisdom: " + maxWisdom);
		}


		return maxTarget;
	}

	public Point getCloser(Point self, Point target){
//		debug("get closer");
		//can't set to 0.5, if 0.5 the result distance may be 0.49
		double targetDis = 0.52;
		double dis = distance(self, target);
		double x = (dis - targetDis) * (target.x - self.x) / dis;
		double y = (dis - targetDis) * (target.y - self.y) / dis;
//		System.out.println("self pos: " + self.x + ", " + self.y);
//		System.out.println("target pos: " + target.x + ", " + target.y);
//		System.out.println("move pos: " + x + ", " + y);
		return new Point(x, y, self_id);
	}
	
	public Point getCloserWithID(Point self, Point target, int id) {
		double targetDis = 0.6;
		double dis = distance(self, target);
		double x = (dis - targetDis) * (target.x - self.x) / dis;
		double y = (dis - targetDis) * (target.y - self.y) / dis;
		return new Point(x, y, id);
	}
	
	public double distance(Point p1, Point p2){
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public void debug(String str){
		//po.println(tick+"\t"+str);
		//if(tick%100 == 0) po.flush();
	}
	
	public void debugNoNewline(String str) {
		//po.print(tick+"\t"+str+"\t");
	}

}
