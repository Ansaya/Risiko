package Game.Connection;

import Game.Player;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Lobby packet
 */
public class Lobby<T extends Player> {

    public final ArrayList<T> toAdd = new ArrayList<>();

    public final ArrayList<T> toRemove = new ArrayList<>();

    public Lobby(Collection<T> ToAdd, Collection<T> ToRemove) {
        if(ToAdd != null)
            toAdd.addAll(ToAdd);

        if(ToRemove != null)
            toRemove.addAll(ToRemove);
    }

    public Lobby(T ToAdd, T ToRemove) {
        if(ToAdd != null)
            toAdd.add(ToAdd);

        if(ToRemove != null)
            toRemove.add(ToRemove);
    }
}
