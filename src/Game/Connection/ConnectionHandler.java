package Game.Connection;

import Game.GameController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Incoming connections handler
 */
public class ConnectionHandler implements Runnable {

    private static ConnectionHandler _instance = new ConnectionHandler();

    public static ConnectionHandler getInstance() { return _instance; }

    private boolean listen = false;

    private ServerSocket server;

    private Thread _threadInstance;

    private ConnectionHandler() {}

    public void Listen(int Port) {
        if(_threadInstance != null){
            terminate();
        }

        try {
            this.server = new ServerSocket(Port);
        }
        catch (IOException e){
            System.out.println("Cannot connect");
        }

        listen = true;
        _threadInstance = new Thread(this);
        _threadInstance.start();
    }

    public void terminate() {
        try {
            listen = false;
            server.close();
            _threadInstance.join();
            _threadInstance = null;
        } catch (Exception e) {}
    }

    @Override
    public void run() {
        while (listen) {
            try {
                System.out.println("Waiting for users...");
                Socket newConn = server.accept();
                System.out.println("New user connected.");
                GameController.getInstance().addPlayer(newConn);
                System.out.println("User passed to game controller.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
