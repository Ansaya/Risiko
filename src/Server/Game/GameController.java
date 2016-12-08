package Server.Game;

import Game.Color;
import Game.Connection.Chat;
import Game.Connection.Lobby;
import Game.MessageReceiver;
import Game.StateType;
import Server.Game.Connection.MessageType;
import com.google.gson.Gson;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main controller to manage matches and user lobby
 */
public class GameController extends MessageReceiver<MessageType> {

    private static GameController _instance = new GameController();

    public static GameController getInstance() { return _instance; }

    /**
     * Currently playing matches' list
     */
    private final HashMap<Integer, Match> matches = new HashMap<>();

    /**
     * Return requested match
     *
     * @param MatchId Match's id
     * @return Match with corresponding id. Null if id isn't found
     */
    public Match getMatch(int MatchId) {
        if(matches.containsKey(MatchId))
            return matches.get(MatchId);

        return null;
    }

    /**
     * Users waiting to play
     */
    private final HashMap<Integer, Player> lobby = new HashMap<>();

    private GameController() {

        // Handler for incoming chat messages routing
        messageHandlers.put(MessageType.Chat, (message) -> {
            this.lobby.forEach((id, p) -> p.RouteMessage(message.Type + "#" + message.Json));
        });

        // Handler for new match initialization request
        messageHandlers.put(MessageType.Match, (message) -> {
            final Game.Connection.Match<Player> requested = (new Gson()).fromJson(message.Json, MessageType.Match.getType());
            System.out.println("Game controller: New match request from " + message.PlayerId);

            final ArrayList<Player> toAdd = new ArrayList<>();
            requested.players.forEach((u) -> {
                toAdd.add(lobby.get(u.id));
                releasePlayer(u.id);
            });

            System.out.println("Game controller: Launch new match with " + toAdd.size() + " players.");
            final int newMatchId = Match.counter.getAndIncrement();
            matches.put(newMatchId, new Match(newMatchId, toAdd));
        });
    }

    /**
     * Starts game controller
     */
    public void init() {
        // Start message receiver
        this.startListen("GameController");

        System.out.println("Game controller: Message receiver up and running.");
    }

    /**
     * Terminate all matches and join game controller thread
     */
    public synchronized void terminate() {
        // Stop message receiver
        this.stopListen();
        System.out.println("Game controller: Message receiver stopped.");

        final Chat<Player> end = new Chat<>(Player.getAI(-1, Color.RED), "Server is shutting down.");

        // Send end message to matches players and close connection
        matches.forEach((matchId, m) -> {
            System.out.println("Game controller: Terminating match " + matchId);
            endMatch(matchId);
        });

        // Send end message and close connection of lobby players
        lobby.forEach((id, p) -> {
            System.out.println("Game controller: Releasing player " + p.getUsername());
            p.SendMessage(MessageType.Chat, end);
            p.closeConnection(true);
        });

        System.out.println("Game controller: All users released. Ready to join.");
    }

    /**
     * Add a new user to the lobby
     *
     * @param Connection Connection relative to the user
     */
    public void addPlayer(int Id, String Username, Socket Connection) {

        final Player newP = new Player(Id, Username, Connection);

        // Notify all players for new player
        lobby.forEach((id, p) -> p.SendMessage(MessageType.Lobby, new Lobby<>(newP, null)));

        lobby.put(Id, newP);

        // Notify new player for all players
        lobby.get(Id).SendMessage(MessageType.Lobby, new Lobby<>(new ArrayList<>(lobby.values()), null));

        System.out.println("Game controller: New player in lobby.");
    }

    /**
     * User gets back from match to lobby
     *
     * @param Player User to set back to lobby
     */
    public void returnPlayer(Player Player) {

        System.out.println("Game controller: Player " + Player.getUsername() + " got back from match.");

        // Notify all players for new player
        lobby.forEach((id ,p) -> p.SendMessage(MessageType.Lobby, new Lobby<>(Player, null)));

        lobby.put(Player.id, Player);

        // Notify new player for all players
        Player.SendMessage(MessageType.Lobby, new Lobby<>(new ArrayList<>(lobby.values()), null));
    }

    /**
     * User is disconnecting from game
     *
     * @param PlayerId Player to remove from lobby
     */
    void releasePlayer(int PlayerId) {
        final Player leaving = lobby.get(PlayerId);
        lobby.remove(PlayerId);
        lobby.forEach((id, p) -> p.SendMessage(MessageType.Lobby, new Lobby<>(null, leaving)));
    }

    /**
     * End specified match and removes it from match list
     *
     * @param MatchId Match to remove
     */
    protected void endMatch(int MatchId) {
        matches.get(MatchId).endMatch();
        matches.remove(MatchId);
    }
}
