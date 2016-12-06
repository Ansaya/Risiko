package Server.Game.Map;

import Game.Map.Territories;

import java.util.HashMap;

/**
 * Represent the complete earth map
 */
public class Map {

    public final HashMap<Territories, Territory> territories = new HashMap<>();

    public Map() {
        for (Territories t: Territories.values()) {
            if(t == Territories.Jolly1 || t == Territories.Jolly2)
                continue;

            territories.put(t, new Territory(t));
        }
    }
}
