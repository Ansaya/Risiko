package Server.Game;

import Game.Map.Army.Color;
import Game.Map.Mission;
import Game.Connection.GameState;
import Game.StateType;
import Server.Game.Connection.MessageType;
import Server.Game.Map.Territory;
import javafx.application.Platform;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Player relative To a match
 */
public class Player extends SocketHandler implements Game.Player {

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

    public Player(int Id, String Username, Socket Connection) {
        super(Connection, "Player" + Id);

        this.id = Id;
        this.username = Username;
    }

    /**
     * Private constructor for AI player
     */
    private Player(){
        id = -1;
        username = "Computer AI";
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
        while (listen) {
            try {
                while ((incoming = receive.readLine()) != null) {

                    // Connection closing requested From client
                    if(incoming.equals("End")){
                        Platform.runLater(() -> closeConnection(false));
                        return;
                    }

                    System.out.println("Player-" + id + ": Received <- " + incoming);

                    String[] info = incoming.split("[#]", 2);

                    if (matchId.get() == -1) {
                        GameController.getInstance().setIncoming(id, MessageType.valueOf(info[0]), info[1]);
                    } else {
                        GameController.getInstance().getMatch(matchId.get()).setIncoming(id, MessageType.valueOf(info[0]), info[1]);
                    }
                }
            }catch (Exception e){
                if(e instanceof IOException) {
                    // Handle loss of connection
                    System.err.println("Player-" + id + ": Connection lost");
                    break;
                }

                System.err.println("Player-" + id + ": Message not recognized.");
                e.printStackTrace();
            }
        }

        if(listen)
            Platform.runLater(() -> closeConnection(false));
    }

    /**
     * Gently close connection with the client
     */
    synchronized void closeConnection(boolean fromServer) {
        send.println("End");

        if(!fromServer) {
            final Match match = matchId.get() != -1 ? GameController.getInstance().getMatch(matchId.get()) : null;

            if(match != null)
                match.setIncoming(id, MessageType.GameState,
                        gson.toJson(new GameState<Player>(StateType.Abandoned, null), MessageType.GameState.getType()));
            else
                GameController.getInstance().releasePlayer(this, true);
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