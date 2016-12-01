package Client;

import Client.Observables.ObservableTerritory;
import Game.Connection.MapUpdate;
import Game.Map.Territories;
import Game.Map.Territory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by fiore on 04/11/2016.
 */
public class MapHandler {

    private HashMap<Territories, ObservableTerritory> territories = new HashMap<>();

    public HashMap<Territories, ObservableTerritory> getTerritories() { return this.territories; }

    private volatile ArrayList<ObservableTerritory> selectedQueue = new ArrayList<>();

    public volatile boolean goAhead = false;

    public void selected(ObservableTerritory Selected) {
        synchronized (selectedQueue) {
            selectedQueue.add(Selected);
            selectedQueue.notify();
        }
    }

    public MapHandler(HashMap<Territories, ObservableTerritory> Map) {
        this.territories = Map;
    }

    public MapUpdate positionArmies(int Armies) {

        while (!goAhead) {

        }

        return new MapUpdate();
    }
}
