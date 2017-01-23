package Client.UI.Login;

import Client.Game.GameController;
import Client.Main;
import Game.Sounds.Sounds;
import com.jfoenix.controls.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Login view controller
 */
public class LoginController implements Initializable {

    private volatile GameController gameController;

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

    private volatile ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;

        parent.setBackground(new Background(new BackgroundImage(new Image(LoginController.class.getResource("background.jpg").toExternalForm()),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                new BackgroundSize(950.0, 500.0, false, false, false, true))));

        loginBtn.setGraphic(new ImageView(LoginController.class.getResource("login.png").toExternalForm()));
        loginBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (evt) -> toLobby());
        usernameField.setOnAction((evt) -> toLobby());

        final Paint focusColor = usernameField.getFocusColor();
        usernameField.textProperty().addListener((ob, oldV, newV) -> usernameField.setFocusColor(focusColor));

        initSettings();
    }

    public void setGameController(GameController GC) {
        this.gameController = GC;
    }

    private void initSettings() {
        final Label langLabel = new Label(resources.getString("language"));
        final ComboBox<Locale> languages = new ComboBox<>();
        languages.setCellFactory(lv -> getDefaultCell());
        languages.setButtonCell(getDefaultCell());
        languages.getItems().addAll(new Locale("en"), new Locale("it"));
        final String prefLang = prefs.get("language", "en");
        final FilteredList<Locale> items = languages.getItems().filtered(l -> l.toString().equals(prefLang));
        languages.setValue(items.isEmpty() ? languages.getItems().get(0) : items.get(0));
        languages.valueProperty().addListener((ObservableValue<? extends Locale> ob, Locale oldV, Locale newV) -> {
            prefs.put("language", newV.toString());
            gameController.setLocale(newV);
            Main.toLogin();
        });
        final VBox langVBox = new VBox(langLabel, languages);

        final Label volLabel = new Label(resources.getString("volume"));
        JFXSlider volume = new JFXSlider(0.0f, 100.0f, Sounds.getVolume());
        volume.setShowTickLabels(true);
        volume.setShowTickMarks(true);
        volume.setMinorTickCount(10);
        volume.setBlockIncrement(1);
        volume.valueProperty().addListener((ObservableValue<? extends Number> ov, Number oldVal, Number newVal) -> Sounds.setVolume((double)newVal));
        volume.addEventHandler(MouseEvent.MOUSE_RELEASED, evt -> Sounds.Match.play());
        final VBox volVBox = new VBox(volLabel, volume);

        final HBox settings = new HBox(10.0, langVBox, volVBox);

        final JFXButton closeBtn = new JFXButton(resources.getString("close"));
        closeBtn.setButtonType(JFXButton.ButtonType.RAISED);
        closeBtn.setStyle("-fx-background-color: #44B449");
        closeBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            sDialog.close();
            Sounds.Button.play();
        });

        final JFXDialogLayout sLayout = new JFXDialogLayout();
        sLayout.setHeading(new Label(resources.getString("settings")));
        sLayout.setBody(settings);
        sLayout.setActions(closeBtn);

        sDialog.setContent(sLayout);

        settingsBtn.setGraphic(new ImageView(LoginController.class.getResource("settings.png").toExternalForm()));
        settingsBtn.addEventFilter(MouseEvent.MOUSE_CLICKED, evt -> Main.showDialog(sDialog));
    }

    private ListCell<Locale> getDefaultCell() {
        return new ListCell<Locale>() {
            @Override
            public void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null)
                    setText(Main.capitalize(item.getDisplayName(item)));
            }
        };
    }

    /**
     * Connect to the server and show lobby screen
     */
    private void toLobby() {
        if(usernameField.getText().equals("")){
            usernameField.setFocusColor(Color.RED);
            usernameField.requestFocus();
            return;
        }

        try {
            gameController.InitConnection(usernameField.getText());
        } catch (Exception e) {
            Main.showDialog(resources.getString("applicationErrorTitle"), e.getMessage(), resources.getString("close"));
            return;
        }

        Main.toLobby();
    }
}
