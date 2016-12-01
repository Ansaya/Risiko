package Game;

import Game.Connection.*;
import com.google.gson.Gson;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main controller to manage matches and user lobby
 */
public class GameController extends MessageReceiver {

    private static GameController _instance = new GameController();

    public static GameController getInstance() { return _instance; }

    /**
     * Currently playing matches' list
     */
    private ArrayList<Match> matches = new ArrayList<>();

    /**
     * List of currently opened matches
     *
     * @return Matches' list
     */
    public ArrayList<Match> getMatches() { return matches; }

    /**
     * Return requested match
     *
     * @param MatchId Match's id
     * @return Match with corresponding id. Null if id isn't found
     */
    public Match getMatch(int MatchId) {
        for (Match p: matches
             ) {
            if(p.getId() == MatchId)
                return p;
        }

        return null;
    }

    /**
     * Users waiting to play
     */
    private HashMap<Integer, Player> lobby = new HashMap<>();

    private GameController() {
        // Handler for leaving players
        messageHandlers.put(MessageType.GameState, (message) -> {
            if(message.Json.equals(StateType.Abandoned.toString()))
                releasePlayer(message.PlayerId);
        });

        // Handler for incoming chat messages routing
        messageHandlers.put(MessageType.Chat, (message) -> {
            this.lobby.forEach((id, p) -> p.RouteMessage(message.Type + "-" + message.Json));
        });

        // Handler for new match initialization request
        messageHandlers.put(MessageType.Match, (message) -> {
            Game.Connection.Match requested = (new Gson()).fromJson(message.Json, Game.Connection.Match.class);
            System.out.println("Game controller: New match request from " + message.PlayerId);

            ArrayList<Player> toAdd = new ArrayList<>();
            requested.getPlayers().forEach((u) -> {
                toAdd.add(lobby.get(u.getUserId()));
                releasePlayer(u.getUserId());
            });

            System.out.println("Game controller: Launch new match with " + toAdd.size() + " players.");
            matches.add(new Match(toAdd));
        });
    }

    /**
     * Starts game controller
     */
    public void init() {
        // Start message receiver
        this.startListen();

        System.out.println("Game controller up and running.");
    }

    /**
     * Terminate all matches and join game controller thread
     */
    public void terminate() {
        // Stop message receiver
        this.stopListen();
        System.out.println("Game controller not listening.");

        Chat end = new Chat(new User(-1, "Admin", Color.RED), "Server is shutting down.");

        // Send end message to matches players and close connection
        for (Match m: matches) {
            System.out.println("Terminating match " + m.getId());
            m.getPlayers().forEach((id, p) -> {
                p.SendMessage(MessageType.Chat, end);
                p.closeConnection(true);
            });
        }

        // Send end message and close connection of lobby players
        System.out.println(lobby.size() + " player in lobby");
        lobby.forEach((id, p) -> {
            System.out.println("Releasing player " + p.getName());
            p.SendMessage(MessageType.Chat, end);
            p.closeConnection(true);
        });

        System.out.println("Game controller terminated.");
    }

    /**
     * Add a new user to the lobby
     *
     * @param Connection Connection relative to the user
     */
    public void addPlayer(int Id, String Username, Socket Connection) {

        Player newP = new Player(Id, Username, Connection);

        // Notify all players for new player
        lobby.forEach((id, p) -> p.SendMessage(MessageType.Lobby, new Lobby(newP, null)));

        lobby.put(Id, new Player(Id, Username, Connection));

        // Notify new player for all players
        lobby.get(Id).SendMessage(MessageType.Lobby, new Lobby(new ArrayList<>(lobby.values()), null));

        System.out.println("Game controller: New player in lobby.");
    }

    /**
     * User gets back from match to lobby
     *
     * @param Player User to set back to lobby
     */
    public void returnPlayer(Player Player) {

        System.out.println("Game controller: Player " + Player.getName() + " got back from match.");

        // Notify all players for new player
        lobby.forEach((id ,p) -> p.SendMessage(MessageType.Lobby, new Lobby(Player, null)));

        lobby.put(Player.getId(), Player);

        // Notify new player for all players
        Player.SendMessage(MessageType.Lobby, new Lobby(new ArrayList<>(lobby.values()), null));
    }

    /**
     * User is disconnecting from game
     *
     * @param PlayerId Player to remove from lobby
     */
    public void releasePlayer(int PlayerId) {
        Player leaving = lobby.get(PlayerId);
        lobby.remove(PlayerId);
        lobby.forEach((id, p) -> p.SendMessage(MessageType.Lobby, new Lobby(null, leaving)));
    }

    /**
     * Removes ended match from list
     *
     * @param MatchId Match to remove
     */
    protected void endMatch(int MatchId) {
        matches.remove(getMatch(MatchId));
    }
}
