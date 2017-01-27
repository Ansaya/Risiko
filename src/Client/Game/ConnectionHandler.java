package Client.Game;

import Client.Game.Connection.MessageType;
import Client.Game.Connection.Serializer.ObservableUserSerializer;
import Client.Game.Connection.Serializer.SimpleObjectPropertySerializer;
import Game.Connection.Serializer.IntegerPropertySerializer;
import Game.Connection.Serializer.StringPropertySerializer;
import Game.Logger;
import Game.SocketHandler;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handler for tcp socket connection
 */
public class ConnectionHandler extends SocketHandler<MessageType> {

    private final GameController GC;

    public ConnectionHandler(GameController GC, Socket Connection, BufferedReader Receive, PrintWriter Send, String Name) {
        super(Connection,
                Receive,
                Send,
                new GsonBuilder()
                        .registerTypeAdapter(IntegerProperty.class, new IntegerPropertySerializer())
                        .registerTypeAdapter(StringProperty.class, new StringPropertySerializer())
                        .registerTypeAdapter(Player.class, new ObservableUserSerializer())
                        .registerTypeAdapter(new TypeToken<SimpleObjectProperty<Player>>(){}.getType(),
                                new SimpleObjectPropertySerializer(Player.class)).create(),
                Name);

        this.GC = GC;
    }

    @Override
    public void run() {
        // Incoming message buffer
        String packet;

        // Listen to the server until necessary
        while (_listen) {
            try {
                while ((packet = _receive.readLine()) != null){
                    if(packet.equals("End")){
                        Platform.runLater(() -> GC.stopConnection(false));
                        return;
                    }

                    String[] info = packet.split("[#]", 2);

                    GC.setIncoming(0, MessageType.valueOf(info[0]), info[1]);
                }

            }catch (Exception e) {
                if(e instanceof IOException) {
                    if(_listen) Logger.err("GamerController: Server connection lost.", e);
                    break;
                }

                Logger.err("GameController: Message not recognized.", e);
            }
        }

        if(_listen)
            Platform.runLater(() -> GC.stopConnection(false));
    }
}
