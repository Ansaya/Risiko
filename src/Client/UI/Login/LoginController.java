package Client.UI.Login;

import Client.Game.GameController;
import Client.Main;
import Game.Sounds.Sounds;
import com.jfoenix.controls.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Login view controller
 */
public class LoginController implements Initializable {

    @FXML
    private AnchorPane parent;

    @FXML
    private JFXButton loginBtn;

    @FXML
    private JFXTextField usernameField;

    @FXML
    private JFXButton settingsBtn;

    private final JFXDialog sDialog = new JFXDialog();

    private final Preferences prefs = Preferences.userNodeForPackage(Client.Main.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parent.setBackground(new Background(new BackgroundImage(new Image(LoginController.class.getResource("background.jpg").toExternalForm()),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(950.0, 500.0, false, false, false, true))));

        loginBtn.setGraphic(new ImageView(LoginController.class.getResource("login.png").toExternalForm()));
        loginBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (evt) -> ToLobby());
        usernameField.setOnAction((evt) -> ToLobby());

        final Paint focusColor = usernameField.getFocusColor();
        usernameField.textProperty().addListener((ob, oldV, newV) -> usernameField.setFocusColor(focusColor));

        initSettings();
    }

    private void initSettings() {
        Label langLabel = new Label("Language");
        ComboBox<String> languages = new ComboBox<>(FXCollections.observableArrayList("English", "Italiano"));
        languages.getSelectionModel().select(0);
        languages.valueProperty().addListener((ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
            prefs.put("language", newVal);
        });
        VBox langVBox = new VBox(langLabel, languages);

        Label volLabel = new Label("Volume");
        JFXSlider volume = new JFXSlider(0.0f, 100.0f, Sounds.getVolume());
        volume.setShowTickLabels(true);
        volume.setShowTickMarks(true);
        volume.setMinorTickCount(10);
        volume.setBlockIncrement(1);
        volume.valueProperty().addListener((ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> Sounds.setVolume((double)newVal));
        volume.addEventHandler(MouseEvent.MOUSE_RELEASED, evt -> Sounds.Match.play());
        VBox volVBox = new VBox(volLabel, volume);

        HBox settings = new HBox(langVBox, volVBox);

        JFXButton closeBtn = new JFXButton("Close");
        closeBtn.setButtonType(JFXButton.ButtonType.RAISED);
        closeBtn.setStyle("-fx-background-color: #44B449");
        closeBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            sDialog.close();
            Sounds.Button.play();
        });

        JFXDialogLayout sLayout = new JFXDialogLayout();
        sLayout.setHeading(new Label("Settings"));
        sLayout.setBody(settings);
        sLayout.setActions(closeBtn);

        sDialog.setContent(sLayout);

        settingsBtn.setGraphic(new ImageView(LoginController.class.getResource("settings.png").toExternalForm()));
        settingsBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> Main.showDialog(sDialog));
    }

    /**
     * Connect to the server and show lobby screen
     */
    private void ToLobby() {
        if(usernameField.getText().equals("")){
            usernameField.setFocusColor(Color.RED);
            usernameField.requestFocus();
            return;
        }

        try {
            GameController.getInstance().InitConnection(usernameField.getText());
        } catch (Exception e) {
            Main.showDialog("Connection error", e.getMessage(), "Close");
            return;
        }

        Main.toLobby();
    }
}
