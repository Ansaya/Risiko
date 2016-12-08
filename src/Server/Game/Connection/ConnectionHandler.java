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
        String username = "";
        int id = 0;
        try {
            username = (new BufferedReader(new InputStreamReader(newConn.getInputStream()))).readLine();

            // If username is empty throw error and exit
            if(username.equals("")){
                System.err.println("Connection handler: Username can not be null.");
                (new PrintWriter(newConn.getOutputStream(), true)).println("Username not valid");
                synchronized (welcomers) {
                    welcomers.notify();
                }
                return;
            }

            // If username is valid confirm login to client and go ahead
            id = playerCounter.getAndIncrement();
            (new PrintWriter(newConn.getOutputStream(), true)).println("OK#" + id);
        } catch (Exception e) {
            System.err.println("Connection handler: Error during username request.");
        }

        System.out.println("Connection handler: New user connected.");
        GameController.getInstance().addPlayer(id, username, newConn);
        System.out.println("Connection handler: User passed to game controller.");
        synchronized (welcomers) {
            welcomers.notify();
        }
    };

    private final Thread reception= new Thread(this, "ConnectionHandler-Reception");

    private final Thread joiner= new Thread(() -> {
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
    }, "ConnectionHandler-Joiner");

    private ConnectionHandler() {}

    public synchronized void Listen(int Port) {
        if(reception.isAlive()){
            terminate();
        }

        try {
            this.server = new ServerSocket(Port);
        }
        catch (IOException e){
            System.err.println("Connection handler: Cannot connect");
        }

        listen = true;
        joiner.start();
        reception.start();
    }

    public synchronized void terminate() {
        try {
            listen = false;
            server.close();
            reception.join();
            while (!welcomers.isEmpty()) {}
            welcomers.notify();
            joiner.join();
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
                welcomers.add(welcome);
                welcome.start();

            } catch (IOException e) {}
        }
    }
}
