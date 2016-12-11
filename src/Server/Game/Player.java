package Server.Game;

import Game.Color;
import Game.Connection.GameState;
import Game.StateType;
import Server.Game.Connection.MessageType;
import Game.Map.Mission;
import Server.Game.Map.Territory;
import com.google.gson.Gson;
import javafx.application.Platform;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Player relative to a match
 */
public class Player extends SocketHandler implements Runnable {

    /**
     * Player's unique id
     */
    public final int id;

    /**
     * Username choose from
     */
    public final String username;

    /**
     * Id of match the player is inside
     */
    private transient final AtomicInteger matchId = new AtomicInteger(-1);

    public int getMatchId() { return this.matchId.get(); }

    /**
     * Current player's isPlaying. True if playing, false if attending the match.
     */
    private transient final AtomicBoolean isPlaying = new AtomicBoolean(false);

    public boolean isPlaying() { return this.isPlaying.get(); }

    /**
     * Color assigned for the match
     */
    private volatile Color color;

    public Color getColor() { return color; }

    /**
     * Dominated territories
     */
    private transient volatile ArrayList<Territory> territories = new ArrayList<>();

    public ArrayList<Territory> getTerritories() { return territories; }

    /**
     * Player's mission
     */
    private transient volatile Mission mission;

    public Mission getMission() { return mission; }

    /**
     * Incoming messages handler
     */
    private transient final Thread _instance = new Thread(this);

    public Player(int Id, String Username, Socket Connection) {
        super(Connection);

        this.id = Id;
        this.username = Username;
        this.listen = true;

        _instance.setName("Player" + this.id);
        _instance.start();
    }

    /**
     * Private constructor for AI player
     *
     * @param MatchId Match where the AI is required
     * @param Color Color for the AI
     */
    private Player(int MatchId, Color Color){
        id = -1;
        username = "Computer AI";
        matchId.set(MatchId);
        color = Color;
        isPlaying.set(true);
    }

    /**
     * Instance an AI static player
     *
     * @param MatchId Match where the AI player is needed
     * @param Color Color of AI on map
     */
    static Player getAI(int MatchId, Color Color) {
        return new Player(MatchId, Color);
    }

    @Override
    public void run() {
        String incoming;

        // Handle all incoming messages
        while (listen) {
            try {
                while ((incoming = receive.readLine()) != null) {

                    // Connection closing requested from client
                    if(incoming.equals("End")){
                        Platform.runLater(() -> closeConnection(false));
                        return;
                    }

                    System.out.println("Player " + this.id + " from thread " + Thread.currentThread().getId() + ": " + incoming);

                    String[] info = incoming.split("[#]");

                    if (matchId.get() == -1) {
                        GameController.getInstance().setIncoming(id, MessageType.valueOf(info[0]), info[1]);
                    } else {
                        GameController.getInstance().getMatch(matchId.get()).setIncoming(id, MessageType.valueOf(info[0]), info[1]);
                    }
                }
            }catch (Exception e){
                // Handle loss of connection
            }
        }

        receive = null;
        send = null;
    }

    /**
     * Gently close connection with the client
     */
    protected void closeConnection(boolean fromServer) {
        send.println("End");

        if(!fromServer) {
            if (matchId.get() != -1)
                GameController.getInstance().getMatch(matchId.get())
                        .setIncoming(this.id,
                                MessageType.GameState,
                                (new Gson()).toJson(new GameState<Player>(StateType.Abandoned, null), MessageType.GameState.getType()));
            else
                GameController.getInstance().releasePlayer(this.id);
        }

        try {
            this.listen = false;
            this.connection.close();
            this._instance.join();
            this.connection = null;
        } catch (Exception e) {}
    }

    /**
     * Setup player to participate a match
     *
     * @param Color Player color in the match
     * @param MatchId Match this player is participating
     */
    synchronized void initMatch(Color Color, int MatchId, Mission Mission) {
        matchId.set(MatchId);
        color = Color;
        mission = Mission;

        // Player is actively playing
        isPlaying.set(true);
    }

    /**
     * Setup player to witness a running match
     *
     * @param MatchId Match id the player is watching
     */
    synchronized void witnessMatch(int MatchId) {
        matchId.set(MatchId);
        isPlaying.set(false);
    }

    /**
     * Reset match fields and bring player back to lobby
     */
    synchronized void exitMatch() {
        matchId.set(-1);
        isPlaying.set(false);
        color = null;
        territories = new ArrayList<>();
        mission = null;
    }

    /**
     * Send a message to the client
     *
     * @param Type Type of message
     * @param MessageObj Object of specified type
     */
    protected void SendMessage(MessageType Type, Object MessageObj) {
        Gson serialize = new Gson();

        // Build packet string as MessageType-SerializedObject
        String packet = Type.toString() + "#" + serialize.toJson(MessageObj, Type.getType());

        synchronized (send) {
            send.println(packet);
        }

        System.out.println("Message sent to " + this.username + ": " + packet);
    }

    /**
     * Send passed string directly
     *
     * @param packet String to send to the client
     */
    protected void RouteMessage(String packet) {
        synchronized (send) {
            send.println(packet);
        }

        System.out.println("Message routed to " + this.username + ": " + packet);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;

        if(o.getClass() != Player.class)
            return false;

        return this.id == ((Player)o).id;
    }
}