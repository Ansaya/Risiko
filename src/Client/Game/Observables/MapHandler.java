package Client.Game.Observables;

import Client.Game.Connection.MessageType;
import Client.Game.GameController;
import Client.Main;
import Game.Connection.Battle;
import Game.Connection.MapUpdate;
import Game.Connection.SpecialMoving;
import Game.Map.Map;
import Game.Map.Mission;
import Game.Sounds.Sounds;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import static Client.Game.Observables.ObservableTerritory.SelectionType.*;

/**
 * Handler for UI events on game map
 */
public class MapHandler {

    private final GameController gameController = GameController.getInstance();

    /**
     * Button to control phase completion
     */
    private volatile Button endPhaseBtn;

    public void setPhaseButton(Button PhaseBtn) {
        endPhaseBtn = PhaseBtn;
        endPhaseBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            endPhaseBtn.setDisable(true);
            endPhase();
        });
        endPhaseBtn.setDisable(true);
    }

    private volatile Label newArmiesLabel;

    public void  setArmiesLabel(Label ArmiesLabel) {
        newArmiesLabel = ArmiesLabel;
        newArmiesLabel.textProperty().bind((new SimpleStringProperty("Armies:  ")).concat(newArmies));
        newArmiesLabel.setVisible(false);
    }

    private volatile Mission mission;

    public void setMission(Mission Mission) {
        mission = Mission;

        Main.showDialog("Mission", "You received your mission to win the game", "Continue");
    }

    private JFXDialog getMissionDialog() {
        return Main.getDialog("Mission", mission != null ? mission.Description : "Mission not yet received.", "Continue");
    }

    public void setMissionButton(Button MissionBtn) {
        MissionBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> Main.showDialog(getMissionDialog()));
    }

    private final HashMap<Integer, ObservableUser> usersList = new HashMap<>();

    public final Map<ObservableTerritory> map;

    private final ObservableUser nullUser = new ObservableUser(-100, "NullUser", null);

    private final ArrayList<SelectedTerritory> selectedQueue = new ArrayList<>();

    private volatile boolean canSelect = false;

    void selected(ObservableTerritory Selected, boolean IsRightClick) {
        if(!canSelect)
            return;

        synchronized (selectedQueue) {
            selectedQueue.add(new SelectedTerritory(Selected, IsRightClick));
            selectedQueue.notify();
        }
    }

    private SelectedTerritory waitSelected(){
        synchronized (selectedQueue){
            try {
                selectedQueue.wait();
            } catch (Exception e) {
                System.err.println("MapHandler: Interrupted exception");
                return null;
            }
        }

        if(selectedQueue.isEmpty())
            return null;

        return selectedQueue.remove(0);
    }

    private SelectedTerritory waitSelected(ObservableUser Owner, boolean Exclude) {
        SelectedTerritory st;
        if(Owner == null)
            Owner = new ObservableUser(-100, "NullUser", null);

        while ((st = waitSelected()) != null){
            if(Owner.equals(st.Selected.getOwner()) ^ Exclude)
                return st;
        }

        return null;
    }

    private void endPhase() {
        canSelect = false;
        synchronized (selectedQueue){
            selectedQueue.notify();
        }
    }

    private final AtomicBoolean attackPhase = new AtomicBoolean(false);

    /**
     * Global counter for armies to be displaced
     */
    private final IntegerProperty newArmies = new SimpleIntegerProperty(0);

    /**
     * Increment global armies counter
     */
    private void addNewArmy() {
        synchronized (newArmies) {
            newArmies.set(newArmies.add(1).get());
        }
    }

    /**
     * Decrement global armies counter if possible
     *
     * @return 1 if counter has been decremented, 0 if not possible to decrement
     */
    private int removeNewArmy() {
        if(newArmies.get() == 0)
            return 0;

        synchronized (newArmies) {
            newArmies.set(newArmies.subtract(1).get());
        }

        return 1;
    }

    public MapHandler(String MapName, Pane MapPane, ArrayList<ObservableUser> UsersList) throws ClassNotFoundException {

        try {
            map = new Map<>(MapName, ObservableTerritory.class);
            map.loadGraphic();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ClassNotFoundException("Can not load requested map.");
        }

        ObservableTerritory.setMapPane(MapPane);
        final SVGPath connections = new SVGPath();
        connections.setContent(map.ConnectionsPath);
        connections.setStroke(Color.BLACK);
        MapPane.getChildren().add(connections);

        if(UsersList != null)
            UsersList.forEach(u -> usersList.put(u.id.get(), u));

        MapPane.getChildren().forEach(l -> {
            if(l instanceof Label) map.getTerritory(l.getId()).initTerritory(this, (Label)l);
        });
    }

    /**
     * Update current map with received information from server
     *
     * @param MapUpdate MapUpdate message from server
     */
    public void updateMap(MapUpdate<ObservableTerritory> MapUpdate) {
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(() -> updateMap(MapUpdate));
            return;
        }

        MapUpdate.Sound.play();

        MapUpdate.updated.forEach((u) -> {
            synchronized (map) {
                ObservableTerritory t = map.getTerritory(u.Name);
                t.Armies.set(u.Armies.get());
                t.NewArmies.set(0);
                if (!t.getOwner().equals(u.getOwner()))
                    t.setOwner(usersList.get(u.getOwner().id.get()));
            }
        });

        // If standard armies displacement play relative sound
        if(MapUpdate.attackDice != null) {
            // Display dice result somewhere
        }

        if(attackPhase.get())
            synchronized (attackPhase){
                attackPhase.notify();
            }
    }

    /**
     * Enable UI controls to position new armies over user territories.
     *
     * @param NewArmies New armies quantity
     * @return At the end of positioning returns updated territory message to send back to the server
     */
    public MapUpdate<ObservableTerritory> positionArmies(int NewArmies) {

        final boolean isSetup = NewArmies == 1;

        Platform.runLater(() -> {
            if(!isSetup) {
                endPhaseBtn.setDisable(false);
                endPhaseBtn.setText("End displacement");
            }
            newArmiesLabel.setVisible(true);
            newArmies.set(NewArmies);
        });

        Main.showDialog("Positioning message", "You have " + NewArmies + " new armies to place.", "Place armies");

        // Display positioning controls only for territories owned From current user
        final ObservableUser current = gameController.getUser();

        // Handle user interaction
        canSelect = true;
        while (canSelect){
            SelectedTerritory st;

            // If setup wait for an empty Territory To be selected
            if(isSetup)
                st = waitSelected(null, false);
            else // Else only current user territories can be selected
                st = waitSelected(current, false);

            // End phase button has been pressed
            if(st == null)
                break;

            if(st.IsRightClick)
                removeArmyFrom(st.Selected, false);
            else
                addArmyTo(st.Selected, false);
        }

        // Disable button and remove Armies label
        Platform.runLater(() -> {
            newArmiesLabel.setVisible(false);
            newArmies.set(0);
        });

        // Check for updated territories
        final MapUpdate<ObservableTerritory> update = new MapUpdate<>();
        map.getTerritories().forEach(territory -> {
            // Add Territory To update only if modified
            if(territory.NewArmies.get() != 0)
                update.updated.add(territory);
        });

        // Return update message To be sent back To the server
        return update;
    }

    /**
     * Handle all events relative to attack phase and send end phase message back to server at the end
     *
     */
    public void attackPhase(){
        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText("End attack phase");
        });

        attackPhase.set(true);

        final ObservableUser current = gameController.getUser();

        Main.showDialog("Attack phase",
                           "Select territory to attack, then your attacking territory and select how many armies to use from the prompt.",
                         "Continue");

        canSelect = true;
        while (canSelect) {
            // Get territory To attack
            SelectedTerritory st = waitSelected(current, true);
            if(st == null) break;
            final ObservableTerritory defender = st.Selected;
            defender.select(Defense);

            // Get attacker territory
            st = waitSelected(current, false);
            if(st == null) break;
            // Disable selection To avoid errors
            canSelect = false;
            final ObservableTerritory attacker = st.Selected;
            attacker.select(Attack);

            // If territories are not adjacent show error
            if(!defender.isAdjacent(attacker)){
                Main.showDialog("Attack error", "You can't start battle between two non adjacent territories.", "Continue");
            }
            else if(attacker.Armies.get() == 1){ // If not enough armies are present show error
                Main.showDialog("Attack error", "You can't start battle from territory with one army only", "Continue");
            }
            else { // Else perform battle
                Platform.runLater(() -> endPhaseBtn.setDisable(true));

                // Request attacking armies
                int atkArmies = 1;

                // If only two armies on territory only one can attack
                // else ask player how many to use
                if (attacker.Armies.get() > 2)
                    atkArmies = attacker.requestAttack();

                // Send battle to server
                gameController.SendMessage(MessageType.Battle, new Battle<>(attacker, defender, atkArmies));

                // Wait for battle completion
                synchronized (attackPhase) {
                    try {
                        attackPhase.wait();
                    } catch (Exception e) {}
                }

                Platform.runLater(() -> endPhaseBtn.setDisable(false));
            }

            defender.select(None);
            attacker.select(None);
            // Enable selection again
            canSelect = true;
        }

        attackPhase.set(false);
    }

    /**
     * Enable positioning controls to let the user move armies to newly conquered territory from attacking territory
     *
     * @param SpecialMoving SpecialMoving message received from server
     * @return Response message with updated displacement to send back to server
     */
    public SpecialMoving<ObservableTerritory> specialMoving(SpecialMoving<ObservableTerritory> SpecialMoving) {
        if(SpecialMoving.From == null)
            return endTurnMove();

        // Enable end phase button
        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText("Continue");
        });

        // Get territories from local map
        final ObservableTerritory from = map.getTerritory(SpecialMoving.From.Name);
        final ObservableTerritory to = map.getTerritory(SpecialMoving.To.Name);

        // Update territories to after battle state
        Platform.runLater(() -> {
            synchronized (map) {
                from.Armies.set(1);
                from.NewArmies.set(SpecialMoving.From.Armies.get() - 1);
                to.Armies.set(SpecialMoving.To.Armies.get());
                to.setOwner(usersList.get(from.getOwner().id.get()));
            }
        });

        canSelect = true;
        while (canSelect){
            SelectedTerritory st = waitSelected();

            // User has pressed continue button
            if(st == null)
                break;

            // Can select only 'to' or 'from'
            if(!st.Selected.equals(from) && !st.Selected.equals(to)) {
                Main.showDialog("Special moving error", "Perform this move only between highlighted territories.", "Continue");
                continue;
            }

            if(st.IsRightClick)
                removeArmyFrom(st.Selected, false);
            else
                addArmyTo(st.Selected, false);
        }

        Platform.runLater(() -> endPhaseBtn.setText("End attack phase"));

        // Notify attackPhase to go ahead with execution
        synchronized (attackPhase){
            attackPhase.notify();
        }

        // If user has not moved Armies return null response
        if(to.NewArmies.get() == 0)
            return new SpecialMoving<>(null, null);

        // Else return new displacement
        return new SpecialMoving<>(from, to);
    }

    /**
     * Enable positioning controls between two selected territories to perform final movement at the end of turn
     *
     * @return Updated territories state after moving
     */
    private SpecialMoving<ObservableTerritory> endTurnMove() {
        // Enable end phase button
        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText("End turn");
            Main.showDialog("Positioning message",
                               "At the of your turn you can move armies between two adjacent territories",
                             "Continue");
        });

        final ObservableUser current = gameController.getUser();

        ObservableTerritory to = null, from = null;

        canSelect = true;
        while (canSelect){
            SelectedTerritory st = waitSelected(current, false);
            if(st == null) break;

            to = st.Selected;
            to.select(Normal);

            st = waitSelected(current, false);
            if(st == null) break;

            from = st.Selected;
            from.select(Normal);

            // Show errors if territories are not adjacent or from territory has only one army
            if(!from.isAdjacent(to)) {
                Main.showDialog("Moving error", "You can't move armies between two non adjacent territories.", "Continue");
            }
            else if (from.Armies.get() == 1){
                Main.showDialog("Moving error", "You can't move armies from territory with one army only", "Continue");
            }
            else{
                // Else handle movement til endPhaseBtn is pressed
                while (canSelect) {
                    Platform.runLater(() -> endPhaseBtn.setText("Continue"));

                    st = waitSelected(current, false);
                    if(st == null) break;

                    boolean isFrom = st.Selected.equals(from);

                    if(st.IsRightClick)
                        removeArmyFrom(st.Selected, isFrom);
                    else
                        addArmyTo(st.Selected, isFrom);

                    if(to.NewArmies.get() != 0)
                        Platform.runLater(() -> endPhaseBtn.setText("End Turn"));
                }

                // If nothing changed expect another selection
                if(to.NewArmies.get() == 0)
                    canSelect = true;
            }

            to.select(None);
            from.select(None);
        }

        if(to == null || to.NewArmies.get() == 0)
            return new SpecialMoving<>(null, null);

        return new SpecialMoving<>(from, to);
    }

    /**
     * Display dialog and request player to chose how many armies to use for defense
     *
     * @param Battle Battle message received from server
     * @return Updated battle message to send back to server
     */
    public Battle<ObservableTerritory> requestDefense(Battle<ObservableTerritory> Battle) {
        Battle.defArmies = map.getTerritory(Battle.to.Name).requestDefense(Battle);

        return Battle;
    }

    /**
     * Add an army to specified territory from global armies counter
     *
     * @param Territory Territory to update
     * @param IsMoving True if adding directly to Armies field, false to add to NewArmies field
     */
    private void addArmyTo(ObservableTerritory Territory, boolean IsMoving) {
        System.out.println("User want to add an army to " + Territory.toString());

        // If no armies can be moved return
        if(newArmies.get() == 0)
            return;

        final Object wait = new Object();

        if(IsMoving){
            // Else perform movement
            Platform.runLater(() -> {
                synchronized (Territory.Armies){
                    Territory.Armies.set(Territory.Armies.add(1).get());
                    removeNewArmy();
                }

                synchronized (wait){
                    wait.notify();
                }
            });

            // Wait for FXThread to update values
            synchronized (wait){
                try {
                    wait.wait();
                } catch (Exception e) {}
            }

            return;
        }

        // Else perform movement
        Platform.runLater(() -> {
            synchronized (Territory.NewArmies){
                Territory.NewArmies.set(Territory.NewArmies.add(removeNewArmy()).get());
            }

            synchronized (wait){
                wait.notify();
            }
        });

        // Wait for FXThread to update values
        synchronized (wait){
            try {
                wait.wait();
            } catch (Exception e) {}
        }

        // If owner is null then we are in setup phase, so update owner and end phase after choice
        if(Territory.NewArmies.get() != 0 && Territory.getOwner().equals(nullUser)) {
            Territory.setOwner(usersList.get(gameController.getUser().id.get()));
            canSelect = false;
        }
    }

    /**
     * Remove an army to specified terrtory and increment global armies counter
     *
     * @param Territory Territory to update
     * @param IsMoving True if removing directly from Armies field, false to remove from NewArmies field
     */
    private void removeArmyFrom(ObservableTerritory Territory, boolean IsMoving) {
        System.out.println("User want to remove an army from " + Territory.toString());

        final Object wait = new Object();

        if(IsMoving){
            // If no army can be moved return
            if(Territory.Armies.get() == 1)
                return;

            // Else perform movement
            Platform.runLater(() -> {
                synchronized (Territory.Armies){
                    Territory.Armies.set(Territory.Armies.subtract(1).get());
                    addNewArmy();
                }

                synchronized (wait){
                    wait.notify();
                }
            });

            // Wait for FXThread to update values
            synchronized (wait){
                try {
                    wait.wait();
                } catch (Exception e) {}
            }

            return;
        }

        // Check if new Armies have been placed on this Territory, then remove one
        if(Territory.NewArmies.get() == 0)
            return;

        Platform.runLater(() -> {
            synchronized (Territory.NewArmies) {
                Territory.NewArmies.set(Territory.NewArmies.subtract(1).get());
                addNewArmy();
            }

            synchronized (wait){
                wait.notify();
            }
        });

        // Wait for FXThread to update values
        synchronized (wait){
            try {
                wait.wait();
            } catch (Exception e) {}
        }
    }

    /**
     * Instance a selected territory and the selection mode (right/left click)
     */
    private class SelectedTerritory {
        final boolean IsRightClick;

        final ObservableTerritory Selected;

        SelectedTerritory(ObservableTerritory Selected, boolean IsRightClick){
            this.IsRightClick = IsRightClick;
            this.Selected = Selected;
        }
    }
}
