package Client.Game.Observables;

import Client.Main;
import Client.Game.ServerTalk;
import Game.Connection.MapUpdate;
import Game.Connection.SpecialMoving;
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

            endPhase();
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

    private static volatile boolean canSelect = false;

    private static final ArrayList<SelectedTerritory> selectedQueue = new ArrayList<>();

    static void selected(ObservableTerritory Selected, boolean IsRightClick) {
        if(!canSelect)
            return;

        synchronized (selectedQueue) {
            selectedQueue.add(new SelectedTerritory(Selected, IsRightClick));
            selectedQueue.notify();
        }
    }

    private static SelectedTerritory waitSelected(){
        synchronized (selectedQueue){
            try {
                selectedQueue.wait();
            } catch (Exception e) {
                System.err.println("UIHandler: Interrupted exception");
                return null;
            }
        }

        if(selectedQueue.isEmpty())
            return null;

        return selectedQueue.remove(0);
    }

    private static SelectedTerritory waitSelected(ObservableUser Owner) {
        SelectedTerritory st;
        while ((st = waitSelected())  != null){
            if(st.Selected.getOwner() == Owner)
                return st;
        }

        return null;
    }

    private static void endPhase() {
        synchronized (selectedQueue){
            selectedQueue.notify();
        }
    }

    public static final AtomicBoolean goAhead = new AtomicBoolean(false);

    /**
     * Global counter for armies to be displaced
     */
    private static final IntegerProperty newArmies = new SimpleIntegerProperty(0);

    static int getNewArmies() {
        return newArmies.get();
    }

    static void addNewArmy() {
        synchronized (newArmies){
            newArmies.set(newArmies.add(1).get());
        }
    }

    static int removeNewArmy() {
        if(newArmies.get() == 0)
            return 0;

        synchronized (newArmies){
            newArmies.set(newArmies.subtract(1).get());
        }

        return 1;
    }

    static volatile Territories newArmiesOwner;

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
            endPhaseBtn.setText("End displacement");
            newArmiesLabel.setVisible(true);
            newArmies.set(NewArmies);
        });

        // Display positioning controls only for territories owned from current user
        final ObservableUser current = ServerTalk.getInstance().getUser();

        Main.showDialog("Positioning message", "You have " + NewArmies + " new armies to place.", "Start displacement");

        while (true){
            SelectedTerritory st;
            if(NewArmies == 1)
                st = waitSelected(null);
            else
                st = waitSelected(current);

            if(st == null)
                break;

            // Do something

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

    /**
     * Enable positioning controls to let the user move armies to newly conquered territory from attacking territory
     *
     * @param SpecialMoving SpecialMoving message revceived from server
     * @return Response message with updated displacement to send back to server
     */
    public static SpecialMoving<ObservableTerritory> specialMoving(SpecialMoving<ObservableTerritory> SpecialMoving) {
        // Enable end phase button
        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText("Continue");
        });

        final ObservableTerritory from = territories.get(SpecialMoving.from.territory);
        final ObservableTerritory to = territories.get(SpecialMoving.to.territory);

        // Set armies in attacking territory as new armies to enable moving
        synchronized (from.armies){
            final int temp = from.armies.get();
            from.armies.set(1);
            synchronized (from.newArmies){
                from.newArmies.set(temp - 1);
            }
        }

        // Enable moving controls in from/to territory
        from.positioningControls(Enabled);
        to.positioningControls(Enabled);

        // Wait for user to complete moves
        synchronized (goAhead){
            try {
                goAhead.wait();
            } catch (Exception e) {}
        }

        from.positioningControls(Disabled);
        to.positioningControls(Disabled);

        // If user has not moved armies return null response
        if(from.newArmies.get() == 0)
            return new SpecialMoving<>(null, null);

        // Else return new displacement
        return new SpecialMoving<>(from, to);
    }

    private static class SelectedTerritory {
        final boolean IsRightClick;

        final ObservableTerritory Selected;

        SelectedTerritory(ObservableTerritory Selected, boolean IsRightClick){
            this.IsRightClick = IsRightClick;
            this.Selected = Selected;
        }
    }
}
