package Server;

import Game.Connection.Chat;
import Game.Connection.ConnectionHandler;
import Game.Connection.MessageType;
import Game.GameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.google.gson.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Risiko - Server Side");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {

        GameController.getInstance().init();
        ConnectionHandler.getInstance().Listen(5757);

        launch(args);
    }

    @Override
    public void stop() throws Exception {
        GameController.getInstance().terminate();
        ConnectionHandler.getInstance().terminate();
        super.stop();
    }
}
