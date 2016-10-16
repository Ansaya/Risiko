package Client;

import Game.Connection.Chat;
import Game.Connection.MessageType;
import com.jfoenix.controls.JFXBadge;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.invoke.LambdaConversionException;
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
    protected TextArea chatTextArea;

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
        server.SendMessage(MessageType.Chat, new Chat(server.getUsername(), chatMessage.getText()));
        chatMessage.clear();
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.server = ServerTalk.getInstance();
        this.server.updateHere(chatTextArea);

        matchBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, Main.openMatch);
        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, sendMessage);
        chatMessage.setOnAction(sendMessage);
    }
}
