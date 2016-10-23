package Game.Map;

import java.util.HashMap;

/**
 * Represent the complete earth map
 */
public class Map {

    private HashMap<Territories, Territory> territories = new HashMap<>();

    public HashMap<Territories, Territory> getTerritories() { return territories; }

    public Map() {
        for (Territories t: Territories.values()
             ) {
            if(t == Territories.Jolly1 || t == Territories.Jolly2)
                continue;

            territories.put(t, new Territory(t));
        }
    }
}
