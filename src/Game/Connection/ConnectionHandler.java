package Game.Connection;

import Game.GameController;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    private Integer playerCounter = 0;

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
            System.out.println("Connection handler: Cannot connect");
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

        System.out.println("Connection handler: Terminated.");
    }

    @Override
    public void run() {
        while (listen) {
            try {
                System.out.println("Connection handler: Waiting for users...");
                Socket newConn = server.accept();

                Thread welcome = new Thread(() -> {
                    int id;
                    synchronized (playerCounter) {
                        id = playerCounter++;
                    }

                    String username = "";

                    try {
                        username = (new BufferedReader(new InputStreamReader(newConn.getInputStream()))).readLine();
                        (new PrintWriter(newConn.getOutputStream(), true)).println("OK-" + id);
                    } catch (Exception e) {}

                    System.out.println("Connection handler: New user connected.");
                    GameController.getInstance().addPlayer(id, username, newConn);
                    System.out.println("Connection handler: User passed to game controller.");
                });
                welcome.start();

            } catch (IOException e) {}
        }
    }
}
