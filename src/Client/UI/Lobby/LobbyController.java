package Client.UI.Lobby;

import Client.Game.GameController;
import Client.Main;
import Client.Game.Observables.ObservableUser;
import Game.Connection.GameState;
import Game.Connection.Lobby;
import Game.Connection.Match;
import Client.Game.Connection.MessageType;
import Game.Connection.MatchLobby;
import Game.Map.Maps;
import Game.StateType;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

/**
 * Login view controller
 */
public class LobbyController implements Initializable {

    @FXML
    private AnchorPane parent;

    private volatile MatchTable matchTable;

    private volatile PlayersTable playersTable;

    private volatile GameController gameController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matchTable = new MatchTable(resources);
        playersTable = new PlayersTable(resources);

        // Create match label
        final Label createLabel = new Label(resources.getString("createMatchLabel"));
        createLabel.setFont(Main.globalFont);

        // New match name text field
        final JFXTextField name = new JFXTextField();
        name.setFont(Main.globalFont);
        name.setPrefWidth(300.0);

        // New match map selector
        final JFXComboBox<Maps> map = new JFXComboBox<>(FXCollections.observableArrayList(Maps.values()));
        map.setCellFactory(new Callback<ListView<Maps>, ListCell<Maps>>() {
            @Override
            public ListCell<Maps> call(ListView<Maps> param) {
                return new ListCell<Maps>() {
                    @Override
                    public void updateItem(Maps item, boolean isEmpty) {
                        super.updateItem(item, isEmpty);
                        if(item != null)
                            setText(Maps.getName(item, resources.getLocale()));
                    }
                };
            }
        });
        map.getSelectionModel().select(0);

        // New match creation button
        final JFXButton createMatchBtn = new JFXButton(resources.getString("createMatchBtn"), new ImageView(LobbyController.class.getResource("match.png").toExternalForm()));
        createMatchBtn.setFont(Main.globalFont);
        createMatchBtn.setPadding(new Insets(5.0, 10.0, 10.0, 5.0));
        createMatchBtn.setButtonType(JFXButton.ButtonType.RAISED);
        createMatchBtn.setStyle("-fx-background-color: #44B449");

        name.setOnAction(evt -> {
            createMatch(name.getText(), map.getSelectionModel().selectedItemProperty().get());
            name.clear();
        });
        createMatchBtn.setOnMouseClicked(evt -> {
            createMatch(name.getText(), map.getSelectionModel().selectedItemProperty().get());
            name.clear();
        });

        // New match fields container
        final VBox newMatchBox = new VBox(createLabel, name, map, createMatchBtn);
        newMatchBox.setSpacing(5.0);
        newMatchBox.setAlignment(Pos.BASELINE_CENTER);
        newMatchBox.setPrefWidth(350.0);
        newMatchBox.visibleProperty().bind(matchTable.visibleProperty());

        // Start match button
        final JFXButton startMatchBtn = new JFXButton(resources.getString("startMatchBtn"), new ImageView(LobbyController.class.getResource("game.png").toExternalForm()));
        startMatchBtn.setButtonType(JFXButton.ButtonType.RAISED);
        startMatchBtn.setFont(Main.globalFont);
        startMatchBtn.setPadding(new Insets(10.0, 20.0, 10.0, 20.0));
        startMatchBtn.setStyle("-fx-background-color: #44B449");
        startMatchBtn.setOnMouseClicked(evt -> {
            if(playersTable.getItems().size() > 1)
                gameController.SendMessage(MessageType.Turn, "");
        });

        // Exit match room button
        final JFXButton exitMatchBtn = new JFXButton(resources.getString("exitMatchBtn"), new ImageView(LobbyController.class.getResource("exit.png").toExternalForm()));
        exitMatchBtn.setButtonType(JFXButton.ButtonType.RAISED);
        exitMatchBtn.setFont(Main.globalFont);
        exitMatchBtn.setPadding(new Insets(10.0, 20.0, 10.0, 20.0));
        exitMatchBtn.setStyle("-fx-background-color: #03A9F4");
        exitMatchBtn.setOnMouseClicked(evt ->
                gameController.SendMessage(MessageType.GameState, new GameState<>(StateType.Abandoned, gameController.getUser())));

        // Match room buttons container
        final VBox matchRoomBox = new VBox(startMatchBtn, exitMatchBtn);
        matchRoomBox.setSpacing(20.0);
        matchRoomBox.setAlignment(Pos.BASELINE_CENTER);
        matchRoomBox.setPrefWidth(350.0);
        matchRoomBox.visibleProperty().bind(playersTable.visibleProperty());

        parent.getChildren().addAll(matchRoomBox, newMatchBox, matchTable, playersTable);
        AnchorPane.setTopAnchor(newMatchBox, 50.0);
        AnchorPane.setRightAnchor(newMatchBox, 0.0);
        AnchorPane.setTopAnchor(matchRoomBox, 50.0);
        AnchorPane.setRightAnchor(matchRoomBox, 0.0);
        AnchorPane.setTopAnchor(matchTable, 50.0);
        AnchorPane.setRightAnchor(matchTable, 350.0);
        AnchorPane.setBottomAnchor(matchTable, 0.0);
        AnchorPane.setLeftAnchor(matchTable, 0.0);
        AnchorPane.setTopAnchor(playersTable, 50.0);
        AnchorPane.setRightAnchor(playersTable, 350.0);
        AnchorPane.setBottomAnchor(playersTable, 0.0);
        AnchorPane.setLeftAnchor(playersTable, 0.0);

        playersTable.setVisible(false);
        matchTable.setVisible(true);
    }

    public void setGameController(GameController GC) {
        this.gameController = GC;
        GC.setUpdateMatches(this::update);
        GC.setUpdateUsers(this::update);
        GC.startExecutor();
    }

    private void createMatch(String Name, Maps Map) {
        if(Name.equals(""))
            return;

        gameController.SendMessage(MessageType.Match, new Match<>(0, Name, Map, gameController.getUser()));
    }

    /**
     * Update UI relatively to given message
     *
     * @param Update Update message from server
     */
    private void update(Object Update) {
        // If server sent rooms
        if(Update instanceof MatchLobby){
            // Disable players view if on
            if(playersTable.isVisible()) {
                matchTable.getItems().clear();
                playersTable.setVisible(false);
            }

            matchTable.updateTable((MatchLobby<Match<ObservableUser>>) Update);
        }

        // If server sent room's players
        if(Update instanceof Lobby){
            // Disable matches lobby if on
            if(matchTable.isVisible()) {
                playersTable.getItems().clear();
                matchTable.setVisible(false);
            }

            playersTable.updateTable((Lobby<ObservableUser>) Update);
        }
    }

    private class MatchTable extends TableView<Match> {

        public MatchTable(ResourceBundle resources) {
            final TableColumn<Match, Integer> idColumn = new TableColumn<>(resources.getString("id"));
            final TableColumn<Match, String> nameColumn = new TableColumn<>(resources.getString("match"));
            final TableColumn<Match, Maps> gameMapColumn = new TableColumn<>(resources.getString("gameMap"));
            final TableColumn<Match, String> playersColumn = new TableColumn<>(resources.getString("players"));
            idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().Id));
            nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().Name));
            gameMapColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().GameMap));
            playersColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().Players.size() + "/6"));
            getColumns().addAll(idColumn, nameColumn, gameMapColumn, playersColumn);

            this.setRowFactory(tv -> {
                final TableRow<Match> row = new TableRow<>();
                row.setOnMouseClicked(evt -> {
                    if(evt.getClickCount() > 1 && !row.isEmpty()) {
                        final Match match = row.getItem();
                        if(match.Players.size() == 6 && !match.IsStarted)
                            return;

                        gameController.SendMessage(MessageType.Match, new Match<>(match.Id, match.Name, match.GameMap, gameController.getUser()));
                    }
                });

                return row;
            });
        }

        /**
         * Update match table with message from server
         *
         * @param Update Server update message
         */
        public void updateTable(MatchLobby<Match<ObservableUser>> Update) {
            if(!Platform.isFxApplicationThread()) {
                Platform.runLater(() -> updateTable(Update));
                return;
            }

            setVisible(true);
            Update.toRemove.forEach(getItems()::remove);
            Update.toAdd.forEach(getItems()::add);
        }
    }

    public class PlayersTable extends TableView<ObservableUser> {

        private final TableColumn<ObservableUser, Integer> idColumn;

        private final TableColumn<ObservableUser, String> usernameColumn;

        public PlayersTable(ResourceBundle resources) {
            idColumn = new TableColumn<>(resources.getString("id"));
            usernameColumn = new TableColumn<>(resources.getString("username"));

            idColumn.setCellValueFactory(data -> data.getValue().Id.asObject());
            usernameColumn.setCellValueFactory(data -> data.getValue().Username);

            getColumns().addAll(idColumn, usernameColumn);
        }

        public void updateTable(Lobby<ObservableUser> Update) {
            updateTable(Update.toAdd, Update.toRemove);
        }

        public void updateTable(Collection<ObservableUser> ToAdd, Collection<ObservableUser> ToRemove) {
            if(!Platform.isFxApplicationThread()) {
                Platform.runLater(() -> updateTable(ToAdd, ToRemove));
                return;
            }

            setVisible(true);
            getItems().removeIf(ToRemove::contains);
            getItems().addAll(ToAdd);
        }
    }
}
