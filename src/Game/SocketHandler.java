package Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Abstract handling for socket connection
 */
public abstract class SocketHandler {

    protected transient Socket connection;

    protected transient BufferedReader receive;

    protected transient PrintWriter send;

    protected transient volatile boolean listen;

    /**
     * Initializes receiver and sender for passed socket
     *
     * @param Connection Connected socket
     */
    public SocketHandler(Socket Connection) {
        if(Connection == null)
            return;

        this.connection = Connection;

        try {
            this.receive = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
            this.send = new PrintWriter(this.connection.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
