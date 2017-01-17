package Client.Game.Observables;

import Client.Main;
import Game.Connection.Battle;
import Game.Map.Territory;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Observable Territory class
 */
public class ObservableTerritory extends Territory<ObservableUser> {

    private volatile transient Pane mapContainer;

    /**
     * SVGPath node corresponding To this Territory
     */
    private final transient SVGPath svgTerritory = new SVGPath();

    /**
     * Currently displayed node list
     */
    private final transient ImageView armyImg = new ImageView();

    /**
     * Armies currently placed on the Territory
     */
    public final IntegerProperty Armies = new SimpleIntegerProperty(0);

    /**
     * Armies placed during positioning phase not yet submitted To server
     */
    public final IntegerProperty NewArmies = new SimpleIntegerProperty(0);

    /**
     * Defending Armies choose form user
     */
    private transient final AtomicInteger useArmies = new AtomicInteger(1);

    private volatile ObservableUser owner = new ObservableUser(-100, "NullUser", null);

    /**
     * Updates current owner and Territory color
     *
     * @param Owner New owner of this Territory
     */
    public void setOwner(ObservableUser Owner) {
        synchronized (owner.Territories){
            owner.Territories.set(owner.Territories.subtract(1).get());
        }

        owner = Owner;

        synchronized (owner.Territories){
            owner.Territories.set(owner.Territories.add(1).get());
        }

        if(Platform.isFxApplicationThread()) {
            svgTerritory.setEffect(new InnerShadow(BlurType.GAUSSIAN, owner.Color.hexColor, 5.0, 5.0, 0, 0));
            armyImg.setImage(owner.Color.armyImg);
        }
        else
            Platform.runLater(() -> {
                svgTerritory.setEffect(new InnerShadow(BlurType.GAUSSIAN, owner.Color.hexColor, 5.0, 5.0, 0, 0));
                armyImg.setImage(owner.Color.armyImg);
            });
    }

    public ObservableUser getOwner() { return owner; }

    public int getArmies() { return Armies.add(NewArmies.get()).get(); }

    /**
     * Instance of map Territory with reference to UI Territory
     *
     * @param MapHandler Map handler for this territory
     */
    public void loadGraphic(MapHandler MapHandler, Pane MapContainer) {
        this.mapContainer = MapContainer;

        // Name label
        final Label name = new Label(Name.toUpperCase().replaceAll(" ", "\n"));
        name.setMouseTransparent(true);
        name.setRotate(LabelR);
        name.getStyleClass().add("name");

        // Territory image
        svgTerritory.setContent(this.SvgPath);
        svgTerritory.setStroke(Color.BLACK);
        svgTerritory.setStrokeWidth(1.5f);
        svgTerritory.setFill(this.Area.Color);
        // Add event handler for selection
        svgTerritory.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> MapHandler.selected(this, evt.getButton().equals(MouseButton.SECONDARY)));
        svgTerritory.addEventFilter(MouseEvent.MOUSE_ENTERED, evt -> svgTerritory.setFill(this.Area.Color.darker()));
        svgTerritory.addEventFilter(MouseEvent.MOUSE_EXITED, evt -> svgTerritory.setFill(this.Area.Color));

        /* Main badge construction */
        final Label l = new Label();
        l.textProperty().bind(Armies.add(NewArmies).asString());
        l.setTextFill(Color.WHITE);
        l.setFont(new Font("Arial Black", 20.0));
        l.setAlignment(Pos.BOTTOM_RIGHT);
        l.setPrefHeight(40.0f);
        l.setPrefWidth(40.0f);
        l.visibleProperty().bind(Armies.isNotEqualTo(0));

        armyImg.setSmooth(true);
        armyImg.setCache(true);
        armyImg.setPreserveRatio(true);

        final StackPane sp = new StackPane(armyImg, l);
        sp.prefWidth(40.0f);
        sp.prefHeight(40.0f);
        sp.setMouseTransparent(true);

        Platform.runLater(() -> {
            name.setLayoutX(getCenterX(svgTerritory) + LabelX);
            name.setLayoutY(getCenterY(svgTerritory) + LabelY);
            sp.setLayoutX(getCenterX(svgTerritory) + ArmyX);
            sp.setLayoutY(getCenterY(svgTerritory) + ArmyY);

            mapContainer.getChildren().addAll(name, sp, svgTerritory);
            svgTerritory.toBack();
        });
    }

    public void select(SelectionType Mode) {
        switch (Mode) {
            case Attack:
                svgTerritory.getStyleClass().add("selected-atk");
                break;
            case Defense:
                svgTerritory.getStyleClass().add("selected-def");
                break;
            case Normal:
                svgTerritory.getStyleClass().add("selected");
                break;
            case None:
                svgTerritory.getStyleClass().removeIf(c -> c.contains("selected"));
                break;
        }
    }

    /**
     * Show popup in UI and return the number of Armies used To useArmies From an battle
     *
     * @param battle Battle message received From server
     * @return Number of Armies defending the Territory
     */
    public int requestDefense(Battle<ObservableTerritory> battle) {
        // Request is not sent From server if Armies are less then two

        // Message shown To the user
        final String popupInfo = "Player " + battle.from.owner.Username.get() + " is attacking from " + battle.from.toString() + " with " + battle.atkArmies +
                " armies to " + battle.to.toString() + ".\r\nChoose how many defending armies to use.";

        Platform.runLater(() -> {
            Main.showDialog("You are under attack!", popupInfo, "Go ahead");
            showList(false);
        });

        // Wait for useArmies To be updated
        synchronized (useArmies){
            try {
                useArmies.wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        return useArmies.get();
    }

    /**
     * Show attack list To user and return chosen number of attacking armies
     *
     * @return Number of armies To use for battle
     */
    public int requestAttack() {
        // Request is not sent From MapHandler if Armies are less then three

        Platform.runLater(() -> showList(true));

        // Wait for useArmies To be updated
        synchronized (useArmies){
            try {
                useArmies.wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        return useArmies.get();
    }

    /**
     * Setup and show defense/attack node list with buttons and event handlers
     */
    private void showList(boolean isAttack) {
        String cssClass = "def";
        String text = "Defense";
        if(isAttack) {
            cssClass = "atk";
            text = "Attack";
        }

        // Buttons setup
        final JFXNodesList list = new JFXNodesList();
        final JFXButton mainBtn = nodeButton(text, cssClass, true);
        list.setSpacing(15);
        list.addAnimatedNode(mainBtn, (expanded)->
                new ArrayList<KeyValue>(){{ add(new KeyValue(mainBtn.getGraphic().rotateProperty(), expanded? 360:0 , Interpolator.EASE_BOTH));}});
        list.setLayoutX(getCenterX(svgTerritory) - 20.0f);
        list.setLayoutY(getCenterY(svgTerritory) - 20.0f);

        final EventHandler<MouseEvent> handler = evt -> {
            final int def = Integer.parseInt(((Button) evt.getSource()).getText());
            synchronized (useArmies) {
                useArmies.set(def);
                useArmies.notify();
            }

            // After choice remove unnecessary UI controls
            mapContainer.getChildren().remove(list);
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
        if(list.getLayoutY() < 250.0)
            list.setRotate(180.0);
        else
            list.setRotate(0.0);

        // Display useArmies list in UI
        if(Platform.isFxApplicationThread()) {
            mapContainer.getChildren().add(list);
            list.animateList();
        }
        else
            Platform.runLater(() -> {
                mapContainer.getChildren().add(list);
                list.animateList();
            });
    }

    /**
     * Button formatter To build node lists
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
     * Get X center coordinates of given node in respect to his parent
     *
     * @param node Node to get center of
     * @return X center position in respect of parent of given node
     */
    private double getCenterX(Node node) {
        return node.getBoundsInParent().getMinX() + (node.getBoundsInLocal().getWidth() / 2);
    }

    /**
     * Get Y center coordinates of given node in respect to his parent
     *
     * @param node Node to get center of
     * @return Y center position in respect of parent of given node
     */
    private double getCenterY(Node node) {
        return node.getBoundsInParent().getMinY() + (node.getBoundsInLocal().getHeight() / 2);
    }

    public enum SelectionType {
        Attack,
        Defense,
        Normal,
        None
    }
}