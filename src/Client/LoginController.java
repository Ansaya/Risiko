package Client;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Login view controller
 */
public class LoginController implements Initializable {

    private Stage window;

    public void setStage(Stage Stage) { this.window = Stage; }

    @FXML
    protected Button loginBtn;

    @FXML
    protected TextField usernameField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new LoginClick());
    }

    private class LoginClick implements EventHandler<Event> {

        public void handle(Event evt) {
            ServerTalk.getInstance().InitConnection(usernameField.getText());
            ToLobby();
        }

        private void ToLobby() {
            FXMLLoader loader = new FXMLLoader();
            Parent root = null;
            try {
                root = (Parent) loader.load(getClass().getResource("lobby.fxml").openStream());
            }catch (IOException e) {
                e.printStackTrace();
            }

            LobbyController lobby = loader.getController();
            lobby.setStage(window);

            window.setTitle("Risiko - Lobby");
            window.setScene(new Scene(root, 1366, 768));
            window.setX(window.getX() - 538);
            window.setY(window.getY() - 125);
            window.setMinWidth(1067);
            window.setMinHeight(600);
            window.setResizable(true);
            window.show();
        }
    }
}
