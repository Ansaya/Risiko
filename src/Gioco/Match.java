package Gioco;

import java.util.ArrayList;

/**
 * Match object to manage game turns in a dedicated thread
 */
public class Match implements Runnable {

    /**
     * Match id
     */
    private int id;

    /**
     * Get match id
     *
     * @return Match id
     */
    public int getId() { return id; }

    /**
     * Players' list for this match
     */
    private ArrayList<Player> players = new ArrayList<>();

    /**
     * Get match's players
     *
     * @return List of players
     */
    public ArrayList<Player> getPlayers() { return players; }

    /**
     * Match's thread
     */
    private Thread _instance;

    /**
     * Global matches counter
     */
    private static int counter = 0;

    public Match(User... Users) {
        if(Users.length < 2 || Users.length > 6)
            throw new UnsupportedOperationException(String.format("Not possible to start playing with %d users.", Users.length));

        this.id = counter++;

        Color.reset();

        for (User u: Users
             ) {
            players.add(new Player(u, Color.next(), this.id));
        }

        this._instance = new Thread(this);
        _instance.start();
    }

    @Override
    public void run() {

    }
}
