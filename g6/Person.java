package wtr.g6;

import wtr.sim.Point;

/**
 * Created by naman on 11/21/15.
 */
public class Person {

    int id;
    // prev thing might not work
//    Point prev_position;
    Point cur_position;
    //wisdom
    int wisdom;
    int chat_id;

    public Person(int id) {
        this(id, null, -1, id);
    }

    public Person(int id, int wisdom) {
        this(id, null, wisdom, id);
    }

    public Person(int id, Point cur_position, int wisdom, int chat_id) {
        this.id = id;
        this.cur_position = cur_position;
        this.wisdom = wisdom;
        this.chat_id = chat_id;
    }

    public void setNewPosition(Point new_position) {
//        prev_position = cur_position;
        cur_position = new_position;
    }

    @Override
    public boolean equals(Object o){
      if(o instanceof Person){
    	  Person c = (Person)o;
        return id == c.id && wisdom == c.wisdom;
      }
      return false;
    }
}
