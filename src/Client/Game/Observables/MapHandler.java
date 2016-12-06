package Client.Game.Observables;

import Client.Main;
import Client.Game.ServerTalk;
import Game.Connection.MapUpdate;
import Game.Map.Territories;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler for UI events on game map
 */
public class MapHandler {

    protected static Pane mapPane;

    private final HashMap<Territories, ObservableTerritory> territories = new HashMap<>();

    public HashMap<Territories, ObservableTerritory> getTerritories() { return this.territories; }

    private final ArrayList<ObservableTerritory> selectedQueue = new ArrayList<>();

    public static final Object goAhead = new Object();

    public static final AtomicInteger newArmies = new AtomicInteger(0);

    public void selected(ObservableTerritory Selected) {
        synchronized (selectedQueue) {
            selectedQueue.add(Selected);
            selectedQueue.notify();
        }
    }

    public MapHandler(Pane MapPane, HashMap<Territories, ObservableTerritory> Map) {
        this.mapPane = MapPane;
        this.territories.putAll(Map);
    }

    /**
     * Remove all UI controls from the map
     */
    public void clearUI() {
        Platform.runLater(() -> {
            // Remove all controls relative to territories management
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
    public MapUpdate<ObservableTerritory> positionArmies(int NewArmies) {
        this.newArmies.set(NewArmies);

        // If only one army to place then is setup phase
        boolean isSetup = NewArmies <= 1;

        // Display positioning controls only for territories owned from current user
        final ObservableUser current = ServerTalk.getInstance().getUser();

        // If is setup phase user can choose all territories
        if(isSetup){
            territories.forEach((territory, obTerritory) -> {
                if(obTerritory.getOwner() == null)
                    obTerritory.positioningControls(false);
            });
        }
        else { //Else armies can be placed only in owned territories
            territories.forEach((territory, obTerritory) -> {
                if (obTerritory.getOwner().equals(current))
                    obTerritory.positioningControls(false);
            });
        }

        Main.showDialog("Positioning message", "You have " + NewArmies + " to place.", "Start displacement");

        // Wait till UI send notification of displacement completed
        synchronized (goAhead){
            try {
                goAhead.wait();
            } catch (InterruptedException e) {}
        }

        // Remove UI controls
        clearUI();

        // Check for updated territories
        final ArrayList<ObservableTerritory> updated = new ArrayList<>();
        territories.forEach((territory, obTerritory) -> {
            // Add territory to update only if modified
            if(obTerritory.newArmies.get() != 0)
                updated.add(obTerritory);
        });

        // Return update message to be sent back to the server
        return new MapUpdate<>(updated);
    }
}
