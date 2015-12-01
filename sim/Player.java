package wtr.sim;

public interface Player {

	// id        -> unique id of player
	// friends   -> unique ids of friends
	// strangers -> number of strangers
	public void init(int   id,
	                 int[] friend_ids,
	                 int   strangers);

	// players     -> nearby players (including yourself)
	// chat_ids    -> who the nearby players are chatting with (-1 if unknown)
	// wiser       -> a point of wisdom was gained in the last turn
	// more_wisdom -> how many wisdom points remain (-1 if unknown)
	// (return)    -> return your next action
	//    x, y -> dx, dy of movement (set to 0 to not move)
	//    id   -> set to your conversation target or your own if none
	public Point play(Point[] players,
	                  int[] chat_ids,
	                  boolean wiser,
	                  int more_wisdom);
}
