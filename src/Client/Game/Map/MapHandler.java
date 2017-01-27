package Client.Game.Map;

import Client.Game.Connection.MessageType;
import Client.Game.GameController;
import Client.Game.Player;
import Client.Main;
import Game.Connection.Battle;
import Game.Connection.MapUpdate;
import Game.Connection.Match;
import Game.Logger;
import Game.Map.Map;
import Game.Map.Mission;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import java.util.Collection;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import static Client.Game.Map.Territory.SelectionType.*;

/**
 * Handler for UI events on game map
 */
public class MapHandler {

    private final GameController gameController;

    private final ResourceBundle resources;

    /**
     * Button to control phase completion
     */
    private volatile Button endPhaseBtn;

    public void setPhaseButton(Button PhaseBtn) {
        endPhaseBtn = PhaseBtn;
        endPhaseBtn.setOnMouseClicked(evt -> {
            endPhaseBtn.setDisable(true);
            endPhase();
        });
        endPhaseBtn.setDisable(true);
    }

    private volatile Label newArmiesLabel;

    public void  setArmiesLabel(Label ArmiesLabel) {
        newArmiesLabel = ArmiesLabel;
        newArmiesLabel.textProperty().bind((new SimpleStringProperty(resources.getString("armies") + ":  ")).concat(newArmies));
        newArmiesLabel.setVisible(false);
    }

    private volatile Mission mission;

    public void setMission(Mission Mission) {
        mission = Mission;

        Main.showDialog(resources.getString("mission"), resources.getString("missionReceived"), resources.getString("continue"));
    }

    public void setMissionButton(Button MissionBtn) {
        MissionBtn.setOnMouseClicked(evt -> Main.showDialog(resources.getString("mission"),
                mission != null ? mission.getDescription(resources.getLocale()) : resources.getString("missionNotReceived"),
                resources.getString("continue")));
    }

    private volatile BiConsumer<Collection<Integer>, Collection<Integer>> showDice;

    public void setShowDice(BiConsumer<Collection<Integer>, Collection<Integer>> ShowDice) { showDice = ShowDice; }

    private final HashMap<Integer, Player> usersList = new HashMap<>();

    public final Map<Territory> map;

    private final Player nullUser = new Player(-100, "NullUser", null);

    private final AtomicReference<SelectedTerritory> selected = new AtomicReference<>();

    private volatile boolean canSelect = false;

    void selected(Territory Selected, boolean IsRightClick) {
        if(!canSelect)
            return;

        synchronized (selected) {
            selected.set(new SelectedTerritory(Selected, IsRightClick));
            selected.notify();
        }
    }

    private SelectedTerritory waitSelected(){
        try {
            selected.wait();
        } catch (InterruptedException e) {
            Logger.err("MapHandler: Exception waiting for territory selection", e);
            return null;
        }

        return selected.get();
    }

    private SelectedTerritory waitSelected(Player Owner, boolean Exclude) {
        SelectedTerritory st;
        if(Owner == null)
            Owner = nullUser;

        while ((st = waitSelected()) != null){
            if(Owner.equals(st.Selected.getOwner()) ^ Exclude)
                return st;
        }

        return null;
    }

    private void endPhase() {
        canSelect = false;
        selected.set(null);
        synchronized (selected){
            selected.notify();
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

    public MapHandler(GameController GC, Match<Player> Match, Pane MapContainer) throws ClassNotFoundException {
        this.gameController = GC;
        this.resources = GC.getResources();

        try {
            map = new Map<>(Match.GameMap, Territory.class);
            map.loadGraphic(resources.getLocale());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ClassNotFoundException(resources.getString("mapLoadError"));
        }

        final SVGPath connections = new SVGPath();
        connections.setContent(map.ConnectionsPath);
        connections.setStroke(Color.BLACK);
        MapContainer.getChildren().add(connections);

        Match.Players.forEach(u -> usersList.put(u.Id.get(), u));

        map.getTerritories().forEach(t -> t.loadGraphic(this, MapContainer));
        Territory.setList(MapContainer, GC.getUser().Color);
    }

    /**
     * Update current map with received information from server. Trigger special move or end turn move if present
     *
     * @param MapUpdate MapUpdate message from server
     */
    public void updateMap(MapUpdate<Territory> MapUpdate) {
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(() -> updateMap(MapUpdate));
            return;
        }

        // Play relative update sound
        MapUpdate.Sound.play();

        MapUpdate.Updated.forEach((u) -> {
            synchronized (map) {
                Territory t = map.getTerritory(u);
                t.Armies.set(u.Armies.get());
                t.NewArmies.set(0);
                if (!t.getOwner().equals(u.getOwner()))
                    t.setOwner(usersList.get(u.getOwner().Id.get()));
            }
        });

        // If update has dice display dice result in UI
        if(MapUpdate.AttackDice != null && showDice != null)
            showDice.accept(MapUpdate.AttackDice, MapUpdate.DefenceDice);

        // If update has move
        if (MapUpdate.HasMove) {
            final Thread move;

            // If player is in attack phase has to perform special move
            if(attackPhase.get() && !MapUpdate.Updated.isEmpty()) {
                move = new Thread(() -> specialMove(map.getTerritory(MapUpdate.Updated.get(0)), map.getTerritory(MapUpdate.Updated.get(1))));
            }
            else if(MapUpdate.Updated.isEmpty() && !attackPhase.get()) { // Else if update is empty has to perform end turn movement
                move = new Thread(this::endTurnMove);
            }
            else // Else HasMove is for another player
                return;

            move.setDaemon(true);
            move.start();
            return;
        }

        if(attackPhase.get()) { // Else if update has no move, notify attackPhase to go ahead if in attack phase
            synchronized (attackPhase) {
                attackPhase.notify();
            }
        }
    }

    /**
     * Enable UI controls to position new armies over user territories.
     *
     * @param NewArmies New armies quantity
     * @return At the end of positioning returns updated territory message to send back to the server
     */
    public MapUpdate<Territory> positionArmies(int NewArmies) {
        System.out.println("Map handler: Positioning Phase");
        final boolean isSetup = NewArmies == 1;

        Platform.runLater(() -> {
            if(!isSetup) {
                endPhaseBtn.setDisable(false);
                endPhaseBtn.setText(resources.getString("endDisplacement"));
            }
            newArmiesLabel.setVisible(true);
            newArmies.set(NewArmies);
        });

        Main.showDialog(resources.getString("positioningMessageTitle"),
                String.format(resources.getString("positioningMessage"), NewArmies),
                resources.getString("placeArmies"));

        // Display positioning controls only for territories owned from current user
        final Player current = gameController.getUser();

        // Handle user interaction
        canSelect = true;
        while (canSelect){
            SelectedTerritory st;

            // If setup wait for an empty territory to be selected
            if(isSetup)
                st = waitSelected(null, false);
            else // Else only current user territories can be selected
                st = waitSelected(current, false);

            // End phase button has been pressed
            if(st == null) break;

            if(st.IsRightClick)
                removeArmyFrom(st.Selected, false);
            else
                addArmyTo(st.Selected, false);
        }

        // Disable button and remove armies label
        Platform.runLater(() -> {
            newArmiesLabel.setVisible(false);
            newArmies.set(0);
        });

        // Check for updated territories and return response message
        return new MapUpdate<>(map.getTerritories().stream().filter(t -> t.NewArmies.isNotEqualTo(0).get()).collect(Collectors.toList()));
    }

    /**
     * Handle all events relative to attack phase and send end phase message back to server at the end
     */
    public void attackPhase(){
        System.out.println("Map handler: Attack phase");
        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText(resources.getString("endAttackPhase"));
        });

        attackPhase.set(true);

        final Player current = gameController.getUser();

        Main.showDialog(resources.getString("attackMessageTitle"),
                           resources.getString("attackMessage"),
                         resources.getString("continue"));

        canSelect = true;
        while (canSelect) {
            // Get territory to attack
            SelectedTerritory st = waitSelected(current, true);
            if(st == null) break;
            final Territory defender = st.Selected;
            defender.select(Defense);

            // Get attacker territory
            st = waitSelected();
            if(st == null) break;
            if(!st.Selected.getOwner().equals(current)){
                defender.select(None);
                if(!st.Selected.equals(defender)){
                    synchronized (selected) {
                        selected.notify();
                    }
                }
                continue;
            }
            // Disable selection to avoid errors
            canSelect = false;
            final Territory attacker = st.Selected;
            attacker.select(Attack);

            // If territories are not adjacent show error
            if(!defender.isAdjacent(attacker)){
                Main.showDialog(resources.getString("attackErrorTitle"),
                        resources.getString("attackErrorAdjacent"),
                        resources.getString("continue"));
                synchronized (selected) {
                    selected.set(new SelectedTerritory(defender, false));
                    selected.notify();
                }
            }
            else if(attacker.Armies.get() == 1){ // If not enough armies are present show error
                Main.showDialog(resources.getString("attackErrorTitle"),
                        resources.getString("attackErrorArmies"),
                        resources.getString("continue"));
                synchronized (selected) {
                    selected.set(new SelectedTerritory(defender, false));
                    selected.notify();
                }
            }
            else { // Else perform battle
                Platform.runLater(() -> endPhaseBtn.setDisable(true));

                // Request attacking armies
                int atkArmies = 1;

                // If only two armies on territory only one can attack
                // else ask player how many to use
                if (attacker.Armies.get() > 2)
                    atkArmies = attacker.requestNumber(true);

                // Send battle to server
                gameController.SendMessage(MessageType.Battle, new Battle<>(attacker, defender, atkArmies));

                // Wait for map update and special move message, if present
                synchronized (attackPhase) {
                    try {
                        attackPhase.wait();
                    } catch (InterruptedException e) {
                        Logger.err("Map handler: Interrupted exception in attack phase.", e);
                        return;
                    }
                }

                Platform.runLater(() -> endPhaseBtn.setDisable(false));
            }

            // Deselect territories
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
     * @param From Attacker territory
     * @param To Newly conquered territory
     */
    private void specialMove(Territory From, Territory To) {
        System.out.println("Map handler: Special movement");
        // Enable end phase button
        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText(resources.getString("continue"));
        });

        // Set armies as new armies to enable movement
        Platform.runLater(() -> {
            synchronized (map) {
                From.NewArmies.set(From.getArmies() - 1);
                From.Armies.set(1);
            }
        });
        final Player current = gameController.getUser();

        canSelect = true;
        while (canSelect){
            SelectedTerritory st = waitSelected(current, false);

            // User has pressed continue button
            if(st == null) break;

            // Can select only 'to' or 'from'
            if(!st.Selected.equals(From) && !st.Selected.equals(To)) {
                Main.showDialog(resources.getString("specialMoveErrorTitle"),
                        resources.getString("specialMoveError"),
                        resources.getString("continue"));
                continue;
            }

            if(st.IsRightClick)
                removeArmyFrom(st.Selected, false);
            else
                addArmyTo(st.Selected, false);
        }

        Platform.runLater(() -> endPhaseBtn.setText(resources.getString("endAttackPhase")));

        // Notify attackPhase to go ahead with execution
        synchronized (attackPhase){
            attackPhase.notify();

            // If user has moved armies return new displacement
            if(To.NewArmies.get() != 0)
                gameController.SendMessage(MessageType.MapUpdate, new MapUpdate<>(From, To));
            else {
                Platform.runLater(() -> From.Armies.set(From.getArmies()));
                gameController.SendMessage(MessageType.MapUpdate, new MapUpdate<>());
            }
        }
    }

    /**
     * Enable positioning controls between two selected territories to perform final movement at the end of turn
     */
    private void endTurnMove() {
        System.out.println("Map handler: End turn movement");
        // Enable end phase button
        Platform.runLater(() -> {
            endPhaseBtn.setDisable(false);
            endPhaseBtn.setText(resources.getString("endTurn"));
            Main.showDialog(resources.getString("positioningMessageTitle"),
                    resources.getString("endTurnMessage"),
                    resources.getString("continue"));
        });

        final Player current = gameController.getUser();

        Territory to = null, from = null;

        canSelect = true;
        while (canSelect){
            SelectedTerritory st = waitSelected(current, false);
            if(st == null) break;

            to = st.Selected;
            to.select(Normal);

            st = waitSelected(current, false);
            if(st == null) break;
            if(st.Selected.equals(to)){
                to.select(None);
                continue;
            }

            from = st.Selected;
            from.select(Normal);

            // Show errors if territories are not adjacent or from territory has only one army
            if(!from.isAdjacent(to)) {
                Main.showDialog(resources.getString("movingErrorTitle"),
                        resources.getString("movingErrorAdjacent"),
                        resources.getString("continue"));
            }
            else if (from.Armies.get() == 1) {
                Main.showDialog(resources.getString("movingErrorTitle"),
                        resources.getString("movingErrorArmies"),
                        resources.getString("continue"));
            }
            else {
                boolean isFrom;
                int fromArmies = from.getArmies();

                // Else handle movement until endPhaseBtn is pressed
                while (canSelect) {
                    st = waitSelected(current, false);

                    // If end trn button is pressed end cycle
                    if(st == null){
                        canSelect = false;
                        break;
                    }

                    if(!(isFrom = st.Selected.equals(from)) && !st.Selected.equals(to)){
                        Main.showDialog(resources.getString("movingErrorTitle"),
                                resources.getString("movingErrorTerritory"),
                                resources.getString("continue"));
                        continue;
                    }

                    if(st.IsRightClick)
                        removeArmyFrom(st.Selected, isFrom);
                    else
                        addArmyTo(st.Selected, isFrom);

                    // If armies are set back to starting territories perform another selection
                    if(from.getArmies() == fromArmies)
                        break;
                }
            }

            to.select(None);
            from.select(None);
        }

        if(to != null && to.NewArmies.get() != 0)
            gameController.SendMessage(MessageType.MapUpdate, new MapUpdate<>(from, to));
        else
            gameController.SendMessage(MessageType.MapUpdate, new MapUpdate<>());
    }

    /**
     * Display dialog and request player to chose how many armies to use for defense
     *
     * @param Battle Battle message received from server
     * @return Updated battle message to send back to server
     */
    public Battle<Territory> requestDefense(Battle<Territory> Battle) {

        final Territory from = map.getTerritory(Battle.from), to = map.getTerritory(Battle.to);

        // Message shown to the user
        final String popupInfo = String.format(resources.getString("defensePopup"),
                from.getOwner().Username.get(),
                Battle.atkArmies,
                from.toString(),
                to.toString());

        Main.showDialog(resources.getString("defensePopupTitle"), popupInfo, resources.getString("continue"));

        Battle.defArmies = to.requestNumber(false);

        return Battle;
    }

    /**
     * Add an army to specified territory from global armies counter
     *
     * @param Territory Territory to update
     * @param IsMoving True if adding directly to Armies field, false to add to NewArmies field
     */
    private void addArmyTo(Territory Territory, boolean IsMoving) {
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
            try {
                wait.wait();
            } catch (InterruptedException e) {
                Logger.err("Map handler: Interrupted exception while adding army", e);
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
        try {
            wait.wait();
        } catch (InterruptedException e) {
            Logger.err("Map handler: Interrupted exception while adding army", e);
            return;
        }

        // If owner is null then we are in setup phase, so update owner and end phase after choice
        if(Territory.NewArmies.get() != 0 && Territory.getOwner().equals(nullUser)) {
            Territory.setOwner(usersList.get(gameController.getUser().Id.get()));
            canSelect = false;
        }
    }

    /**
     * Remove an army to specified territory and increment global armies counter
     *
     * @param Territory Territory to update
     * @param IsMoving True if removing directly from Armies field, false to remove from NewArmies field
     */
    private void removeArmyFrom(Territory Territory, boolean IsMoving) {
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
            try {
                wait.wait();
            } catch (InterruptedException e) {
                Logger.err("Map handler: Interrupted exception while removing army", e);
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
        try {
            wait.wait();
        } catch (InterruptedException e) {
            Logger.err("Map handler: Interrupted exception while removing army", e);
        }
    }

    /**
     * Instance a selected territory and the selection mode (right/left click)
     */
    private class SelectedTerritory {
        final boolean IsRightClick;

        final Territory Selected;

        SelectedTerritory(Territory Selected, boolean IsRightClick){
            this.IsRightClick = IsRightClick;
            this.Selected = Selected;
        }
    }
}
