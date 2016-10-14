package Client;

import Game.Match;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Match view controller
 */
public class MatchController implements Initializable {

    private Stage window;

    public void setStage(Stage Stage) { this.window = Stage; }

    private double mapRatio = 725.0f / 480.0f;

    @FXML
    protected AnchorPane page;

    @FXML
    protected AnchorPane worldMap;

    @FXML
    protected Pane mapPane;

    @FXML
    protected Label label;

    private ArrayList<Node> territories = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        worldMap.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            double containerRatio = (double) newValue / worldMap.getHeight();

            if(containerRatio < mapRatio){
                Scale newScale = new Scale();
                double scaleValue = (double) newValue / 725.0f;
                newScale.setPivotX(0);
                newScale.setPivotY(0);
                newScale.setX(scaleValue);
                newScale.setY(scaleValue);

                mapPane.getTransforms().clear();
                mapPane.getTransforms().add(newScale);

                AnchorPane.setLeftAnchor(mapPane, -180.0f * scaleValue);
                AnchorPane.setTopAnchor(mapPane, -130.0f * scaleValue);
            }

        });
        worldMap.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            double containerRatio = (double) newValue / worldMap.getHeight();

            if(containerRatio > mapRatio) {
                Scale newScale = new Scale();
                double scaleValue = (double) newValue / 480.0f;
                newScale.setPivotX(0);
                newScale.setPivotY(0);
                newScale.setX(scaleValue);
                newScale.setY(scaleValue);

                mapPane.getTransforms().clear();
                mapPane.getTransforms().add(newScale);

                AnchorPane.setLeftAnchor(mapPane, -180.0f * scaleValue);
                AnchorPane.setTopAnchor(mapPane, -130.0f * scaleValue);
            }
        });

        mapPane.getChildren().forEach((c) -> {
            territories.add(c);
            c.addEventHandler(MouseEvent.MOUSE_PRESSED, new TerritoryClick());
        });
    }

    private class TerritoryClick implements EventHandler<Event> {

        public void handle(Event evt) {
            Node sender = (Node) evt.getTarget();

            territories.forEach((t) -> t.getStyleClass().remove("selected"));

            sender.getStyleClass().add("selected");

            System.out.println(sender.getId());
            System.out.println(sender.getStyleClass());
        }
    }
}
