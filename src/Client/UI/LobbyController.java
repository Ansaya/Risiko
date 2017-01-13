package Client.UI;

import Client.Game.GameController;
import Client.Main;
import Client.Game.Observables.ObservableUser;
import Game.Connection.Lobby;
import Game.Connection.Match;
import Client.Game.Connection.MessageType;
import Game.Connection.MatchLobby;
import Game.Map.Maps;
import com.jfoenix.controls.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Login view controller
 */
public class LobbyController implements Initializable {

    @FXML
    private AnchorPane parent;

    private final MatchTable matchTable = new MatchTable();

    private final PlayersTable playersTable = new PlayersTable();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        final Label createLabel = new Label("Create new match");
        createLabel.setFont(Main.globalFont);

        final JFXTextField name = new JFXTextField();
        name.setFont(Main.globalFont);
        final JFXComboBox<Maps> map = new JFXComboBox<>(FXCollections.observableArrayList(Maps.values()));
        map.getSelectionModel().select(0);

        final JFXButton createMatchBtn = new JFXButton("Create match", new ImageView(LobbyController.class.getResource("match.png").toExternalForm()));
        createMatchBtn.setFont(Main.globalFont);
        createMatchBtn.setPadding(new Insets(5.0, 10.0, 10.0, 5.0));
        createMatchBtn.setButtonType(JFXButton.ButtonType.RAISED);
        createMatchBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if(name.getText().equals(""))
                return;

            final Match<ObservableUser> newMatch = new Match<>(0,
                    name.getText(),
                    map.getSelectionModel().selectedItemProperty().get(),
                    GameController.getInstance().getUser());

            GameController.getInstance().SendMessage(MessageType.Match, newMatch);
        });

        final VBox newMatchBox = new VBox(createLabel, name, map, createMatchBtn);
        newMatchBox.setSpacing(5.0);
        newMatchBox.setAlignment(Pos.BASELINE_CENTER);
        newMatchBox.setPrefWidth(350.0);

        playersTable.visibleProperty().addListener((ob, oldV, newV) -> matchTable.setVisible(!newV));
        newMatchBox.visibleProperty().bind(matchTable.visibleProperty());

        parent.getChildren().addAll(newMatchBox, matchTable, playersTable);
        AnchorPane.setTopAnchor(newMatchBox, 50.0);
        AnchorPane.setRightAnchor(newMatchBox, 0.0);
        AnchorPane.setTopAnchor(matchTable, 50.0);
        AnchorPane.setRightAnchor(matchTable, 350.0);
        AnchorPane.setBottomAnchor(matchTable, 0.0);
        AnchorPane.setLeftAnchor(matchTable, 0.0);
        AnchorPane.setTopAnchor(playersTable, 50.0);
        AnchorPane.setRightAnchor(playersTable, 350.0);
        AnchorPane.setBottomAnchor(playersTable, 0.0);
        AnchorPane.setLeftAnchor(playersTable, 0.0);
    }

    public void setGameController() {
        GameController.getInstance().setUpdateMatches(matchTable::updateTable);
        GameController.getInstance().setUpdateUsers(playersTable::updateTable);
        GameController.getInstance().startExecutor();
    }

    private class MatchTable extends TableView<Match> {

        public MatchTable() {
            final TableColumn<Match, Integer> idColumn = new TableColumn<>("ID");
            final TableColumn<Match, String> nameColumn = new TableColumn<>("Match");
            final TableColumn<Match, Maps> gameMapColumn = new TableColumn<>("Game map");
            idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().Id));
            nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().Name));
            gameMapColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().MapName));
            getColumns().addAll(idColumn, nameColumn, gameMapColumn);

            this.setRowFactory(tv -> {
                final TableRow<Match> row = new TableRow<>();
                row.setOnMouseClicked(evt -> {
                    if(evt.getClickCount() > 1 && !row.isEmpty()){
                        GameController.getInstance().SendMessage(MessageType.Match, row.getItem());
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
            setVisible(true);

            getItems().removeIf(Update.toRemove::contains);
            final ObservableUser user = GameController.getInstance().getUser();
            Update.toAdd.forEach(m -> m.Players.add(user));
            getItems().addAll(Update.toAdd);
        }
    }

    private class PlayersTable extends TableView<ObservableUser> {

        private final TableColumn<ObservableUser, Integer> idColumn = new TableColumn<>("ID");

        private final TableColumn<ObservableUser, String> usernameColumn = new TableColumn<>("Username");

        private final Callback<TableColumn<ObservableUser, Integer>, TableCell<ObservableUser, Integer>> defaultCellFactory = idColumn.getCellFactory();

        public PlayersTable() {
            idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
            usernameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getUsername()));

            getColumns().addAll(idColumn, usernameColumn);
        }

        public void updateTable(Lobby<ObservableUser> Update) {
            setVisible(true);
            getItems().removeIf(Update.toRemove::contains);
            getItems().addAll(Update.toAdd);
        }

        public void addUserColor() {
            idColumn.setCellFactory(tr -> {
                final ImageView iv = new ImageView();
                iv.setPreserveRatio(true);
                iv.setY(30.0);

                final TableCell<ObservableUser, Integer> idCell = new TableCell<ObservableUser, Integer>() {
                    public void updateItem(ObservableUser item, boolean empty) {
                        if(item != null)
                            iv.setImage(item.getColor().armyImg);
                    }
                };

                idCell.setGraphic(iv);

                return idCell;
            });
        }

        public void removeUserColor() {
            idColumn.setCellFactory(defaultCellFactory);
        }
    }
}
