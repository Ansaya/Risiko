package Client;

import Game.Connection.Chat;
import Game.Connection.MessageType;
import com.jfoenix.controls.JFXBadge;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
        if(chatMessage.getText().equals(""))
            return;

        server.SendMessage(MessageType.Chat, new Chat(server.getUsername(), chatMessage.getText()));
        chatMessage.clear();
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.server = ServerTalk.getInstance();
        this.server.updateHere(chatSP, chatContainer);

        matchBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, Main.openMatch);
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);
    }
}
