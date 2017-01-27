package Server.Game;

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
                    System.err.println("Connection handler: Username can not be null.");
                    (new PrintWriter(newConn.getOutputStream(), true)).println("Username not valid");
                    return;
                }

                // If username is valid confirm login To client and go ahead
                id = playerCounter.getAndIncrement();
                (send = new PrintWriter(newConn.getOutputStream(), true)).println("OK#" + id);
            } catch (Exception e) {
                System.err.println("Connection handler: Error during username request.");
                e.printStackTrace();
                return;
            }

            System.out.println("Connection handler: New user connected.");
            GC.addPlayer(new Player(id, username, newConn, receive, send, GC));
            System.out.println("Connection handler: User passed to game controller.");
        };
    }

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
            System.err.println("Connection handler: Cannot connect");
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
            System.err.println("Connection handler: Exception during termination.");
            e.printStackTrace();
            return;
        }

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
                if(!listen && e instanceof IOException)
                    break;
                else {
                    System.err.println("Connection handler: Bad connection handling");
                    e.printStackTrace();
                }
            }
        }
    }
}
