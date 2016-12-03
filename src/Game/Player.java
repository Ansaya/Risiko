package Game;

import Game.Connection.MessageType;
import Game.Map.Mission;
import Game.Map.Territory;
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
    private volatile int id;

    public int getId() { return this.id; }

    /**
     * Username choose from
     */
    private volatile String name;

    public String getName() { return this.name;}

    /**
     * Id of match the player is inside
     */
    private final AtomicInteger matchId = new AtomicInteger(-1);

    public int getMatchId() { return this.matchId.get(); }

    /**
     * Current player's isPlaying. True if playing, false if attending the match.
     */
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);

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
    private Thread _instance;

    public Player(int Id, String Username, Socket Connection) {
        super(Connection);

        this.id = Id;
        this.name = Username;
        this.listen = true;
        this._instance = new Thread(this);
        this._instance.start();
    }

    /**
     * Instance an AI static player
     *
     * @param MatchId Match where the AI player is needed
     */
    public Player(int MatchId) {
        super(null);

        this.id = -1;
        this.name = "Computer AI";
        this.matchId .set(MatchId);
        this.isPlaying.set(true);
        this.color = Color.values()[2];
    }

    @Override
    public void run() {
        String incoming = "";

        // Handle all incoming messages
        while (listen) {
            try {
                while ((incoming = receive.readLine()) != null) {

                    // Connection closing requested from client
                    if(incoming.equals("End")){
                        Platform.runLater(() -> closeConnection(false));
                        return;
                    }

                    System.out.println("Player " + this.getId() + ": " + incoming);

                    if(incoming != "") {
                        String[] info = incoming.split("[-]");

                        if (matchId.get() == -1) {
                            GameController.getInstance().setIncoming(id, MessageType.valueOf(info[0]), info[1]);
                        } else {
                            GameController.getInstance().getMatch(matchId.get()).setIncoming(id, MessageType.valueOf(info[0]), info[1]);
                        }
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
            if (matchId.get() != 0)
                GameController.getInstance().getMatch(matchId.get()).setIncoming(this.id, MessageType.GameState, StateType.Abandoned.toString());
            else
                GameController.getInstance().setIncoming(this.id, MessageType.GameState, StateType.Abandoned.toString());
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
    protected void initMatch(Color Color, int MatchId) {
        this.matchId.set(MatchId);
        this.color = Color;

        // Player is actively playing
        this.isPlaying.set(true);
    }

    /**
     * Reset match fields and bring player back to lobby
     */
    protected synchronized void exitMatch() {
        matchId.set(0);
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
        String packet = Type.toString() + "-" + serialize.toJson(MessageObj);

        synchronized (send) {
            send.println(packet);
        }

        System.out.println("Message sent to " + this.name + ": " + packet);
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

        System.out.println("Message routed to " + this.name + ": " + packet);
    }
}