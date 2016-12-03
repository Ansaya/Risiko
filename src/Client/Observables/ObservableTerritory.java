package Client.Observables;

import Client.Main;
import Game.Connection.Attack;
import Game.Connection.User;
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
    /**
     * SVGPath node corresponding to this territory
     */
    private SVGPath svgTerritory;

    /**
     * Currently displayed node list
     */
    private JFXNodesList currentNode;

    /**
     * Label containing territory name
     */
    private Label label;

    /**
     * Armies currently placed on the territory
     */
    public IntegerProperty Armies = new SimpleIntegerProperty(0);

    /**
     * Armies placed during positioning phase not yet submitted to server
     */
    public IntegerProperty NewArmies = new SimpleIntegerProperty(0);

    /**
     * Defending armies choose form user
     */
    private volatile Integer defend = 1;

    private User owner;

    /**
     * Updates current owner and territory color
     *
     * @param Owner New owner of this territory
     */
    public void setOwner(User Owner) {
        this.owner = Owner;

        // Change foreground territory color accordingly to new owner's color
        Platform.runLater(() -> svgTerritory.setStyle("-fx-background-color: " + owner.getColor().toString().toLowerCase()));
    }

    public User getOwner() { return this.owner; }

    public ObservableTerritory(SVGPath SVGTerritory, Label Label) {
        this.svgTerritory = SVGTerritory;
        this.label = Label;

        this.Armies = new SimpleIntegerProperty(0);

        JFXButton btn = new JFXButton();
        btn.setButtonType(JFXButton.ButtonType.RAISED);
        btn.getStyleClass().add("animated-option-button-");

        Label l = new Label();
        l.textProperty().bind(Armies.add(NewArmies).asString());
        l.setStyle("-fx-text-fill:WHITE;");
        btn.setGraphic(l);

        this.currentNode = new JFXNodesList();
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
                if(MapHandler.newArmies > 0){
                    synchronized (MapHandler.newArmies) {
                        MapHandler.newArmies--;
                    }
                    synchronized (NewArmies) {
                        this.NewArmies.add(1);
                    }
                }
            });

            // Event handler to remove new armies
            sub.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {
                System.out.println("User want to remove an army from " + this.svgTerritory.getId());

                // Check if new armies have been placed on this territory, then remove one
                if(this.NewArmies.get() > 0) {
                    synchronized (NewArmies) {
                        this.NewArmies.subtract(1);
                    }
                    synchronized (MapHandler.newArmies) {
                        MapHandler.newArmies++;
                    }
                }
            });
        }
    }

    /**
     * Show popup in UI and return the number of armies used to defend from an attack
     *
     * @return Number of armies defending the territory
     * @throws InterruptedException
     */
    public int requestDefense(Attack attack) throws InterruptedException {
        int armies = Integer.valueOf(label.getText());
        if(armies < 2)
            return 1;

        JFXNodesList defendList = getDefendList();

        // Message shown to the user
        String popupInfo = "Player " + attack.getFrom().getOwner().getName() + " is attacking from " + attack.getFrom().toString() + " with " + attack.getArmies() +
                " armies to " + attack.getTo().toString() + "\r\nChoose how many armies do you want to defend with.";

        Platform.runLater(() -> {
            Main.showDialog("You are under attack!", popupInfo, "Go ahead");
            MapHandler.mapPane.getChildren().add(defendList);
        });

        // Wait for defend to be updated
        synchronized (defend){
            defend.wait();
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
}