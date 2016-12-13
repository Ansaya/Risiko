package Client.Game.Observables;

import Client.Game.Connection.MessageType;
import Client.Main;
import Client.Game.ServerTalk;
import Game.Connection.Battle;
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

    private static ServerTalk serverTalk = ServerTalk.getInstance();

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
        newArmiesLabel.textProperty().bind((new SimpleStringProperty("Available Armies:  ")).concat(newArmies));
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

    private static final ArrayList<SelectedTerritory> selectedQueue = new ArrayList<>();

    private static volatile boolean canSelect = false;

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

    private static SelectedTerritory waitSelected(ObservableUser Owner, boolean Exclude) {
        SelectedTerritory st;
        while ((st = waitSelected())  != null){
            if((st.Selected.getOwner() == Owner) ^ Exclude)
                return st;
        }

        return null;
    }

    private static void endPhase() {
        canSelect = false;
        synchronized (selectedQueue){
            selectedQueue.notify();
        }
    }

    public static final AtomicBoolean goAhead = new AtomicBoolean(false);

    private static final AtomicBoolean attackPhase = new AtomicBoolean(false);

    /**
     * Global counter for Armies to be displaced
     */
    private static final IntegerProperty newArmies = new SimpleIntegerProperty(0);

    private static void addNewArmy() {
        newArmies.set(newArmies.add(1).get());
    }

    private static int removeNewArmy() {
        if(newArmies.get() == 0)
            return 0;

        newArmies.set(newArmies.subtract(1).get());

        return 1;
    }

    private static volatile Territories newArmiesOwner;

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

    public static void updateMap(MapUpdate<ObservableTerritory> MapUpdate) {
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(() -> updateMap(MapUpdate));
            return;
        }

        MapUpdate.updated.forEach((u) -> {
            synchronized (territories) {
                ObservableTerritory t = territories.get(u.Territory);
                t.Armies.set(u.Armies.get());
                t.NewArmies.set(0);
                if (!u.getOwner().equals(t.getOwner()))
                    t.setOwner(u.getOwner());
            }
        });

        if(attackPhase.get())
            synchronized (attackPhase){
                attackPhase.notify();
            }
    }

    /**
     * Enable UI controls to position new Armies over user territories.
     *
     * @param NewArmies New Armies quantity
     * @return At the end of positioning returns updated Territory message to send back to the server
     */
    public static MapUpdate<ObservableTerritory> positionArmies(int NewArmies) {
        if(!UIHandler.goAhead.get())
            synchronized (UIHandler.goAhead) {
                try {
                    UIHandler.goAhead.wait();
                } catch (Exception e) {}
            }

        final boolean isSetup = NewArmies == 1;
        final boolean isMoving = NewArmies == 0;

        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText("End displacement");
            newArmiesLabel.setVisible(true);
            newArmies.set(NewArmies);
        });

        if(isMoving)
            newArmiesOwner = null;

        // Display positioning controls only for territories owned from current user
        final ObservableUser current = serverTalk.getUser();

        Main.showDialog("Positioning message", "You have " + NewArmies + " new Armies to place.", "Start displacement");

        // Handle user interaction
        canSelect = true;
        while (canSelect){
            SelectedTerritory st;

            // If setup wait for an empty Territory to be selected
            if(isSetup)
                st = waitSelected(null, false);
            else // Else only current user territories can be selected
                st = waitSelected(current, false);

            // End phase button has been pressed
            if(st == null)
                break;

            if(st.IsRightClick)
                removeArmyFrom(st.Selected, isMoving);
            else
                addArmyTo(st.Selected, isMoving);

        }

        // Disable button and remove Armies label
        Platform.runLater(() -> {
            endPhaseBtn.setDisable(true);
            newArmiesLabel.setVisible(false);
            newArmies.set(0);
        });

        // Check for updated territories
        final ArrayList<ObservableTerritory> updated = new ArrayList<>();
        territories.forEach((territory, obTerritory) -> {
            // Add Territory to update only if modified
            if(obTerritory.NewArmies.get() != 0)
                updated.add(obTerritory);
        });

        // Return update message to be sent back to the server
        return new MapUpdate<>(updated);
    }

    public static void attackPhase(){
        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText("End attack phase");
        });

        attackPhase.set(true);

        final ObservableUser current = serverTalk.getUser();

        Main.showDialog("Attack phase",
                           "Select territory to attack, then your attacking territory and select how many armies to use from the prompt.",
                         "Continue");

        canSelect = true;
        while (canSelect) {
            // Get territory to attack
            SelectedTerritory st = waitSelected(current, true);
            if(st == null) break;
            final ObservableTerritory defender = st.Selected;

            // Get attacker territory
            st = waitSelected(current, false);
            if(st == null) break;
            // Disable selection to avoid errors
            canSelect = false;
            final ObservableTerritory attacker = st.Selected;

            // Check if battle can take place
            if(!defender.Territory.isAdjacent(attacker.Territory)){
                Main.showDialog("Attack error", "You can't start battle between two non adjacent territories.", "Continue");
                continue;
            }

            if(attacker.Armies.get() == 1){
                Main.showDialog("Attack error", "You can't start battle from territory with one army only", "Continue");
                continue;
            }

            Platform.runLater(() -> endPhaseBtn.setDisable(true));

            // If check is passed request attacking armies
            int atkArmies = 1;

            // If only two armies on territory only one can attack
            // else ask to user how many to be used
            if(attacker.Armies.get() > 2)
                atkArmies = attacker.requestAttack();

            // Send battle to server
            serverTalk.SendMessage(MessageType.Battle, new Battle<>(attacker, defender, atkArmies));

            // Wait for battle completion
            synchronized (attackPhase){
                try {
                    attackPhase.wait();
                } catch (Exception e) {}
            }

            Platform.runLater(() -> endPhaseBtn.setDisable(false));
            // Enable selection again
            canSelect = true;
        }

        Platform.runLater(() -> endPhaseBtn.setDisable(true));
        attackPhase.set(false);

        // Report end of attack phase to server
        serverTalk.SendMessage(MessageType.Battle, new Battle<>(null, null, 0));
    }

    /**
     * Enable positioning controls to let the user move Armies to newly conquered Territory from attacking Territory
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

        final ObservableTerritory from = territories.get(SpecialMoving.from.Territory);
        final ObservableTerritory to = territories.get(SpecialMoving.to.Territory);

        // Set Armies in attacking Territory as new Armies to enable moving
        Platform.runLater(() -> {
            synchronized (from.Armies){
                final int temp = from.Armies.get();
                from.Armies.set(1);
                synchronized (from.NewArmies){
                    from.NewArmies.set(temp - 1);
                }
            }
        });

        canSelect = true;
        while (canSelect){
            // To avoid moving armies backward from 'to' to 'from'
            newArmiesOwner = from.Territory;

            SelectedTerritory st = waitSelected();

            // User has pressed continue button
            if(st == null)
                break;

            // Can select only 'to' or 'from'
            if(!st.Selected.equals(from) && !st.Selected.equals(to)) {
                Main.showDialog("Special moving error", "Perform this move only from attacking territory to newly conquered territory", "Continue");
                continue;
            }

            if(st.IsRightClick)
                removeArmyFrom(st.Selected, true);
            else
                addArmyTo(st.Selected, true);
        }

        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText("End attack phase");
        });

        // Notify attackPhase to go ahead with execution
        synchronized (attackPhase){
            attackPhase.notify();
        }

        // If user has not moved Armies return null response
        if(from.NewArmies.get() == 0)
            return new SpecialMoving<>(null, null);

        // Else return new displacement
        return new SpecialMoving<>(from, to);
    }

    public static Battle<ObservableTerritory> requestDefense(Battle<ObservableTerritory> Battle) {
        Battle.defArmies = territories.get(Battle.to.Territory).requestDefense(Battle);

        return Battle;
    }

    private static void addArmyTo(ObservableTerritory Territory, boolean IsMoving) {
        if(IsMoving){
            // If no armies can be moved return
            if(newArmies.get() == 0)
                return;

            // If user is moving Armies to non adjacent Territory notify error
            if(!Territory.Territory.isAdjacent(newArmiesOwner)){
                Main.showDialog("Moving error", "You can move Armies between adjacent territories only.", "Continue");
                return;
            }

            // Else perform movement
            Platform.runLater(() -> {
                synchronized (Territory.NewArmies){
                    Territory.NewArmies.set(Territory.NewArmies.add(removeNewArmy()).get());
                }
            });

            // If all Armies are moved back to owner Territory, reset newArmiesOwner
            if(newArmies.get() == 0 && newArmiesOwner == Territory.Territory)
                newArmiesOwner = null;

            return;
        }

        System.out.println("User want to add an army to " + Territory.Territory.toString());

        // If new army is available UIHandler.removeNewArmy() returns 1 else 0
        Platform.runLater(() -> {
            synchronized (Territory.NewArmies) {
                Territory.NewArmies.set(Territory.NewArmies.add(removeNewArmy()).get());
            }
        });

        // If owner is null then we are in setup phase, so update owner and end phase after choice
        if(Territory.NewArmies.get() != 0 && Territory.getOwner() == null) {
            Territory.setOwner(ServerTalk.getInstance().getUser());
            endPhase();
        }

    }

    private static void removeArmyFrom(ObservableTerritory Territory, boolean IsMoving) {
        if(IsMoving){
            // If no army can be moved return
            if(Territory.Armies.get() == 1)
                return;

            // If Armies owner is null set this Territory as owner
            if(newArmiesOwner == null)
                newArmiesOwner = Territory.Territory;

            // If user moves Armies from different territories notify error
            if(newArmiesOwner != Territory.Territory){
                Main.showDialog("Moving error", "You can move Armies from one Territory only.", "Continue");
                return;
            }

            // Else perform movement
            Platform.runLater(() -> {
                synchronized (Territory.Armies){
                    Territory.Armies.set(Territory.Armies.subtract(1).get());
                }
                addNewArmy();
            });

            return;
        }

        System.out.println("User want to remove an army from " + Territory.Territory.toString());

        // Check if new Armies have been placed on this Territory, then remove one
        if(Territory.NewArmies.get() > 0)
        Platform.runLater(() -> {
            synchronized (Territory.NewArmies) {
                Territory.NewArmies.set(Territory.NewArmies.subtract(1).get());
            }
            addNewArmy();
        });

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
