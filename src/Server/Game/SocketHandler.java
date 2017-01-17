package Server.Game;

import Server.Game.Connection.MessageType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Abstract handling for socket connection
 */
public abstract class SocketHandler implements Runnable {

    /**
     * Socket connection object
     */
    protected final transient Socket connection;

    /**
     * Receiving buffer for socket messages
     */
    final transient BufferedReader receive;

    /**
     * Transmitting writer for socket messages
     */
    final transient PrintWriter send;

    /**
     * Serializer/deserializer for socket messages
     */
    final transient Gson gson;

    private final transient String name;

    /**
     * To be used in run() method
     */
    transient volatile boolean listen = false;

    /**
     * Incoming messages handler
     */
    private transient final Thread _instance;

    /**
     * Initializes receiver and sender for passed socket
     *
     * @param Connection Connected socket
     */
    SocketHandler(Socket Connection, String Name) {
        this.connection = Connection;
        this.name = Name;

        BufferedReader br = null;
        PrintWriter pw = null;

        try {
            br = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
            pw = new PrintWriter(this.connection.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.receive = br;
        this.send = pw;

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Match.class, new GameController.MatchSerializer());
        gson = builder.create();

        this.listen = true;
        _instance = new Thread(this);
        if(Name != null)
            _instance.setName(name);
        _instance.start();
    }

    SocketHandler() {
        connection = null;
        name = null;
        receive = null;
        send = null;
        gson = null;
        _instance = null;
    }

    /**
     * Stop listener thread and close connection
     */
    protected void closeConnection() {
        try {
            this.listen = false;
            connection.close();
            _instance.join();
        } catch (IOException | InterruptedException e) {
            System.err.println(name + ": Exception during socket handler join.");
            e.printStackTrace();
        }
    }

    @Override
    public abstract void run();

    /**
     * Send a message to the client
     *
     * @param Type Type of message
     * @param MessageObj Object of specified type
     */
    protected void SendMessage(MessageType Type, Object MessageObj) {
        if(gson == null)
            return;

        // Build packet string as MessageType#SerializedObject
        RouteMessage(Type.toString() + "#" + gson.toJson(MessageObj, Type.getType()));
    }

    /**
     * Send passed string directly
     *
     * @param packet String to send to the client
     */
    protected void RouteMessage(String packet) {
        if(send == null)
            return;

        synchronized (send) {
            send.println(packet);
        }

        System.out.println(name + ": Sent -> " + packet);
    }
}
