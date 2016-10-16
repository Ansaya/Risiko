package Client;

import Game.Connection.Chat;
import Game.Connection.MessageType;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.server = ServerTalk.getInstance();
        this.server.updateHere(chatTextArea);

        matchBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new OpenMatch());

        chatSendBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                server.SendMessage(MessageType.Chat, new Chat(server.getUsername(), chatMessage.getText()));
                chatMessage.clear();
            }
        });
    }

    private class OpenMatch implements EventHandler<Event> {

        public void handle(Event evt) {
            FXMLLoader loader = new FXMLLoader();
            Parent root = null;
            try {
                root = (Parent) loader.load(getClass().getResource("match.fxml").openStream());
                root.getStylesheets().add(Main.class.getResource("map.css").toExternalForm());
            }catch (IOException e) {
                e.printStackTrace();
            }

            MatchController newMatch = loader.getController();
            newMatch.setStage(window);

            window.setTitle("Risiko - Match");
            window.setScene(new Scene(root, window.getWidth(), window.getHeight()));
            window.show();
        }
    }
}
