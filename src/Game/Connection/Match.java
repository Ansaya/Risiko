package Game.Connection;

import Game.Player;
import java.util.ArrayList;

/**
 * Match initialization message
 */
public class Match<T extends Player> {

    public final int Id;

    public final T Player;

    public final String MapName;

    public Match(int Id, String MapName, T Player) {
        this.Id = Id;
        this.MapName = MapName;
        this.Player = Player;
    }
}
