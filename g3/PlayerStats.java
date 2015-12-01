package wtr.g3;

import wtr.sim.Point;

public class PlayerStats {

    public int id;
    public final int wisdom;
    public int wisdomRemaining;

    public PlayerStats(int id, int wisdom){
        this.id = id;
        this.wisdom = round(wisdom, 10);
        this.wisdomRemaining = this.wisdom;
    }

    private int round(int num, double nearest){
        return (int) (Math.ceil(num / nearest) * nearest);
    }

    public void setWisdomRemaining(int wisdomRemaining){
        this.wisdomRemaining = wisdomRemaining;
    }

    public Point getTalkMove(){
        return new Point(0.0, 0.0, this.id);
    }

    public boolean hasWisdom(){
        return wisdomRemaining > 0;
    }

    public String toString(){
        return id + ": " + wisdomRemaining + "/" + wisdom;
    }

}
