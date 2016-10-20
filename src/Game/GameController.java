package Game;

import Game.Connection.Chat;
import Game.Connection.Lobby;
import Game.Connection.MessageType;
import com.google.gson.Gson;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

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
    private ArrayList<Player> lobby = new ArrayList<>();

    private GameController() {
        messageHandlers.put(MessageType.GameState, (message) -> {
            if(message.Json.equals(StateType.Abandoned.toString()))
                releasePlayer(message.PlayerId);
        });

        messageHandlers.put(MessageType.Chat, (message) -> {
            this.lobby.forEach((p) -> p.RouteMessage(message.Type + "-" + message.Json));
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

        Chat end = new Chat("Admin", "Server is shutting down");

        // Send end message to matches players and close connection
        for (Match m: matches) {
            System.out.println("Terminating match " + m.getId());
            m.getPlayers().forEach((p) -> {
                p.SendMessage(MessageType.Chat, end);
                p.closeConnection(true);
            });
        }

        // Send end message and close connection of lobby players
        System.out.println(lobby.size() + " player in lobby");
        for (Player p: lobby
             ) {
            System.out.println("Releasing player " + p.getName());
            p.SendMessage(MessageType.Chat, end);
            p.closeConnection(true);
        }

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
        lobby.forEach((p) -> p.SendMessage(MessageType.Lobby, new Lobby(newP, null)));

        lobby.add(newP);

        // Notify new player for all players
        newP.SendMessage(MessageType.Lobby, new Lobby(lobby, null));

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
        lobby.forEach((p) -> p.SendMessage(MessageType.Lobby, new Lobby(Player, null)));

        lobby.add(Player);

        // Notify new player for all players
        Player.SendMessage(MessageType.Lobby, new Lobby(lobby, null));
    }

    /**
     * User is disconnecting from game
     *
     * @param PlayerId Player to remove from lobby
     */
    public void releasePlayer(int PlayerId) {
        for (Player p: lobby) {
            if(p.getId() == PlayerId) {
                lobby.remove(p);
                System.out.println("Game controller: User " + p.getName() + " disconnected.");

                // Notify all players of leaving player
                lobby.forEach((l) -> l.SendMessage(MessageType.Lobby, new Lobby(null, p)));
                return;
            }
        }
    }

    /**
     * Starts a new match with passed users
     *
     * @param Players Players to add to new match
     */
    public void newMatch(Player... Players) {
        matches.add(new Match(Players));

        lobby.removeAll(Arrays.asList(Players));
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
