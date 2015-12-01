package wtr.g6;

import java.util.Comparator;

/**
 * Created by naman on 11/29/15.
 */
public class PureWisdomComparator implements Comparator<Person> {

    @Override
    public int compare(Person p1, Person p2) {
        int wisdom1 = p1.wisdom == -1 ? 100 : p1.wisdom;
        int wisdom2 = p2.wisdom == -1 ? 100 : p2.wisdom;

        return -(wisdom1 - wisdom2);
    }

}
