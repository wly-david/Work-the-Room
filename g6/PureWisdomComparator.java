package wtr.g6;

import java.util.Comparator;

/**
 * Created by naman on 11/29/15.
 */
public class PureWisdomComparator implements Comparator<Person> {

	int totalWisdomofStranger;
	int unknownPeople;

	public PureWisdomComparator(int totalWisdomofStranger, int unknownPeople)
	{
		this.totalWisdomofStranger = totalWisdomofStranger;
		this.unknownPeople = unknownPeople;
	}

    @Override
    public int compare(Person p1, Person p2) {
        int wisdom1 = p1.wisdom == -1 ? 100 : p1.wisdom;
        int wisdom2 = p2.wisdom == -1 ? 100 : p2.wisdom;
        int scale1 = p1.chat_id == p1.id ? 100 : 1;
        int scale2 = p2.chat_id == p2.id ? 100 : 1;

        return -(scale1*wisdom1 - scale2*wisdom2);
    }

}
