package Client;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matchBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, new CreateMatch());
    }

    private class CreateMatch implements EventHandler<Event> {

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
            window.setScene(new Scene(root));
            window.show();
        }
    }
}
