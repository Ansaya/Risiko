package Game.Connection;

import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;

/**
 * Match initialization message
 */
public class Match {

    private ArrayList<User> players;

    public ArrayList<User> getPlayers() { return this.players; }

    public Match(ArrayList<User> Players) {
        this.players = Players;
    }
}
