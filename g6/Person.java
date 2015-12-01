package wtr.g6;

import wtr.sim.Point;

/**
 * Created by naman on 11/21/15.
 */
public class Person {

    int id;
    // prev thing might not work
//    Status prev_status;
    Status cur_status;
//    Point prev_position;
    Point cur_position;
    //wisdom
    int wisdom;
    int chat_id;

    public Person(int id) {
        this(id, Status.stayed, null, -1, id);
    }

    public Person(int id, int wisdom) {
        this(id, Status.stayed, null, wisdom, id);
    }

    public Person(int id, Status cur_status, Point cur_position, int wisdom, int chat_id) {
        this.id = id;
        this.cur_status = cur_status;
        this.cur_position = cur_position;
        this.wisdom = wisdom;
        this.chat_id = chat_id;
    }

    public void setNewPosition(Point new_position) {
//        prev_position = cur_position;
        cur_position = new_position;
    }

    public void setNewStatus(Status new_status) {
//        prev_status = cur_status;
        cur_status = new_status;
    }
    
    @Override
    public boolean equals(Object o){
      if(o instanceof Person){
    	  Person c = (Person)o;
        return id == c.id && wisdom == c.wisdom;
      }
      return false;
    }

    @Override
    public int hashCode() {
        int[] a = new int[] {id, wisdom};
        return java.util.Arrays.hashCode(a);
    }
}
