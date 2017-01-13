package Game.Connection;

import Game.Map.Maps;
import Game.Player;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Match initialization message
 */
public class Match<T extends Player> {

    public final int Id;

    public final String Name;

    public final ArrayList<T> Players;

    public final Maps MapName;

    public Match(int Id, String Name, Maps MapName, Collection<T> Players) {
        this.Id = Id;
        this.Name = Name;
        this.MapName = MapName;
        this.Players = Players != null ? new ArrayList<>(Players) : null;
    }

    public Match(int Id, String Name, Maps MapName, T Player) {
        this.Id = Id;
        this.Name = Name;
        this.MapName = MapName;
        this.Players = Player != null ? new ArrayList<>(Collections.singletonList(Player)) : null;
    }
}
