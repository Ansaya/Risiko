package Game.Connection;

import java.util.ArrayList;

/**
 * Match initialization message
 */
public class Match<T> {

    public ArrayList<T> players = new ArrayList<>();

    public Match(ArrayList<T> Players) {
        this.players = Players;
    }
}
