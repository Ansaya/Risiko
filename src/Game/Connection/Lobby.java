package Game.Connection;

import java.util.ArrayList;

/**
 * Lobby packet
 */
public class Lobby<T> {

    public final ArrayList<T> toAdd = new ArrayList<>();

    public final ArrayList<T> toRemove = new ArrayList<>();

    public Lobby(ArrayList<T> ToAdd, ArrayList<T> ToRemove) {
        if(ToAdd != null)
            ToAdd.forEach((p) -> toAdd.add(p));

        if(ToRemove != null)
            ToRemove.forEach((p) -> toRemove.add(p));
    }

    public Lobby(T ToAdd, T ToRemove) {
        if(ToAdd != null)
            toAdd.add(ToAdd);

        if(ToRemove != null)
            toRemove.add(ToRemove);
    }
}
