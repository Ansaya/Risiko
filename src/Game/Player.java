package Game;

import Game.Connection.MessageDispatcher;
import Game.Connection.MessageType;
import Game.Map.Mission;
import Game.Map.Territory;
import com.google.gson.Gson;

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
    private ArrayList<Territory> territories = new ArrayList<>();

    public ArrayList<Territory> getTerritories() { return territories; }

    /**
     * Player's mission
     */
    private Mission mission;

    public Mission getMission() { return mission; }

    /**
     * Incoming messages handler
     */
    private Thread _instance;

    private static int counter = 0;

    public Player(Socket Connection) {
        super(Connection);

        this.id = counter++;

        this.listen = true;
        this._instance = new Thread(this);
        this._instance.start();
    }

    @Override
    public void run() {
        String incoming = "";

        // First message contains username only
        try {
            incoming = receive.readLine();

            // Set username
            this.name = incoming;

            // Send connection accepted confirmation
            send.println("OK");
        } catch (IOException e) {
            System.out.println("Exception for player " + this.id);
            e.printStackTrace();
            return;
        }

        // Handle all incoming messages
        while (listen) {
            try {
                while ((incoming = receive.readLine()) != null) {

                    // If message isn't empty route it to message dispatcher
                    if(!incoming.equals(""))
                        MessageDispatcher.getInstance().setIncoming(this.matchId + "-" + this.id + "-" + incoming);
                }
            }catch (Exception e){}
        }

        receive = null;
        send = null;
        connection = null;
    }

    /**
     * Gently close connection with the client
     */
    protected void closeConnection() {
        send.println("End");

        try {
            this.listen = false;
            this.connection.close();
            this._instance.join();
        } catch (Exception e) {}
    }

    /**
     * Setup player to participate a match
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
     * Send a message to the client
     *
     * @param Type Type of message
     * @param MessageObj Object of specified type
     */
    protected void SendMessage(MessageType Type, Object MessageObj) {
        Gson serialize = new Gson();

        // Build packet string as MessageType-SerializedObject
        String packet = Type.toString() + "-" + serialize.toJson(MessageObj);

        send.println(packet);
    }

    /**
     * Send passed string directly
     *
     * @param packet String to send to the client
     */
    protected void RouteMessage(String packet) {
        send.println(packet);
    }
}