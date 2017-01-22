package Client.UI.Match.Army;

import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by fiore on 20/01/2017.
 */
public class ArmyBadge {

    @FXML
    private StackPane badge;

    @FXML
    private ImageView armyImg;

    @FXML
    private Label armyNumber;

    public static ArmyBadge getBadge(NumberBinding Number, Consumer<Boolean> Select) {
        final ArmyBadge ab;

        final FXMLLoader loader = new FXMLLoader();

        try {
            loader.load(ArmyBadge.class.getResource("ArmyBadge.fxml").openStream());
            ab = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ab.armyImg.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> Select.accept(evt.getButton().equals(MouseButton.SECONDARY)));
        ab.armyNumber.visibleProperty().bind(Number.isNotEqualTo(0));
        ab.armyNumber.textProperty().bind(Number.asString());

        return ab;
    }

    public StackPane getBadge() {
        return badge;
    }

    public ObjectProperty<Image> getImageProperty() {
        return armyImg.imageProperty();
    }
}
