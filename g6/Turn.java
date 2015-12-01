package wtr.g6;

/**
 * Created by naman on 11/24/15.
 */
public class Turn {

    int id;
    int chat_id_tried;
    boolean spoke;
    boolean wiser;

    public Turn(int id) {
        this(id, -1, false, false);
    }

    public Turn(int id, int chat_id_tried) {
        this(id, chat_id_tried, false, false);
    }

    public Turn(int id, int chat_id_tried, boolean spoke, boolean wiser) {
        this.id = id;
        this.chat_id_tried = chat_id_tried;
        this.spoke = spoke;
        this.wiser = wiser;
    }
}
