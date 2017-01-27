package Game;

import Game.Connection.TypeEnumerator;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Abstract handling for socket connection
 */
public abstract class SocketHandler<T extends TypeEnumerator> implements Runnable {

    /**
     * Socket connection object
     */
    public final transient Socket _connection;

    /**
     * Receiving buffer for socket messages
     */
    public final transient BufferedReader _receive;

    /**
     * Transmitting writer for socket messages
     */
    public final transient PrintWriter _send;

    /**
     * Serializer/deserializer for socket messages
     */
    public final transient Gson _gson;

    private final transient String name;

    /**
     * To be used in run() method
     */
    public transient volatile boolean _listen = false;

    /**
     * Incoming messages handler
     */
    private transient final Thread _instance;

    /**
     * Initializes receiver and sender for passed socket
     *
     * @param Connection Connected socket
     */
    public SocketHandler(Socket Connection, BufferedReader Receive, PrintWriter Send, Gson Gson, String Name) {
        this._connection = Connection;
        this.name = Name;

        this._receive = Receive;
        this._send = Send;

        if(Gson != null)
            _gson = Gson;
        else
            _gson = new Gson();

        this._listen = true;
        _instance = new Thread(this);
        if(Name != null)
            _instance.setName(name);
        _instance.start();
    }

    public SocketHandler() {
        _connection = null;
        name = null;
        _receive = null;
        _send = null;
        _gson = null;
        _instance = null;
    }

    /**
     * Stop listener thread and close connection
     */
    public void closeConnection() {
        if(!_listen) return;

        this._listen = false;

        try {
            _connection.close();
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
    public void SendMessage(T Type, Object MessageObj) {
        if(_gson == null)
            return;

        // Build packet string as MessageType#SerializedObject
        RouteMessage(Type.toString() + "#" + _gson.toJson(MessageObj, Type.getType()));
    }

    /**
     * Send passed string directly
     *
     * @param packet String to send to the client
     */
    public void RouteMessage(String packet) {
        if(_send == null)
            return;

        synchronized (_send) {
            _send.println(packet);
        }
    }
}
