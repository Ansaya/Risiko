package Server;

import Server.Game.Connection.ConnectionHandler;
import Server.Game.GameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("UI/main.fxml"));
        primaryStage.setTitle("Risiko - Server");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        // Launch game server
        GameController.getInstance().init();
        ConnectionHandler.getInstance().Listen(5757);
    }


    public static void main(String[] args) {

        // Load UI
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        ConnectionHandler.getInstance().terminate();
        GameController.getInstance().terminate();
        System.out.println("Shutdown completed");

        super.stop();
    }
}
