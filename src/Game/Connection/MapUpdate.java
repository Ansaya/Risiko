package Game.Connection;

import Game.Map.Territory;
import java.util.ArrayList;

/**
 * Map packet
 */
public class MapUpdate {

    /**
     * Updated territories on the map
     */
    private ArrayList<Territory> updated;

    public ArrayList<Territory> getUpdated() { return this.updated; }

    public MapUpdate(ArrayList<Territory> Updated) {
        this.updated = Updated;
    }

    public MapUpdate(Territory Updated) {
        this.updated = new ArrayList<>();
        updated.add(Updated);
    }
}
