package Client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage window;

    public static EventHandler openMatch = (evt) -> {
        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = (Parent) loader.load(Main.class.getResource("match.fxml").openStream());
            root.getStylesheets().add(Main.class.getResource("map.css").toExternalForm());
            root.getStylesheets().add(Main.class.getResource("chat.css").toExternalForm());
        }catch (IOException e) {
            e.printStackTrace();
        }

        MatchController newMatch = loader.getController();
        newMatch.setParent((StackPane) root);

        window.setTitle("Risiko - Match");
        window.setScene(new Scene(root, window.getWidth(), window.getHeight()));
        window.show();
    };

    public static Runnable toLobby = () -> {
        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = (Parent) loader.load(Main.class.getResource("lobby.fxml").openStream());
            root.getStylesheets().add(Main.class.getResource("chat.css").toExternalForm());
        }catch (IOException e) {
            e.printStackTrace();
        }

        LobbyController lobby = loader.getController();
        lobby.setParent((StackPane) root);

        window.setTitle("Risiko - Lobby");
        window.setResizable(true);
        window.setX(window.getX() - 538.0);
        window.setY(window.getY() - 125.0);
        window.setMinWidth(1067.0);
        window.setMinHeight(600.0);
        window.setScene(new Scene(root, 1366, 768));
        window.show();
    };

    public static Runnable toLogin = () -> {
        FXMLLoader loader = new FXMLLoader();
        Parent root = null;
        try {
            root = (Parent) loader.load(Main.class.getResource("login.fxml").openStream());
        }catch (IOException e) {
            e.printStackTrace();
        }

        LoginController login = loader.getController();
        login.setParent((StackPane) root);

        window.setTitle("Risiko - Login");
        window.setWidth(250.0);
        window.setHeight(300.0);
        window.setMinWidth(250.0);
        window.setMinHeight(300.0);
        window.setScene(new Scene(root, 250, 300));
        window.setResizable(false);
        window.show();
    };

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.window = primaryStage;

        Main.toLogin.run();
    }


    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void stop() throws Exception {
        ServerTalk.getInstance().StopConnection(true);

        super.stop();
    }
}
