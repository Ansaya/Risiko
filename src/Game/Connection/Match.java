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

    public final Maps GameMap;

    public Match(int Id, String Name, Maps GameMap, Collection<T> Players) {
        this.Id = Id;
        this.Name = Name;
        this.GameMap = GameMap;
        if(Players != null)
            this.Players.addAll(Players);
    }

    public Match(int Id, String Name, Maps GameMap, T Player) {
        this.Id = Id;
        this.Name = Name;
        this.GameMap = GameMap;
        if(Player != null)
            this.Players.add(Player);
    }

    private Match() {
        Id = 0;
        Name = "";
        GameMap = null;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Match && ((Match)other).Id == this.Id;
    }
}
