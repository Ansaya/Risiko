package Server.Game.Map;

import Game.Map.Territories;
import java.util.HashMap;

/**
 * Represent the complete earth map
 */
public class Map {

    private final HashMap<Territories, Territory> territories = new HashMap<>();

    public Territory getTerritory(Territories Territory) {
        return territories.get(Territory);
    }

    public Map() {
        for (Territories t: Territories.values()) {
            if(t == Territories.Jolly1 || t == Territories.Jolly2)
                continue;

            territories.put(t, new Territory(t));
        }
    }
}
