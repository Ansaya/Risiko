package Client;

import Game.Match;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Match view controller
 */
public class MatchController implements Initializable {

    private double mapRatio = 725.0f / 480.0f;

    @FXML
    public AnchorPane page;

    @FXML
    public AnchorPane worldMap;

    @FXML
    public Text testo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        testo.textProperty().bind(worldMap.widthProperty().asString().concat(" X ").concat(worldMap.heightProperty()));


        worldMap.getChildren().forEach((c) -> {
            /*
            if((c instanceof Group)){
                worldMap.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    double containerRatio = (double) newValue / worldMap.getHeight();
                    c.prefWidth((double) newValue);
                    if(containerRatio < mapRatio)
                        AnchorPane.setLeftAnchor(c, (double)newValue * 20.0f / 72.5f);
                });
                worldMap.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    double containerRatio = (double) newValue / worldMap.getHeight();
                    c.prefHeight((double)newValue);
                    if(containerRatio > mapRatio)
                        AnchorPane.setTopAnchor(c, (double)newValue * 20.0f / 48.0f );
                });
            }*/

            worldMap.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                double containerRatio = (double) newValue / worldMap.getHeight();

                if(containerRatio < mapRatio) {
                    c.setScaleX((double) newValue / 725.0f);
                    c.setScaleY(c.getScaleX());
                }
            });

            worldMap.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                double containerRatio = worldMap.getWidth() / (double)newValue;

                if(containerRatio > mapRatio) {
                    c.setScaleY((double) newValue / 480.0f);
                    c.setScaleX(c.getScaleY());
                }
            });

            c.addEventHandler(MouseEvent.MOUSE_PRESSED, new TerritoryClick());
        });
    }

    private class TerritoryClick implements EventHandler<Event> {

        public void handle(Event evt) {
            SVGPath sender = (SVGPath) evt.getTarget();

            System.out.println(sender.getId());
        }
    }
}
