package Client.UI;

import Client.Main;
import Client.Game.Observables.ObservableUser;
import Client.Game.ServerTalk;
import Game.Connection.Chat;
import Game.Connection.Match;
import Client.Game.Connection.MessageType;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Login view controller
 */
public class LobbyController implements Initializable {

    @FXML
    protected Button matchBtn;

    private ServerTalk server = ServerTalk.getInstance();

    @FXML
    protected JFXTreeTableView<ObservableUser> lobbyTable;

    @FXML
    protected TreeTableColumn<ObservableUser, Integer> idColumn;

    @FXML
    protected TreeTableColumn<ObservableUser, String> usernameColumn;

    @FXML
    protected Label lobbyCount;

    @FXML
    protected JFXTextField searchField;

    @FXML
    protected JFXButton deselectAllBtn;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* Chat setup */
        this.server.setChatUpdate(chatSP, chatContainer);
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);

        matchBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
            // Get selected users
            ObservableList<TreeItem<ObservableUser>> selected = lobbyTable.getSelectionModel().getSelectedItems();

            // Check if users number is correct to begin a match
            if(selected.size() < 1 || selected.size() >= 6) {
                Main.showDialog("Match creation", "Cannot create a match with " + (selected.size() + 1) + " users.", "Close");
                return;
            }

            // Populate user list for the match
            ArrayList<ObservableUser> players = new ArrayList<>();
            selected.forEach((t) -> players.add(t.getValue()));
            players.add(server.getUser());

            // Send match request to the server
            server.SendMessage(MessageType.Match, new Match<>(players));

            // ServerTalk will open match view when match confirmation is received from the server
        });

        /* Lobby view setup */
        idColumn.setCellValueFactory(data -> data.getValue().getValue().id.asObject());
        usernameColumn.setCellValueFactory(data -> data.getValue().getValue().username);

        final RecursiveTreeItem<ObservableUser> rootItem = new RecursiveTreeItem<ObservableUser>(FXCollections.observableArrayList(), RecursiveTreeObject::getChildren);
        lobbyTable.getColumns().setAll(idColumn, usernameColumn);
        lobbyTable.setRoot(rootItem);
        lobbyTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Bind user counter
        rootItem.getChildren().addListener((ListChangeListener.Change<? extends TreeItem<ObservableUser>> c) -> lobbyCount.setText("( " + rootItem.getChildren().size() + " )"));

        // Bind search field
        searchField.textProperty().addListener((o,oldVal,newVal)-> {
            System.out.println("Search: " + newVal);
            rootItem.setPredicate((u) -> (u.getValue().id.get()+"").contains(newVal) || u.getValue().username.get().contains(newVal));
        });

        this.server.setUsersUpdate(rootItem.getChildren());
    }
}
