package Game.Connection;

import Game.Map.Mission;
import java.util.ArrayList;

/**
 * Match initialization message
 */
public class Match<T> {

    public ArrayList<T> Players = new ArrayList<>();

    public final Mission Mission;

    public Match(ArrayList<T> Players) {
        this.Players = Players;
        this.Mission = null;
    }

    public Match(ArrayList<T> Players, Mission Mission) {
        this.Players = Players;
        this.Mission = Mission;
    }
}
