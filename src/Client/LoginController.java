package Client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Login view controller
 */
public class LoginController implements Initializable {

    @FXML
    protected Button loginBtn;

    @FXML
    protected TextField usernameField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (evt) -> ToLobby());
        usernameField.setOnAction((evt) -> ToLobby());
    }

    /**
     * Connect to the server and show lobby screen
     */
    private void ToLobby() {
        try {
            ServerTalk.getInstance().InitConnection(usernameField.getText());
        } catch (Exception e) {
            Main.showDialog("Connection error", e.getMessage(), "Close");
            return;
        }

        Main.toLobby();
    }
}
