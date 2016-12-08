package Game.Connection;

import java.util.ArrayList;

/**
 * Map packet
 */
public class MapUpdate<T> {

    /**
     * Updated territories on the map
     */
    public final ArrayList<T> updated = new ArrayList<>();

    public MapUpdate(ArrayList<T> Updated) {
        this.updated.addAll(Updated);
    }

    public MapUpdate(T Updated) {
        updated.add(Updated);
    }
}
