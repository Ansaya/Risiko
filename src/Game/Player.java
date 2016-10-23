package Game;

import Game.Connection.GameState;
import Game.Connection.MessageDispatcher;
import Game.Connection.MessageType;
import Game.Map.Mission;
import Game.Map.Territory;
import com.google.gson.Gson;
import javafx.application.Platform;

import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Player relative to a match
 */
public class Player extends SocketHandler implements Runnable {

    /**
     * Player's unique id
     */
    private int id;

    public int getId() { return this.id; }

    /**
     * Username choose from
     */
    private String name;

    public String getName() { return this.name;}

    /**
     * Id of match the player is inside
     */
    private int matchId = 0;

    public int getMathcId() { return this.matchId; }

    /**
     * Current player's isPlaying. True if playing, false if attending the match.
     */
    private boolean isPlaying = false;

    public boolean isPlaying() { return this.isPlaying; }

    /**
     * Color assigned for the match
     */
    private Color color;

    public Color getColor() { return color; }

    /**
     * Dominated territories
     */
    transient private ArrayList<Territory> territories = new ArrayList<>();

    public ArrayList<Territory> getTerritories() { return territories; }

    /**
     * Player's mission
     */
    transient private Mission mission;

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

                    // If message isn't empty route it to message dispatcher
                    if(!incoming.equals(""))
                        MessageDispatcher.getInstance().setIncoming(this.matchId + "-" + this.id + "-" + incoming);
                }
            }catch (Exception e){}
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
            if (matchId != 0)
                GameController.getInstance().getMatch(matchId).setIncoming(this.id, MessageType.GameState, StateType.Abandoned.toString());
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
        this.matchId = MatchId;
        this.color = Color;

        // Player is actively playing
        this.isPlaying = true;
    }

    /**
     * Reset match fields and bring player back to lobby
     */
    protected void exitMatch() {
        matchId = 0;
        isPlaying = false;
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
        if(!connection.isConnected()) {
            closeConnection(false);
            System.out.println("Player " + name + " got connection error.");
        }

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
        if(!connection.isConnected()) {
            closeConnection(false);
            System.out.println("Player " + name + " got connection error.");
        }

        synchronized (send) {
            send.println(packet);
        }

        System.out.println("Message routed to " + this.name + ": " + packet);
    }
}