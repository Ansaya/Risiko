package Server.Game;

import Server.Logger;

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

    private volatile boolean listen = false;

    private ServerSocket server;

    private final AtomicInteger playerCounter = new AtomicInteger(0);

    private final Consumer<Socket> welcomeAction;

    private volatile Thread reception;

    public ConnectionHandler(GameController GC) {
        welcomeAction = newConn -> {
            final String username;
            final int id;
            final BufferedReader receive;
            final PrintWriter send;
            try {
                username = (receive = new BufferedReader(new InputStreamReader(newConn.getInputStream()))).readLine();

                // If username is empty throw error and exit
                if(username.equals("")){
                    Logger.err("Connection handler: Username can not be null.");
                    (new PrintWriter(newConn.getOutputStream(), true)).println("Username not valid");
                    return;
                }

                // If username is valid confirm login To client and go ahead
                id = playerCounter.getAndIncrement();
                (send = new PrintWriter(newConn.getOutputStream(), true)).println("OK#" + id);
            } catch (Exception e) {
                Logger.err("Connection handler: Error during username request.");
                e.printStackTrace();
                return;
            }

            Logger.log("Connection handler: New user connected.");
            GC.addPlayer(new Player(id, username, newConn, receive, send, GC));
            Logger.log("Connection handler: User passed to game controller.");
        };
    }

    public boolean isListening() { return listen; }

    /**
     * Start listening for new users on specified port
     *
     * @param port Listening port
     */
    public void listen(int port) {
        if(listen)
            terminate();

        try {
            this.server = new ServerSocket(port);
        }
        catch (IOException e){
            Logger.err("Connection handler: Cannot connect");
            return;
        }

        reception = new Thread(this, "ConnectionHandler-Reception");

        listen = true;
        reception.start();
    }

    /**
     * Stop listening
     */
    public void terminate() {
        if(!listen) return;

        listen = false;

        try {
            server.close();
            reception.join();
        } catch (IOException | InterruptedException e) {
            Logger.err("Connection handler: Exception during termination.");
            e.printStackTrace();
            return;
        }

        Logger.log("Connection handler: Terminated.");
    }

    @Override
    public void run() {
        while (listen) {
            try {
                Logger.log("Connection handler: Waiting for users...");
                final Socket newConn = server.accept();

                Thread welcome = new Thread(() -> welcomeAction.accept(newConn), "ConnectionHandler-Welcomer");
                welcome.setDaemon(true);
                welcome.start();

            } catch (Exception e) {
                if(!listen && e instanceof IOException)
                    break;
                else {
                    Logger.err("Connection handler: Bad connection handling");
                    e.printStackTrace();
                }
            }
        }
    }
}
