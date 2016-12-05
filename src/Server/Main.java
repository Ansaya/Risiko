package Server;

import Client.Observables.ObservableUser;
import Game.Connection.ConnectionHandler;
import Game.GameController;
import Game.Player;
import com.google.gson.Gson;
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

        // Launch game server
        GameController.getInstance().init();
        ConnectionHandler.getInstance().Listen(5757);
    }


    public static void main(String[] args) {

        // Load UI
        //launch(args);

        ObservableUser u = new ObservableUser(1, "Giocatore", "RED");
        Gson gson = new Gson();
        String jsonU = gson.toJson(u);
        System.out.println(jsonU);

        Player p = gson.fromJson(jsonU, Player.class);
    }

    @Override
    public void stop() throws Exception {
        ConnectionHandler.getInstance().terminate();
        GameController.getInstance().terminate();
        System.out.println("Shutdown completed");

        Thread.sleep(3000);

        super.stop();
    }
}
