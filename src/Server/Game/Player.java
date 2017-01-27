package Server.Game;

import Game.Map.Army.Color;
import Game.Map.Mission;
import Game.Connection.GameState;
import Game.SocketHandler;
import Game.StateType;
import Server.Game.Connection.MessageType;
import Server.Game.Connection.Serializer.MatchSerializer;
import Server.Game.Map.Territory;
import Server.Logger;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Player relative to a match
 */
public class Player extends SocketHandler<MessageType> implements Game.Player {

    /**
     * Player's unique id
     */
    private final int id;

    @Override
    public int getId() {
        return id;
    }

    /**
     * Username choose from
     */
    private final String username;

    @Override
    public String getUsername() { return username; }

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

    @Override
    public Color getColor() { return color; }

    /**
     * Dominated territories
     */
    private transient volatile ArrayList<Territory> territories = new ArrayList<>();

    public ArrayList<Territory> getTerritories() { return territories; }

    /**
     * Player's Mission
     */
    private transient volatile Mission mission;

    public Mission getMission() { return mission; }

    private final transient GameController GC;

    public Player(int Id, String Username, Socket Connection, BufferedReader Receive, PrintWriter Send, GameController GC) {
        super(Connection,
                Receive,
                Send,
                new GsonBuilder().registerTypeAdapter(Match.class, new MatchSerializer()).create(),
                "Player-" + Id);

        this.id = Id;
        this.username = Username;
        this.GC = GC;
    }

    /**
     * Private constructor for AI player
     */
    private Player(){
        id = -1;
        username = "Computer AI";
        GC = null;
    }

    /**
     * Instance an AI static player
     */
    public static Player getAI() {
        return new Player();
    }

    @Override
    public void run() {
        String incoming;

        // Handle all incoming messages
        while (_listen) {
            try {
                while ((incoming = _receive.readLine()) != null) {

                    // Connection closing requested From client
                    if(incoming.equals("End")){
                        Platform.runLater(() -> closeConnection(false));
                        return;
                    }

                    String[] info = incoming.split("[#]", 2);

                    if (matchId.get() == -1) {
                        GC.setIncoming(id, MessageType.valueOf(info[0]), info[1]);
                    } else {
                        GC.getMatch(matchId.get()).setIncoming(id, MessageType.valueOf(info[0]), info[1]);
                    }
                }
            }catch (Exception e){
                if(e instanceof IOException) {
                    if(_listen) Logger.err("Player-" + id + ": Client connection lost.");
                    break;
                }

                Logger.err("Player-" + id + ": Message not recognized.");
                e.printStackTrace();
            }
        }

        if(_listen)
            Platform.runLater(() -> closeConnection(false));
    }

    /**
     * Gently close connection with the client
     */
    synchronized void closeConnection(boolean fromServer) {
        Logger.log("Player-" + id + ": Connection closed");
        _send.println("End");

        if(!fromServer) {
            final Match match = matchId.get() != -1 ? GC.getMatch(matchId.get()) : null;

            if(match != null)
                match.setIncoming(id, MessageType.GameState,
                        _gson.toJson(new GameState<Player>(StateType.Abandoned, null), MessageType.GameState.getType()));
            else
                GC.releasePlayer(this, true);
        }

        super.closeConnection();
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
     * Add player to match
     *
     * @param MatchId Match id the player is watching
     */
    synchronized void enterMatch(int MatchId) {
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

    @Override
    public boolean equals(Object other) {
        if(other instanceof Color)
            return other == this.color;

        return other instanceof Player && this.id == ((Player) other).id;
    }
}