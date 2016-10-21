package Client;

import Client.Observables.ObservableUser;
import Game.Connection.Chat;
import Game.Connection.Lobby;
import Game.Connection.MessageType;
import Game.Connection.User;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.jws.soap.SOAPBinding;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Login view controller
 */
public class LobbyController implements Initializable {

    private Stage window;

    public void setStage(Stage Stage) { window = Stage; }

    @FXML
    protected Button matchBtn;

    private ServerTalk server;

    @FXML
    protected JFXTreeTableView<ObservableUser> lobbyTable;

    @FXML
    protected Label lobbyCount;

    @FXML
    protected JFXTextField searchField;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.server = ServerTalk.getInstance();

        /* Chat setup */
        this.server.setChatUpdate(chatSP, chatContainer);
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);

        matchBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, Main.openMatch);

        /* Lobby view setup */
        JFXTreeTableColumn<ObservableUser, Integer> idColumn = new JFXTreeTableColumn<>("User ID");
        idColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ObservableUser, Integer> param) -> {
            if(idColumn.validateValue(param)) return param.getValue().getValue().UserId.asObject();
            else return idColumn.getComputedValue(param);
        });

        JFXTreeTableColumn<ObservableUser, String> usernameColumn = new JFXTreeTableColumn<>("Username");
        usernameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ObservableUser, String> param) -> {
            if(usernameColumn.validateValue(param)) return param.getValue().getValue().Username;
            else return usernameColumn.getComputedValue(param);
        });

        final RecursiveTreeItem<ObservableUser> rootItem = new RecursiveTreeItem<ObservableUser>(FXCollections.observableArrayList(), RecursiveTreeObject::getChildren);
        lobbyTable.getColumns().setAll(idColumn, usernameColumn);
        lobbyTable.setRoot(rootItem);
        lobbyTable.setShowRoot(false);
        lobbyTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Bind user counter
        rootItem.getChildren().addListener((ListChangeListener.Change<? extends TreeItem<ObservableUser>> c) -> lobbyCount.setText("( " + rootItem.getChildren().size() + " )"));

        // Bind search field

        searchField.textProperty().addListener((o,oldVal,newVal)-> {
            System.out.println("Search: " + newVal);
            rootItem.setPredicate((u) -> (u.getValue().UserId.get()+"").contains(newVal) || u.getValue().Username.get().contains(newVal));
        });


        this.server.setLobbyUpdate(rootItem.getChildren());
    }
}
