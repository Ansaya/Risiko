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
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.getIcons().add(new Image(Client.Main.class.getResource("icon.png").openStream()));

        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(Main.class.getResource("UI/main.fxml").openStream());
        Controller c = loader.getController();
        c.initGameController();

        primaryStage.setTitle("Risiko - Server");
        primaryStage.setMinWidth(800.0f);
        primaryStage.setMinHeight(600.0f);
        primaryStage.setScene(new Scene(root, 800.0f, 600.0f));
        primaryStage.show();

        // Launch connection handler
        ConnectionHandler.getInstance().Listen(5757);
    }


    public static void main(String[] args) {

        //args = new String[] { "risiko.jar", "console" };

        final ArrayList<String> Args = new ArrayList<>(Arrays.asList(args));

        if(Args.contains("console")) {
            GameController.getInstance().init();
            ConnectionHandler.getInstance().Listen(5757);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    terminate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
            System.out.println("Press CTRL + C to exit...");
        }
        else {
            // Load UI
            launch(args);
        }
    }

    private static void terminate() {
        System.out.println("Shouting down...");
        ConnectionHandler.getInstance().terminate();
        GameController.getInstance().terminate();
        System.out.println("Shutdown completed");
    }

    @Override
    public void stop() throws Exception {
        terminate();

        super.stop();
    }
}
