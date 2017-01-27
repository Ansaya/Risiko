package Server;

import Server.Game.ConnectionHandler;
import Server.Game.GameController;
import Server.Game.Match;
import Server.Game.Player;
import Server.UI.Controller;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends Application {

    private static final int listenPort = 5757;

    private static final GameController GC = new GameController();

    private static final ConnectionHandler CH = new ConnectionHandler(GC);

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.getIcons().add(new Image(Client.Main.class.getResource("icon.png").openStream()));

        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(Main.class.getResource("UI/main.fxml").openStream());
        Controller c = loader.getController();
        c.initGameController(Main::initialize, Main::terminate);

        primaryStage.setTitle("Risiko - Server");
        primaryStage.setMinWidth(800.0f);
        primaryStage.setMinHeight(600.0f);
        primaryStage.setScene(new Scene(root, 800.0f, 600.0f));
        primaryStage.show();
    }


    public static void main(String[] args) {
        final ArrayList<String> Args = new ArrayList<>(Arrays.asList(args));

        if(Args.contains("console")) {
            initialize(null, null);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    terminate();
                } catch (Exception e) {
                    System.err.println("Shutdown error.");
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

    private static void initialize(ObservableList<Player> Players, ObservableList<Match> Matches) {
        System.out.println("Initializing...");
        GC.init(Players, Matches);
        CH.listen(listenPort);
        System.out.println("Initialization completed.");
    }

    private static void terminate() {
        System.out.println("Shouting down...");
        CH.terminate();
        GC.terminate();
        System.out.println("Shutdown completed.");
    }

    @Override
    public void stop() throws Exception {
        terminate();

        super.stop();
    }
}
