package Client.Game.Observables;

import Client.Main;
import Client.Game.ServerTalk;
import Game.Connection.MapUpdate;
import Game.Map.Territories;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static Client.Game.Observables.ObservableTerritory.PosControls.*;

/**
 * Handler for UI events on game map
 */
public class MapHandler {

    static Pane mapPane;

    public static final HashMap<Territories, ObservableTerritory> territories = new HashMap<>();

    private static final ArrayList<Territories> selectedQueue = new ArrayList<>();

    public static final AtomicBoolean goAhead = new AtomicBoolean(false);

    static final AtomicInteger newArmies = new AtomicInteger(0);

    static void selected(Territories Selected) {
        synchronized (selectedQueue) {
            selectedQueue.add(Selected);
            selectedQueue.notify();
        }
    }

    public static void Init(Pane MapPane, HashMap<Territories, ObservableTerritory> Map) {
        mapPane = MapPane;
        territories.putAll(Map);
        goAhead.set(true);
        synchronized (goAhead){
            goAhead.notify();
        }
    }

    /**
     * Display UI controls to position new armies over user territories.
     *
     * @param NewArmies New armies quantity
     * @return At the end of positioning returns updated territory message to send back to the server
     */
    public static MapUpdate<ObservableTerritory> positionArmies(int NewArmies) {
        newArmies.set(NewArmies);

        // If only one army to place then is setup phase
        boolean isSetup = NewArmies <= 1;

        // Display positioning controls only for territories owned from current user
        final ObservableUser current = ServerTalk.getInstance().getUser();

        // If is setup phase user can choose all territories
        if(isSetup){
            territories.forEach((territory, obTerritory) -> {
                if(obTerritory.getOwner() == null)
                    obTerritory.positioningControls(Enabled);
            });
        }
        else { //Else armies can be placed only in owned territories
            territories.forEach((territory, obTerritory) -> {
                if (obTerritory.getOwner().equals(current))
                    obTerritory.positioningControls(Enabled);
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
        territories.forEach((territory, obTerritory) -> obTerritory.positioningControls(Disabled));

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
