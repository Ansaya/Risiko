package Server.Game.Connection;

import Server.Game.GameController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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

    private final ArrayList<Thread> welcomers = new ArrayList<>();

    private final Consumer<Socket> welcomeAction = newConn -> {
        int id = playerCounter.getAndIncrement();

        String username = "";

        try {
            username = (new BufferedReader(new InputStreamReader(newConn.getInputStream()))).readLine();
            (new PrintWriter(newConn.getOutputStream(), true)).println("OK#" + id);
        } catch (Exception e) {}

        System.out.println("Connection handler: New user connected.");
        GameController.getInstance().addPlayer(id, username, newConn);
        System.out.println("Connection handler: User passed to game controller.");
    };

    private Thread reception;

    private Thread joiner;

    private ConnectionHandler() {}

    public synchronized void Listen(int Port) {
        if(reception != null){
            terminate();
        }

        try {
            this.server = new ServerSocket(Port);
        }
        catch (IOException e){
            System.out.println("Connection handler: Cannot connect");
        }

        listen = true;

        joiner = new Thread(() -> {
            while (listen){
                try{
                    if(welcomers.isEmpty())
                        synchronized (welcomers) {
                            welcomers.wait();
                        }

                    welcomers.get(0).join();
                    welcomers.remove(0);
                } catch (Exception e) {}
            }
        });
        joiner.start();
        reception = new Thread(this);
        reception.start();
    }

    public synchronized void terminate() {
        try {
            listen = false;
            server.close();
            reception.join();
            reception = null;
            welcomers.notify();
            joiner.join();
            joiner = null;
        } catch (Exception e) {}

        System.out.println("Connection handler: Terminated.");
    }

    @Override
    public void run() {
        while (listen) {
            try {
                System.out.println("Connection handler: Waiting for users...");
                final Socket newConn = server.accept();

                Thread welcome = new Thread(() -> welcomeAction.accept(newConn));
                welcomers.add(welcome);
                welcome.start();

            } catch (IOException e) {}
        }
    }
}
