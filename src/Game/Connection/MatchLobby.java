package Game.Connection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by fiore on 12/01/2017.
 */
public class MatchLobby<T> {
    public final ArrayList<T> toAdd = new ArrayList<>();

    public final ArrayList<T> toRemove = new ArrayList<>();

    public MatchLobby(Collection<T> ToAdd, Collection<T> ToRemove) {
        if(ToAdd != null)
            toAdd.addAll(ToAdd);

        if(ToRemove != null)
            toRemove.addAll(ToRemove);
    }

    public MatchLobby(T ToAdd, T ToRemove) {
        if(ToAdd != null)
            toAdd.add(ToAdd);

        if(ToRemove != null)
            toRemove.add(ToRemove);
    }
}
