package Server.Game;

import com.google.gson.Gson;
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
    final transient Gson gson = new Gson();

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

        this.listen = true;
        _instance = new Thread(this);
        if(Name != null)
            _instance.setName(Name);
        _instance.start();
    }

    SocketHandler() {
        connection = null;
        receive = null;
        send = null;
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
        } catch (Exception e) {}
    }

    @Override
    public abstract void run();
}
