package Client;

import javafx.event.ActionEvent;
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
            // Notify user for error
            return;
        }

        Main.toLobby.run();
    }
}
