package Client.UI;

import Client.Game.Observables.*;
import Client.Game.ServerTalk;
import Client.Main;
import Game.Connection.*;
import Client.Game.Connection.MessageType;
import Game.Map.Territories;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Match view controller
 */
public class MatchController implements Initializable {

    private double mapRatio = 725.0f / 480.0f;

    private ServerTalk server = ServerTalk.getInstance();

    @FXML
    protected StackPane parent;

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
            server.SendMessage(MessageType.Chat, new Chat<>(server.getUser(), chatMessage.getText().trim()));

        chatMessage.clear();
    };

    /* Map */
    @FXML
    protected AnchorPane worldMap;

    @FXML
    protected Pane mapPane;

    @FXML
    protected JFXTreeTableView<ObservableUser> playersList;

    @FXML
    protected TreeTableColumn<ObservableUser, Integer> idColumn;

    @FXML
    protected TreeTableColumn<ObservableUser, String> usernameColumn;

    @FXML
    protected TreeTableColumn<ObservableUser, Integer> territoriesColumn;

    /* Game */
    @FXML
    protected JFXButton endTurnBtn;

    @FXML
    protected Label newArmiesLabel;

    @FXML
    protected Button cardsBtn;

    private JFXDialog cardsDialog;

    @FXML
    protected Button missionBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /* Chat setup */
        this.server.setChatUpdate(chatSP, chatContainer);
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);

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

        /* Map handler setup */
        final ArrayList<Label> labels = new ArrayList<>();

        // Retrieve labels from map view
        mapPane.getChildren().forEach((c) -> {
            if(c instanceof Label) {
                labels.add((Label) c);
            }
        });

        // Initialize UI handler
        UIHandler.Init(mapPane, labels, endTurnBtn, newArmiesLabel);
        UIHandler.CardsHandler = new CardsHandler(parent);
        cardsDialog = UIHandler.CardsHandler.getCardsDialog();
        cardsBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> cardsDialog.show());

        /* Players table setup */
        idColumn.setCellValueFactory(data -> data.getValue().getValue().id.asObject());
        usernameColumn.setCellValueFactory(data -> data.getValue().getValue().username);
        territoriesColumn.setCellValueFactory(data -> data.getValue().getValue().territories.asObject());

        final RecursiveTreeItem<ObservableUser> rootItem = new RecursiveTreeItem<ObservableUser>(FXCollections.observableArrayList(), RecursiveTreeObject::getChildren);
        playersList.getColumns().setAll(idColumn, usernameColumn, territoriesColumn);
        playersList.setRoot(rootItem);

        // Update server talk objects
        server.setUsersUpdate(rootItem.getChildren());
    }
}
