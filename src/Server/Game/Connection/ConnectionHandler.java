package Server.Game.Connection;

import Server.Game.GameController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Incoming connections handler
 */
public class ConnectionHandler implements Runnable {

    private static ConnectionHandler _instance = new ConnectionHandler();

    public static ConnectionHandler getInstance() { return _instance; }

    private volatile boolean listen = false;

    private ServerSocket server;

    private final AtomicInteger playerCounter = new AtomicInteger(0);

    private final Consumer<Socket> welcomeAction = newConn -> {
        String username = "";
        int id = 0;
        try {
            username = (new BufferedReader(new InputStreamReader(newConn.getInputStream()))).readLine();

            // If username is empty throw error and exit
            if(username.equals("")){
                System.err.println("Connection handler: Username can not be null.");
                (new PrintWriter(newConn.getOutputStream(), true)).println("Username not valid");
                return;
            }

            // If username is valid confirm login To client and go ahead
            id = playerCounter.getAndIncrement();
            (new PrintWriter(newConn.getOutputStream(), true)).println("OK#" + id);
        } catch (Exception e) {
            System.err.println("Connection handler: Error during username request.");
        }

        System.out.println("Connection handler: New user connected.");
        GameController.getInstance().addPlayer(id, username, newConn);
        System.out.println("Connection handler: User passed To game controller.");
    };

    private final Thread reception= new Thread(this, "ConnectionHandler-Reception");

    private ConnectionHandler() {}

    public void Listen(int port) {
        if(listen)
            terminate();

        try {
            this.server = new ServerSocket(port);
        }
        catch (IOException e){
            System.err.println("Connection handler: Cannot connect");
            return;
        }

        listen = true;
        reception.start();
    }

    public void terminate() {
        listen = false;

        try {
            server.close();
            reception.join();
        } catch (Exception e) {}

        System.out.println("Connection handler: Terminated.");
    }

    @Override
    public void run() {
        while (listen) {
            try {
                System.out.println("Connection handler: Waiting for users...");
                final Socket newConn = server.accept();

                Thread welcome = new Thread(() -> welcomeAction.accept(newConn), "ConnectionHandler-Welcomer");
                welcome.setDaemon(true);
                welcome.start();

            } catch (Exception e) {
                if(!listen)
                    break;
            }
        }
    }
}
