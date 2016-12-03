package Server;

import Game.Connection.ConnectionHandler;
import Game.Connection.MessageDispatcher;
import Game.GameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Risiko - Server Side");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {

        // Load UI
        launch(args);

        // Launch game server
        GameController.getInstance().init();
        MessageDispatcher.getInstance().init();
        ConnectionHandler.getInstance().Listen(5757);
    }

    @Override
    public void stop() throws Exception {
        ConnectionHandler.getInstance().terminate();
        GameController.getInstance().terminate();
        MessageDispatcher.getInstance().terminate();
        System.out.println("Shutdown completed");


        Thread.sleep(3000);

        super.stop();
    }
}
