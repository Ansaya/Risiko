package Client.UI;

import Client.Game.GameController;
import Client.Main;
import Client.Game.Observables.ObservableUser;
import Game.Connection.Chat;
import Game.Connection.Lobby;
import Game.Connection.Match;
import Client.Game.Connection.MessageType;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Login view controller
 */
public class LobbyController implements Initializable {

    @FXML
    private Button matchBtn;

    private final RecursiveTreeItem<ObservableUser> usersRoot = new RecursiveTreeItem<ObservableUser>(FXCollections.observableArrayList(), RecursiveTreeObject::getChildren);

    @FXML
    private JFXTreeTableView<ObservableUser> lobbyTable;

    @FXML
    private TreeTableColumn<ObservableUser, Integer> idColumn;

    @FXML
    private TreeTableColumn<ObservableUser, String> usernameColumn;

    @FXML
    private Label lobbyCount;

    @FXML
    private JFXTextField searchField;

    @FXML
    private JFXButton deselectAllBtn;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        matchBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
            // Get selected users
            ObservableList<TreeItem<ObservableUser>> selected = lobbyTable.getSelectionModel().getSelectedItems();

            // Check if users number is correct to begin a match
            if(selected.size() < 1 || selected.size() >= 6) {
                Main.showDialog("Match creation", "Cannot create a match with " + (selected.size() + 1) + " users.", "Close");
                return;
            }

            // Populate user list for the match
            final ArrayList<ObservableUser> players = new ArrayList<>();
            selected.forEach((t) -> players.add(t.getValue()));
            players.add(GameController.getInstance().getUser());

            // Send match request to the server
            GameController.getInstance().SendMessage(MessageType.Match, new Match<>(players));

            // GameController will open match view when match confirmation is received from the server
        });

        /* Lobby view setup */
        idColumn.setCellValueFactory(data -> data.getValue().getValue().id.asObject());
        usernameColumn.setCellValueFactory(data -> data.getValue().getValue().username);

        lobbyTable.getColumns().setAll(idColumn, usernameColumn);
        lobbyTable.setRoot(usersRoot);
        lobbyTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Bind user counter
        usersRoot.getChildren().addListener((ListChangeListener.Change<? extends TreeItem<ObservableUser>> c) -> lobbyCount.setText("( " + usersRoot.getChildren().size() + " )"));

        // Bind search field
        searchField.textProperty().addListener((o,oldVal,newVal)-> {
            System.out.println("Search: " + newVal);
            usersRoot.setPredicate((u) -> (u.getValue().id.get()+"").contains(newVal) || u.getValue().username.get().contains(newVal));
        });
    }

    public void setGameController() {
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);
        GameController.getInstance().setChatEntry(this::addChatEntry);
        GameController.getInstance().setUpdateUsers(this::updateUsers);
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

    private void updateUsers(Lobby<ObservableUser> Lobby) {
        if(Lobby == null)
            return;

        if(!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> updateUsers(Lobby));
            return;
        }

        Lobby.toRemove.forEach(u -> usersRoot.getChildren().removeIf(ul -> ul.getValue().equals(u)));
        Lobby.toAdd.remove(GameController.getInstance().getUser());
        Lobby.toAdd.forEach(u -> usersRoot.getChildren().add(new TreeItem<>(u)));
    }

    private Label getChatEntry() {
        final Label chatEntry = new Label();
        chatEntry.prefWidth(228.0f);
        chatEntry.getStyleClass().add("chat");
        chatEntry.setWrapText(true);

        return chatEntry;
    }
}
