package Client.Game.Map;

import Client.Game.Player;
import Client.Main;
import Client.UI.Match.Army.ArmyBadge;
import Client.UI.Match.Army.ArmyList;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;

/**
 * Observable Territory class
 */
public class Territory extends Game.Map.Territory<Player> {

    private static ArmyList al;

    public static void setList(Pane MapContainer, Game.Map.Army.Color Army) {
        if(Army == null) return;

        al = ArmyList.getArmyList(Army);
        al.getList().setVisible(false);
        Platform.runLater(() -> MapContainer.getChildren().add(al.getList()));
    }

    private volatile transient Pane mapContainer;

    /**
     * SVGPath node corresponding to this territory
     */
    private final transient SVGPath svgTerritory = new SVGPath();

    /**
     * Armies currently placed on the territory
     */
    public final IntegerProperty Armies = new SimpleIntegerProperty(0);

    /**
     * Armies placed during positioning phase not yet submitted to server
     */
    public final IntegerProperty NewArmies = new SimpleIntegerProperty(0);

    private final SimpleObjectProperty<Player> owner = new SimpleObjectProperty<>(new Player(-100, "NullUser", null));

    /**
     * Updates current owner and territory color
     *
     * @param Owner New owner of this territory
     */
    @Override
    public void setOwner(Player Owner) {
        owner.get().Territories.remove(this);
        owner.set(Owner);
        owner.get().Territories.add(this);
    }

    @Override
    public Player getOwner() { return owner.getValue(); }

    public int getArmies() { return Armies.add(NewArmies).getValue().intValue(); }

    /**
     * Initialize UI territory context
     *
     * @param MapHandler Map handler for this territory
     * @param MapContainer Container pane for this territory
     */
    public void loadGraphic(MapHandler MapHandler, Pane MapContainer) {
        this.mapContainer = MapContainer;

        // Name label
        final Label name = new Label(toString().toUpperCase().replaceAll(" ", "\n"));
        name.setFont(Main.globalFont);
        name.setStyle("-fx-font-size: 14px");
        name.setTextFill(Color.WHITE);
        name.setTextAlignment(TextAlignment.CENTER);
        name.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 1.5, 1.0, 0, 0));
        name.setMouseTransparent(true);
        name.setRotate(LabelR);

        // Territory image
        svgTerritory.setContent(this.SvgPath);
        svgTerritory.setStroke(Color.BLACK);
        svgTerritory.setStrokeWidth(1.5f);
        svgTerritory.setFill(this.Area.Color);
        final InnerShadow is = new InnerShadow(BlurType.GAUSSIAN, Color.TRANSPARENT, 5.0, 1.0, 0, 0);
        svgTerritory.setEffect(is);

        // Add event handler for selection
        svgTerritory.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> MapHandler.selected(this, evt.getButton().equals(MouseButton.SECONDARY)));
        svgTerritory.addEventFilter(MouseEvent.MOUSE_ENTERED, evt -> svgTerritory.setFill(this.Area.Color.darker()));
        svgTerritory.addEventFilter(MouseEvent.MOUSE_EXITED, evt -> svgTerritory.setFill(this.Area.Color));

        final ArmyBadge ab = ArmyBadge.getBadge(Armies.add(NewArmies), isRight -> MapHandler.selected(this, isRight));
        final StackPane badge = ab.getBadge();
        badge.setMouseTransparent(true);

        owner.addListener((ob, old, newOwner) -> {
            if(newOwner.Color == null) return;

            ab.getImageProperty().set(newOwner.Color.armyImg);
            is.setColor(newOwner.Color.hexColor);
        });

        Platform.runLater(() -> {
            name.setLayoutX(getCenterX(svgTerritory) + LabelX);
            name.setLayoutY(getCenterY(svgTerritory) + LabelY);
            badge.setLayoutX(getCenterX(svgTerritory) + ArmyX);
            badge.setLayoutY(getCenterY(svgTerritory) + ArmyY);

            mapContainer.getChildren().addAll(name, badge, svgTerritory);
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
     * Popup selection list for this territory and returns selected number of armies
     *
     * @return Selected armies
     */
    public int requestNumber(boolean isAttack) {
        final Integer armies = getArmies();
        if(armies == 1) return 1;

        Platform.runLater(() -> {
            Armies.set(1);

            synchronized (armies) {
                armies.notify();
            }
        });

        try {
            synchronized (armies) {
                armies.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final int selected = al.getNumber(getCenterX(svgTerritory) + ArmyX,
                getCenterY(svgTerritory) + ArmyY,
                isAttack ? Math.min(armies - 1, 3) : Math.min(armies, 2), (mapContainer.getHeight() - getCenterY(svgTerritory)) < 220.0 );

        Platform.runLater(() -> {
            Armies.set(armies);

            synchronized (armies) {
                armies.notify();
            }
        });

        try {
            synchronized (armies) {
                armies.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return selected;
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