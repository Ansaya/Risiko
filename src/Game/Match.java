package Game;

import Game.Connection.GameState;
import Game.Connection.MessageType;
import Game.Map.Map;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Match object to manage game turns in a dedicated thread
 */
public class Match extends MessageReceiver implements Runnable {

    /**
     * Match id
     */
    private int id;

    public int getId() { return id; }

    /**
     * Players' list for this match
     */
    private ArrayList<Player> players = new ArrayList<>();

    public ArrayList<Player> getPlayers() { return players; }

    /**
     * Game map
     */
    private Map gameMap = new Map();

    /**
     * Current turn
     */
    private Turn current;

    /**
     * Match events dispatcher
     */
    private Thread _instance;

    /**
     * Global matches counter
     */
    private static int counter = 0;

    /**
     * Instance a new match and starts the game
     *
     * @param Players Players who will play in this match
     */
    public Match(Player... Players) {
        if(Players.length < 2 || Players.length > 6)
            throw new UnsupportedOperationException(String.format("Not possible to start playing with %d users.", Players.length));

        this.id = counter++;

        Color.reset();

        // Add each player to the match
        for (Player p: Players
             ) {
            p.initMatch(Color.next(), this.id);
            players.add(p);
        }

        // Start match event dispatcher
        this._instance = new Thread(this);
        _instance.start();

        Setup();

        // Instance initial turn
    }

    @Override
    public void run() {

        boolean playing = true;

        while (playing) {

            try {
                // Waits for new message and stores it when it comes
                this.incomingJson.wait();
                int playerId = this.incomingPlayerId;
                MessageType type = this.incomingType;
                String json = this.incomingJson;

                // Select correct route for received message
                switch (type) {
                    case Turn:
                        // If a player notified end of his turn, go ahead with next player
                        if(json == "GoAhead") {
                            this.current = new Turn(this, nextPlaying(this.current.getPlaying()));
                        }

                        if(json == "Winner"){
                            String winner = json.split("[-]")[0];
                            players.forEach((p) -> {
                                if(p.getName() == winner)
                                    p.SendMessage(MessageType.GameState, new GameState(StateType.Winner, null));
                                else
                                    p.SendMessage(MessageType.GameState, new GameState(StateType.Looser, winner));
                            });

                            playing = false;
                            break;
                        }
                        break;
                    case Chat:
                        // Reroute message back to all players as MessageType-JsonSerializedMessage
                        this.players.forEach((p) -> p.RouteMessage(type + "-" + json));
                        break;
                    default:
                        // Any other message is routed to current turn to handle game progress
                        this.current.setIncoming(playerId, type, json);
                        break;
                }

            } catch (Exception e) {}
        }

        // Dispose all
        GameController.getInstance().endMatch(this.id);
    }

    /**
     * Get next player in turn orders
     *
     * @param lastPlaying Last player who played
     * @return Player who has to play now
     */
    public Player nextPlaying(Player lastPlaying) {
        int current = this.players.indexOf(lastPlaying);
        for(int i = current; i < players.size(); i++) {
            if(this.players.get(i).getState() == true)
                return this.players.get(i);
        }

        for (int i = 0; i < current; i++){
            if(this.players.get(i).getState() == true)
                return this.players.get(i);
        }

        return null;
    }

    /**
     * Takes care of initial armies distribution and territories choosing turns
     */
    private void Setup() {

    }
}
