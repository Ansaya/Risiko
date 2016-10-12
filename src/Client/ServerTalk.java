package Client;

import java.io.IOException;
import java.net.Socket;

/**
 * Handle communication with the server
 */
public class ServerTalk implements Runnable {

    private static ServerTalk _instance = new ServerTalk();

    public static ServerTalk getInstance() { return _instance; }

    private Socket connection;

    private Thread _threadInstance;


    private ServerTalk() {

    }

    /**
     * Setup and start connection with the server
     *
     * @param serverAddress Address of the server
     * @param port Port to use
     */
    public void InitConnection(String serverAddress, int port) {
        try {
            connection = new Socket(serverAddress, port);
        } catch (IOException e) {
            System.out.println("Cannot connect with the server");
        }

        _threadInstance = new Thread(this);
        _threadInstance.start();
    }

    /**
     * Stops current connection with the server
     */
    public void StopConnection() {
        // Implement connection interruption
    }

    @Override
    public void run() {
        // Implement communication with server
    }
}
