package Client;

import Client.Game.GameController;
import Client.Game.Observables.ObservableUser;
import Client.UI.ChatBox.ChatBox;
import Client.UI.Lobby.LobbyController;
import Client.UI.Login.LoginController;
import Client.UI.Match.MatchController;
import Game.Connection.Match;
import Game.Map.Army.Color;
import Game.Map.Maps;
import Game.Sounds.Sounds;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Main extends Application {

    private static Stage window;

    private static StackPane parent;

    private static GameController gameController;

    public static void setGameController(GameController GC) { gameController = GC; }

    public static final Object dialogClosed = new Object();

    public static void toMatch(Match<ObservableUser> Match) {
        if(!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> toMatch(Match));
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setResources(gameController.getResources());
        Parent root = null;
        try {
            root = loader.load(MatchController.class.getResource("match.fxml").openStream());
            root.getStylesheets().add(MatchController.class.getResource("match.css").toExternalForm());
            root.getStylesheets().add(Main.class.getResource("UI/global.css").toExternalForm());
        }catch (IOException e) {
            e.printStackTrace();
        }

        MatchController mc = loader.getController();
        try {
            mc.setGameController(gameController, Match);
        } catch (ClassNotFoundException e) {
            showDialog("Loading error", "There has been an error loading the map", "Continue");
            return;
        }

        parent = (StackPane) root;
        final ChatBox cb = gameController.getChatBox();
        ((AnchorPane)parent.getChildren().get(0)).getChildren().add(cb);
        AnchorPane.setRightAnchor(cb, 25.0);
        AnchorPane.setBottomAnchor(cb, 0.0);

        window.setTitle("Risiko - Match");
        window.setScene(new Scene(root, 1067, 600));
        window.show();

        Platform.runLater(() -> mc.updateMapSize(1067, 600));
    }

    public static void toLobby() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(Main::toLobby);
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setResources(gameController.getResources());
        Parent root = null;
        try {
            root = loader.load(LobbyController.class.getResource("lobby.fxml").openStream());
            root.getStylesheets().add(LobbyController.class.getResource("lobby.css").toExternalForm());
            root.getStylesheets().add(Main.class.getResource("UI/global.css").toExternalForm());
        }catch (IOException e) {
            e.printStackTrace();
        }

        LobbyController lc = loader.getController();
        lc.setGameController(gameController);

        parent = (StackPane) root;
        final ChatBox cb = gameController.getChatBox();
        ((AnchorPane)parent.getChildren().get(0)).getChildren().add(cb);
        AnchorPane.setRightAnchor(cb, 25.0);
        AnchorPane.setBottomAnchor(cb, 0.0);

        window.setTitle("Risiko - Lobby");
        window.setResizable(true);
        window.setScene(new Scene(root, 1067, 600));
        window.show();
    }

    public static void toLogin() {
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(Main::toLogin);
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setResources(gameController.getResources());
        Parent root = null;
        try {
            root = loader.load(LoginController.class.getResource("login.fxml").openStream());
            root.getStylesheets().add(Main.class.getResource("UI/global.css").toExternalForm());
        }catch (IOException e) {
            e.printStackTrace();
        }

        LoginController lc = loader.getController();
        lc.setGameController(gameController);
        parent = (StackPane) root;

        window.setTitle("Risiko - Login");
        window.setScene(new Scene(root, 1067, 600));
        window.setResizable(false);
        window.show();
    }

    public static JFXDialog getDialog(String Heading, String Body, String BtnText) {
        final JFXDialog dialog = new JFXDialog();

        final JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label(Heading, new ImageView(Main.class.getResource("UI/danger.png").toExternalForm())));
        layout.setBody(new Label(Body));
        if(BtnText != null){
            JFXButton btn = new JFXButton(BtnText);
            btn.setButtonType(JFXButton.ButtonType.RAISED);
            btn.setStyle("-fx-background-color: #44B449");
            btn.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
                Sounds.Button.play();
                synchronized (dialogClosed){
                    dialogClosed.notify();
                }

                dialog.close();
            });
            layout.setActions(btn);
        }
        dialog.setContent(layout);

        return dialog;
    }

    public static void showDialog(String Heading, String Body, String BtnText) {
        final JFXDialog dialog = getDialog(Heading, Body, BtnText);
        if(Platform.isFxApplicationThread())
            dialog.show(parent);
        else
            Platform.runLater(() -> dialog.show(parent));
    }

    public static void showDialog(JFXDialog Dialog) {
        if(Platform.isFxApplicationThread())
            Dialog.show(parent);
        else
            Platform.runLater(() -> Dialog.show(parent));
    }

    public static Font globalFont = Font.font("Trebuchet MS", 12.0f);

    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;
        window.getIcons().add(new Image(Main.class.getResource("icon.png").openStream()));
        window.setMinWidth(1067.0);
        window.setMinHeight(600.0);

        gameController = new GameController();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                gameController.StopConnection(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        toLogin();

        /*toMatch(new Match<>(0, "Test match", Maps.ClassicRisikoMap, Arrays.asList(
                new ObservableUser(1, "Giocatore1", Color.BLACK),
                new ObservableUser(2, "Giocatore2", Color.RED),
                new ObservableUser(3, "Giocatore3", Color.BLUE),
                new ObservableUser(4, "Giocatore4", Color.GREEN),
                new ObservableUser(5, "Giocatore5", Color.YELLOW))));*/
    }


    public static void main(String[] args) {
        launch(args);
    }
}
