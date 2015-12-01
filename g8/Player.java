package wtr.g8;

import wtr.sim.Point;

import java.util.*;

public class Player implements wtr.sim.Player {

    // your own id
    private int self_id = -1;
    private int soulmate_id = -1;
    private int last_change = 0;
    // the remaining wisdom per player
    private double[] W = null;
    private int[] Wmax = null;
    private boolean[] Wb = null;
    private int[] Wus = null;
    private int strangers;
    private int friends;
    private int pchatid;
    private int tick = -1;
    private int interference_counter = 0;

    // random generator
    private Random random = new Random();

    private void println(String s) {
//        System.out.println(self_id + "\t" + tick + "\t|\t" + s);
    }

    // init function called once
    public void init(int id, int[] friend_ids, int strangers) {
        self_id = id;
        // initialize the wisdom array
        int N = friend_ids.length + strangers + 2;
        W = new double[N];
        Wmax = new int[N];
        Wb = new boolean[N];
        Wus = new int[N];
        for (int i = 0; i != N; ++i) {
            Wmax[i] = 0;
            W[i] = 0;
            Wb[i] = i == self_id;
            Wus[i] = 10;
        }
        Wb[self_id] = true;

        for (int friend_id : friend_ids) {
            W[friend_id] = 50;
            Wmax[friend_id] = 50;
            Wus[friend_id] = 50;
            Wb[friend_id] = true;
        }

        this.friends = friend_ids.length;
        this.strangers = strangers;

        computeMLE();
        pchatid = -1;
    }

    private double dist(Point a, Point b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    private boolean inRange(Point a, Point b, double mindist, double maxdist) {
        double d = dist(a, b);
        return d >= mindist && d <= maxdist;
    }

    private boolean inRange(Point a, Point b) {
        return inRange(a, b, 0.5, 2.0);
    }

    private int compare(Point a, Point b, Map<Integer, Integer> chat_lut) {
        int chata = chat_lut.get(a.id);
        int chatb = chat_lut.get(b.id);

        return Double.compare(Wus[b.id] - 500 * Integer.signum(chatb), Wus[a.id] - 500 * Integer.signum(chata));
    }

    private Point teleport(Point location, Map<Integer, Point> id_lut) {
        return teleport(location, 0, id_lut);
    }

    private Point teleport(Point location, double offset, Map<Integer, Point> id_lut) {
        Point self = id_lut.get(self_id);
        double x = Math.max(Math.min(location.x, 20), 0);
        double y = Math.max(Math.min(location.y, 20), 0);
        double dx = x - self.x;
        double dy = y - self.y;
        double r = Math.hypot(dx, dy) - offset;
        if (Math.abs(r) > 6) {
            r = Math.signum(r) * 6;
        }
        double th = Math.atan2(dy, dx);

        return new Point(r * Math.cos(th), r * Math.sin(th), self_id);

    }

    private Point teleportToHighestScore(Map<Integer, Point> id_lut,
                                         Map<Integer, Integer> chat_lut) {
        if (id_lut.size() <= 1) {
            return null;
        }

        List<Point> l = new ArrayList<>();

        for (Point p : id_lut.values()) {
            if (W[p.id] <= 0) {
                continue;
            }
            l.add(p);
        }

        if (l.isEmpty()) {
            return null;
        }

        Collections.sort(l, (a, b) -> compare(a, b, chat_lut));

        Point tgt = l.get(0);

        // compute a point that is 0.5 away
        println("teleport computed to  " + tgt.id);
        return teleport(tgt, 0.52, id_lut);
    }

    private Point teleportToHighestScoreDensity(Map<Integer, Point> id_lut) {
        if (id_lut.size() <= 1) {
            return null;
        }

        double x = 0, y = 0, s = 0;

        for (Point p : id_lut.values()) {
            if (W[p.id] <= 0) {
                continue;
            }
            s += W[p.id];
            x += W[p.id] * p.x;
            y += W[p.id] * p.y;
        }
        return teleport(new Point(x / s, y / s, self_id), id_lut);
    }

    void computeMLE() {
        int n_even = strangers / 3;
        if (strangers % 3 > 0) {
            n_even += 1;
        }
        int n_0 = n_even, n_10 = n_even, n_20 = n_even;

        for (int i = 0; i < Wmax.length; ++i) {
            if (Wmax[i] >= 50 || !Wb[i]) {
                // skip friends, soulmate, and unknown
                continue;
            }
            switch (Wmax[i]) {
                case 0:
                    --n_0;
                    break;
                case 10:
                    --n_10;
                    break;
                case 20:
                    --n_20;
                    break;
                default:
                    throw new RuntimeException("Should never enter here");
            }
        }

        int n_remaining = n_0 + n_10 + n_20;

        if (n_remaining != 0) {
            double mle = 0;
            // if W = 10, we have 2/3 chance of getting 10 points and 1/3 chance of getting 0
            mle += n_10 * 2.0 / 3 * 10;

            // if W = 20, we have 1/3 chance of getting 20 points and 1/3 chance of 10 and 1/3 chance of 0
            mle += n_20 * (1.0 / 3 * 20 + 1.0 / 3 * 10);

            if (soulmate_id < 0) {
                mle = (mle + 200) / (n_remaining + 1);
            } else {
                mle /= n_remaining;
            }

            for (int i = 0; i < W.length; ++i) {
                if (!Wb[i]) {
                    W[i] = mle;
                    Wus[i] = (int) (mle + 0.5);
                }
            }
        }
    }

    // play function
    public Point play(Point[] players, int[] chat_ids,
                      boolean wiser, int more_wisdom) {
        ++tick;
        Map<Integer, Point> id_lut = new HashMap<>(); // :: id -> Point
        Map<Integer, Integer> chat_lut = new HashMap<>(); // :: id -> id

        for (int i = 0; i < players.length; ++i) {
            id_lut.put(players[i].id, players[i]);
            if (chat_ids[i] == players[i].id) {
                chat_lut.put(players[i].id, -1);
            } else {
                chat_lut.put(players[i].id, chat_ids[i]);
            }
        }

        // find where you are and who you chat with
        Point self = id_lut.get(self_id);
        Integer chat_id = chat_lut.get(self_id);

        Point move = null;

        if (chat_id < 0) {
            interference_counter = 0;
            // not chatting with anyone right now!
            List<Point> very_close = new ArrayList<>();

            // find new chat buddy
            for (Point p : players) {
                // skip if no more wisdom to gain
                if (W[p.id] <= 0) {
                    continue;
                }

                // compute squared distance
                // start chatting if in range
                if (inRange(self, p, 0.5, 0.6)) {
                    very_close.add(p);
                }
            }

            Collections.sort(very_close, (a, b) -> compare(a, b, chat_lut));
            if (!very_close.isEmpty()) {
                move = new Point(0, 0, very_close.get(0).id);
                println("Initiating conversation with " + very_close.get(0).id);
            } else {
                move = teleportToHighestScore(id_lut, chat_lut);
            }

        } else {
            // record known wisdom
            W[chat_id] = more_wisdom;

            if (!Wb[chat_id]) {
                println("Just met " + chat_id + " with wisdom " + more_wisdom);
                Wb[chat_id] = true;
                Wus[chat_id] = more_wisdom;
                if (more_wisdom > 198) {
                    soulmate_id = chat_id;
                    Wmax[chat_id] = 200;
                } else if (more_wisdom > 18) {
                    Wmax[chat_id] = 20;
                } else if (more_wisdom > 8) {
                    Wmax[chat_id] = 10;
                } else {
                    Wmax[chat_id] = 0;
                }

                computeMLE();
            }

            if (wiser) {
                --Wus[chat_id];
                if (Wus[chat_id] < 0 && Wmax[chat_id] == 20) {
                    // this is a 20-20 stranger!
                    Wus[chat_id] += 10;
                }
            }

            if (Wus[chat_id] > more_wisdom) {
                Wus[chat_id] = more_wisdom;
            }

            // attempt to continue chatting if there is more wisdom
            println("was chatting with " + chat_id + " with remaining wisdom " + more_wisdom + " wiser: " +
                    (wiser ? 1 : 0) + " Wus: " + Wus[chat_id]);
            move = new Point(0.0, 0.0, chat_id);
            if (wiser) {
                interference_counter = 0;
            } else {
                ++interference_counter;
                if (interference_counter > 5 || more_wisdom == 0) {
                    if (more_wisdom == 0) {
                        println("Done talking with " + chat_id);
                    } else {
                        println("too much interference with " + chat_id);
                    }
                    move = teleportToHighestScore(id_lut, chat_lut);
                }
            }
        }

//        Point teleport = teleportToHighestScoreDensity(id_lut);
//
//        if (teleport != null) {
//            return teleport;
//        }

        if (pchatid >= 0 && chat_id < 0) {
            // left conversation this tick
            int time = tick - last_change;
            println("Left conversation with\t" + pchatid + "\tafter " + time + " ticks [Wmax: " + Wmax[pchatid] +
                    ", Wus: " + Wus[pchatid] + "]");
            boolean dislikes = false;

            if (time <= 1) {
                if (Wus[pchatid] > 0 && Wmax[pchatid] <= 20) {
                    println(pchatid + " doesn't like us");
                    Wus[pchatid] -= 5;
                    dislikes = true;
                }
            } else {
                println(pchatid + " might like us");
                Wus[pchatid] = (int) W[pchatid];
            }

            if (id_lut.containsKey(pchatid) && Wus[pchatid] > 0) {
                println("distance to " + pchatid + " is " + dist(self, id_lut.get(pchatid)));

                // try to get them back
                if (!dislikes) {
                    println("attempting to teleport to " + pchatid);
                    move = teleport(id_lut.get(pchatid), 0.5, id_lut);
                }
            }

        }

        if (chat_id != pchatid) {
            last_change = tick;
        }

        pchatid = chat_id;

        if (move != null) {
            if (move.id == self_id) {
                println("final move: teleporting to " + String.format("(%.02f, %.02f)", move.x, move.y));
            } else {
                println("final move: chat with " + move.id + " at distance " + dist(self, id_lut.get(move.id)));
            }
            return move;
        }

        // return a random move
        double dir = random.nextDouble() * 2 * Math.PI;
        double dx = 6 * Math.cos(dir) + self.x;
        double dy = 6 * Math.sin(dir) + self.y;
        println("final move: random teleport");
        return teleport(new Point(dx, dy, self_id), id_lut);
    }
}
