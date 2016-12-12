package Client.Game.Observables;

import Client.Main;
import Client.Game.ServerTalk;
import Game.Connection.MapUpdate;
import Game.Map.Mission;
import Game.Map.Territories;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import static Client.Game.Observables.ObservableTerritory.PosControls.*;

/**
 * Handler for UI events on game map
 */
public class UIHandler {

    /**
     * Container for all map territories, badges and labels
     */
    static Pane mapPane;

    /**
     * Button to control phase completion
     */
    private static Button endPhaseBtn;

    public static void setPhaseButton(Button PhaseBtn) {
        endPhaseBtn = PhaseBtn;
        endPhaseBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            endPhaseBtn.setDisable(true);
            synchronized (goAhead){
                goAhead.notify();
            }
        });
        endPhaseBtn.setDisable(true);
    }

    private static Label newArmiesLabel;

    public static void  setArmiesLabel(Label ArmiesLabel) {
        newArmiesLabel = ArmiesLabel;
        newArmiesLabel.textProperty().bind((new SimpleStringProperty("Available armies:  ")).concat(newArmies));
        newArmiesLabel.setVisible(false);
    }

    public static CardsHandler CardsHandler;

    public static Mission Mission;

    public static void setMissionButton(Button MissionBtn) {
        MissionBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> Main.showDialog("Mission", Mission.Description, "Continue"));
    }

    /**
     * Territories displayed in mapPane
     */
    public static final HashMap<Territories, ObservableTerritory> territories = new HashMap<>();

    private static final ArrayList<Territories> selectedQueue = new ArrayList<>();

    public static final AtomicBoolean goAhead = new AtomicBoolean(false);

    /**
     * Global counter for armies to be displaced
     */
    static final IntegerProperty newArmies = new SimpleIntegerProperty(0);

    static void selected(Territories Selected) {
        synchronized (selectedQueue) {
            selectedQueue.add(Selected);
            selectedQueue.notify();
        }
    }

    public static void Init(Pane MapPane, ArrayList<Label> Labels) {
        mapPane = MapPane;

        Labels.forEach(l -> {
            Territories t = Territories.valueOf(l.getId());
            territories.put(t, new ObservableTerritory(t, l));
        });

        goAhead.set(true);
        synchronized (goAhead){
            goAhead.notify();
        }
    }

    public static void Reset() {
        mapPane = null;
        territories.clear();
        selectedQueue.clear();
        newArmies.set(0);
        goAhead.set(false);
    }

    /**
     * Enable UI controls to position new armies over user territories.
     *
     * @param NewArmies New armies quantity
     * @return At the end of positioning returns updated territory message to send back to the server
     */
    public static MapUpdate<ObservableTerritory> positionArmies(int NewArmies) {
        if(!UIHandler.goAhead.get())
            synchronized (UIHandler.goAhead) {
                try {
                    UIHandler.goAhead.wait();
                } catch (Exception e) {}
            }

        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            newArmiesLabel.setVisible(true);
            newArmies.set(NewArmies);
        });

        // Display positioning controls only for territories owned from current user
        final ObservableUser current = ServerTalk.getInstance().getUser();

        // If is setup phase user can choose all territories
        if(NewArmies <= 1){
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

        Main.showDialog("Positioning message", "You have " + NewArmies + " new armies to place.", "Start displacement");

        // Wait till UI send notification of displacement completed
        synchronized (goAhead){
            try {
                goAhead.wait();
            } catch (Exception e) {
                System.err.println("UIHandler: Interrupted exception");
                return null;
            }
        }

        // Disable button and remove armies label
        endPhaseBtn.setDisable(true);
        newArmiesLabel.setVisible(false);

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
