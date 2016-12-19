package Server.Game.Map;

import Game.Map.RealWorldMap;
import java.util.HashMap;

/**
 * Represent the complete earth map
 */
public class Map {

    private final HashMap<RealWorldMap, Territory> territories = new HashMap<>();

    public Territory getTerritory(RealWorldMap Territory) {
        return territories.get(Territory);
    }

    public Map() {
        for (RealWorldMap t: RealWorldMap.values()) {
            if(t == RealWorldMap.Jolly1 || t == RealWorldMap.Jolly2)
                continue;

            territories.put(t, new Territory(t));
        }
    }
}
