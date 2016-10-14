package Game.Map;

import java.util.ArrayList;

/**
 * Represent the complete earth map
 */
public class Map {

    private ArrayList<Territory> territories = new ArrayList<>();

    public ArrayList<Territory> getTerritories() { return territories; }

    public Map() {
        for (Territories t: Territories.values()
             ) {
            if(t == Territories.Jolly1 || t == Territories.Jolly2)
                continue;

            territories.add(new Territory(t));
        }
    }
}
