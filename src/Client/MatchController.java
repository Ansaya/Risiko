package Client;

import Game.Connection.Chat;
import Game.Connection.MessageType;
import Game.Map.Territories;
import Game.Match;
import com.jfoenix.controls.JFXBadge;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Match view controller
 */
public class MatchController implements Initializable {

    private Stage window;

    public void setStage(Stage Stage) { this.window = Stage; }

    private double mapRatio = 725.0f / 480.0f;

    private ServerTalk server = ServerTalk.getInstance();

    /* Chat fields */
    @FXML
    protected ScrollPane chatSP;

    @FXML
    protected VBox chatContainer;

    @FXML
    protected TextField chatMessage;

    @FXML
    protected Button chatSendBtn;

    @FXML
    protected JFXBadge chatBadge;

    /**
     * Lambda for chat message sending
     */
    private EventHandler sendMessage = (evt) -> {
        if(!chatMessage.getText().trim().equals(""))
            server.SendMessage(MessageType.Chat, new Chat(server.getUsername(), chatMessage.getText().trim()));

        chatMessage.clear();
    };

    /* Map */
    @FXML
    protected AnchorPane worldMap;

    @FXML
    protected Pane mapPane;

    @FXML
    protected Label label;

    private ArrayList<Node> territories = new ArrayList<>();

    private HashMap<Territories, Node> map = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /* Chat setup */
        this.server.setChatUpdate(chatSP, chatContainer);
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);

        // Set chat updatable fields
        server.setChatUpdate(chatSP, chatContainer);


        /* Map rescaling */
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
            double containerRatio = worldMap.getWidth() / (double) newValue;

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

        // Get territories and set click event listener
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
