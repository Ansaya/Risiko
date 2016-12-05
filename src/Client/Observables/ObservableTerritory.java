package Client.Observables;

import Client.Main;
import Client.ServerTalk;
import Client.Connection.Attack;
import Game.Map.Territories;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.SVGPath;
import java.util.ArrayList;

/**
 * Observable territory class
 */
public class ObservableTerritory {

    public final Territories territory;

    /**
     * SVGPath node corresponding to this territory
     */
    private final transient SVGPath svgTerritory;

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
    private transient volatile Integer defend = 1;

    private volatile ObservableUser owner = null;

    /**
     * Updates current owner and territory color
     *
     * @param Owner New owner of this territory
     */
    public void setOwner(ObservableUser Owner) {
        this.owner = Owner;

        // Change foreground territory color accordingly to new owner's color
        Platform.runLater(() -> svgTerritory.setStyle("-fx-background-color: " + owner.color.get().toLowerCase()));
    }

    public ObservableUser getOwner() { return this.owner; }

    /**
     *
     *
     * @param SVGTerritory SVGPath for this territory in UI
     * @param Label Label for this territory in UI
     */
    public ObservableTerritory(Territories Territory, SVGPath SVGTerritory, Label Label) {
        this.territory = Territory;
        this.svgTerritory = SVGTerritory;
        this.label = Label;

        final Label l = new Label();
        l.textProperty().bind(armies.add(newArmies).asString());
        l.setStyle("-fx-text-fill:WHITE;");

        final JFXButton btn = new JFXButton();
        btn.setButtonType(JFXButton.ButtonType.RAISED);
        btn.getStyleClass().add("animated-option-button-");
        btn.setGraphic(l);

        currentNode.addAnimatedNode(btn);
        currentNode.setLayoutX(getCenterX(label));
        currentNode.setLayoutY(getCenterY(label));

        Platform.runLater(() -> MapHandler.mapPane.getChildren().add(currentNode));
    }

    /**
     * Display positioning command in the UI, so that the user can add or remove armies from this territory
     *
     * @param isMoving False is adding new armies, true if moving armies during last phase of the turn.
     */
    public void positioningControls(boolean isMoving) {
        // Add button to the right of central node
        JFXButton add = nodeButton("+", "atk", false);
        add.setLayoutX(currentNode.getLayoutX() + 50.0f);
        add.setLayoutY(currentNode.getLayoutY());

        // Sub button to the left of central node
        JFXButton sub = nodeButton("-", "def", false);
        sub.setLayoutX(currentNode.getLayoutX() - 50.0f);
        sub.setLayoutY(currentNode.getLayoutY());

        // Add correct event handler for moving/positioning circumstances
        if(isMoving){
            // Event handler to move armies to this territory
            add.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {
                // Implement moving checks to get armies from adjacent territories
                System.out.println("User want to move armies in " + this.svgTerritory.getId());
            });

            // Event handler to move armies from this territory
            sub.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {
                // Implement moving checks to move armies from this territory to adjacent territories
                System.out.println("User want to move armies from" + this.svgTerritory.getId());
            });
        }
        else {
            // Event handler to add new armies
            add.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {
                System.out.println("User want to add an army to " + this.svgTerritory.getId());

                // Check if new armies are there, then add one
                if(MapHandler.newArmies.get() > 0){
                    MapHandler.newArmies.getAndDecrement();

                    synchronized (newArmies) {
                        this.newArmies.set(newArmies.add(1).get());
                    }

                    // If owner is null then we are in setup phase, so end phase after choice
                    if(this.owner == null) {
                        setOwner(ServerTalk.getInstance().getUser());
                        synchronized (MapHandler.goAhead){
                            MapHandler.goAhead.notify();
                        }
                    }
                }
            });

            // Event handler to remove new armies
            sub.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {
                System.out.println("User want to remove an army from " + this.svgTerritory.getId());

                // Check if new armies have been placed on this territory, then remove one
                if(this.newArmies.get() > 0) {
                    synchronized (newArmies) {
                        this.newArmies.set(newArmies.subtract(1).get());
                    }

                    MapHandler.newArmies.getAndIncrement();
                }
            });
        }

        Platform.runLater(() -> {
            currentNode.addAnimatedNode(add);
            currentNode.addAnimatedNode(sub);
        });
    }

    /**
     * Show popup in UI and return the number of armies used to defend from an attack
     *
     * @param attack Attack message received from server
     * @return Number of armies defending the territory
     */
    public int requestDefense(Attack attack) {
        if(armies.get() < 2)
            return 1;

        final JFXNodesList defendList = getDefendList();

        // Message shown to the user
        final String popupInfo = "Player " + attack.from.owner.username + " is attacking from " + attack.from.toString() + " with " + attack.armies +
                " armies to " + attack.to.toString() + "\r\nChoose how many armies do you want to defend with.";

        Platform.runLater(() -> {
            Main.showDialog("You are under attack!", popupInfo, "Go ahead");
            MapHandler.mapPane.getChildren().add(defendList);
        });

        // Wait for defend to be updated
        synchronized (defend){
            try {
                defend.wait();
            } catch (Exception e){}
        }

        return defend;
    }

    /**
     * Setup defense node list with buttons and event handlers
     *
     * @return Initialized defense list to display in UI
     */
    private JFXNodesList getDefendList() {
        // Main button
        JFXButton mainBtn = nodeButton("Defense", "def", true);

        JFXNodesList nodesList = new JFXNodesList();
        nodesList.setSpacing(10);
        nodesList.addAnimatedNode(mainBtn, (expanded)-> new ArrayList<KeyValue>(){{ add(new KeyValue(mainBtn.getGraphic().rotateProperty(), expanded? 360:0 , Interpolator.EASE_BOTH));}});

        // One army button
        JFXButton btn1 = nodeButton("1", "def", false);
        btn1.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {
            System.out.println("User want to defend " + this.svgTerritory.getId() + " with one army.");
            synchronized (defend) {
                defend = 1;
                defend.notify();
            }

            // After the choice remove unnecessary UI controls
            MapHandler.mapPane.getChildren().removeIf(node -> node.getId() == "btnRemove");
        });

        nodesList.addAnimatedNode(btn1);

        // Two armies button
        JFXButton btn2 = nodeButton("2", "def", false);
        btn2.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {
            System.out.println("User want to defend " + this.svgTerritory.getId() + " with two armies.");
            synchronized (defend) {
                defend = 2;
                defend.notify();
            }

            // After choice remove unnecessary UI controls
            MapHandler.mapPane.getChildren().removeIf(node -> node.getId() == "btnRemove");
        });

        nodesList.addAnimatedNode(btn2);

        nodesList.setLayoutX(getCenterX(label));
        nodesList.setLayoutY(getCenterY(label));

        // Check where to display additional buttons if near window border
        if(nodesList.getLayoutX() < 200.0)
            nodesList.setRotate(270);
        else
            nodesList.setRotate(90);

        return nodesList;
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
        btn.setId("btnRemove");

        if(label) {
            Label l = new Label(Text);
            l.setStyle("-fx-text-fill:WHITE;");
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
}