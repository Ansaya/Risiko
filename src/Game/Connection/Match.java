package Game.Connection;

import Game.Map.Maps;
import Game.Player;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Match initialization message
 */
public class Match<T extends Player> {

    public final int Id;

    public final String Name;

    public final ArrayList<T> Players = new ArrayList<>();

    public final Maps MapName;

    public Match(int Id, String Name, Maps MapName, Collection<T> Players) {
        this.Id = Id;
        this.Name = Name;
        this.MapName = MapName;
        if(Players != null)
            this.Players.addAll(Players);
    }

    public Match(int Id, String Name, Maps MapName, T Player) {
        this.Id = Id;
        this.Name = Name;
        this.MapName = MapName;
        if(Player != null)
            this.Players.add(Player);
    }
}
