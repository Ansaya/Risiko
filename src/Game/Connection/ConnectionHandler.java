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
    private ServerSocket server;

    private Thread _instance;

    public ConnectionHandler(int port) {
        try {
            this.server = new ServerSocket(port);
        }
        catch (IOException e){
            System.out.println("Cannot connect");
        }
    }

    public void Listen() {
        if(_instance != null){
            try {
                server.close();
                _instance.join();
                _instance = null;
            } catch (Exception e) {}
        }
        _instance = new Thread(this);
        _instance.start();
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
