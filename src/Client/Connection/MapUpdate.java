package Client.Connection;

import Client.Observables.ObservableTerritory;
import java.util.ArrayList;

/**
 * Created by fiore on 05/12/2016.
 */
public class MapUpdate {
    /**
     * Updated territories on the map
     */
    public final ArrayList<ObservableTerritory> updated;

    public MapUpdate(ArrayList<ObservableTerritory> Updated) {
        this.updated = Updated;
    }

    public MapUpdate(ObservableTerritory Updated) {
        this.updated = new ArrayList<>();
        updated.add(Updated);
    }
}
