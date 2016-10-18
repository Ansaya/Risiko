package Game;

import Game.Connection.GameState;
import Game.Connection.MessageType;
import Game.Map.Map;

import java.util.ArrayList;

/**
 * Match object to manage game turns in a dedicated thread
 */
public class Match extends MessageReceiver {

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
        for (Player p: Players) {
            p.initMatch(Color.next(), this.id);
            players.add(p);
        }

        // Setup and start match message receiver
        listenersInit();

        Setup();

        // Instance initial turn
    }

    /**
     * Initialize handlers for new messages and start message receiver
     */
    private void listenersInit() {
        messageHandlers.put(MessageType.Turn, (message) -> {
            // If a player notified end of his turn, go ahead with next player
            if(message.Json.equals("GoAhead")) {
                this.current = new Turn(this, nextPlaying(this.current.getPlaying()));
            }

            if(message.Json.equals("Winner")){
                String winner = message.Json.split("[-]")[0];
                players.forEach((p) -> {
                    if(p.getName().equals(winner))
                        p.SendMessage(MessageType.GameState, new GameState(StateType.Winner, null));
                    else
                        p.SendMessage(MessageType.GameState, new GameState(StateType.Looser, winner));
                });


            }
        });

        messageHandlers.put(MessageType.Chat, (message) -> {
            // Reroute message back to all players as MessageType-JsonSerializedMessage
            this.players.forEach((p) -> p.RouteMessage(message.Type + "-" + message.Json));
        });

        messageHandlers.put(MessageType.GameState, (message) -> {
            if(message.Json.equals(StateType.Abandoned.toString()))
                releasePlayer(message.PlayerId);
        });

        defaultHandler = (message) -> {
            // Any other message is routed to current turn to handle game progress
            this.current.setIncoming(message);
        };

        startListen();
    }

    /**
     * Release player from current match
     *
     * @param PlayerId Id of player to release
     */
    public void releasePlayer(int PlayerId) {
        for (Player p: players) {
            if(p.getId() != PlayerId)
                continue;

            if(p.isPlaying()){
                // Manage match interdiction

                p.exitMatch();
            }

            GameController.getInstance().returnPlayer(p);

            players.remove(p);

            return;
        }
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
            if(this.players.get(i).isPlaying())
                return this.players.get(i);
        }

        for (int i = 0; i < current; i++){
            if(this.players.get(i).isPlaying())
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
