package Client;

import Client.Game.GameController;
import Client.Game.Observables.ObservableUser;
import Client.UI.LobbyController;
import Client.UI.MatchController;
import Game.Connection.Match;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    private static Stage window;

    private static StackPane parent;

    public static final Object dialogClosed = new Object();

    public static void toMatch(Match<ObservableUser> Match) {
        if(!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> toMatch(Match));
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = loader.load(Main.class.getResource("UI/match.fxml").openStream());

            root.getStylesheets().add(Main.class.getResource("UI/map.css").toExternalForm());
            root.getStylesheets().add(Main.class.getResource("UI/chat.css").toExternalForm());
        }catch (IOException e) {
            e.printStackTrace();
        }

        MatchController mc = loader.getController();
        try {
            mc.setGameController("RealWorldMap", Match.Players);
        } catch (ClassNotFoundException e) {
            showDialog("Loading error", "There has been an error loading the map", "Continue");
            return;
        }

        parent = (StackPane) root;

        window.setTitle("Risiko - Match");
        window.setScene(new Scene(root, window.getWidth(), window.getHeight()));
        window.show();
    }

    public static void toLobby() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(Main::toLobby);
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = loader.load(Main.class.getResource("UI/lobby.fxml").openStream());
            root.getStylesheets().add(Main.class.getResource("UI/chat.css").toExternalForm());
        }catch (IOException e) {
            e.printStackTrace();
        }

        LobbyController lc = loader.getController();
        lc.setGameController();

        parent = (StackPane) root;

        window.setTitle("Risiko - Lobby");
        window.setResizable(true);
        window.setX(window.getX() - 538.0);
        window.setY(window.getY() - 125.0);
        window.setMinWidth(1067.0);
        window.setMinHeight(600.0);
        window.setScene(new Scene(root, 1366, 768));
        window.show();
    }

    public static void toLogin() {
        if(!Platform.isFxApplicationThread()){
            Platform.runLater(Main::toLogin);
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = loader.load(Main.class.getResource("UI/login.fxml").openStream());
        }catch (IOException e) {
            e.printStackTrace();
        }

        loader.getController();
        parent = (StackPane) root;

        window.setTitle("Risiko - Login");
        window.setMinWidth(250.0);
        window.setMinHeight(300.0);
        window.setWidth(250.0);
        window.setHeight(300.0);
        window.setScene(new Scene(root, 250, 300));
        window.setResizable(false);
        window.show();
    }

    public static JFXDialog getDialog(String Heading, String Body, String BtnText) {
        final JFXDialog dialog = new JFXDialog();

        final JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label(Heading));
        layout.setBody(new Label(Body));
        if(BtnText != null){
            JFXButton btn = new JFXButton(BtnText);
            btn.setButtonType(JFXButton.ButtonType.RAISED);
            btn.setStyle("-fx-background-color: #44B449");
            btn.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
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

    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;
        window.getIcons().add(new Image(Main.class.getResource("icon.png").openStream()));

        toLogin();

        //toMatch(new Match<>(null));
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        GameController.getInstance().StopConnection(true);

        super.stop();
    }
}
