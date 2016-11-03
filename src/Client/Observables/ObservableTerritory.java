package Client.Observables;

import Game.Connection.User;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXNodesList;
import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

/**
 * Observable territory class
 */
public class ObservableTerritory {
    /**
     * SVGPath node corresponding to this territory
     */
    private Node svgTerritory;

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

    private User owner;

    /**
     * Updates current owner and territory color
     *
     * @param Owner New owner of this territory
     */
    public void setOwner(User Owner) {
        this.owner = Owner;

        Platform.runLater(() -> {
            svgTerritory.setStyle("-fx-background-color: " + owner.getColor().toString().toLowerCase());
        });
    }

    public User getOwner() { return this.owner; }

    private static Pane mapPane;

    public static void setMapPane(Pane MapPane) { mapPane = MapPane; }

    public ObservableTerritory(Node SVGTerritory, Label Label) {
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

        Platform.runLater(() -> mapPane.getChildren().add(currentNode));
    }

    /**
     * Show popup in UI and return the number of armies used to defend from an attack
     *
     * @return Number of armies defending the territory
     * @throws InterruptedException
     */
    public int requestDefense(String DialogText) throws InterruptedException {
        int armies = Integer.valueOf(label.getText());
        if(armies < 2)
            return 1;

        JFXNodesList defendList = getDefendList();
        defendList.addEventFilter(MouseEvent.MOUSE_CLICKED, (evt) -> {});

        Integer defend = 1;

        JFXDialog popup = new JFXDialog();
        popup.setContent(new Label(DialogText));
        popup.addEventFilter(DialogEvent.DIALOG_CLOSE_REQUEST, (e) -> mapPane.getChildren().remove(popup));

        Platform.runLater(() -> {
            mapPane.getChildren().add(popup);
            popup.show();
            mapPane.getChildren().add(defendList);

            synchronized (defend){
                // Update defend value
                defend.notify();
            }
        });

        // Wait for defend to be updated
        synchronized (defend){
            defend.wait();
        }

        return defend;
    }

    private JFXNodesList getDefendList() {
        JFXButton mainBtn = nodeButton("Defense", "def", true);

        JFXNodesList nodesList = new JFXNodesList();
        nodesList.setSpacing(10);
        nodesList.addAnimatedNode(mainBtn, (expanded)-> new ArrayList<KeyValue>(){{ add(new KeyValue(mainBtn.getGraphic().rotateProperty(), expanded? 360:0 , Interpolator.EASE_BOTH));}});
        nodesList.addAnimatedNode(nodeButton("1", "def", false));
        nodesList.addAnimatedNode(nodeButton("2", "def", false));

        nodesList.setLayoutX(getCenterX(label));
        nodesList.setLayoutY(getCenterY(label));

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