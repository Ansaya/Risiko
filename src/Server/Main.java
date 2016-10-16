package Server;

import Game.Connection.Chat;
import Game.Connection.MessageType;
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

        //GameController gc = GameController.getInstance();
        //ConnectionHandler ch = new ConnectionHandler(5757);
        //ch.Listen();

        Chat message = new Chat("Ansaya", "Test message");
        System.out.println(message.getSender());
        System.out.println(message.getMessage());

        Gson gson = new Gson();
        String json = gson.toJson(message);
        String toSend = MessageType.Chat.toString() + "-2-28-" + json;
        System.out.println(json);

        String[] parts = toSend.split("[-]");
        System.out.println("Message type: " + parts[0]);
        System.out.println("User id: " + parts[1]);
        System.out.println("Match id: " + parts[2]);

        Chat recevied = gson.fromJson(parts[3], Chat.class);
        System.out.println("Sender: " + recevied.getSender());
        System.out.println("Message: " + recevied.getMessage());

        launch(args);
    }
}
