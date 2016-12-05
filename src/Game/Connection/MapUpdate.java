package Game.Connection;

import java.util.ArrayList;

/**
 * Map packet
 */
public class MapUpdate<T> {

    /**
     * Updated territories on the map
     */
    public final ArrayList<T> updated;

    public MapUpdate(ArrayList<T> Updated) {
        this.updated = Updated;
    }

    public MapUpdate(T Updated) {
        this.updated = new ArrayList<>();
        updated.add(Updated);
    }
}
