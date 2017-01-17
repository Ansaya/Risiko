package Client.UI.Match;

import Client.Game.Connection.MessageType;
import Client.Game.GameController;
import Client.Game.Observables.*;
import Game.Map.Maps;
import Game.Sounds.Sounds;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Match view controller
 */
public class MatchController implements Initializable {

    @FXML
    private AnchorPane parent;

    private CardsHandler cardsHandler;

    /* Map */
    @FXML
    private AnchorPane worldMap;

    @FXML
    private Pane mapPane;

    private MapHandler mapHandler;

    private double mapRatio = 1.0;

    private double mapPrefWidth = 100.0;

    private double mapPrefHeight = 100.0;

    /* Players list */
    @FXML
    private JFXTreeTableView<ObservableUser> playersList;

    @FXML
    private TreeTableColumn<ObservableUser, Integer> idColumn;

    @FXML
    private TreeTableColumn<ObservableUser, String> usernameColumn;

    @FXML
    private TreeTableColumn<ObservableUser, Integer> territoriesColumn;

    private final RecursiveTreeItem<ObservableUser> usersRoot = new RecursiveTreeItem<ObservableUser>(FXCollections.observableArrayList(), RecursiveTreeObject::getChildren);

    /* Game */
    @FXML
    private JFXButton endTurnBtn;

    @FXML
    private Label newArmiesLabel;

    @FXML
    private Button cardsBtn;

    @FXML
    private Button missionBtn;

    private DiceBox diceBox = new DiceBox(120);

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

        diceBox.setVisible(false);
        parent.getChildren().add(diceBox);
        AnchorPane.setLeftAnchor(diceBox, 50.0);
        AnchorPane.setTopAnchor(diceBox, 50.0);
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
        final ObservableUser current = GameController.getInstance().getUser();
        final AtomicBoolean imPlaying = new AtomicBoolean(false);
        UsersList.forEach(u -> {
            if (u.equals(current)) {
                current.Color = u.Color;
                imPlaying.set(true);
            }

            final ImageView iv = new ImageView(u.Color.armyImg);
            iv.setX(30.0);
            iv.setPreserveRatio(true);

            usersRoot.getChildren().add(new TreeItem<>(u, iv));
        });
        playersList.setPrefHeight(35.0 * UsersList.size() + 30.0);

        mapHandler = new MapHandler(MapName, mapPane, UsersList);
        mapPrefWidth = mapHandler.map.PrefWidth;
        mapPrefHeight = mapHandler.map.PrefHeight;
        mapRatio = mapPrefWidth / mapPrefHeight;
        mapHandler.setArmiesLabel(newArmiesLabel);
        mapHandler.setMissionButton(missionBtn);
        mapHandler.setPhaseButton(endTurnBtn);
        mapHandler.setShowDice(diceBox::showDice);

        // If user is not a player request update for current map conditions
        if(!imPlaying.get())
            GameController.getInstance().SendMessage(MessageType.Turn, "Update");
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

    private class DiceBox extends HBox {
        private final VBox attackDice = new VBox(5);

        private final VBox defenseDice = new VBox(5);

        private final Image[] dice = new Image[6];

        public DiceBox(double width) {
            setSpacing(10.0);
            setPadding(new Insets(5.0));
            setStyle("-fx-background-color: rgba(0,0,0,.35);-fx-border-radius: 10px");
            getChildren().addAll(attackDice, defenseDice);

            widthProperty().addListener((ob, oldV, newV) -> {
                final double dieWidth = ((double)newV - 20) / 2;

                attackDice.setPrefWidth(dieWidth);
                defenseDice.setPrefWidth(dieWidth);
                setPrefHeight(dieWidth * 3 + attackDice.getSpacing() * 2);
            });

            setPrefWidth(width);

            final Timeline t = new Timeline( new KeyFrame(Duration.seconds(3), ae -> setVisible(false)));

            visibleProperty().addListener((ob, oldV, newV) -> {
                if(newV) t.playFromStart();
            });

            setOnMouseClicked(evt -> setVisible(false));

            for (int i = 0; i < 6; i++)
                dice[i] = new Image(MatchController.class.getResource((i+1) + ".png").toExternalForm());
        }

        /**
         * Show given dice values in UI
         *
         * @param AttackDice Attacking dice results
         * @param DefenseDice Defending dice results
         */
        public void showDice(Collection<Integer> AttackDice, Collection<Integer> DefenseDice) {
            attackDice.getChildren().clear();
            defenseDice.getChildren().clear();

            AttackDice.forEach(die -> attackDice.getChildren().add(getDie(dice[die - 1], true)));
            DefenseDice.forEach(die -> defenseDice.getChildren().add(getDie(dice[die - 1], false)));

            setVisible(true);
        }

        /**
         * Build die to display
         *
         * @param Die Die image
         * @param isAtk True if attack die, false if defense die
         * @return Initialized ImageView for requested die
         */
        private Pane getDie(Image Die, boolean isAtk) {
            final ImageView iv = new ImageView(Die);
            iv.setFitWidth(defenseDice.getPrefWidth());
            iv.setPreserveRatio(true);
            iv.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,.50), 3, 1.0, 0, 0)");

            final Pane p = new Pane(iv);
            if(isAtk)
                p.setStyle("-fx-background-color: #b00000");
            else
                p.setStyle("-fx-background-color: dodgerblue");
            p.setPrefSize(defenseDice.getPrefWidth(), defenseDice.getPrefWidth());

            return p;
        }
    }
}
