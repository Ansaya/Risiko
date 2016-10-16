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

    private ServerSocket server;

    private Thread _threadInstance;

    private ConnectionHandler() {}

    public void Listen(int Port) {
        if(_threadInstance != null){
            try {
                server.close();
                _threadInstance.join();
                _threadInstance = null;
            } catch (Exception e) {}
        }

        try {
            this.server = new ServerSocket(Port);
        }
        catch (IOException e){
            System.out.println("Cannot connect");
        }

        _threadInstance = new Thread(this);
        _threadInstance.start();
    }

    public void terminate() {
        try {
            server.close();
            _threadInstance.join();
        } catch (Exception e) {}
    }

    @Override
    public void run() {
        while (true) {
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
