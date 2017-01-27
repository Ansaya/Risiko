package Server;

import Game.Logger;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends Application {

    private static final int listenPort = 8080;

    private static final GameController GC = new GameController();

    private static final ConnectionHandler CH = new ConnectionHandler(GC);

    public static Path getClientLogPath() {
        final Path clientLogDir = Paths.get("./ClientLogs/" + LocalDate.now(ZoneId.of("Z")) + "/");

        if(Files.notExists(clientLogDir)) {
            try {
                Files.createDirectories(clientLogDir);
            } catch (IOException e) {
                Logger.err("Cannot create client log directory.", e);
            }
        }

        return clientLogDir;
    }

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

        Logger.setOutPath(LocalDate.now(ZoneId.of("Z")) + " outlog.txt");
        Logger.setErrPath(LocalDate.now(ZoneId.of("Z")) + " errlog.txt");

        if(Args.contains("console")) {
            initialize(null, null);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    terminate();
                } catch (Exception e) {
                    Logger.log("Shutdown error.");
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
        if(GC.isListening() && CH.isListening()) return;

        Logger.log("Initializing...");
        GC.init(Players, Matches);
        CH.listen(listenPort);
        Logger.log("Initialization completed.");
    }

    private static void terminate() {
        if(!GC.isListening() && !CH.isListening()) return;

        Logger.log("Shouting down...");
        CH.terminate();
        GC.terminate();
        Logger.log("Shutdown completed.");
    }

    @Override
    public void stop() throws Exception {
        terminate();

        super.stop();
    }
}
