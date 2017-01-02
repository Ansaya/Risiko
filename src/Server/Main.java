package Server;

import Server.Game.Connection.ConnectionHandler;
import Server.Game.GameController;
import Server.UI.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.getIcons().add(new Image(Client.Main.class.getResource("icon.png").openStream()));

        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(Main.class.getResource("UI/main.fxml").openStream());
        Controller c = loader.getController();
        c.initGameController();

        primaryStage.setTitle("Risiko - Server");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        // Launch connection handler
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
