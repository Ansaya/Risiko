package Client.Game.Observables;

import Client.Main;
import Game.Connection.Battle;
import Game.Map.Territories;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Observable Territory class
 */
public class ObservableTerritory {

    public final Territories Territory;

    /**
     * SVGPath node corresponding to this Territory
     */
    private final transient SVGPath svgTerritory = new SVGPath();

    /**
     * Currently displayed node list
     */
    private final transient JFXNodesList currentNode = new JFXNodesList();

    /**
     * Label containing Territory name
     */
    private final transient Label label;

    /**
     * Armies currently placed on the Territory
     */
    public final IntegerProperty Armies = new SimpleIntegerProperty(0);

    /**
     * Armies placed during positioning phase not yet submitted to server
     */
    public final IntegerProperty NewArmies = new SimpleIntegerProperty(0);

    /**
     * Defending Armies choose form user
     */
    private transient final AtomicInteger useArmies = new AtomicInteger(1);

    private volatile ObservableUser owner = null;

    /**
     * Updates current owner and Territory color
     *
     * @param Owner New owner of this Territory
     */
    public void setOwner(ObservableUser Owner) {
        this.owner = Owner;

        // Change foreground Territory color accordingly to new owner's color
        Platform.runLater(() -> svgTerritory.setFill(owner.color.hexColor));
    }

    public ObservableUser getOwner() { return this.owner; }

    /**
     * Instance of map Territory with reference to UI Territory
     *
     * @param Territory Territory associated to this object
     * @param Label Label for this Territory in UI
     */
    public ObservableTerritory(Territories Territory, Label Label) {
        this.Territory = Territory;
        this.label = Label;
        label.setMouseTransparent(true);    // Set label mouse transparent to avoid selection issues
        svgTerritory.setContent(this.Territory.svgPath);
        svgTerritory.getStyleClass().add("Territory");
        svgTerritory.setFill(this.Territory.continent.hexColor);
        // Add event handler for selection
        svgTerritory.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> UIHandler.selected(this, evt.getButton().equals(MouseButton.SECONDARY)));

        /* Main badge construction */
        final Label l = new Label();
        l.textProperty().bind(Armies.add(NewArmies).asString());
        l.setTextFill(Color.WHITE);

        final JFXButton btn = new JFXButton();
        btn.setButtonType(JFXButton.ButtonType.RAISED);
        btn.getStyleClass().add("animated-option-button-");
        btn.setGraphic(l);

        currentNode.addAnimatedNode(btn);
        currentNode.setLayoutX(getCenterX(label));
        currentNode.setLayoutY(getCenterY(label));
        currentNode.setMouseTransparent(true);

        Platform.runLater(() -> {
            UIHandler.mapPane.getChildren().addAll(svgTerritory, currentNode);
            svgTerritory.toBack();
        });
    }

    /**
     * Show popup in UI and return the number of Armies used to useArmies from an battle
     *
     * @param battle Battle message received from server
     * @return Number of Armies defending the Territory
     */
    public int requestDefense(Battle<ObservableTerritory> battle) {
        // Request is not sent from server if Armies are less then two

        // Message shown to the user
        final String popupInfo = "Player " + battle.from.owner.username.get() + " is attacking from " + battle.from.toString() + " with " + battle.atkArmies +
                " Armies to " + battle.to.toString() + "\r\nChoose how many Armies do you want to useArmies with.";

        Platform.runLater(() -> {
            Main.showDialog("You are under battle!", popupInfo, "Go ahead");
            showList(false);
        });

        // Wait for useArmies to be updated
        synchronized (useArmies){
            try {
                useArmies.wait();
            } catch (Exception e){}
        }

        return useArmies.get();
    }

    /**
     * Show attack list to user and return chosen number of attacking armies
     *
     * @return Number of armies to use for battle
     */
    public int requestAttack() {
        // Request is not sent from UIHandler if Armies are less then three

        Platform.runLater(() -> showList(true));

        // Wait for useArmies to be updated
        synchronized (useArmies){
            try {
                useArmies.wait();
            } catch (Exception e){}
        }

        return useArmies.get();
    }

    /**
     * Setup and show defense/attack node list with buttons and event handlers
     */
    private void showList(boolean isAttack) {
        String cssClass = "def";
        if(isAttack)
            cssClass = "atk";

        // Buttons setup
        final JFXNodesList list = new JFXNodesList();
        final JFXButton mainBtn = nodeButton("Attack", cssClass, true);
        list.setSpacing(10);
        list.addAnimatedNode(mainBtn, (expanded)->
                new ArrayList<KeyValue>(){{ add(new KeyValue(mainBtn.getGraphic().rotateProperty(), expanded? 360:0 , Interpolator.EASE_BOTH));}});
        list.setLayoutX(getCenterX(label));
        list.setLayoutY(getCenterY(label));

        final EventHandler<MouseEvent> handler = evt -> {
            final int def = Integer.parseInt(((Button) evt.getSource()).getText());
            synchronized (useArmies) {
                useArmies.set(def);
                useArmies.notify();
            }

            // After choice remove unnecessary UI controls
            UIHandler.mapPane.getChildren().remove(list);
        };

        final JFXButton btn1 = nodeButton("1", cssClass, false);
        final JFXButton btn2 = nodeButton("2", cssClass, false);
        btn1.addEventFilter(MouseEvent.MOUSE_CLICKED, handler);
        btn2.addEventFilter(MouseEvent.MOUSE_CLICKED, handler);
        list.addAnimatedNode(btn1);
        list.addAnimatedNode(btn2);

        if(isAttack && Armies.get() > 3){
            final JFXButton btn3 = nodeButton("3", cssClass, false);
            btn3.addEventFilter(MouseEvent.MOUSE_CLICKED, handler);
            list.addAnimatedNode(btn3);
        }

        // Check where to display additional buttons if near window border
        if(list.getLayoutX() < 200.0)
            list.setRotate(270);
        else
            list.setRotate(90);

        // Display useArmies list in UI
        if(Platform.isFxApplicationThread()) {
            UIHandler.mapPane.getChildren().add(list);
            list.animateList();
        }
        else
            Platform.runLater(() -> {
                UIHandler.mapPane.getChildren().add(list);
                list.animateList();
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
        final JFXButton btn = new JFXButton();
        btn.setButtonType(JFXButton.ButtonType.RAISED);
        btn.getStyleClass().add("animated-option-button-" + cssClass);

        if(label) {
            final Label l = new Label(Text);
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
        return Territory.toString();
    }

    enum PosControls {
        Disabled,
        Enabled,
        Moving
    }
}