package Client.UI;

import Client.Game.GameController;
import Client.Game.Observables.*;
import Game.Connection.Chat;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Match view controller
 */
public class MatchController implements Initializable {

    private double mapRatio = 725.0f / 480.0f;

    private CardsHandler cardsHandler;

    /* Chat fields */
    @FXML
    private ScrollPane chatSP;

    @FXML
    private VBox chatContainer;

    @FXML
    private TextField chatMessage;

    @FXML
    private Button chatSendBtn;

    @FXML
    private JFXBadge chatBadge;

    private final AtomicInteger lastSenderId = new AtomicInteger(-1);

    /**
     * Lambda for chat message sending
     */
    private EventHandler sendMessage = (evt) -> {
        if(!chatMessage.getText().trim().equals(""))
            GameController.getInstance().SendChat(chatMessage.getText().trim());

        chatMessage.clear();
    };

    private final RecursiveTreeItem<ObservableUser> usersRoot = new RecursiveTreeItem<ObservableUser>(FXCollections.observableArrayList(), RecursiveTreeObject::getChildren);

    /* Map */
    @FXML
    private AnchorPane worldMap;

    @FXML
    private Pane mapPane;

    private MapHandler mapHandler;

    @FXML
    private JFXTreeTableView<ObservableUser> playersList;

    @FXML
    private TreeTableColumn<ObservableUser, Integer> idColumn;

    @FXML
    private TreeTableColumn<ObservableUser, String> usernameColumn;

    @FXML
    private TreeTableColumn<ObservableUser, Integer> territoriesColumn;

    /* Game */
    @FXML
    private JFXButton endTurnBtn;

    @FXML
    private Label newArmiesLabel;

    @FXML
    private Button cardsBtn;

    @FXML
    private Button missionBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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

        /* Players table setup */
        idColumn.setCellValueFactory(data -> data.getValue().getValue().id.asObject());
        usernameColumn.setCellValueFactory(data -> data.getValue().getValue().username);
        territoriesColumn.setCellValueFactory(data -> data.getValue().getValue().territories.asObject());

        playersList.getColumns().setAll(idColumn, usernameColumn, territoriesColumn);
        playersList.setRoot(usersRoot);
    }

    private void setMapHandler(ArrayList<ObservableUser> UsersList) {
        if(UsersList != null) {
            final ObservableUser current = GameController.getInstance().getUser();
            UsersList.forEach(u -> {
                if (u.equals(current))
                    current.color = u.color;

                usersRoot.getChildren().add(new TreeItem<>(u));
            });
        }
        mapHandler = new MapHandler(mapPane, UsersList);
        mapHandler.setArmiesLabel(newArmiesLabel);
        mapHandler.setMissionButton(missionBtn);
        mapHandler.setPhaseButton(endTurnBtn);
    }

    private void setCardsHandler() {
        cardsHandler = new CardsHandler();
        cardsHandler.setCardsButton(cardsBtn);
    }

    public void setGameController(ArrayList<ObservableUser> UsersList) {
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);
        GameController.getInstance().setChatEntry(this::addChatEntry);
        setMapHandler(UsersList);
        GameController.getInstance().setMapHandler(mapHandler);
        setCardsHandler();
        GameController.getInstance().setCardsHandler(cardsHandler);
        GameController.getInstance().startExecutor();
    }

    private void addChatEntry(Chat<ObservableUser> Chat) {
        final Label sender = getChatEntry();
        final Label text = getChatEntry();

        sender.setText(Chat.sender.username.get());
        text.setText(Chat.message);

        // If message is From this client display it on opposite side of chat view
        if(Chat.sender.equals(GameController.getInstance().getUser())){
            sender.setAlignment(Pos.TOP_RIGHT);
            text.setAlignment(Pos.TOP_RIGHT);
        }

        if(Chat.sender.color != null) {
            sender.setTextFill(Chat.sender.color.hexColor);
            text.setTextFill(Chat.sender.color.hexColor);
        }

        if(!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> {
                // If message is from same sender as before, avoid writing sender again
                if (lastSenderId.get() != Chat.sender.id.get())
                    chatContainer.getChildren().add(sender);

                lastSenderId.set(Chat.sender.id.get());

                chatContainer.getChildren().add(text);

                // Scroll container to end
                chatSP.setVvalue(1.0f);
            });
            return;
        }

        // If message is from same sender as before, avoid writing sender again
        if(lastSenderId.get() != Chat.sender.id.get())
            chatContainer.getChildren().add(sender);

        lastSenderId.set(Chat.sender.id.get());

        chatContainer.getChildren().add(text);

        // Scroll container to end
        chatSP.setVvalue(1.0f);
    }

    private Label getChatEntry() {
        final Label chatEntry = new Label();
        chatEntry.prefWidth(228.0f);
        chatEntry.getStyleClass().add("chat");
        chatEntry.setWrapText(true);

        return chatEntry;
    }
}
