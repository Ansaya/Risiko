package Client.Game.Observables;

import Client.Main;
import Client.Game.ServerTalk;
import Game.Connection.Battle;
import Game.Map.Territories;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Observable territory class
 */
public class ObservableTerritory {

    public final Territories territory;

    /**
     * SVGPath node corresponding to this territory
     */
    private final transient SVGPath svgTerritory = new SVGPath();

    /**
     * Currently displayed node list
     */
    private final transient JFXNodesList currentNode = new JFXNodesList();

    /**
     * Label containing territory name
     */
    private final transient Label label;

    /**
     * armies currently placed on the territory
     */
    public final IntegerProperty armies = new SimpleIntegerProperty(0);

    /**
     * armies placed during positioning phase not yet submitted to server
     */
    public final IntegerProperty newArmies = new SimpleIntegerProperty(0);

    /**
     * Defending armies choose form user
     */
    private transient final AtomicInteger defend = new AtomicInteger(1);

    private transient volatile boolean posEnabled = false;

    private transient volatile boolean isMoving = false;

    /**
     * Display positioning command in the UI, so that the user can add or remove armies from this territory
     *
     * @param State State of positioning controls
     */
    public void positioningControls(PosControls State) {
        switch (State){
            case Disabled:
                posEnabled = false;
                break;
            case Enabled:
                posEnabled = true;
                isMoving = false;
                break;
            case Moving:
                posEnabled = true;
                isMoving = true;
                break;
        }
    }

    private volatile ObservableUser owner = null;

    /**
     * Updates current owner and territory color
     *
     * @param Owner New owner of this territory
     */
    public void setOwner(ObservableUser Owner) {
        this.owner = Owner;

        // Change foreground territory color accordingly to new owner's color
        Platform.runLater(() -> svgTerritory.setFill(owner.color.hexColor));
    }

    public ObservableUser getOwner() { return this.owner; }

    /**
     * Instance of map territory with reference to UI territory
     *
     * @param Territory Territory associated to this object
     * @param Label Label for this territory in UI
     */
    public ObservableTerritory(Territories Territory, Label Label) {
        this.territory = Territory;
        this.label = Label;
        label.setMouseTransparent(true);    // Set label mouse transparent to avoid selection issues
        svgTerritory.setContent(territory.svgPath);
        svgTerritory.getStyleClass().add("territory");
        svgTerritory.setFill(territory.continent.hexColor);

        // Setup positioning/moving events which will be triggered from posEnabled boolean
        svgTerritory.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> {
            if(!posEnabled)
                return;

            // Get clicked button of mouse (left as add button, right as remove button)
            final boolean isRightClick = evt.getButton().equals(MouseButton.SECONDARY);

            // If moving armies between territories perform special actions
            if(isMoving){
                if(isRightClick){ // Remove army
                    // If no army can be moved return
                    if(armies.get() == 1)
                        return;

                    // If armies owner is null set this territory as owner
                    if(UIHandler.newArmiesOwner == null)
                        UIHandler.newArmiesOwner = territory;

                    // If user moves armies from different territories notify error
                    if(UIHandler.newArmiesOwner != territory){
                        Main.showDialog("Moving error", "You can move armies from one territory only.", "Continue");
                        return;
                    }

                    // Else perform movement
                    synchronized (armies){
                        armies.set(armies.subtract(1).get());
                    }
                    UIHandler.addNewArmy();
                }
                else {  // Add army
                    // If no armies can be moved return
                    if(UIHandler.getNewArmies() == 0)
                        return;

                    // If user is moving armies to non adjacent territory notify error
                    if(!territory.isAdjacent(UIHandler.newArmiesOwner)){
                        Main.showDialog("Moving error", "You can move armies between adjacent territories only.", "Continue");
                        return;
                    }

                    // Else perform movement
                    synchronized (newArmies){
                        newArmies.set(newArmies.add(UIHandler.removeNewArmy()).get());
                    }

                    // If all armies are moved back to owner territory, reset newArmiesOwner
                    if(UIHandler.getNewArmies() == 0 && UIHandler.newArmiesOwner == territory)
                        UIHandler.newArmiesOwner = null;
                }
                return; // Return after completion
            }

            // Else if positioning armies at the beginning of turn perform normal positioning
            if(isRightClick){
                System.out.println("User want to remove an army from " + territory.toString());

                // Check if new armies have been placed on this territory, then remove one
                if(newArmies.get() > 0) {
                    synchronized (newArmies) {
                        newArmies.set(newArmies.subtract(1).get());
                    }

                    UIHandler.addNewArmy();
                }
            }
            else {
                System.out.println("User want to add an army to " + territory.toString());

                // If new army is available UIHandler.removeNewArmy() returns 1 else 0
                synchronized (newArmies) {
                    newArmies.set(newArmies.add(UIHandler.removeNewArmy()).get());
                }

                // If owner is null then we are in setup phase, so update owner and end phase after choice
                if(newArmies.get() != 0 && this.owner == null) {
                    setOwner(ServerTalk.getInstance().getUser());
                    synchronized (UIHandler.goAhead){
                        UIHandler.goAhead.notify();
                    }
                }
            }
        });


        /* Main badge construction */
        final Label l = new Label();
        l.textProperty().bind(armies.add(newArmies).asString());
        l.setTextFill(Color.WHITE);

        final JFXButton btn = new JFXButton();
        btn.setButtonType(JFXButton.ButtonType.RAISED);
        btn.getStyleClass().add("animated-option-button-");
        btn.setGraphic(l);

        currentNode.addAnimatedNode(btn);
        currentNode.setLayoutX(getCenterX(label));
        currentNode.setLayoutY(getCenterY(label));
        currentNode.setMouseTransparent(true);

        // Add event handler for selection
        svgTerritory.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> UIHandler.selected(this, evt.getButton().equals(MouseButton.SECONDARY)));

        Platform.runLater(() -> {
            UIHandler.mapPane.getChildren().addAll(svgTerritory, currentNode);
            svgTerritory.toBack();
        });
    }

    /**
     * Show popup in UI and return the number of armies used to defend from an battle
     *
     * @param battle Battle message received from server
     * @return Number of armies defending the territory
     */
    public int requestDefense(Battle<ObservableTerritory> battle) {
        // Request is not sent from server if armies are less then two

        // Message shown to the user
        final String popupInfo = "Player " + battle.from.owner.username.get() + " is attacking from " + battle.from.toString() + " with " + battle.atkArmies +
                " armies to " + battle.to.toString() + "\r\nChoose how many armies do you want to defend with.";

        Platform.runLater(() -> {
            Main.showDialog("You are under battle!", popupInfo, "Go ahead");
            showDefenseList();
        });

        // Wait for defend to be updated
        synchronized (defend){
            try {
                defend.wait();
            } catch (Exception e){}
        }

        return defend.get();
    }

    /**
     * Setup and show defense node list with buttons and event handlers
     */
    private void showDefenseList() {
        // Buttons setup
        final JFXNodesList defenseList = new JFXNodesList();
        JFXButton mainBtn = nodeButton("Defense", "def", true);
        JFXButton btn1 = nodeButton("1", "def", false);
        JFXButton btn2 = nodeButton("2", "def", false);

        // One army button
        btn1.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {
            System.out.println("User want to defend " + this.svgTerritory.getId() + " with one army.");
            synchronized (defend) {
                defend.set(1);
                defend.notify();
            }

            // After the choice remove unnecessary UI controls
            UIHandler.mapPane.getChildren().remove(defenseList);
        });

        // Two armies button
        btn2.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {
            System.out.println("User want to defend " + this.svgTerritory.getId() + " with two armies.");
            synchronized (defend) {
                defend.set(2);
                defend.notify();
            }

            // After choice remove unnecessary UI controls
            UIHandler.mapPane.getChildren().remove(defenseList);
        });

        defenseList.setSpacing(10);
        defenseList.addAnimatedNode(mainBtn, (expanded)->
                new ArrayList<KeyValue>(){{ add(new KeyValue(mainBtn.getGraphic().rotateProperty(), expanded? 360:0 , Interpolator.EASE_BOTH));}});
        defenseList.addAnimatedNode(btn1);
        defenseList.addAnimatedNode(btn2);
        defenseList.setLayoutX(getCenterX(label));
        defenseList.setLayoutY(getCenterY(label));

        // Check where to display additional buttons if near window border
        if(defenseList.getLayoutX() < 200.0)
            defenseList.setRotate(270);
        else
            defenseList.setRotate(90);

        // Display defend list in UI
        if(Platform.isFxApplicationThread()) {
            UIHandler.mapPane.getChildren().add(defenseList);
            defenseList.animateList();
        }
        else
            Platform.runLater(() -> {
                UIHandler.mapPane.getChildren().add(defenseList);
                defenseList.animateList();
            });
    }

    /**
     * Button formatter to build node lists
     *
     * @param Text Text to put inside node
     * @param cssClass Class identifier to add to animated-option-button-*
     * @param label Specify to add text as graphic inside button
     * @return Formatted button as specified
     */
    private JFXButton nodeButton(String Text, String cssClass, boolean label) {
        JFXButton btn = new JFXButton();
        btn.setButtonType(JFXButton.ButtonType.RAISED);
        btn.getStyleClass().add("animated-option-button-" + cssClass);

        if(label) {
            Label l = new Label(Text);
            l.setTextFill(Color.WHITE);
            btn.setGraphic(l);
        }
        else {
            btn.setText(Text);
            btn.getStyleClass().add("animated-option-button-selector");
        }

        return btn;
    }

    /**
     * Get X center coordinates of given label in respect to his parent
     *
     * @param Label Label to get position of
     * @return X center position in respect of parent of given node
     */
    private double getCenterX(Label Label) {
        return Label.getBoundsInParent().getMinX() + (Label.getPrefWidth() / 2) - 17.5;
    }

    /**
     * Get Y center coordinates of given label in respect to his parent
     *
     * @param Label Label to get position of
     * @return Y center position in respect of parent of given node
     */
    private double getCenterY(Label Label) {
        return Label.getBoundsInParent().getMinY() + (Label.getPrefHeight() / 2) - 17.5;
    }

    @Override
    public String toString() {
        return territory.toString();
    }

    enum PosControls {
        Disabled,
        Enabled,
        Moving
    }
}