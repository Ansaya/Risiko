package Gioco;

import java.net.Socket;

/**
 * Object representing a connected user
 */
public class User implements Runnable {

    private int id;

    private String name;

    public String getName() { return name;}

    private Socket connection;

    private Thread _instance;

    private static int counter = 0;

    public User(Socket Connection) {
        this.id = counter++;
        this.connection = Connection;

        this._instance = new Thread(this);
        _instance.start();
    }

    @Override
    public void run() {
        // First user message contains username
        // Set username

        while (true) {

        }
    }
}
