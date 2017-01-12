package Game.Connection;

import java.util.ArrayList;

/**
 * Match initialization message
 */
public class Match<T> {

    public final ArrayList<T> Players;

    public final String MapName;

    public Match(String MapName, ArrayList<T> Players) {
        this.MapName = MapName;
        this.Players = Players;
    }
}
