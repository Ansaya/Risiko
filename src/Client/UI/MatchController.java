package Client.UI;

import Client.Game.GameController;
import Client.Game.Observables.*;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Match view controller
 */
public class MatchController implements Initializable {

    private double mapRatio = 725.0f / 480.0f;

    private final CardsHandler cardsHandler = new CardsHandler();

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

    public void setMapHandler() {
        mapHandler = new MapHandler(mapPane);
        GameController.getInstance().setMapHandler(mapHandler);
        mapHandler.setArmiesLabel(newArmiesLabel);
        mapHandler.setMissionButton(missionBtn);
        mapHandler.setPhaseButton(endTurnBtn);
    }

    public void setCardsHandler() {
        cardsHandler.setCardsButton(cardsBtn);
        GameController.getInstance().setCardsHandler(cardsHandler);
    }

    public void setGameController() {
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);
        GameController.getInstance().setChatUpdate(chatSP, chatContainer);
        GameController.getInstance().setUsersUpdate(usersRoot.getChildren());
    }
}
