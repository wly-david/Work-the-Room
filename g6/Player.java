package wtr.g6;

import wtr.sim.Point;

import java.util.*;


public class Player implements wtr.sim.Player {

    int tick = 0;

	// your own id
	private int self_id = -1;
    private int soulmate_id = -1;

    // map of all people in the room
    Map<Integer, Person> people;

	// random generator
	private Random random = new Random();

	PriorityQueue<Person> maximum_wisdom_queue;
    WisdomComparator comparator;
    PureWisdomComparator pureWisdomComparator;

    int k_turn = 3;

    Map<Integer, Turn> turns;

	private boolean exhaust = false;
    private int last_move_turn = 0;

	// init function called once
	public void init(int id, int[] friend_ids, int strangers)
	{
        turns = new HashMap<Integer, Turn>();
        turns.put(0, new Turn(0, self_id));
        people = new HashMap<Integer, Person>();
		self_id = id;
		// initialize the wisdom array
		int N = friend_ids.length + strangers + 2;
		for (int i = 0 ; i != N ; ++i) {
            int wisdom = i == self_id ? 0 : -1;
            Person person = new Person(i, wisdom);
            people.put(i, person);
        }
		for (int friend_id : friend_ids) {
            people.get(friend_id).wisdom = 50;
        }
        comparator = new WisdomComparator(people.get(self_id));
        pureWisdomComparator = new PureWisdomComparator();
	}

	// play function
	public Point play(Point[] players, int[] chat_ids,
	                  boolean wiser, int more_wisdom)
	{
        Point response = new Point(0, 0, self_id);
        try {
            // update tracking parameters
            tick++;
            turns.put(tick, new Turn(tick));
            // find where you are and who you chat with
            int i = 0, j = 0;
            while (players[i].id != self_id) i++; // Find myself in the players array.
            while (players[j].id != chat_ids[i]) j++; // Find my chat-buddy (who I'm currently talking to)
            Point self = players[i];
            Point chat = players[j];
            // update wisdom of the person chatting with
            people.get(chat.id).wisdom = more_wisdom;
            if(more_wisdom > 50) {
                soulmate_id = chat.id;
            }

            Turn prev_turn = turns.get(tick - 1);
            prev_turn.spoke = prev_turn.chat_id_tried == chat_ids[i];
            prev_turn.wiser = wiser;

            // update people's info
            updatePeopleAndQueue(players, chat_ids);

            if(wiser && more_wisdom > 0) {
                response = new Point(0, 0, chat.id);
                return response;
            }

            // look for soulmate
            for(Point player: players) {
                if(player.id == soulmate_id) {
                    if(people.get(soulmate_id).wisdom > 0) {
                        if(Utils.inTalkRange(self, player) && lastKTurnsSuccessful(k_turn, soulmate_id)) {
                            response = new Point(0, 0, soulmate_id);
                            return response;
                        } else {
                            response = moveCloserToPerson(self, player);
                            return response;
                        }
                    }
                }
            }

            // if did not gain any wisdom in last 5 turns where we spoke, move to a random location
            if(!lastKTurnsSuccessfulSinceLastMoved(5)) {
                response = moveToARandomLocation();
                return response;
            }

            if (exhaust) {
                if (more_wisdom > 0 && chat_ids[j] == self.id && lastKTurnsSuccessful(k_turn, chat_ids[i])) {
                    System.out.println("EXHAUST: I, " + self.id + ", am talking to " + chat.id);
                    response = new Point(0, 0, chat.id);
                    return response;
                } else {
                    exhaust = false;
                }
            }

            while(!maximum_wisdom_queue.isEmpty()) {
                Person person = maximum_wisdom_queue.poll();
                if(lastKTurnsSuccessful(k_turn, person.id)) {
                    response = new Point(0, 0, person.id);
                    exhaust = true;
                    return response;
                }
            }

            // otherwise move to a new location
            response = moveToANewLocation(players);
            return response;

        } finally {
            turns.get(tick).chat_id_tried = response.id;
            if(response.x != 0 || response.y != 0) {
                last_move_turn = tick;
            }
        }
    }

    // if since last moved:
    //   -  there hasn't been k conversations, then it is considered successful
    //   -  if had at least 1 successful conversation in last k conversation, then it is successful
    //   -  if we had k unsuccessful conversations, then it is considered not successful
    private boolean lastKTurnsSuccessfulSinceLastMoved(int k) {
        int t = 0;
        for(int i = 1; t <= k && tick - i >= last_move_turn; i++) {
            if(!turns.containsKey(tick - i)) {
                return true;
            }
            Turn turn = turns.get(tick - i);
            if(turn.spoke) {
                t++;
                if(turn.wiser) {
                    return true;
                }
            }
        }
        if(t <= k) {
            return true;
        }
        return false;
    }

    // same as above, just checks for the id given
    private boolean lastKTurnsSuccessful(int k, int chat_id_tried) {
        int t = 0;
        for(int i = 1; t <= k && tick - i >= 0 && i <= 10; i++) {
            if(!turns.containsKey(tick - i)) {
                return true;
            }
            Turn turn = turns.get(tick - i);
            if(turn.chat_id_tried != chat_id_tried) {
                continue;
            }
            if(turn.spoke) {
                t++;
                if(turn.wiser) {
                    return true;
                }
            }
        }
        if(t <= k) {
            return true;
        }
        return false;
    }

    //
    private Point moveToANewLocation(Point[] players) {
        Point self = people.get(self_id).cur_position;
        PriorityQueue<Person> queue = new PriorityQueue<Person>(pureWisdomComparator);
        for(Point player: players) {
            double distance = Utils.distance(self, player);
            // if the person is not in talking range and has wisdom to offer, move to that person's location
            if(distance > 2 && distance <= 6 && people.get(player.id).wisdom != 0) {
                queue.offer(people.get(player.id));
            }
        }

        if(queue.size() > 0) {
            return moveCloserToPerson(self, queue.peek().cur_position);
        }
        // if no one found, move to a random position
        return moveToARandomLocation();
    }

    //
    private Point moveToARandomLocation() {
        Point self = people.get(self_id).cur_position;
        double dx = 0;
        double dy = 0;
        double x = -1;
        double y = -1;
        while(x < 0 || x > 20 || y < 0 || y > 20) {
            double dir = random.nextDouble() * 2 * Math.PI;
            dx = 6 * Math.cos(dir);
            dy = 6 * Math.sin(dir);
            x = self.x + dx;
            y = self.y + dy;
        }
        return new Point(dx, dy, self_id);
    }

    private Point moveCloserToPerson(Point self, Point player) {
        double theta = Math.atan2(player.y - self.y, player.x - self.x);
        double distance = Utils.distance(self, player);
        double new_distance = distance - 0.50;
        double dx = Math.abs(new_distance * Math.sin(theta));
        double dy = Math.abs(new_distance * Math.cos(theta));
        if(player.x - self.x < 0) {
            dx = -dx;
        }
        if(player.y - self.y < 0) {
            dy = -dy;
        }
        return new Point(dx, dy, self_id);
    }

    private void updatePeopleAndQueue(Point[] players, int[] chat_ids) {
        maximum_wisdom_queue = new PriorityQueue<Person>(comparator);
        Set<Integer> visibleIds = new HashSet<Integer>();
        for (int ii = 0; ii < players.length; ii++) {
            Point player = players[ii];
            int id = player.id;
            visibleIds.add(id);
            Person person = people.get(id);
            person.setNewPosition(player);
            person.chat_id = chat_ids[ii];
        }
        Person self = people.get(self_id);
        for(Point player: players) {
            Person other = people.get(player.id);
            if(Utils.inTalkRange(self.cur_position, other.cur_position) && other.wisdom != 0) {
                maximum_wisdom_queue.add(people.get(player.id));
            }
        }
        for(Integer id: people.keySet()) {
            if(visibleIds.contains(id)) {
                continue;
            }
            people.get(id).chat_id = -1;
        }
    }

}
