package Client.UI;

import Client.Game.GameController;
import Client.Game.Observables.*;
import Game.Map.Maps;
import Game.Sounds.Sounds;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Match view controller
 */
public class MatchController implements Initializable {

    private double mapRatio = 1.0;

    private double mapPrefWidth = 100.0;

    private double mapPrefHeight = 100.0;

    private CardsHandler cardsHandler;

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

        endTurnBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> Sounds.Button.play());
        cardsBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> Sounds.Button.play());
        missionBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> Sounds.Button.play());

        /* Map rescaling */
        worldMap.widthProperty().addListener(this::setMapWidth);
        worldMap.heightProperty().addListener(this::setMapHeight);

        /* Players table setup */
        idColumn.setCellValueFactory(data -> data.getValue().getValue().Id.asObject());
        usernameColumn.setCellValueFactory(data -> data.getValue().getValue().Username);
        territoriesColumn.setCellValueFactory(data -> data.getValue().getValue().Territories.asObject());

        playersList.setRoot(usersRoot);
        playersList.setMouseTransparent(true);
    }

    public void updateMapSize(double newWidth, double newHeight){
        setMapWidth(null, null, newWidth);
        setMapHeight(null, null, newHeight);
    }

    private void setMapWidth(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        double containerRatio = (double) newValue / worldMap.getHeight();

        if(containerRatio < mapRatio)
            updateMap((double) newValue / mapPrefWidth);
    }

    private void setMapHeight(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        double containerRatio = worldMap.getWidth() / (double) newValue;

        if(containerRatio > mapRatio)
            updateMap((double) newValue / mapPrefHeight);
    }

    private void updateMap(double scaleValue) {
        Scale newScale = new Scale();
        newScale.setPivotX(0);
        newScale.setPivotY(0);
        newScale.setX(scaleValue);
        newScale.setY(scaleValue);

        mapPane.getTransforms().clear();
        mapPane.getTransforms().add(newScale);

        AnchorPane.setLeftAnchor(mapPane, -160.0f * scaleValue);
        AnchorPane.setTopAnchor(mapPane, -110.0f * scaleValue);
    }

    private void setMapHandler(Maps MapName, ArrayList<ObservableUser> UsersList) throws ClassNotFoundException {
        if(UsersList != null) {
            final ObservableUser current = GameController.getInstance().getUser();
            UsersList.forEach(u -> {
                if (u.equals(current))
                    current.Color = u.Color;

                final ImageView iv = new ImageView(u.Color.armyImg);
                iv.setX(30.0);
                iv.setPreserveRatio(true);

                usersRoot.getChildren().add(new TreeItem<>(u, iv));
            });
            playersList.setPrefHeight(35.0 * UsersList.size() + 30.0);
        }

        mapHandler = new MapHandler(MapName, mapPane, UsersList);
        mapPrefWidth = mapHandler.map.PrefWidth;
        mapPrefHeight = mapHandler.map.PrefHeight;
        mapRatio = mapPrefWidth / mapPrefHeight;
        mapHandler.setArmiesLabel(newArmiesLabel);
        mapHandler.setMissionButton(missionBtn);
        mapHandler.setPhaseButton(endTurnBtn);
    }

    private void setCardsHandler() {
        cardsHandler = new CardsHandler();
        cardsHandler.setCardsButton(cardsBtn);
    }

    public void setGameController(Maps MapName, ArrayList<ObservableUser> UsersList) throws ClassNotFoundException {
        setMapHandler(MapName, UsersList);
        GameController.getInstance().setMapHandler(mapHandler);
        setCardsHandler();
        GameController.getInstance().setCardsHandler(cardsHandler);
        GameController.getInstance().startExecutor();
    }
}
