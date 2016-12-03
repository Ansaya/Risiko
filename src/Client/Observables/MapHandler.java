package Client.Observables;

import Client.ServerTalk;
import Game.Connection.MapUpdate;
import Game.Connection.User;
import Game.Map.Territories;
import Game.Map.Territory;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handler for UI events on game map
 */
public class MapHandler {

    protected static Pane mapPane;

    private HashMap<Territories, ObservableTerritory> territories = new HashMap<>();

    public HashMap<Territories, ObservableTerritory> getTerritories() { return this.territories; }

    private volatile ArrayList<ObservableTerritory> selectedQueue = new ArrayList<>();

    public volatile Boolean goAhead = false;

    public static volatile Integer newArmies = 0;

    public void selected(ObservableTerritory Selected) {
        synchronized (selectedQueue) {
            selectedQueue.add(Selected);
            selectedQueue.notify();
        }
    }

    public MapHandler(Pane MapPane, HashMap<Territories, ObservableTerritory> Map) {
        this.mapPane = MapPane;
        this.territories = Map;
    }

    /**
     * Remove all UI controls from the map
     */
    public void clearUI() {
        Platform.runLater(() -> {
            mapPane.getChildren().removeIf(node -> node.getId() == ("btnRemove"));

            // Notify action completed
            synchronized (goAhead){
                goAhead.notify();
            }
        });

        // Wait for UI thread to complete the action
        synchronized (goAhead) {
            try {
                goAhead.wait();
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Display UI controls to position new armies over user territories.
     *
     * @param NewArmies New armies quantity
     * @return At the end of positioning returns updated territory message to send back to the server
     */
    public MapUpdate positionArmies(int NewArmies) {
        this.newArmies = NewArmies;

        // Display positioning controls only for territories owned from current user
        User current = ServerTalk.getInstance().getUser();
        territories.forEach((territory, obTerritory) -> {
            if(obTerritory.getOwner().getUserId() == current.getUserId())
                obTerritory.positioningControls(true);
        });

        // Wait till UI send notification of displacement completed
        synchronized (goAhead){
            try {
                goAhead.wait();
            } catch (InterruptedException e) {}
        }

        clearUI();

        // Check for updated territories
        ArrayList<Territory> updated = new ArrayList<>();
        territories.forEach((territory, obTerritory) -> {
            // Add territory to update only if modified
            if(obTerritory.NewArmies.get() != 0){
                Territory t = new Territory(territory);
                t.addNewArmies(obTerritory.NewArmies.get());
            }
        });

        // Return update message to be sent back to the server
        return new MapUpdate(updated);
    }
}
