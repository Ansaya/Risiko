package Client.UI.Match.Army;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import Game.Map.Army.Color;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fiore on 20/01/2017.
 */
public class ArmyList implements Initializable {

    private Pane parent;

    @FXML
    private VBox list;

    public VBox getList() { return list; }

    @FXML
    private AnchorPane army1;

    @FXML
    private AnchorPane army2;

    @FXML
    private AnchorPane army3;

    private ArrayList<ImageView> armyImages = new ArrayList<>();

    private final AtomicInteger selected = new AtomicInteger(1);

    public static ArmyList getArmyList(Color Army) {
        final ArmyList al;

        final FXMLLoader loader = new FXMLLoader();

        try {
            loader.load(ArmyList.class.getResource("ArmyList.fxml").openStream());
            al = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        al.setArmy(Army);

        return al;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        list.getChildren().forEach(ap -> ((AnchorPane)ap).getChildren().forEach(node -> {
                if(node instanceof ImageView)
                    armyImages.add((ImageView) node);
            }));

        army1.setOnMouseClicked(evt -> {
            synchronized (selected){
                selected.set(1);
                selected.notify();
            }
        });

        army2.setOnMouseClicked(evt -> {
            synchronized (selected){
                selected.set(2);
                selected.notify();
            }
        });

        army3.setOnMouseClicked(evt -> {
            synchronized (selected){
                selected.set(3);
                selected.notify();
            }
        });
    }

    /**
     * Popup the list and return the selected number of armies
     *
     * @param LayoutX X offset inside map container
     * @param LayoutY Y offset inside map container
     * @return Selected number of armies
     */
    public int getNumber(double LayoutX, double LayoutY, int Max) {
        Platform.runLater(() -> {
            list.setLayoutX(LayoutX);
            list.setLayoutY(LayoutY);
            list.toFront();

            army3.setVisible(Max == 3);
            army3.setMouseTransparent(Max != 3);

            list.setVisible(true);
        });

        synchronized (selected) {
            try {
                selected.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return 1;
            }
        }

        Platform.runLater(() -> {
            list.setVisible(false);

            synchronized (selected) {
                selected.notify();
            }
        });

        synchronized (selected) {
            try {
                selected.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return selected.get();
    }

    private void setArmy(Color Army) {
        armyImages.forEach(iv -> iv.setImage(Army.armyImg));
    }

    private void setParent(Pane Parent) {
        parent = Parent;
    }
}
